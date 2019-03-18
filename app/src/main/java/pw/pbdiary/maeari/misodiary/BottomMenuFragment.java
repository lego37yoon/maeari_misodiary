package pw.pbdiary.maeari.misodiary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;

import java.util.Objects;

public class BottomMenuFragment extends BottomSheetDialogFragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);
        NavigationView nav = view.findViewById(R.id.list);
        nav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_michinrandom:
                        MainActivity mA = new MainActivity();
                        mA.mWebView = Objects.requireNonNull(getActivity()).findViewById(R.id.webView);
                        mA.mTextMessage = Objects.requireNonNull(getActivity()).findViewById(R.id.title_main);
                        mA.mWebView.loadUrl("https://www.misodiary.net/main/random_friends");
                        mA.mTextMessage.setText(getResources().getString(R.string.title_michinrandom));
                        FragmentManager fm = getActivity().getSupportFragmentManager();
                        fm.beginTransaction().remove(BottomMenuFragment.this).commit();
                        break;
                    case R.id.nav_opench:
                        mA = new MainActivity();
                        mA.mWebView = Objects.requireNonNull(getActivity()).findViewById(R.id.webView);
                        mA.mTextMessage = Objects.requireNonNull(getActivity()).findViewById(R.id.title_main);
                        mA.mWebView.loadUrl("https://www.misodiary.net");
                        mA.mTextMessage.setText(getResources().getString(R.string.title_opench));
                        fm = getActivity().getSupportFragmentManager();
                        fm.beginTransaction().remove(BottomMenuFragment.this).commit();
                        break;
                    case R.id.nav_profile:
                        mA = new MainActivity();
                        mA.mWebView = Objects.requireNonNull(getActivity()).findViewById(R.id.webView);
                        mA.mTextMessage = Objects.requireNonNull(getActivity()).findViewById(R.id.title_main);
                        mA.mWebView.loadUrl("https://www.misodiary.net/home/dashboard");
                        mA.mTextMessage.setText(getResources().getString(R.string.title_profile));
                        fm = getActivity().getSupportFragmentManager();
                        fm.beginTransaction().remove(BottomMenuFragment.this).commit();
                        break;
                    case R.id.nav_logout:
                        mA = new MainActivity();
                        mA.mWebView = Objects.requireNonNull(getActivity()).findViewById(R.id.webView);
                        SharedPreferences cookie = Objects.requireNonNull(getContext()).getSharedPreferences("cookie", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = cookie.edit();
                        editor.putString("cookie","");
                        editor.apply();
                        android.webkit.CookieManager cM = CookieManager.getInstance();
                        cM.removeAllCookies(null);
                        mA.mWebView.reload();
                        fm = getActivity().getSupportFragmentManager();
                        fm.beginTransaction().remove(BottomMenuFragment.this).commit();
                        break;
                }
                return true;
            }
        });
        return view;
    }

}
