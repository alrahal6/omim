package com.mapsrahal.maps.downloader;

import androidx.fragment.app.Fragment;

import com.mapsrahal.maps.base.BaseMwmFragmentActivity;
import com.mapsrahal.maps.base.OnBackPressListener;

public class DownloaderActivity extends BaseMwmFragmentActivity
{
  public static final String EXTRA_OPEN_DOWNLOADED = "open downloaded";

  @Override
  protected Class<? extends Fragment> getFragmentClass()
  {
    return DownloaderFragment.class;
  }

  @Override
  public void onBackPressed()
  {
    OnBackPressListener fragment = (OnBackPressListener)getSupportFragmentManager().findFragmentById(getFragmentContentResId());
    if (!fragment.onBackPressed())
      super.onBackPressed();
  }
}
