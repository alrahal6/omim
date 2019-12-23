package com.mapsrahal.maps.model;

import androidx.room.Entity;

import java.util.Date;

public class Post {

    private Integer id;
    private int userId;
    private double srcLat;
    private double srcLng;
    private double destLat;
    private double destLng;
    private double tripDistance;
    private Date startTime;
    private String endTime;
    private String sourceAddress;
    private String destinationAddress;
    private double srcDistDiff;
    private double destDistDiff;
    private String phone;

    public double getSrcDistDiff() {
        return srcDistDiff;
    }

    public void setSrcDistDiff(double srcDistDiff) {
        this.srcDistDiff = srcDistDiff;
    }

    public double getDestDistDiff() {
        return destDistDiff;
    }

    public void setDestDistDiff(double destDistDiff) {
        this.destDistDiff = destDistDiff;
    }

    public String getSourceAddress() {
        return sourceAddress;
    }

    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public Post(Integer id,int userId, double srcLat, double srcLng, double destLat, double destLng,
                double tripDistance, String sourceAddress, String destinationAddress,Date startTime,String phone) {
        this.id = id;
        this.userId = userId;
        this.srcLat = srcLat;
        this.srcLng = srcLng;
        this.destLat = destLat;
        this.destLng = destLng;
        this.tripDistance = tripDistance;
        this.sourceAddress = sourceAddress;
        this.destinationAddress = destinationAddress;
        this.startTime = startTime;
        //this.endTime = endTime;
        this.phone = phone;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public double getSrcLat() {
        return srcLat;
    }

    public void setSrcLat(double srcLat) {
        this.srcLat = srcLat;
    }

    public double getSrcLng() {
        return srcLng;
    }

    public void setSrcLng(double srcLng) {
        this.srcLng = srcLng;
    }

    public double getDestLat() {
        return destLat;
    }

    public void setDestLat(double destLat) {
        this.destLat = destLat;
    }

    public double getDestLng() {
        return destLng;
    }

    public void setDestLng(double destLng) {
        this.destLng = destLng;
    }

    public double getTripDistance() {
        return tripDistance;
    }

    public void setTripDistance(double tripDistance) {
        this.tripDistance = tripDistance;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
