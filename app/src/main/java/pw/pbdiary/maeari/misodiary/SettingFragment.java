package pw.pbdiary.maeari.misodiary;

import android.os.Bundle;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;

public class SettingFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.setting_fragment, rootKey);
        Preference delCache = (Preference) findPreference("deleteCache");
        delCache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                File dir = getActivity().getCacheDir();
                if (dir != null && dir.isDirectory()) {
                    try {
                        File[] children = dir.listFiles();
                        if (children.length > 0) {
                            for (int i = 0; i < children.length; i++) {
                                File[] temp = children[i].listFiles();
                                for (int x = 0; x < temp.length; x++) {
                                    temp[x].delete();
                                }
                            }
                            Snackbar.make(getActivity().findViewById(android.R.id.content),getResources().getString(R.string.string_cache_deleted),Snackbar.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Log.e("Cache", "failed cache clean");
                    }
                } else {
                    Snackbar.make(getActivity().findViewById(android.R.id.content),getResources().getString(R.string.error_failed_del_cache),Snackbar.LENGTH_LONG).show();
                }
                return false;
            }
        });
    }

}
