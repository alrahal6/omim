package com.mapsrahal.maps.websocket;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.gson.Gson;
import com.mapsrahal.maps.MapActivity;
import com.mapsrahal.maps.MwmApplication;
import com.mapsrahal.maps.MySharedPreference;
import com.mapsrahal.maps.R;
import com.mapsrahal.maps.SplashActivity;
import com.mapsrahal.maps.UserTripInfo;
import com.mapsrahal.util.Constants;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.logging.HttpLoggingInterceptor;

import static com.mapsrahal.maps.MwmApplication.CHANNEL_ID;
import static com.mapsrahal.maps.MwmApplication.CHANNEL_NAME;
import static com.mapsrahal.maps.SplashActivity.EXTRA_ACTIVITY_TO_START;
import static com.mapsrahal.maps.activity.SelectorActivity.PASSENGER_CAPTAIN_SELECTOR;

public class ServerConnection extends Service {

    //public static final String ACTION_MSG_RECEIVED = "msgReceived";
    //public static final String ACTION_MSG_ACCEPTED = "requestAccepted";
    //public static final String ACTION_MSG_SEND = "msgReceived";
    private static final String RECONNECT_IF_BROKEN = "reconnectIfBroken";
    public static final String ACTION_NETWORK_STATE_CHANGED = "networkStateChanged";
    private static final long INTERVAL_FIVE_MINS = 60 * 1000;
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
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private boolean isUpdating = false;
    private boolean isAlarmSet = false;
    //private Handler mMessageHandler;
    //private ServerListener mListener;

    public enum ConnectionStatus {
        DISCONNECTED,
        CONNECTED
    }

