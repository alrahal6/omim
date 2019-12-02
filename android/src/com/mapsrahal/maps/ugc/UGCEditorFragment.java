package com.mapsrahal.maps.ugc;

import android.os.Bundle;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.mapsrahal.maps.Framework;
import com.mapsrahal.maps.R;
import com.mapsrahal.maps.base.BaseToolbarAuthFragment;
import com.mapsrahal.maps.background.Notifier;
import com.mapsrahal.maps.bookmarks.data.FeatureId;
import com.mapsrahal.maps.metrics.UserActionsLogger;
import com.mapsrahal.maps.widget.ToolbarController;
import com.mapsrahal.util.ConnectionState;
import com.mapsrahal.util.Language;
import com.mapsrahal.util.UiUtils;
import com.mapsrahal.util.Utils;
import com.mapsrahal.util.statistics.Statistics;

import java.util.List;

public class UGCEditorFragment extends BaseToolbarAuthFragment
{
  static final String ARG_FEATURE_ID = "arg_feature_id";
  static final String ARG_TITLE = "arg_title";
  static final String ARG_DEFAULT_RATING = "arg_default_rating";
  static final String ARG_RATING_LIST = "arg_rating_list";
  static final String ARG_CAN_BE_REVIEWED = "arg_can_be_reviewed";
  static final String ARG_LAT = "arg_lat";
  static final String ARG_LON = "arg_lon";
  static final String ARG_ADDRESS = "arg_address";
  @NonNull
  private final UGCRatingAdapter mUGCRatingAdapter = new UGCRatingAdapter();
  @SuppressWarnings("NullableProblems")
  @NonNull
  private EditText mReviewEditText;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState)
  {
    View root = inflater.inflate(R.layout.fragment_ugc_editor, container, false);
    mReviewEditText = root.findViewById(R.id.review);

    RecyclerView rvRatingView = root.findViewById(R.id.ratings);
    rvRatingView.setLayoutManager(new LinearLayoutManager(getContext()));
    rvRatingView.getLayoutManager().setAutoMeasureEnabled(true);
    rvRatingView.setNestedScrollingEnabled(false);
    rvRatingView.setHasFixedSize(false);
    rvRatingView.setAdapter(mUGCRatingAdapter);
    Bundle args = getArguments();
    if (args == null)
      throw new AssertionError("Args must be passed to this fragment!");

    UiUtils.showIf(args.getBoolean(ARG_CAN_BE_REVIEWED), mReviewEditText);

    List<UGC.Rating> ratings = args.getParcelableArrayList(ARG_RATING_LIST);
    if (ratings != null)
      setDefaultRatingValue(args, ratings);
    return root;
  }

  private void setDefaultRatingValue(@NonNull Bundle args, @NonNull List<UGC.Rating> ratings)
  {
    for (UGC.Rating rating : ratings)
      rating.setValue(args.getInt(ARG_DEFAULT_RATING, 0));
    mUGCRatingAdapter.setItems(ratings);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
  {
    super.onViewCreated(view, savedInstanceState);
    getToolbarController().setTitle(getArguments().getString(ARG_TITLE));
    View submitButton = getToolbarController().getToolbar().findViewById(R.id.submit);
    submitButton.setOnClickListener(v -> onSubmitButtonClick());
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
        Statistics.INSTANCE.trackEvent(Statistics.EventName.UGC_REVIEW_CANCEL);
        super.onUpClick();
      }
    };
  }

  @Override
  @CallSuper
  public void onStart()
  {
    super.onStart();

    UGC.setSaveListener((boolean result) ->
    {
      if (!result)
      {
        finishActivity();
        return;
      }

      UserActionsLogger.logUgcSaved();
      Statistics.INSTANCE.trackEvent(Statistics.EventName.UGC_REVIEW_SUCCESS);

      if (!ConnectionState.isConnected())
      {
        if (isAuthorized())
          Utils.toastShortcut(getContext(), R.string.ugc_thanks_message_auth);
        else
          Utils.toastShortcut(getContext(), R.string.ugc_thanks_message_not_auth);

        finishActivity();
        return;
      }
      authorize();
    });
  }

  @Override
  @CallSuper
  public void onStop()
  {
    super.onStop();
    UGC.setSaveListener(null);
  }

  @Override
  public void onAuthorizationFinish(boolean success)
  {
    if (success)
    {
      final Notifier notifier = Notifier.from(getActivity().getApplication());
      notifier.cancelNotification(Notifier.ID_IS_NOT_AUTHENTICATED);
      Utils.toastShortcut(getContext(), R.string.ugc_thanks_message_auth);
    }
    else
    {
      Utils.toastShortcut(getContext(), R.string.ugc_thanks_message_not_auth);
    }

    finishActivity();
  }

  private void finishActivity()
  {
    if (isAdded())
      getActivity().finish();
  }

  @Override
  public void onAuthorizationStart()
  {
    finishActivity();
  }

  @Override
  public void onSocialAuthenticationCancel(@Framework.AuthTokenType int type)
  {
    Statistics.INSTANCE.trackEvent(Statistics.EventName.UGC_AUTH_DECLINED);
    Utils.toastShortcut(getContext(), R.string.ugc_thanks_message_not_auth);
    finishActivity();
  }

  @Override
  public void onSocialAuthenticationError(int type, @Nullable String error)
  {
    Statistics.INSTANCE.trackUGCAuthFailed(type, error);
    Utils.toastShortcut(getContext(), R.string.ugc_thanks_message_not_auth);
    finishActivity();
  }

  private void onSubmitButtonClick()
  {
    List<UGC.Rating> modifiedRatings = mUGCRatingAdapter.getItems();
    UGC.Rating[] ratings = new UGC.Rating[modifiedRatings.size()];
    modifiedRatings.toArray(ratings);
    UGCUpdate update = new UGCUpdate(ratings, mReviewEditText.getText().toString(),
                                     System.currentTimeMillis(), Language.getDefaultLocale(),
                                     Language.getKeyboardLocale());
    FeatureId featureId = getArguments().getParcelable(ARG_FEATURE_ID);
    if (featureId == null)
    {

      throw new AssertionError("Feature ID must be non-null for ugc object! " +
                               "Title = " + getArguments().getString(ARG_TITLE) +
                               "; address = " + getArguments().getString(ARG_ADDRESS) +
                               "; lat = " + getArguments().getDouble(ARG_LAT) +
                               "; lon = " + getArguments().getDouble(ARG_LON));
    }

    UGC.setUGCUpdate(featureId, update);
  }
}
