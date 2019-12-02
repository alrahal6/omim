package com.mapsrahal.maps.discovery;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

import com.mapsrahal.maps.promo.PromoCityGallery;
import com.mapsrahal.maps.search.SearchResult;

public interface DiscoveryResultReceiver
{
  @MainThread
  void onHotelsReceived(@NonNull SearchResult[] results);
  @MainThread
  void onAttractionsReceived(@NonNull SearchResult[] results);
  @MainThread
  void onCafesReceived(@NonNull SearchResult[] results);
  @MainThread
  void onLocalExpertsReceived(@NonNull LocalExpert[] experts);
  @MainThread
  void onError(@NonNull ItemType type);
  @MainThread
  void onCatalogPromoResultReceived(@NonNull PromoCityGallery promoCityGallery);
  @MainThread
  void onNotFound();
}
