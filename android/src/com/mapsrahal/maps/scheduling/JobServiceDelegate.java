package com.mapsrahal.maps.scheduling;

import android.app.Application;
import androidx.annotation.NonNull;

import com.mapsrahal.maps.background.NotificationService;
import com.mapsrahal.util.ConnectionState;

class JobServiceDelegate
{
  @NonNull
  private final Application mApp;

  JobServiceDelegate(@NonNull Application app)
  {
    mApp = app;
  }

  boolean onStartJob()
  {
    ConnectionState.Type type = ConnectionState.requestCurrentType(mApp);
    if (type == ConnectionState.Type.WIFI)
      NotificationService.startOnConnectivityChanged(mApp);

    retryJob();
    return true;
  }

  private void retryJob()
  {
    ConnectivityJobScheduler.from(mApp).listen();
  }

  boolean onStopJob()
  {
    return false;
  }
}
