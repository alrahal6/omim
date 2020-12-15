package com.mapsrahal.maps.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dpro.widgets.OnWeekdaysChangeListener;
import com.dpro.widgets.WeekdaysPicker;
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog;
import com.mapsrahal.maps.R;
import com.mapsrahal.maps.api.ApiClient;
import com.mapsrahal.maps.api.ApiInterface;
import com.mapsrahal.maps.model.RepeatOnce;
import com.mapsrahal.maps.model.RepeatRegular;
import com.mapsrahal.util.DateUtils;
import com.mapsrahal.util.UiUtils;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapsrahal.maps.adapter.MyHistoryAdapter.FROM_ADDR;
import static com.mapsrahal.maps.adapter.MyHistoryAdapter.TO_ADDR;
import static com.mapsrahal.maps.adapter.MyHistoryAdapter.TRIP_DISTANCE;
import static com.mapsrahal.maps.adapter.MyHistoryAdapter.TRIP_ID;

public class RepeatRegularActivity extends AppCompatActivity implements
        View.OnClickListener, AdapterView.OnItemSelectedListener {

    private Date startingTime;
    private TextView mDateTime,mRoFrom,mRoTo,mRoDistance,mRequiredSeats;
    private Button mRepeatTrip;
    private double tripId;
    private String genderCargoTxt = "";
    private WeekdaysPicker widget;

    private int seatCount = 1;
    private int genderCargoId = 0;
    private ImageView mAddSeat, mRemoveSeat;
    private String TAG = RepeatOnceActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UiUtils.setupColorStatusBar(this, R.color.bg_statusbar);
        setContentView(R.layout.activity_repeat_regular);
        Toolbar toolbar = findViewById(R.id.toolbar);
        mDateTime = findViewById(R.id.new_time);
        mRepeatTrip = findViewById(R.id.save_repeat_once);
        mRoFrom = findViewById(R.id.ro_from);
        mRoTo = findViewById(R.id.ro_to);
        mRoDistance = findViewById(R.id.ro_distance);
        tripId = getIntent().getDoubleExtra(TRIP_ID,0);
        String fromAddress = getIntent().getStringExtra(FROM_ADDR);
        String toAddress = getIntent().getStringExtra(TO_ADDR);
        double distance = getIntent().getDoubleExtra(TRIP_DISTANCE,0);
        mRoFrom.setText(fromAddress);
        mRoTo.setText(toAddress);
        mRequiredSeats = findViewById(R.id.required_ro_seats);
        mRoDistance.setText(distance+" KM");
        mAddSeat = findViewById(R.id.add_ro_seat);
        mAddSeat.setOnClickListener(this);
        mRemoveSeat = findViewById(R.id.remove_ro_seat);
        mRemoveSeat.setOnClickListener(this);
        widget = (WeekdaysPicker) findViewById(R.id.weekdays);
        List<Integer> days = Arrays.asList(Calendar.MONDAY,Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.SUNDAY);

        widget.setSelectedDays(days);
        widget.setOnWeekdaysChangeListener(new OnWeekdaysChangeListener() {
            @Override
            public void onChange(View view, int clickedDayOfWeek, List<Integer> selectedDays) {
                // Do Something
            }
        });
        //bundle.get(TRIP_ID);
        ArrayAdapter<CharSequence> adapter;
        adapter = ArrayAdapter.createFromResource(this,
                R.array.select_gender, android.R.layout.simple_spinner_item);
        Spinner spinner = findViewById(R.id.gender_spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        mRepeatTrip.setOnClickListener(this);
        mDateTime.setOnClickListener(this);
        toolbar.setTitle(R.string.repeat_regular);
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
        int sun = 2,mon=2,tue=2,wed=2,thu=2,fri=2,sat=2;
        List<Integer> selectedDays = widget.getSelectedDays();
        int[] array = new int[selectedDays.size()];
        if(array.length == 0) {
            finish();
        }
        for(int i=0;i<array.length;i++) {
            array[i] = selectedDays.get(i);
            switch (array[i]) {
                case 1:
                    sun = 1;
                    break;
                case 2:
                    mon = 1;
                    break;
                case 3:
                    tue = 1;
                    break;
                case 4:
                    wed = 1;
                    break;
                case 5:
                    thu = 1;
                    break;
                case 6:
                    fri = 1;
                    break;
                case 7:
                    sat = 1;
                    break;
            }
            //boolean sun =
            //Log.d(TAG,"sun : "+array[i]);
            //System.out.println(array[i]);
        }
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        RepeatRegular repeatRegular = new RepeatRegular(tripId,startingTime,
                1,genderCargoId,genderCargoTxt,seatCount,
                sun,mon,tue,wed,thu,fri,sat);
        //Log.d(TAG,""+ repeatOnce);
        Call<List<RepeatRegular>> call = apiInterface.repeatRegular(repeatRegular);

        call.enqueue(new Callback<List<RepeatRegular>>() {
            @Override
            public void onResponse(Call<List<RepeatRegular>> call, Response<List<RepeatRegular>> response) {
                //Log.d(TAG,"Response "+response.body());
                //Log.d(TAG,"Repeat Success"+response.message());
                //onBackPressed();
                //Log.d(TAG,"Repeat Success");
                finish();
            }

            @Override
            public void onFailure(Call<List<RepeatRegular>> call, Throwable t) {
                Toast.makeText(RepeatRegularActivity.this,"An error occurred! please try again",Toast.LENGTH_LONG).show();
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
            case R.id.add_ro_seat:
                if (seatCount < 4) {
                    seatCount++;
                    setSeat();
                    //getCalculatedPrice();
                } else {
                    Toast.makeText(this, getString(R.string.max_4_seats), Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.remove_ro_seat:
                if (seatCount > 1) {
                    seatCount--;
                    setSeat();
                    //getCalculatedPrice();
                }
                break;
        }
    }

    private void setSeat() {
        mRequiredSeats.setText("" + seatCount);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String text = adapterView.getItemAtPosition(i).toString();
        genderCargoTxt = text;
        genderCargoId = i;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}