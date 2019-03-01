package com.example.piCarCustomer;

import java.sql.Timestamp;

public class SingleOrder {
    private String orderID;
    private String driverID;
    private String memId;
    private Integer state;
    private Integer totalAmount;
    private String startLoc;
    private String endLoc;
    private Timestamp startTime;
    private Timestamp endTime;
    private Double startLng;
    private Double startLat;
    private Double endLng;
    private Double endLat;
    private Integer orderType;
    private Integer rate;
    private String note;
    private Timestamp launchTime;

    public SingleOrder() {}

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getDriverID() {
        return driverID;
    }

    public void setDriverID(String driverID) {
        this.driverID = driverID;
    }

    public String getMemID() {
        return memId;
    }

    public void setMemID(String memID) {
        this.memId = memID;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Integer totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStartLoc() {
        return startLoc;
    }

    public void setStartLoc(String startLoc) {
        this.startLoc = startLoc;
    }

    public String getEndLoc() {
        return endLoc;
    }

    public void setEndLoc(String endLoc) {
        this.endLoc = endLoc;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public Double getStartLng() {
        return startLng;
    }

    public void setStartLng(Double startLng) {
        this.startLng = startLng;
    }

    public Double getStartLat() {
        return startLat;
    }

    public void setStartLat(Double startLat) {
        this.startLat = startLat;
    }

    public Double getEndLng() {
        return endLng;
    }

    public void setEndLng(Double endLng) {
        this.endLng = endLng;
    }

    public Double getEndLat() {
        return endLat;
    }

    public void setEndLat(Double endLat) {
        this.endLat = endLat;
    }

    public Integer getOrderType() {
        return orderType;
    }

    public void setOrderType(Integer orderType) {
        this.orderType = orderType;
    }

    public Integer getRate() {
        return rate;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Timestamp getLaunchTime() {
        return launchTime;
    }

    public void setLaunchTime(Timestamp lauchTime) {
        this.launchTime = lauchTime;
    }

    @Override
    public String toString() {
        return "SingleOrderVO [orderId=" + orderID + ", driverId=" + driverID + ", state=" + state + ", totalAmount="
                + totalAmount + ", startLoc=" + startLoc + ", endLoc=" + endLoc + ", startTime=" + startTime
                + ", endTime=" + endTime + ", startLng=" + startLng + ", startLat=" + startLat + ", endLng=" + endLng
                + ", endLat=" + endLat + ", orderType=" + orderType + ", rate=" + rate + ", note=" + note
                + ", lauchTime=" + launchTime + "]";
    }


}