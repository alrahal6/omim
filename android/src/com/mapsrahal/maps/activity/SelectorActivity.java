package com.mapsrahal.maps.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.mapsrahal.maps.MapActivity;
import com.mapsrahal.maps.MySharedPreference;
import com.mapsrahal.maps.R;
import com.mapsrahal.maps.api.ApiClient;
import com.mapsrahal.maps.api.ApiInterface;
import com.mapsrahal.maps.auth.IsBlocked;
import com.mapsrahal.maps.model.AmIBlocked;
import com.mapsrahal.maps.model.GetMyHistory;
import com.mapsrahal.util.UiUtils;

import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectorActivity extends AppCompatActivity implements View.OnClickListener {

    Button mPassengerCab,mPassengerPool,mPassengerAny,mCaptainCab,mCaptainPool,
            mCaptainAny,mNearByPassenger,mPassHist;
    public static final String PASSENGER_CAPTAIN_SELECTOR = "passenger_captain_selector";

    public static final int PASSENGER_TAXI_ONLY = 1;
    public static final int PASSENGER_SHARE_ONLY = 2;
    public static final int PASSENGER_ANY = 3;
    public static final int CAPTAIN_TAXI_ONLY = 4;
    public static final int CAPTAIN_SHARE_ONLY = 5;
    public static final int CAPTAIN_ANY = 6;
    private ProgressBar mProgressbar;
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
        mProgressbar = findViewById(R.id.select_progress);
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
        mNearByPassenger = findViewById(R.id.passengers_nearby);
        mNearByPassenger.setOnClickListener(this);
        mPassHist = findViewById(R.id.passenger_history);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.passenger_cab_only:
                startActivity(PASSENGER_TAXI_ONLY);
                break;
            case R.id.captain_cab_only:
                startActivity(CAPTAIN_TAXI_ONLY);
                break;
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
            case R.id.passengers_nearby:
                startMatchActivity();
                break;
        }
    }

    private void startActivity(int v) {
        MySharedPreference.getInstance(SelectorActivity.this).setSelectorId(v);
        Intent intent = new Intent(SelectorActivity.this, MapActivity.class);
        intent.putExtra(PASSENGER_CAPTAIN_SELECTOR,v);
        startActivity(intent);
        //isBlocked(v);
        /*mProgressbar.setVisibility(View.VISIBLE);
        AmIBlocked amIBlocked = new AmIBlocked(
                MySharedPreference.getInstance(this).getUserId(),
                MySharedPreference.getInstance(this).getPhoneNumber(),
                v
        );

        Call<IsBlocked> call = apiService.verifyUser(amIBlocked);
        call.enqueue(new Callback<IsBlocked>() {
            @Override
            public void onResponse(Call<IsBlocked> call, Response<IsBlocked> response) {
                IsBlocked m = response.body();
                if (m.isAllowed()) {
                    mProgressbar.setVisibility(View.GONE);
                    MySharedPreference.getInstance(SelectorActivity.this).setSelectorId(v);
                    Intent intent = new Intent(SelectorActivity.this, MapActivity.class);
                    intent.putExtra(PASSENGER_CAPTAIN_SELECTOR,v);
                    startActivity(intent);
                    //finish();
                } else {
                    showUserAlreadyConfirmed(m.getMessage());
                }
            }

            @Override
            public void onFailure(Call<IsBlocked> call, Throwable t) {
                showUserAlreadyConfirmed("Failed to connect,please check your internet");
            }
        });*/
    }

    private void startMatchActivity() {
        //MySharedPreference.getInstance(this).setSelectorId(v);
        if(isAllowedNow()) {
            Intent intent = new Intent(this, MatchingListActivity.class);
            //intent.putExtra(PASSENGER_CAPTAIN_SELECTOR,v);
            startActivity(intent);
        } else {
            Toast.makeText(this,"Please try after sometime!",Toast.LENGTH_LONG).show();
        }
    }

    private boolean isAllowedNow() {
        long searchedTime = MySharedPreference.getInstance(this).getIsSearched();
        long newTime = System.currentTimeMillis();
        long millis = TimeUnit.MINUTES.toMillis(10);
        long addedTime = newTime - searchedTime;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(addedTime);
        if(minutes >= 10) {
            return true;
        }
        return false;
    }

    /*private void showUserAlreadyConfirmed(String msg) {
        mProgressbar.setVisibility(View.GONE);
        try {
            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        dialog.dismiss();
                        break;
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(SelectorActivity.this);
            builder.setMessage("Alert! " + msg).setPositiveButton("Dismiss", dialogClickListener)
                    .show();
        } catch (Exception e) {

        }
    }*/
}
