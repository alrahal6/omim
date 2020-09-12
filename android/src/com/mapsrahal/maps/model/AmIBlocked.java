package com.mapsrahal.maps.model;


public class AmIBlocked {

    private int userId;
    private String phone;
    private int selector;

    public AmIBlocked(int userId, String statusFlag,int selector) {
        this.userId = userId;
        this.phone = statusFlag;
        this.selector = selector;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getSelector() {
        return selector;
    }

    public void setSelector(int selector) {
        this.selector = selector;
    }
}
