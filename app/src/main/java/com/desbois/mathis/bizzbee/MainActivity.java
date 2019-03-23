package com.desbois.mathis.bizzbee;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Classe MainActivity.
 *
 * Activité principale de l'application. C'est sur celle-ci qu'arrive l'utilisateur au démarrage de
 * l'application. A partir d'elle, l'utilisateur peut accéder à la plupart des autres pages.
 * Elle contient un NavigationDrawer afin de faciliter la navigation entre les autres activités.
 *
 * On peut accéder à :
 * * Le tableau de bord (par défaut au démarrage)
 * * La liste des ruchers
 * * La connexion
 * * La déconnexion
 * * Les préférences
 * * La page "à propos"
 *
 * @see AppCompatActivity
 * @see com.desbois.mathis.bizzbee.WelcomeFragment.WelcomeListener
 *
 * @author Maxime Gautier
 */
public class MainActivity extends AppCompatActivity implements WelcomeFragment.WelcomeListener {

    public static final int CONNECTION = 0;
    public static final int CONNECTION_OK = 1;
    public static final int CONNECTION_BAD_URL = 2;

    private static final String TAG = "MainActivity";

    private static DrawerLayout drawerLayout;
    private NavigationView mNavigationView;

    private SharedPreferences sharedPref;

    private BizzbeeApp app;

    private Fragment prevFrag;

    /**
     * Création de l'activité.
     *
     * Initialise toutes les variables nécessaires au fonctionnement de l'application.
     * Etablit les préférences choisies par l'utilisateur ou celle par défaut.
     * Met en place la gestion de la navigation par le NavigationDrawer
     *
     * @param savedInstanceState Instance de l'état enregistrée avant que l'activité n'ait été détruite
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationView = findViewById(R.id.nav_view);

        drawerLayout = findViewById(R.id.drawer_layout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

        app = (BizzbeeApp) getApplicationContext();

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Fragment frag = (prevFrag == null) ? new WelcomeFragment() : prevFrag;

        if (sharedPref.contains("serv_url")) {
            app.setServUrl(sharedPref.getString("serv_url", ""));
            Log.i(TAG, "Add serv");
        }

        if(sharedPref.getBoolean("stay_connected", app.isConnected()) && app.isBizzbeeUrl()) {
            try {
                connect(sharedPref.getString("login", ""),
                        sharedPref.getString("pass", ""));
            } catch (CredentialsException e) {
                Log.e(TAG, "" + e.getMessage());
            }

            frag = (prevFrag == null) ? new WelcomeFragment() : prevFrag; // TODO a changer en tableau de bord
        } else {
            try {
                deconnect(this);
            } catch (CredentialsException e) {
                Log.e(TAG, "" + e.getMessage());
            }
        }

        FragmentManager fragManage = getSupportFragmentManager();

        fragManage.beginTransaction()
                .replace(R.id.flContent, frag)
                .commit();

        if(sharedPref.getBoolean("notif", true)) {
            app.startBizzbeeService();
        }

        setMenu(app.isConnected());

        mNavigationView.setNavigationItemSelectedListener(menuItem -> {
            drawerLayout.closeDrawers();

            // Add code here to update the UI based on the item selected
            // For example, swap UI fragments here

            Fragment fragment = null;
            FragmentManager fragmentManager = getSupportFragmentManager();
            Intent intent;

            switch(menuItem.getItemId()) {
                case R.id.nav_accueil:
                    fragment = new WelcomeFragment();
                    prevFrag = fragment;
                    break;
                case R.id.nav_dashboard:
                    fragment = new WelcomeFragment();
                    prevFrag = fragment;
                    break;
                case R.id.nav_rucher:
                    fragment = new ListRucherFragment();
                    prevFrag = fragment;
                    break;
                case R.id.nav_settings:
                    intent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    break;
                case R.id.nav_connect:
                    startConnectionActivity();
                    break;
                case R.id.nav_about:
                    intent = new Intent(MainActivity.this, AboutActivity.class);
                    startActivity(intent);
                    break;
                case R.id.nav_deconnect:
                    AlertDialog.Builder alert = new AlertDialog.Builder(this);
                    alert.setTitle("Do you want to logout ?");
                    alert.setMessage("You won't be able to access data until you reconnect.");

                    alert.setPositiveButton("Ok", (dialog, whichButton) -> {
                        try {
                            deconnect(this);
                        } catch (CredentialsException e) {
                            Toast.makeText(this, "Oops... Can't disconnect", Toast.LENGTH_LONG).show();
                        }
                    });

                    alert.setNegativeButton("Cancel", (dialog, whichButton) -> {});

                    alert.show();

                    break;
                default :
                    Toast.makeText(this, "Oops... There is an error", Toast.LENGTH_LONG).show();
                    return true;
            }

            if(fragment != null) {
                menuItem.setChecked(true);
                fragmentManager.beginTransaction()
                    .replace(R.id.flContent, fragment)
                    .commit();
            }

            return true;
        });

    }

    /**
     * Remise au premier plan de l'activité.
     *
     * Met à jour toutes les données nécessaires pour le bon fonctionnement de l'activité.
     * * L'url du serveur BizzBee
     * * Si l'option "Resté connecté" a été cochée
     * * Si l'utilisateur est connecté
     * * Sur quelle page était l'utilisateur quand il a quité l'activité
     */
    @Override
    public void onResume() {
        super.onResume();

        Log.i(TAG, "OnResume " + sharedPref.getBoolean("stay_connected", true)
                + " " + sharedPref.contains("stay_connected") + " " + app.isConnected());

        if (app.getServUrl().equals("") && sharedPref.contains("serv_url")) {
            app.setServUrl(sharedPref.getString("serv_url", ""));
            Log.i(TAG, "Add serv");
        } else {
            Log.i(TAG, "Not serv url");
        }

        Fragment frag = (prevFrag == null) ? new WelcomeFragment() : prevFrag;

        if(sharedPref.getBoolean("stay_connected", app.isConnected()) && app.isBizzbeeUrl()) {
            try {
                connect(sharedPref.getString("login", ""),
                        sharedPref.getString("pass", ""));
            } catch (CredentialsException e) {
                Log.e(TAG, "" + e.getMessage());
            }

            frag = (prevFrag == null) ? new WelcomeFragment() : prevFrag; // TODO a changer en tableau de bord
        } else {
            try {
                deconnect(this);
            } catch (CredentialsException e) {
                Log.e(TAG, "" + e.getMessage());
            }
        }

        FragmentManager fragManage = getSupportFragmentManager();

        fragManage.beginTransaction()
                .replace(R.id.flContent, frag)
                .commit();

        Log.i(TAG, "C'est a toi " + app.isConnected());

        setMenu(app.isConnected());
    }

