package com.mapsrahal.maps.editor;

import androidx.fragment.app.Fragment;

import com.mapsrahal.maps.base.BaseMwmFragmentActivity;

public class ReportActivity extends BaseMwmFragmentActivity
{
  @Override
  protected Class<? extends Fragment> getFragmentClass()
  {
    return ReportFragment.class;
  }
}
