package pw.pbdiary.maeari.misodiary;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.Objects;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    WebView mWebView;
    MaterialButton misoLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        misoLogin = findViewById(R.id.misoLogin);
        mWebView = findViewById(R.id.loginWebView);
        mWebView.setWebViewClient(new loginweb());
        mWebView.loadUrl("https://www.misodiary.net/member/login");
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        TextInputEditText mPWField = findViewById(R.id.misoPWField);
        mPWField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mPWField.setTransformationMethod(PasswordTransformationMethod.getInstance());
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        mPWField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE) {
                    misoLogin.performClick();
                    return true;
                }
                return false;
            }
        });
    }
    public void onFindIDPWClicked(View view) {
        misoCustomTab c = new misoCustomTab();
        c.launch(LoginActivity.this,"https://www.misodiary.net/pages/id_lost");
    }
    public void onRegisterClicked(View view) {
        misoCustomTab c = new misoCustomTab();
        c.launch(LoginActivity.this,"https://www.misodiary.net/member/register");
    }

    public void onPreviousClicked(View view) {
        TextInputLayout mIDLayout = findViewById(R.id.misoID);
        TextInputLayout mPWLayout = findViewById(R.id.misoPW);
        MaterialButton mLoginButton = findViewById(R.id.misoLogin);
        Button mFindSome = findViewById(R.id.misoFindIDPW);
        Button mRegisterButton = findViewById(R.id.misoRegister);
        ConstraintLayout mToggle = findViewById(R.id.logintoggle);
        WebView mWebView = findViewById(R.id.loginWebView);
        Button mNewLogin = findViewById(R.id.misoNewLogin);
        //CheckBox mAutoLogin = findViewById(R.id.autologincb);
        ConstraintLayout mPreviousLayout = findViewById(R.id.previousLayout);
        mIDLayout.setVisibility(View.GONE);
        mPWLayout.setVisibility(View.GONE);
        mLoginButton.setVisibility(View.GONE);
        mFindSome.setVisibility(View.GONE);
        mRegisterButton.setVisibility(View.GONE);
        mToggle.setVisibility(View.GONE);
        mWebView.setVisibility(View.VISIBLE);
        mNewLogin.setVisibility(View.VISIBLE);
        //mAutoLogin.setVisibility(View.GONE);
        mPreviousLayout.setVisibility(View.VISIBLE);
    }

    public void onNewLoginClicked(View view) {
        TextInputLayout mIDLayout = findViewById(R.id.misoID);
        TextInputLayout mPWLayout = findViewById(R.id.misoPW);
        MaterialButton mLoginButton = findViewById(R.id.misoLogin);
        Button mFindSome = findViewById(R.id.misoFindIDPW);
        Button mRegisterButton = findViewById(R.id.misoRegister);
        ConstraintLayout mToggle = findViewById(R.id.logintoggle);
        WebView mWebView = findViewById(R.id.loginWebView);
        Button mNewLogin = findViewById(R.id.misoNewLogin);
        //CheckBox mAutoLogin = findViewById(R.id.autologincb);
        ConstraintLayout mPreviousLayout = findViewById(R.id.previousLayout);
        mIDLayout.setVisibility(View.VISIBLE);
        mPWLayout.setVisibility(View.VISIBLE);
        mLoginButton.setVisibility(View.VISIBLE);
        mFindSome.setVisibility(View.VISIBLE);
        mRegisterButton.setVisibility(View.VISIBLE);
        mToggle.setVisibility(View.VISIBLE);
        mWebView.setVisibility(View.GONE);
        mNewLogin.setVisibility(View.GONE);
        //mAutoLogin.setVisibility(View.GONE);
        mPreviousLayout.setVisibility(View.GONE);
    }

    public void onSubmitClicked(View view) {
        TextInputEditText mIDField = findViewById(R.id.misoIDField);
        TextInputEditText mPWField = findViewById(R.id.misoPWField);
        TextInputLayout misoID = findViewById(R.id.misoID);
        TextInputLayout misoPW = findViewById(R.id.misoPW);
        boolean canLogin = true;
        if(Objects.requireNonNull(mIDField.getText()).length() < 2 || mIDField.getText().length() > 16) {
            misoID.setError(getResources().getText(R.string.loginIDlength));
            canLogin = false;
        }
        if(Objects.requireNonNull(mPWField.getText()).length() < 8 || mPWField.getText().length() > 18) {
            misoPW.setError(getResources().getText(R.string.loginPWlength));
            canLogin = false;
        }
        String[] IDChars = mIDField.getText().toString().split(" ");
        for (String ID : IDChars ) {
            boolean isAllowedC = Pattern.matches("^[a-zA-Z0-9_]*$",ID);
            if(!isAllowedC) {
                misoID.setError(getResources().getString(R.string.loginIDAllowchar));
                canLogin = false;
            }
        }
        String[] PWChars = mPWField.getText().toString().split("");
        for (String PW : PWChars) {
            boolean isAllowedC = Pattern.matches("^[a-zA-Z0-9!@#$%^&*()]*$",PW);
            if(!isAllowedC) {
                misoPW.setError(getResources().getString(R.string.loginPWAllowchar));
                canLogin = false;
            }
        }
        if(canLogin) {
            String ua = mWebView.getSettings().getUserAgentString();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
            Log.d("INFO","TRYING LOGIN");
            new loginTask(ua,view).execute();
        }
    }

    public class loginTask extends AsyncTask<Void, Void, String> {
        String ua;
        View v;
        loginTask(String ua, View v) {
            this.ua = ua;
            this.v = v;
        }
        @Override
        protected String doInBackground(Void...voids) {
            CookieManager cM = CookieManager.getInstance();
            TextInputEditText mIDField = (TextInputEditText) findViewById(R.id.misoIDField);
            TextInputEditText mPWField = (TextInputEditText) findViewById(R.id.misoPWField);
            String prevCookie = cM.getCookie("www.misodiary.net");
            try {
                Connection.Response loginTokenRes = Jsoup.connect("https://www.misodiary.net/api_member/auth_token")
                        .userAgent(ua)
                        .referrer("https://www.misodiary.net/member/login")
                        .followRedirects(true)
                        .ignoreContentType(true)
                        .postDataCharset("UTF-8")
                        .header("Host","www.misodiary.net")
                        .header("Connection","keep-alive")
                        .header("Accept","*/*")
                        .header("Origin","https://www.misodiary.net")
                        .header("X-Requested-With","XMLHttpRequest")
                        .header("Accept-Encoding","gzip, deflate")
                        .header("Accept-Language","ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                        .header("Cookie",prevCookie)
                        .method(Connection.Method.POST)
                        .data("uid",mIDField.getText().toString(),"pwd",mPWField.getText().toString())
                        .execute();
                if(loginTokenRes.cookie("ci_session")==null) {
                    Snackbar.make(v,getResources().getString(R.string.loginFailed),Snackbar.LENGTH_LONG).show();
                    return "failed";
                } else {
                    cM.setCookie("https://www.misodiary.net","ci_session="+loginTokenRes.cookie("ci_session"));
                    return loginTokenRes.cookie("ci_session");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(!result.equals("failed")) {
                loginprocess(result);
            }
        }
    }

    public void loginprocess(String loginedCookie) {
        CookieManager cM = CookieManager.getInstance();
        cM.setCookie("https://www.misodiary.net","ci_session="+loginedCookie);
        SharedPreferences cookie = getSharedPreferences("cookie", Context.MODE_PRIVATE);
        String cookieEn = cM.getCookie("www.misodiary.net");
        SharedPreferences.Editor editor = cookie.edit();
        editor.putString("cookie",cookieEn);
        editor.apply();
        Intent intent = new Intent();
        intent.putExtra("cookie","ci_session="+loginedCookie);
        String mainStatus = intent.getStringExtra("status");
        intent.putExtra("status",mainStatus);
        setResult(RESULT_OK,intent);
        finish();
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
            CookieManager cM = CookieManager.getInstance();
            if (!url.startsWith("https://www.misodiary.net/member")) {
                SharedPreferences cookie = getSharedPreferences("cookie", Context.MODE_PRIVATE);
                String cookieEn = cM.getCookie("www.misodiary.net");
                SharedPreferences.Editor editor = cookie.edit();
                editor.putString("cookie",cookieEn);
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
                Intent intent = new Intent();
                intent.putExtra("cookie","ci_session="+cM.getCookie("www.misodiary.net"));
                setResult(RESULT_OK,intent);
                finish();
            } else {
                try {
                    misoCustomTab c = new misoCustomTab();
                    c.launch(LoginActivity.this,url);
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
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            misoLogin.performClick();
        }
        return super.onKeyDown(keyCode, event);
    }
}
