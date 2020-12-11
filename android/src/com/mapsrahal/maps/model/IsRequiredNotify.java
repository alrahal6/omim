package com.mapsrahal.maps.model;

public class IsRequiredNotify {

    private int userId;
    private boolean isRequired;

    public IsRequiredNotify(int userId, boolean isRequired) {
        this.userId = userId;
        this.isRequired = isRequired;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        isRequired = required;
    }
}
