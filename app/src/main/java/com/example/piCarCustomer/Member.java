package com.example.piCarCustomer;

import android.os.Parcel;
import android.os.Parcelable;

import java.sql.Date;

public class Member implements Parcelable {
    private String memID;
    private String name;
    private String email;
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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.memID);
        dest.writeString(this.name);
        dest.writeString(this.email);
        dest.writeString(this.phone);
        dest.writeString(this.creditCard);
        dest.writeInt(this.pet);
        dest.writeInt(this.smoke);
        dest.writeInt(this.gender);
        dest.writeInt(this.token);
        dest.writeInt(this.activityToken);
        dest.writeLong(this.birthday.getTime());
        dest.writeInt(this.verified);
        dest.writeInt(this.babySeat);
    }

    protected Member(Parcel in) {
        this.memID = in.readString();
        this.name = in.readString();
        this.email = in.readString();
        this.phone = in.readString();
        this.creditCard = in.readString();
        this.pet = in.readInt();
        this.smoke = in.readInt();
        this.gender = in.readInt();
        this.token = in.readInt();
        this.activityToken = in.readInt();
        this.birthday = new Date(in.readLong());
        this.verified = in.readInt();
        this.babySeat = in.readInt();
    }

    public static final Parcelable.Creator<Member> CREATOR = new Parcelable.Creator<Member>() {
        @Override
        public Member createFromParcel(Parcel source) {
            return new Member(source);
        }

        @Override
        public Member[] newArray(int size) {
            return new Member[size];
        }
    };
}
