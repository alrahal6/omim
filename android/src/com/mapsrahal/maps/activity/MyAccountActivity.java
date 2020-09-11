package com.mapsrahal.maps.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.TextView;

import com.mapsrahal.maps.MySharedPreference;
import com.mapsrahal.maps.R;
import com.mapsrahal.maps.api.ApiClient;
import com.mapsrahal.maps.api.PostApi;
import com.mapsrahal.maps.model.GetMyHistory;
import com.mapsrahal.maps.model.MyAccount;
import com.mapsrahal.maps.model.MyTripHistory;
import com.mapsrahal.maps.model.NearbySearch;
import com.mapsrahal.util.UiUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyAccountActivity extends AppCompatActivity {

    private TextView mMyBalance;
    private PostApi postApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UiUtils.setupColorStatusBar(this, R.color.bg_statusbar);
        setContentView(R.layout.activity_my_account);
        Toolbar toolbar = findViewById(R.id.acc_toolbar);
        toolbar.setTitle(R.string.account_bal);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        postApi = ApiClient.getClient().create(PostApi.class);
        mMyBalance = findViewById(R.id.my_balance);
        getMyBalance();
    }

    private void getMyBalance() {
        GetMyHistory getMyHistory = new GetMyHistory(
                MySharedPreference.getInstance(this).getUserId(),
                MySharedPreference.getInstance(this).getPhoneNumber()
        );

        Call<MyAccount> call = postApi.myAccount(getMyHistory);
        call.enqueue(new Callback<MyAccount>() {
            @Override
            public void onResponse(Call<MyAccount> call, Response<MyAccount> response) {
                if (!response.isSuccessful()) {
                    return;
                }
                MyAccount m = response.body();
                mMyBalance.setText("Available Balance : " + m.getAmount() + " SDG");
            }

            @Override
            public void onFailure(Call<MyAccount> call, Throwable t) {
                //Log.d("MY Message"," Failure"+ t.getMessage());
                mMyBalance.setText("Sorry! unable to get balance");
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}