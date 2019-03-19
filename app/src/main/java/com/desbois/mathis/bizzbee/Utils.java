package com.desbois.mathis.bizzbee;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

abstract class Utils implements Callback {
    private static String tmp = "";
    private static boolean changed = false;

    public static void setTmp(String t) {
        tmp = t;
        changed = true;

        Log.i("Bizzbee", "Changed");
    }

    public static boolean isUrl(String s) {
        return Pattern.compile("^(http:\\/\\/|https:\\/\\/)?(www.)?([a-zA-Z0-9]+).[a-zA-Z0-9]*.[a-z]{3}.?([a-z]+)?$")
                .matcher(s).matches();
    }

    public static boolean isBizzbeeUrl(String s) {
        OkHttpClient client = new OkHttpClient();

        Log.i("Bizzbee", "BBBB " + s);

        Request request = new Request.Builder()
                .url("https://" + s.replaceAll(" ", "") + "/version")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Utils.setTmp("");
                Log.i("Bizzbee", e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Utils.setTmp(response.body().string());

                Log.i("Bizzbee", tmp);
            }
        });

        new Handler().postDelayed(() -> {
            cancelRequest(client);
            tmp = "";
            changed = false;
        }, 10000);

        while (!changed);

        changed = false;

        return isUrl(s) && Pattern.compile("^Bizzbee-\\d+.\\d+").matcher(tmp).matches();
    }

    public static void cancelRequest(OkHttpClient okHttpClient) {
        //When you want to cancel:
        //A) go through the queued calls and cancel if the tag matches:
        for (Call call : okHttpClient.dispatcher().queuedCalls()) {
            if (call.request().tag().equals("connection"))
                call.cancel();
        }

        //B) go through the running calls and cancel if the tag matches:
        for (Call call : okHttpClient.dispatcher().runningCalls()) {
            if (call.request().tag().equals("connection"))
                call.cancel();
        }
    }

    public static String beautifyUrl(String s) {
        return s.replaceAll("^(http:\\/\\/|https:\\/\\/)?", "").replaceAll("/$", "");
    }

    public static void removeSharedPeferences(SharedPreferences sharedPref, String key) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(key);
        editor.apply();
    }

    public static void addSharedPreferences(SharedPreferences sharedPref, String key, String value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void addSharedPreferences(SharedPreferences sharedPref, String key, boolean value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }
}
