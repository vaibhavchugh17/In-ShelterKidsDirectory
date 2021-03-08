package com.example.in_shelterkidsdirectory;

import java.io.Serializable;

public class Parent implements Serializable {
    String firstName;
    String lastName;
    String DOB;
    String homeAddress;
    String Occupation;
    String phoneNumber;
    String extraInformation;

    public Parent(String firstName, String lastName, String DOB, String homeAddress, String occupation, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.DOB = DOB;
        this.homeAddress = homeAddress;
        Occupation = occupation;
        this.phoneNumber = phoneNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDOB() {
        return DOB;
    }

    public void setDOB(String DOB) {
        this.DOB = DOB;
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress;
    }

    public String getOccupation() {
        return Occupation;
    }

    public void setOccupation(String occupation) {
        Occupation = occupation;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getExtraInformation() {
        return extraInformation;
    }

    public void setExtraInformation(String extraInformation) {
        this.extraInformation = extraInformation;
    }
}
