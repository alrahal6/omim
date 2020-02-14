package com.mapsrahal.maps.model;


public class CallLog {

    private int userId;
    private String phone;
    private int tripId;

    public CallLog(int userId, String phone, int tripId) {
        this.userId = userId;
        this.phone = phone;
        this.tripId = tripId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }
}