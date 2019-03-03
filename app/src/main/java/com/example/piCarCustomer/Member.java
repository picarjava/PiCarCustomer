package com.example.piCarCustomer;

import java.sql.Date;

public class Member {

    private String memID;
    private String name;
    private String email;
    private String password;
    private String phone;
    private String creditCard;
    private Integer pet;
    private Integer smoke;
    private Integer gender;
    private Integer token;
    private Integer activityToken;
    private Date birthday;
    private Integer verified;
    private Integer babySeat;


    public String getMemID() {
        return memID;
    }

    public void setMemID(String memID) {
        this.memID = memID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(String creditCard) {
        this.creditCard = creditCard;
    }

    public Integer getPet() {
        return pet;
    }

    public void setPet(Integer pet) {
        this.pet = pet;
    }

    public Integer getSmoke() {
        return smoke;
    }

    public void setSmoke(Integer smoke) {
        this.smoke = smoke;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public Integer getToken() {
        return token;
    }

    public void setToken(Integer token) {
        this.token = token;
    }

    public Integer getActivityToken() {
        return activityToken;
    }

    public void setActivityToken(Integer activityToken) {
        this.activityToken = activityToken;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public Integer getVerified() {
        return verified;
    }

    public void setVerified(Integer verified) {
        this.verified = verified;
    }

    public Integer getBabySeat() {
        return babySeat;
    }

    public void setBabySeat(Integer babySeat) {
        this.babySeat = babySeat;
    }
}
