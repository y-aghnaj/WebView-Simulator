package com.test.webviewsimulator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MotionDetectorBroadcastReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "js_alert_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Check if the intent action matches the console message
        if ("motion motion: 1 with tag MainActivity".equals(intent.getAction())) {
            // Create and send a notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentTitle("Motion Detected")
                    .setContentText("Motion has been detected.")
                    .setSmallIcon(R.drawable.notification_icon)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            NotificationManagerCompat.from(context).notify(0, builder.build());
        }
    }
}