    /**
     * Réception des réponses d'activités.
     *
     * Récupère les informations provenant d'activités afin de les mettre à jour.
     * * Pour l'activité ConnexionActivity
     * * * Mettre à jour les identifiants de session s'ils sont corrects
     * * * Affiche un message d'erreur si l'url du serveur BizzBee saisie est incorrect
     *
     * @param requestCode   Code de requête qui a été utilisé pour lancer l'activité
     * @param resultCode    Code de retour suite à la fin de l'activité appelée
     * @param data          Données à transmettre entre les 2 activités
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i(TAG, "OnActivityResult");

        if (requestCode == CONNECTION) {
            if(resultCode == CONNECTION_OK) {
                //result[0] = login : result[1] = pass
                ArrayList<String> result = data.getStringArrayListExtra("credentials");

                try {
                    connect(result.get(0), result.get(1));
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            } else if(resultCode == CONNECTION_BAD_URL) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle("Error")
                        .setMessage("L'url saisie est incorrect...")
                        .setNegativeButton("Ok", null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setCancelable(true);
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }

    /**
     * Connexion de l'utilisateur.
     *
     * Connecte l'utilisateur avec les identifiants passés en paramètre.
     *
     * @param l                         Login de l'utilisateur
     * @param p                         Mot de passe de l'utilisateur
     * @throws CredentialsException     Si on essaie de rentrer de nouveaux identifiants si l'on est déjà connecté
     */
    public void connect(String l, String p) throws CredentialsException {
        app.setCredentials(l, p);

        setMenu(app.isConnected());
    }

    /**
     * Déconnexion de l'utilisateur.
     *
     * Déconnecte l'utilisateur et réinitialise toutes les variables pour remettre l'application dans son
     * état d edépart.
     *
     * @param c                         Contexte dans lequel est appelé la méthode
     * @throws CredentialsException     Si on essaie de se déconnecter alors que l'utilisateur l'est déjà
     */
    public void deconnect(Context c) throws CredentialsException {
        Utils.removeSharedPeferences(sharedPref, "login");
        Utils.removeSharedPeferences(sharedPref, "pass");
        Utils.removeSharedPeferences(sharedPref, "stay_connected");

        app.setCredentials();

        Toast.makeText(c, "You have been disconnected !", Toast.LENGTH_LONG).show();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.flContent, new WelcomeFragment())
                .commit();

        setMenu(app.isConnected());
    }

    /**
     * Met à jour le menu du NavigationDrawer.
     *
     * Affiche et cache les menus du NavigationDrawer selon l'état de session de l'utilisateur (connecté ou non)
     *
     * @param connected     Etat de la session de l'utilisateur
     */
    private void setMenu(boolean connected) {
        Menu nav = mNavigationView.getMenu();

        nav.findItem(R.id.nav_connect).setVisible(!connected);
        nav.findItem(R.id.nav_accueil).setVisible(!connected);
        nav.findItem(R.id.nav_deconnect).setVisible(connected);
        nav.findItem(R.id.nav_dashboard).setVisible(connected);
        nav.findItem(R.id.nav_rucher).setVisible(connected);
    }

    /**
     * Mise à jour du titre dans l'ActionBar.
     *
     * Met à jour le titre de l'ActionBar de l'activité avec le nouveau titre passé en paramètre.
     *
     * @param title     Nouveau titre pour l'ActionBar
     *
     * @see ActionBar
     */
    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    /**
     * Appel de ConnexionActivity en attendant un résultat en retour.
     *
     * Appel ConnexionActivity avec la requête CONNECION en se mettant en attente d'un résultat de celle.
     *
     * @see ConnexionActivity
     */
    public void startConnectionActivity() {
        startActivityForResult(new Intent(MainActivity.this, ConnexionActivity.class), CONNECTION);
    }

    /**
     * Callback lors d'un clic sur l'item d'un menu.
     *
     * Callback appelée lorsque l'utilisateur clique sur l'item d'un menu et agit selon l'item cliqué.
     *
     * @param item      MenuItem qui a été cliqué
     * @return          Résultat de la méthode de la classe parente
     *
     * @see MenuItem
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Callback lors d'un clic sur le WelcomeFragment
     *
     * Callback appelée lorsque l'utilisateur clique dans un WelcomeFragment et démarre une activité selon le bouton cliqué.
     *
     * @param v     View sur laquelle l'utilisateur à cliqué
     *
     * @see View
     * @see WelcomeFragment
     */
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
