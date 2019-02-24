package pw.pbdiary.maeari.misodiary;

import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;

public class misoCustomTab {
    public void launch(AppCompatActivity activity, String url) {
        CustomTabsIntent.Builder b = new CustomTabsIntent.Builder();
        b.setToolbarColor(ContextCompat.getColor(activity,R.color.colorPrimary));
        b.setShowTitle(true);
        CustomTabsIntent i = b.build();
        i.launchUrl(activity, Uri.parse(url));
    }
}
