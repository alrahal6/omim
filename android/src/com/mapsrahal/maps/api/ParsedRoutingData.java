package com.mapsrahal.maps.api;

import com.mapsrahal.maps.Framework;

/**
 * Represents Framework::ParsedRoutingData from core.
 */
public class ParsedRoutingData
{
  public final RoutePoint[] mPoints;
  @Framework.RouterType
  public final int mRouterType;

  public ParsedRoutingData(RoutePoint[] points, int routerType) {
    this.mPoints = points;
    this.mRouterType = routerType;
  }
}
