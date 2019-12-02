package com.mapsrahal.maps.tips;

import androidx.annotation.NonNull;

import com.mapsrahal.maps.MwmActivity;

public interface ClickInterceptor
{
  void onInterceptClick(@NonNull MwmActivity activity);
}
