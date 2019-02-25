package com.desbois.mathis.bizzbee;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button mConnexionButton;
    private Button mAboutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mConnexionButton = (Button) findViewById(R.id.activity_main_connexion_button);
        mAboutButton = (Button) findViewById(R.id.activity_main_about_button);
    }
}
