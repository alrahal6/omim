package com.mapsrahal.maps.model;

public class NearbySearch {

    public int nearImage;
    public String nearFrom;
    public String nearTo;
    public String nearDistance;
    public String nearSeats;
    public String nearTime;
    public String nearGender;
    public String nearAmount;
    public double fromLat;
    public double fromLng;
    public double toLat;
    public double toLng;

    public NearbySearch(int nearImage, String nearFrom, String nearTo,
                        String nearDistance, String nearSeats, String nearTime,
                        String nearGender, String nearAmount, double fromLat,
                        double fromLng, double toLat, double toLng) {
        this.nearImage = nearImage;
        this.nearFrom = nearFrom;
        this.nearTo = nearTo;
        this.nearDistance = nearDistance;
        this.nearSeats = nearSeats;
        this.nearTime = nearTime;
        this.nearGender = nearGender;
        this.nearAmount = nearAmount;
        this.fromLat = fromLat;
        this.fromLng = fromLng;
        this.toLat = toLat;
        this.toLng = toLng;
    }

    public int getNearImage() {
        return nearImage;
    }

    public void setNearImage(int nearImage) {
        this.nearImage = nearImage;
    }

    public String getNearFrom() {
        return nearFrom;
    }

    public void setNearFrom(String nearFrom) {
        this.nearFrom = nearFrom;
    }

    public String getNearTo() {
        return nearTo;
    }

    public void setNearTo(String nearTo) {
        this.nearTo = nearTo;
    }

    public String getNearDistance() {
        return nearDistance;
    }

    public void setNearDistance(String nearDistance) {
        this.nearDistance = nearDistance;
    }

    public String getNearSeats() {
        return nearSeats;
    }

    public void setNearSeats(String nearSeats) {
        this.nearSeats = nearSeats;
    }

    public String getNearTime() {
        return nearTime;
    }

    public void setNearTime(String nearTime) {
        this.nearTime = nearTime;
    }

    public String getNearGender() {
        return nearGender;
    }

    public void setNearGender(String nearGender) {
        this.nearGender = nearGender;
    }

    public String getNearAmount() {
        return nearAmount;
    }

    public void setNearAmount(String nearAmount) {
        this.nearAmount = nearAmount;
    }

    public double getFromLat() {
        return fromLat;
    }

    public void setFromLat(double fromLat) {
        this.fromLat = fromLat;
    }

    public double getFromLng() {
        return fromLng;
    }

    public void setFromLng(double fromLng) {
        this.fromLng = fromLng;
    }

    public double getToLat() {
        return toLat;
    }

    public void setToLat(double toLat) {
        this.toLat = toLat;
    }

    public double getToLng() {
        return toLng;
    }

    public void setToLng(double toLng) {
        this.toLng = toLng;
    }
}
