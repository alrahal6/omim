package com.mapsrahal.maps.model;

public class LatLngDestination {

    private double lat,lng;
    private int userId;

    public LatLngDestination(double lat, double lng, int userId) {
        this.lat = lat;
        this.lng = lng;
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

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
