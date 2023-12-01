package com.example.student_information_management.data.repository;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.student_information_management.data.model.Student;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StudentRepository {
    private FirebaseFirestore db;

    public StudentRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public void addStudent(Student student, OnSuccessListener<DocumentReference> onSuccessListener) {
        DocumentReference newStudentRef = db.collection("students").document();
        student.setId(newStudentRef.getId());
        newStudentRef.set(student)
                .addOnSuccessListener(aVoid -> {
                    onSuccessListener.onSuccess(newStudentRef);
                })
                .addOnFailureListener(e -> {
                    Log.w("StudentRepository", "Error adding document", e);
                });
    }

    public void getStudents(MutableLiveData<List<Student>> studentsLiveData) {
        db.collection("students")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Student> studentList = new ArrayList<>();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Student student = documentSnapshot.toObject(Student.class);
                        studentList.add(student);
                    }
                    studentsLiveData.setValue(studentList);
                });
    }

    public void deleteStudent(Student student, OnCompleteListener<Void> onCompleteListener) {
        db.collection("students").document(student.getId())
                .delete()
                .addOnCompleteListener(onCompleteListener);
    }

    public void editStudent(String studentId, Map<String, Object> updatedStudent, OnCompleteListener<Void> onCompleteListener) {
        db.collection("students").document(studentId)
                .update(updatedStudent)
                .addOnCompleteListener(onCompleteListener);
    }
}
