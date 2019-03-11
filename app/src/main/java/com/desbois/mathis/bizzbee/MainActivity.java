package com.desbois.mathis.bizzbee;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements WelcomeFragment.WelcomeListener {

    public static final int CONNECTION = 0;
    public static final int CONNECTION_OK = 1;

    private static final String TAG = "MainActivity";

    private static DrawerLayout drawerLayout;
    private final List<MenuItem> items = new ArrayList<>();
    private int position = 0;

    private Menu menu;

    private boolean connected = false;
    private String login = "";
    private String pass = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_drawer);

        drawerLayout = findViewById(R.id.drawer_layout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

        NavigationView navigationView = findViewById(R.id.nav_view);
        menu = navigationView.getMenu();

        for(int i = 0; i < menu.size(); i++){
            items.add(menu.getItem(i));
        }

        Fragment frag = new WelcomeFragment();
        FragmentManager fragManage = getSupportFragmentManager();

        fragManage.beginTransaction()
                .replace(R.id.flContent, frag)
                .commit();

        navigationView.setNavigationItemSelectedListener(menuItem -> {
            drawerLayout.closeDrawers();

            // Add code here to update the UI based on the item selected
            // For example, swap UI fragments here

            int pos = items.indexOf(menuItem);

            Fragment fragment = null;
            FragmentManager fragmentManager = getSupportFragmentManager();

            switch(menuItem.getItemId()) {
                case R.id.nav_accueil:
                    fragment = new WelcomeFragment();
                    break;
                case R.id.nav_connect:
                    startConnectionActivity();
                    break;
                case R.id.nav_about:
                    Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                    startActivity(intent);
                    break;
                case R.id.nav_deconnect:
                    try {
                        deconnect(this);
                    } catch (CredentialsException e) {
                        Toast.makeText(this, "Oops... Can't disconnect", Toast.LENGTH_LONG).show();
                    }

                    break;
                default :
                    Toast.makeText(this, "Oops... There is an error", Toast.LENGTH_LONG).show();
                    return true;
            }

            position = pos;
            if(fragment != null) {
                menuItem.setChecked(true);
                fragmentManager.beginTransaction()
                    .replace(R.id.flContent, fragment)
                    .commit();
            }

            return true;
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CONNECTION) {
            if(resultCode == MainActivity.CONNECTION_OK) {
                //result[0] = login : result[1] = pass
                ArrayList<String> result = data.getStringArrayListExtra("credentials");

                try {
                    connect(result.get(0), result.get(1));
                } catch (CredentialsException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
    }

    public void connect(String l, String p) throws CredentialsException {
        setCredentials(l, p);
        connected = true;

        getMenuItem(R.id.nav_connect).setVisible(false);
        getMenuItem(R.id.nav_deconnect).setVisible(true);
    }

    public void deconnect(Context c) throws CredentialsException {
        setCredentials("","");
        connected = false;
        Toast.makeText(c, "You have been disconnected !", Toast.LENGTH_LONG).show();

        getMenuItem(R.id.nav_connect).setVisible(true);
        getMenuItem(R.id.nav_deconnect).setVisible(false);
    }

    private void setCredentials(String l, String p) throws CredentialsException {
        if(!connected) {
            if(login.equals("")) {
                login = l;
            } else {
                throw new CredentialsException();
            }

            if(pass.equals("")) {
                pass = p;
            } else {
                throw new CredentialsException();
            }
        } else {
            if(!login.equals("")) {
                login = l;
            } else {
                throw new CredentialsException();
            }

            if(!pass.equals("")) {
                pass = p;
            } else {
                throw new CredentialsException();
            }
        }

    }

    public MenuItem getMenuItem(int id) {
        boolean trouve = false;
        int i = 0;
        MenuItem res = null;

        while(!trouve && i < items.size()) {
            if (items.get(i).getItemId() == id) {
                trouve = true;
                res = items.get(i);
            }

            i++;
        }

        return res;
    }

    public boolean isConnected() {
        return connected;
    }

    public String getLogin() {
        return login;
    }

    public String getPass() {
        return pass;
    }

    public String getAuthorization() throws CredentialsException {
        if(connected) {
            return Base64.encodeToString((login + ":" + pass).getBytes(), Base64.DEFAULT);
        } else {
            throw new CredentialsException();
        }
    }

    public void startConnectionActivity() {
        startActivityForResult(new Intent(MainActivity.this, ConnexionActivity.class), CONNECTION);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onButtonClick(View v) {
        switch (v.getId()) {
            case R.id.activity_main_connexion_button:
                startConnectionActivity();
                break;
            case R.id.activity_main_about_button:
                Intent babout = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(babout);
        }
    }
}
