package com.example.student_information_management.data.model;

import com.google.firebase.Timestamp;

public class Certificate {
    private String id;
    private String idStudent;
    private String name;
    private String dateOfExam;
    private String result;

    public Certificate() {
    }

    public Certificate(String id, String idStudent, String name, String dateOfExam, String result) {
        this.id = id;
        this.idStudent = idStudent;
        this.name = name;
        this.dateOfExam = dateOfExam;
        this.result = result;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdStudent() {
        return idStudent;
    }

    public void setIdStudent(String idStudent) {
        this.idStudent = idStudent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDateOfExam() {
        return dateOfExam;
    }

    public void setDateOfExam(String dateOfExam) {
        this.dateOfExam = dateOfExam;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
