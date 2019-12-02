package com.mapsrahal.maps.discovery;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mapsrahal.util.NetworkPolicy;
import com.mapsrahal.util.concurrency.UiThread;

final class Locals
{
  public static final Locals INSTANCE = new Locals();

  @Nullable
  private LocalsListener mListener;

  private Locals() {}

  public void setLocalsListener(@Nullable LocalsListener listener)
  {
    mListener = listener;
  }

  public native void nativeRequestLocals(@NonNull NetworkPolicy policy,
                                         double lat, double lon);

  // Called from JNI.
  @MainThread
  void onLocalsReceived(@NonNull LocalExpert[] experts)
  {
    if (!UiThread.isUiThread())
      throw new AssertionError("Must be called from UI thread!");

    if (mListener != null)
      mListener.onLocalsReceived(experts);
  }

  // Called from JNI.
  @MainThread
  void onLocalsErrorReceived(@NonNull LocalsError error)
  {
    if (!UiThread.isUiThread())
      throw new AssertionError("Must be called from UI thread!");

    if (mListener != null)
      mListener.onLocalsErrorReceived(error);
  }

  public interface LocalsListener
  {
    void onLocalsReceived(@NonNull LocalExpert[] experts);
    void onLocalsErrorReceived(@NonNull LocalsError error);
  }
}
