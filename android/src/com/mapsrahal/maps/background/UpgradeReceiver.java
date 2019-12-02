package com.mapsrahal.maps.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mapsrahal.maps.MwmApplication;
import com.mapsrahal.util.CrashlyticsUtils;
import com.mapsrahal.util.log.Logger;
import com.mapsrahal.util.log.LoggerFactory;

import static com.mapsrahal.maps.MwmApplication.backgroundTracker;

public class UpgradeReceiver extends BroadcastReceiver
{
  private static final Logger LOGGER = LoggerFactory.INSTANCE.getLogger(LoggerFactory.Type.MISC);
  private static final String TAG = UpgradeReceiver.class.getSimpleName();
  @Override
  public void onReceive(Context context, Intent intent)
  {
    String msg = "onReceive: " + intent + " app in background = "
                 + !backgroundTracker().isForeground();
    LOGGER.i(TAG, msg);
    CrashlyticsUtils.log(Log.INFO, TAG, msg);
    MwmApplication.onUpgrade();
  }
}
