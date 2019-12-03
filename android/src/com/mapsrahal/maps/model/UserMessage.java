package com.mapsrahal.maps.model;

public class UserMessage {

    private int userId;
    private String title,msgBody;


    public UserMessage(int userId,String title, String msgBody) {
        this.userId = userId;
        this.title = title;
        this.msgBody = msgBody;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return msgBody;
    }

    public void setBody(String body) {
        this.msgBody = body;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
