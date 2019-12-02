package com.mapsrahal.maps.adapter;

import androidx.annotation.NonNull;
import android.view.View;

public interface OnItemClickListener<T>
{
  void onItemClick(@NonNull View v, @NonNull T item);
}
