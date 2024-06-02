package com.test.webviewsimulator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MotionDetectorBroadcastReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "js_alert_channel"; // ID du canal de notification

    @Override
    public void onReceive(Context context, Intent intent) {
        // Vérifier si l'action de l'intent correspond au message de la console
        if ("motion motion: 1 with tag MainActivity".equals(intent.getAction())) {
            // Créer et envoyer une notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentTitle("Mouvement détecté") // Titre de la notification
                    .setContentText("Un mouvement a été détecté.") // Texte de la notification
                    .setSmallIcon(R.drawable.notification_icon) // Icône de la notification
                    .setPriority(NotificationCompat.PRIORITY_HIGH) // Priorité de la notification
                    .setAutoCancel(true); // La notification se ferme automatiquement quand elle est cliquée

            // Envoyer la notification
            NotificationManagerCompat.from(context).notify(0, builder.build());
        }
    }
}
