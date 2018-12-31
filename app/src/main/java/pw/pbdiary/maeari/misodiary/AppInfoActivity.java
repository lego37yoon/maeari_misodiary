package pw.pbdiary.maeari.misodiary;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class AppInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_info);
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
