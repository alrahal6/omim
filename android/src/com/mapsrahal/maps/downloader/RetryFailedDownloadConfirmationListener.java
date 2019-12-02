package com.mapsrahal.maps.downloader;

import android.app.Application;
import androidx.annotation.NonNull;

import com.mapsrahal.maps.background.Notifier;

public class RetryFailedDownloadConfirmationListener implements Runnable
{
  @NonNull
  private final Application mApplication;

  RetryFailedDownloadConfirmationListener(@NonNull Application application)
  {
    mApplication = application;
  }

  @Override
  public void run()
  {
    final Notifier notifier = Notifier.from(mApplication);
    notifier.cancelNotification(Notifier.ID_DOWNLOAD_FAILED);
  }
}
