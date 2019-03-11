package com.desbois.mathis.bizzbee;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class PasswordActivity extends Activity {
    private TextView mPassword_text;
    private EditText mPassword_mail;
    private Button mPassword_button;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mdp_oublie);
        mPassword_text = findViewById(R.id.mdp_oublie_text);
        mPassword_mail = findViewById(R.id.mdp_oublie_mail);
        mPassword_button = findViewById(R.id.mdp_oublie_button);
    }
}
