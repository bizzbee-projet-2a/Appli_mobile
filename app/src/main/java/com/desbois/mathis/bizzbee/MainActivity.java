package com.desbois.mathis.bizzbee;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import static android.view.View.*;

public class MainActivity extends Activity implements OnClickListener {

    private Button mConnexionButton;
    private Button mAboutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mConnexionButton = (Button) findViewById(R.id.activity_main_connexion_button);
        mAboutButton = (Button) findViewById(R.id.activity_main_about_button);

        mAboutButton.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {

                Intent babout = new Intent(MainActivity.this, ConnexionActivity.class);
                startActivity(babout);

    }
}
