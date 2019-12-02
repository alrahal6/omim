package com.mapsrahal.maps.tips;

import android.app.Activity;
import androidx.annotation.NonNull;
import android.view.View;

import com.mapsrahal.maps.MwmActivity;
import com.mapsrahal.util.statistics.Statistics;

public abstract class TutorialClickListener implements View.OnClickListener
{
  @NonNull
  private final Activity mActivity;
  @NonNull
  private final Tutorial mTutorial;

  public TutorialClickListener(@NonNull Activity activity, @NonNull Tutorial tutorial)
  {
    mActivity = activity;
    mTutorial = tutorial;
  }

  @Override
  public final void onClick(View v)
  {
    Tutorial tutorial = Tutorial.requestCurrent(mActivity, mActivity.getClass());
    if (tutorial == mTutorial)
    {
      MwmActivity mwmActivity = (MwmActivity) mActivity;
      ClickInterceptor interceptor = tutorial.createClickInterceptor();
      interceptor.onInterceptClick(mwmActivity);
      Statistics.INSTANCE.trackTipsEvent(Statistics.EventName.TIPS_TRICKS_CLICK, tutorial.ordinal());
      return;
    }

    onProcessClick(v);
  }

  public abstract void onProcessClick(@NonNull View view);
}
