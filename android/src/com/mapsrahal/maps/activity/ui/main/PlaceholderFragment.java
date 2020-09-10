package com.mapsrahal.maps.activity.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.mapsrahal.maps.MySharedPreference;
import com.mapsrahal.maps.R;
import com.mapsrahal.maps.api.ApiClient;
import com.mapsrahal.maps.api.PostApi;
import com.mapsrahal.maps.model.GetMyHistory;
import com.mapsrahal.maps.model.MyTripHistory;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlaceholderFragment extends Fragment {

    private PostApi postApi;
    private static final String ARG_SECTION_NUMBER = "section_number";
    private TextView mFrom,mTo,mDistance,mSeats,mTime,mGender,mAmount;

    public static PlaceholderFragment newInstance(int index) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postApi = ApiClient.getClient().create(PostApi.class);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_rides, container, false);
        mFrom = root.findViewById(R.id.my_from);
        mTo = root.findViewById(R.id.my_to);
        mDistance = root.findViewById(R.id.my_distance);
        mSeats = root.findViewById(R.id.my_seats);
        mTime = root.findViewById(R.id.my_time);
        mGender = root.findViewById((R.id.my_gender));
        mAmount = root.findViewById(R.id.my_amount);
        getCurrent();
        return root;
    }

    private void getCurrent() {
        GetMyHistory getMyHistory = new GetMyHistory(
                MySharedPreference.getInstance(getActivity()).getUserId(),
                MySharedPreference.getInstance(getActivity()).getPhoneNumber()
        );

        Call<MyTripHistory> call = postApi.myCurrent(getMyHistory);
        call.enqueue(new Callback<MyTripHistory>() {
            @Override
            public void onResponse(Call<MyTripHistory> call, Response<MyTripHistory> response) {
                if (!response.isSuccessful()) {
                    return;
                }
                MyTripHistory res = response.body();
                mFrom.setText(res.getfAddress());
                mTo.setText(res.gettAddress());
                mDistance.setText(""+res.getDistance());
                mSeats.setText(res.getPhone());
                mTime.setText(res.getmTripTime());
                mGender.setText(res.getNote());
                mAmount.setText(res.getPrice());
            }

            @Override
            public void onFailure(Call<MyTripHistory> call, Throwable t) {

            }
        });
    }
}