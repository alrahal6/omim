package com.mapsrahal.maps.auth;

public class IsBlocked {
    private String userPhone;
    private int userId;
    private boolean isAllowed;

    public IsBlocked(String userPhone, int userId,boolean isAllowed) {
        this.userPhone = userPhone;
        this.userId = userId;
        this.isAllowed = isAllowed;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public boolean isAllowed() {
        return isAllowed;
    }

    public void setAllowed(boolean allowed) {
        isAllowed = allowed;
    }
}
