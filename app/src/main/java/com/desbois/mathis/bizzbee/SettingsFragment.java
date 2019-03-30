package com.desbois.mathis.bizzbee;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;

public class SettingsFragment extends PreferenceFragment
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
        switch (key) {
            case "notif":
                SwitchPreference testPref = (SwitchPreference) findPreference("notif");
                boolean notif = sharedPreferences.getBoolean("notif", false);

                if (notif) {
                    testPref.setSummary("Enabled");
                    ((BizzbeeApp) getActivity().getApplication()).startBizzbeeService();
                } else {
                    testPref.setSummary("Disabled");
                    ((BizzbeeApp) getActivity().getApplication()).stopBizzbeeService();
                }

                break;
            case "serv_url": {
                String test_url = sharedPreferences.getString(key, "");
                EditTextPreference text = (EditTextPreference) findPreference(key);

                final ProgressDialog progressDialog = new ProgressDialog(getActivity());

                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Testing url...");
                progressDialog.show();

                if (!((BizzbeeApp) getActivity().getApplication()).setServUrl(test_url)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
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
                    Toast.makeText(getActivity(), "Oops... An error happened with server's url", Toast.LENGTH_LONG).show();
                } else {
                    Log.i("Bizzbee", "CCCC " + sharedPreferences.getString(key, "None"));
                    text.setSummary(text.getText());
                    Toast.makeText(getActivity(), "Server url set", Toast.LENGTH_LONG).show();
                }

                progressDialog.dismiss();
                break;
            }
            case "lim_humidite": {
                String limHumidite = sharedPreferences.getString(key, "");
                EditTextPreference text = (EditTextPreference) findPreference(key);

                try {
                    ((BizzbeeApp) getActivity().getApplication()).setSeuilHumidite(Double.parseDouble(limHumidite));
                    text.setSummary("Seuil : " + ((BizzbeeApp) getActivity().getApplication()).getSeuilHumidite());
                    Toast.makeText(getActivity(), "Humidity limit set", Toast.LENGTH_LONG).show();
                } catch (NumberFormatException e) {
                    Toast.makeText(getActivity(), "Must be a number...", Toast.LENGTH_LONG).show();
                    Utils.removeSharedPeferences(sharedPref, key);
                }
                break;
            }
            case "lim_temperature": {
                String limTemperature = sharedPreferences.getString(key, "");
                EditTextPreference text = (EditTextPreference) findPreference(key);

                try {
                    ((BizzbeeApp) getActivity().getApplication()).setSeuilTemperature(Double.parseDouble(limTemperature));
                    text.setSummary("Seuil : " + ((BizzbeeApp) getActivity().getApplication()).getSeuilTemperature());
                    Toast.makeText(getActivity(), "Temperature limit set", Toast.LENGTH_LONG).show();
                } catch (NumberFormatException e) {
                    Toast.makeText(getActivity(), "Must be a number...", Toast.LENGTH_LONG).show();
                    Utils.removeSharedPeferences(sharedPref, key);
                }
                break;
            }
            case "lim_poids": {
                String limPoids = sharedPreferences.getString(key, "");
                EditTextPreference text = (EditTextPreference) findPreference(key);

                try {
                    ((BizzbeeApp) getActivity().getApplication()).setSeuilPoids(Double.parseDouble(limPoids));
                    text.setSummary("Seuil : " + ((BizzbeeApp) getActivity().getApplication()).getSeuilPoids());
                    Toast.makeText(getActivity(), "Weight limit set", Toast.LENGTH_LONG).show();
                } catch (NumberFormatException e) {
                    Toast.makeText(getActivity(), "Must be a number...", Toast.LENGTH_LONG).show();
                    Utils.removeSharedPeferences(sharedPref, key);
                }
                break;
            }
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