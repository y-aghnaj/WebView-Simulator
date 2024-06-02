package com.test.webviewsimulator;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity"; // Tag pour les journaux
    private WebView myWebView; // Instance de WebView
    private static final String CHANNEL_ID = "js_alert_channel"; // ID du canal de notification
    private static final String URL = "http://192.168.1.108:8080"; // URL statique pour charger dans WebView
    private static final int PERMISSION_REQUEST_CODE = 1; // Code de demande de permission

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel(); // Créer un canal de notification pour les alertes JS

        myWebView = findViewById(R.id.webview); // Initialiser WebView depuis le layout
        myWebView.setWebViewClient(new myWebClient()); // Définir un WebViewClient personnalisé
        myWebView.setWebChromeClient(new myWebChromeClient()); // Définir un WebChromeClient personnalisé
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true); // Activer JavaScript
        webSettings.setDomStorageEnabled(true); // Activer le stockage DOM

        // Charger l'URL statique
        myWebView.loadUrl(URL);
        Log.d(TAG, "Chargement de l'URL : " + URL);

        myWebView.addJavascriptInterface(new WebAppInterface(), "Android"); // Ajouter une interface JavaScript

        // Vérifier la permission de notification
        checkNotificationPermission();
    }

    // Méthode pour vérifier et demander la permission de notification
    private void checkNotificationPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
        }
    }

    // Classe personnalisée WebViewClient pour gérer les événements de chargement de page
    private static class myWebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.d(TAG, "Page commencée à charger : " + url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url); // Charger la nouvelle URL
            Log.d(TAG, "Chargement de l'URL remplacé : " + url);
            return true;
        }
    }

    // Classe personnalisée WebChromeClient pour gérer les messages de console et autres événements web
    private class myWebChromeClient extends WebChromeClient {
        @Override
        public boolean onConsoleMessage(android.webkit.ConsoleMessage consoleMessage) {
            Log.d(TAG, "Message de console : " + consoleMessage.message());
            String message = consoleMessage.message();
            if (message.equals("motion motion: 1")) {
                // Envoyer une diffusion lorsque un message de console spécifique est reçu
                Intent intent = new Intent("motion motion: 1 with tag MainActivity");
                sendBroadcast(intent);
                Log.d(TAG, "Diffusion envoyée pour motion: 1");
            }
            return super.onConsoleMessage(consoleMessage);
        }
    }

    // Remplacer le comportement du bouton retour pour naviguer dans l'historique de WebView
    @Override
    public void onBackPressed() {
        if (myWebView.canGoBack()) {
            myWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    // Méthode pour créer un canal de notification pour les appareils exécutant Android O et supérieur
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Canal d'alerte JavaScript";
            String description = "Canal pour les notifications d'alerte JavaScript";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // Classe d'interface JavaScript pour permettre l'interaction depuis le JavaScript de la page web
    public class WebAppInterface {
        @JavascriptInterface
        public void log(String message) {
            Log.d(TAG, "Message de log de l'interface JavaScript : " + message);
            if ("motion: 1".equals(message) || "motion motion: 1".equals(message)) {
                // Envoyer une diffusion lorsque un message spécifique est reçu du JavaScript
                Intent intent = new Intent("motion motion: 1 with tag MainActivity");
                sendBroadcast(intent);
                Log.d(TAG, "Diffusion envoyée pour motion: 1");
            }
        }
    }
}
