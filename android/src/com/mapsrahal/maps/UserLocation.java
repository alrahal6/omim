package com.mapsrahal.maps;

public class UserLocation {

    private final int userId;
    private final double lat;
    private final double lng;
    private final int myFlag;
    private final int driverId;
    private final String phone;

    public UserLocation(int userId, double lat, double lng, int myFlag, int driverId, String phone) {
        this.userId = userId;
        this.lat = lat;
        this.lng = lng;
        this.myFlag = myFlag;
        this.driverId = driverId;
        this.phone = phone;
    }

    /*public int getUserId() {
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
    }*/

    public String getPhone() {
        return phone;
    }
}
