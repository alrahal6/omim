package com.mapsrahal.util;

import androidx.annotation.NonNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils
{
  private DateUtils()
  {
  }

  @NonNull
  public static DateFormat getMediumDateFormatter()
  {
    return DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
  }

  @NonNull
  public static DateFormat getShortDateFormatter()
  {
    return DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
  }

  public static Date timePlusFifteen(Date date) {
    final long FIFTEEN_MINUTE_IN_MILLIS = 60000 * 15;
    long curTimeInMs = date.getTime();
    return new Date(curTimeInMs + FIFTEEN_MINUTE_IN_MILLIS);
    //String pattern = "d MMM-HH:mm";
    //SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
    //return simpleDateFormat.format(afterAddingMins);
  }

  public static Date timeMinusFifteen(Date date) {
    final long FIFTEEN_MINUTE_IN_MILLIS = 60000 * -15;
    long curTimeInMs = date.getTime();
    return new Date(curTimeInMs + FIFTEEN_MINUTE_IN_MILLIS);
    //String pattern = "d MMM-HH:mm";
    //SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
    //return simpleDateFormat.format(afterAddingMins);
  }

  public static String formatDate(Date date) {
    //final long FIFTEEN_MINUTE_IN_MILLIS = 60000 * 15;//millisecs
    long curTimeInMs = date.getTime();
    Date afterAddingMins = new Date(curTimeInMs);
    String pattern = "d MMM-HH:mm";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
    //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    return simpleDateFormat.format(afterAddingMins);
  }

  public static String formatDateStr(Date date) {
    //long curTimeInMs = date.getTime();
    Date afterAddingMins = new Date(date.getTime());
    String pattern = "d MMM-HH:mm";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
    //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    return simpleDateFormat.format(afterAddingMins);
  }

    public static String formatDateStrGmt(Date date) {
        //long curTimeInMs = date.getTime();
        Date afterAddingMins = new Date(date.getTime());
        String pattern = "d MMM-HH:mm";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return simpleDateFormat.format(afterAddingMins);
    }

  public static String formatString(String dateStr) {
    //final long FIFTEEN_MINUTE_IN_MILLIS = 60000 * 15;//millisecs
    //long curTimeInMs = date.getTime();
    //Date afterAddingMins = new Date(curTimeInMs + FIFTEEN_MINUTE_IN_MILLIS);
    String pattern = "d MMM-HH:mm";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    Date date = null;
    try {
      date = simpleDateFormat.parse(dateStr);
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return simpleDateFormat.format(date);
  }

  public static String formatDateCustom(Date date) {
    //long curTimeInMs = date.getTime();
    DateFormat dateFormat = new SimpleDateFormat("d MMM-HH:mm");
    //dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    return dateFormat.format(date);
  }
}
