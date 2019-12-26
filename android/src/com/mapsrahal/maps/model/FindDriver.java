package com.mapsrahal.maps.model;

public class FindDriver {

    private int userId;
    private double lat;
    private double lng;
    private double distance;

    public FindDriver(int userId, double lat, double lng, double distance) {
        this.userId = userId;
        this.lat = lat;
        this.lng = lng;
        this.distance = distance;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
