package com.mapsrahal.maps.ugc.routes;

import androidx.fragment.app.Fragment;

import com.mapsrahal.maps.base.BaseMwmFragmentActivity;

public class SendLinkPlaceholderActivity extends BaseMwmFragmentActivity
{
  @Override
  protected boolean useTransparentStatusBar()
  {
    return false;
  }

  @Override
  protected Class<? extends Fragment> getFragmentClass()
  {
    return SendLinkPlaceholderFragment.class;
  }
}
