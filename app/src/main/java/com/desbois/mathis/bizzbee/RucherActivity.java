package com.desbois.mathis.bizzbee;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.TaskStackBuilder;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RucherActivity extends AppCompatActivity
        implements Connectable {
    private static final String url = "/getTreeFromRucher";
    private static final String TAG = "RucherActivity";

    public static final String RUCHER_ID_PARENT = "RucherActivity.idSrc";
    public static final String RUCHER_ID = "RucherActivity.idRucher";

    private static final int BAD_ID = -1;

    private int id;
    private Handler handler = new Handler();

    private ProgressDialog progressDialog;

    private Rucher mRucher;

    private TextView mTitle;
    private TextView mSubtitle;

    private RucherListeRuchesFragment ruchesFragment;

    private RucherPagerAdapter adapter;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rucher);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);

        actionbar.setTitle(R.string.rucher);

        mTitle = findViewById(R.id.title);
        mSubtitle = findViewById(R.id.subtitle);

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        ViewPager viewPager = findViewById(R.id.pager);
        adapter = new RucherPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        Log.i(TAG, "" + getIntent().getIntExtra(RUCHER_ID, BAD_ID));

        id = getIntent().getIntExtra(RUCHER_ID, BAD_ID);

        makeRequest();
    }

    @Override
    public void makeRequest() {
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
                .url("https://" + ((BizzbeeApp)getApplication()).getServUrl() + url + "?rucher=" + id)
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
                String nom = null;
                String idRucher = null;
                String idParent = null;

                progressDialog.dismiss();

                if(text.equals("[]")) {
                    runOnUiThread(() -> {
                        getDataFailed("No data...");
                    });
                }

                ArrayList<Composant> mChild = new ArrayList<>();
                try {
                    JSONArray array = new JSONArray(text);
                    JSONObject json = array.getJSONObject(0);

                    nom = json.getString("nom");
                    idRucher = json.getString("id");
                    idParent = json.getString("id_parent");

                    if(json.has("child")) {
                        JSONArray children = json.getJSONArray("child");

                        for(int i = 0; i < children.length(); i++) {
                            JSONObject tmpComp = children.getJSONObject(i);
                            String tmpNom = tmpComp.getString("nom");
                            int tmpId = tmpComp.getInt("id");

                            Composant child = new Composant(tmpId, tmpNom);
                            mChild.add(child);

                            Log.i(TAG, tmpNom + " " + tmpId);
                        }
                    }

                    mRucher = new Rucher(id, nom, Integer.parseInt(idParent), mChild);

                } catch (JSONException e) {
                    Log.e(TAG, "" + e.getMessage());
                }

                String finalNom = nom;
                String finalId = getString(R.string.subtitle) + " " + idRucher;

                while (!ruchesFragment.isReady());

                runOnUiThread(() -> {
                    if(response.isSuccessful()) {
                        mTitle.setText(finalNom);
                        mSubtitle.setText(finalId);

                        ruchesFragment.setmListe(mRucher.getRuches());
                        ruchesFragment.updateView();
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

    @Override
    public void computeData(JSONArray j) {}

    public void getDataFailed(String m) {
        Toast.makeText(getBaseContext(), m, Toast.LENGTH_LONG).show();
        this.finish();
    }

    public Rucher getRucher() {
        return mRucher;
    }

    public int getId() {
        return mRucher.getId();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return false;
    }

    @Override
    public void onPrepareSupportNavigateUpTaskStack(TaskStackBuilder builder) {
        super.onPrepareSupportNavigateUpTaskStack(builder);
        Intent rucherIntent = new Intent(this, RucherActivity.class);

        rucherIntent.putExtra(RucherActivity.RUCHER_ID, mRucher.getIdParent());

        builder.addNextIntent(rucherIntent);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof RucherListeRuchesFragment) {
            ruchesFragment = (RucherListeRuchesFragment) fragment;
        }
    }
}
