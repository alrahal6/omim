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
import com.mapsrahal.maps.gallery.SimpleSingleItemAdapterStrategy;

public class SimpleErrorAdapterStrategy
    extends SimpleSingleItemAdapterStrategy<Holders.SimpleViewHolder>
{
  SimpleErrorAdapterStrategy(@Nullable ItemSelectedListener<Items.Item> listener)
  {
    super(listener);
  }

  @Override
  protected int getTitle()
  {
    return R.string.discovery_button_other_error_message;
  }

  @NonNull
  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent)
  {
    return inflater.inflate(R.layout.item_discovery_simple_error, parent, false);
  }

  @Override
  protected Holders.SimpleViewHolder createViewHolder(@NonNull View itemView)
  {
    return new Holders.SimpleViewHolder(itemView, mItems, getListener());
  }
}
