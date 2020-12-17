package com.mapsrahal.maps.websocket;

public class CaptainAccepted {

    private final int userId;
    private final double lat;
    private final double lng;
    private int myFlag;
    private final int driverId;
    private final String phone;
    private final String captainName;
    private final String captainVehicle;

    public CaptainAccepted(int userId, double lat, double lng, int myFlag, int driverId,
                           String phone, String captainName, String captainVehicle) {
        this.userId = userId;
        this.lat = lat;
        this.lng = lng;
        this.myFlag = myFlag;
        this.driverId = driverId;
        this.phone = phone;
        this.captainName = captainName;
        this.captainVehicle = captainVehicle;
    }

    public String getPhone() {
        return phone;
    }

    public String getCaptainName() {
        return captainName;
    }

    public String getCaptainVehicle() {
        return captainVehicle;
    }

    public int getMyFlag() {
        return myFlag;
    }

    public void setMyFlag(int myFlag) {
        this.myFlag = myFlag;
    }
}
