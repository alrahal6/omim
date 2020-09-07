package com.mapsrahal.maps.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.mapsrahal.maps.R;
import com.mapsrahal.maps.adapter.MatchingAdapter;
import com.mapsrahal.maps.api.ApiClient;
import com.mapsrahal.maps.api.PostApi;
import com.mapsrahal.maps.model.NearbySearch;
import com.mapsrahal.util.UiUtils;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MatchingListActivity extends AppCompatActivity {

    private ArrayList<NearbySearch> mMatchingList;
    private RecyclerView mRecyclerView;
    private MatchingAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private PostApi postApi;
    private FusedLocationProviderClient fusedLocationClient;
    private double fromLat = 15.5007, fromLng = 32.5599;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UiUtils.setupColorStatusBar(this, R.color.bg_statusbar);
        setContentView(R.layout.activity_matching_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.passengers_nearby);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        postApi = ApiClient.getClient().create(PostApi.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {
                    fromLat = location.getLatitude();
                    fromLng = location.getLongitude();
                    nearbyPost();
                }
            }
        });
        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                fromLat = location.getLatitude();
                fromLng = location.getLongitude();
                nearbyPost();
            }
        });
    }

    // todo add more criteria
    private void nearbyPost() {
        NearbySearch nSearch = new NearbySearch(
                1, "", "", "",
                "", "", "", "",
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
                createNearByList(response.body());
            }

            @Override
            public void onFailure(Call<List<NearbySearch>> call, Throwable t) {
                //Log.d("MY Message"," Failure"+ t.getMessage());
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
            startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
