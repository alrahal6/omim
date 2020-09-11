package com.mapsrahal.maps.model;

public class MyAccount {

    private String amount;
    private String phone;

    public MyAccount(String amount, String phone) {
        this.amount = amount;
        this.phone = phone;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
