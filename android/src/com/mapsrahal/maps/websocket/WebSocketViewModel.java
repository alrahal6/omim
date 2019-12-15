package com.mapsrahal.maps.websocket;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class WebSocketViewModel extends ViewModel {
    private static final String TAG = "WebSocketViewModel";

    private MutableLiveData<Boolean> mIsProgressBarUpdating = new MutableLiveData<>();
    private boolean isConnected = false;
    private MutableLiveData<Boolean> mIsConnected = new MutableLiveData<>();
    private MutableLiveData<ServerConnection.ServerConnectionBinder> mBinder = new MutableLiveData<>();

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder iBinder) {
            //Log.d(TAG, "ServiceConnection: connected to service.");
            // We've bound to MyService, cast the IBinder and get MyBinder instance
            ServerConnection.ServerConnectionBinder binder = (ServerConnection.ServerConnectionBinder) iBinder;
            mBinder.postValue(binder);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            //Log.d(TAG, "ServiceConnection: disconnected from service.");
            mBinder.postValue(null);
        }
    };


    public ServiceConnection getServiceConnection() {
        return serviceConnection;
    }

    public LiveData<ServerConnection.ServerConnectionBinder> getBinder() {
        return mBinder;
    }


    public LiveData<Boolean> getIsProgressBarUpdating(){
        return mIsProgressBarUpdating;
    }

    public void setIsProgressBarUpdating(boolean isUpdating){
        mIsProgressBarUpdating.postValue(isUpdating);
    }

    public void setIsConnected(boolean isConnected) {
        mIsConnected.postValue(isConnected);
    }

    public LiveData<Boolean> getIsConnected() {
        return mIsConnected;
    }

}
