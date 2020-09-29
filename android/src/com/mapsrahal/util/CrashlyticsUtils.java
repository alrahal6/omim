package com.mapsrahal.util;

import android.util.Log;

import androidx.annotation.NonNull;

//import com.crashlytics.android.Crashlytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.mapsrahal.maps.MwmApplication;

public final class CrashlyticsUtils
{
  public static void logException(@NonNull Throwable exception)
  {
    if (!checkCrashlytics())
      return;
    FirebaseCrashlytics.getInstance().recordException(exception);
    //Crashlytics.logException(exception);
  }

  public static void log(int priority, @NonNull String tag, @NonNull String msg)
  {
    if (!checkCrashlytics())
      return;
    FirebaseCrashlytics.getInstance().log(toLevel(priority) + "/" + tag + ": " + msg);
    //Crashlytics.log(priority, tag, msg);
  }

  private static boolean checkCrashlytics()
  {
    MwmApplication app = MwmApplication.get();
    return app.getMediator().isCrashlyticsEnabled();
    /*if (!app.getMediator().isCrashlyticsEnabled())
      return false;

    if (!app.getMediator().isCrashlyticsInitialized())
      app.getMediator().initCrashlytics();
    return true;*/
  }

  @NonNull
  private static String toLevel(int level)
  {
    switch (level)
    {
      case Log.VERBOSE:
        return "V";
      case Log.DEBUG:
        return "D";
      case Log.INFO:
        return "I";
      case Log.WARN:
        return "W";
      case Log.ERROR:
        return "E";
      default:
        throw new IllegalArgumentException("Undetermined log level: " + level);
    }
  }

  private CrashlyticsUtils() {}
}
