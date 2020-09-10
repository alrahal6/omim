package com.mapsrahal.maps.model;

public class MyTripHistory {

    private int fUserId;
    private int tUserId;
    private int mFlag;
    private double tripId;
    private double distance;
    private String price;
    private String mTripTime;
    private String phone;
    private String name;
    private String fAddress;
    private String tAddress;
    private String note;
    private double fLat;
    private double fLng;
    private double tLat;
    private double tLng;

    public MyTripHistory(int fUserId, int tUserId, int mFlag, double tripId, double distance, String price, String mTripTime, String phone, String name, String fAddress, String tAddress, String note, double fLat, double fLng, double tLat, double tLng) {
        this.fUserId = fUserId;
        this.tUserId = tUserId;
        this.mFlag = mFlag;
        this.tripId = tripId;
        this.distance = distance;
        this.price = price;
        this.mTripTime = mTripTime;
        this.phone = phone;
        this.name = name;
        this.fAddress = fAddress;
        this.tAddress = tAddress;
        this.note = note;
        this.fLat = fLat;
        this.fLng = fLng;
        this.tLat = tLat;
        this.tLng = tLng;
    }

    public int getfUserId() {
        return fUserId;
    }

    public void setfUserId(int fUserId) {
        this.fUserId = fUserId;
    }

    public int gettUserId() {
        return tUserId;
    }

    public void settUserId(int tUserId) {
        this.tUserId = tUserId;
    }

    public int getmFlag() {
        return mFlag;
    }

    public void setmFlag(int mFlag) {
        this.mFlag = mFlag;
    }

    public double getTripId() {
        return tripId;
    }

    public void setTripId(double tripId) {
        this.tripId = tripId;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getmTripTime() {
        return mTripTime;
    }

    public void setmTripTime(String mTripTime) {
        this.mTripTime = mTripTime;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getfAddress() {
        return fAddress;
    }

    public void setfAddress(String fAddress) {
        this.fAddress = fAddress;
    }

    public String gettAddress() {
        return tAddress;
    }

    public void settAddress(String tAddress) {
        this.tAddress = tAddress;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public double getfLat() {
        return fLat;
    }

    public void setfLat(double fLat) {
        this.fLat = fLat;
    }

    public double getfLng() {
        return fLng;
    }

    public void setfLng(double fLng) {
        this.fLng = fLng;
    }

    public double gettLat() {
        return tLat;
    }

    public void settLat(double tLat) {
        this.tLat = tLat;
    }

    public double gettLng() {
        return tLng;
    }

    public void settLng(double tLng) {
        this.tLng = tLng;
    }
}
