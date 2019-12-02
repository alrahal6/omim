package com.mapsrahal.maps.widget.placepage;

import androidx.annotation.NonNull;

import com.mapsrahal.maps.settings.RoadType;

public interface RoutingModeListener
{
  void toggleRouteSettings(@NonNull RoadType roadType);
}
