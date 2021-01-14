package com.example.in_shelterkidsdirectory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Book implements Serializable {
  String title;
  String author;
  String ISBN;
  String status;
  String description = "";
  String owner;
  HashMap<String,String> requests;
  HashMap<String,Integer> notifications;
  //HashMap<String,Integer> acceptNotifications;
  //HashMap<String,Integer> declineNotifications;

  String uid;
  String borrower;
  String location;

  public Book(String title, String author, String ISBN) {
    this.title = title;
    this.author = author;
    this.ISBN = ISBN;
  }

  public Book(String title, String author, String ISBN, String status) {
    this.title = title;
    this.author = author;
    this.ISBN = ISBN;
    this.status = status;
  }

  public Book(String title, String author, String ISBN, String status, String description) {
    this.title = title;
    this.author = author;
    this.ISBN = ISBN;
    this.status = status;
    this.description = description;
  }

  public Book(String title, String author, String ISBN, String status, String description, String owner) {
    this.title = title;
    this.author = author;
    this.ISBN = ISBN;
    this.status = status;
    this.description = description;
    this.owner = owner;
  }

  public Book(String title, String author, String ISBN, String status, String description, String owner, HashMap<String,String> req) {
    this.title = title;
    this.author = author;
    this.ISBN = ISBN;
    this.status = status;
    this.description = description;
    this.owner = owner;
    if (req == null) {
      this.requests = new HashMap<>();
    } else {
      this.requests = req;
    }
    this.notifications = new HashMap<>();
    //this.acceptNotifications = new HashMap<>();
    //this.declineNotifications = new HashMap<>();
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public HashMap<String, String> getRequests() {
    return requests;
  }

  public void setRequests(HashMap<String,String> requests) {
    this.requests = requests;
  }

  public void removeRequest(String request){

    if(!this.requests.containsKey(request)){
      throw new IllegalArgumentException();
    }
    else {
      this.requests.remove(request);
    }

  }

  public void addRequest(String user, String status){
    this.requests.put(user,status);
  }

  public void emptyRequests() {
    this.requests.clear();
  }

  public String getUid() {
    return uid;
  }

  public void setUid(String uid) {
    this.uid = uid;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getISBN() {
    return ISBN;
  }

  public void setISBN(String ISBN) {
    this.ISBN = ISBN;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getBorrower() {
    return borrower;
  }

  public void setBorrower(String borrower) {
    this.borrower = borrower;
  }

  public HashMap<String, Integer> getNotifications() {
    return notifications;
  }
  /*
      public HashMap<String, Integer> getAcceptNotifications() {
          return acceptNotifications;
      }
      public HashMap<String, Integer> getDeclineNotifications() {
          return declineNotifications;
      }
  */
  public void setNotifications(HashMap<String, Integer> notifications) {
    if (notifications!=null){
      this.notifications = notifications;
    }
  }

  public void addNotification(String user){
    notifications.put(user,0);
  }
  /*
      public void addAcceptNotification(String user){
          acceptNotifications.put(user,0);
      }
      public void addDeclineNotification(String user){
          declineNotifications.put(user,0);
      }
  */
  public void updateNotification(String user){
    notifications.put(user,1);
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }
}