package pw.pbdiary.maeari.misodiary;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.UnknownHostException;

public class AgreementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agreement);
        Chip mfmppchip = (Chip) findViewById(R.id.mfmpp);
        ChipGroup misoAgree = (ChipGroup) findViewById(R.id.amChoice);
        final String mfmppLink = getResources().getString(R.string.mfmagreelink);
        mfmppchip.setChecked(true);
        new PageTaskMPP(AgreementActivity.this,mfmppLink).execute();
        misoAgree.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup chipGroup, int i) {
                switch (chipGroup.getCheckedChipId()) {
                    case R.id.mfmpp:
                        new PageTaskMPP(AgreementActivity.this,mfmppLink).execute();
                        break;
                    case R.id.mtos:
                        new PageTaskToS(AgreementActivity.this).execute();
                        break;
                    case R.id.mpp:
                        new PageTaskPP(AgreementActivity.this).execute();
                }
            }
        });
    }

    public void onagreeBackClicked(View view) {
        finish();
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
                Document doc = Jsoup.connect("http://www.misodiary.net/document/provision").get();
                elementone = doc.select("div[class=row]").eq(0);
            } catch (UnknownHostException e) {
                Looper.prepare();
                Toast.makeText(activityReference.get().getApplicationContext(),activityReference.get().getResources().getString(R.string.noInternetConnection),Toast.LENGTH_LONG).show();
                Looper.loop();
                e.printStackTrace();
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
                Document doc = Jsoup.connect("http://www.misodiary.net/document/privacy").get();
                elementtwo = doc.select("div[class=row]").eq(0);
            } catch (UnknownHostException e) {
                Looper.prepare();
                Toast.makeText(activityReference.get().getApplicationContext(),activityReference.get().getResources().getString(R.string.noInternetConnection),Toast.LENGTH_LONG).show();
                Looper.loop();
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
        private String mfmppLink;
        PageTaskMPP(AgreementActivity context, String mfmppLink) {
            activityReference = new WeakReference<>(context);
            this.mfmppLink = mfmppLink;
        }
        @Override
        protected String doInBackground(Void... params) {
            try{
                doc = Jsoup.connect(mfmppLink).get();
            } catch(UnknownHostException e) {
                Looper.prepare();
                Toast.makeText(activityReference.get().getApplicationContext(),activityReference.get().getResources().getString(R.string.noInternetConnection),Toast.LENGTH_LONG).show();
                activityReference.get().finish();
                Looper.loop();
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