    public interface ServerListener {
        void onNewMessage(String message);
        void onStatusChange(ConnectionStatus status);
    }

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean networkIsOn = intent.getBooleanExtra(ACTION_NETWORK_STATE_CHANGED, false);
            if (networkIsOn) {
                connect();
            } //else {
            //disconnect();
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
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakelockTag");
        wakeLock.acquire();

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
                    Log.i(TAG,"Connection Count "+mClient.connectionPool().connectionCount());
                    //Log.i(TAG,"Idle Connection Count "+mClient.connectionPool().idleConnectionCount());
                    //Log.i(TAG,"location coordinates "+ currentLocation.getLatitude() + " - " + currentLocation.getLongitude());
                }
            }
        };

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(MwmApplication.get().getApplicationContext());
        start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent.getAction().equals( Constants.STOPFOREGROUND_ACTION)) {
            Log.i(TAG, "Received Stop Foreground Intent");
            //your end servce code
            stopForeground(true);
            stopSelf();
        }
        Intent notificationIntent = new Intent(this, MapActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(CHANNEL_NAME)
                //.setContentText(input)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
        Log.i(TAG,"Service thread Id Connect : "+ Thread.currentThread().getId());
        connect();
        start();
        //setAlarm();
        //Log.i(TAG,"Service thread Id "+ Thread.currentThread().getId());
        return START_REDELIVER_INTENT;
    }

    @SuppressLint("MissingPermission")
    private void start() {
        if (!isUpdating) {
            //Log.i(TAG, "Started location tracking");
            mFusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback, Looper.myLooper());
            isUpdating = true;
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
        disconnect();
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
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

    /*public void registerListener(ServerListener listener) {
        mListener = listener;
    }*/

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

    private void sendMessageReceivedEvent(String myMsg) {
        MySharedPreference.getInstance(MwmApplication.get().getApplicationContext()).userMessage(myMsg);
        Intent driverForeground = new Intent(this, MapActivity.class);
        //driverForeground.setAction(Intent.ACTION_MAIN);
        //driverForeground.addCategory(Intent.CATEGORY_LAUNCHER);
        //Intent driverForeground = new Intent(this, SplashActivity.class);
        driverForeground.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //driverForeground.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        driverForeground.putExtra(PASSENGER_CAPTAIN_SELECTOR,4);
        //driverForeground.putExtra(EXTRA_ACTIVITY_TO_START, MapActivity.class);
        //driverForeground.setAction(Intent.ACTION_MAIN);
        //driverForeground.addCategory(Intent.CATEGORY_LAUNCHER);
        //driverForeground.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //driverForeground.addCategory(Intent.CATEGORY_LAUNCHER);
        driverForeground.setAction("intent.mycustom.action");
        driverForeground.addCategory(Intent.CATEGORY_DEFAULT);
        startActivity(driverForeground);
        //String CUSTOM_ACTION = "intent.mycustom.action";
        //Intent it = new Intent();
        //it.setAction(CUSTOM_ACTION);
        //it.setComponent(new ComponentName(context.getPackageName(), MyMainActivity.class.getName()));
        //it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //it.addCategory(Intent.CATEGORY_DEFAULT);
        //startActivity(it);


        /*Intent intent = new Intent(this, SplashActivity.class);
        intent.putExtra(EXTRA_ACTIVITY_TO_START, MapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK ); // You need this if starting
        intent.putExtra(PASSENGER_CAPTAIN_SELECTOR,4);   //  the activity from a service
        //intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //intent.setAction(Intent.ACTION_MAIN);
        intent.setAction("intent.mycustom.action");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        startActivity(intent);*/



        //mListener.onNewMessage(myMsg);
        //Message msg = mMessageHandler.obtainMessage(0, myMsg);
        //mHandler.sendMessage(msg);

        //Intent intent = new Intent(ACTION_MSG_RECEIVED);
        //intent.putExtra("MyDriverMessage", myMsg);
        //LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        //MyNotificationManager.getInstance(this).displayNotification("Amgad app", "Client Request");
    }

    //@SuppressLint("HandlerLeak")
    private void connect() {
        if (!isHaveMessage) {
            Request request = new Request.Builder()
                    .url(Constants.Url.SERVER_WS_URL + "?token=" + userId)
                    .build();
            mWebSocket = mClient.newWebSocket(request, new SocketListener());
            //Log.i(TAG," First connection");
            // todo check later
            mClient.dispatcher().executorService().shutdown();
            isHaveMessage = true;
        }
        start();
    }

    /*@SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //mMessageHandler.sendMessage(msg);
            mListener.onNewMessage((String) msg.obj);
        }
    };*/

    private void sendMe(String message) {
        try {
            mWebSocket.send(message);
        } catch (Exception e) {
            //connect();
        }
    }

    private void disconnect() {
        if (wakeLock.isHeld()) {
            wakeLock.release();
            //Log.i("MyWakeLock","Released");
        }
        if (mWebSocket != null) {
            mWebSocket.cancel();
            mWebSocket = null;
        }
        //mListener = null;
        stop();
        //cancelAlarm();
    }

    public void sendReq(UserTripInfo userTripInfo) {
        /*String mPrice = (price == 0) ? phone : String.valueOf(price);
        //Log.i(TAG,"price : "+mPrice);
        UserLocation userLocation = new UserLocation(
                userId,
                distance,
                duration,
                flag, driverId, mPrice);*/
        //Log.i(TAG,"" + gSon.toJson(userLocation));
        sendMe("" + gSon.toJson(userTripInfo));
        // mWebSocket.send(""+ gSon.toJson(userLocation));
    }

    public void sendMessage(int flag, int driverId, double distance, double duration, double price) {
        String mPrice = (price == 0) ? phone : String.valueOf(price);
        //Log.i(TAG,"price : "+mPrice);
        UserLocation userLocation = new UserLocation(
                userId,
                distance,
                duration,
                flag, driverId, mPrice);
        //Log.i(TAG,"" + gSon.toJson(userLocation));
        sendMe("" + gSon.toJson(userLocation));
        // mWebSocket.send(""+ gSon.toJson(userLocation));
    }

    /*public void sendPriceMessage(int flag, int driverId,double distance,double duration,double price) {
        UserLocation userLocation = new UserLocation(
                userId,
                distance,
                duration,
                flag, driverId, String.valueOf(price));
        sendMe("" + gSon.toJson(userLocation));
        // mWebSocket.send(""+ gSon.toJson(userLocation));
    }*/

    /*public interface ServerListener {
        void onNewMessage(String message);
    }*/

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
            Log.i(TAG, "Message (String) received: " + text);
            sendMessageReceivedEvent(text);
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            Log.i(TAG, "Code: " + code + " - Reason: " + reason);
            disconnect();
            //sendMessageReceivedEvent();
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            //Log.i(TAG, ""+ response.message());
            Log.i(TAG, "Websocket onFailure()");
            disconnect();
        }
    }
}
