package com.example.cmotoemployee.Model;

public class Payment {
    private String Date;
    private String PayOn;
    private int Price;
    private int CarsCleaned;

    public Payment() {
    }

    public Payment(String date, String payOn, int price, int carsCleaned) {
        Date = date;
        PayOn = payOn;
        Price = price;
        CarsCleaned = carsCleaned;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getPayOn() {
        return PayOn;
    }

    public void setPayOn(String payOn) {
        PayOn = payOn;
    }

    public int getPrice() {
        return Price;
    }

    public void setPrice(int price) {
        Price = price;
    }

    public int getCarsCleaned() {
        return CarsCleaned;
    }

    public void setCarsCleaned(int carsCleaned) {
        CarsCleaned = carsCleaned;
    }
}
