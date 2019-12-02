package com.mapsrahal.maps.background;

import androidx.annotation.NonNull;

public interface NotificationChannelProvider
{
  @NonNull
  String getUGCChannel();

  void setUGCChannel();

  @NonNull
  String getDownloadingChannel();

  void setDownloadingChannel();
}
