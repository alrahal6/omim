package com.mapsrahal.maps.model;

import android.util.Log;

import androidx.annotation.NonNull;

public class MatchingItem implements Comparable<MatchingItem> {
    private int id,userId;
    private String mText1;
    private String mText2;
    private String mTripTime;
    private String mTotDistTxt;
    private String mAmount;
    private String mExtraDistance;
    private String mPhone;

    public double getmYourDistance() {
        return mYourDistance;
    }

    private double mYourDistance;
    private Double mTripDistance,mTotTripDistance;

    public String getmAmount() {
        return mAmount;
    }

    public String getmExtraDistance() {
        return mExtraDistance;
    }

    public MatchingItem(int id,int userId, String mText1, String mText2, Double mTripDistance, String mTripTime,
                        Double mTotTripDistance, String mTotDistTxt,String mAmount, String mExtraDistance,double mYourDistance,String mPhone) {
        this.id = id;
        this.userId = userId;
        this.mText1 = mText1;
        this.mText2 = mText2;
        this.mTripDistance = mTripDistance;
        this.mTripTime = mTripTime;
        this.mTotTripDistance = mTotTripDistance;
        this.mTotDistTxt = mTotDistTxt;
        this.mAmount = mAmount;
        this.mExtraDistance = mExtraDistance;
        this.mYourDistance = mYourDistance;
        this.mPhone = mPhone;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getmTotDistTxt() {
        return mTotDistTxt;
    }

    public Double getmTotTripDistance() {
        return mTotTripDistance;
    }

    public Double getmTripDistance() {
        return mTripDistance;
    }

    public String getmTripTime() {
        return mTripTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getmText1() {
        return mText1;
    }

    public void setmText1(String mText1) {
        this.mText1 = mText1;
    }

    public String getmText2() {
        return mText2;
    }

    public void setmText2(String mText2) {
        this.mText2 = mText2;
    }

    public String getmPhone() {
        return mPhone;
    }

    public void setmPhone(String mPhone) {
        this.mPhone = mPhone;
    }

    @Override
    public int compareTo(@NonNull MatchingItem matchingItem) {
        //Log.d("MatchingItem",mTotTripDistance +"- "+matchingItem.mTotTripDistance);
        if (getmTotTripDistance() == null || matchingItem.getmTotTripDistance() == null) {
            return 0;
        }
        return getmTotTripDistance().compareTo(matchingItem.getmTotTripDistance());
    }
}
