package com.mapsrahal.maps.auth;

import androidx.annotation.IdRes;
import android.view.View;
import android.webkit.WebView;

import com.mapsrahal.maps.R;
import com.mapsrahal.maps.base.BaseMwmFragment;
import com.mapsrahal.util.Utils;

public class BaseWebViewMwmFragment extends BaseMwmFragment
{
  @Override
  public boolean onBackPressed()
  {
    View root;
    WebView webView = null;
    boolean goBackAllowed = (root = getView()) != null
                && (webView = Utils.castTo(root.findViewById(getWebViewResId()))) != null
                && webView.canGoBack();
    if (goBackAllowed)
      webView.goBack();
    return goBackAllowed;
  }

  @IdRes
  protected int getWebViewResId()
  {
    return R.id.webview;
  }
}
