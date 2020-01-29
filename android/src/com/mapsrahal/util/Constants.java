package com.mapsrahal.util;

import com.mapsrahal.maps.BuildConfig;

public final class Constants
{
  public static final String STORAGE_PATH = "/Android/data/%s/%s/";
  public static final String OBB_PATH = "/Android/obb/%s/";

  public static final int KB = 1024;
  public static final int MB = 1024 * 1024;
  public static final int GB = 1024 * 1024 * 1024;
  public static final String STARTFOREGROUND_ACTION = "startForeground";
  public static final String STOPFOREGROUND_ACTION = "stopForeground";

  static final int CONNECTION_TIMEOUT_MS = 5000;
  static final int READ_TIMEOUT_MS = 30000;

  public static class Url
  {
    public static final String GE0_PREFIX = "ge0://";
    public static final String MAILTO_SCHEME = "mailto:";
    public static final String MAIL_SUBJECT = "?subject=";
    public static final String MAIL_BODY = "&body=";
    public static final String HTTP_GE0_PREFIX = "http://ge0.me/";

    public static final String PLAY_MARKET_HTTPS_APP_PREFIX = "https://play.google.com/store/apps/details?id=";

    public static final String FB_MAPSME_COMMUNITY_HTTP = "http://www.facebook.com/MapsWithMe";
    // Profile id is taken from http://graph.facebook.com/MapsWithMe
    public static final String FB_MAPSME_COMMUNITY_NATIVE = "fb://profile/111923085594432";
    public static final String TWITTER_MAPSME_HTTP = "https://twitter.com/MAPS_ME";

    public static final String WEB_SITE = "http://maps.me";

    public static final String COPYRIGHT = "file:///android_asset/copyright.html";
    public static final String FAQ = "file:///android_asset/faq.html";
    public static final String OPENING_HOURS_MANUAL = "file:///android_asset/opening_hours_how_to_edit.html";

    public static final String OSM_REGISTER = "https://www.openstreetmap.org/user/new";
    public static final String OSM_RECOVER_PASSWORD = "https://www.openstreetmap.org/user/forgot-password";
    public static final String OSM_ABOUT = "https://wiki.openstreetmap.org/wiki/About_OpenStreetMap";


      private Url() {}
  }

  public static class Email
  {
    public static final String FEEDBACK = "android@maps.me";
    public static final String SUPPORT = BuildConfig.SUPPORT_MAIL;
    public static final String RATING = "rating@maps.me";

    private Email() {}
  }

  public static class ActiveProcess {
    public static final int NO_PROCESS = 0;
    public static final int CAPTAIN_ONLINE = 1;
    public static final int PASSENGER_HAVE_ACTIVE_RIDE = 2;
    public static final int CAPTAIN_HAVE_CONFIRMED_LIST = 3;
  }

  public static class Notification {
    public static final int PASSENGER_REQUEST = 1;
    public static final int PASSENGER_ACCEPTED = 2;
    public static final int PASSENGER_REFUSED = 3;
    public static final int PASSENGER_CANCELLED = 4;
    public static final int DRIVER_INVITE = 5;
    public static final int DRIVER_ACCEPTED = 6;
    public static final int DRIVER_REFUSED = 7;
    public static final int DRIVER_CANCELLED = 8;
    public static final int DRIVER_REACHED = 9;
    public static final int PASSENGER_DELAY = 10;
    public static final int DRIVER_DELAY = 11;
    public static final int TRIP_STARTED = 12;
    public static final int TRIP_COMPLETED = 13;
    private Notification() {}
  }

  public static class Package
  {
    public static final String FB_PACKAGE = "com.facebook.katana";
    public static final String MWM_PRO_PACKAGE = "com.mapsrahal.maps.pro";
    public static final String MWM_LITE_PACKAGE = "com.mapsrahal.maps";
    public static final String MWM_SAMSUNG_PACKAGE = "com.mapsrahal.maps.samsung";
    public static final String TWITTER_PACKAGE = "com.twitter.android";

    private Package() {}
  }

  public static class Rating
  {
    public static final float RATING_INCORRECT_VALUE = 0.0f;

    private Rating() {};
  }


  public static final String MWM_DIR_POSTFIX = "/Rahal/";
  public static final String CACHE_DIR = "cache";
  public static final String FILES_DIR = "files";

  private Constants() {}
}
