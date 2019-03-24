package com.desbois.mathis.bizzbee;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Service BizzBee.
 *
 * Service s'exécutant en arrière-plan qui sert à alerter l'utilisateurs du dépassement de valeurs
 * sur les ruches via des notifications
 *
 * @see Service
 *
 * @author Maxime Gautier
 */
public class BizzbeeService extends Service {
    private static final String TAG = "BizzBeeService";
    private static final String urlTree = "/getTree";
    private static final String urlRuche = "/rucheInfos";

    private static final int SUMMARY_ID = 0;
    private static final int NOTIF_ID = 1;
    private static final int NOTIF_ID_ALERT = 2;
    private static final String NOTIF_CHANNEL_ID = "Bizzbee_Id";

    private static String GROUP_KEY_ALERT = "ALERT_GROUP";

    private SharedPreferences sharedPref;

    private Handler handler = new Handler();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Démarrage du service BizzBee.
     *
     * Méthode étant appelé lors du démarrage du service BizzBee.
     * Met en place tout le nécessaire afin de démarrer le service :
     * * Mettre en place la boucle de requètes selon un intervalle de temps
     * * Afficher la notification spécifiant le fonctionnement du service en arrière-plan (requis pour API > 28)
     *
     * @param intent    Intent à partir duquel le service est lancé.
     * @param flags     Flags spécifiés lors du démarage du service.
     * @param startId   Identifiant de départ.
     *
     * @return          Int spécifiant le comportement du service si celui-ci est tué.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        // do your jobs here
        createNotificationChannel();

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        startForeground();

        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();

        // Create the Handler object (on the main thread by default)
        Handler handler = new Handler();

        // Define the code block to be executed
        Runnable runnableCode = new Runnable() {
            @Override
            public void run() {
                // Do something here on the main thread
                if(sharedPref.getBoolean("notif", false)) {
                    Log.i(TAG, "" + ((BizzbeeApp)getApplication()).isBizzbeeUrl());

                    if(!((BizzbeeApp)getApplication()).isBizzbeeUrl()) {
                        Toast.makeText(getApplicationContext(), "You cannot enable notifications without providing URL", Toast.LENGTH_LONG).show();
                        Utils.removeSharedPeferences(sharedPref, "notif");
                        ((BizzbeeApp)getApplication()).stopBizzbeeService();
                    } else {
                        makeRequest();
                    }
                }

                // Repeat this the same runnable code block again another 2 seconds
                // 'this' is referencing the Runnable object
                // TODO voir délai de chaque requete
                handler.postDelayed(this, 10000);
            }
        };

