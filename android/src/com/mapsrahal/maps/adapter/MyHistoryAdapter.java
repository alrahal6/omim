package com.mapsrahal.maps.adapter;

import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mapsrahal.maps.R;
import com.mapsrahal.maps.model.MyTripHistory;
import com.mapsrahal.maps.model.MyTripHistory;

import java.util.ArrayList;

public class MyHistoryAdapter extends RecyclerView.Adapter<MyHistoryAdapter.MatchingViewHolder> {
    private ArrayList<MyTripHistory> mMatchingList;
    private OnItemClickListener mListener;
    private SparseBooleanArray itemStateArray= new SparseBooleanArray();

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public class MatchingViewHolder extends RecyclerView.ViewHolder {
        public TextView mFrom,mTo;
        public TextView mDistance,mSeats,mTime,
                mGender,mAmount;

        public MatchingViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            mFrom = itemView.findViewById(R.id.hist_from);
            mTo = itemView.findViewById(R.id.hist_to);
            mDistance = itemView.findViewById(R.id.hist_distance);
            mSeats = itemView.findViewById(R.id.hist_seats);
            mTime = itemView.findViewById(R.id.hist_time);
            mGender = itemView.findViewById((R.id.hist_gender));
            mAmount = itemView.findViewById(R.id.hist_amount);
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
        holder.mSeats.setText(currentItem.getName());
        holder.mTime.setText(currentItem.getNote());
        holder.mGender.setText(currentItem.getPhone());
        holder.mAmount.setText(currentItem.getPrice()+" SDG");
    }

    @Override
    public int getItemCount() {
        return mMatchingList.size();
    }
}
