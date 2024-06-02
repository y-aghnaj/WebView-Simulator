package com.test.webviewsimulator;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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

    private static final String TAG = "MainActivity";
    private WebView myWebView;
    private static final String CHANNEL_ID = "js_alert_channel";
    private static final String URL = "http://test-cam.duckdns.org:8080/";
    private static final int PERMISSION_REQUEST_CODE = 1;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();

        myWebView = findViewById(R.id.webview);
        myWebView.setWebViewClient(new MyWebClient());
        myWebView.setWebChromeClient(new MyWebChromeClient());
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        myWebView.loadUrl(URL);
        Log.d(TAG, "Loading URL: " + URL);

        myWebView.addJavascriptInterface(new WebAppInterface(), "Android");

        checkNotificationPermission();

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter("motion_motion_1");
        MotionDetectorBroadcastReceiver receiver = new MotionDetectorBroadcastReceiver();
        registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED);
    }


    private void checkNotificationPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
        } else {
            Log.d(TAG, "Notification permission already granted");
        }
    }

    private static class MyWebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.d(TAG, "Page started loading: " + url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            Log.d(TAG, "Overriding URL: " + url);
            return true;
        }
    }

    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onConsoleMessage(android.webkit.ConsoleMessage consoleMessage) {
            Log.d(TAG, "Console message: " + consoleMessage.message());
            String message = consoleMessage.message();
            if (message.contains("motion 1")) {
                Intent intent = new Intent("motion_motion_1");
                sendBroadcast(intent);
                Log.d(TAG, "Broadcast sent for motion: 1 with message: " + message);
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
            String description = "Channel for JavaScript alert notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static class MotionDetectorBroadcastReceiver extends BroadcastReceiver {
        private static final String TAG = "MotionDetectorBroadcastReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Broadcast received for action: " + intent.getAction());
            if ("motion_motion_1".equals(intent.getAction())) {
                sendNotification(context);
            }
        }

        private void sendNotification(Context context) {
            Log.d(TAG, "Sending notification...");
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentTitle("Motion Detected")
                    .setContentText("A motion has been detected.")
                    .setSmallIcon(R.drawable.notification_icon)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission not granted to post notifications");
                return;
            }

            NotificationManagerCompat.from(context).notify(0, builder.build());
            Log.d(TAG, "Notification sent");
        }
    }

    public class WebAppInterface {
        @JavascriptInterface
        public void log(String message) {
            Log.d(TAG, "JavaScript log message: " + message);
            if ("motion: 1".equals(message) || "motion motion: 1".equals(message)) {
                Intent intent = new Intent("motion_motion_1");
                sendBroadcast(intent);
                Log.d(TAG, "Broadcast sent for motion: 1 with message: " + message);
            }
        }
    }
}
