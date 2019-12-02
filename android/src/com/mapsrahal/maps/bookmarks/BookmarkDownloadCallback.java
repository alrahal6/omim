package com.mapsrahal.maps.bookmarks;

import androidx.annotation.NonNull;

import com.mapsrahal.maps.bookmarks.data.PaymentData;

public interface BookmarkDownloadCallback
{
  void onAuthorizationRequired();
  void onPaymentRequired(@NonNull PaymentData data);
}
