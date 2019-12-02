package com.mapsrahal.maps.ads;

import androidx.annotation.Nullable;

public interface NativeAdError
{
  @Nullable
  String getMessage();

  int getCode();
}
