package com.royal.edunotes;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.royal.edunotes._activities.MainActivity;
import com.royal.edunotes._activities.NotificationDetailActivity;
import com.royal.edunotes._activities.VocabSearchActivity;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        String title = "Vocab Tricks Reels";
        String body = "";
        String searchQuery = "";

        // Check if message contains a data payload (Custom data sent from console)
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            Map<String, String> data = remoteMessage.getData();
            
            // You can send key "search" with value "diligent" from Firebase console
            if (data.containsKey("search")) {
                searchQuery = data.get("search");
            } else if (data.containsKey("vocab")) {
                searchQuery = data.get("vocab");
            } else if (data.containsKey("idiom")) {
                searchQuery = data.get("idiom");
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
        }

        // If body is empty but we have a search query, set body to the search query
        if ((body == null || body.isEmpty()) && !searchQuery.isEmpty()) {
            body = "Check out this word: " + searchQuery;
        }

        sendNotification(title, body, searchQuery);
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
        // If you want to send messages to this application instance or manage this apps subscriptions on the server side, send the FCM registration token to your app server.
    }

    private void sendNotification(String title, String messageBody, String searchQuery) {
        Intent intent;
        
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            // Open VocabSearchActivity and auto-search
            intent = new Intent(this, VocabSearchActivity.class);
            intent.putExtra(Utility.SEARCH_KEY, searchQuery);
        } else {
            // Open NotificationDetailActivity by default
            intent = new Intent(this, NotificationDetailActivity.class);
            intent.putExtra("title", title);
            intent.putExtra("message", messageBody);
        }
        
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT);

        String channelId = "fcm_default_channel";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.vocab_swap_logo)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
