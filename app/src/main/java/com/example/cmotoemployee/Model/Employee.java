package com.example.cmotoemployee.Model;

public class Employee {

    private String Number;
    private String Address;
    private String Name;
    private String Email;
    private String Area;

    public Employee(String number, String address, String name, String email, String area) {
        Number = number;
        Address = address;
        Name = name;
        Email = email;
        Area = area;
    }

    public Employee() {
    }

    public String getNumber() {
        return Number;
    }

    public void setNumber(String number) {
        Number = number;
    }

    public String getArea() {
        return Number;
    }

    public void setArea(String area) {
        Area = area;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }
}
