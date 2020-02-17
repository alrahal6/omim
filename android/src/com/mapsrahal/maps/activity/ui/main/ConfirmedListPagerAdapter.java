package com.mapsrahal.maps.activity.ui.main;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import androidx.viewpager.widget.PagerAdapter;

import com.mapsrahal.maps.MySharedPreference;
import com.mapsrahal.maps.R;
import com.mapsrahal.maps.api.ApiClient;
import com.mapsrahal.maps.api.UserMessageApi;
import com.mapsrahal.maps.model.MatchingItem;
import com.mapsrahal.maps.model.UserMessage;
import com.mapsrahal.util.Constants;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ConfirmedListPagerAdapter extends PagerAdapter {

    private ArrayList<UserMessage> matchingItems;
    private LayoutInflater layoutInflater;
    private Context mContext;
    private UserMessage userMessage;
    private ConfirmationSelectionListener confirmListener;
    private final int[] matchingList = new int[20];

    public interface ConfirmationSelectionListener {
        void goToLocation(double toLat, double toLng);
        void showInGMap(double fromLat,double fromLng,double toLat, double toLng);
        void callConfirmed(String phone);
    }

    public ConfirmedListPagerAdapter(ArrayList<UserMessage> matchingItems, Context context, FragmentManager fm) {
        //super(fm);
        confirmListener = (ConfirmationSelectionListener) context;
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

        TextView mTextView1,mTripRoute, mTextView2,mTextView3,mTripDistance,mTripTime,mAmount,mYourDistance,mExtraDistance;
        Button mRequestMatch,mRemoveMatch;
        mTextView1 = view.findViewById(R.id.textView);
        mTextView2 = view.findViewById(R.id.textView2);
        mTextView3 = view.findViewById(R.id.textView3);
        mTripDistance = view.findViewById(R.id.trip_distance);
        mTripTime = view.findViewById(R.id.start_time);
        mAmount = view.findViewById(R.id.trip_amount);
        mTripRoute = view.findViewById((R.id.trip_route_plan));
        mExtraDistance = view.findViewById(R.id.extra_distance);
        mYourDistance = view.findViewById(R.id.your_distance);
        mRequestMatch = view.findViewById(R.id.request_match);
        mRemoveMatch = view.findViewById(R.id.remove_match);

        //matchingItems.get(position).getfLat();
        mTextView1.setText(matchingItems.get(position).getName());
        mTextView2.setText(matchingItems.get(position).getfAddress());
        mTextView3.setText(matchingItems.get(position).gettAddress());
        mTripDistance.setText(matchingItems.get(position).getDistance() + "KM");
        mTripTime.setText("Time : "+matchingItems.get(position).getmTripTime());
        mAmount.setText(matchingItems.get(position).getPrice()+ "SDG");
        mYourDistance.setText("0"+matchingItems.get(position).getPhone());
        mExtraDistance.setText("Gender : "+matchingItems.get(position).getNote());
        mTripRoute.setText("0"+matchingItems.get(position).getPhone());
        mRequestMatch.setText("Route To");
        mRemoveMatch.setText("View On Google Map");
        //mRequestMatch.setOnClickListener(v -> openInGoogleMap());
        mRequestMatch.setOnClickListener(
                v ->  confirmListener.goToLocation(matchingItems.get(position).getfLat(),
                        matchingItems.get(position).getfLng())
        );
        mRemoveMatch.setOnClickListener(v ->
                confirmListener.showInGMap(matchingItems.get(position).getfLat(),
                        matchingItems.get(position).getfLng(),
                        matchingItems.get(position).gettLat(),
                        matchingItems.get(position).gettLng())
        );
        String phoneNumber = "0" + matchingItems.get(position).getPhone();
        mYourDistance.setOnClickListener(v-> confirmListener.callConfirmed(phoneNumber));
        mYourDistance.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_green_24dp, 0, 0, 0);
        //mYourDistance.setDr(R.drawable.ic_call_green_24dp);
        /*if(matchingList[position] != 1) {
            mRequestMatch.setText("Add");
            mRequestMatch.setTextColor(Color.GREEN);
        } else {
            mRequestMatch.setText("Remove");
            mRequestMatch.setTextColor(Color.RED);
        }*/

        container.addView(view, 0);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }
}