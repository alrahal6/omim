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
import com.mapsrahal.maps.model.NearbySearch;

import java.util.ArrayList;

public class MatchingAdapter extends RecyclerView.Adapter<MatchingAdapter.MatchingViewHolder> {
    private ArrayList<NearbySearch> mMatchingList;
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
        public Button mGoogleMap;
        public TextView mFrom,mTo;
        public TextView mDistance,mSeats,mTime,
                mGender,mAmount;

        public MatchingViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            //mImageView = itemView.findViewById(R.id.imageView);
            mFrom = itemView.findViewById(R.id.near_from);
            mTo = itemView.findViewById(R.id.near_to);
            mDistance = itemView.findViewById(R.id.near_distance);
            mSeats = itemView.findViewById(R.id.near_seats);
            mTime = itemView.findViewById(R.id.near_time);
            mGender = itemView.findViewById((R.id.near_gender));
            mAmount = itemView.findViewById(R.id.near_amount);
            mGoogleMap = itemView.findViewById(R.id.near_google_map);

            mGoogleMap.setOnClickListener(v -> {
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

    public MatchingAdapter(ArrayList<NearbySearch> exampleList) {
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
        NearbySearch currentItem = mMatchingList.get(position);
        //holder.mImageView.setImageResource(currentItem.getId());
        holder.mFrom.setText(currentItem.getNearFrom());
        holder.mTo.setText(currentItem.getNearTo());
        holder.mDistance.setText(currentItem.getNearDistance()+" KM");
        holder.mSeats.setText(currentItem.getNearSeats());
        holder.mTime.setText(currentItem.getNearTime());
        holder.mGender.setText(currentItem.getNearGender());
        holder.mAmount.setText(currentItem.getNearAmount()+" SDG");

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
