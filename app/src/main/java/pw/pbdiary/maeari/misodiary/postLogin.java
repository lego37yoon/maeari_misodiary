package pw.pbdiary.maeari.misodiary;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class postLogin {

    private OkHttpClient client;
    private static postLogin instance = new postLogin();
    public static postLogin getInstance() {
        return instance;
    }

    private postLogin() {
        this.client = new OkHttpClient();
    }

    public void post(String logindata, Callback cblogin, String ua) {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.get("application/json; charset=utf-8"),logindata);
        Request request = new Request.Builder().url("https://www.misodiary.net/api_member/auth_token")
                .post(body)
                .header("User-Agent",ua)
                .build();
        client.newCall(request).enqueue(cblogin);
    }
}
