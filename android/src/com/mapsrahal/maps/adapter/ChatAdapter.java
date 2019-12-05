package com.mapsrahal.maps.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mapsrahal.maps.R;
import com.mapsrahal.maps.model.ChatItem;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private ArrayList<ChatItem> mMessageList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;
        public Button mRequestMatch;
        public TextView mChatMessage;
        //public TextView mTextView2,mTripDistance;

        public ChatViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            //mImageView = itemView.findViewById(R.id.imageView);
            mChatMessage = itemView.findViewById(R.id.chat_message);
            //mTextView2 = itemView.findViewById(R.id.textView2);
            //mTripDistance = itemView.findViewById(R.id.trip_distance);

            /*mRequestMatch.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                        //itemStateArray.put(position, true);
                    }
                }
            });*/
        }

    }

    public ChatAdapter(ArrayList<ChatItem> chatList) {
        mMessageList = chatList;
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_list_item, parent, false);
        ChatViewHolder evh = new ChatViewHolder(v, mListener);
        return evh;
    }

    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {
        ChatItem currentItem = mMessageList.get(position);
        holder.mChatMessage.setText(currentItem.getMessage());
        //holder.mTextView2.setText(currentItem.getMessage());
        //holder.mTripDistance.setText(currentItem.getToPhoneNumber());

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }
}
