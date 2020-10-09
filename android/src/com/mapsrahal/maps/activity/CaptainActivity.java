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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mapsrahal.maps.MapActivity;
import com.mapsrahal.maps.MwmApplication;
import com.mapsrahal.maps.MySharedPreference;
import com.mapsrahal.maps.R;
import com.mapsrahal.maps.UserTripInfo;
import com.mapsrahal.maps.websocket.ServerConnection;
import com.mapsrahal.maps.websocket.WebSocketViewModel;
import com.mapsrahal.util.UiUtils;

import java.util.Timer;
import java.util.TimerTask;

public class CaptainActivity extends AppCompatActivity implements ServerConnection.ServerListener {

    private Ringtone ringtone;
    private UserTripInfo g;
    private final Gson gSon = new Gson();
    private WebSocketViewModel mViewModel;
    private ServerConnection mService;
    private int requestingPassenger = 0;
    private ProgressBar mCaptProgress;
    private TextView mProgressText;
    private int requestResponse = 3;
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
                    |WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        //String name = null;
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            String name = intent.getStringExtra("ACTION_TYPE");
            if(name == "RECEIVE_CALL") {
                // trip accepted
                //callType ="Audio";
            } else if(name == "DIALOG_CALL") {
                // wanna see the details
                //callType ="Video";
            } else {
                // trip cancelled
            }

        }
        setContentView(R.layout.activity_captain);
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

    private void sendBusy() {
        if (ringtone.isPlaying()) {
            ringtone.stop();
        }
    }

    private void acceptTrip() {

    }

    private void setObservers() {
        mViewModel.getBinder().observe(this, myBinder -> {
            if (myBinder == null) {
                //Log.d(TAG, "onChanged: unbound from service");
            } else {
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
}