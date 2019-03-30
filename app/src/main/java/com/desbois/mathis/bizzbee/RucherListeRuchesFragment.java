package com.desbois.mathis.bizzbee;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// Instances of this class are fragments representing a single
// object in our collection.
public class RucherListeRuchesFragment extends ListComponentFragment {
    private static final String TAG = "RucherListeRuchesFrag";
    private static final String url = "/estrucher";
    private static final String listRucheUrl = "/getTreeFromRucher";

    private boolean ready = false;

    private Handler handler = new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_ruche, container, false);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getActivity(), "Item: " + position, Toast.LENGTH_SHORT).show();

        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://" + ((BizzbeeApp)getActivity().getApplication()).getServUrl() + url + "?ruche=" + id)
                .tag("ruche")
                .build();

        Callback callback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.removeCallbacksAndMessages(null);
                Log.i(TAG, "Failed");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //le retour est effectué dans un thread différent
                handler.removeCallbacksAndMessages(null);
                final String text = response.body().string();

                Intent babout;
                String key;
                if(response.isSuccessful() && text.equals("true")) {
                    babout = new Intent(getContext(), RucherActivity.class);
                    key = "idRucher";
                } else {
                    babout = new Intent(getContext(), RucheActivity.class);
                    key = "idRuche";
                }

                babout.putExtra(key, mListe.get(position).getId());
                babout.putExtra(RucherActivity.RUCHER_ID, ((RucherActivity)getActivity()).getId());

                startActivity(babout);
            }
        };

        Call response = okHttpClient.newCall(request);
        response.enqueue(callback);
        Log.i(TAG, response.request().toString());

        handler.postDelayed(() -> {
            Utils.cancelRequest(okHttpClient);
        }, 10000);
    }

    @Override
    public void makeRequest() {}

    @Override
    public void computeData(JSONArray json) {}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Log.i(TAG, "Ready");
        ready = true;
    }

    public void updateView() {
        getActivity().runOnUiThread(() -> {
            Log.i(TAG, "bjt " + mListe.isEmpty());


            ComposantAdapter adapter = new ComposantAdapter(mListe, getContext());
            setListAdapter(adapter);
            getListView().setOnItemClickListener(RucherListeRuchesFragment.this);
        });
    }

    public boolean isReady() {
        return ready;
    }
}
