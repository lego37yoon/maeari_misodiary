package pw.pbdiary.maeari.misodiary;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends AppCompatActivity implements OnKeyboardVisibilityListener{

    //액티비티 내에서 사용하는 항목 불러오기 위한 변수 지정 작업
    public TextView mTextMessage;
    public AppBarLayout topAppBar;
    public WebView mWebView;
    public Chip cIlsang;
    public Chip cToday;
    public Chip cMichinRandom;
    public Chip cFood;
    public ImageButton searchBtn;
    //파일 업로드를 위한 변수
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

        //각 변수 항목 지정
        mTextMessage = (TextView) findViewById(R.id.title_main);
        cIlsang = (Chip) findViewById(R.id.chipIlsang);
        cToday = (Chip) findViewById(R.id.chipToday);
        cMichinRandom = (Chip) findViewById(R.id.chipMichinrandom);
        cFood = (Chip) findViewById(R.id.chipFoods);
        mWebView = (WebView) findViewById(R.id.webView);
        topAppBar = (AppBarLayout) findViewById(R.id.appBarLayout3);
        searchBtn = (ImageButton) findViewById(R.id.menu_search);
        //Top App Bar 설정
        BottomAppBar mAppBar = findViewById(R.id.navigation);
        setSupportActionBar(mAppBar);

        //Top App Bar 다크모드 대응
        int nowNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        Drawable mDrawable = searchBtn.getDrawable();
        switch (nowNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                topAppBar.setBackground(new ColorDrawable(getResources().getColor(R.color.mainAppBarDay)));
                mTextMessage.setTextColor(getResources().getColor(R.color.text_color_primary));
                DrawableCompat.setTint(mDrawable,getResources().getColor(R.color.colorPrimaryDark));
                DrawableCompat.setTintMode(mDrawable, PorterDuff.Mode.SRC_IN);
                searchBtn.setImageDrawable(mDrawable);
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                topAppBar.setBackground(new ColorDrawable(getResources().getColor(R.color.colorAccent)));
                mTextMessage.setTextColor(getResources().getColor(R.color.mainAppBarDay));
                DrawableCompat.setTint(mDrawable,getResources().getColor(R.color.mainAppBarDay));
                DrawableCompat.setTintMode(mDrawable, PorterDuff.Mode.SRC_IN);
                break;
        }

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

        mWebView.setWebViewClient(new misoWeb());
        new misoWebViewer().set(this,mWebView);

        //쿠키 불러오기
        SharedPreferences cookie = getSharedPreferences("cookie",Context.MODE_PRIVATE);
        CookieManager cM = CookieManager.getInstance();
        if(cookie.getString("cookie","") != null) {
            Log.d("COOKIE",cookie.getString("cookie",""));
            cM.setCookie("m3day.cafe24.com",cookie.getString("cookie",""));
        }

        //불러오는 페이지에 따라 글자 변경
        if(getIntent().getStringExtra("status") != null) {
            String status = getIntent().getStringExtra("status");
            switch (status) {
                case "opench":
                    mTextMessage.setText(R.string.title_opench);
                    mWebView.loadUrl("http://m3day.cafe24.com/");
                    break;
                case "michinrandom":
                    mTextMessage.setText(R.string.title_michinrandom);
                    mWebView.loadUrl("http://m3day.cafe24.com/board/findfriend");
                    cMichinRandom.setChecked(true);
                    break;
                case "profile":
                    mTextMessage.setText(R.string.title_profile);
                    mWebView.loadUrl("http://m3day.cafe24.com/board/daytime");
                    cIlsang.setChecked(true);
                    break;
                default:
                    mTextMessage.setText(R.string.title_opench);
                    mWebView.loadUrl("http://m3day.cafe24.com");
                    break;
            }
        } else {
            //설정된 메인화면 값에 따라 화면 변경
            SharedPreferences mainscdefault = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences statusSave= getSharedPreferences("status",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = statusSave.edit();
            switch (Objects.requireNonNull(mainscdefault.getString("screendefault", "opench"))){
                case "opench":
                    editor.putString("status","opench");
                    editor.apply();
                    mTextMessage.setText(R.string.title_opench);
                    mWebView.loadUrl("http://m3day.cafe24.com");
                    break;
                case "michinrandom":
                    editor.putString("status","michinrandom");
                    editor.apply();
                    mTextMessage.setText(R.string.title_michinrandom);
                    mWebView.loadUrl("http://m3day.cafe24.com/board/findfriend");
                    break;
                case "profile":
                    editor.putString("status","profile");
                    editor.apply();
                    mTextMessage.setText(R.string.title_profile);
                    mWebView.loadUrl("http://m3day.cafe24.com/board/daytime");
                    break;
                default:
                    mTextMessage.setText(R.string.title_opench);
                    mWebView.loadUrl("http://m3day.cafe24.com");
                    break;
            }
        }

        //Swipe To Refresh 적용
        SwipeRefreshLayout pullRefresh = findViewById(R.id.swipeMain);
        pullRefresh.setColorSchemeColors(getResources().getColor(R.color.colorPrimary),getResources().getColor(R.color.colorPrimaryDark),getResources().getColor(R.color.colorAccent));
        pullRefresh.setOnRefreshListener(() -> mWebView.reload());

        //LG 기기에서 나타나는 색 미지정 오류 해결
        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimary));

        //키보드 올라올 때 앱 바 가리기
        setKeyboardVisibilityListener(this);
    }

    //하단 앱바에 메뉴 연결
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bottom_app_bar, menu);
        return true;
    }
    //메뉴 클릭 이벤트 관리
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_noti:
                Intent notiInt = new Intent(getApplicationContext(), NotiActivity.class);
                startActivity(notiInt);
                break;
            case R.id.menu_myaccount:
                Intent myAccountInt = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(myAccountInt);
                break;
            case R.id.menu_logout:
                CookieManager cM = CookieManager.getInstance();
                SharedPreferences cookie = getSharedPreferences("cookie", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = cookie.edit();
                editor.putString("cookie","");
                editor.apply();
                cM.removeAllCookies(null);
                mWebView.reload();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //키보드 올라왔는지 여부에 따라 하단 바 숨길지 말지 결정
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
            if(url.startsWith("http://m3day.cafe24.com/notification")) {
                view.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(getApplicationContext(), NotiActivity.class);
                startActivity(intent);
            } else if(url.startsWith("http://m3day.cafe24.com/search?skeyword=")) {
                view.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(getApplicationContext(),SearchActivity.class);
                String keyword = url.replace("http://m3day.cafe24.com/search?skeyword=","");
                intent.putExtra("keyword",keyword);
                startActivity(intent);
            } else if(url.startsWith("http://m3day.cafe24.com/post")) {
                view.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(getApplicationContext(),PostViewActivity.class);
                String postNumber = url.replace("http://m3day.cafe24.com/post/","");
                intent.putExtra("postNumber",postNumber);
                startActivity(intent);
            } else if(url.startsWith("http://m3day.cafe24.com/profile/")) {
                view.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(getApplicationContext(),ProfileViewActivity.class);
                String accountID = url.replace("http://m3day.cafe24.com/profile/","");
                intent.putExtra("accountID",accountID);
                startActivity(intent);
            } else if(url.startsWith("http://m3day.cafe24.com/login?url=")) {
                view.setVisibility(View.INVISIBLE);
            } else if(url.startsWith("http://m3day.cafe24.com")) {
                view.loadUrl(url);
            } else {
                misoCustomTab c = new misoCustomTab();
                c.launch(MainActivity.this,url);
            }
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if(url.startsWith("http://m3day.cafe24.com/board/findfriend")) {
                mTextMessage.setText(R.string.title_michinrandom);
                SharedPreferences statusSave= getSharedPreferences("status",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = statusSave.edit();
                editor.putString("status","michinrandom");
                editor.apply();
            } else if(url.startsWith("http://m3day.cafe24.com/board/daytime")) {
                mTextMessage.setText(R.string.title_profile);
                SharedPreferences statusSave= getSharedPreferences("status",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = statusSave.edit();
                editor.putString("status","profile");
                editor.apply();
            } else if(url.startsWith("http://m3day.cafe24.com/board")) {
                mTextMessage.setText(R.string.title_opench);
                SharedPreferences statusSave= getSharedPreferences("status",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = statusSave.edit();
                editor.putString("status","opench");
                editor.apply();
            } else if(url.startsWith("http://m3day.cafe24.com/login?url=")) {
                Intent loginIntent = new Intent(getApplicationContext(),LoginActivity.class);
                loginIntent.putExtra("status",url.replace("http://m3day.cafe24.com/login?url=",""));
            } else if(url.startsWith("https://m3day.cafe24.com")||url.startsWith("http://m3day.cafe24.com")){
                if(url.equals("https://m3day.cafe24.com")||url.equals("https://m3day.cafe24.com/")||url.equals("http://m3day.cafe24.com")||url.equals("http://m3day.cafe24.com/")) {
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
                cM.setCookie("m3day.cafe24.com",cookie.getString("cookie",""));
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

    public void onSearchClicked(View v) {
        Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
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
        mWebView.loadUrl("http://m3day.cafe24.com/board/daytime");

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
                cM.setCookie("m3day.cafe24.com", data.getStringExtra("cookie"));
                if (data.getStringExtra("status") != null) {
                    String status = getIntent().getStringExtra("status");
                    switch (status) {
                        case "opench":
                            mTextMessage.setText(R.string.title_opench);
                            if (Objects.requireNonNull(mainscdefault.getString("screendefault", "opench")).equals("profile")|| Objects.requireNonNull(forProfile.getString("ifP", "false")).equals("true")) {
                                mTextMessage.setText(R.string.title_profile);
                                mWebView.loadUrl("http://m3day.cafe24.com/board/daytime");
                                SharedPreferences.Editor editor = forProfile.edit();
                                editor.putString("ifP","false");
                                editor.apply();
                            } else {
                                mWebView.loadUrl("http://m3day.cafe24.com/");
                            }
                            break;
                        case "michinrandom":
                            mTextMessage.setText(R.string.title_michinrandom);
                            mWebView.loadUrl("http://m3day.cafe24.com/board/findfriends");
                            break;
                        case "profile":
                            mTextMessage.setText(R.string.title_profile);
                            mWebView.loadUrl("http://m3day.cafe24.com/board/daytime");
                            break;
                    }
                } else {
                    if (Objects.requireNonNull(mainscdefault.getString("screendefault", "opench")).equals("profile") || Objects.requireNonNull(forProfile.getString("ifP", "false")).equals("true")) {
                        mTextMessage.setText(R.string.title_profile);
                        mWebView.loadUrl("http://m3day.cafe24.com/board/daytime");
                        SharedPreferences.Editor editor = forProfile.edit();
                        editor.putString("ifP","false");
                        editor.apply();
                    } else {
                        mWebView.loadUrl("http://m3day.cafe24.com/");
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
            cM.setCookie("m3day.cafe24.com",cookie.getString("cookie",""));
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
        //WebHistoryItem prevURL = currentHistory.getItemAtIndex(currentHistory.getCurrentIndex()-1);
        if(url.startsWith("http://m3day.cafe24.com/main/opench")) {
            mTextMessage.setText(R.string.title_opench);
        } else if(url.startsWith("https://m3day.cafe24.com/board/findfriends")) {
            mTextMessage.setText(R.string.title_michinrandom);
        } else if(url.startsWith("https://m3day.cafe24.com/board/daytime")) {
            mTextMessage.setText(R.string.title_profile);
        } else if(url.startsWith("http://m3day.cafe24.com/board")) {
            mTextMessage.setText(getResources().getString(R.string.title_opench));
        } else if(url.startsWith("http://m3day.cafe24.com/notification")) {
            Intent intent = new Intent(getApplicationContext(), NotiActivity.class);
            startActivity(intent);
        } else if(url.startsWith("http://m3day.cafe24.com/search?skeyword=")) {
            Intent intent = new Intent(getApplicationContext(),SearchActivity.class);
            String keyword = url.replace("http://m3day.cafe24.com/search?skeyword=","");
            intent.putExtra("keyword",keyword);
            startActivity(intent);
        } else if(url.startsWith("https://m3day.cafe24.com/post")) {
            Intent intent = new Intent(getApplicationContext(),PostViewActivity.class);
            String postNumber = url.replace("https://m3day.cafe24.com/post/","");
            intent.putExtra("postNumber",postNumber);
            startActivity(intent);
        } else if(url.startsWith("http://m3day.cafe24.com/profile")) {
            Intent intent = new Intent(getApplicationContext(),ProfileViewActivity.class);
            String accountID = url.replace("http://m3day.cafe24.com/profile/","");
            intent.putExtra("accountID",accountID);
            startActivity(intent);
        } /* else if(url.startsWith("https://m3day.cafe24.com/member/login")) {
            mWebView.goBackOrForward(mWebView.copyBackForwardList().getCurrentIndex()-2);
            backControl(mWebView);
        } */ else if(url.startsWith("https://m3day.cafe24.com")||url.startsWith("http://m3day.cafe24.com")){
            if(url.equals("https://m3day.cafe24.com")||url.equals("https://m3day.cafe24.com/")||url.equals("http://m3day.cafe24.com")||url.equals("http://m3day.cafe24.com/")) {
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
