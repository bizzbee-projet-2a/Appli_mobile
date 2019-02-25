package com.desbois.mathis.bizzbee;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

public class ButtonTestActivity extends Activity {

    Button bouton;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about2);
        bouton= (Button) findViewById(R.id.activity_main_about_button);
    }

}
