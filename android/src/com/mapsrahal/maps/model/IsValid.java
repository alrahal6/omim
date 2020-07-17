package com.mapsrahal.maps.model;

public class IsValid {

    private String message;
    private int flag;

    public IsValid(String message, int flag) {
        this.message = message;
        this.flag = flag;
    }
    
    public String getDistance() {
        return message;
    }

    public void setDistance(String distance) {
        this.message = distance;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }
}
