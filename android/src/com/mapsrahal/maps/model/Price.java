package com.mapsrahal.maps.model;

public class Price {

    private double price;
    private String distance;
    private int flag;

    public Price(double price, String distance, int flag) {
        this.price = price;
        this.distance = distance;
        this.flag = flag;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }
}
