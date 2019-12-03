package com.mapsrahal.maps.model;

public class RequestMatch {

    private double reqId,reqUsrId;

    public RequestMatch(double reqId, double reqUsrId) {
        this.reqId = reqId;
        this.reqUsrId = reqUsrId;
    }

    public double getReqId() {
        return reqId;
    }

    public void setReqId(double reqId) {
        this.reqId = reqId;
    }

    public double getReqUsrId() {
        return reqUsrId;
    }

    public void setReqUsrId(double reqUsrId) {
        this.reqUsrId = reqUsrId;
    }
}
