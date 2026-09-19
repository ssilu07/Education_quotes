package com.royal.edunotes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && "android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            NotificationHelper.scheduleRepeatingRTCNotification(context, "9", "30");
        }
    }
}
