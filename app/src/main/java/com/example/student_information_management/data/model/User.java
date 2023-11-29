package com.example.student_information_management.data.model;

import com.google.firebase.Timestamp;

import java.util.List;

public class User {
    private String uid;
    private String email;
    private String name;
    private int age;
    private String phoneNumber;
    private String status;
    private String role;
    private List<Timestamp> loginHistory;

    private String password;
    public User() {
    }

    public User(String uid) {
        this.uid = uid;
    }

    public User(String uid, String email, String name, int age, String phoneNumber, String status, String role, List<Timestamp> loginHistory, String password) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.age = age;
        this.phoneNumber = phoneNumber;
        this.status = status;
        this.role = role;
        this.loginHistory = loginHistory;
        this.password = password;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<Timestamp> getLoginHistory() {
        return loginHistory;
    }

    public void setLoginHistory(List<Timestamp> loginHistory) {
        this.loginHistory = loginHistory;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
