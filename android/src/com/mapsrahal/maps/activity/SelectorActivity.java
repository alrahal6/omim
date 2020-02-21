package com.mapsrahal.maps.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.mapsrahal.maps.MapActivity;
import com.mapsrahal.maps.MySharedPreference;
import com.mapsrahal.maps.R;
import com.mapsrahal.maps.activity.ui.BlockActivity;
import com.mapsrahal.maps.api.ApiClient;
import com.mapsrahal.maps.api.ApiInterface;
import com.mapsrahal.maps.auth.IsBlocked;
import com.mapsrahal.util.UiUtils;

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

        switch (v.getId()) {
            case R.id.passenger_cab_only:
                startActivity(PASSENGER_TAXI_ONLY);
                break;
            case R.id.passenger_pool_only:
                startActivity(PASSENGER_SHARE_ONLY);
                break;
            case R.id.passenger_any:
                startActivity(PASSENGER_ANY);
                break;
            case R.id.captain_cab_only:
                startActivity(CAPTAIN_TAXI_ONLY);
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

        // todo check is allowed to use this feature
        int id = MySharedPreference.getInstance(getApplicationContext()).getUserId();
        String phone = MySharedPreference.getInstance(getApplicationContext()).getPhoneNumber();
        IsBlocked isBlocked = new IsBlocked(phone,id,false);
        Call<IsBlocked> call = apiService.verifyUser(isBlocked);
        call.enqueue(new Callback<IsBlocked>() {
            @Override
            public void onResponse(Call<IsBlocked> call, Response<IsBlocked> response) {
                if(!response.isSuccessful()) {
                    doNotAllow();
                    return;
                }
                if(response.body().isAllowed()) {
                    allowIn(v);
                } else {
                    doNotAllow();
                }
            }

            @Override
            public void onFailure(Call<IsBlocked> call, Throwable t) {
                doNotAllow();
                return;
            }
        });

        //finish();
    }

    private void allowIn(int v) {
        MySharedPreference.getInstance(getApplicationContext()).setSelectorId(v);
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra(PASSENGER_CAPTAIN_SELECTOR,v);
        startActivity(intent);
    }

    private void doNotAllow() {
        Intent intent = new Intent(this, BlockActivity.class);
        startActivity(intent);
    }
}
