package com.example.in_shelterkidsdirectory;

import java.util.ArrayList;

public class Kid {
    String firstName;
    String lastName;
    String middleName;
    String eyeColor;
    String DOB;
    String hairColor;
    float height;
    String nationality;
    ArrayList<String> allergies;
    ArrayList<String> birthmarks;

    //These attributes down below are not included in the constructor as they will be added later when the kid information is added using setters
    Parent father;
    Parent mother;
    ArrayList<String> concerns;
    ArrayList<String> notes;
    ArrayList<String> referrals;

    public Kid(String firstName, String lastName, String middleName, String eyeColor, String DOB, String hairColor, float height, String nationality, ArrayList<String> allergies, ArrayList<String> birthmarks) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.eyeColor = eyeColor;
        this.DOB = DOB;
        this.hairColor = hairColor;
        this.height = height;
        this.nationality = nationality;
        this.allergies = allergies;
        this.birthmarks = birthmarks;
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

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getEyeColor() {
        return eyeColor;
    }

    public void setEyeColor(String eyeColor) {
        this.eyeColor = eyeColor;
    }

    public String getDOB() {
        return DOB;
    }

    public void setDOB(String DOB) {
        this.DOB = DOB;
    }

    public String getHairColor() {
        return hairColor;
    }

    public void setHairColor(String hairColor) {
        this.hairColor = hairColor;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public ArrayList<String> getAllergies() {
        return allergies;
    }

    public void setAllergies(ArrayList<String> allergies) {
        this.allergies = allergies;
    }

    public ArrayList<String> getBirthmarks() {
        return birthmarks;
    }

    public void setBirthmarks(ArrayList<String> birthmarks) {
        this.birthmarks = birthmarks;
    }

    public Parent getFather() {
        return father;
    }

    public void setFather(Parent father) {
        this.father = father;
    }

    public Parent getMother() {
        return mother;
    }

    public void setMother(Parent mother) {
        this.mother = mother;
    }

    public ArrayList<String> getConcerns() {
        return concerns;
    }

    public void setConcerns(ArrayList<String> concerns) {
        this.concerns = concerns;
    }

    public ArrayList<String> getNotes() {
        return notes;
    }

    public void setNotes(ArrayList<String> notes) {
        this.notes = notes;
    }

    public ArrayList<String> getReferrals() {
        return referrals;
    }

    public void setReferrals(ArrayList<String> referrals) {
        this.referrals = referrals;
    }
}
