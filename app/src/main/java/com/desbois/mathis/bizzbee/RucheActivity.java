package com.desbois.mathis.bizzbee;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RucheActivity extends AppCompatActivity {
    private static final String url = "/rucheInfos";
    private static final String TAG = "RucheActivity";

    private static final int BAD_ID = -1;

    private Handler handler = new Handler();

    private ProgressDialog progressDialog;

    private TextView mTitle;
    private TextView mSubtitle;
    private ImageView mIcon;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ruche);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);

        actionbar.setTitle(R.string.ruche);

        mTitle = findViewById(R.id.title);
        mSubtitle = findViewById(R.id.subtitle);
        mIcon = findViewById(R.id.icon);

        getData(getIntent().getIntExtra("idRuche", BAD_ID));
    }

    public void getData(int id) {
        if(id == BAD_ID) {
            getDataFailed("Bad id");
            return;
        }

        progressDialog = new ProgressDialog(this);

        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Getting information...");
        progressDialog.show();

        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://" + ((BizzbeeApp)getApplication()).getServUrl() + url + "?ruche=" + id)
                .tag("connection")
                .build();

        Callback callback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.removeCallbacksAndMessages(null);
                progressDialog.dismiss();

                runOnUiThread(() -> {
                    Log.i(TAG, "Failed");
                    getDataFailed("Failed");
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //le retour est effectué dans un thread différent
                handler.removeCallbacksAndMessages(null);
                final String text = response.body().string();
                Bitmap decodedByte = null;
                String nom = null;
                String idRuche = null;

                progressDialog.dismiss();

                if(text.equals("{}")) {
                    runOnUiThread(() -> {
                        getDataFailed("No data...");
                    });
                }

                try {
                    JSONObject json = new JSONObject(text);

                    byte[] decodedString = Base64.decode(json.getString("img"), Base64.DEFAULT);
                    decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                    nom = json.getString("name");
                    idRuche = json.getString("id");
                } catch (JSONException e) {
                    Log.e(TAG, "" + e.getMessage());
                }

                String finalNom = nom;
                String finalId = getString(R.string.subtitle) + " " + idRuche;
                Bitmap finalDecodedByte = decodedByte;
                runOnUiThread(() -> {
                    if(response.isSuccessful()) {
                        mIcon.setImageBitmap(finalDecodedByte);
                        mTitle.setText(finalNom);
                        mSubtitle.setText(finalId);
                    } else {
                        Log.i(TAG, "Fail");
                        getDataFailed("Not success");
                    }
                });
            }
        };

        Call response = okHttpClient.newCall(request);
        response.enqueue(callback);
        Log.i(TAG, response.request().toString());

        progressDialog.setOnCancelListener((DialogInterface dialog) -> {
            Utils.cancelRequest(okHttpClient);
            getDataFailed("Attempt canceled...");
            handler.removeCallbacksAndMessages(null);
        });

        handler.postDelayed(() -> {
            Utils.cancelRequest(okHttpClient);
            progressDialog.dismiss();
            getDataFailed("Timeout error...");
        }, 10000);
    }

    public void getDataFailed(String m) {
        Toast.makeText(getBaseContext(), m, Toast.LENGTH_LONG).show();
        this.finish();
    }

}
