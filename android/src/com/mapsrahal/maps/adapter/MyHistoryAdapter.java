package com.mapsrahal.maps.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.mapsrahal.maps.R;
import com.mapsrahal.maps.activity.ContactActivity;
import com.mapsrahal.maps.activity.MyAccountActivity;
import com.mapsrahal.maps.activity.MyRidesActivity;
import com.mapsrahal.maps.activity.RepeatOnceActivity;
import com.mapsrahal.maps.activity.RepeatRegularActivity;
import com.mapsrahal.maps.model.MyTripHistory;
import com.mapsrahal.maps.model.MyTripHistory;

import java.util.ArrayList;

public class MyHistoryAdapter extends RecyclerView.Adapter<MyHistoryAdapter.MatchingViewHolder> {
    private ArrayList<MyTripHistory> mMatchingList;
    private OnItemClickListener mListener;
    private SparseBooleanArray itemStateArray= new SparseBooleanArray();
    public static final String TRIP_ID = "hist_trip_id";
    public static final String FROM_ADDR = "from_address";
    public static final String TO_ADDR = "to_address";
    public static final String TRIP_DISTANCE = "trip_distance";

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public class MatchingViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {
        public TextView mFrom,mTo;
        public TextView mDistance,mSeats,mTime,
                mGender,mAmount;
        public ImageButton mRegularMenu;
        private final Context context;

        public MatchingViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            context = itemView.getContext();
            mFrom = itemView.findViewById(R.id.hist_from);
            mTo = itemView.findViewById(R.id.hist_to);
            mDistance = itemView.findViewById(R.id.hist_distance);
            mSeats = itemView.findViewById(R.id.hist_status);
            mTime = itemView.findViewById(R.id.hist_time);
            mGender = itemView.findViewById((R.id.hist_gender));
            mAmount = itemView.findViewById(R.id.hist_amount);
            mRegularMenu = itemView.findViewById(R.id.regular_menu);
            mRegularMenu.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            showRegularMenu(view);
        }

        private void showRegularMenu(View v) {
            PopupMenu regularMenu = new PopupMenu(v.getContext(),v);
            regularMenu.inflate(R.menu.regular_menu);
            regularMenu.setOnMenuItemClickListener(this);
            regularMenu.show();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            Intent intent;
            switch (item.getItemId()) {
                /*case R.id.reschedule:
                   intent = new Intent(context,MyAccountActivity.class);
                   //startActivity(intent);
                    context.startActivity(intent);
                    return true;
                case R.id.need_return:
                    intent = new Intent(context,MyRidesActivity.class);
                    //startActivity(intent);
                    context.startActivity(intent);
                    return true;*/
                case R.id.repeat_once:
                    intent = new Intent(context, RepeatOnceActivity.class);
                    int position = getAdapterPosition();
                    MyTripHistory currentItem = mMatchingList.get(position);
                    intent.putExtra(TRIP_ID,currentItem.getTripId());
                    intent.putExtra(FROM_ADDR,currentItem.getfAddress());
                    intent.putExtra(TO_ADDR,currentItem.gettAddress());
                    intent.putExtra(TRIP_DISTANCE,currentItem.getDistance());
                    //startActivity(intent);
                    context.startActivity(intent);
                    return true;
                case R.id.repeat_regular:
                    intent = new Intent(context, RepeatRegularActivity.class);
                    int positionA = getAdapterPosition();
                    MyTripHistory currentRItem = mMatchingList.get(positionA);
                    intent.putExtra(TRIP_ID,currentRItem.getTripId());
                    intent.putExtra(FROM_ADDR,currentRItem.getfAddress());
                    intent.putExtra(TO_ADDR,currentRItem.gettAddress());
                    intent.putExtra(TRIP_DISTANCE,currentRItem.getDistance());
                    //startActivity(intent);
                    //startActivity(intent);
                    context.startActivity(intent);
                    return true;
                default:
                    return false;
                    //throw new IllegalStateException("Unexpected value: " + item.getItemId());
            }
        }
    }

    public MyHistoryAdapter(ArrayList<MyTripHistory> exampleList) {
        mMatchingList = exampleList;
    }

    @Override
    public MatchingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_history_items, parent, false);
        MatchingViewHolder evh = new MatchingViewHolder(v, mListener);
        return evh;
    }

    @Override
    public void onBindViewHolder(MatchingViewHolder holder, int position) {
        MyTripHistory currentItem = mMatchingList.get(position);
        holder.mFrom.setText(currentItem.getfAddress());
        holder.mTo.setText(currentItem.gettAddress());
        holder.mDistance.setText(currentItem.getDistance()+" KM");
        holder.mSeats.setText(currentItem.getNote());
        holder.mTime.setText(currentItem.getmTripTime());
        holder.mGender.setText("");
        holder.mAmount.setText(currentItem.getPrice()+" SDG");
    }

    @Override
    public int getItemCount() {
        return mMatchingList.size();
    }
}
