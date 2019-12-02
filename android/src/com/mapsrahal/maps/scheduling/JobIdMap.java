package com.mapsrahal.maps.scheduling;

import com.mapsrahal.maps.background.NotificationService;
import com.mapsrahal.maps.background.WorkerService;
import com.mapsrahal.maps.bookmarks.SystemDownloadCompletedService;
import com.mapsrahal.maps.geofence.GeofenceTransitionsIntentService;
import com.mapsrahal.maps.location.TrackRecorderWakeService;
import com.mapsrahal.util.Utils;

import java.util.HashMap;
import java.util.Map;

public class JobIdMap
{
  private static final Map<Class<?>, Integer> MAP = new HashMap<>();

  static {
    MAP.put(Utils.isLollipopOrLater() ? NativeJobService.class : FirebaseJobService.class, calcIdentifier(MAP.size()));
    MAP.put(NotificationService.class, calcIdentifier(MAP.size()));
    MAP.put(TrackRecorderWakeService.class, calcIdentifier(MAP.size()));
    MAP.put(SystemDownloadCompletedService.class, calcIdentifier(MAP.size()));
    MAP.put(WorkerService.class, calcIdentifier(MAP.size()));
    MAP.put(GeofenceTransitionsIntentService.class, calcIdentifier(MAP.size()));
  }

  private static final int ID_BASIC = 1070;
  private static final int JOB_TYPE_SHIFTS = 12;

  private static int calcIdentifier(int count)
  {
    return (count + 1 << JOB_TYPE_SHIFTS) + ID_BASIC;
  }

  public static int getId(Class<?> clazz)
  {
    Integer integer = MAP.get(clazz);
    if (integer == null)
      throw new IllegalArgumentException("Value not found for args : " + clazz);
    return integer;
  }
}
