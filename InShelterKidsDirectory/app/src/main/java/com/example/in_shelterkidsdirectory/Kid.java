package com.example.in_shelterkidsdirectory;

import java.io.Serializable;
import java.util.ArrayList;

public class Kid implements Serializable {
    String firstName;
    String lastName;
    String middleName;
    String eyeColor;
    String DOB;
    String hairColor;
    String status;
    String UID;
    String height;
    String nationality;
    String allergies;
    String birthmarks;
    Parent father;
    Parent mother;
    String DOA;
    String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    Parent guardian;
    ArrayList<Note> concerns;
    ArrayList<Note> notes;
    ArrayList<Parent> referrals;

    public Kid(){
        this.referrals = new ArrayList<>();
    }


    public Kid(String firstName, String lastName, String middleName, String eyeColor, String DOB, String hairColor, String status, String height, String nationality, String allergies, String birthmarks) {
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
        this.status=status;
        this.referrals = new ArrayList<>();
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

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getBirthmarks() {
        return birthmarks;
    }

    public void setBirthmarks(String birthmarks) {
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

    public ArrayList<Note> getConcerns() {
        return concerns;
    }

    public void setConcerns(ArrayList<Note> concerns) {
        this.concerns = concerns;
    }

    public ArrayList<Note> getNotes() {
        return notes;
    }

    public void setNotes(ArrayList<Note> notes) {
        this.notes = notes;
    }

    public ArrayList<Parent> getReferrals() {
        return referrals;
    }

    public void setReferrals(ArrayList<Parent> referrals) {
        this.referrals = referrals;
    }

    public void addReferrals (Parent parent){
         referrals.add(parent);
    }

    public void removeReferral (Parent parent) {
        referrals.remove(parent);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }


    public Parent getGuardian() {
        return guardian;
    }

    public void setGuardian(Parent guardian) {
        this.guardian = guardian;
    }

    public String getDOA() {
        return DOA;
    }

    public void setDOA(String DOA) {
        this.DOA = DOA;
    }
}
