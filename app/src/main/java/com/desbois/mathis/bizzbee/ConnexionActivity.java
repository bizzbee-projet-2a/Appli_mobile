package com.desbois.mathis.bizzbee;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ConnexionActivity extends AppCompatActivity implements View.OnClickListener {
    private static String connectionUrl = "/login";

    private static final String TAG = "ConnexionActivity";

    private EditText mNameValue;
    private EditText mPasswordValue;
    private Button mConnexionButton;
    private TextView mPasswordForget;
    private CheckBox mStayConnected;

    private String url = "";

    private Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connexion);

        if(((BizzbeeApp)getApplication()).isConnected()) {
            this.finish();
        }

        if(!((BizzbeeApp)getApplication()).isBizzbeeUrl()) {
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            View promptView = layoutInflater.inflate(R.layout.input_dialog, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
            alertDialogBuilder.setView(promptView);

            final EditText editText = promptView.findViewById(R.id.edittext);
            // setup a dialog window
            alertDialogBuilder.setCancelable(false)
                    .setPositiveButton("OK", (dialog, id) -> {
                        url = editText.getText().toString();
                        Log.i(TAG, url);

                        dialog.dismiss();

                        final ProgressDialog progressDialog = new ProgressDialog(ConnexionActivity.this);

                        progressDialog.setIndeterminate(true);
                        progressDialog.setMessage("Testing url...");
                        progressDialog.show();

                        if(!((BizzbeeApp) getApplication()).setServUrl(url)) {
                            progressDialog.dismiss();
                            setResult(MainActivity.CONNECTION_BAD_URL, new Intent());
                            this.finish();
                        } else {
                            progressDialog.dismiss();
                        }
                    })
                    .setNegativeButton("Annuler", (dialog, id) -> {
                        dialog.cancel();

                        setResult(MainActivity.CONNECTION_BAD_URL, new Intent());
                        this.finish();
                    });

            // create an alert dialog
            AlertDialog alert = alertDialogBuilder.create();
            alert.show();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);

        actionbar.setTitle(R.string.connexion);

        mNameValue = findViewById(R.id.activity_connexion_name_value);
        mPasswordValue = findViewById(R.id.activity_connexion_password_value);

        mConnexionButton = findViewById(R.id.activity_connexion_button_connexion);
        mPasswordForget = findViewById(R.id.activity_connexion_password_forgetit);

        mStayConnected = findViewById(R.id.stay_connected);

        mConnexionButton.setOnClickListener(this);

        mPasswordValue.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                Log.i(TAG,"Enter pressed");
                login();
            }
            return false;
        });
    }

    @Override
    public void onClick(View view) {
        Log.i(TAG, view.getId() + " " + R.id.activity_connexion_password_forgetit);
        switch (view.getId()) {
            case R.id.activity_connexion_button_connexion:
                login();
                break;
            case R.id.activity_connexion_password_forgetit:
                Intent intent = new Intent(this, PasswordActivity.class);
                startActivity(intent);
        }
    }

    public void login() {
        if (!validate()) {
            onLoginFailed("Login failed");
            return;
        }

        mConnexionButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(ConnexionActivity.this);

        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        OkHttpClient okHttpClient = new OkHttpClient();

        String login = mNameValue.getText().toString();
        String password = mPasswordValue.getText().toString();
        boolean stayConnected = mStayConnected.isChecked();

        RequestBody requestBody = new FormBody.Builder()
                .add("login", login)
                .add("password", password)
                .build();

        Log.i(TAG, "Login : " + login);
        Log.i(TAG, "Password : " + password);

        Request request = new Request.Builder()
                .url("https://" + ((BizzbeeApp)getApplication()).getServUrl() + connectionUrl)
                .tag("connection")
                .post(requestBody)
                .build();

        Log.i(TAG, request.body().contentType().toString());

        Callback callback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Log.i(TAG, "Failed");
                    Toast.makeText(ConnexionActivity.this, "Failed",
                            Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //le retour est effectué dans un thread différent
                final String text = response.body().string();

                runOnUiThread(() -> {
                    if(response.isSuccessful() && text.equals("OK")) {
                        onLoginSuccess(login, password, stayConnected);
                    } else {
                        onLoginFailed("Login failed");
                    }
                });

                progressDialog.dismiss();
            }
        };

        Call response = okHttpClient.newCall(request);
        response.enqueue(callback);
        Log.i(TAG, response.request().toString());

        progressDialog.setOnCancelListener((DialogInterface dialog) -> {
            Utils.cancelRequest(okHttpClient);
            onLoginFailed("Attempt canceled...");
            handler.removeCallbacksAndMessages(null);
        });

        handler.postDelayed(() -> {
            Utils.cancelRequest(okHttpClient);
            onLoginFailed("Timeout error...");
            progressDialog.dismiss();
        }, 10000);
    }

    public void onLoginSuccess(String l, String p, boolean s) {
        mConnexionButton.setEnabled(true);
        Toast.makeText(getBaseContext(), "Login successful", Toast.LENGTH_LONG).show();
        Log.i(TAG, "Connected");

        ArrayList<String> credentials = new ArrayList<>();
        credentials.add(l);
        credentials.add(p);
        Intent returnIntent = new Intent();
        returnIntent.putExtra("credentials", credentials);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(s) {
            Utils.addSharedPreferences(sharedPref, "stay_connected", s);
            Utils.addSharedPreferences(sharedPref, "login", l);
            Utils.addSharedPreferences(sharedPref, "pass", p);
        }

        setResult(MainActivity.CONNECTION_OK, returnIntent);

        handler.removeCallbacksAndMessages(null);
        this.finish();
    }

    public void onLoginFailed(String text) {
        mConnexionButton.setEnabled(true);
        Toast.makeText(getBaseContext(), text, Toast.LENGTH_LONG).show();
        Log.i(TAG, "Not connected");
    }

    public boolean validate() {
        boolean valid = true;

        String login = mNameValue.getText().toString();
        String password = mPasswordValue.getText().toString();

        if (login.isEmpty()) {
            mNameValue.setError("enter a valid login");
            valid = false;
        } else {
            mNameValue.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            mPasswordValue.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            mPasswordValue.setError(null);
        }

        return valid;
    }
}
