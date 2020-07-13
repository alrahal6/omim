package com.mapsrahal.maps.activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mapsrahal.maps.MapActivity;
import com.mapsrahal.maps.MySharedPreference;
import com.mapsrahal.maps.R;
import com.mapsrahal.maps.activity.ui.BlockActivity;
import com.mapsrahal.maps.api.ApiClient;
import com.mapsrahal.maps.api.ApiInterface;
import com.mapsrahal.maps.auth.IsBlocked;
import com.mapsrahal.maps.background.ConnectivityChangedReceiver;
import com.mapsrahal.util.UiUtils;

import java.net.InetAddress;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectorActivity extends AppCompatActivity implements View.OnClickListener {

    Button mPassengerCab,mPassengerPool,mPassengerAny,mCaptainCab,mCaptainPool,mCaptainAny;
    public static final String PASSENGER_CAPTAIN_SELECTOR = "passenger_captain_selector";

    public static final int PASSENGER_TAXI_ONLY = 1;
    public static final int PASSENGER_SHARE_ONLY = 2;
    public static final int PASSENGER_ANY = 3;
    public static final int CAPTAIN_TAXI_ONLY = 4;
    public static final int CAPTAIN_SHARE_ONLY = 5;
    public static final int CAPTAIN_ANY = 6;
    private ApiInterface apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selector);
        UiUtils.setupColorStatusBar(this, R.color.bg_statusbar);
        apiService = ApiClient.getClient().create(ApiInterface.class);
        /*
         todo check is have
          1. confirmed list
          2. confirmed trip
          3. isCaptain online
         */
        mPassengerCab = findViewById(R.id.passenger_cab_only);
        mPassengerCab.setOnClickListener(this);
        mPassengerPool = findViewById(R.id.passenger_pool_only);
        mPassengerPool.setOnClickListener(this);
        mPassengerAny = findViewById(R.id.passenger_any);
        mPassengerAny.setOnClickListener(this);
        mCaptainCab = findViewById(R.id.captain_cab_only);
        mCaptainCab.setOnClickListener(this);
        mCaptainPool = findViewById(R.id.captain_pool_only);
        mCaptainPool.setOnClickListener(this);
        mCaptainAny = findViewById(R.id.captain_any);
        mCaptainAny.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if(MySharedPreference.getInstance(this).isCaptainOnline()) {
            startActivity(CAPTAIN_TAXI_ONLY);
            return;
        }
        switch (v.getId()) {
            /*case R.id.passenger_cab_only:
                startActivity(PASSENGER_TAXI_ONLY);
                break;
            case R.id.captain_cab_only:
                startActivity(CAPTAIN_TAXI_ONLY);
                break;*/
            case R.id.passenger_pool_only:
                startActivity(PASSENGER_SHARE_ONLY);
                break;
            case R.id.passenger_any:
                startActivity(PASSENGER_ANY);
                break;

            case R.id.captain_pool_only:
                startActivity(CAPTAIN_SHARE_ONLY);
                break;
            case R.id.captain_any:
                startActivity(CAPTAIN_ANY);
                break;
        }
    }

    private void startActivity(int v) {
        MySharedPreference.getInstance(getApplicationContext()).setSelectorId(v);
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra(PASSENGER_CAPTAIN_SELECTOR,v);
        startActivity(intent);
    }
}
