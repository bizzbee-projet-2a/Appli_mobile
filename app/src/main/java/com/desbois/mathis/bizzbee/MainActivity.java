package com.desbois.mathis.bizzbee;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements WelcomeFragment.WelcomeListener {

    private DrawerLayout drawerLayout;
    private static final List<MenuItem> items = new ArrayList<>();
    private static int position = 0;


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

        Menu menu;

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

            if(position == pos) {
                return true;
            }

            Fragment fragment = null;
            FragmentManager fragmentManager = getSupportFragmentManager();
            Intent intent = null;

            switch(pos) {
                case 0:
                    fragment = new WelcomeFragment();
                    break;
                case 1:
                    intent = new Intent(MainActivity.this, ConnexionActivity.class);
                    break;
                case 2:
                    intent = new Intent(MainActivity.this, AboutActivity.class);
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
