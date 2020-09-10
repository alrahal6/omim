package com.mapsrahal.maps.activity.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mapsrahal.maps.MySharedPreference;
import com.mapsrahal.maps.R;
import com.mapsrahal.maps.adapter.MatchingAdapter;
import com.mapsrahal.maps.adapter.MyHistoryAdapter;
import com.mapsrahal.maps.api.ApiClient;
import com.mapsrahal.maps.api.PostApi;
import com.mapsrahal.maps.model.GetMyHistory;
import com.mapsrahal.maps.model.MyTripHistory;
import com.mapsrahal.maps.model.NearbySearch;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TripHistoryFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private MyHistoryAdapter mAdapter;
    private PostApi postApi;
    private ArrayList<MyTripHistory> mHistoryList;

    public static TripHistoryFragment newInstance(int index) {
        TripHistoryFragment fragment = new TripHistoryFragment();
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
        View root = inflater.inflate(R.layout.fragment_history_trip, container, false);
        mRecyclerView = root.findViewById(R.id.rv_history);
        nearbyPost();
        return root;
    }

    private void nearbyPost() {
        GetMyHistory getMyHistory = new GetMyHistory(
          MySharedPreference.getInstance(getActivity()).getUserId(),
                MySharedPreference.getInstance(getActivity()).getPhoneNumber()
        );

        Call<List<MyTripHistory>> call = postApi.myHistory(getMyHistory);
        call.enqueue(new Callback<List<MyTripHistory>>() {
            @Override
            public void onResponse(Call<List<MyTripHistory>> call, Response<List<MyTripHistory>> response) {
                if (!response.isSuccessful()) {
                    return;
                }
                createNearByList(response.body());
            }

            @Override
            public void onFailure(Call<List<MyTripHistory>> call, Throwable t) {
                //Log.d("MY Message"," Failure"+ t.getMessage());
            }
        });
    }

    public void createNearByList(List<MyTripHistory> body) {
        mHistoryList = new ArrayList<>();
        for (MyTripHistory res : body) {
            mHistoryList.add(new MyTripHistory(
                    res.getfUserId(),res.gettUserId(),res.getmFlag(),
                    res.getTripId(),res.getDistance(),res.getPrice(),res.getmTripTime(),
                    res.getPhone(),res.getName(),res.getfAddress(),res.gettAddress(),
                    res.getNote(),res.getfLat(),res.gettLng(),res.gettLat(),res.gettLng()
            ));

        }
        buildRecyclerView();
    }

    public void buildRecyclerView() {
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mAdapter = new MyHistoryAdapter(mHistoryList);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

}