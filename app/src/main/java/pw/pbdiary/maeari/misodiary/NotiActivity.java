package pw.pbdiary.maeari.misodiary;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class NotiActivity extends AppCompatActivity {

    WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noti);
        mWebView = (WebView) findViewById(R.id.notiWebView);
        mWebView.setWebViewClient(new misoWeb3());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl("https://www.misodiary.net/member/notification");
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
            if (url.startsWith("https://www.misodiary.net/main")) {
                if (url.startsWith("https://www.misodiary.net/main/opench/?keyword=")) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("openKeyword", url);
                } else if (url.startsWith("https://www.misodiary.net/main/random_friends")) {
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    String status = "michinrandom";
                    i.putExtra("status",status);
                    startActivity(i);
                } else {
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);
                }
            } else if (url.startsWith("https://www.misodiary.net/home/dashboard")) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                String status = "profile";
                i.putExtra("status",status);
                startActivity(i);
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
            } else if(url.startsWith("https://www.misodiary.net/member/login")) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            } else if(url.startsWith("https://www.misodiary.net")||url.startsWith("http://www.misodiary.net")){
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
            SwipeRefreshLayout pullRefresh = findViewById(R.id.notiRefresher);
            pullRefresh.setRefreshing(false);
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
}
