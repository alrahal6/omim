package com.mapsrahal.maps.editor;

import androidx.fragment.app.Fragment;

import com.mapsrahal.maps.base.BaseMwmFragmentActivity;

public class OsmAuthActivity extends BaseMwmFragmentActivity
{
  @Override
  protected Class<? extends Fragment> getFragmentClass()
  {
    return OsmAuthFragment.class;
  }
}
