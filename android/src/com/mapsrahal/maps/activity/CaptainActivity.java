package com.mapsrahal.maps.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.mapsrahal.maps.MapActivity;
import com.mapsrahal.maps.MwmApplication;
import com.mapsrahal.maps.MySharedPreference;
import com.mapsrahal.maps.R;
import com.mapsrahal.maps.UserTripInfo;
import com.mapsrahal.maps.websocket.ServerConnection;
import com.mapsrahal.maps.websocket.WebSocketViewModel;

public class CaptainActivity extends AppCompatActivity implements ServerConnection.ServerListener {

    private Ringtone ringtone;
    private UserTripInfo g;
    private final Gson gSon = new Gson();
    private WebSocketViewModel mViewModel;
    private ServerConnection mService;
    private int requestingPassenger = 0;
    private int requestResponse = 3;
    private static final String TAG = CaptainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_captain);
        initRingTone();
        mViewModel = ViewModelProviders.of(this).get(WebSocketViewModel.class);
        setObservers();
        //processMessage("");
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
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ringtone = RingtoneManager.getRingtone(this, notification);
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
        //g = gSon.fromJson(myMsg, UserTripInfo.class);
        //int flag = g.getMyFlag();
        final int flag = 4;
        //requestingPassenger = g.getUserId();
        //tripId = String.valueOf(g.getTripId());
        //Log.i(TAG,"request received");
        switch (flag) {
            case 4:
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
                break;
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