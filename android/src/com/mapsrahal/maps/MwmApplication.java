package com.mapsrahal.maps;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDex;

import com.appsflyer.AppsFlyerLib;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.mapsrahal.maps.analytics.ExternalLibrariesMediator;
import com.mapsrahal.maps.background.AppBackgroundTracker;
import com.mapsrahal.maps.background.NotificationChannelFactory;
import com.mapsrahal.maps.background.NotificationChannelProvider;
import com.mapsrahal.maps.background.Notifier;
import com.mapsrahal.maps.base.MediaPlayerWrapper;
import com.mapsrahal.maps.bookmarks.data.BookmarkManager;
import com.mapsrahal.maps.downloader.CountryItem;
import com.mapsrahal.maps.downloader.MapManager;
import com.mapsrahal.maps.editor.Editor;
import com.mapsrahal.maps.geofence.GeofenceRegistry;
import com.mapsrahal.maps.geofence.GeofenceRegistryImpl;
import com.mapsrahal.maps.location.LocationHelper;
import com.mapsrahal.maps.location.TrackRecorder;
import com.mapsrahal.maps.maplayer.subway.SubwayManager;
import com.mapsrahal.maps.maplayer.traffic.TrafficManager;
import com.mapsrahal.maps.routing.RoutingController;
import com.mapsrahal.maps.scheduling.ConnectivityJobScheduler;
import com.mapsrahal.maps.scheduling.ConnectivityListener;
import com.mapsrahal.maps.search.SearchEngine;
import com.mapsrahal.maps.sound.TtsPlayer;
import com.mapsrahal.maps.ugc.UGC;
import com.mapsrahal.util.Config;
import com.mapsrahal.util.Counters;
import com.mapsrahal.util.KeyValue;
import com.mapsrahal.util.SharedPropertiesUtils;
import com.mapsrahal.util.StorageUtils;
import com.mapsrahal.util.ThemeSwitcher;
import com.mapsrahal.util.UiUtils;
import com.mapsrahal.util.log.Logger;
import com.mapsrahal.util.log.LoggerFactory;
import com.mapsrahal.util.statistics.Statistics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MwmApplication extends Application implements AppBackgroundTracker.OnTransitionListener
{
  @SuppressWarnings("NullableProblems")
  @NonNull
  private Logger mLogger;
  public final static String TAG = "MwmApplication";
  private static final String FILE_NAME = "Sudan_West.mwm";
  private static final String FILE_NAME_EAST = "Sudan_East.mwm";
  private static MwmApplication sSelf;
  private SharedPreferences mPrefs;
  private AppBackgroundTracker mBackgroundTracker;
  @SuppressWarnings("NullableProblems")
  @NonNull
  private SubwayManager mSubwayManager;
  public static final String CHANNEL_ID = "my_rahal_01";
  public static final String CHANNEL_ID_NOTIFY = "carpoolee_notification";
  public static final String CHANNEL_ID_CALL_CAPTAIN = "online_captatin" ;
  public static final String CHANNEL_NAME = "Captain Request";
  public static final String CHANNEL_NAME_CAPTAIN = "Captain Online";
  public static final String CHANNEL_NAME_USER = "User Message";
  private static final String CHANNEL_DESCRIPTION = "Taxi CarPool App";
  private boolean mFrameworkInitialized;
  private boolean mPlatformInitialized;

  private Handler mMainLoopHandler;
  private final Object mMainQueueToken = new Object();
  @NonNull
  private final AppBackgroundTracker.OnVisibleAppLaunchListener mVisibleAppLaunchListener = new VisibleAppLaunchListener();
  @SuppressWarnings("NullableProblems")
  @NonNull
  private ConnectivityListener mConnectivityListener;
  @NonNull
  private final MapManager.StorageCallback mStorageCallbacks = new StorageCallbackImpl();
  @SuppressWarnings("NullableProblems")
  @NonNull
  private AppBackgroundTracker.OnTransitionListener mBackgroundListener;
  @SuppressWarnings("NullableProblems")
  @NonNull
  private ExternalLibrariesMediator mMediator;
  @SuppressWarnings("NullableProblems")
  @NonNull
  private PurchaseOperationObservable mPurchaseOperationObservable;
  @SuppressWarnings("NullableProblems")
  @NonNull
  private MediaPlayerWrapper mPlayer;
  @SuppressWarnings("NullableProblems")
  @NonNull
  private GeofenceRegistry mGeofenceRegistry;
  private boolean mFirstLaunch;

  @NonNull
  public SubwayManager getSubwayManager()
  {
    return mSubwayManager;
  }

  public MwmApplication()
  {
    super();
    sSelf = this;
  }

  /**
   * Use the {@link #from(Context)} method instead.
   */
  @Deprecated
  public static MwmApplication get()
  {
    return sSelf;
  }

  @NonNull
  public static MwmApplication from(@NonNull Context context)
  {
    return (MwmApplication) context.getApplicationContext();
  }

  /**
   *
   * Use {@link #backgroundTracker(Context)} instead.
   */
  @Deprecated
  public static AppBackgroundTracker backgroundTracker()
  {
    return sSelf.mBackgroundTracker;
  }

  @NonNull
  public static AppBackgroundTracker backgroundTracker(@NonNull Context context)
  {
    return ((MwmApplication) context.getApplicationContext()).getBackgroundTracker();
  }

  /**
   *
   * Use {@link #prefs(Context)} instead.
   */
  @Deprecated
  public synchronized static SharedPreferences prefs()
  {
    if (sSelf.mPrefs == null)
      sSelf.mPrefs = sSelf.getSharedPreferences(sSelf.getString(R.string.pref_file_name), MODE_PRIVATE);

    return sSelf.mPrefs;
  }

  @NonNull
  public static SharedPreferences prefs(@NonNull Context context)
  {
    String prefFile = context.getString(R.string.pref_file_name);
    return context.getSharedPreferences(prefFile, MODE_PRIVATE);
  }

  @Override
  protected void attachBaseContext(Context base)
  {
    super.attachBaseContext(base);
    MultiDex.install(this);
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Override
  public void onCreate()
  {
    super.onCreate();
    //Fabric.with(this, new Crashlytics());
    //FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
    //UiUtils.setupColorStatusBar(sSelf.getApplicationContext(), R.color.bg_statusbar);
    LoggerFactory.INSTANCE.initialize(this);
    mLogger = LoggerFactory.INSTANCE.getLogger(LoggerFactory.Type.MISC);
    mBackgroundListener = new AppBaseTransitionListener(this);
    getLogger().d(TAG, "Application is created");
    mMainLoopHandler = new Handler(getMainLooper());
    mMediator = new ExternalLibrariesMediator(this);
    mMediator.initSensitiveDataToleranceLibraries();
    mMediator.initSensitiveDataStrictLibrariesAsync();
    Statistics.INSTANCE.setMediator(mMediator);

    mPrefs = getSharedPreferences(getString(R.string.pref_file_name), MODE_PRIVATE);
    initNotificationChannels();

    mBackgroundTracker = new AppBackgroundTracker();
    mBackgroundTracker.addListener(mVisibleAppLaunchListener);
    mSubwayManager = new SubwayManager(this);
    mConnectivityListener = new ConnectivityJobScheduler(this);
    mConnectivityListener.listen();

    mPurchaseOperationObservable = new PurchaseOperationObservable();
    mPlayer = new MediaPlayerWrapper(this);
    mGeofenceRegistry = new GeofenceRegistryImpl(this);
  }

  private void initNotificationChannels()
  {
    //NotificationChannelProvider channelProvider = NotificationChannelFactory.createProvider(this);
    //channelProvider.setUGCChannel();
    //channelProvider.setDownloadingChannel();
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
      /*NotificationManager mNotificationManager = getSystemService(NotificationManager.class);
      NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
      mChannel.setDescription(CHANNEL_DESCRIPTION);
      mChannel.enableLights(true);
      mChannel.setLightColor(Color.GREEN);
      mChannel.enableVibration(true);
      //Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
      Uri alarmSound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.cancel_alarm);
      AudioAttributes att = new AudioAttributes.Builder()
              .setUsage(AudioAttributes.USAGE_NOTIFICATION)
              .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
              .build();
      mChannel.setSound(alarmSound,att);
      mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
      mNotificationManager.createNotificationChannel(mChannel);*/

      try {
        Uri ringUri= Settings.System.DEFAULT_RINGTONE_URI;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Call Notifications");
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
           channel.setSound(ringUri,
                    new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setLegacyStreamType(AudioManager.STREAM_RING)
                            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION).build());
        NotificationManager mNotificationManagerCapt = getSystemService(NotificationManager.class);
        channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        mNotificationManagerCapt.createNotificationChannel(channel);
      } catch (Exception e) {
        e.printStackTrace();
      }

      NotificationManager mNotificationManager1 = getSystemService(NotificationManager.class);
      NotificationChannel mChannel1 = new NotificationChannel(CHANNEL_ID_CALL_CAPTAIN, CHANNEL_NAME_CAPTAIN, NotificationManager.IMPORTANCE_NONE);
      mChannel1.setDescription(CHANNEL_DESCRIPTION);
      mChannel1.enableLights(true);
      mChannel1.setLightColor(Color.GREEN);
      mChannel1.enableVibration(true);
      mChannel1.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
      mNotificationManager1.createNotificationChannel(mChannel1);

      //NotificationManager mNotificationManager2 = getSystemService(NotificationManager.class);
      NotificationChannel mChannel2 = new NotificationChannel(CHANNEL_ID_NOTIFY, CHANNEL_NAME_USER, NotificationManager.IMPORTANCE_DEFAULT);
      mChannel2.setDescription(CHANNEL_DESCRIPTION);
      mChannel2.enableLights(true);
      mChannel2.setLightColor(Color.GREEN);
      mChannel2.enableVibration(true);
      mChannel2.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
      mNotificationManager1.createNotificationChannel(mChannel2);
    }
  }

  private boolean isFileExist(String locFile) {
    String filePath = StorageUtils.getSettingsPath()+locFile;
    File file = new File(filePath);
    if(file.exists())
      return true;
    else
      return false;
  }

  private void copyAssets(String locFile) {
    /*AssetManager assetManager = getAssets();
    String[] files = null;
    try {
      files = assetManager.list("");
    } catch (IOException e) {
      Log.e("tag", "Failed to get asset file list.", e);
    }
    for(String filename : files) {*/
    InputStream in = null;
    OutputStream out = null;
    try {
      in = getAssets().open(locFile);

      String outDir = StorageUtils.getSettingsPath();

      File outFile = new File(outDir, locFile);

      out = new FileOutputStream(outFile);
      copyFile(in, out);
      in.close();
      in = null;
      out.flush();
      out.close();
      out = null;
    } catch(IOException e) {
      Log.e("tag", "Failed to copy asset file: " + locFile, e);
    }
    //}
  }

  private void copyFile(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[1024];
    int read;
    while((read = in.read(buffer)) != -1){
      out.write(buffer, 0, read);
    }
  }

  /*private void initNotificationChannels()
  {
    NotificationChannelProvider channelProvider = NotificationChannelFactory.createProvider(this);
    channelProvider.setUGCChannel();
    channelProvider.setDownloadingChannel();
  }*/

  /**
   * Initialize native core of application: platform and framework. Caller must handle returned value
   * and do nothing with native code if initialization is failed.
   *
   * @return boolean - indicator whether native initialization is successful or not.
   */
  public boolean initCore()
  {
    initNativePlatform();
    if (!mPlatformInitialized)
      return false;

    initNativeFramework();
    return mFrameworkInitialized;
  }

  private void initNativePlatform()
  {
    if (mPlatformInitialized)
      return;

    final boolean isInstallationIdFound = mMediator.setInstallationIdToCrashlytics();

    final String settingsPath = StorageUtils.getSettingsPath();
    getLogger().d(TAG, "onCreate(), setting path = " + settingsPath);
    final String filesPath = StorageUtils.getFilesPath(this);
    getLogger().d(TAG, "onCreate(), files path = " + filesPath);
    final String tempPath = StorageUtils.getTempPath(this);
    getLogger().d(TAG, "onCreate(), temp path = " + tempPath);

    // If platform directories are not created it means that native part of app will not be able
    // to work at all. So, we just ignore native part initialization in this case, e.g. when the
    // external storage is damaged or not available (read-only).
    if (!createPlatformDirectories(settingsPath, filesPath, tempPath))
      return;

    // First we need initialize paths and platform to have access to settings and other components.
    nativeInitPlatform(StorageUtils.getApkPath(this), StorageUtils.getStoragePath(settingsPath),
                       filesPath, tempPath, StorageUtils.getObbGooglePath(), BuildConfig.FLAVOR,
                       BuildConfig.BUILD_TYPE, UiUtils.isTablet());

    Config.setStatisticsEnabled(SharedPropertiesUtils.isStatisticsEnabled());

    @SuppressWarnings("unused")
    Statistics s = Statistics.INSTANCE;

    if (!isInstallationIdFound)
      mMediator.setInstallationIdToCrashlytics();

    mBackgroundTracker.addListener(mBackgroundListener);
    TrackRecorder.init();
    Editor.init(this);
    UGC.init(this);
    mPlatformInitialized = true;
    if(!isFileExist(FILE_NAME)) {
      copyAssets(FILE_NAME);
    }
    if(!isFileExist(FILE_NAME_EAST)) {
      copyAssets(FILE_NAME_EAST);
    }
  }

  private boolean createPlatformDirectories(@NonNull String settingsPath, @NonNull String filesPath,
                                            @NonNull String tempPath)
  {
    if (SharedPropertiesUtils.shouldEmulateBadExternalStorage())
      return false;

    return StorageUtils.createDirectory(settingsPath) &&
           StorageUtils.createDirectory(filesPath) &&
           StorageUtils.createDirectory(tempPath);
  }

  private void initNativeFramework()
  {
    if (mFrameworkInitialized)
      return;

    nativeInitFramework();

    MapManager.nativeSubscribe(mStorageCallbacks);

    initNativeStrings();
    SearchEngine.INSTANCE.initialize();
    BookmarkManager.loadBookmarks();
    TtsPlayer.INSTANCE.init(this);
    ThemeSwitcher.restart(false);
    LocationHelper.INSTANCE.initialize();
    RoutingController.get().initialize();
    TrafficManager.INSTANCE.initialize();
    SubwayManager.from(this).initialize();
    mPurchaseOperationObservable.initialize();
    mBackgroundTracker.addListener(this);
    mFrameworkInitialized = true;
  }

  private void initNativeStrings()
  {
    nativeAddLocalization("core_entrance", getString(R.string.core_entrance));
    nativeAddLocalization("core_exit", getString(R.string.core_exit));
    nativeAddLocalization("core_my_places", getString(R.string.core_my_places));
    nativeAddLocalization("core_my_position", getString(R.string.core_my_position));
    nativeAddLocalization("core_placepage_unknown_place", getString(R.string.core_placepage_unknown_place));
    nativeAddLocalization("postal_code", getString(R.string.postal_code));
    nativeAddLocalization("wifi", getString(R.string.wifi));
  }

  public boolean arePlatformAndCoreInitialized()
  {
    return mFrameworkInitialized && mPlatformInitialized;
  }

  @NonNull
  public AppBackgroundTracker getBackgroundTracker()
  {
    return mBackgroundTracker;
  }

  static
  {
    System.loadLibrary("mapsrahal");
  }

  @SuppressWarnings("unused")
  void sendAppsFlyerTags(@NonNull String tag, @NonNull KeyValue[] params)
  {
    HashMap<String, Object> paramsMap = new HashMap<>();
    for (KeyValue p : params)
      paramsMap.put(p.mKey, p.mValue);
    AppsFlyerLib.getInstance().trackEvent(this, tag, paramsMap);
  }

  public void sendPushWooshTags(String tag, String[] values)
  {
    getMediator().getEventLogger().sendTags(tag, values);
  }

  @NonNull
  public ExternalLibrariesMediator getMediator()
  {
    return mMediator;
  }

  @NonNull
  PurchaseOperationObservable getPurchaseOperationObservable()
  {
    return mPurchaseOperationObservable;
  }

  public static void onUpgrade()
  {
    Counters.resetAppSessionCounters();
  }

  @SuppressWarnings("unused")
  void forwardToMainThread(final long taskPointer)
  {
    Message m = Message.obtain(mMainLoopHandler, new Runnable()
    {
      @Override
      public void run()
      {
        nativeProcessTask(taskPointer);
      }
    });
    m.obj = mMainQueueToken;
    mMainLoopHandler.sendMessage(m);
  }

  @NonNull
  public ConnectivityListener getConnectivityListener()
  {
    return mConnectivityListener;
  }

  @NonNull
  public MediaPlayerWrapper getMediaPlayer()
  {
    return mPlayer;
  }

  @NonNull
  public GeofenceRegistry getGeofenceRegistry()
  {
    return mGeofenceRegistry;
  }

  private native void nativeInitPlatform(String apkPath, String storagePath, String privatePath,
                                         String tmpPath, String obbGooglePath, String flavorName,
                                         String buildType, boolean isTablet);
  private static native void nativeInitFramework();
  private static native void nativeProcessTask(long taskPointer);
  private static native void nativeAddLocalization(String name, String value);
  private static native void nativeOnTransit(boolean foreground);

  @NonNull
  public Logger getLogger()
  {
    return mLogger;
  }

  public void setFirstLaunch(boolean isFirstLaunch)
  {
    mFirstLaunch = isFirstLaunch;
  }

  public boolean isFirstLaunch()
  {
    return mFirstLaunch;
  }

  @Override
  public void onTransit(boolean foreground)
  {
    nativeOnTransit(foreground);
  }

  private static class VisibleAppLaunchListener implements AppBackgroundTracker.OnVisibleAppLaunchListener
  {
    @Override
    public void onVisibleAppLaunch()
    {
      Statistics.INSTANCE.trackColdStartupInfo();
    }
  }

  private class StorageCallbackImpl implements MapManager.StorageCallback
  {
    @Override
    public void onStatusChanged(List<MapManager.StorageCallbackData> data)
    {
      Notifier notifier = Notifier.from(MwmApplication.this);
      for (MapManager.StorageCallbackData item : data)
        if (item.isLeafNode && item.newStatus == CountryItem.STATUS_FAILED)
        {
          if (MapManager.nativeIsAutoretryFailed())
          {
            notifier.notifyDownloadFailed(item.countryId, MapManager.nativeGetName(item.countryId));
            MapManager.sendErrorStat(Statistics.EventName.DOWNLOADER_ERROR, MapManager.nativeGetError(item.countryId));
          }

          return;
        }
    }

    @Override
    public void onProgress(String countryId, long localSize, long remoteSize) {}
  }
}
