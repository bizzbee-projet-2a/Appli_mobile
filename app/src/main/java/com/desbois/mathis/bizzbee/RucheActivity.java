package com.desbois.mathis.bizzbee;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.TaskStackBuilder;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RucheActivity extends AppCompatActivity {
    private static final String url = "/rucheInfos";
    private static final String TAG = "RucheActivity";

    public static final String RUCHE_ID = "RucheActivity.idRuche";

    private static final int BAD_ID = -1;

    private Ruche mRuche;

    private ArrayList<Double> poids = new ArrayList<>();
    private ArrayList<Double> temperature = new ArrayList<>();
    private ArrayList<Double> humidite = new ArrayList<>();

    private List<Entry> entriesGroup1 = new ArrayList<>();
    private List<Entry> entriesGroup2 = new ArrayList<>();
    private List<Entry> entriesGroup3 = new ArrayList<>();

    private Handler handler = new Handler();

    private ProgressDialog progressDialog;

    private TextView mTitle;
    private TextView mSubtitle;
    private ImageView mIcon;

    private LineChart lineChart;

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

        Log.i(TAG, "" + getIntent().getIntExtra(RUCHE_ID, BAD_ID));
        Log.i(TAG, "" + getIntent().hasExtra(RUCHE_ID));

        lineChart = findViewById(R.id.graph);

        makeRequest(getIntent().getIntExtra(RUCHE_ID, BAD_ID));
    }

    public void makeRequest(int id) {
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
                .tag("ruche")
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
                String idParent = null;

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
                    idParent = json.getString("id_parent");

                    mRuche = new Ruche(id, nom, Integer.parseInt(idParent));

                    computeData("humidite", json.getJSONArray("humidite"));
                    computeData("temperature", json.getJSONArray("temperature"));
                    computeData("poids", json.getJSONArray("poids"));
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

                        float groupSpace = 0.06f;
                        float barSpace = 0.02f; // x2 dataset
                        float barWidth = 0.45f; // x2 dataset
                        // (0.02 + 0.45) * 2 + 0.06 = 1.00 -> interval per "group"

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            entriesGroup1.sort((entry, t1) -> Float.compare(entry.getX(), t1.getX()));
                            entriesGroup2.sort((entry, t1) -> Float.compare(entry.getX(), t1.getX()));
                            entriesGroup3.sort((entry, t1) -> Float.compare(entry.getX(), t1.getX()));
                        }

                        LineDataSet set1 = new LineDataSet(entriesGroup1, "Poids");
                        LineDataSet set2 = new LineDataSet(entriesGroup2, "Température");
                        LineDataSet set3 = new LineDataSet(entriesGroup3, "Humidité");

                        LineData data = new LineData(set1, set2, set3);
                        set1.setColors(ColorTemplate.rgb("#ff0000"));
                        set2.setColors(ColorTemplate.rgb("#00ff00"));
                        set3.setColors(ColorTemplate.rgb("#0000ff"));

                        lineChart.setData(data);
                        //barChart.groupBars(-0.5f, groupSpace, barSpace); // perform the "explicit" grouping
                        lineChart.animateXY(300, 300);
                        lineChart.invalidate(); // refresh


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

    @Override
    public void onPrepareSupportNavigateUpTaskStack(TaskStackBuilder builder) {
        super.onPrepareSupportNavigateUpTaskStack(builder);
        Intent rucherIntent = new Intent(this, RucherActivity.class);

        rucherIntent.putExtra(RucherActivity.RUCHER_ID, mRuche.getIdParent());

        builder.addNextIntent(rucherIntent);
    }

    @Override
    public void onNewIntent(Intent intent) {
        makeRequest(intent.getIntExtra(RUCHE_ID, BAD_ID));
    }

    public void computeData(String type, JSONArray json) {
        for(int i = 0; i < json.length(); i++) {
            try {
                JSONObject child = json.getJSONObject(i);

                DateTimeFormatter f = ISODateTimeFormat.dateTime();

                Double val = Double.parseDouble(child.getString("val"));
                DateTime date = f.parseDateTime(child.getString("date_mesure"));
                switch (type) {
                    case "humidite":
                        humidite.add(val);
                        entriesGroup3.add(new Entry(date.getDayOfYear(), val.floatValue()));
                        break;
                    case "temperature":
                        temperature.add(val);
                        entriesGroup2.add(new Entry(date.getDayOfYear(), val.floatValue()));
                        break;
                    case "poids":
                        poids.add(val);
                        entriesGroup1.add(new Entry(date.getDayOfYear(), val.floatValue()));
                        break;
                }
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
}
