package pw.pbdiary.maeari.misodiary;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.chip.Chip;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class AgreementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agreement);
        Chip mfmppchip = (Chip) findViewById(R.id.mfmpp);
        mfmppchip.setChecked(true);
        new PageTaskMPP(AgreementActivity.this).execute();
        mfmppchip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new PageTaskMPP(AgreementActivity.this).execute();
            }
        });
        Chip mtosppchip = (Chip) findViewById(R.id.mtos);
        mfmppchip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new PageTaskToS(AgreementActivity.this).execute();
            }
        });
        Chip mppchip = (Chip) findViewById(R.id.mpp);
        mfmppchip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new PageTaskPP(AgreementActivity.this).execute();
            }
        });
    }
    private static class PageTaskToS extends AsyncTask<Void,Void,String> {
        private Elements elementone;
        private WeakReference<AgreementActivity> activityReference;

        PageTaskToS(AgreementActivity context) {
            activityReference = new WeakReference<>(context);
        }
        @Override
        protected String doInBackground(Void... params) {
            try{
                Document doc = Jsoup.connect("https://www.misodiary.net/member/register").get();
                elementone = doc.select("div[class=policy]").eq(0);
            } catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            AgreementActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;
            TextView pv = activity.findViewById(R.id.policy_view);
            pv.setText(Html.fromHtml(elementone.html()));
        }
    }
    private static class PageTaskPP extends AsyncTask<Void,Void,String> {
        private Elements elementtwo;
        private WeakReference<AgreementActivity> activityReference;

        PageTaskPP(AgreementActivity context) {
            activityReference = new WeakReference<>(context);
        }
        @Override
        protected String doInBackground(Void... params) {
            try{
                Document doc = Jsoup.connect("https://www.misodiary.net/member/register").get();
                elementtwo = doc.select("div[class=policy]").eq(1);
            } catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            AgreementActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;
            TextView pv = activity.findViewById(R.id.policy_view);
            pv.setText(Html.fromHtml(elementtwo.html()));
        }
    }
    private static class PageTaskMPP extends AsyncTask<Void,Void,String> {
        private Document doc;
        private WeakReference<AgreementActivity> activityReference;

        PageTaskMPP(AgreementActivity context) {
            activityReference = new WeakReference<>(context);
        }
        @Override
        protected String doInBackground(Void... params) {
            try{
                doc = Jsoup.connect("https://latios.pbdiary.pw/maeari/misodiary/privacypolicy.html").get();
            } catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            AgreementActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;
            TextView pv = activity.findViewById(R.id.policy_view);
            pv.setText(Html.fromHtml(doc.html()));
        }
    }
}
