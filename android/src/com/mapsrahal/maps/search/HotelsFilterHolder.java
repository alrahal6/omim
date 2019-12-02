package com.mapsrahal.maps.search;

import androidx.annotation.Nullable;

interface HotelsFilterHolder
{
  @Nullable
  HotelsFilter getHotelsFilter();
  @Nullable
  BookingFilterParams getFilterParams();
}
