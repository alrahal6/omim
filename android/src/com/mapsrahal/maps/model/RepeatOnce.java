package com.mapsrahal.maps.model;

import java.util.Date;

public class RepeatOnce {

    private int tripId;
    private Date newTime;
    private double newPrice;
    private int dropDownId;
    private String dropDownVal;
    private int newSeats;

    public RepeatOnce(int tripId, Date newTime, double newPrice, int dropDownId, String dropDownVal, int newSeats) {
        this.tripId = tripId;
        this.newTime = newTime;
        this.newPrice = newPrice;
        this.dropDownId = dropDownId;
        this.dropDownVal = dropDownVal;
        this.newSeats = newSeats;
    }

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
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
}
