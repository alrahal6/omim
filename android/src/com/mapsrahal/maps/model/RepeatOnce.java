package com.mapsrahal.maps.model;

import java.util.Date;

public class RepeatOnce {

    private int tripId;
    private Date newTime;
    private int newSeats;
    private int dropDownId;
    private String dropDownVal;
    private double newPrice;
    private Date entryTime;

    public RepeatOnce(int tripId, Date newTime, int newSeats, int dropDownId, String dropDownVal, double newPrice, Date entryTime) {
        this.tripId = tripId;
        this.newTime = newTime;
        this.newSeats = newSeats;
        this.dropDownId = dropDownId;
        this.dropDownVal = dropDownVal;
        this.newPrice = newPrice;
        this.entryTime = entryTime;
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

    public int getNewSeats() {
        return newSeats;
    }

    public void setNewSeats(int newSeats) {
        this.newSeats = newSeats;
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

    public double getNewPrice() {
        return newPrice;
    }

    public void setNewPrice(double newPrice) {
        this.newPrice = newPrice;
    }

    public Date getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(Date entryTime) {
        this.entryTime = entryTime;
    }
}
