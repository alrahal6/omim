package com.mapsrahal.maps.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mapsrahal.util.CrashlyticsUtils;
import com.mapsrahal.util.log.Logger;
import com.mapsrahal.util.log.LoggerFactory;

import static com.mapsrahal.maps.MwmApplication.backgroundTracker;

public class TrackRecorderWakeReceiver extends BroadcastReceiver
{
  private static final Logger LOGGER = LoggerFactory.INSTANCE.getLogger(LoggerFactory.Type.MISC);
  private static final String TAG = TrackRecorderWakeReceiver.class.getSimpleName();
  @Override
  public void onReceive(Context context, Intent intent)
  {
    String msg = "onReceive: " + intent + " app in background = "
                 + !backgroundTracker().isForeground();
    LOGGER.i(TAG, msg);
    CrashlyticsUtils.log(Log.INFO, TAG, msg);
    TrackRecorder.onWakeAlarm(context);
  }
}
