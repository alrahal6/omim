package com.mapsrahal.maps.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mapsrahal.maps.MySharedPreference;
import com.mapsrahal.maps.R;
import com.mapsrahal.maps.adapter.MatchingAdapter;
import com.mapsrahal.maps.api.ApiClient;
import com.mapsrahal.maps.api.PostApi;
import com.mapsrahal.maps.api.UserMessageApi;
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
        userMessageApi = ApiClient.getClient().create(UserMessageApi.class);
        mMatchMaker = new MatchMaker();
        //createPost();
        mMatchingList = new ArrayList<>();

        mMatchingList.add(new NearbySearch(1,"Khartoum","Bahari","7",
        "2","6 Aug 2020 13:00","Male","100",1.1,
                1.1,1.1,1.1));

        buildRecyclerView();
    }

    // todo add more criteria
    private void createPost() {
        /*Post post = new Post(null,MySharedPreference.getInstance(this).getUserId(),
                MySharedPreference.getInstance(this).getFrmLat(),
                MySharedPreference.getInstance(this).getFrmLng(),
                MySharedPreference.getInstance(this).getToLat(),
                MySharedPreference.getInstance(this).getToLng(),
                mMyTripDistance,
                MySharedPreference.getInstance(this).getFrmAddress().trim(),
                MySharedPreference.getInstance(this).getToAddress().trim(),
                new Date(MySharedPreference.getInstance(this).getStartTime()),
                MySharedPreference.getInstance(this).getPhoneNumber(),0,0,"x",
                0.0,1,"x","");

        Call<List<Post>> call = postApi.createPost(post);

        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                if (!response.isSuccessful()) {
                    return;
                }
                //createMatchList(response.body());
            }
            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {

            }
        });*/
    }

    public void createMatchList(List<Post> body) {
        /*mMatchingList = new ArrayList<>();
        for (Post post : body) {
            String totDistTxt = prepareRouteDistance(Utils.roundTwoDecimals(post.getSrcDistDiff()),
                    Utils.roundTwoDecimals(post.getTripDistance()),Utils.roundTwoDecimals(post.getDestDistDiff()));
            double totDist = Utils.roundTwoDecimals(post.getSrcDistDiff()+post.getTripDistance()+post.getDestDistDiff());
            double extra = 0;
            if(mMyTripDistance < totDist) {
                extra = totDist - mMyTripDistance;
            }

            String amount = "" + post.getTripDistance() * 2;
            String extraDistance = "" + Utils.roundTwoDecimals(extra);
            //mMatchMaker.getMatchingList();
            if(MySharedPreference.getInstance(this).isCaptain()) {
                if (isCaptainEligible(mMyTripDistance, totDist, post.getSrcDistDiff(), post.getDestDistDiff(), post.getTripDistance())) {
                    // todo get accurate distance and add
                    mMatchingList.add(new MatchingItem(post.getId(),post.getUserId(),
                            post.getSourceAddress(), post.getDestinationAddress(),
                            post.getTripDistance(), DateUtils.formatDateStr(post.getStartTime()), totDist, totDistTxt,
                            amount,extraDistance,mMyTripDistance,post.getEndTime()));
                }
            } else {
                if (isPassengerEligible(mMyTripDistance, totDist, post.getSrcDistDiff(), post.getDestDistDiff(), post.getTripDistance())) {
                    // todo get accurate distance and add
                    mMatchingList.add(new MatchingItem(post.getId(),post.getUserId(),
                            post.getSourceAddress(), post.getDestinationAddress(),
                            post.getTripDistance(),  DateUtils.formatDateStr(post.getStartTime()), totDist, totDistTxt,
                            amount,extraDistance,mMyTripDistance,post.getEndTime()));
                }
            }
        }
        //Collections.sort(mMatchingList);
        //Collections.reverse(mMatchingList);
        buildRecyclerView();*/
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

    public void openGoogleMap(int position) {
        //mMatchingList.get(position).getmAmount();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
