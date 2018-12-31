package pw.pbdiary.maeari.misodiary;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends AppCompatActivity {

    //액티비티 내에서 사용하는 항목 불러오기 위한 변수 지정 작업
    private TextView mTextMessage;
    private BottomNavigationView mBottomNavigationView;
    private WebView mWebView;
    //파일 업로드를 위한 변수
    private static final String TYPE_IMAGE = "image/*";
    private static final int INPUT_FILE_REQUEST_CODE = 1;
    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;
    //첫실행 여부 저장?
    public static final String saveFirst ="saveFirst";
    //뒤로가기 두 번으로 종료하기 위한 부분
    //private long pressedTime = 0;

    //네비게이션 바 눌렸을 때 반응할 코드
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.nav_opench:
                    mTextMessage.setText(R.string.title_home);
                    mWebView.loadUrl("https://www.misodiary.net/main/opench/");
                    return true;
                case R.id.nav_michinrandom:
                    mTextMessage.setText(R.string.title_michinrandom);
                    mWebView.loadUrl("https://www.misodiary.net/main/random_friends");
                    return true;
                case R.id.nav_profile:
                    mTextMessage.setText(R.string.title_profile);
                    mWebView.loadUrl("https://www.misodiary.net/home/dashboard");
                    return true;
            }
            return false;
        }
    };

    //처음 액티비티 실행될 때
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //pressedTime = System.currentTimeMillis();
        SharedPreferences sp = getSharedPreferences(saveFirst, Context.MODE_PRIVATE);
        if (!sp.getBoolean("first",false)) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("first",true);
            editor.apply();
            Intent intent = new Intent(this, PermissionCheckInfo.class);
            startActivity(intent);
        }
        SharedPreferences cookie = getSharedPreferences("cookie",Context.MODE_PRIVATE);
        mTextMessage = (TextView) findViewById(R.id.message);
        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.setWebViewClient(new misoWeb());
        CookieManager cM = CookieManager.getInstance();
        cM.setAcceptCookie(true);
        if(cookie.getString("cookie","") != null) {
            cM.setCookie("www.misodiary.net",cookie.getString("cookie","")+"; Expires=Fri, 31 Dec 2100 00:00:00 KST;");
        }
        //웹 뷰 설정
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setAllowContentAccess(true);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setAllowFileAccessFromFileURLs(true);
        //파일 업로드 기능 추가
        mWebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onCloseWindow(WebView w) {
                super.onCloseWindow(w);
                finish();
            }

            @Override
            public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg) {
                final WebSettings settings = view.getSettings();
                settings.setDomStorageEnabled(true);
                settings.setJavaScriptEnabled(true);
                settings.setAllowFileAccess(true);
                settings.setAllowContentAccess(true);
                view.setWebChromeClient(this);
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(view);
                resultMsg.sendToTarget();
                return false;
            }

            public boolean onShowFileChooser(WebView webView,
                                             ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (mFilePathCallback != null) {
                    mFilePathCallback.onReceiveValue(null);
                }
                mFilePathCallback = filePathCallback;
                imageChooser();
                return true;
            }

            private void imageChooser() {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                        takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        String error1 = getResources().getString(R.string.error1);
                        Log.e(getClass().getName(), error1, ex);
                    }

                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        mCameraPhotoPath = "file:"+photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile));
                    } else {
                        takePictureIntent = null;
                    }
                }

                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType(TYPE_IMAGE);

                Intent[] intentArray;
                if(takePictureIntent != null) {
                    intentArray = new Intent[]{takePictureIntent};
                } else {
                    intentArray = new Intent[0];
                }
                String ImageCapture = getResources().getString(R.string.ImageCapture);
                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, ImageCapture);
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

                startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);
            }
        });
        mWebView.loadUrl("https://www.misodiary.net");
        mBottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        mBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        String status = getIntent().getStringExtra("status");

        if(status != null) {
            if (status.equals("profile")) {
                mBottomNavigationView.getMenu().findItem(R.id.nav_profile).setChecked(true);
                mTextMessage.setText(R.string.title_profile);
                mWebView.loadUrl("https://www.misodiary.net/home/dashboard");
            } else if (status.equals("opench")) {
                String openKeyword = getIntent().getStringExtra("openKeyword");
                mTextMessage.setText(R.string.title_opench);
                mBottomNavigationView.getMenu().findItem(R.id.nav_opench).setChecked(true);
                mWebView.loadUrl(openKeyword);
            } else if (status.equals("michinrandom")) {
                mBottomNavigationView.getMenu().findItem(R.id.nav_michinrandom).setChecked(true);
                mTextMessage.setText(R.string.title_michinrandom);
                mWebView.loadUrl("https://www.misodiary.net/main/random_friends");
            }
        }

        SwipeRefreshLayout pullRefresh = findViewById(R.id.swipeMain);
        pullRefresh.setColorSchemeColors(getResources().getColor(R.color.colorPrimary),getResources().getColor(R.color.colorPrimaryDark),getResources().getColor(R.color.colorAccent));
        pullRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mWebView.reload();
            }
        });
    }

    public class misoWeb extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest urls) {
            String url = urls.getUrl().toString();
            if(url.startsWith("https://www.misodiary.net/main/opench")) {
                mBottomNavigationView.getMenu().findItem(R.id.nav_opench).setChecked(true);
                mTextMessage.setText(R.string.title_opench);
                view.loadUrl(url);
            } else if(url.startsWith("https://www.misodiary.net/main/random_friends")) {
                mTextMessage.setText(R.string.title_michinrandom);
                mBottomNavigationView.getMenu().findItem(R.id.nav_michinrandom).setChecked(true);
                view.loadUrl(url);
            } else if(url.startsWith("https://www.misodiary.net/home/dashboard")) {
                mTextMessage.setText(R.string.title_profile);
                mBottomNavigationView.getMenu().findItem(R.id.nav_profile).setChecked(true);
                view.loadUrl(url);
            } else if(url.startsWith("https://www.misodiary.net/member/notification")) {
                Intent intent = new Intent(getApplicationContext(), NotiActivity.class);
                startActivity(intent);
            } else if(url.startsWith("https://www.misodiary.net/post/search/")) {
                Intent intent = new Intent(getApplicationContext(),SearchActivity.class);
                String keyword = url.replace("https://www.misodiary.net/post/search/","");
                intent.putExtra("keyword",keyword);
                startActivity(intent);
            } else if(url.startsWith("https://www.misodiary.net/post/single")) {
                Intent intent = new Intent(getApplicationContext(),PostViewActivity.class);
                String postNumber = url.replace("https://www.misodiary.net/post/single/","");
                intent.putExtra("postNumber",postNumber);
                startActivity(intent);
            } else if(url.startsWith("https://www.misodiary.net/home/main")) {
                Intent intent = new Intent(getApplicationContext(),ProfileViewActivity.class);
                String accountID = url.replace("https://www.misodiary.net/home/main/","");
                intent.putExtra("accountID",accountID);
                startActivity(intent);
            } else if(url.startsWith("https://www.misodiary.net")){
                view.loadUrl(url);
            } else {
                try {
                    Intent bi = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(bi);
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            view.loadUrl("javascript:document.getElementsByClassName('navbar')[0].remove()");
            SwipeRefreshLayout pullRefresh = findViewById(R.id.swipeMain);
            pullRefresh.setRefreshing(false);
            if(url.equals("https://www.misodiary.net/home/dashboard")) {
                CookieManager cM = CookieManager.getInstance();
                SharedPreferences cookie = getSharedPreferences("cookie",Context.MODE_PRIVATE);
                String cookieS = cM.getCookie("www.misodiary.net");
                Log.d("COOKIE", cM.getCookie("www.misodiary.net"));
                SharedPreferences.Editor editor = cookie.edit();
                editor.putString("cookie",cookieS);
                editor.apply();
            } else {
                CookieManager cM = CookieManager.getInstance();
                SharedPreferences cookie = getSharedPreferences("cookie",Context.MODE_PRIVATE);
                cM.setCookie("www.misodiary.net",cookie.getString("cookie","")+"; Expires=Fri, 31 Dec 2100 00:00:00 KST;");
            }
        }
        @Override
        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            String message = getResources().getString(R.string.notification_error_ssl_cert_invalid);
            switch (error.getPrimaryError()) {
                case SslError.SSL_UNTRUSTED:
                    message = getResources().getString(R.string.notification_error_ssl_authority_untrusted);
                    break;
                case SslError.SSL_EXPIRED:
                    message = getResources().getString(R.string.notification_error_ssl_cert_expired);
                    break;
                case SslError.SSL_IDMISMATCH:
                    message = getResources().getString(R.string.notification_error_ssl_hostname_invalid);
                    break;
                case SslError.SSL_NOTYETVALID:
                    message = getResources().getString(R.string.notification_error_ssl_time_yet);
                    break;
            }
            message += getResources().getString(R.string.continue_anyway);

            builder.setTitle(getResources().getString(R.string.notification_error_ssl_cert_invalid));
            builder.setMessage(message);
            builder.setPositiveButton(getResources().getString(R.string.error_continue), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handler.proceed();
                }
            });
            builder.setNegativeButton(getResources().getString(R.string.error_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handler.cancel();
                }
            });
            final AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    public void onSearchClicked(View v) {
        Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
        startActivity(intent);
    }
    public void onNotiClicked(View v) {
        Intent intent = new Intent(getApplicationContext(), NotiActivity.class);
        startActivity(intent);
    }
    public void onMyAccountClicked(View v) {
        Intent intent = new Intent(getApplicationContext(), MyAccountActivity.class);
        startActivity(intent);
    }
    public void onAppInfoClicked(View v) {
        Intent intent = new Intent(getApplicationContext(), AppInfoActivity.class);
        startActivity(intent);
    }

    /* @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() > pressedTime + 2000) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_back), Toast.LENGTH_LONG).show();
            return;
        } else if (System.currentTimeMillis() <= pressedTime + 2000) {
            super.onBackPressed();
        }
    } */

    /* @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if(keyCode == KeyEvent.KEYCODE_BACK) {
                if (System.currentTimeMillis() > pressedTime + 2000) {
                    pressedTime = System.currentTimeMillis();
                    if (mWebView.canGoBack()) {
                        String url = mWebView.copyBackForwardList().getItemAtIndex(mWebView.copyBackForwardList().getCurrentIndex() - 1).getUrl();
                        if (url.startsWith("https://www.misodiary.net/main/opench")) {
                            mBottomNavigationView.getMenu().findItem(R.id.nav_opench).setChecked(true);
                            mWebView.loadUrl(url);
                        } else if (url.startsWith("https://www.misodiary.net/main/random_friends")) {
                            mBottomNavigationView.getMenu().findItem(R.id.nav_michinrandom).setChecked(true);
                            mWebView.loadUrl(url);
                        } else if (url.startsWith("https://www.misodiary.net/home/dashboard")) {
                            mBottomNavigationView.getMenu().findItem(R.id.nav_profile).setChecked(true);
                            mWebView.loadUrl(url);
                        } else {
                            mWebView.loadUrl(url);
                        }
                    } else {
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.error_back),Toast.LENGTH_LONG).show();
                    }
                }
                if (System.currentTimeMillis() <= pressedTime +2000) {
                    finish();
                }
            }

        }
        return super.onKeyDown(keyCode, event);
    } */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INPUT_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (mFilePathCallback == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }
            Uri[] results = new Uri[]{getResultUri(data)};

            mFilePathCallback.onReceiveValue(results);
            mFilePathCallback = null;
        } else {
            if (mFilePathCallback != null) mFilePathCallback.onReceiveValue(null);
            if (mUploadMessage != null) mUploadMessage.onReceiveValue(null);
            mFilePathCallback = null;
            mUploadMessage = null;
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private Uri getResultUri(Intent data) {
        Uri result = null;
        if(data == null || TextUtils.isEmpty(data.getDataString())) {
            if(mCameraPhotoPath != null) {
                result = Uri.parse(mCameraPhotoPath);
            }
        } else {
            String filePath = "";
            filePath = data.getDataString();
            result = Uri.parse(filePath);
        }

        return result;
    }

    @Override
    protected void onResume() {
        CookieManager cM = CookieManager.getInstance();
        super.onResume();
    }
}
