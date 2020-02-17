package com.mapsrahal.maps.activity.ui.main;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
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


public class CargoStatePagerAdapter extends PagerAdapter {

    private ArrayList<MatchingItem> matchingItems;
    private LayoutInflater layoutInflater;
    private Context mContext;
    //private UserMessage userMessage;
    private CargoSelectionListener matchingListener;
    //private final int[] matchingList = new int[20];

    public interface CargoSelectionListener {
        void callMatch(int position);
        void routeInMap(double fromLat, double fromLng, double toLat, double toLng);
    }

    public CargoStatePagerAdapter(ArrayList<MatchingItem> matchingItems, Context context, FragmentManager fm) {
        //super(fm);
        matchingListener = (CargoSelectionListener) context;
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


        mTextView1.setText(matchingItems.get(position).getName());
        mTextView2.setText(matchingItems.get(position).getmText1());
        mTextView3.setText(matchingItems.get(position).getmText2());
        mTripDistance.setText(matchingItems.get(position).getmTotDistTxt()+" KM");
        mTripTime.setText("Time : "+matchingItems.get(position).getmTripTime());
        mAmount.setText(matchingItems.get(position).getDropDownVal());
        //mAmount.setVisibility(View.GONE);
        mYourDistance.setText("Vehicle : "+ matchingItems.get(position).getDropDownVal());
        //mExtraDistance.setText("Extra Distance : "+matchingItems.get(position).getmExtraDistance());
        //mTripRoute.setText(""+matchingItems.get(position).getmTotDistTxt());
        mRequestMatch.setText("Call");
        mRequestMatch.setTextColor(Color.GREEN);
        mRequestMatch.setOnClickListener(v -> matchingListener.callMatch(position));
        mRemoveMatch.setOnClickListener(v ->
                    matchingListener.routeInMap(matchingItems.get(position).getfLat(),
                            matchingItems.get(position).getfLng(),
                            matchingItems.get(position).gettLat(),
                            matchingItems.get(position).gettLng())
        );
        container.addView(view, 0);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }
}