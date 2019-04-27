package pw.pbdiary.maeari.misodiary;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebHistoryItem;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends AppCompatActivity implements OnKeyboardVisibilityListener{

    //액티비티 내에서 사용하는 항목 불러오기 위한 변수 지정 작업
    public TextView mTextMessage;
    public WebView mWebView;
    //파일 업로드를 위한 변수
    private static final String TYPE_IMAGE = "image/*";
    private static final int INPUT_FILE_REQUEST_CODE = 1;
    private static final int LOGIN_REQUEST_CODE_MAIN = 2;
    private static final int FIRST_START_REQUEST_CODE = 3;
    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;
    //첫실행 여부 저장?
    public static final String saveFirst ="saveFirst";
    //뒤로가기 두 번으로 종료하기 위한 부분
    //private long pressedTime = 0;

    //처음 액티비티 실행될 때
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomAppBar mAppBar = findViewById(R.id.navigation);
        setSupportActionBar(mAppBar);
        //For My Profile.
        SharedPreferences forProfileOnly= getSharedPreferences("ifP",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor2 = forProfileOnly.edit();
        editor2.putString("ifP","false");
        editor2.apply();
        //pressedTime = System.currentTimeMillis();
        SharedPreferences sp = getSharedPreferences(saveFirst, Context.MODE_PRIVATE);
        if (!sp.getBoolean("first",false)) {
            Intent intent = new Intent(getApplicationContext(),PermissionCheckInfo.class);
            startActivityForResult(intent,FIRST_START_REQUEST_CODE);
        }
        mTextMessage = (TextView) findViewById(R.id.title_main);
        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.setWebViewClient(new misoWeb());
        SharedPreferences cookie = getSharedPreferences("cookie",Context.MODE_PRIVATE);
        CookieManager cM = CookieManager.getInstance();
        if(cookie.getString("cookie","") != null) {
            Log.d("COOKIE",cookie.getString("cookie",""));
            cM.setCookie("www.misodiary.net",cookie.getString("cookie",""));
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
        if(getIntent().getStringExtra("status") != null) {
            String status = getIntent().getStringExtra("status");
            switch (status) {
                case "opench":
                    mTextMessage.setText(R.string.title_opench);
                    mWebView.loadUrl("https://www.misodiary.net/main/opench");
                    break;
                case "michinrandom":
                    mTextMessage.setText(R.string.title_michinrandom);
                    mWebView.loadUrl("https://www.misodiary.net/main/random_friends");
                    break;
                case "profile":
                    mTextMessage.setText(R.string.title_profile);
                    mWebView.loadUrl("https://www.misodiary.net/home/dashboard");
                    break;
            }
        } else {
            SharedPreferences mainscdefault = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences statusSave= getSharedPreferences("status",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = statusSave.edit();
            switch (Objects.requireNonNull(mainscdefault.getString("screendefault", "opench"))){
                case "opench":
                    editor.putString("status","opench");
                    editor.apply();
                    mTextMessage.setText(R.string.title_opench);
                    mWebView.loadUrl("https://www.misodiary.net/main/opench");
                    break;
                case "michinrandom":
                    editor.putString("status","michinrandom");
                    editor.apply();
                    mTextMessage.setText(R.string.title_michinrandom);
                    mWebView.loadUrl("https://www.misodiary.net/main/random_friends");
                    break;
                case "profile":
                    editor.putString("status","profile");
                    editor.apply();
                    mTextMessage.setText(R.string.title_profile);
                    mWebView.loadUrl("https://www.misodiary.net/home/dashboard");
                    break;
                default:
                    mTextMessage.setText(R.string.title_opench);
                    mWebView.loadUrl("https://www.misodiary.net/main/opench");
                    break;
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

        //키보드 올라올 때 앱 바 가리기
        setKeyboardVisibilityListener(this);
    }

    private void setKeyboardVisibilityListener(final OnKeyboardVisibilityListener onKeyboardVisibilityListener) {
        final View parentView = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        parentView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            private boolean alreadyOpen;
            private final int defaultKeyboardHeightDP = 100;
            private final int EstimatedKeyboardDP = defaultKeyboardHeightDP + (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? 48 : 0);
            private final Rect rect = new Rect();

            @Override
            public void onGlobalLayout() {
                int estimatedKeyboardHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, EstimatedKeyboardDP, parentView.getResources().getDisplayMetrics());
                parentView.getWindowVisibleDisplayFrame(rect);
                int heightDiff = parentView.getRootView().getHeight() - (rect.bottom - rect.top);
                boolean isShown = heightDiff >= estimatedKeyboardHeight;

                if (isShown == alreadyOpen) {
                    Log.i("Keyboard state", "Ignoring global layout change...");
                    return;
                }
                alreadyOpen = isShown;
                onKeyboardVisibilityListener.onVisibilityChanged(isShown);
            }
        });
    }

    @Override
    public void onVisibilityChanged(boolean visible) {
        BottomAppBar bab = findViewById(R.id.navigation);
        FloatingActionButton fab = findViewById(R.id.newArticle);
        if(visible) {
            bab.setVisibility(View.GONE);
            fab.hide();
        } else {
            bab.setVisibility(View.VISIBLE);
            fab.show();
        }
    }

    public class misoWeb extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest urls) {
            String url = urls.getUrl().toString();
            if(url.startsWith("https://www.misodiary.net/member/notification")) {
                view.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(getApplicationContext(), NotiActivity.class);
                startActivity(intent);
            } else if(url.startsWith("https://www.misodiary.net/post/search/")) {
                view.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(getApplicationContext(),SearchActivity.class);
                String keyword = url.replace("https://www.misodiary.net/post/search/","");
                intent.putExtra("keyword",keyword);
                startActivity(intent);
            } else if(url.startsWith("https://www.misodiary.net/post/single")) {
                view.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(getApplicationContext(),PostViewActivity.class);
                String postNumber = url.replace("https://www.misodiary.net/post/single/","");
                intent.putExtra("postNumber",postNumber);
                startActivity(intent);
            } else if(url.startsWith("https://www.misodiary.net/home/main")) {
                view.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(getApplicationContext(),ProfileViewActivity.class);
                String accountID = url.replace("https://www.misodiary.net/home/main/","");
                intent.putExtra("accountID",accountID);
                startActivity(intent);
            } else if(url.startsWith("https://www.misodiary.net/member/login")) {
                view.setVisibility(View.INVISIBLE);
            } else if(url.startsWith("https://www.misodiary.net")) {
                view.loadUrl(url);
            } else {
                misoCustomTab c = new misoCustomTab();
                c.launch(MainActivity.this,url);
            }
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if(url.startsWith("https://www.misodiary.net/main/opench")) {
                mTextMessage.setText(R.string.title_opench);
                SharedPreferences statusSave= getSharedPreferences("status",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = statusSave.edit();
                editor.putString("status","opench");
                editor.apply();
            } else if(url.startsWith("https://www.misodiary.net/main/random_friends")) {
                mTextMessage.setText(R.string.title_michinrandom);
                SharedPreferences statusSave= getSharedPreferences("status",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = statusSave.edit();
                editor.putString("status","michinrandom");
                editor.apply();
            } else if(url.startsWith("https://www.misodiary.net/home/dashboard")) {
                mTextMessage.setText(R.string.title_profile);
                SharedPreferences statusSave= getSharedPreferences("status",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = statusSave.edit();
                editor.putString("status","profile");
                editor.apply();
            } else if(url.startsWith("https://www.misodiary.net/member/login")) {
                SharedPreferences statusRequest = getSharedPreferences("status",Context.MODE_PRIVATE);
                Intent loginIntent = new Intent(getApplicationContext(),LoginActivity.class);
                loginIntent.putExtra("status",statusRequest.getString("status","opench"));
                startActivityForResult(loginIntent,LOGIN_REQUEST_CODE_MAIN);
            } else if(url.startsWith("https://www.misodiary.net")||url.startsWith("http://www.misodiary.net")){
                if(url.equals("https://www.misodiary.net")||url.equals("https://www.misodiary.net/")||url.equals("http://www.misodiary.net")||url.equals("http://www.misodiary.net/")) {
                    mTextMessage.setText(R.string.title_opench);
                }
            }
            view.loadUrl("javascript:document.getElementsByClassName('navbar')[0].remove()");
            SwipeRefreshLayout pullRefresh = findViewById(R.id.swipeMain);
            pullRefresh.setRefreshing(false);
            SharedPreferences cookie = getSharedPreferences("cookie",Context.MODE_PRIVATE);
            CookieManager cM = CookieManager.getInstance();
            cM.setAcceptCookie(true);
            if(cookie.getString("cookie","") != null) {
                cM.setCookie("www.misodiary.net",cookie.getString("cookie",""));
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
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(intent);
    }
    public void onProfileClicked(View view) {
        SharedPreferences statusSave= getSharedPreferences("status",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = statusSave.edit();
        editor.putString("status","profile");
        editor.apply();
        SharedPreferences forProfileOnly= getSharedPreferences("ifP",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor2 = forProfileOnly.edit();
        editor2.putString("ifP","true");
        editor2.apply();
        mWebView.loadUrl("https://www.misodiary.net/home/dashboard");

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            BottomMenuFragment bmf = new BottomMenuFragment();
            bmf.show(getSupportFragmentManager(),bmf.getTag());
        }
        return super.onOptionsItemSelected(item);
    }

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
        if (requestCode == LOGIN_REQUEST_CODE_MAIN && resultCode == RESULT_OK) {
            super.onActivityResult(requestCode, resultCode, data);
            mWebView.setVisibility(View.VISIBLE);
            if (data.getStringExtra("cookie") == null) {
                mWebView.goBack();
            } else {
                SharedPreferences mainscdefault = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences forProfile = getSharedPreferences("ifP",Context.MODE_PRIVATE);
                CookieManager cM = CookieManager.getInstance();
                cM.setCookie("www.misodiary.net", data.getStringExtra("cookie"));
                if (data.getStringExtra("status") != null) {
                    String status = getIntent().getStringExtra("status");
                    switch (status) {
                        case "opench":
                            mTextMessage.setText(R.string.title_opench);
                            if (Objects.requireNonNull(mainscdefault.getString("screendefault", "opench")).equals("profile")|| Objects.requireNonNull(forProfile.getString("ifP", "false")).equals("true")) {
                                mTextMessage.setText(R.string.title_profile);
                                mWebView.loadUrl("https://www.misodiary.net/home/dashboard");
                                SharedPreferences.Editor editor = forProfile.edit();
                                editor.putString("ifP","false");
                                editor.apply();
                            } else {
                                mWebView.loadUrl("https://www.misodiary.net/");
                            }
                            break;
                        case "michinrandom":
                            mTextMessage.setText(R.string.title_michinrandom);
                            mWebView.loadUrl("https://www.misodiary.net/main/random_friends");
                            break;
                        case "profile":
                            mTextMessage.setText(R.string.title_profile);
                            mWebView.loadUrl("https://www.misodiary.net/home/dashboard");
                            break;
                    }
                } else {
                    if (Objects.requireNonNull(mainscdefault.getString("screendefault", "opench")).equals("profile") || Objects.requireNonNull(forProfile.getString("ifP", "false")).equals("true")) {
                        mTextMessage.setText(R.string.title_profile);
                        mWebView.loadUrl("https://www.misodiary.net/home/dashboard");
                        SharedPreferences.Editor editor = forProfile.edit();
                        editor.putString("ifP","false");
                        editor.apply();
                    } else {
                        mWebView.loadUrl("https://www.misodiary.net/");
                    }
                }
            }
        } else if(requestCode != FIRST_START_REQUEST_CODE && resultCode != RESULT_OK) {
            finish();
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
        mWebView = findViewById(R.id.webView);
        mWebView.setVisibility(View.VISIBLE);
        SharedPreferences cookie = getSharedPreferences("cookie",Context.MODE_PRIVATE);
        CookieManager cM = CookieManager.getInstance();
        if(cookie.getString("cookie","") != null) {
            cM.setCookie("www.misodiary.net",cookie.getString("cookie",""));
        }
        super.onResume();
    }



    //뒤로가기 누를때
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mTextMessage = findViewById(R.id.title_main);
            if(mWebView.canGoBack()) {
                backControl(mWebView);
            } else {
                finish();
            }
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void backControl(WebView mWebView) {
        mWebView.goBack();
        String url = mWebView.getOriginalUrl();
        WebBackForwardList currentHistory = mWebView.copyBackForwardList();
        WebHistoryItem prevURL = currentHistory.getItemAtIndex(currentHistory.getCurrentIndex()-1);
        if(url.startsWith("https://www.misodiary.net/main/opench")) {
            mTextMessage.setText(R.string.title_opench);
        } else if(url.startsWith("https://www.misodiary.net/main/random_friends")) {
            mTextMessage.setText(R.string.title_michinrandom);
        } else if(url.startsWith("https://www.misodiary.net/home/dashboard")) {
            mTextMessage.setText(R.string.title_profile);
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
        } /* else if(url.startsWith("https://www.misodiary.net/member/login")) {
            mWebView.goBackOrForward(mWebView.copyBackForwardList().getCurrentIndex()-2);
            backControl(mWebView);
        } */ else if(url.startsWith("https://www.misodiary.net")||url.startsWith("http://www.misodiary.net")){
            if(url.equals("https://www.misodiary.net")||url.equals("https://www.misodiary.net/")||url.equals("http://www.misodiary.net")||url.equals("http://www.misodiary.net/")) {
                mTextMessage.setText(R.string.title_opench);
            }
        } else {
            try {
                Intent bi = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(bi);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
