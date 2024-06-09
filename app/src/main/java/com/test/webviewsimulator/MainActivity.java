package com.test.webviewsimulator;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.content.BroadcastReceiver;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity"; // Tag pour les logs
    private WebView myWebView; // Déclaration de l'objet WebView
    private static final String CHANNEL_ID = "js_alert_channel"; // ID du canal de notification
    private static final String URL = "http://192.168.130.166:8080/"; // URL à charger dans la WebView
    private static final int PERMISSION_REQUEST_CODE = 1; // Code de demande de permission

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Définit la vue du layout activity_main

        createNotificationChannel(); // Crée le canal de notification

        myWebView = findViewById(R.id.webview); // Lie l'objet WebView avec l'élément du layout
        myWebView.setWebViewClient(new MyWebClient()); // Définit le client WebView
        myWebView.setWebChromeClient(new MyWebChromeClient()); // Définit le client WebChrome
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true); // Active JavaScript
        webSettings.setDomStorageEnabled(true); // Active le stockage DOM

        myWebView.loadUrl(URL); // Charge l'URL
        Log.d(TAG, "Loading URL: " + URL); // Log de l'URL chargée

        myWebView.addJavascriptInterface(new WebAppInterface(), "Android"); // Ajoute une interface JavaScript

        checkNotificationPermission(); // Vérifie les permissions de notification

        // Enregistre le BroadcastReceiver
        IntentFilter filter = new IntentFilter("motion_motion_1");
        MotionDetectorBroadcastReceiver receiver = new MotionDetectorBroadcastReceiver();
        registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED);
    }

    private void checkNotificationPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Demande de la permission de notification si elle n'est pas déjà accordée
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
        } else {
            Log.d(TAG, "Notification permission already granted");
        }
    }

    private static class MyWebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.d(TAG, "Page started loading: " + url); // Log au début du chargement de la page
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            Log.d(TAG, "Overriding URL: " + url); // Log de l'URL surchargée
            return true;
        }
    }

    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onConsoleMessage(android.webkit.ConsoleMessage consoleMessage) {
            Log.d(TAG, "Console message: " + consoleMessage.message()); // Log des messages de la console JavaScript
            String message = consoleMessage.message();
            if (message.contains("motion") && message.contains("1")) {
                Intent intent = new Intent("motion_motion_1");
                sendBroadcast(intent);
                Log.d(TAG, "Broadcast sent for motion: 1 with message: " + message); // Envoi d'un broadcast pour un message spécifique
            }
            return super.onConsoleMessage(consoleMessage);
        }
    }

    @Override
    public void onBackPressed() {
        if (myWebView.canGoBack()) {
            myWebView.goBack(); // Retour à la page précédente dans WebView si possible
        } else {
            super.onBackPressed();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "JavaScript Alert Channel";
            String description = "Channel for JavaScript alert notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel); // Crée le canal de notification pour Android O et supérieur
        }
    }

    public static class MotionDetectorBroadcastReceiver extends BroadcastReceiver {
        private static final String TAG = "MotionDetectorBroadcastReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Broadcast received for action: " + intent.getAction()); // Log de la réception du broadcast
            if ("motion_motion_1".equals(intent.getAction())) {
                sendNotification(context);
            }
        }

        private void sendNotification(Context context) {
            Log.d(TAG, "Sending notification...");

            // Crée une intention explicite pour une activité dans votre application
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            // Crée l'action
            NotificationCompat.Action action = new NotificationCompat.Action.Builder(
                    R.drawable.ic_open, // Remplacez par votre icône d'action
                    "Open App", // Libellé du bouton d'action
                    pendingIntent
            ).build();

            // Construit la notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentTitle("Motion Detected")
                    .setContentText("A motion has been detected.")
                    .setSmallIcon(R.drawable.notification_icon)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .addAction(action); // Ajoute le bouton d'action ici

            // Vérifie la permission de notification
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission not granted to post notifications");
                return;
            }

            // Notifie l'utilisateur
            NotificationManagerCompat.from(context).notify(0, builder.build());
            Log.d(TAG, "Notification sent");
        }
    }

    public class WebAppInterface {
        @JavascriptInterface
        public void log(String message) {
            Log.d(TAG, "JavaScript log message: " + message);
            if (message.contains("motion") && message.contains("1")) {
                Intent intent = new Intent("motion_motion_1");
                sendBroadcast(intent);
                Log.d(TAG, "Broadcast sent for motion: 1 with message: " + message); // Envoi d'un broadcast pour un message spécifique de JavaScript
            }
        }
    }
}
