package com.mapsrahal.maps.model;

import java.util.Date;

public class PostRes {

    private Integer id;
    private int userId;
    private double srcLat;
    private double srcLng;
    private double destLat;
    private double destLng;
    private double tripDistance;
    private String startTime;
    private String endTime;
    private String sourceAddress;
    private String destinationAddress;
    private double srcDistDiff;
    private double destDistDiff;
    private String phone;


    private int seats;
    private int dropDownId;
    private String dropDownVal;
    private double price;
    private int selectorFlag;
    private String name;


    public int getSelectorFlag() {
        return selectorFlag;
    }

    public void setSelectorFlag(int selectorFlag) {
        this.selectorFlag = selectorFlag;
    }

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

    public PostRes(Integer id, int userId, double srcLat, double srcLng, double destLat, double destLng,
                   double tripDistance, String sourceAddress, String destinationAddress, String startTime,
                   String phone, int seats, int dropDownId, String dropDownVal, double price, int selectorFlag,
                   String name) {

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
        this.seats = seats;
        this.dropDownId = dropDownId;
        this.dropDownVal = dropDownVal;
        this.price = price;
        this.selectorFlag = selectorFlag;
        this.name = name;
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

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
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

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        this.seats = seats;
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
