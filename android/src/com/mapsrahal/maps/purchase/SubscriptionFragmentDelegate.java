package com.mapsrahal.maps.purchase;

import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import com.mapsrahal.maps.Framework;
import com.mapsrahal.maps.R;
import com.mapsrahal.util.Utils;
import com.mapsrahal.util.statistics.Statistics;

abstract class SubscriptionFragmentDelegate
{
  @NonNull
  private final AbstractBookmarkSubscriptionFragment mFragment;

  SubscriptionFragmentDelegate(@NonNull AbstractBookmarkSubscriptionFragment fragment)
  {
    mFragment = fragment;
  }

  abstract void showButtonProgress();

  abstract void hideButtonProgress();

  abstract void onProductDetailsLoading();

  abstract void onPriceSelection();

  @NonNull
  abstract PurchaseUtils.Period getSelectedPeriod();

  void onReset()
  {
    // Do nothing by default.
  }


  @CallSuper
  void onCreateView(@NonNull View root)
  {
    View restoreButton = root.findViewById(R.id.restore_purchase_btn);
    restoreButton.setOnClickListener(v -> openSubscriptionManagementSettings());

    View termsOfUse = root.findViewById(R.id.term_of_use_link);
    termsOfUse.setOnClickListener(v -> openTermsOfUseLink());
    View privacyPolicy = root.findViewById(R.id.privacy_policy_link);
    privacyPolicy.setOnClickListener(v -> openPrivacyPolicyLink());
  }

  void onDestroyView()
  {
    // Do nothing by default.
  }

  private void openSubscriptionManagementSettings()
  {
    Utils.openUrl(mFragment.requireContext(), "https://play.google.com/store/account/subscriptions");
    Statistics.INSTANCE.trackPurchaseEvent(Statistics.EventName.INAPP_PURCHASE_PREVIEW_RESTORE,
                                           mFragment.getSubscriptionType().getServerId());
  }

  private void openTermsOfUseLink()
  {
    Utils.openUrl(mFragment.requireContext(), Framework.nativeGetTermsOfUseLink());
  }

  private void openPrivacyPolicyLink()
  {
    Utils.openUrl(mFragment.requireContext(), Framework.nativeGetPrivacyPolicyLink());
  }

  @NonNull
  AbstractBookmarkSubscriptionFragment getFragment()
  {
    return mFragment;
  }
}
