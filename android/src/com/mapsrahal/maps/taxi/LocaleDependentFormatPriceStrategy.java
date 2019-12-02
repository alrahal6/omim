package com.mapsrahal.maps.taxi;

import androidx.annotation.NonNull;

import com.mapsrahal.util.Utils;

class LocaleDependentFormatPriceStrategy implements FormatPriceStrategy
{
  @NonNull
  @Override
  public String format(@NonNull TaxiInfo.Product product)
  {
    return Utils.formatCurrencyString(product.getPrice(), product.getCurrency());
  }
}
