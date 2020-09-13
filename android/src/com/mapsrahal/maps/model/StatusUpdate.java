package com.mapsrahal.maps.model;

public class StatusUpdate {

    private int userId;
    private int statusFlag;

    public StatusUpdate(int userId, int statusFlag) {
        this.userId = userId;
        this.statusFlag = statusFlag;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getStatusFlag() {
        return statusFlag;
    }

    public void setStatusFlag(int statusFlag) {
        this.statusFlag = statusFlag;
    }
}
