package com.mapsrahal.maps.websocket;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

import com.mapsrahal.maps.MwmApplication;
import com.mapsrahal.maps.activity.CaptainActivity;

public class CallNotificationActionReceiver extends BroadcastReceiver {

    Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.mContext=context;
        if (intent != null && intent.getExtras() != null) {

            String action ="";
            action=intent.getStringExtra("ACTION_TYPE");

            if (action != null&& !action.equalsIgnoreCase("")) {
                performClickAction(context, action);
            }

            // Close the notification after the click action is performed.
            Intent iclose = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcast(iclose);
            //context.stopService(new Intent(context, ServerConnection.class));

        }
    }

    private void performClickAction(Context context, String action) {
        if(action.equalsIgnoreCase("RECEIVE_CALL")) {

            if (checkAppPermissions()) {
                Intent intentCallReceive = new Intent(mContext, CaptainActivity.class);
                intentCallReceive.putExtra("Call", "incoming");
                intentCallReceive.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(intentCallReceive);
            } else {
                Intent intent = new Intent(MwmApplication.get().getApplicationContext(), CaptainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("CallFrom","call from push");
                mContext.startActivity(intent);

            }
        } else if(action.equalsIgnoreCase("DIALOG_CALL")) {
            // show ringing activity when phone is locked
            Intent intent = new Intent(MwmApplication.get().getApplicationContext(), CaptainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mContext.startActivity(intent);
        } else {
            //context.stopService(new Intent(context, ServerConnection.class));
            Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcast(it);
        }
    }

    private Boolean checkAppPermissions() {
        return true;
        //return hasReadPermissions() && hasWritePermissions() && hasCameraPermissions() && hasAudioPermissions();
    }

    /*private boolean hasAudioPermissions() {
        return (ContextCompat.checkSelfPermission(MwmApplication.get().getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);
    }

    private boolean hasReadPermissions() {
        return (ContextCompat.checkSelfPermission(MwmApplication.get().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    private boolean hasWritePermissions() {
        return (ContextCompat.checkSelfPermission(MwmApplication.get().getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }
    private boolean hasCameraPermissions() {
        return (ContextCompat.checkSelfPermission(MwmApplication.get().getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
    }*/

    /*class AutoDismissNotification : BroadcastReceiver() {

    companion object {
        private const val KEY_EXTRA_NOTIFICATION_ID = "notification_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(intent.getIntExtra(KEY_EXTRA_NOTIFICATION_ID, 0))
    }

    fun setAlarm(context: Context, notificationId: Int, time: Long) {
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, AutoDismissNotification::class.java)
        alarmIntent.putExtra(KEY_EXTRA_NOTIFICATION_ID, notificationId)
        val alarmPendingIntent = PendingIntent.getBroadcast(context, notificationId, alarmIntent, PendingIntent.FLAG_ONE_SHOT)
        alarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + time, alarmPendingIntent)
    }

    fun cancelAlarm(context: Context, notificationId: Int) {
        val alarmIntent = Intent(context, AutoDismissNotification::class.java)
        alarmIntent.putExtra(KEY_EXTRA_NOTIFICATION_ID, notificationId)
        val alarmPendingIntent = PendingIntent.getBroadcast(context, notificationId, alarmIntent, PendingIntent.FLAG_ONE_SHOT)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(alarmPendingIntent)
    }
}

long timeOut = 5 * 1000L; // Five seconds
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    builder.setTimeoutAfter(timeOut);
}
else {
    AutoDismissNotification().setAlarm(this, notificationId, timeOut);
}

*/
}
