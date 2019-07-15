package pw.pbdiary.maeari.misodiary;

import androidx.annotation.Nullable;
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
import android.os.Message;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class NotiActivity extends AppCompatActivity {

    WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noti);
        mWebView = (WebView) findViewById(R.id.notiWebView);
        mWebView.setWebChromeClient(new WebChromeClient() {
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
        });
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new misoWeb3());
        mWebView.loadUrl("http://www.misodiary.net/notification");
        SwipeRefreshLayout pullrefresh = findViewById(R.id.notiRefresher);
        pullrefresh.setColorSchemeColors(getResources().getColor(R.color.colorPrimary),getResources().getColor(R.color.colorPrimaryDark),getResources().getColor(R.color.colorAccent));
        pullrefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mWebView.reload();
            }
        });
    }
    public class misoWeb3 extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest urls) {
            String url = urls.getUrl().toString();
            if (url.startsWith("http://www.misodiary.net/board")) {
                if (url.startsWith("http://www.misodiary.net/board/daytime")) {
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    String status = "profile";
                    i.putExtra("status",status);
                    startActivity(i);
                } else if (url.startsWith("http://www.misodiary.net/findfriend")) {
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    String status = "michinrandom";
                    i.putExtra("status",status);
                    startActivity(i);
                } else if (url.startsWith("http://www.misodiary.net/board")) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("openKeyword", url);
                } else {
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);
                }
            } else if(url.startsWith("http://www.misodiary.net/search?skeyword=")) {
                Intent intent = new Intent(getApplicationContext(),SearchActivity.class);
                String keyword = url.replace("http://www.misodiary.net/search?skeyword=","");
                intent.putExtra("keyword",keyword);
                startActivity(intent);
            } else if(url.startsWith("http://www.misodiary.net/post")) {
                Intent intent = new Intent(getApplicationContext(),PostViewActivity.class);
                String postNumber = url.replace("http://www.misodiary.net/post/","");
                intent.putExtra("postNumber",postNumber);
                startActivity(intent);
            } else if(url.startsWith("http://www.misodiary.net/mypage")) {
                Intent intent = new Intent(getApplicationContext(),ProfileViewActivity.class);
                String accountID = url.replace("http://www.misodiary.net/mypage","");
                intent.putExtra("accountID",accountID);
                startActivity(intent);
            } else if(url.startsWith("http://www.misodiary.net/login")) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivityForResult(intent,2);
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
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            view.loadUrl("javascript:document.getElementsByClassName('navbar')[0].remove()");
            SwipeRefreshLayout pullRefresh = findViewById(R.id.notiRefresher);
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
            final AlertDialog.Builder builder = new AlertDialog.Builder(NotiActivity.this);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 2 && resultCode==RESULT_OK) {
            if(data.getStringExtra("cookie")!=null) {
                CookieManager cM = CookieManager.getInstance();
                cM.setCookie("www.misodiary.net",data.getStringExtra("cookie"));
                mWebView.loadUrl("http://www.misodiary.net/notification");
            } else {
                finish();
            }
        } else {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebView.loadUrl("http://www.misodiary.net/notification");
    }
}
