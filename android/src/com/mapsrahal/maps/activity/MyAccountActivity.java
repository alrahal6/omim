package com.mapsrahal.maps.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.TextView;

import com.mapsrahal.maps.R;
import com.mapsrahal.util.UiUtils;

public class MyAccountActivity extends AppCompatActivity {

    private TextView mMyBalance;

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
        mMyBalance = findViewById(R.id.my_balance);
        mMyBalance.setText("Available Balance : 100SDG");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}