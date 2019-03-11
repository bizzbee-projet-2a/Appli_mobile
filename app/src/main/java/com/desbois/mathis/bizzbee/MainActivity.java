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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements WelcomeFragment.WelcomeListener {

    private static DrawerLayout drawerLayout;
    private static final List<MenuItem> items = new ArrayList<>();
    private static int position = 0;

    private static Menu menu;

    private static boolean connected = false;
    private static String login = "";
    private static String pass = "";


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
            // close drawer when item is tapped
            drawerLayout.closeDrawers();

            // Add code here to update the UI based on the item selected
            // For example, swap UI fragments here

            int pos = items.indexOf(menuItem);

            Fragment fragment = null;
            FragmentManager fragmentManager = getSupportFragmentManager();
            Intent intent = null;

            switch(menuItem.getItemId()) {
                case R.id.nav_accueil:
                    fragment = new WelcomeFragment();
                    break;
                case R.id.nav_connect:
                    intent = new Intent(MainActivity.this, ConnexionActivity.class);
                    break;
                case R.id.nav_about:
                    intent = new Intent(MainActivity.this, AboutActivity.class);
                    break;
                case R.id.nav_deconnect:
                    try {
                        deconnect(this);
                    } catch (CredentialsException e) {
                        Toast.makeText(this, "Oops... Can't deconnect", Toast.LENGTH_LONG).show();
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
            } else if(intent != null) {
                startActivity(intent);
            }

            return true;
        });

    }

    public static void connect(String l, String p) throws CredentialsException {
        setCredentials(l, p);
        connected = true;

        getMenuItem(R.id.nav_connect).setVisible(false);
        getMenuItem(R.id.nav_deconnect).setVisible(true);
    }

    public static void deconnect(Context c) throws CredentialsException {
        setCredentials("","");
        connected = false;
        Toast.makeText(c, "You have been deconnected !", Toast.LENGTH_LONG).show();

        getMenuItem(R.id.nav_connect).setVisible(true);
        getMenuItem(R.id.nav_deconnect).setVisible(false);
    }

    private static void setCredentials(String l, String p) throws CredentialsException {
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

    public static MenuItem getMenuItem(int id) {
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

    public static boolean isConnected() {
        return connected;
    }

    public static String getLogin() {
        return login;
    }

    public static String getPass() {
        return pass;
    }

    public static String getAuthorization() throws CredentialsException {
        if(connected) {
            return Base64.encodeToString((login + ":" + pass).getBytes(), Base64.DEFAULT);
        } else {
            throw new CredentialsException();
        }
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
                Intent bconnexion = new Intent(MainActivity.this, ConnexionActivity.class);
                startActivity(bconnexion);
                break;
            case R.id.activity_main_about_button:
                Intent babout = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(babout);
        }
    }
}
