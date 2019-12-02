package com.mapsrahal.maps.geofence;

import androidx.annotation.NonNull;

import com.mapsrahal.maps.location.LocationPermissionNotGrantedException;

public interface GeofenceRegistry
{
  void registerGeofences(@NonNull GeofenceLocation location) throws LocationPermissionNotGrantedException;
  void unregisterGeofences() throws LocationPermissionNotGrantedException;
}
