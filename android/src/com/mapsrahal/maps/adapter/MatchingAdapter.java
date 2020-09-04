package com.mapsrahal.maps.adapter;


import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mapsrahal.maps.MwmApplication;
import com.mapsrahal.maps.R;
import com.mapsrahal.maps.model.MatchingItem;

import java.util.ArrayList;

public class MatchingAdapter extends RecyclerView.Adapter<MatchingAdapter.MatchingViewHolder> {
    private ArrayList<MatchingItem> mMatchingList;
    private OnItemClickListener mListener;
    private SparseBooleanArray itemStateArray= new SparseBooleanArray();

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public class MatchingViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;
        public Button mRequestMatch;
        public TextView mTextView1;
        public TextView mTextView2,mTripDistance,mTripTime,mYourDistance,mTripRoute,mAmount,mExtraDistance;

        public MatchingViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            //mImageView = itemView.findViewById(R.id.imageView);
            mTextView1 = itemView.findViewById(R.id.textView);
            mTextView2 = itemView.findViewById(R.id.textView2);
            mTripDistance = itemView.findViewById(R.id.trip_distance);
            mTripTime = itemView.findViewById(R.id.start_time);
            mAmount = itemView.findViewById(R.id.trip_amount);
            mTripRoute = itemView.findViewById((R.id.trip_route_plan));
            mExtraDistance = itemView.findViewById(R.id.extra_distance);
            mYourDistance = itemView.findViewById(R.id.your_distance);
            mRequestMatch = itemView.findViewById(R.id.request_match);

            mRequestMatch.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                        //itemStateArray.put(position, true);
                    }
                }
            });
        }

    }

    public MatchingAdapter(ArrayList<MatchingItem> exampleList) {
        mMatchingList = exampleList;
    }

    @Override
    public MatchingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.nearby_list_items, parent, false);
        MatchingViewHolder evh = new MatchingViewHolder(v, mListener);
        return evh;
    }

    @Override
    public void onBindViewHolder(MatchingViewHolder holder, int position) {
        MatchingItem currentItem = mMatchingList.get(position);
        //holder.mImageView.setImageResource(currentItem.getId());
        holder.mTextView1.setText(currentItem.getmText1());
        holder.mTextView2.setText(currentItem.getmText2());
        holder.mTripDistance.setText("Trip Distance : ");
        holder.mTripTime.setText("Time : "+currentItem.getmTripTime());
        holder.mAmount.setText("Amount : "+currentItem.getmAmount());
        holder.mYourDistance.setText("Your Distance : ");
        holder.mExtraDistance.setText("Extra Distance : "+currentItem.getmExtraDistance());
        holder.mTripRoute.setText(""+currentItem.getmTotDistTxt());
        //itemStateArray.put(position,false);
        /*if (!itemStateArray.get(position, false)) {
            itemStateArray.put(position, true);
            holder.mRequestMatch.setText("Waiting...");
            holder.mRequestMatch.setTextColor(Color.BLUE);
        } else {
            itemStateArray.put(position, false);
            holder.mRequestMatch.setText("Request");
            holder.mRequestMatch.setTextColor(MwmApplication.get().getResources().getColor(R.color.base_green));
        }*/
    }

    @Override
    public int getItemCount() {
        return mMatchingList.size();
    }
}
