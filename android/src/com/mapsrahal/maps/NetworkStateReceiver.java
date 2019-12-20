package com.mapsrahal.maps;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.mapsrahal.maps.websocket.ServerConnection;

public class NetworkStateReceiver extends BroadcastReceiver {

    private static final String TAG = NetworkStateReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Network connectivity changed");
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        boolean networkIsOn = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        Intent broadcastIntent = new Intent(ServerConnection.ACTION_NETWORK_STATE_CHANGED);
        broadcastIntent.putExtra(ServerConnection.ACTION_NETWORK_STATE_CHANGED, networkIsOn);
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
    }
}