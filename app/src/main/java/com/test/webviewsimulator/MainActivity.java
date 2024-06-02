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

    private static final String TAG = "MainActivity";
    private WebView myWebView;
    private static final String CHANNEL_ID = "js_alert_channel";
    private static final String URL = "http://192.168.1.108:8080"; // Static URL
    private static final int PERMISSION_REQUEST_CODE = 1;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();

        myWebView = findViewById(R.id.webview);
        myWebView.setWebViewClient(new myWebClient());
        myWebView.setWebChromeClient(new myWebChromeClient());
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        // Load the static URL
        myWebView.loadUrl(URL);
        Log.d(TAG, "Loading URL: " + URL);

        myWebView.addJavascriptInterface(new WebAppInterface(), "Android");

        // Check for notification permission
        checkNotificationPermission();
    }

    private void checkNotificationPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
        }
    }

    private static class myWebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.d(TAG, "Page started loading: " + url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            Log.d(TAG, "URL loading overridden: " + url);
            return true;
        }
    }

    private class myWebChromeClient extends WebChromeClient {
        @Override
        public boolean onConsoleMessage(android.webkit.ConsoleMessage consoleMessage) {
            Log.d(TAG, "Console message: " + consoleMessage.message());
            String message = consoleMessage.message();
            if (message.equals("motion motion: 1")) {
                // Send broadcast
                Intent intent = new Intent("motion motion: 1 with tag MainActivity");
                sendBroadcast(intent);
                Log.d(TAG, "Broadcast sent for motion: 1");
            }
            return super.onConsoleMessage(consoleMessage);
        }
    }

    @Override
    public void onBackPressed() {
        if (myWebView.canGoBack()) {
            myWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "JavaScript Alert Channel";
            String description = "Channel for JavaScript Alert notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public class WebAppInterface {
        @JavascriptInterface
        public void log(String message) {
            Log.d(TAG, "JavaScript interface log message: " + message);
            if ("motion: 1".equals(message)||"motion motion: 1".equals(message)) {
                Intent intent = new Intent("motion motion: 1 with tag MainActivity");
                sendBroadcast(intent);
                Log.d(TAG, "Broadcast sent for motion: 1");
            }
        }
    }
}
