package pw.pbdiary.maeari.misodiary;

import android.Manifest;
import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import java.util.Date;

public class PermissionCheckInfo extends AppCompatActivity {

    int ALL_PERMISSION_GRANTED;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_permission_check_info);
        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimary));
    }

    public void onStartMainClicked(View v) {
        CheckBox cb = findViewById(R.id.checkBox);
        if(cb.isChecked()) {
            SharedPreferences sp = getSharedPreferences("saveFirst", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("first",true);
            editor.apply();
            Intent i = new Intent();
            setResult(RESULT_OK,i);
            finish();
        } else {
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.check_agree),Toast.LENGTH_LONG).show();
        }
    }

    public void onRequestPermissions(View v){
        CheckBox cb = findViewById(R.id.checkBox);
        if(cb.isChecked()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA},ALL_PERMISSION_GRANTED);
            SharedPreferences sp = getSharedPreferences("saveFirst", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("first",true);
            editor.apply();
        } else {
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.check_agree),Toast.LENGTH_LONG).show();
        }
    }

    public void onTOSClicked(View v) {
        Intent i = new Intent(getApplicationContext(),AgreementActivity.class);
        startActivity(i);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length >0) {
            Intent i = new Intent();
            setResult(RESULT_OK,i);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        this.finishAffinity();
    }
}