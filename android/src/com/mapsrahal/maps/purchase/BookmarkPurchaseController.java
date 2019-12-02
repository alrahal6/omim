package com.mapsrahal.maps.purchase;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;
import com.mapsrahal.maps.PrivateVariables;
import com.mapsrahal.util.ConnectionState;
import com.mapsrahal.util.log.Logger;
import com.mapsrahal.util.log.LoggerFactory;
import com.mapsrahal.util.statistics.Statistics;

import java.util.List;

class BookmarkPurchaseController extends AbstractPurchaseController<ValidationCallback,
    PlayStoreBillingCallback, PurchaseCallback>
{
  private static final Logger LOGGER = LoggerFactory.INSTANCE.getLogger(LoggerFactory.Type.BILLING);
  private static final String TAG = AbstractPurchaseController.class.getSimpleName();
  @NonNull
  private final PlayStoreBillingCallback mBillingCallback = new PlayStoreBillingCallbackImpl();
  @NonNull
  private final ValidationCallback mValidationCallback;
  @Nullable
  private final String mServerId;

  BookmarkPurchaseController(@NonNull PurchaseValidator<ValidationCallback> validator,
                             @NonNull BillingManager<PlayStoreBillingCallback> billingManager,
                             @Nullable String productId, @Nullable String serverId)
  {
    super(validator, billingManager, productId);
    mServerId = serverId;
    mValidationCallback = new ValidationCallbackImpl(mServerId);
  }

  @Override
  void onInitialize(@NonNull Activity activity)
  {
    getValidator().addCallback(mValidationCallback);
    getBillingManager().addCallback(mBillingCallback);
  }

  @Override
  void onDestroy()
  {
    getValidator().removeCallback();
    getBillingManager().removeCallback(mBillingCallback);
  }

  private class ValidationCallbackImpl extends AbstractBookmarkValidationCallback
  {

    ValidationCallbackImpl(@Nullable String serverId)
    {
      super(serverId);
    }

    @Override
    void onValidationError(@NonNull ValidationStatus status)
    {
      if (getUiCallback() != null)
        getUiCallback().onValidationFinish(false);
    }

    @Override
    void consumePurchase(@NonNull String purchaseData)
    {
      LOGGER.i(TAG, "Bookmark purchase consuming...");
      getBillingManager().consumePurchase(PurchaseUtils.parseToken(purchaseData));
    }
  }

  private class PlayStoreBillingCallbackImpl extends AbstractPlayStoreBillingCallback
  {
    @Override
    public void onPurchaseFailure(int error)
    {
      super.onPurchaseFailure(error);
      //noinspection ConstantConditions
      Statistics.INSTANCE.trackPurchaseStoreError(mServerId, error);
    }

    @Override
    public void onProductDetailsLoaded(@NonNull List<SkuDetails> details)
    {
      if (getUiCallback() != null)
        getUiCallback().onProductDetailsLoaded(details);
    }

    @Override
    void validate(@NonNull String purchaseData)
    {
      getValidator().validate(mServerId, PrivateVariables.bookmarksVendor(), purchaseData);
    }

    @Override
    public void onPurchasesLoaded(@NonNull List<Purchase> purchases)
    {
      if (!ConnectionState.isWifiConnected())
      {
        LOGGER.i(TAG, "Validation postponed, connection not WI-FI.");
        return;
      }

      if (purchases.isEmpty())
      {
        LOGGER.i(TAG, "Non-consumed bookmark purchases not found");
        return;
      }

      for (Purchase target: purchases)
      {
        LOGGER.i(TAG, "Validating existing purchase data for '" + target.getSku()
                      + " " + target.getOrderId() + "'...");
        getValidator().validate(mServerId, PrivateVariables.bookmarksVendor(), target.getOriginalJson());
      }
    }

    @Override
    public void onConsumptionSuccess()
    {
      LOGGER.i(TAG, "Bookmark purchase consumed");
      if (getUiCallback() != null)
        getUiCallback().onValidationFinish(true);
    }

    @Override
    public void onConsumptionFailure()
    {
      LOGGER.w(TAG, "Bookmark purchase not consumed");
      if (getUiCallback() != null)
        getUiCallback().onValidationFinish(false);
    }
  }
}
