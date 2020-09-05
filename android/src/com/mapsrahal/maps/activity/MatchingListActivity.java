package com.mapsrahal.maps.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import com.mapsrahal.maps.MySharedPreference;
import com.mapsrahal.maps.R;
import com.mapsrahal.maps.adapter.MatchingAdapter;
import com.mapsrahal.maps.api.ApiClient;
import com.mapsrahal.maps.api.PostApi;
import com.mapsrahal.maps.api.UserMessageApi;
import com.mapsrahal.maps.bookmarks.data.MapObject;
import com.mapsrahal.maps.location.LocationHelper;
import com.mapsrahal.maps.model.MatchMaker;
import com.mapsrahal.maps.model.MatchingItem;
import com.mapsrahal.maps.model.NearbySearch;
import com.mapsrahal.maps.model.Post;
import com.mapsrahal.maps.model.UserMessage;
import com.mapsrahal.util.Constants;
import com.mapsrahal.util.DateUtils;
import com.mapsrahal.util.UiUtils;
import com.mapsrahal.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MatchingListActivity extends AppCompatActivity {

    private ArrayList<NearbySearch> mMatchingList;
    //private int requestToId;

    private RecyclerView mRecyclerView;
    private MatchingAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private static double ELEGIBLE_LIMIT = 1.4d;

    private Button buttonInsert;
    private Button buttonRemove;
    private EditText editTextInsert;
    private EditText editTextRemove;
    private double mMyTripDistance;
    private PostApi postApi;
    private UserMessageApi userMessageApi;
    private String mMatchingPercentage;
    private UserMessage userMessage;
    private MatchMaker mMatchMaker;
    private FusedLocationProviderClient fusedLocationClient;
    //private double fromLat = 15.5007, fromLng = 32.5599;
    //private double fromLat = 15.695882, fromLng = 32.491116;
    private double fromLat = 15.534632, fromLng = 32.553520;

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
        mMyTripDistance = 1.1;//Double.parseDouble(MySharedPreference.getInstance(this).getTripDistance().trim());
        postApi = ApiClient.getClient().create(PostApi.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {
                    // Logic to handle location object
                    fromLat = location.getLatitude();
                    fromLng = location.getLongitude();
                    //Log.d("LatitudeM : ",""+location.getLatitude());
                    //Log.d("LongitudeM : ",""+location.getLongitude());
                    mMatchMaker = new MatchMaker();
                    nearbyPost();
                }
            }
        });
        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                fromLat = location.getLatitude();
                fromLng = location.getLongitude();
                mMatchMaker = new MatchMaker();
                nearbyPost();
            }
        });

        /*fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            fromLat = location.getLatitude();
                            fromLng = location.getLongitude();
                            Log.d("LatitudeM : ",""+location.getLatitude());
                            Log.d("LongitudeM : ",""+location.getLongitude());
                            mMatchMaker = new MatchMaker();
                            nearbyPost();
                        }
                    }


                });*/

        /*mMatchingList = new ArrayList<>();
        for(int i = 0;i<20;i++) {
            mMatchingList.add(new NearbySearch(1, "Khartoum", "Bahari", "7",
                    "2", "6 Aug 2020 13:00", "Male", "100", 1.1,
                    1.1, 1.1, 1.1));
        }
        buildRecyclerView();*/
    }

    // todo add more criteria
    private void nearbyPost() {
        //MapObject m = LocationHelper.INSTANCE.getMyPosition();
        //Log.d("MY Message"," Working1");
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
                //Log.d("MY Message"," Working2");
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
        //Log.d("MY Message"," List");
        for (NearbySearch res : body) {
            //Log.d("MY Message"," Inside");
            mMatchingList.add(new NearbySearch(res.getNearImage(),
                    res.getNearFrom(),res.getNearTo(),res.getNearDistance(),
                    res.getNearSeats(),res.getNearTime(),res.getNearGender(),
                    res.getNearAmount(),res.getFromLat(),res.getFromLng(),
                    res.getToLat(),res.getToLng()));

        }
        buildRecyclerView();
    }

    private double getPercentage(double a,double b) {
        return ((b * 100d) / a)/100d;
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

    private int getFlag() {
        return Constants.Notification.PASSENGER_REQUEST;
    }

    private void openGoogleMap(int position) {
        //private void openInGoogleMap(double fromLat, double fromLng, double toLat, double toLng) {
        double fromLat = mMatchingList.get(position).getFromLat();
        double toLat = mMatchingList.get(position).getToLat();
        double fromLng = mMatchingList.get(position).getFromLng();
        double toLng = mMatchingList.get(position).getToLng();
            String url = "http://maps.google.com/maps?saddr=" + fromLat + ","
                    + fromLng + "&daddr=" + toLat + "," + toLng + "&mode=driving";
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
            intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
            startActivity(intent);
       // }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
