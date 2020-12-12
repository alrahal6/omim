package com.mapsrahal.maps;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.mapsrahal.maps.model.UserMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MySharedPreference {
    private static final String SHARED_PREF_NAME = "mypref";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_HAVE_TOKEN = "haveToken";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_CAPTAIN_ONLINE = "captainOnline";
    private static final String KEY_BINDED = "serviceBinded";
    private static final String KEY_MSG_TIME = "msgTime";
    private static final String KEY_SELECTOR = "keySelector";
    private static final String KEY_CAPT_RES = "keyCaptRes";
    private static final String KEY_USER_ID = "userid";
    private static final String KEY_USER_TYPE = "usertype";
    private static final String KEY_MESSAGE = "keyMessage";
    private static final String KEY_NOTIFICATION = "keyNotification";
    private static final String KEY_TRIP_ID = "tripId";
    private static final String KEY_DISTANCE = "distance";
    private static final String KEY_START_TIME = "tripStartTime";
    private static final String KEY_FROM_LAT = "frmLat";
    private static final String KEY_FROM_LNG = "frmLng";
    private static final String KEY_TO_LAT = "toLat";
    private static final String KEY_TO_LNG = "toLng";
    private static final String KEY_TRIP_DISTANCE = "tripDistance";
    private static final String KEY_FROM_ADDRESS = "frmAddress";
    private static final String KEY_TO_ADDRESS = "toAddress";
    private static final String KEY_SEARCHED_TIME = "searchedTime";
    private static final String KEY_IS_NOTIFY = "isRequired";

    private static final String KEY_NOTIFY = "isNotify";
    private static final String KEY_NOTIFY_TITLE = "notifyTitle";
    private static final String KEY_NOTIFY_BODY = "notifyBody";

    private static final String KEY_ACTIVE_PROCESS_ID = "activeprocessid";
    private static final String KEY_START_STATUS_ID = "startstatusid";
    private static final String CAPTAIN_TICKET_DTLS = "captainticketdtls";

    private static MySharedPreference mInstance;
    private static Context mCtx;

    private MySharedPreference(Context context) {
        //mCtx = context.getApplicationContext();
        mCtx = MwmApplication.get().getApplicationContext();
    }

    public static synchronized MySharedPreference getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MySharedPreference(context);
        }
        return mInstance;
    }

    public void userTripInfo(double fromLat, double fromLng, double toLat, double toLng,
                             String distance, String sourceAddress, String destAddress, Date startTime) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(KEY_FROM_LAT, Double.doubleToRawLongBits(fromLat));
        editor.putLong(KEY_FROM_LNG, Double.doubleToRawLongBits(fromLng));
        editor.putLong(KEY_TO_LAT, Double.doubleToRawLongBits(toLat));
        editor.putLong(KEY_TO_LNG, Double.doubleToRawLongBits(toLng));
        editor.putString(KEY_TRIP_DISTANCE, distance);
        editor.putString(KEY_FROM_ADDRESS, sourceAddress);
        editor.putString(KEY_TO_ADDRESS, destAddress);
        editor.putLong(KEY_START_TIME,startTime.getTime());
        editor.apply();
    }

    public double getFrmLat() {
        return Double.longBitsToDouble(getSharedPreference().getLong(KEY_FROM_LAT, 0));
    }

    public double getFrmLng() {
        return Double.longBitsToDouble(getSharedPreference().getLong(KEY_FROM_LNG, 0));
    }

    public double getToLat() {
        return Double.longBitsToDouble(getSharedPreference().getLong(KEY_TO_LAT, 0));
    }

    public double getToLng() {
        return Double.longBitsToDouble(getSharedPreference().getLong(KEY_TO_LNG, 0));
    }

    public String getTripDistance() {
        return getSharedPreference().getString(KEY_TRIP_DISTANCE, "na");
    }

    public String getFrmAddress() {
        return getSharedPreference().getString(KEY_FROM_ADDRESS, "na");
    }

    public String getToAddress() {
        return getSharedPreference().getString(KEY_TO_ADDRESS, "na");
    }

    private SharedPreferences getSharedPreference() {
        return mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    public void userLogin(int id, String phone,String name, int type) {
        //SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = getSharedPreference().edit();
        editor.putInt(KEY_USER_ID, id);
        editor.putString(KEY_PHONE, phone);
        editor.putString(KEY_USER_NAME,name);
        editor.putInt(KEY_USER_TYPE, type);
        editor.apply();
    }

    public void userMessage(String message) {
        //SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = getSharedPreference().edit();
        editor.putString(KEY_MESSAGE, message);
        editor.apply();
    }

    public void userNotification(String message) {
        //SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = getSharedPreference().edit();
        editor.putString(KEY_NOTIFICATION, message);
        editor.apply();
    }

    public String getUserNotification() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_NOTIFICATION, null);
    }

    public void setCaptainOnline(boolean online) {
        //SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = getSharedPreference().edit();
        editor.putBoolean(KEY_CAPTAIN_ONLINE, online);
        editor.apply();
    }

    public void setBind(boolean binded) {
        //SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = getSharedPreference().edit();
        editor.putBoolean(KEY_BINDED, binded);
        editor.apply();
    }

    public void setMsgRcvdTime(long msgTime) {
        //SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = getSharedPreference().edit();
        editor.putLong(KEY_MSG_TIME, msgTime);
        editor.apply();
    }

    public void setSelectorId(int id) {
        //SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = getSharedPreference().edit();
        editor.putInt(KEY_SELECTOR, id);
        editor.apply();
    }

    public int getSelectorId() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(KEY_SELECTOR, 0);
    }

    public void setCaptRespId(int id) {
        //SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = getSharedPreference().edit();
        editor.putInt(KEY_CAPT_RES, id);
        editor.apply();
    }

    public int getCaptRespId() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(KEY_CAPT_RES, 0);
    }

    public long getMsgRcvdTime() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getLong(KEY_MSG_TIME, 0L);
    }

    public boolean isBinded() {
        return getSharedPreference().getBoolean(KEY_BINDED, false);
    }

    public void setNotify(boolean isRequired) {
        SharedPreferences.Editor editor = getSharedPreference().edit();
        editor.putBoolean(KEY_IS_NOTIFY, isRequired);
        editor.apply();
    }

    public boolean getNotify() {
        return getSharedPreference().getBoolean(KEY_IS_NOTIFY, false);
    }

    public String getUserMessage() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_MESSAGE, null);
    }

    public boolean isLoggedIn() {
        //SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return getSharedPreference().getString(KEY_PHONE, null) != null;
    }

    // todo return real value
    public boolean isCaptain() {
        return true;
    }

    public boolean isCaptainOnline() {
        return getSharedPreference().getBoolean(KEY_CAPTAIN_ONLINE, false);
    }

    public void logout() {
        //SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = getSharedPreference().edit();
        editor.clear();
        editor.apply();
    }

    public String getPhoneNumber() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_PHONE, null);
    }

    public int getUserId() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(KEY_USER_ID, 0);
    }

    public void recordTrip(String tripId,long startTime,float distance) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TRIP_ID,tripId);
        editor.putLong(KEY_START_TIME, startTime);
        editor.putFloat(KEY_DISTANCE, distance);
        editor.apply();
    }

    /*
    Editor putDouble(final Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }
    double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
    return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }
     */

    public void finishTrip() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TRIP_ID,null);
        editor.apply();
    }

    public String getTripId() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_TRIP_ID, null);
    }

    public long getStartTime() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getLong(KEY_START_TIME, 0);
    }

    public float getTravelledDistance() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getFloat(KEY_DISTANCE, 0);
    }

    public String getUserName() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_NAME, null);
    }

    public void newTokenAvailable() {
        //SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = getSharedPreference().edit();
        editor.putBoolean(KEY_HAVE_TOKEN,true);
        editor.apply();
    }

    public boolean isHaveNewToken() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_HAVE_TOKEN, false);
    }

    public void clearNewToken() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_HAVE_TOKEN,false);
        editor.apply();
    }

    public void clearStartStatus() {
        SharedPreferences.Editor editor = getSharedPreference().edit();
        editor.putBoolean(KEY_START_STATUS_ID, false);
        editor.apply();
    }

    public void addStartStatus() {
        SharedPreferences.Editor editor = getSharedPreference().edit();
        editor.putBoolean(KEY_START_STATUS_ID, true);
        editor.apply();
    }

    public boolean getStartStatus() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_START_STATUS_ID, false);
    }

    public void clearActiveProcess() {
        SharedPreferences.Editor editor = getSharedPreference().edit();
        editor.putInt(KEY_ACTIVE_PROCESS_ID, 0);
        editor.apply();
    }

    public void addActiveProcess(int activeProcessId) {
        SharedPreferences.Editor editor = getSharedPreference().edit();
        editor.putInt(KEY_ACTIVE_PROCESS_ID, activeProcessId);
        editor.apply();
    }

    public int getActiveProcess() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(KEY_ACTIVE_PROCESS_ID, 0);
    }

    public void addToSearched(long searchedTime) {
        SharedPreferences.Editor editor = getSharedPreference().edit();
        //editor.putInt(KEY_ACTIVE_PROCESS_ID, activeProcessId);
        editor.putLong(KEY_SEARCHED_TIME, searchedTime);
        editor.apply();
    }

    public long getIsSearched() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getLong(KEY_SEARCHED_TIME, 0);
    }

    public void putNotification(String title,String body) {
        SharedPreferences.Editor editor = getSharedPreference().edit();
        //editor.putInt(KEY_ACTIVE_PROCESS_ID, activeProcessId);
        editor.putString(KEY_NOTIFY_TITLE, title);
        editor.putString(KEY_NOTIFY_BODY, body);
        //editor.apply();
        editor.commit();
    }

    public String getTitle() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_NOTIFY_TITLE, "");
    }

    public String getBody() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_NOTIFY_BODY, "");
    }

    public void addToNotify(boolean isNotify) {
        SharedPreferences.Editor editor = getSharedPreference().edit();
        //editor.putInt(KEY_ACTIVE_PROCESS_ID, activeProcessId);
        editor.putBoolean(KEY_NOTIFY, isNotify);
        editor.apply();
    }

    public boolean getIsNotified() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_NOTIFY, false);
    }

    public void putListConfirmed(String key, List<UserMessage> objArray){
    	checkForNullKey(key);
    	Gson gson = new Gson();
    	ArrayList<String> objStrings = new ArrayList<>();
    	for(Object obj : objArray){
    		objStrings.add(gson.toJson(obj));
    	}
    	putListString(key, objStrings);
    }

    public void putListString(String key, ArrayList<String> stringList) {
        checkForNullKey(key);
        String[] myStringList = stringList.toArray(new String[stringList.size()]);
        getSharedPreference().edit().putString(key, TextUtils.join("‚‗‚", myStringList)).apply();
    }

    public void checkForNullKey(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
    }

    public ArrayList<UserMessage> getListConfirmed(String key) {
    	Gson gson = new Gson();

    	ArrayList<String> objStrings = getListString(key);
    	ArrayList<UserMessage> objects = new ArrayList<>();

    	for(String jObjString : objStrings) {
            UserMessage value  = gson.fromJson(jObjString,  UserMessage.class);
    		objects.add(value);
    	}
    	return objects;
    }

    public ArrayList<String> getListString(String key) {
        return new ArrayList<>(Arrays.asList(TextUtils.split(getSharedPreference().getString(key, ""), "‚‗‚")));
    }

    public void putCaptainTicketDtls(UserMessage userMessage) {
        Gson gson = new Gson();
        String json = gson.toJson(userMessage);
        getSharedPreference().edit().putString(CAPTAIN_TICKET_DTLS, json).apply();
    }

    public UserMessage getCaptainTicketDtls() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(CAPTAIN_TICKET_DTLS, "");
        return gson.fromJson(json, UserMessage.class);
    }

}