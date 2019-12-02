package com.mapsrahal.maps.routing;

public interface RoutingBottomMenuListener
{
  void onUseMyPositionAsStart();
  void onSearchRoutePoint(@RoutePointInfo.RouteMarkType int type);
  void onRoutingStart();
}
