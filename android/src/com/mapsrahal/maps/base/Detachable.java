package com.mapsrahal.maps.base;

import androidx.annotation.NonNull;

public interface Detachable<T>
{
  void attach(@NonNull T object);
  void detach();
}
