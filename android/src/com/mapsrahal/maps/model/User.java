package com.mapsrahal.maps.model;

public class User {

    private String userName,phone;
    private int id, vehicleType;

    public User(String userName, String phone, int vehicleType) {
        this.userName = userName;
        this.phone = phone;
        this.vehicleType = vehicleType;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(int vehicleType) {
        this.vehicleType = vehicleType;
    }
}
