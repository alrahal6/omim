package com.mapsrahal.maps.ugc.routes;

import androidx.fragment.app.Fragment;

import com.mapsrahal.maps.base.BaseToolbarActivity;

public class UgcRoutePropertiesActivity extends BaseToolbarActivity
{
  @Override
  protected Class<? extends Fragment> getFragmentClass()
  {
    return UgcRoutePropertiesFragment.class;
  }
}
