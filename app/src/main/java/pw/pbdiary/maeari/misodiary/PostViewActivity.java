package pw.pbdiary.maeari.misodiary;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PostViewActivity extends AppCompatActivity {

    private static final String TYPE_IMAGE = "image/*";
    private static final int INPUT_FILE_REQUEST_CODE = 1;

    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;
    WebView mWebView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postview);

        mWebView = (WebView) findViewById(R.id.postviewWebview);
        String postNumber = getIntent().getStringExtra("postNumber");
        mWebView.loadUrl("https://www.misodiary.net/post/single/"+postNumber);

        mWebView.setWebViewClient(new misoWeb4());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setAllowContentAccess(true);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setAllowFileAccessFromFileURLs(true);
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
                        Log.e(getClass().getName(), "@string/error1", ex);
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

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

                startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);
            }
        });
        SwipeRefreshLayout pullRefresh = findViewById(R.id.postRefresher);
        pullRefresh.setColorSchemeColors(getResources().getColor(R.color.colorPrimary),getResources().getColor(R.color.colorPrimaryDark),getResources().getColor(R.color.colorAccent));
        pullRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mWebView.reload();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
    public class misoWeb4 extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest urls) {
            String url = urls.getUrl().toString();
            if (url.startsWith("https://www.misodiary.net/main")) {
                if (url.startsWith("https://www.misodiary.net/main/opench/?keyword=")) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("openKeyword", url);
                    String status = "opench";
                    intent.putExtra("status", status);
                } else if (url.startsWith("https://www.misodiary.net/main/random_friends")) {
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    String status = "michinrandom";
                    i.putExtra("status", status);
                    startActivity(i);
                } else {
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);
                }
            } else if (url.startsWith("https://www.misodiary.net/home/dashboard")) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                String status = "profile";
                i.putExtra("status", status);
                startActivity(i);
            } else if (url.startsWith("https://www.misodiary.net/member/notification")) {
                Intent intent = new Intent(getApplicationContext(), NotiActivity.class);
                startActivity(intent);
            } else if (url.startsWith("https://www.misodiary.net/member/setting")) {
                Intent i = new Intent(getApplicationContext(), MyAccountActivity.class);
                startActivity(i);
            } else if (url.startsWith("https://www.misodiary.net/post/search")) {
                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                String keyword = url.replace("https://www.misodiary.net/post/search/", "");
                intent.putExtra("keyword", keyword);
                startActivity(intent);
            } else if (url.startsWith("https://www.misodiary.net/post/single")) {
                String postNumber = url.replace("https://www.misodiary/net/post/search/","");
                mWebView.loadUrl("https://www.misodiary.net/post/single/"+postNumber);
            } else if(url.startsWith("https://www.misodiary.net/home/main")) {
                Intent intent = new Intent(getApplicationContext(),ProfileViewActivity.class);
                String accountID = url.replace("https://www.misodiary.net/home/main/","");
                intent.putExtra("accountID",accountID);
                startActivity(intent);
            } else if(url.startsWith("https://www.misodiary.net/member/login")) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            } else if(url.startsWith("https://www.misodiary.net")||url.startsWith("http://www.misodiary.net")){
                if(url.equals("https://www.misodiary.net")||url.equals("https://www.misodiary.net/")||url.equals("http://www.misodiary.net")||url.equals("http://www.misodiary.net/")) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    String status = "opench";
                    intent.putExtra("status", status);
                }
            } else {
                try {
                    misoCustomTab c = new misoCustomTab();
                    c.launch(PostViewActivity.this,url);
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            view.loadUrl("javascript:document.getElementsByClassName('navbar')[0].remove()");
            SwipeRefreshLayout pullRefresh = findViewById(R.id.postRefresher);
            pullRefresh.setRefreshing(false);
            SharedPreferences cookie = getSharedPreferences("cookie", Context.MODE_PRIVATE);
            CookieManager cM = CookieManager.getInstance();
            cM.setAcceptCookie(true);
            if(cookie.getString("cookie","") != null) {
                cM.setCookie("www.misodiary.net",cookie.getString("cookie",""));
            }
        }
        @Override
        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(PostViewActivity.this);
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
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return imageFile;
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

    public void onBackButtonPressed(View v){
        onBackPressed();
    }
}
