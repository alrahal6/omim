package com.mapsrahal.util.statistics;

import androidx.annotation.NonNull;

public interface StatisticValueConverter<T>
{
  @NonNull
  T toStatisticValue();
}
