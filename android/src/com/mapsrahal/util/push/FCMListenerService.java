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
import com.google.gson.Gson;
import com.mapsrahal.maps.MapActivity;
import com.mapsrahal.maps.MwmApplication;
import com.mapsrahal.maps.MySharedPreference;
import com.mapsrahal.maps.R;
import com.mapsrahal.maps.SplashActivity;
import com.mapsrahal.maps.activity.ResultActivity;
import com.mapsrahal.util.Constants;

import java.util.Map;

import static com.mapsrahal.maps.MwmApplication.CHANNEL_ID;
import static com.mapsrahal.maps.MwmApplication.CHANNEL_ID_NOTIFY;

public class FCMListenerService extends FirebaseMessagingService {
    private static final String TAG = FCMListenerService.class.getSimpleName();
    Gson gson = new Gson();
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
            sendJustNotify(remoteMessage.getNotification());
            //sendNotification(remoteMessage.getNotification(), null);
        }
    }

    private void sendJustNotify(RemoteMessage.Notification notification) {
        //String title;
        //String body;
        String flag;
        String title = notification.getTitle();
        String body  = notification.getBody();
        MySharedPreference.getInstance(this).addToNotify(true);
        MySharedPreference.getInstance(this).putNotification(title,body);
        Intent notifyIntent = new Intent(this, ResultActivity.class);
        // Set the Activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //notifyIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //notifyIntent.putExtra("title", title);
        //notifyIntent.putExtra("message", body);
        // Create the PendingIntent
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(
                this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this,CHANNEL_ID_NOTIFY)
                        .setSmallIcon(R.drawable.about_logo)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setAutoCancel(true)
                        .setContentIntent(notifyPendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());

        boolean isForeground = MwmApplication.backgroundTracker(MwmApplication.get().getApplicationContext()).isForeground();
        if(isForeground) {
            startActivity(notifyIntent);
            //return;
        }
        //SplashActivity.start(this,ResultActivity.class,notifyIntent);
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
        String usrNotification = gson.toJson(data);
        MySharedPreference.getInstance(MwmApplication.get().getApplicationContext()).userNotification(usrNotification);

        //Log.d(TAG, "Message from server " + title + body);
        Intent intent = new Intent(this,  MapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
        intent.setAction("intent.mycustom.action");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
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
        boolean isForeground = MwmApplication.backgroundTracker(MwmApplication.get().getApplicationContext()).isForeground();
        if(isForeground) {
            startActivity(intent);
            //return;
        }
    }

    private String getFlagTitle(String flag) {
        String title = "";
    
        int mFlag = Integer.parseInt(flag);
        switch (mFlag) {
            case Constants.Notification.PASSENGER_REQUEST:
                title = getString(R.string.passenger_request);
                break;
            case Constants.Notification.PASSENGER_ACCEPTED:
                title = getString(R.string.passenger_accepted_request);
                break;
            case Constants.Notification.PASSENGER_REFUSED:
                title = getString(R.string.passenger_cancel);
                break;
            case Constants.Notification.DRIVER_INVITE:
                title = getString(R.string.captain_invitation);
                break;
            case Constants.Notification.DRIVER_ACCEPTED:
                MySharedPreference.getInstance(MwmApplication.get().getApplicationContext())
                        .addActiveProcess(Constants.ActiveProcess.PASSENGER_HAVE_ACTIVE_RIDE);
                title = getString(R.string.captain_accepted);
                break;
            case Constants.Notification.DRIVER_REFUSED:
                title = getString(R.string.captain_cancelled);
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
            case Constants.Notification.PASSENGER_REFUSED:
                body = "Passenger Rejected your request";
                break;
            case Constants.Notification.DRIVER_INVITE:
                body = "Driver Invited for trip!";
                break;
            case Constants.Notification.DRIVER_ACCEPTED:
                body = "Driver Accepted you request!";
                break;
            case Constants.Notification.DRIVER_REFUSED:
                body = "Driver Rejected your request!";
                break;

        }
        return body;
    }

}
