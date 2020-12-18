package com.mapsrahal.maps.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;

import com.mapsrahal.maps.R;
import com.mapsrahal.util.UiUtils;

public class PaymentActivity extends AppCompatActivity implements View.OnClickListener {

    private RatingBar ratingbar;
    private Button mSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UiUtils.setupColorStatusBar(this,R.color.bg_statusbar);
        setContentView(R.layout.activity_payment);
        ratingbar = findViewById(R.id.rating_bar);
        mSubmit = findViewById(R.id.rating_button);
        mSubmit.setOnClickListener(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.payment);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onClick(View view) {
        String rating = String.valueOf(ratingbar.getRating());
        //Toast.makeText(this, rating, Toast.LENGTH_LONG).show();

        // todo save and finish
        finish();
    }
}