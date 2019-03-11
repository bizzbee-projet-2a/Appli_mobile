package com.desbois.mathis.bizzbee;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ConnexionActivity extends AppCompatActivity implements View.OnClickListener {
    private static String connectionUrl = "https://bizzbee.maximegautier.fr/login";

    private static final String TAG = "ConnexionActivity";
    private static final int REQUEST_SIGNUP = 0;

    private EditText mNameValue;
    private EditText mPasswordValue;
    private Button mConnexionButton;
    private TextView mPasswordForget;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connexion);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);

        actionbar.setTitle(R.string.connexion);

        mNameValue = findViewById(R.id.activity_connexion_name_value);
        mPasswordValue = findViewById(R.id.activity_connexion_password_value);

        mConnexionButton = findViewById(R.id.activity_connexion_button_connexion);
        mPasswordForget = findViewById(R.id.activity_connexion_password_forgetit);

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
        Log.i("TEST", view.getId() + " " + R.id.activity_connexion_password_forgetit);
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
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        new Handler().postDelayed(() -> onLoginFailed("Timeout error..."), 10000);

        OkHttpClient okHttpClient = new OkHttpClient();

        String login = mNameValue.getText().toString();
        String password = mPasswordValue.getText().toString();

        RequestBody requestBody = new FormBody.Builder()
                .add("login", login)
                .add("password", password)
                .build();

        Log.i(TAG, "Login : " + login);
        Log.i(TAG, "Password : " + password);

        Request request = new Request.Builder()
                .url(connectionUrl)
                .post(requestBody)
                .build();

        Log.i(TAG, request.body().contentType().toString());

        Callback callback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Log.i("Connection", "Failed");
                    Toast.makeText(ConnexionActivity.this, "Failed",
                            Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //le retour est effectué dans un thread différent
                final String text = response.body().string();

                runOnUiThread(() -> {
                    if(text.equals("OK")) {
                        onLoginSuccess(login, password);
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

    public void onLoginSuccess(String l, String p) {
        mConnexionButton.setEnabled(true);
        Toast.makeText(getBaseContext(), "Login successful", Toast.LENGTH_LONG).show();
        Log.i(TAG, "Connected");

        try {
            MainActivity.connect(l, p);
        } catch (CredentialsException e) {
            Log.e("Bizzbee", e.getMessage());
        }

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
