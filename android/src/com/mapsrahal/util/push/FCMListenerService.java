package com.mapsrahal.util.push;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mapsrahal.maps.MySharedPreference;
import com.mapsrahal.maps.R;
import com.mapsrahal.maps.SplashActivity;
import com.mapsrahal.util.Constants;

import java.util.Map;

import static com.mapsrahal.maps.MwmApplication.CHANNEL_ID;

public class FCMListenerService extends FirebaseMessagingService {
    private static final String TAG = FCMListenerService.class.getSimpleName();

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        //Log.d(TAG, "New Token " + s);
        //MySharedPreference.getInstance(getApplicationContext()).newTokenAvailable();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "Message from server " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {
            sendNotification(null, remoteMessage.getData());
        }
        if (remoteMessage.getNotification() != null) {
            sendNotification(remoteMessage.getNotification(), null);
        }
    }

    private void sendNotification(RemoteMessage.Notification notification,
                                  Map<String, String> data) {
        String title;
        String body;
        String flag;
        // todo get real data to process
        // ex: data.get("itemName")+" "+data.get("itemPrice")
        if(notification == null) {
            flag = data.get("mFlag");
            title = getFlagTitle(flag);
            body = getFlagBody(flag);
        } else {
            title = notification.getTitle();
            body  = notification.getBody();
        }
        //Log.d(TAG, "Message from server " + title + body);
        Intent intent = new Intent(this,  SplashActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this,CHANNEL_ID)
                        .setSmallIcon(R.drawable.about_logo)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }

    private String getFlagTitle(String flag) {
        String title = "";
    
        int mFlag = Integer.parseInt(flag);
        switch (mFlag) {
            case Constants.Notification.PASSENGER_REQUEST:
                title = "Passenger Request";
                break;
            case Constants.Notification.PASSENGER_ACCEPTED:
                title = "Passenger Accepted";
                break;
            
        }
        return title;
    }

    private String getFlagBody(String flag) {
        String body = "";

        int mFlag = Integer.parseInt(flag);
        switch (mFlag) {
            case Constants.Notification.PASSENGER_REQUEST:
                body = "Passenger Request";
                break;
            case Constants.Notification.PASSENGER_ACCEPTED:
                body = "Passenger Accepted";
                break;

        }
        return body;
    }

}
