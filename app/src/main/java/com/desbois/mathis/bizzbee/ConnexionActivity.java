package com.desbois.mathis.bizzbee;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ConnexionActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView mNameText;
    private TextView mPasswordText;
    private TextView mPasswordForgetIt;
    private EditText mNameValue;
    private EditText mPasswordValue;
    private Button mConnexionButton;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connexion);
        mNameText = (TextView) findViewById(R.id.activity_connexion_name_text);
        mPasswordText = (TextView) findViewById(R.id.activity_connexion_password_text);
        mPasswordForgetIt = (TextView) findViewById(R.id.activity_connexion_password_forgetit);
        mNameValue = (EditText) findViewById(R.id.activity_connexion_name_value);
        mPasswordValue = (EditText) findViewById(R.id.activity_connexion_password_value);
        mConnexionButton = (Button) findViewById(R.id.activity_connexion_button_connexion);
        mPasswordForgetIt.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_connexion_password_forgetit:
                Intent password = new Intent(ConnexionActivity.this, PasswordActivity.class);
                startActivity(password);
                break;
        }
    }
}
