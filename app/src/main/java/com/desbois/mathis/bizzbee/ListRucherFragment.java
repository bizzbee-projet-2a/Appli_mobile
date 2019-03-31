package com.desbois.mathis.bizzbee;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ListRucherFragment extends ListComponentFragment {
    private static String listRucherUrl = "/racine";

    private static final String TAG = "ListRucherFragment";

    private ProgressDialog progressDialog;

    private Handler handler = new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((MainActivity) getActivity()).setActionBarTitle("Ruchers");

        return inflater.inflate(R.layout.fragment_list_rucher, container, false);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getActivity(), "Item: " + mListe.get(position).getId(), Toast.LENGTH_SHORT).show();

        Intent babout = new Intent(getContext(), RucherActivity.class);
        babout.putExtra(RucherActivity.RUCHER_ID, ((Composant)parent.getItemAtPosition(position)).getId());

        startActivity(babout);
    }

    public void makeRequest() {
        progressDialog = new ProgressDialog(getContext());

        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Getting information...");
        progressDialog.show();

        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://" + ((BizzbeeApp)getActivity().getApplication()).getServUrl() + listRucherUrl)
                .tag("service")
                .build();

        Callback callback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.removeCallbacksAndMessages(null);
                progressDialog.dismiss();
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

                        getActivity().runOnUiThread(() -> {
                            ComposantAdapter adapter = new ComposantAdapter(mListe, getContext());
                            setListAdapter(adapter);
                            getListView().setOnItemClickListener(ListRucherFragment.this);
                        });
                    } catch (JSONException e) {
                        Log.e(TAG, "" + e.getMessage());
                    }

                } else {
                    Log.i(TAG, "Fail");
                }

                progressDialog.dismiss();
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
            Log.i(TAG, "Timeout error...");
            Toast.makeText(getContext(), "Timeout error...", Toast.LENGTH_LONG).show();
        }, 10000);
    }

    public void computeData(JSONArray json) {
        for(int i = 0; i < json.length(); i++) {
            try {
                JSONObject child = json.getJSONObject(i);

                Log.i(TAG, "" + child.has("child"));
                mListe.add(new Rucher(child.getInt("id"), child.getString("nom")));

            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    public void getDataFailed(String m) {
        Toast.makeText(getContext(), m, Toast.LENGTH_LONG).show();
    }
}
