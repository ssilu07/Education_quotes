package com.royal.edunotes;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import androidx.annotation.RequiresApi;

import com.royal.edunotes._activities.MainActivity;

import java.util.Random;

public class AlarmReceiver extends BroadcastReceiver {

    String[] text = {"Bored! Have fun with amazing Vocab",
            "Find some new and trending Vocab here",
            "Your daily vocab quiz is waiting!",
            "Don't break your streak! Learn new words today",
            "Time for your daily vocab practice!",
            "New vocab tricks are waiting for you"};

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onReceive(Context context, Intent intent) {
        //Get notification manager to manage/send notifications


        //Intent to invoke app when click on notification.
        //In this sample, we want to start/launch this sample app when user clicks on notification
        Intent intentToRepeat = new Intent(context, MainActivity.class);
        //set flag to restart/relaunch the app
        intentToRepeat.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        //Pending intent to handle launch of Activity in intent above
        PendingIntent pendingIntent =
                PendingIntent.getActivity(context, NotificationHelper.ALARM_TYPE_RTC, intentToRepeat, PendingIntent.FLAG_IMMUTABLE);

        //Build notification
        Notification repeatedNotification = buildLocalNotification(context, pendingIntent).build();

        //Send local notification
        NotificationHelper.getNotificationManager(context).notify(NotificationHelper.ALARM_TYPE_RTC, repeatedNotification);
    }

    private static final String CHANNEL_ID = "daily_vocab_channel";

    public Notification.Builder buildLocalNotification(Context context, PendingIntent pendingIntent) {
        // Create notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Daily Vocab Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Daily vocabulary learning reminders");
            NotificationHelper.getNotificationManager(context).createNotificationChannel(channel);
        }

        int idx = new Random().nextInt(text.length);
        String random = (text[idx]);

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(context);
        }

        builder.setContentIntent(pendingIntent)
                .setContentText(random)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.qwerty))
                .setSmallIcon(R.mipmap.qwerty)
                .setContentTitle("English Vocab")
                .setAutoCancel(true);

        return builder;
    }
}
