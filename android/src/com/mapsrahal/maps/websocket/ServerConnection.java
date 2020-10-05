package com.mapsrahal.maps.websocket;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.gson.Gson;
import com.mapsrahal.maps.Framework;
import com.mapsrahal.maps.MapActivity;
import com.mapsrahal.maps.MwmApplication;
import com.mapsrahal.maps.MyNotificationManager;
import com.mapsrahal.maps.MySharedPreference;
import com.mapsrahal.maps.R;
import com.mapsrahal.maps.SplashActivity;
import com.mapsrahal.maps.UserTripInfo;
import com.mapsrahal.maps.activity.CaptainActivity;
import com.mapsrahal.util.Constants;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.logging.HttpLoggingInterceptor;

import static com.mapsrahal.maps.MwmApplication.CHANNEL_ID;
import static com.mapsrahal.maps.MwmApplication.CHANNEL_ID_CALL_CAPTAIN;
import static com.mapsrahal.maps.MwmApplication.CHANNEL_NAME;
import static com.mapsrahal.maps.MwmApplication.CHANNEL_NAME_CAPTAIN;
import static com.mapsrahal.maps.SplashActivity.EXTRA_ACTIVITY_TO_START;
import static com.mapsrahal.maps.activity.SelectorActivity.PASSENGER_CAPTAIN_SELECTOR;

public class ServerConnection extends Service {

    public static final String ACTION_MSG_RECEIVED = "msgReceived";
    public static final String ACTION_MSG_ACCEPTED = "requestAccepted";
    public static final String ACTION_MSG_SEND = "msgReceived";
    private static final String RECONNECT_IF_BROKEN = "reconnectIfBroken";
    public static final String ACTION_NETWORK_STATE_CHANGED = "networkStateChanged";
    private static final long INTERVAL_FIVE_MINS = 3 * 60 * 1000;
    private static final String TAG = ServerConnection.class.getSimpleName();
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 15000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 15000;
    private final IBinder mBinder = new ServerConnectionBinder();
    private final int userId;
    private final String phone;
    private final Gson gSon = new Gson();
    private PowerManager.WakeLock wakeLock;
    private WebSocket mWebSocket;
    private OkHttpClient mClient;
    private boolean isHaveMessage = false;
    private boolean isMessageReceived = false;
    private String receivedMessage = null;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private boolean isUpdating = false;
    private boolean isAlarmSet = false;
    private Handler mMessageHandler;
    private ServerListener mListener;
    private Ringtone r;
    private static final long START_TIME_IN_MILLIS = 20000;
    private UserTripInfo g;
    private int requestingPassenger = 0;
    private String tripId;
    private boolean mTimerRunning;
    private CountDownTimer mCountDownTimer;
    private long startTime = 0;
    private static final int SEND_BUSY = 2;
    private static final int ACCEPT_REQUEST = 3;
    private static final int TRIP_CANCELLED = 5;
    private static final int REACHED_CUSTOMER = 11;
    private static final int TRIP_STARTED = 12;
    private static final int TRIP_COMPLETED = 13;
    private static final int DISTANCE_NOTIFY = 50;
    //private WebSocketViewModel mViewModel;

    public enum ConnectionStatus {
        DISCONNECTED,
        CONNECTED
    }

