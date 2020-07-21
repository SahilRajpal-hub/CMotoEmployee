package com.example.cmotoemployee.Model;

public class Car {

    private String Number;
    private String Model;
    private String Name;
    private String Category;
    private String Color;
    private String Photo;
    private String MobileNo;
    private String Address;
    private String Location;

    public Car(String number, String model, String name, String category, String color, String photo, String mobileNo, String address, String location) {
        Number = number;
        Model = model;
        Name = name;
        Category = category;
        Color = color;
        Photo = photo;
        MobileNo = mobileNo;
        Address = address;
        Location = location;
    }

    public Car() {
    }

    public String getNumber() {
        return Number;
    }

    public void setNumber(String number) {
        Number = number;
    }

    public String getModel() {
        return Model;
    }

    public void setModel(String model) {
        Model = model;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getCategory() {
        return Category;
    }

    public void setCategory(String category) {
        Category = category;
    }

    public String getColor() {
        return Color;
    }

    public void setColor(String color) {
        Color = color;
    }

    public String getPhoto() {
        return Photo;
    }

    public void setPhoto(String photo) {
        Photo = photo;
    }

    public String getMobileNo() {
        return MobileNo;
    }

    public void setMobileNo(String mobileNo) {
        MobileNo = mobileNo;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }
}
