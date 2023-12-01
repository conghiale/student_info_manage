package com.example.student_information_management.data.model;

public class Student {
    private String id;
    private String name;
    private String email;
    private String mobile;
    private String studentClass;
    private double gpa;

    public Student() {
    }
    public Student(String id, String name, String email, String mobile, String studentClass, double gpa) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.studentClass = studentClass;
        this.gpa = gpa;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getStudentClass() {
        return studentClass;
    }

    public void setStudentClass(String studentClass) {
        this.studentClass = studentClass;
    }

    public double getGpa() {
        return gpa;
    }

    public void setGpa(double gpa) {
        this.gpa = gpa;
    }
}
