package com.mapsrahal.maps.widget.placepage;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.mapsrahal.maps.gallery.Items;
import com.mapsrahal.util.ConnectionState;
import com.mapsrahal.util.NetworkPolicy;
import com.mapsrahal.util.Utils;

public class ErrorCatalogPromoListener<T extends Items.Item> implements com.mapsrahal.maps.gallery.ItemSelectedListener<T>
{
  @NonNull
  private final FragmentActivity mActivity;
  @NonNull
  private final NetworkPolicy.NetworkPolicyListener mListener;

  public ErrorCatalogPromoListener(@NonNull FragmentActivity activity,
                                   @NonNull NetworkPolicy.NetworkPolicyListener listener)
  {
    mActivity = activity;
    mListener = listener;
  }

  @Override
  public void onMoreItemSelected(@NonNull T item)
  {
  }

  @Override
  public void onActionButtonSelected(@NonNull T item, int position)
  {
  }

  @Override
  public void onItemSelected(@NonNull T item, int position)
  {
    if (ConnectionState.isConnected())
      NetworkPolicy.checkNetworkPolicy(mActivity.getSupportFragmentManager(),  mListener, true);
    else
      Utils.showSystemSettings(getActivity());
  }

  @NonNull
  protected FragmentActivity getActivity()
  {
    return mActivity;
  }
}
