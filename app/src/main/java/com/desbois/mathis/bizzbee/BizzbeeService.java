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
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

public class BizzbeeService extends Service {
    private static final String url = "";

    private static final int NOTIF_ID = 1;
    private static final int NOTIF_ID_ALERT = 2;
    private static final String NOTIF_CHANNEL_ID = "Bizzbee_Id";

    private SharedPreferences sharedPref;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        // do your jobs here
        createNotificationChannel();

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        startForeground();

        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();

        // Create the Handler object (on the main thread by default)
        Handler handler = new Handler();

        // Define the code block to be executed
        Runnable runnableCode = new Runnable() {
            @Override
            public void run() {
                // Do something here on the main thread
                if(sharedPref.getBoolean("notif", false)) {
                    Toast.makeText(getApplicationContext(), "On y retourne", Toast.LENGTH_SHORT).show();

                    makeRequest();
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

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Service done", Toast.LENGTH_SHORT).show();
        NotificationManagerCompat.from(this).cancelAll();
    }


    private int i = 0;
    public void makeRequest() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIF_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_buzzbee)
                .setContentTitle("Test title")
                .setContentText("Test content " + i)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        i++;

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.cancel(NOTIF_ID_ALERT);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(NOTIF_ID_ALERT, builder.build());
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