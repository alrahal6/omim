package com.mapsrahal.maps.model;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Query;

import java.util.List;

@Entity(tableName = "match_table")
public class MatchingItem  {
    @PrimaryKey
    public int id;
    public int userId;
    public String mText1;
    public String mText2;
    public String mTripTime;
    public String mTotDistTxt;
    public String mAmount;
    public String mExtraDistance;
    public String mPhone;
    public String extraDistance;
    public double mMyTripDistance;
    public double fLat;
    public double fLng;
    public double tLat;
    public double tLng;

    public MatchingItem(int id, int userId, String mText1, String mText2, String mTripTime,
                        String mTotDistTxt, String mAmount, String mExtraDistance, String mPhone,
                        String extraDistance, double mMyTripDistance,
                        double fLat,double fLng,double tLat,double tLng) {
        this.id = id;
        this.userId = userId;
        this.mText1 = mText1;
        this.mText2 = mText2;
        this.mTripTime = mTripTime;
        this.mTotDistTxt = mTotDistTxt;
        this.mAmount = mAmount;
        this.mExtraDistance = mExtraDistance;
        this.mPhone = mPhone;
        this.extraDistance = extraDistance;
        this.mMyTripDistance = mMyTripDistance;
        this.fLat = fLat;
        this.fLng = fLng;
        this.tLat = tLat;
        this.tLng = tLng;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getmText1() {
        return mText1;
    }

    public String getmText2() {
        return mText2;
    }

    public String getmTripTime() {
        return mTripTime;
    }

    public String getmTotDistTxt() {
        return mTotDistTxt;
    }

    public String getmAmount() {
        return mAmount;
    }

    public String getmExtraDistance() {
        return mExtraDistance;
    }

    public String getmPhone() {
        return mPhone;
    }

    public double getfLat() {
        return fLat;
    }

    public double getfLng() {
        return fLng;
    }

    public double gettLat() {
        return tLat;
    }

    public double gettLng() {
        return tLng;
    }
}
