package com.mapsrahal.maps;


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

import com.mapsrahal.maps.adapter.MatchingAdapter;
import com.mapsrahal.maps.api.ApiClient;
import com.mapsrahal.maps.api.PostApi;
import com.mapsrahal.maps.api.UserMessageApi;
import com.mapsrahal.maps.model.MatchMaker;
import com.mapsrahal.maps.model.MatchingItem;
import com.mapsrahal.maps.model.Post;
import com.mapsrahal.maps.model.UserMessage;
import com.mapsrahal.util.DateUtils;
import com.mapsrahal.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MatchingListActivity extends AppCompatActivity {

    private ArrayList<MatchingItem> mMatchingList;
    private int requestToId;

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

    private MatchMaker mMatchMaker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matching_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mMyTripDistance = Double.parseDouble(MySharedPreference.getInstance(this).getTripDistance().trim());
        postApi = ApiClient.getClient().create(PostApi.class);
        userMessageApi = ApiClient.getClient().create(UserMessageApi.class);
        mMatchMaker = new MatchMaker();
        createPost();
    }

    // todo add more criteria
    private void createPost() {
        Post post = new Post(null,MySharedPreference.getInstance(this).getUserId(),
                MySharedPreference.getInstance(this).getFrmLat(),
                MySharedPreference.getInstance(this).getFrmLng(),
                MySharedPreference.getInstance(this).getToLat(),
                MySharedPreference.getInstance(this).getToLng(),
                mMyTripDistance,
                MySharedPreference.getInstance(this).getFrmAddress().trim(),
                MySharedPreference.getInstance(this).getToAddress().trim(),
                new Date(MySharedPreference.getInstance(this).getStartTime()));

        Call<List<Post>> call = postApi.createPost(post);

        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                if (!response.isSuccessful()) {
                    return;
                }
                createMatchList(response.body());
            }
            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {

            }
        });
    }

    public void createMatchList(List<Post> body) {
        mMatchingList = new ArrayList<>();
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
                            amount,extraDistance,mMyTripDistance));
                }
            } else {
                if (isPassengerEligible(mMyTripDistance, totDist, post.getSrcDistDiff(), post.getDestDistDiff(), post.getTripDistance())) {
                    // todo get accurate distance and add
                    mMatchingList.add(new MatchingItem(post.getId(),post.getUserId(),
                            post.getSourceAddress(), post.getDestinationAddress(),
                            post.getTripDistance(),  DateUtils.formatDateStr(post.getStartTime()), totDist, totDistTxt,
                            amount,extraDistance,mMyTripDistance));
                }
            }
        }
        Collections.sort(mMatchingList);
        Collections.reverse(mMatchingList);
        buildRecyclerView();
    }

    private double getPercentage(double a,double b) {
        return ((b * 100d) / a)/100d;
    }

    //  todo check later
    private boolean isCaptainEligible(double mMyTripDistance, double totDist, double srcDistDiff,
                               double destDistDiff, double tripDistance) {
        // my trip distance is greater than my distance
        if (mMyTripDistance >= totDist) {
            return true;
        } else {
            // my trip distance is less than my distance
            // so i have to travel more as a captain
            double percentage = getPercentage(mMyTripDistance,totDist);
            if(percentage > ELEGIBLE_LIMIT)
                return false;
            return true;
        }
    }

    // todo check later
    private boolean isPassengerEligible(double mMyTripDistance, double totDist, double srcDistDiff,
                                        double destDistDiff, double tripDistance) {
        // my trip distance is greater than my distance
        if (mMyTripDistance <= totDist) {
            return true;
            // totDist
        } else {
            double percentage = getPercentage(totDist,mMyTripDistance);
            if(percentage > ELEGIBLE_LIMIT)
                return false;
            return true;
        }
    }

    private String prepareRouteDistance(double ab,double bc, double cd) {
        //Spannable wordtoSpan = new SpannableString("");
        //wordtoSpan.setSpan(new ForegroundColorSpan(Color.BLUE), 15, 30, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        String a = "A- ";
        String b = " ->B- ";
        String c = " ->C-> ";
        String d = " ->D";
        return a+ab+b+bc+c+cd+d;
    }

    public void buildRecyclerView() {
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new MatchingAdapter(mMatchingList);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(position -> sendRequest(position));
    }

    public void sendRequest(int position) {
        //mMatchingList.get(position).changeText1(text);
        //mMatchingList.get(position).
        requestToId = mMatchingList.get(position).getUserId();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure? Send Request!").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
        //mAdapter.notifyItemChanged(position);
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    sendMessage();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    Toast.makeText(MatchingListActivity.this, "Request not Send", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    private void sendMessage() {
        UserMessage userMessage = new UserMessage(requestToId,"You have ride request","You got matching ride");

        Call<UserMessage> call = userMessageApi.sentMessage(userMessage);

        call.enqueue(new Callback<UserMessage>() {
            @Override
            public void onResponse(Call<UserMessage> call, Response<UserMessage> response) {
                Toast.makeText(MatchingListActivity.this, "Request Send Successfully " +requestToId, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<UserMessage> call, Throwable t) {
                Toast.makeText(MatchingListActivity.this, "Request Send Failed! " +requestToId, Toast.LENGTH_LONG).show();

            }
        });
    }

      /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm");
        builder.setMessage("Are you sure?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing but close the dialog
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
        */

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
