package com.mapsrahal.maps.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog;
import com.mapsrahal.maps.R;
import com.mapsrahal.maps.api.ApiClient;
import com.mapsrahal.maps.api.ApiInterface;
import com.mapsrahal.maps.api.PostApi;
import com.mapsrahal.maps.api.RequestApi;
import com.mapsrahal.maps.bookmarks.data.AbstractCategoriesSnapshot;
import com.mapsrahal.maps.model.NearbySearch;
import com.mapsrahal.maps.model.RepeatOnce;
import com.mapsrahal.util.DateUtils;
import com.mapsrahal.util.UiUtils;

import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.POST;

public class RepeatOnceActivity extends AppCompatActivity implements View.OnClickListener {

    private Date startingTime;
    private TextView mDateTime;
    private Button mRepeatTrip;
    private String TAG = RepeatOnceActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UiUtils.setupColorStatusBar(this, R.color.bg_statusbar);
        setContentView(R.layout.activity_repeat_once);
        Toolbar toolbar = findViewById(R.id.toolbar);
        mDateTime = findViewById(R.id.new_time);
        mRepeatTrip = findViewById(R.id.save_repeat_once);
        mRepeatTrip.setOnClickListener(this);
        mDateTime.setOnClickListener(this);
        toolbar.setTitle("Repeat Trip");
        startingTime = DateUtils.timePlusFifteen(new Date());
        mDateTime.setText(DateUtils.formatDate(startingTime));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void dateTime() {
        new SingleDateAndTimePickerDialog.Builder(this)
                .bottomSheet()
                .minDateRange(new Date())
                .displayListener(picker -> {
                })
                .title(getString(R.string.sel_date_time))
                .listener(date -> {
                    startingTime = date;
                    mDateTime.setText(DateUtils.formatDateStr(date));
                }).display();
    }

    private void repeatTrip() {
        //String myDt = ""+startingTime;
        //Log.d(TAG,"date : "+startingTime);
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        RepeatOnce repeatOnce = new RepeatOnce(11,startingTime,
                1,1,"any",2);
        //Log.d(TAG,""+ repeatOnce);
        Call<List<RepeatOnce>> call = apiInterface.repeatOnce(repeatOnce);

        call.enqueue(new Callback<List<RepeatOnce>>() {
            @Override
            public void onResponse(Call<List<RepeatOnce>> call, Response<List<RepeatOnce>> response) {
                //Log.d(TAG,"Response "+response.body());
                //Log.d(TAG,"Repeat Success"+response.message());
                //onBackPressed();
                //Log.d(TAG,"Repeat Success");
                finish();
            }

            @Override
            public void onFailure(Call<List<RepeatOnce>> call, Throwable t) {
                Toast.makeText(RepeatOnceActivity.this,"An error occurred! please try again",Toast.LENGTH_LONG).show();
                //Log.d(TAG,"Repeat failed" + t.getMessage());
            }

        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.new_time:
                dateTime();
                break;
            case R.id.save_repeat_once:
                repeatTrip();
                break;
        }
    }
}