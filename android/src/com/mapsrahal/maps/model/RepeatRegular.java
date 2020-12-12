package com.mapsrahal.maps.model;

import java.util.Date;

public class RepeatRegular {

    private double tripId;
    private Date newTime;
    private double newPrice;
    private int dropDownId;
    private String dropDownVal;
    private int newSeats;
    private boolean sun;
    private boolean mon;
    private boolean tue;
    private boolean wed;
    private boolean thu;
    private boolean fri;
    private boolean sat;

    public RepeatRegular(double tripId, Date newTime, double newPrice, int dropDownId, String dropDownVal, int newSeats, boolean sun, boolean mon, boolean tue, boolean wed, boolean thu, boolean fri, boolean sat) {
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

    public boolean isSun() {
        return sun;
    }

    public void setSun(boolean sun) {
        this.sun = sun;
    }

    public boolean isMon() {
        return mon;
    }

    public void setMon(boolean mon) {
        this.mon = mon;
    }

    public boolean isTue() {
        return tue;
    }

    public void setTue(boolean tue) {
        this.tue = tue;
    }

    public boolean isWed() {
        return wed;
    }

    public void setWed(boolean wed) {
        this.wed = wed;
    }

    public boolean isThu() {
        return thu;
    }

    public void setThu(boolean thu) {
        this.thu = thu;
    }

    public boolean isFri() {
        return fri;
    }

    public void setFri(boolean fri) {
        this.fri = fri;
    }

    public boolean isSat() {
        return sat;
    }

    public void setSat(boolean sat) {
        this.sat = sat;
    }
}
