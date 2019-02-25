package com.desbois.mathis.bizzbee;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button mConnexionButton;
    private Button mInscriptionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mConnexionButton = (Button) findViewById(R.id.activity_main_connexion_button);
        mInscriptionButton = (Button) findViewById(R.id.activity_main_inscription_button);
    }
}
