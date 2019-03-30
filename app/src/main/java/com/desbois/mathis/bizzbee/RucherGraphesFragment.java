package com.desbois.mathis.bizzbee;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// Instances of this class are fragments representing a single
// object in our collection.
public class RucherGraphesFragment extends Fragment {
    private static final String TAG = "RucherGraphesFragment";

    private static final String urlTree = "/getTree";
    private static final String urlRuche = "/rucheInfos";

    private Handler handler = new Handler();

    private ArrayList<Integer> poids = new ArrayList<>();
    private ArrayList<Integer> temperature = new ArrayList<>();
    private ArrayList<Integer> humidite = new ArrayList<>();

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        View view =inflater.inflate(R.layout.fragment_tab_graphes, container, false);

        //getData(getIntent().getIntExtra("idRuche", BAD_ID));


        BarChart barChart = view.findViewById(R.id.graph);

        poids.add(15);
        temperature.add(45);
        temperature.add(4);
        temperature.add(48);
        humidite.add(24);

        float groupSpace = 0.06f;
        float barSpace = 0.02f; // x2 dataset
        float barWidth = 0.45f; // x2 dataset
        // (0.02 + 0.45) * 2 + 0.06 = 1.00 -> interval per "group"

        List<BarEntry> entriesGroup1 = new ArrayList<>();
        List<BarEntry> entriesGroup2 = new ArrayList<>();
        List<BarEntry> entriesGroup3 = new ArrayList<>();

        // fill the lists
        for(int i = 0; i < poids.size(); i++) {
            entriesGroup1.add(new BarEntry(i, poids.get(i)));
            entriesGroup2.add(new BarEntry(i, temperature.get(i)));
            entriesGroup3.add(new BarEntry(i, humidite.get(i)));
        }

        BarDataSet set1 = new BarDataSet(entriesGroup1, "Poids");
        BarDataSet set2 = new BarDataSet(entriesGroup2, "Température");
        BarDataSet set3 = new BarDataSet(entriesGroup3, "Humidité");

        BarData data = new BarData(set1, set2, set3);
        set1.setColors(ColorTemplate.MATERIAL_COLORS);
        set2.setColors(ColorTemplate.MATERIAL_COLORS);
        set3.setColors(ColorTemplate.MATERIAL_COLORS);

        data.setBarWidth(barWidth); // set the width of each bar
        barChart.setData(data);
        barChart.groupBars(-0.5f, groupSpace, barSpace); // perform the "explicit" grouping
        barChart.invalidate(); // refresh

        return view;

    }

    public void makeRequest() {
        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://" + ((BizzbeeApp)getActivity().getApplication()).getServUrl() + urlTree + "?apiculteur=1")
                .tag("service")
                .build();

        Callback callback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.removeCallbacksAndMessages(null);
                Log.i(TAG, "Failed");
                Log.i(TAG, e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //le retour est effectué dans un thread différent
                handler.removeCallbacksAndMessages(null);
                final String text = response.body().string();

                if(text.equals("[]")) {
                    Log.i(TAG, "No data...");
                    return;
                }

                if(response.isSuccessful()) {
                    Log.i(TAG, "Success");

                    try {
                        computeData(new JSONArray(text));
                    } catch (JSONException e) {
                        Log.e(TAG, "" + e.getMessage());
                    }

                } else {
                    Log.i(TAG, "Fail");
                }
            }
        };

        Call response = okHttpClient.newCall(request);
        response.enqueue(callback);
        Log.i(TAG, response.request().toString());

        handler.postDelayed(() -> {
            Utils.cancelRequest(okHttpClient);
            Log.i(TAG, "Timeout error...");
            Toast.makeText(getActivity().getApplicationContext(), "Timeout error...", Toast.LENGTH_LONG).show();
        }, 10000);
    }

    public void computeData(JSONArray json) {
        for(int i = 0; i < json.length(); i++) {
            try {
                JSONObject child = json.getJSONObject(i);

                Log.i(TAG, "" + child.has("child"));

                if(!child.has("child")) {
                    OkHttpClient okHttpClient = new OkHttpClient();

                    int idRuche = child.getInt("id");
                    Request request = new Request.Builder()
                            .url("https://" + ((BizzbeeApp)getActivity().getApplication()).getServUrl() + urlRuche + "?ruche=" + idRuche)
                            .tag("ruche")
                            .build();

                    Callback callback = new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {}

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            //le retour est effectué dans un thread différent
                            handler.removeCallbacksAndMessages(null);
                            final String text = response.body().string();

                            if(text.equals("{}")) {
                                Log.i(TAG, "No data...");
                                return;
                            }

                            if(response.isSuccessful()) {
                                Log.i(TAG, "Success " + idRuche);

                                try {
                                    JSONObject json = new JSONObject(text);

                                    JSONObject actual = json.getJSONObject("actual");

                                    if(actual.has("humidite")) {
                                        humidite.add(Integer.parseInt(actual.getJSONObject("humidite").getString("val")));
                                    }

                                    if(actual.has("temperature")) {
                                        temperature.add(Integer.parseInt(actual.getJSONObject("temperature").getString("val")));
                                    }

                                    if(actual.has("poids")) {
                                        poids.add(Integer.parseInt(actual.getJSONObject("poids").getString("val")));
                                    }
                                } catch (JSONException e) {
                                    Log.e(TAG, "" + e.getMessage());
                                }
                            } else {
                                Log.i(TAG, "Fail");
                            }
                        }
                    };

                    Call response = okHttpClient.newCall(request);
                    response.enqueue(callback);
                    Log.i(TAG, response.request().toString());

                    handler.postDelayed(() -> {
                        Utils.cancelRequest(okHttpClient);
                        Log.i(TAG, "Timeout error...");
                    }, 10000);
                } else {
                    if(child.has("child")) {
                        computeData(child.getJSONArray("child"));
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
}
