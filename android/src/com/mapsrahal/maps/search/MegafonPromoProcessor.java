package com.mapsrahal.maps.search;

import android.app.Activity;
import androidx.annotation.NonNull;

import com.mapsrahal.maps.Framework;
import com.mapsrahal.util.Utils;

public class MegafonPromoProcessor implements PromoCategoryProcessor
{
  @NonNull
  private final Activity mActivity;

  MegafonPromoProcessor(@NonNull Activity activity)
  {
    mActivity = activity;
  }

  @Override
  public void process()
  {
    Utils.openUrl(mActivity, Framework.nativeGetMegafonCategoryBannerUrl());
  }
}
