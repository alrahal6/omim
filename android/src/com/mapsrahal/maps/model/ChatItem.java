package com.mapsrahal.maps.model;


public class ChatItem  {
    private String phone;
    private String message;
    private String toPhoneNumber;

    public ChatItem(String phone, String message, String toPhoneNumber) {
        this.phone = phone;
        this.message = message;
        this.toPhoneNumber = toPhoneNumber;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToPhoneNumber() {
        return toPhoneNumber;
    }

    public void setToPhoneNumber(String toPhoneNumber) {
        this.toPhoneNumber = toPhoneNumber;
    }
}
