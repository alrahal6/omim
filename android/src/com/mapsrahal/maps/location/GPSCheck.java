package com.mapsrahal.maps.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mapsrahal.maps.MwmApplication;
import com.mapsrahal.util.log.Logger;
import com.mapsrahal.util.log.LoggerFactory;

import static com.mapsrahal.maps.MwmApplication.backgroundTracker;

public class GPSCheck extends BroadcastReceiver
{
  private static final Logger LOGGER = LoggerFactory.INSTANCE.getLogger(LoggerFactory.Type.LOCATION);
  private static final String TAG = GPSCheck.class.getSimpleName();
  @Override
  public void onReceive(Context context, Intent intent) {
    String msg = "onReceive: " + intent + " app in background = "
                 + !backgroundTracker().isForeground();
    LOGGER.i(TAG, msg);
    if (MwmApplication.get().arePlatformAndCoreInitialized() && MwmApplication.backgroundTracker().isForeground())
    {
      LocationHelper.INSTANCE.restart();
    }
  }
}
