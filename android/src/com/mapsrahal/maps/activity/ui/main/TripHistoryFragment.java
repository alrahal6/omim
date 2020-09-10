package com.mapsrahal.maps.activity.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mapsrahal.maps.MySharedPreference;
import com.mapsrahal.maps.R;
import com.mapsrahal.maps.adapter.MatchingAdapter;
import com.mapsrahal.maps.api.ApiClient;
import com.mapsrahal.maps.api.PostApi;
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
    private MatchingAdapter mAdapter;
    private PostApi postApi;
    private ArrayList<NearbySearch> mMatchingList;

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
        /*pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);*/

        /*Bundle bundle = getArguments();
        index = bundle.getInt(ARG_SECTION_NUMBER);

        //We create a MyRouteListAdapter with a null cursor
        Cursor cursor = null;
        routeListAdapter = new MyRouteListAdapter(cursor, getActivity());
        updateList();*/
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history_trip, container, false);
        mRecyclerView = root.findViewById(R.id.rv_history);
        nearbyPost();
        /*mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mAdapter = new MatchingAdapter(mMatchingList);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);*/
        return root;
    }

    private void nearbyPost() {
        NearbySearch nSearch = new NearbySearch(
                1, "", "", "",
                "", "", "", "",
                15.500700, 32.559898,
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
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mAdapter = new MatchingAdapter(mMatchingList);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    /*
    @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            //Inflate the layout
            layout = (RelativeLayout) inflater.inflate(R.layout.fragment_route_list, container, false);

            routeSource = new RouteDataSource (getActivity());

            //Now that we have the routeSource, we can prepare the routeListAdapter
            routeRecyclerList = (RecyclerView) layout.findViewById(R.id.route_list_view);
             // use a linear layout manager
            mLayoutManager = new LinearLayoutManager(getActivity());
            routeRecyclerList.setLayoutManager(mLayoutManager);

            routeRecyclerList.setAdapter(routeListAdapter);

            return layout;
        }

            public void updateList(){

              //we get the loaderManager to start the query
              //If a Loader has already been initiated, we need to reload it. Otherwise the
              //onCreateLoader callback method will not be called
              //Else, we initiate a Loader

            LoaderManager manager = getLoaderManager();
            if (manager.getLoader(LOADER_GET_ALL_ROUTES)!= null)
                    manager.restartLoader(LOADER_GET_ALL_ROUTES, null, this);
            else manager.initLoader(LOADER_GET_ALL_ROUTES, null, this);

        }
       */
}