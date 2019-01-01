package pw.pbdiary.maeari.misodiary;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toolbar;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {
    WebView mWebView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mWebView = findViewById(R.id.loginWebView);
        mWebView.setWebViewClient(new loginweb());
        mWebView.loadUrl("https://www.misodiary.net/member/login");
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);

    }

    public class loginweb extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            view.loadUrl("javascript:document.getElementsByClassName('navbar')[0].remove()");
        }

        @Override
        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
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
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            if (!url.startsWith("https://www.misodiary.net/member")) {
                CookieManager cM = CookieManager.getInstance();
                SharedPreferences cookie = getSharedPreferences("cookie", Context.MODE_PRIVATE);
                String cookieS = cM.getCookie("www.misodiary.net");
                Log.d("COOKIE", cM.getCookie("www.misodiary.net"));
                SharedPreferences.Editor editor = cookie.edit();
                editor.putString("cookie",cookieS);
                editor.apply();
            }
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
                view.loadUrl(url);
            } else if (url.startsWith("https://www.misodiary.net/post/search")) {
                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                String keyword = url.replace("https://www.misodiary.net/post/search/", "");
                intent.putExtra("keyword", keyword);
                startActivity(intent);
            } else if (url.startsWith("https://www.misodiary.net/post/single")) {
                Intent intent = new Intent(getApplicationContext(),PostViewActivity.class);
                String postNumber = url.replace("https://www.misodiary.net/post/single/","");
                intent.putExtra("postNumber",postNumber);
                startActivity(intent);
            } else if(url.startsWith("https://www.misodiary.net/home/main")) {
                Intent intent = new Intent(getApplicationContext(),ProfileViewActivity.class);
                String accountID = url.replace("https://www.misodiary.net/home/main/","");
                intent.putExtra("accountID",accountID);
                startActivity(intent);
            } else if(url.startsWith("https://www.misodiary.net")||url.startsWith("http://www.misodiary.net")){
                if(url.equals("https://www.misodiary.net")||url.equals("https://www.misodiary.net/")||url.equals("http://www.misodiary.net")||url.equals("http://www.misodiary.net/")) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    String status = "opench";
                    intent.putExtra("status", status);
                }
            } else {
                try {
                    Intent bi = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(bi);
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return super.shouldOverrideUrlLoading(view, request);
        }
    }

    public void onBackatLogin(View view) {
        finish();
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(mWebView.canGoBack()) {
                mWebView.goBack();
            } else {
                finish();
            }
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
