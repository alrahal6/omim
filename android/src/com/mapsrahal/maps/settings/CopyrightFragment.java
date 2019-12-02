package com.mapsrahal.maps.settings;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.mapsrahal.maps.R;
import com.mapsrahal.maps.WebContainerDelegate;
import com.mapsrahal.maps.base.OnBackPressListener;
import com.mapsrahal.util.Constants;

public class CopyrightFragment extends BaseSettingsFragment
                            implements OnBackPressListener
{
  private WebContainerDelegate mDelegate;

  @Override
  protected int getLayoutRes()
  {
    return R.layout.fragment_web_view_with_progress;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
  {
    View root = super.onCreateView(inflater, container, savedInstanceState);

    mDelegate = new WebContainerDelegate(root, Constants.Url.COPYRIGHT)
    {
      @Override
      protected void doStartActivity(Intent intent)
      {
        startActivity(intent);
      }
    };

    return root;
  }

  @Override
  public boolean onBackPressed()
  {
    if (!mDelegate.onBackPressed())
      getSettingsActivity().replaceFragment(AboutFragment.class,
                                            getString(R.string.about_menu_title), null);

    return true;
  }
}
