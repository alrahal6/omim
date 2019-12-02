package com.mapsrahal.maps.editor;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapsrahal.maps.R;
import com.mapsrahal.maps.base.BaseMwmFragmentActivity;
import com.mapsrahal.maps.base.BaseMwmToolbarFragment;
import com.mapsrahal.maps.widget.ToolbarController;
import com.mapsrahal.util.statistics.Statistics;

public class AuthFragment extends BaseMwmToolbarFragment
{
  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
  {
    return inflater.inflate(R.layout.fragment_auth_editor, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
  {
    super.onViewCreated(view, savedInstanceState);
    getToolbarController().setTitle(R.string.thank_you);
    OsmAuthFragmentDelegate osmAuthDelegate = new OsmAuthFragmentDelegate(this)
    {
      @Override
      protected void loginOsm()
      {
        ((BaseMwmFragmentActivity) getActivity()).replaceFragment(OsmAuthFragment.class, null, null);
      }
    };
    osmAuthDelegate.onViewCreated(view, savedInstanceState);
  }

  @NonNull
  @Override
  protected ToolbarController onCreateToolbarController(@NonNull View root)
  {
    return new ToolbarController(root, getActivity())
    {
      @Override
      public void onUpClick()
      {
        Statistics.INSTANCE.trackEvent(Statistics.EventName.EDITOR_AUTH_DECLINED);
        super.onUpClick();
      }
    };
  }
}
