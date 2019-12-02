package com.mapsrahal.maps.gallery.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapsrahal.maps.R;
import com.mapsrahal.maps.gallery.Holders;
import com.mapsrahal.maps.gallery.ItemSelectedListener;
import com.mapsrahal.maps.gallery.Items;

class CatalogPromoLoadingAdapterStrategy extends SimpleLoadingAdapterStrategy
{
  CatalogPromoLoadingAdapterStrategy(@Nullable ItemSelectedListener<Items.Item> listener,
                                     @Nullable String url)
  {
    super(listener, url);
  }

  @NonNull
  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent)
  {
    return inflater.inflate(R.layout.catalog_promo_placeholder_card, parent, false);
  }

  @Override
  protected Holders.SimpleViewHolder createViewHolder(@NonNull View itemView)
  {
    return new Holders.CrossPromoLoadingHolder(itemView, mItems, getListener());
  }
}
