package com.mapsrahal.maps.bookmarks;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.mapsrahal.maps.bookmarks.data.BookmarkManager;
import com.mapsrahal.maps.dialog.AlertDialogCallback;
import com.mapsrahal.maps.purchase.BookmarkSubscriptionActivity;
import com.mapsrahal.maps.purchase.PurchaseUtils;
import com.mapsrahal.util.log.Logger;
import com.mapsrahal.util.log.LoggerFactory;
import com.mapsrahal.util.statistics.Statistics;

class InvalidSubscriptionAlertDialogCallback implements AlertDialogCallback
{
  @NonNull
  private final Fragment mFragment;

  InvalidSubscriptionAlertDialogCallback(@NonNull Fragment fragment)
  {
    mFragment = fragment;
  }

  @Override
  public void onAlertDialogPositiveClick(int requestCode, int which)
  {
    BookmarkSubscriptionActivity.startForResult(mFragment, PurchaseUtils.REQ_CODE_PAY_CONTINUE_SUBSCRIPTION,
                                                Statistics.ParamValue.POPUP);
  }

  @Override
  public void onAlertDialogNegativeClick(int requestCode, int which)
  {
    Logger logger = LoggerFactory.INSTANCE.getLogger(LoggerFactory.Type.BILLING);
    String tag = InvalidSubscriptionAlertDialogCallback.class.getSimpleName();
    logger.i(tag, "Delete invalid categories, user didn't continue subscription...");
    BookmarkManager.INSTANCE.deleteInvalidCategories();
  }

  @Override
  public void onAlertDialogCancel(int requestCode)
  {
    // Invalid subs dialog is not cancellable, so do nothing here.
  }
}
