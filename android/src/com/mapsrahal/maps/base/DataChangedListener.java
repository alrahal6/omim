package com.mapsrahal.maps.base;

public interface DataChangedListener<T> extends Detachable<T>
{
  void onChanged();
}
