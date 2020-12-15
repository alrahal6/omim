package com.mapsrahal.maps.model;

import java.util.Date;

public class RepeatRegular {

    private double tripId;
    private Date newTime;
    private double newPrice;
    private int dropDownId;
    private String dropDownVal;
    private int newSeats;
    private int sun;
    private int mon;
    private int tue;
    private int wed;
    private int thu;
    private int fri;
    private int sat;

    public RepeatRegular(double tripId, Date newTime, double newPrice, int dropDownId, String dropDownVal,
                         int newSeats, int sun, int mon, int tue, int wed, int thu, int fri, int sat) {
        this.tripId = tripId;
        this.newTime = newTime;
        this.newPrice = newPrice;
        this.dropDownId = dropDownId;
        this.dropDownVal = dropDownVal;
        this.newSeats = newSeats;
        this.sun = sun;
        this.mon = mon;
        this.tue = tue;
        this.wed = wed;
        this.thu = thu;
        this.fri = fri;
        this.sat = sat;
    }

    public double getTripId() {
        return tripId;
    }

    public void setTripId(double tripId) {
        this.tripId = tripId;
    }

    public Date getNewTime() {
        return newTime;
    }

    public void setNewTime(Date newTime) {
        this.newTime = newTime;
    }

    public double getNewPrice() {
        return newPrice;
    }

    public void setNewPrice(double newPrice) {
        this.newPrice = newPrice;
    }

    public int getDropDownId() {
        return dropDownId;
    }

    public void setDropDownId(int dropDownId) {
        this.dropDownId = dropDownId;
    }

    public String getDropDownVal() {
        return dropDownVal;
    }

    public void setDropDownVal(String dropDownVal) {
        this.dropDownVal = dropDownVal;
    }

    public int getNewSeats() {
        return newSeats;
    }

    public void setNewSeats(int newSeats) {
        this.newSeats = newSeats;
    }

    public int getSun() {
        return sun;
    }

    public void setSun(int sun) {
        this.sun = sun;
    }

    public int getMon() {
        return mon;
    }

    public void setMon(int mon) {
        this.mon = mon;
    }

    public int getTue() {
        return tue;
    }

    public void setTue(int tue) {
        this.tue = tue;
    }

    public int getWed() {
        return wed;
    }

    public void setWed(int wed) {
        this.wed = wed;
    }

    public int getThu() {
        return thu;
    }

    public void setThu(int thu) {
        this.thu = thu;
    }

    public int getFri() {
        return fri;
    }

    public void setFri(int fri) {
        this.fri = fri;
    }

    public int getSat() {
        return sat;
    }

    public void setSat(int sat) {
        this.sat = sat;
    }
}
