package com.mapsrahal.maps;

import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.core.app.NotificationCompat;

import static android.content.Context.NOTIFICATION_SERVICE;

public class MyNotificationManager {
    private static MyNotificationManager mInstance;
    private final Context mCtx;
    private static final String CHANNEL_ID_CALL_CAPTAIN = "call_captatin" ;

    private MyNotificationManager(Context context) {
        mCtx = context.getApplicationContext();
    }

    public static synchronized MyNotificationManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MyNotificationManager(context);
        }
        return mInstance;
    }

    public void displayNotification(String title, String body) {
        //Uri notifySound = Uri.parse("android.resource://"+mCtx.getPackageName()+"/" +R.raw.notification_sound);
        Uri notifySound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mCtx, CHANNEL_ID_CALL_CAPTAIN)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setSound(notifySound)
                        .setContentText(body);
        /*Intent resultIntent = new Intent(mCtx, DriverMapsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mCtx, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);*/
        NotificationManager mNotifyMgr =
                (NotificationManager) mCtx.getSystemService(NOTIFICATION_SERVICE);
        if (mNotifyMgr != null) {
            mNotifyMgr.notify(1, mBuilder.build());
        }
    }
}
