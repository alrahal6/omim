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

import com.mapsrahal.maps.R;

public class TripHistoryFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private PageViewModel pageViewModel;

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
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);

        /*Bundle bundle = getArguments();
        index = bundle.getInt(ARG_SECTION_NUMBER);

        //We create a MyRouteListAdapter with a null cursor
        Cursor cursor = null;
        routeListAdapter = new MyRouteListAdapter(cursor, getActivity());
        updateList();*/

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_rides, container, false);
        final TextView textView = root.findViewById(R.id.section_label);
        pageViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText("Recycler View");
            }
        });
        return root;
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