    public interface ServerListener {
        void onNewMessage(String myFlag);
        void onStatusChange(ConnectionStatus status);
    }

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean networkIsOn = intent.getBooleanExtra(ACTION_NETWORK_STATE_CHANGED, false);
            //Log.i(TAG," network is on "+ networkIsOn);
            if (networkIsOn) {
                connect();
            } //else {
            //}
        }
    };

    private final BroadcastReceiver alarmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.i(TAG," Alarm received Outside");
            if (mClient.connectionPool().connectionCount() == 0) {
                isHaveMessage = false;
                connect();
                //Log.i(TAG," Alarm received Inside");
            }
        }
    };

    public ServerConnection() {
        //mListener = listener;
        userId = MySharedPreference.getInstance(this).getUserId();
        phone = MySharedPreference.getInstance(this).getPhoneNumber();
    }

    @SuppressLint("InvalidWakeLockTag")
    @Override
    public void onCreate() {
        super.onCreate();
        startOnline();
        //mViewModel = ViewModelProviders.of(getApplicationContext()).get(WebSocketViewModel.class);
        /*new Thread(new Runnable() {
            @Override
            public void run() {*/
                PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakelockTag");
                wakeLock.acquire();
                try {
                    //Uri notificationRaw = Uri.parse("android.resource://" + this.getPackageName() + "/raw/driver_call.mp3");
                    //Log.i(TAG,"Uri "+ notificationRaw);get
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                    //mediaPlayer = MediaPlayer.create(getApplicationContext(), notification);
                    r = RingtoneManager.getRingtone(ServerConnection.this, notification);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //Log.i("MyWakeLockIsWorking","Connected");
                mClient = new OkHttpClient.Builder()
                        .addInterceptor(new HttpLoggingInterceptor())
                        //.cache(new Cache(cacheDir, cacheSize))
                        .connectTimeout(0, TimeUnit.SECONDS)
                        .pingInterval(10000L, TimeUnit.MILLISECONDS)
                        .readTimeout(0, TimeUnit.SECONDS)
                        .retryOnConnectionFailure(true)
                        .build();
                //mClient.dispatcher().executorService().shutdown();
                locationRequest = LocationRequest.create();
                locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
                locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
                builder.addLocationRequest(locationRequest);
                LocationSettingsRequest locationSettingsRequest = builder.build();

                locationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        if (locationResult == null) {
                            return;
                        }
                        for (Location currentLocation : locationResult.getLocations()) {
                            UserLocation userLocation = new UserLocation(
                                    userId,
                                    currentLocation.getLatitude(),
                                    currentLocation.getLongitude(),
                                    1, 1, phone);
                            sendMe("" + gSon.toJson(userLocation));
                            //Log.i(TAG,"Connection Count "+mClient.connectionPool().connectionCount());
                            //Log.i(TAG,"Idle Connection Count "+mClient.connectionPool().idleConnectionCount());
                            //Log.i(TAG,"location coordinates "+ currentLocation.getLatitude() + " - " + currentLocation.getLongitude());
                        }
                    }
                };

                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(MwmApplication.get().getApplicationContext());
                //connect();
                //start();
                setAlarm();
            //}
       // }).start();
    }

    private void startOnline() {
        try {
            Intent notificationIntent = new Intent(this, MapActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    0, notificationIntent, 0);
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_CALL_CAPTAIN)
                    .setContentTitle(CHANNEL_NAME_CAPTAIN)
                    //.setContentText(input)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(1, notification);
        } catch (Exception e) {

        }
    }

    private void notifyTimer(int sec) {

        final int[] timeRemaining = {sec};
        final Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Intent intent1local = new Intent();
                intent1local.setAction("Counter");
                timeRemaining[0]--;
                //NotificationUpdate(timeRemaining[0]);
                startMe();
                if (timeRemaining[0] <= 0) {
                    timer.cancel();
                    closeNotification();
                }
                //intent1local.putExtra("TimeRemaining", timeRemaining[0][0]);
                //sendBroadcast(intent1local);
            }
        }, 0,4000);

        /*Timer t = new Timer();
        //Set the schedule function and rate
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                startMe();
            }
        },0,4000);*/
    }

    private void closeNotification() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(14);
    }

    private void startMe() {
        //try {
            Bundle data = null;
            String name = "",callType = "Request";
            int NOTIFICATION_ID = 120;
            /*if (intent != null && intent.getExtras() != null) {
                data = intent.getExtras();
                name = data.getString("inititator");
                if(MwmApplication.get().getCall_type().equalsIgnoreCase(ApplicationRef.Constants.AUDIO_CALL)){
                    callType ="Audio";
                }
                else {
                    callType ="Video";
                }

            }*/
            try {
                Intent receiveCallAction = new Intent(MwmApplication.get().getApplicationContext(), CaptainActivity.class);
                receiveCallAction.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                receiveCallAction.putExtra("ConstantApp.CALL_RESPONSE_ACTION_KEY", "ConstantApp.CALL_RECEIVE_ACTION");
                receiveCallAction.putExtra("ACTION_TYPE", "RECEIVE_CALL");
                receiveCallAction.putExtra("NOTIFICATION_ID",NOTIFICATION_ID);
                receiveCallAction.setAction("RECEIVE_CALL");

                Intent cancelCallAction = new Intent(MwmApplication.get().getApplicationContext(), CaptainActivity.class);
                cancelCallAction.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                cancelCallAction.putExtra("ConstantApp.CALL_RESPONSE_ACTION_KEY", "ConstantApp.CALL_CANCEL_ACTION");
                cancelCallAction.putExtra("ACTION_TYPE", "CANCEL_CALL");
                cancelCallAction.putExtra("NOTIFICATION_ID",NOTIFICATION_ID);
                cancelCallAction.setAction("CANCEL_CALL");

                Intent callDialogAction = new Intent(MwmApplication.get().getApplicationContext(), CaptainActivity.class);
                callDialogAction.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                callDialogAction.putExtra("ACTION_TYPE", "DIALOG_CALL");
                callDialogAction.putExtra("NOTIFICATION_ID",NOTIFICATION_ID);
                callDialogAction.setAction("DIALOG_CALL");
                /*
                PendingIntent receiveCallPendingIntent = PendingIntent.getBroadcast(MwmApplication.get().getApplicationContext(), 1200, receiveCallAction, PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent cancelCallPendingIntent = PendingIntent.getBroadcast(MwmApplication.get().getApplicationContext(), 1201, cancelCallAction, PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent callDialogPendingIntent = PendingIntent.getBroadcast(MwmApplication.get().getApplicationContext(), 1202, callDialogAction, PendingIntent.FLAG_UPDATE_CURRENT);
                */
                PendingIntent receiveCallPendingIntent = PendingIntent.getActivity(MwmApplication.get().getApplicationContext(), 1200, receiveCallAction, PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent cancelCallPendingIntent = PendingIntent.getActivity(MwmApplication.get().getApplicationContext(), 1201, cancelCallAction, PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent callDialogPendingIntent = PendingIntent.getActivity(MwmApplication.get().getApplicationContext(), 1202, callDialogAction, PendingIntent.FLAG_UPDATE_CURRENT);

                //createChannel();
                NotificationCompat.Builder notificationBuilder = null;
                //if (data != null) {
                    Uri ringUri= Settings.System.DEFAULT_RINGTONE_URI;
                    notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                            .setContentTitle(CHANNEL_NAME)
                            .setContentText("Incoming "+callType)
                            .setSmallIcon(R.drawable.ic_call_green_24dp)
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setCategory(NotificationCompat.CATEGORY_CALL)
                            .addAction(R.drawable.bg_active_icon, getString(R.string.cancel), cancelCallPendingIntent)
                            .addAction(R.drawable.bg_active_icon, getString(R.string.accept), receiveCallPendingIntent)
                            .setContentIntent(callDialogPendingIntent)
                            //.setOngoing(true)
                            .setAutoCancel(true)
                            .setSound(ringUri)
                            //.setTimeoutAfter(20000)
                            .setFullScreenIntent(callDialogPendingIntent, true);

                //}

                Notification incomingCallNotification = null;
                if (notificationBuilder != null) {
                    incomingCallNotification = notificationBuilder.build();
                }

                //if(isNotify) {
                    NotificationManager notificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(14,incomingCallNotification);
                //} else {
                    //startForeground(NOTIFICATION_ID, incomingCallNotification);
                //}

            } catch (Exception e) {
                e.printStackTrace();
            }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        try {
            if (intent.getAction().equals(Constants.STOPFOREGROUND_ACTION)) {
                //Log.i(TAG, "Received Stop Foreground Intent");
                //your end servce code
                disconnect();
                //cancelAlarm();
                stopForeground(true);
                stopSelf();
            }
            startOnline();
            connect();
            start();
        } catch (Exception e) {

        }
        //Log.i(TAG,"Service thread Id Connect : "+ Thread.currentThread().getId());
        //setAlarm();
        //Log.i(TAG,"Service thread Id "+ Thread.currentThread().getId());
        return START_REDELIVER_INTENT;
    }

    @SuppressLint("MissingPermission")
    private void start() {
        if (!isUpdating) {
            //Log.i(TAG, "Started location tracking");
            if(MySharedPreference.getInstance(this).isCaptainOnline()) {
                mFusedLocationClient.requestLocationUpdates(locationRequest,
                        locationCallback, Looper.myLooper());
                isUpdating = true;
            }
        }
    }

    private void stop() {
        if (mFusedLocationClient != null) {
            //Log.i(TAG, "Stopped location tracking");
            mFusedLocationClient.removeLocationUpdates(locationCallback);
            isUpdating = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopForeground(true);
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //Log.i(TAG, "onBind");
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(ServerConnection.ACTION_NETWORK_STATE_CHANGED));
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //Log.i(TAG, "onUnbind");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        return false;
    }

    public void registerListener(ServerListener listener) {
        mListener = listener;
    }

    private void setAlarm() {
        AlarmManager alarmMgr = (AlarmManager) (this.getSystemService(Context.ALARM_SERVICE));
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, new Intent(RECONNECT_IF_BROKEN), 0);
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + INTERVAL_FIVE_MINS,
                INTERVAL_FIVE_MINS, alarmIntent);
        /*alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 10000,
                10000, alarmIntent);*/
        this.registerReceiver(alarmReceiver, new IntentFilter(RECONNECT_IF_BROKEN));
        isAlarmSet = true;
        //Log.i(TAG,"Alarm set");
    }

    private void cancelAlarm() {
        if(isAlarmSet) {
            PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, new Intent(RECONNECT_IF_BROKEN), 0);
            AlarmManager alarmMgr = (AlarmManager) (this.getSystemService(Context.ALARM_SERVICE));
            alarmMgr.cancel(alarmIntent);
            this.unregisterReceiver(alarmReceiver);
            isAlarmSet = false;
        }
        //Log.i(TAG,"Alarm Cancelled");
    }

    private void sendMessageReceivedBroadcast(String myMsg) {
        Intent intent = new Intent(ACTION_MSG_RECEIVED);
        intent.putExtra("MyDriverMessage",myMsg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendMessageReceivedEvent(String myMsg) {

        isMessageReceived = true;
        receivedMessage = myMsg;
        //receivedMessage = myMsg;
        Intent intent = new Intent(this, CaptainActivity.class);
        //Intent intent = new Intent(this, SplashActivity.class);
        //intent.putExtra(EXTRA_ACTIVITY_TO_START, MapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction("intent.mycustom.action");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        g = gSon.fromJson(myMsg, UserTripInfo.class);
        int flag = g.getMyFlag();
        MySharedPreference.getInstance(this).userMessage(myMsg);
        //mBinder.pingBinder()
        if(flag == 4) {

            //startActivity(intent);
            boolean isForeground = MwmApplication.backgroundTracker(MwmApplication.get().getApplicationContext()).isForeground();
            if(isForeground) {
                startActivity(intent);
                //return;
            } else {
                /*final Intent notificationIntent = new Intent(this, MapActivity.class);
                notificationIntent.setAction(Intent.ACTION_MAIN);
                notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);*/
                /*PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                        PendingIntent.FLAG_ONE_SHOT);

                NotificationCompat.Builder notificationBuilder =
                        new NotificationCompat.Builder(this,CHANNEL_ID)
                                .setSmallIcon(R.drawable.about_logo)
                                .setContentTitle("Passenger Request")
                                .setContentText("Passenger Requesting for Ride")
                                .setAutoCancel(true)
                                .setContentIntent(pendingIntent);
                Uri alarmSound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.cancel_alarm);
                //Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                notificationBuilder.setSound(alarmSound);
                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                notificationManager.notify(0, notificationBuilder.build());*/
                startMe();
                //notifyTimer(7);
                //playRingtone();
                //Intent intent1 = new Intent(this, MapActivity.class);
                //intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //startActivity(notificationIntent);
                //sendMessageReceivedBroadcast(myMsg);
            }
        } else {
            sendMessageReceivedBroadcast(myMsg);
        }

    }

    private void playRingtone() {
        try {
            if(r != null) {
                r.play();
            }
        } catch (Exception e) {

        }

    }

    //@SuppressLint("HandlerLeak")
    /*private void connect() {
        if (!isHaveMessage) {
            Request request = new Request.Builder()
                    .url(Framework.nativeGetWsUrlLocal() + "?token=" + userId +"&is="+MySharedPreference.getInstance(this).isCaptainOnline())
                    .build();
            mWebSocket = mClient.newWebSocket(request, new SocketListener());
            isHaveMessage = true;
        }
        start();
    }*/

    private void connect() {
        if (!isHaveMessage) {
            Request request = new Request.Builder()
                    .url(Framework.nativeGetWsUrl() + "?token=" + userId +"&is="+MySharedPreference.getInstance(this).isCaptainOnline())
                    .build();
            mWebSocket = mClient.newWebSocket(request, new SocketListener());
            isHaveMessage = true;
        }
        start();
    }

    public void sendMe(String message) {
        try {
            //Log.d(TAG,message);
            mWebSocket.send(message);
        } catch (Exception e) {
            //connect();
        }
    }

    private void disconnect() {
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
        if (mWebSocket != null) {
            mWebSocket.cancel();
            mWebSocket = null;
        }
        //mListener = null;
        stop();
        cancelAlarm();
    }

    public void sendReq(UserTripInfo userTripInfo) {
        sendMe("" + gSon.toJson(userTripInfo));
    }

    public void sendMessage(int flag, int driverId, double distance, double duration, double price) {
        String mPrice = (price == 0) ? phone : String.valueOf(price);
        UserLocation userLocation = new UserLocation(
                userId,
                distance,
                duration,
                flag, driverId, mPrice);
        sendMe("" + gSon.toJson(userLocation));
    }

    public final class ServerConnectionBinder extends Binder {
        public ServerConnection getService() {
            return ServerConnection.this;
        }
    }

    private class SocketListener extends WebSocketListener {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            Log.i(TAG, "Websocket onOpen()");
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            //Log.i(TAG, "Message (String) received: " + text);
            sendMessageReceivedEvent(text);
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            Log.i(TAG, "Code: " + code + " - Reason: " + reason);
            //sendMessageReceivedEvent();
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            //Log.i(TAG, ""+ response.message());
            Log.i(TAG, "Websocket onFailure()");
        }
    }
}
