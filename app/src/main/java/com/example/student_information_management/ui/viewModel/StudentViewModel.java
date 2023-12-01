package com.example.student_information_management.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.student_information_management.data.model.Student;
import com.example.student_information_management.data.repository.StudentRepository;

import java.util.List;

public class StudentViewModel extends ViewModel {
    private MutableLiveData<List<Student>> students = new MutableLiveData<>();
    private StudentRepository studentRepository = new StudentRepository();

    public LiveData<List<Student>> getStudents() {
        if (students.getValue() == null || students.getValue().isEmpty()) {
            loadStudents();
        }
        return students;
    }

    public void loadStudents() {
        studentRepository.getStudents(students);
    }
}
