package com.mapsrahal.maps.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.mapsrahal.maps.MySharedPreference;
import com.mapsrahal.maps.R;
import com.mapsrahal.maps.adapter.MatchingAdapter;
import com.mapsrahal.maps.api.ApiClient;
import com.mapsrahal.maps.api.PostApi;
import com.mapsrahal.maps.model.NearbySearch;
import com.mapsrahal.maps.websocket.UserLocation;
import com.mapsrahal.util.UiUtils;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MatchingListActivity extends AppCompatActivity {

    private ArrayList<NearbySearch> mMatchingList;
    private RecyclerView mRecyclerView;
    private MatchingAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private TextView mNearbyStatus;
    private PostApi postApi;
    private LocationRequest locationRequest;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 15000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 15000;
    private boolean isHaveLocation = false;
    private FusedLocationProviderClient fusedLocationClient;
    private double fromLat = 1.1, fromLng = 1.1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UiUtils.setupColorStatusBar(this, R.color.bg_statusbar);
        setContentView(R.layout.activity_matching_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        mNearbyStatus = findViewById(R.id.nearby_status);
        toolbar.setTitle(R.string.passengers_nearby);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        postApi = ApiClient.getClient().create(PostApi.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();
        getLocationUpdates();
        fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {
                    isHaveLocation = true;
                    fromLat = location.getLatitude();
                    fromLng = location.getLongitude();
                    nearbyPost();
                }
            }
        });

        if(!isHaveLocation) {
            mNearbyStatus.setVisibility(View.VISIBLE);
        }
    }

    // todo add more criteria
    private void nearbyPost() {
        String dt = ""+new Date();
        NearbySearch nSearch = new NearbySearch(
                1, "", "", "",
                "", dt, "", "",
                 fromLat, fromLng,
                1.1, 1.1
        );

        Call<List<NearbySearch>> call = postApi.nearbySearch(nSearch);
        call.enqueue(new Callback<List<NearbySearch>>() {
            @Override
            public void onResponse(Call<List<NearbySearch>> call, Response<List<NearbySearch>> response) {
                if (!response.isSuccessful()) {
                    return;
                }
                mNearbyStatus.setVisibility(View.GONE);
                if(response.body().size() < 1) {
                    mNearbyStatus.setVisibility(View.VISIBLE);
                    mNearbyStatus.setText("No Passengers found! please try later");
                    return;
                }
                createNearByList(response.body());
            }

            @Override
            public void onFailure(Call<List<NearbySearch>> call, Throwable t) {
                //Log.d("MY Message"," Failure"+ t.getMessage());
                mNearbyStatus.setVisibility(View.VISIBLE);
                mNearbyStatus.setText("No Passengers found! please try later");
            }
        });
    }
    
    public void createNearByList(List<NearbySearch> body) {
        mMatchingList = new ArrayList<>();
        for (NearbySearch res : body) {
            mMatchingList.add(new NearbySearch(res.getNearImage(),
                    res.getNearFrom(),res.getNearTo(),res.getNearDistance(),
                    res.getNearSeats(),res.getNearTime(),res.getNearGender(),
                    res.getNearAmount(),res.getFromLat(),res.getFromLng(),
                    res.getToLat(),res.getToLng()));

        }
        buildRecyclerView();
    }

    public void buildRecyclerView() {
        MySharedPreference.getInstance(this).addToSearched(System.currentTimeMillis());
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new MatchingAdapter(mMatchingList);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(position -> openGoogleMap(position));
    }

    private void openGoogleMap(int position) {
        final double fromLat = mMatchingList.get(position).getFromLat();
        final double toLat = mMatchingList.get(position).getToLat();
        final double fromLng = mMatchingList.get(position).getFromLng();
        final double toLng = mMatchingList.get(position).getToLng();
            String url = "http://maps.google.com/maps?saddr=" + fromLat + ","
                    + fromLng + "&daddr=" + toLat + "," + toLng + "&mode=driving";
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
            intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        try {
            startActivity(intent);
        } catch(Exception e) {
            Toast.makeText(this,"Please install Google Map",Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @SuppressLint("MissingPermission")
    private void getLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest,mLocationCallback, Looper.myLooper());
    }

    private final LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult == null) {
                return;
            }
            for(Location location : locationResult.getLocations()) {

            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }
}
