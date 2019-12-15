package com.mapsrahal.maps;

public class UserTripInfo {

    private final int userId;
    private double lat;
    private double lng;
    private String pickupAddress;
    private double destLat;
    private double destLng;
    private String destAddress;
    private int myFlag;
    private int driverId;
    private final String phone;
    private final String customerName;
    private double distance;
    private double duration;
    private double price;
    private String car;
    private double tripId;
    private float base;
    private float km;
    private float mins;
    private int minDis;

    public UserTripInfo(int userId, String phone, String customerName) {
        this.userId = userId;
        this.customerName = customerName;
        this.lat = 0;
        this.lng = 0;
        this.pickupAddress = "";
        this.destLat = 0;
        this.destLng = 0;
        this.destAddress = "";
        this.myFlag = 4;
        this.driverId = driverId;
        this.phone = phone;
        this.tripId = 0 ;
        this.base = 0;
        this.km = 0;
        this.mins = 0;
        this.minDis = 0;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCar() {
        return car;
    }

    public void setCar(String car) {
        this.car = car;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public void setDestLat(double destLat) {
        this.destLat = destLat;
    }

    public void setDestLng(double destLng) {
        this.destLng = destLng;
    }

    public void setDestAddress(String destAddress) {
        this.destAddress = destAddress;
    }

    public void setMyFlag(int myFlag) {
        this.myFlag = myFlag;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public double getDestLat() {
        return destLat;
    }

    public double getDestLng() {
        return destLng;
    }

    public String getDestAddress() {
        return destAddress;
    }

    public int getUserId() {
        return userId;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public int getMyFlag() {
        return myFlag;
    }

    public int getDriverId() {
        return driverId;
    }

    public String getPhone() {
        return phone;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getTripId() {
        return tripId;
    }

    public void setTripId(double tripId) {
        this.tripId = tripId;
    }

    public float getBase() {
        return base;
    }

    public void setBase(float base) {
        this.base = base;
    }

    public float getKm() {
        return km;
    }

    public void setKm(float km) {
        this.km = km;
    }

    public float getMins() {
        return mins;
    }

    public void setMins(float mins) {
        this.mins = mins;
    }

    public int getMinDis() {
        return minDis;
    }

    public void setMinDis(int minDis) {
        this.minDis = minDis;
    }
}