package com.mapsrahal.maps.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_table")
public class User {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String userName;
    private int phone;
    private int vehicleType;

    public User(String userName, int phone, int vehicleType) {
        this.userName = userName;
        this.phone = phone;
        this.vehicleType = vehicleType;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public int getPhone() {
        return phone;
    }

    public int getVehicleType() {
        return vehicleType;
    }
}
