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
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

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
import com.mapsrahal.maps.MyNotificationManager;
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

    public void stopRingTone() {
        if(r.isPlaying()) {
            r.stop();
        }
    }

    private void reachedCustomer() {
        //send(REACHED_CUSTOMER, 0, 0, 0);
        //isOnWaytoCustomer = false;
        //swipeButtonSettings.setActionConfirmText(getString(R.string.start_trip));
        //mSwipeButton.setSwipeButtonCustomItems(swipeButtonSettings);
        //mSwipeButton.setText(R.string.start_trip);
    }

    private void acceptRequest(UserTripInfo g) {
        /*try {
            //r.stop();
            mService.stopRingTone();
            send(ACCEPT_REQUEST, 0, 0, 0);
            mAcceptBusyInfo.setVisibility(View.GONE);
            mSwipeLayout.setVisibility(View.VISIBLE);
            mOpenGMap.setVisibility(View.VISIBLE);
            //mCustomerName.setText("");
            //mCustomerPhone.setText("");
            base = g.getBase();
            km = g.getKm();
            mins = g.getMins();
            minDis = g.getMinDis();
            //tripId = String.valueOf(g.getTripId());
            distance = g.getDistance();
            duration = g.getDuration();
            price = g.getPrice();
            if (mTimerRunning) {
                stopTimer();
            }
            // todo register accepted driver with trip id
            // tripId
            prepareGoToCustomer();
        } catch (Exception e) {
            Log.d(TAG, "Error accept request " + e.getMessage());
        }*/
    }

    private void respondBusy() {
        try {
            r.stop();
            //mService.stopRingTone();
            sendMessage(SEND_BUSY, requestingPassenger,0, 0, 0);
            //mCustomerInfo.setVisibility(View.GONE);
            //mCustomerName.setText("");
            //mCustomerPhone.setText("");
            if (mTimerRunning) {
                stopTimer();
            }
        } catch (Exception e) {
            Log.d(TAG, "Error respond busy " + e.getMessage());
        }
    }

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

    public boolean isMessageReceived() {
        return isMessageReceived;
    }

    public void setIsReceivedFalse() {
        isMessageReceived = false;
    }


    public String receivedMessage() {
        return receivedMessage;
    }

    @SuppressLint("InvalidWakeLockTag")
    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakelockTag");
        wakeLock.acquire();
        try {
            //Uri notificationRaw = Uri.parse("android.resource://" + this.getPackageName() + "/raw/driver_call.mp3");
            //Log.i(TAG,"Uri "+ notificationRaw);
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            //mediaPlayer = MediaPlayer.create(getApplicationContext(), notification);
            r = RingtoneManager.getRingtone(this, notification);
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
            disconnect();
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

    private void sendMessageReceivedEvent(String myMsg) {
        //MySharedPreference.getInstance(MwmApplication.get().getApplicationContext()).userMessage(myMsg);
        isMessageReceived = true;
        receivedMessage = myMsg;
        processMessage(myMsg);
        Intent intent = new Intent(this, SplashActivity.class);
        intent.putExtra(EXTRA_ACTIVITY_TO_START, MapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
        //intent.putExtra(PASSENGER_CAPTAIN_SELECTOR,4);
        //intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //intent.setAction(Intent.ACTION_MAIN);
        //intent.setAction("intent.mycustom.action");
        //intent.addCategory(Intent.CATEGORY_DEFAULT);
        startActivity(intent);
        //Message m = mMessageHandler.obtainMessage(0, myMsg);
        //mMessageHandler.sendMessage(m);
        //mListener.onNewMessage(myMsg);
        //Message msg = mMessageHandler.obtainMessage(0, myMsg);
        //mHandler.sendMessage(msg);
        //Intent intent = new Intent(ACTION_MSG_RECEIVED);
        //intent.putExtra("MyDriverMessage", myMsg);
        //LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        //MyNotificationManager.getInstance(this).displayNotification("Amgad app", "Client Request");
    }

    private void processMessage(String myMsg) {
        g = gSon.fromJson(myMsg, UserTripInfo.class);
        int flag = g.getMyFlag();
        requestingPassenger = g.getUserId();
        tripId = String.valueOf(g.getTripId());
        //Log.i(TAG,"request received");
        switch (flag) {
            case 4:
                r.play();
                //mediaPlayer.start();
                /*mCustomerInfo.setVisibility(View.VISIBLE);
                mAcceptBusyInfo.setVisibility(View.VISIBLE);
                mSwipeButton.setText("Reached Customer");
                mSwipeLayout.setVisibility(View.GONE);
                mCustomerName.setText(getString(R.string.name) + g.getCustomerName());
                mCustomerPickup.setText(getString(R.string.pickup) + g.getPickupAddress());
                mCustomerDestination.setText(getString(R.string.destination) + g.getDestAddress());
                phoneNumber = "0" + g.getPhone();
                mCustomerPhone.setText(getString(R.string.customer_phone) + g.getPhone());
                mTripDistance.setText(getString(R.string.price) + g.getPrice() + getString(R.string.distance) + g.getDistance());
                */
                if (!mTimerRunning) {
                    startTimer();
                }
                break;
            case 5:
                //r.play();
                /*mCustomerInfo.setVisibility(View.VISIBLE);
                mSwipeLayout.setVisibility(View.GONE);
                mCustomerName.setText(R.string.passenger_cancel);
                mCustomerPickup.setText("");
                mCustomerDestination.setText("");
                //mCustomerPhone.setText("");
                mTripDistance.setText("");
                mAcceptBusyInfo.setVisibility(View.GONE);
                updateResponse(TRIP_CANCELLED);*/
                MyNotificationManager.getInstance(this).displayNotification("Request Cancelled", "Sorry! request cancelled by passenger");
                if (r.isPlaying()) {
                    r.stop();
                }
                if (mTimerRunning) {
                    stopTimer();
                }
                cancelCall();
                break;
            /*case 13:
                mCustomerInfo.setVisibility(View.GONE);
                updateResponse(TRIP_COMPLETED);
                // todo display payment details
                break;*/
            case 3:
                /*removeRequest();
                // mCancelRequest.setVisibility(View.GONE);
                mDriverInfo.setVisibility(View.VISIBLE);
                //mDriverName.setText("Driver Phone: "+g.getPhone());
                //Log.i(TAG,g.getUserId() + " D - " +g.getDriverId());
                g.setMyFlag(9);
                try {
                    mService.sendMessage(g);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mDriverPhone.setVisibility(View.VISIBLE);
                mDriverPhone.setText("Driver Phone: " + g.getPhone());
                MyNotificationManager.getInstance(MapActivity.this).displayNotification("Driver Found", "Driver Coming to you");
                //mRequest.setText("Driver Found, Coming to you");
                phoneNumber = "0" + g.getPhone();
                isDriverAccepted = true;
                isRequestInProgress = false;
                erasePolylines();*/
                //if(mMap != null) {
                //mMap.clear();
                //}
                break;
            case 2:
                //isDriverBusy = true;
                //isRequestInProgress = false;
                //removeRequest();
                //requestHandler.removeCallbacks(requestRunnable);
                //requestHandler.postDelayed(requestRunnable, 0);
                //if (requestCounter < 9) {
                //getClosestDriver();
                //} else {
                //mRequest.setText("Sorry! Driver Not Found");
                //}
                break;
            case 11:
                MyNotificationManager.getInstance(this).displayNotification("Driver Reached", "Driver Reached your place");
                break;
            case 9:
                //Log.i(TAG," receiving driver current location"+ g);
                //LatLng newLocation;
                //double oldLat = oldLocation.latitude;
                //double oldLng = oldLocation.longitude;
                //double newLat = g.getLat();
                //double newLng = g.getLng();
                //LatLng newLocation = new LatLng(g.getLat(), g.getLng());
                //if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                //float rotation = (float) SphericalUtil.computeHeading(oldLocation, newLocation);
                //rotateMarker(mDriverMarker, newLocation, rotation);
                //oldLocation = newLocation;
                //} else {
                //updateDriverLocMarker(newLocation);
                //}
                // break;
                break;
            case 6:
                MyNotificationManager.getInstance(this).displayNotification("Trip Canceled", "Trip Cancelled by driver");
                //mDriverPhone.setVisibility(View.VISIBLE);
                //mCustomerInfo.setVisibility(View.VISIBLE);
                //mCustomerName.setText("");
                //mCustomerPickup.setText("");
                //mCustomerDestination.setText("");
                //mCustomerPhone.setText("Sorry! Passenger Canceled the carPriceArray");
                //mTripDistance.setText("");
                break;
            case 12:
                //mCancelRequest.setVisibility(View.GONE);
                MyNotificationManager.getInstance(this).displayNotification("Trip Started", "Trip Started by driver");
                startTime = System.currentTimeMillis();
                //timerHandler.postDelayed(timerRunnable, 0);
                //mCustomerInfo.setVisibility(View.VISIBLE);
                //mCustomerName.setText("Trip Started");
                break;
            case 13:
                //onEndtrip(Double.valueOf(g.getPhone()));
                //timerHandler.removeCallbacks(timerRunnable);
                //mDriverInfo.setVisibility(View.GONE);
                //mpayAndRating.setVisibility(View.VISIBLE);
                //mAmount.setText("Pay Driver : " + g.getPhone() + " SDG");
                MyNotificationManager.getInstance(this).displayNotification("Trip Completed", "Trip Completed");
                // mCustomerInfo.setVisibility(View.GONE);
                // todo display payment details
                break;
            case 99:
                //userTripInfo.setDriverId(driverId);
                break;
        }
    }

    private void cancelCall() {
        final MediaPlayer[] player = {null};
        if (player[0] == null) {
            player[0] = MediaPlayer.create(this, R.raw.cancel_alarm);
            player[0].setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (player[0] != null) {
                        player[0].release();
                        player[0] = null;
                    }
                }
            });
        }
        player[0].start();
    }

    private void startTimer() {
        mCountDownTimer = new CountDownTimer(START_TIME_IN_MILLIS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                //Log.i(TAG,"Timer Started...");
            }

            @Override
            public void onFinish() {
                mTimerRunning = false;
                respondBusy();
                //Log.i(TAG,"Finished timer");
            }
        }.start();
        mTimerRunning = true;
    }

    private void stopTimer() {
        mCountDownTimer.cancel();
        mTimerRunning = false;
        //Log.i(TAG,"Timer Stopped");
    }

    //@SuppressLint("HandlerLeak")
    private void connect() {
        if (!isHaveMessage) {
            Request request = new Request.Builder()
                    .url(Constants.Url.SERVER_WS_URL + "?token=" + userId)
                    .build();
            mWebSocket = mClient.newWebSocket(request, new SocketListener());
            mMessageHandler = new Handler(msg -> {
                mListener.onNewMessage((String) msg.obj);
                return true;
            });
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
        mListener = null;
        mMessageHandler.removeCallbacksAndMessages(null);
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
