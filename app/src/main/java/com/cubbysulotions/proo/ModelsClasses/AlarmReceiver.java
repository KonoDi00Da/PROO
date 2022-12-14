package com.cubbysulotions.proo.ModelsClasses;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

import com.cubbysulotions.proo.MainActivity.MainActivity;
import com.cubbysulotions.proo.SplashScreen;

import java.util.Random;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //Get id and message from intent

        int notificationID = intent.getIntExtra("notificationID", 0);
        int requestCode = intent.getIntExtra("requestCode", 0);
        String message = intent.getStringExtra("todo");

        //When notification is tapped call main activity
        Intent main = new Intent(context, SplashScreen.class);

        PendingIntent contentIntent = PendingIntent.getActivity(context,
                requestCode,
                main,
                PendingIntent.FLAG_UPDATE_CURRENT| PendingIntent.FLAG_IMMUTABLE);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        //Prepare notification
        Notification.Builder builder = new Notification.Builder(context);
        builder.setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("It's Time")
                .setContentText(message)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL);

        //notify

        notificationManager.notify(notificationID, builder.build());
    }
}
