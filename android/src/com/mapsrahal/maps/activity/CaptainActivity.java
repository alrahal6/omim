package com.mapsrahal.maps.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mapsrahal.maps.MapActivity;
import com.mapsrahal.maps.MapFragment;
import com.mapsrahal.maps.MwmApplication;
import com.mapsrahal.maps.MySharedPreference;
import com.mapsrahal.maps.R;
import com.mapsrahal.maps.UserTripInfo;
import com.mapsrahal.maps.websocket.ServerConnection;
import com.mapsrahal.maps.websocket.WebSocketViewModel;
import com.mapsrahal.util.UiUtils;

import java.util.Timer;
import java.util.TimerTask;

import static com.mapsrahal.maps.MapActivity.ACCEPT_REQUEST;
import static com.mapsrahal.maps.MapActivity.SEND_BUSY;
import static com.mapsrahal.maps.activity.SelectorActivity.PASSENGER_CAPTAIN_SELECTOR;

public class CaptainActivity extends AppCompatActivity
        implements ServerConnection.ServerListener, View.OnClickListener {

    private Ringtone ringtone;
    private UserTripInfo g;
    private final Gson gSon = new Gson();
    private WebSocketViewModel mViewModel;
    private ServerConnection mService;
    private int requestingPassenger = 0;
    private ProgressBar mCaptProgress;
    private TextView mProgressText;
    private int requestResponse = 3;
    private Button mCaptainAccept,mCaptainBusy;
    private boolean isBounded = false;
    private boolean isSendAccepted = false;
    private boolean isSendBusy = false;
    private static final String TAG = CaptainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UiUtils.setupColorStatusBar(this, R.color.bg_captain_call);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager myKM = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
            //if( myKM.inKeyguardRestrictedInputMode()) {
                myKM.requestDismissKeyguard(this,null);
                //it is locked
            //} else {
                //it is not locked
            //}
            //keyguardManager KeyguardManager = getSystemService(Context.KEYGUARD_SERVICE);
            //KeyguardManager.requestDismissKeyguard(this, null);
        } else {
            /*this.window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)*/
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }
        //String name = null;
        setContentView(R.layout.activity_captain);
        mCaptainAccept = findViewById(R.id.captainAccept);
        mCaptainBusy = findViewById(R.id.captainBusy);
        mCaptainAccept.setOnClickListener(this);
        mCaptainBusy.setOnClickListener(this);
        mCaptProgress = findViewById(R.id.capt_progress_bar);
        mProgressText = findViewById(R.id.text_view_progress);
        //mCaptProgress.setTe
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(14);
        initRingTone();
        mViewModel = ViewModelProviders.of(this).get(WebSocketViewModel.class);
        setObservers();
        processMessage("");
        Intent intent = getIntent();
        final Intent bIntent = getMyIntent();
        bindMyService(bIntent);
        if (intent != null && intent.getAction() != null) {
            //String name = intent.getStringExtra("ACTION_TYPE");
            String name = intent.getAction();
            //Log.d(TAG,"name : "+name);
            if(name != null) {
                if (name.equals("RECEIVE_CALL")) {
                    //sendAccepted();
                    //Log.d(TAG,"Send Accepted");
                    // trip accepted
                    //callType ="Audio";
                } else if(name.equals("CANCEL_CALL")) {
                    //sendBusy();
                    // wanna see the details
                    //Log.d(TAG,"Just show");
                    //callType ="Video";
                } else {
                    //Log.d(TAG,"Send Busy");

                    // trip cancelled
                }
            }
        }
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        String msg = MySharedPreference.getInstance(MwmApplication.get().getApplicationContext()).getUserMessage();
        // todo remove later
        //MySharedPreference.getInstance(this).clearActiveProcess();
        //Log.i(TAG,"Shared message : "+msg);
        if (msg != null) {
            //isOnRequestBtn = true;
            processMessage(msg);
            //if(activeProcess != Constants.ActiveProcess.PASSENGER_HAVE_ACTIVE_RIDE) {
            MySharedPreference.getInstance(MwmApplication.get().getApplicationContext()).userMessage(null);
            //}
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
                updateProgress(((timeRemaining[0] * 100) / sec),timeRemaining[0]);
                if (timeRemaining[0] <= 0) {
                    timer.cancel();
                    sendBusy();
                }
            }
        }, 0,1000);
    }

    private void updateProgress(int pro,int progress) {
        runOnUiThread(() -> {
            String remPer = progress+" Sec";
            mCaptProgress.setProgress(pro);
            mProgressText.setText(remPer);
        });
    }

    private void sendAccepted() {
        if(!isSendAccepted) {
            isSendAccepted = true;
            if (ringtone.isPlaying()) {
                ringtone.stop();
            }
            // todo send accepted and finish
            send(3, 10, 1.1, 1.0);
            Intent mapIntent = new Intent(this, MapActivity.class);
            MySharedPreference.getInstance(CaptainActivity.this).setCaptRespId(ACCEPT_REQUEST);
            mapIntent.putExtra(PASSENGER_CAPTAIN_SELECTOR, ACCEPT_REQUEST);
            startActivity(mapIntent);
            if(isBounded) {
                unBindMyService();
            }
            finish();
        }
    }

    private void sendBusy() {
        if(!isSendBusy) {
            isSendBusy = true;
            if (ringtone.isPlaying()) {
                ringtone.stop();
            }
            // todo send busy and finish
            //Intent mapIntent = new Intent(this,MapActivity.class);
            //MySharedPreference.getInstance(CaptainActivity.this).setCaptRespId(SEND_BUSY);
            //mapIntent.putExtra(PASSENGER_CAPTAIN_SELECTOR,SEND_BUSY);
            //startActivity(mapIntent);
            send(2, 10, 1.1, 1.0);
            if(isBounded) {
                unBindMyService();
            }
            finish();
        }
    }

    private Intent getMyIntent() {
        //Context context = getContext();
        return new Intent(this, ServerConnection.class);
    }

    private void bindMyService(Intent intent) {
        MySharedPreference.getInstance(this).setBind(true);
        bindService(intent, mViewModel.getServiceConnection(), Context.BIND_AUTO_CREATE);
    }

    private void unBindMyService() {
        if(MySharedPreference.getInstance(this).isBinded()) {
            MySharedPreference.getInstance(this).setBind(false);
            unbindService(mViewModel.getServiceConnection());
            //LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        }
    }

    private void setObservers() {
        mViewModel.getBinder().observe(this, myBinder -> {
            if (myBinder == null) {
                Log.d(TAG, "onChanged: unbound from service");
                isBounded = false;
            } else {
                isBounded = true;
                //Log.d(TAG, "onChanged: bound to service.");
                mService = myBinder.getService();
                mService.registerListener(this);
            }
        });
    }

    private void initRingTone() {
        try {
            //Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            Uri ringUri= Uri.parse("android.resource://com.mapsrahal.maps/raw/calling");
            ringtone = RingtoneManager.getRingtone(this, ringUri);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void send(int flag, double distance, double duration, double price) {
        try {
            mService.sendMessage(flag, requestingPassenger, distance, duration, price);
            updateResponse(flag);
        } catch (Exception e) {
            Log.d(TAG, "Error sending message " + e.getMessage());
        }
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //Log.i(TAG, "onServiceConnected");
            ServerConnection.ServerConnectionBinder binder = (ServerConnection.ServerConnectionBinder) service;
            mService = binder.getService();
            //mService.registerListener(DriverMapsActivity.this);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            //Log.i(TAG, "onServiceDisconnected");
            mService = null;
        }
    };

    private void sendMe() {
        UserTripInfo userTripInfo = new UserTripInfo(
                1,"912394658","sd",
                "tpr","chennai",1.1,1.1,200,450
        );
        try {
            mService.sendReq(userTripInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processMessage(String myMsg) {
        String msg = MySharedPreference.getInstance(this).getUserMessage();
        if(msg != null) {
            try {
                g = gSon.fromJson(msg, UserTripInfo.class);
                int flag = g.getMyFlag();
                requestingPassenger = g.getUserId();
                switch (flag) {
                    case 4:
                        notifyTimer(g.getMinDis());
                        if (ringtone != null) {
                            ringtone.play();
                        }
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
                if (!mTimerRunning) {
                    startTimer();
                }*/
                        break;
                    default:
                        //userTripInfo.setDriverId(driverId);
                        Toast.makeText(this, "No Progress", Toast.LENGTH_LONG).show();
                        break;
                }
            } catch (Exception e) {

            }
        }
    }

    private void updateResponse(int responseId) {
        requestResponse = responseId;
        //MyBase.getInstance(this).addToRequestQueue(updateIsOnReq);
    }

    @Override
    public void onNewMessage(String myFlag) {

    }

    @Override
    public void onStatusChange(ServerConnection.ConnectionStatus status) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.captainAccept:
                sendAccepted();
                break;
            case R.id.captainBusy:
                sendBusy();
                break;
            default:
                break;
        }
    }
}