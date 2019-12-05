package com.mapsrahal.maps.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mapsrahal.maps.R;
import com.mapsrahal.maps.adapter.ChatAdapter;
import com.mapsrahal.maps.adapter.MatchingAdapter;
import com.mapsrahal.maps.api.PostApi;
import com.mapsrahal.maps.api.UserMessageApi;
import com.mapsrahal.maps.model.ChatItem;
import com.mapsrahal.maps.model.MatchMaker;
import com.mapsrahal.maps.model.MatchingItem;
import com.mapsrahal.maps.websocket.ServerConnection;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity implements
        ServerConnection.ServerListener, View.OnClickListener {

    private final String SERVER_URL = "ws://192.168.43.214:3002/x";
    private ServerConnection mServerConnection;
    private TextView mConnectionStatus;
    private TextView mMessageFromServer;
    private Button mSendMessageButton;
    int mCounter = 0;

    private ArrayList<ChatItem> mMessageList = null;
    private int requestToId;

    private RecyclerView mRecyclerView;
    private ChatAdapter mAdapter;
    //private RecyclerView.LayoutManager mLayoutManager;

    private static double ELEGIBLE_LIMIT = 1.4d;

    private Button buttonInsert;
    private Button buttonRemove;
    private EditText editTextInsert;
    private EditText editTextRemove,mMessage;
    private double mMyTripDistance;
    private PostApi postApi;
    private UserMessageApi userMessageApi;
    private String mMatchingPercentage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mConnectionStatus = findViewById(R.id.server_connection_status);
        mMessage = findViewById(R.id.my_message);
        mMessageFromServer = findViewById(R.id.message_from_server);
        mSendMessageButton = findViewById(R.id.send_message_button);
        mSendMessageButton.setOnClickListener(this);
        mServerConnection = new ServerConnection(SERVER_URL);
        mSendMessageButton.setEnabled(false);
        buildRecyclerView();
    }

    public void buildRecyclerView() {
        mMessageList = new ArrayList<>();
        ChatItem c = new ChatItem("x","y","z");
        mMessageList.add(c);
        mRecyclerView = findViewById(R.id.recycler_view_message_list);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mLayoutManager.setStackFromEnd(true);
        mLayoutManager.setSmoothScrollbarEnabled(true);
        //mLayoutManager.setReverseLayout(true);
        mAdapter = new ChatAdapter(mMessageList);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        //mAdapter.setOnItemClickListener(position -> sendRequest(position));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mServerConnection.connect(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mServerConnection.disconnect();
    }

    public void onSendClicked() {
        Log.i("WS","Sending Message");
        mServerConnection.sendMessage(mMessage.getText().toString());
        mMessage.setText("");
    }

    @Override
    public void onNewMessage(String message) {
        //mMessageList = new ArrayList<>();
        Log.i("WS","Received Message : "+ message);
        ChatItem msg = new ChatItem("9123914658",message,"912391525");
        mMessageList.add(msg);
        mAdapter.notifyDataSetChanged();
        //mMessageFromServer.setText(message);
    }

    @Override
    public void onStatusChange(ServerConnection.ConnectionStatus status) {
        String statusMsg = (status == ServerConnection.ConnectionStatus.CONNECTED ?
                getString(R.string.connected) : getString(R.string.disconnected));
        mConnectionStatus.setText(statusMsg);
        mSendMessageButton.setEnabled(status == ServerConnection.ConnectionStatus.CONNECTED);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.send_message_button:
                onSendClicked();
                break;
        }
    }
}
