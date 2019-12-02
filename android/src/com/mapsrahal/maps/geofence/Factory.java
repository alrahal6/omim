package com.mapsrahal.maps.geofence;

import androidx.annotation.NonNull;

import com.google.android.gms.location.Geofence;
import com.mapsrahal.maps.bookmarks.data.FeatureId;

class Factory
{
  @NonNull
  public static FeatureId from(@NonNull Geofence geofence)
  {
    String requestId = geofence.getRequestId();
    return FeatureId.fromFeatureIdString(requestId);
  }
}
