package pw.pbdiary.maeari.misodiary;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class AgreementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agreement);
    }
    private class PageTaskToS extends AsyncTask<Void,Void,Void> {
        private Elements elementone;
        @Override
        protected Void doInBackground(Void... voids) {
            try{
                Document doc = Jsoup.connect("https://www.misodiary.net/member/register").get();
                elementone = doc.select("div[class=policy]").eq(0);
            } catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            TextView pv = findViewById(R.id.policy_view);
            pv.setText(Html.fromHtml(elementone.html()));
        }
    }
    private class PageTaskPP extends AsyncTask<Void,Void,Void> {
        private Elements elementtwo;
        @Override
        protected Void doInBackground(Void... voids) {
            try{
                Document doc = Jsoup.connect("https://www.misodiary.net/member/register").get();
                elementtwo = doc.select("div[class=policy]").eq(1);
            } catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            TextView pv = findViewById(R.id.policy_view);
            pv.setText(Html.fromHtml(elementtwo.html()));
        }
    }
}
