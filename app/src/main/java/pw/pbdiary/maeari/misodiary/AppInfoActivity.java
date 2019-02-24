package pw.pbdiary.maeari.misodiary;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class AppInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_info);
        TextView mTextView = findViewById(R.id.buildinfo);
        TextView mTextView2 = findViewById(R.id.codename);
        mTextView2.setText(BuildConfig.VERSION_NAME+getResources().getString(R.string.version_string));
        mTextView.setText(getResources().getString(R.string.app_name)+"\n"+BuildConfig.VERSION_NAME+getResources().getString(R.string.version_string)+"\n BUILD: "+BuildConfig.VERSION_CODE+" MODE: "+BuildConfig.BUILD_TYPE);
    }

    public void onOSLClicked(View v) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://latios.pbdiary.pw/maeari/misodiary/license.html"));
        startActivity(i);
    }
    public void onMaeariClicked(View v) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://latios.pbdiary.pw/maeari"));
        startActivity(i);
    }
    public void onMisoClicked(View v) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.misodiary.net"));
        startActivity(i);
    }
}
