package com.mapsrahal.maps.model;


public class GetMyHistory {

    private int userId;
    private String phone;

    public GetMyHistory(int userId, String statusFlag) {
        this.userId = userId;
        this.phone = statusFlag;
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
}