        // Start the initial runnable task by posting through the handler
        handler.post(runnableCode);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    /**
     * Affichage de la notification de fonctionnement en arrière-plan.
     *
     * Affiche une notification montrant le fonctionnement en arrière-plan de l'application.
     * Notification requise pour les terminaux sous Android > 8 (API > 28).
     */
    private void startForeground() {
        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        startForeground(NOTIF_ID, new NotificationCompat.Builder(this,
                NOTIF_CHANNEL_ID) // don't forget create a notification channel first
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_buzzbee)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Service is running background")
                .setContentIntent(pendingIntent)
                .build());
    }

    /**
     * Destruction du service.
     *
     * Détruit le service BizzBee en le spécifiant à l'utilisateur et en supprimant toutes les notifications
     * relatives à BizzBee.
     */
    @Override
    public void onDestroy() {
        Toast.makeText(this, "Service done", Toast.LENGTH_SHORT).show();
        NotificationManagerCompat.from(this).cancelAll();
    }

    /**
     * Requète de mise à jour.
     *
     * Requète sur l'url <URL_SERVEUR>/getTree?apiculteur=<ID> afin d'analyser si une ruche a dépassé
     * un des facteurs spécifié.
     */
    public void makeRequest() {
        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://" + ((BizzbeeApp)getApplication()).getServUrl() + urlTree + "?apiculteur=1")
                .tag("service")
                .build();

        Callback callback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.removeCallbacksAndMessages(null);
                Log.i(TAG, "Failed");
                Log.i(TAG, e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //le retour est effectué dans un thread différent
                handler.removeCallbacksAndMessages(null);
                final String text = response.body().string();

                if(text.equals("[]")) {
                    Log.i(TAG, "No data...");
                    return;
                }

                if(response.isSuccessful()) {
                    Log.i(TAG, "Success");

                    try {
                        computeData(new JSONArray(text));
                    } catch (JSONException e) {
                        Log.e(TAG, "" + e.getMessage());
                    }

                } else {
                    Log.i(TAG, "Fail");
                }
            }
        };

        Call response = okHttpClient.newCall(request);
        response.enqueue(callback);
        Log.i(TAG, response.request().toString());

        handler.postDelayed(() -> {
            Utils.cancelRequest(okHttpClient);
            Log.i(TAG, "Timeout error...");
            Toast.makeText(getApplicationContext(), "Timeout error...", Toast.LENGTH_LONG).show();
        }, 10000);
    }

    public void computeData(JSONArray json) {
        for(int i = 0; i < json.length(); i++) {
            try {
                JSONObject child = json.getJSONObject(i);

                Log.i(TAG, "" + child.has("child"));

                if(child.getInt("isrucher") < 0) {
                    OkHttpClient okHttpClient = new OkHttpClient();

                    int idRuche = child.getInt("id");
                    Request request = new Request.Builder()
                            .url("https://" + ((BizzbeeApp)getApplication()).getServUrl() + urlRuche + "?ruche=" + idRuche)
                            .tag("ruche")
                            .build();

                    Callback callback = new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {}

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            //le retour est effectué dans un thread différent
                            handler.removeCallbacksAndMessages(null);
                            final String text = response.body().string();

                            if(text.equals("{}")) {
                                Log.i(TAG, "No data...");
                                return;
                            }

                            if(response.isSuccessful()) {
                                Log.i(TAG, "Success " + idRuche);

                                try {
                                    JSONObject json = new JSONObject(text);

                                    JSONObject actual = json.getJSONObject("actual");

                                    String notifText = "There is a problem with hive " + json.getString("name");
                                    boolean isGood = true;
                                    String grandText = "";

                                    if(actual.has("humidite")) {
                                        double humid = Double.parseDouble(actual.getJSONObject("humidite").getString("val"));
                                        if(humid > 50L) {
                                            grandText += "\t- Humidite : " + humid + "\n";
                                            isGood = false;
                                        }
                                    }

                                    if(actual.has("temperature")) {
                                        double temp = Double.parseDouble(actual.getJSONObject("temperature").getString("val"));
                                        if(temp > 50L) {
                                            grandText += "\t- Temperature : " + temp + "\n";
                                            isGood = false;
                                        }
                                    }

                                    if(actual.has("poids")) {
                                        double poids = Double.parseDouble(actual.getJSONObject("poids").getString("val"));
                                        if(poids > 50L) {
                                            grandText += "\t- Poids : " + poids + "\n";
                                            isGood = false;
                                        }
                                    }

                                    if(!isGood) {
                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), NOTIF_CHANNEL_ID)
                                                .setSmallIcon(R.drawable.ic_buzzbee)
                                                .setStyle(new NotificationCompat.BigTextStyle()
                                                        .bigText(notifText + "\n" + grandText))
                                                .setContentTitle("Alerte")
                                                .setContentText(notifText)
                                                .setGroup(GROUP_KEY_ALERT)
                                                .setDefaults(Notification.DEFAULT_ALL)
                                                .setPriority(NotificationCompat.PRIORITY_HIGH);

                                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

                                        // notificationId is a unique int for each notification that you must define
                                        notificationManager.notify(NOTIF_ID_ALERT + idRuche, builder.build());

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            Notification summaryNotification =
                                                    new NotificationCompat.Builder(getApplicationContext(), NOTIF_CHANNEL_ID)
                                                            .setContentTitle("Alerte(s)")
                                                            //set content text to support devices running API level < 24
                                                            .setContentText("New alert(s)")
                                                            .setSmallIcon(R.drawable.ic_buzzbee)
                                                            //build summary info into InboxStyle template
                                                            .setStyle(new NotificationCompat.InboxStyle()
                                                                    .addLine("You have multiple alerts on your hives !")
                                                                    .setSummaryText("Alerte sur vos ruches !"))
                                                            //specify which group this notification belongs to
                                                            .setGroup(GROUP_KEY_ALERT)
                                                            //set this notification as the summary for the group
                                                            .setGroupSummary(true)
                                                            .build();

                                            notificationManager.notify(SUMMARY_ID, summaryNotification);
                                        }

                                    }
                                } catch (JSONException e) {
                                    Log.e(TAG, "" + e.getMessage());
                                }
                            } else {
                                Log.i(TAG, "Fail");
                            }
                        }
                    };

                    Call response = okHttpClient.newCall(request);
                    response.enqueue(callback);
                    Log.i(TAG, response.request().toString());

                    handler.postDelayed(() -> {
                        Utils.cancelRequest(okHttpClient);
                        Log.i(TAG, "Timeout error...");
                    }, 10000);
                } else {
                    if(child.has("child")) {
                        computeData(child.getJSONArray("child"));
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = NOTIF_CHANNEL_ID;
            String description = "Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIF_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}