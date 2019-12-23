package com.mapsrahal.maps.activity.ui.main;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import com.mapsrahal.maps.MySharedPreference;
import com.mapsrahal.maps.R;
import com.mapsrahal.maps.activity.MatchingListActivity;
import com.mapsrahal.maps.api.ApiClient;
import com.mapsrahal.maps.api.UserMessageApi;
import com.mapsrahal.maps.model.MatchingItem;
import com.mapsrahal.maps.model.UserMessage;
import com.mapsrahal.util.Constants;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MatchingStatePagerAdapter extends PagerAdapter {

    private ArrayList<MatchingItem> matchingItems;
    private LayoutInflater layoutInflater;
    private Context mContext;
    private UserMessage userMessage;

    public MatchingStatePagerAdapter(ArrayList<MatchingItem> matchingItems,Context context, FragmentManager fm) {
        //super(fm);
        this.mContext = context;
        this.matchingItems = matchingItems;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }

    @Override
    public int getCount() {
        return matchingItems.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        //super.isViewFromObject(view, object);
        return view.equals(object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.matching_list_items, container, false);

        TextView mTextView1,mTripRoute, mTextView2,mTripDistance,mTripTime,mAmount,mYourDistance,mExtraDistance;
        Button mRequestMatch;
        mTextView1 = view.findViewById(R.id.textView);
        mTextView2 = view.findViewById(R.id.textView2);
        mTripDistance = view.findViewById(R.id.trip_distance);
        mTripTime = view.findViewById(R.id.start_time);
        mAmount = view.findViewById(R.id.trip_amount);
        mTripRoute = view.findViewById((R.id.trip_route_plan));
        mExtraDistance = view.findViewById(R.id.extra_distance);
        mYourDistance = view.findViewById(R.id.your_distance);
        mRequestMatch = view.findViewById(R.id.request_match);

        mTextView1.setText(matchingItems.get(position).getmText1());
        mTextView2.setText(matchingItems.get(position).getmText2());
        mTripDistance.setText("Trip Distance : ");
        mTripTime.setText("Time : "+matchingItems.get(position).getmTripTime());
        mAmount.setText("Amount : "+matchingItems.get(position).getmAmount());
        mYourDistance.setText("Your Distance : ");
        mExtraDistance.setText("Extra Distance : "+matchingItems.get(position).getmExtraDistance());
        mTripRoute.setText(""+matchingItems.get(position).getmTotDistTxt());
        mRequestMatch.setOnClickListener(v -> this.sendRequest(position));

        container.addView(view, 0);
        return view;
    }

    public void sendRequest(int position) {
        //mMatchingList.get(position).changeText1(text);
        //mMatchingList.get(position).
        userMessage = new UserMessage(
                MySharedPreference.getInstance(mContext).getUserId(),
                matchingItems.get(position).getUserId(),
                getFlag(),matchingItems.get(position).getId(),
                0.0d,
                matchingItems.get(position).getmTripTime(),
                matchingItems.get(position).getmAmount(),
                matchingItems.get(position).getmPhone(),
                matchingItems.get(position).getmPhone(),
                matchingItems.get(position).getmText1(),
                matchingItems.get(position).getmText2(),
                matchingItems.get(position).getmPhone(),
                0.0d,0.0d,0.0d,0.0d
        );
        //requestToId = mMatchingList.get(position).getUserId();
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("Are you sure? Send Request!").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
        //mAdapter.notifyItemChanged(position);
    }

    private int getFlag() {
        return Constants.Notification.PASSENGER_REQUEST;
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    sendMessage();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    Toast.makeText(mContext, "Request not Send", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    private void sendMessage() {
        UserMessageApi userMessageApi = ApiClient.getClient().create(UserMessageApi.class);
        Call<UserMessage> call = userMessageApi.sentMessage(userMessage);

        call.enqueue(new Callback<UserMessage>() {
            @Override
            public void onResponse(Call<UserMessage> call, Response<UserMessage> response) {
                Toast.makeText(mContext, "Request Send Successfully ", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<UserMessage> call, Throwable t) {
                Toast.makeText(mContext, "Request Send Failed! ", Toast.LENGTH_LONG).show();

            }
        });
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }
}