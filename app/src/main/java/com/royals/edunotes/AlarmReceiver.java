package com.royals.edunotes;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import androidx.annotation.RequiresApi;

import com.royals.edunotes._activities.MainActivity;

import java.util.Random;

public class AlarmReceiver extends BroadcastReceiver {

    String[] text = {"Bored! Have fun with amazing Quotes",
            "Find some new and trending Quotes here",
            "Find your favourite Quotes here",
            "Hurry! Most trending Quotes are here"};

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
                PendingIntent.getActivity(context, NotificationHelper.ALARM_TYPE_RTC, intentToRepeat, PendingIntent.FLAG_UPDATE_CURRENT);

        //Build notification
        Notification repeatedNotification = buildLocalNotification(context, pendingIntent).build();

        //Send local notification
        NotificationHelper.getNotificationManager(context).notify(NotificationHelper.ALARM_TYPE_RTC, repeatedNotification);
    }

    public Notification.Builder buildLocalNotification(Context context, PendingIntent pendingIntent) {
        int idx = new Random().nextInt(text.length);
        String random = (text[idx]);
        Notification.Builder builder =
                (Notification.Builder) new Notification.Builder(context)
                        .setContentIntent(pendingIntent)
                        .setContentText(random)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.qwerty))
                        .setSmallIcon(R.mipmap.qwerty)
                        .setContentTitle("20000+ Best Quotes")
                        .setAutoCancel(true);

        return builder;
    }
}
