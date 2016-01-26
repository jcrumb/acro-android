package com.crumbsauce.acro;


import android.os.AsyncTask;
import android.util.Log;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

interface LogInResultHandler {
    void receiveLoginResult(String result);
}

public class Backend {
    private static Backend ourInstance = new Backend();

    public static Backend getInstance() {
        return ourInstance;
    }

    private String backendBaseURL = "http://192.168.0.34:5000";
    private static final String LOG_TAG = "BACKEND";

    public static final String ERROR_STRING = "ERR_BACKEND"; // Any of the methods that return String will return this value on error

    private Backend() {

    }

    public AsyncTask<Void, Void, String> logInWithTokenAsync(final LogInResultHandler callback, final String googleIdToken) {
        return new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                return logInWithToken(googleIdToken);
            }

            @Override
            protected void onPostExecute(String result) {
                callback.receiveLoginResult(result);
            }
        };
    }

    private String logInWithToken(String googleIdToken) {
        String tokenJSON = String.format("{\"idToken\":\"%s\"}", googleIdToken);
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                tokenJSON
        );


        Request req = new Request.Builder()
                .url(backendBaseURL + "/login/google/verifytoken")
                .post(body)
                .build();

        Response resp;
        String response = ERROR_STRING;
        try {
            resp = client.newCall(req).execute();
            switch (resp.code()) {
                case 400:
                    Log.d(LOG_TAG, "Received 400 from server");
                    break;
                case 200:
                    response = resp.body().string().trim();
                    Log.d(LOG_TAG, response);
                    break;
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.toString());
        }

        return response;
    }

}
