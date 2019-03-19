package com.desbois.mathis.bizzbee;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = SettingsFragment.class.getSimpleName();

    private SharedPreferences sharedPref;

    private String prev_url = "";

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);

        //sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
    }


    @Override
    public void onResume() {
        super.onResume();
        //unregister the preferenceChange listener
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("notif")) {
            SwitchPreference testPref = (SwitchPreference) findPreference("notif");
            boolean notif = sharedPreferences.getBoolean("notif", false);

            //Do whatever you want here. This is an example.
            if (notif) {
                testPref.setSummary("Enabled");
                ((BizzbeeApp)getActivity().getApplication()).startBizzbeeService();
            } else {
                testPref.setSummary("Disabled");
                ((BizzbeeApp)getActivity().getApplication()).stopBizzbeeService();
            }

        } else if(key.equals("serv_url")) {
            String test_url = sharedPreferences.getString(key, "");
            EditTextPreference text = (EditTextPreference) findPreference(key);

            final ProgressDialog progressDialog = new ProgressDialog(getContext());

            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Testing url...");
            progressDialog.show();

            if(!((BizzbeeApp)getActivity().getApplication()).setServUrl(test_url)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                        .setTitle("Error")
                        .setMessage("L'url saisie est incorrect...")
                        .setNegativeButton("Ok", null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setCancelable(true);
                AlertDialog alert = builder.create();
                alert.show();

                Utils.removeSharedPeferences(sharedPreferences, key);
                Log.i("Bizzbee", sharedPreferences.getString(key, "None"));

                text.setSummary("Please provide the server's url");
                Toast.makeText(getContext(), "Oops... An error happened with server's url", Toast.LENGTH_LONG).show();
            } else {
                Log.i("Bizzbee", "CCCC " + sharedPreferences.getString(key, "None"));
                text.setSummary(text.getText());
                Toast.makeText(getContext(), "Server url set", Toast.LENGTH_LONG).show();
            }

            progressDialog.dismiss();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //unregister the preference change listener
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}