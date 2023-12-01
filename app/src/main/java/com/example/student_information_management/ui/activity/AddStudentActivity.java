package com.example.student_information_management.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import com.example.student_information_management.data.model.Student;
import com.example.student_information_management.data.repository.StudentRepository;
import com.example.student_information_management.databinding.ActivityAddStudentBinding;
import com.example.student_information_management.ui.viewModel.StudentViewModel;

import java.util.List;

public class AddStudentActivity extends AppCompatActivity {
    private StudentRepository studentRepository;
    private ActivityAddStudentBinding binding;

    StudentViewModel viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddStudentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        studentRepository = new StudentRepository();

        binding.add.setOnClickListener(v -> {
            Student student = new Student();
            student.setName(binding.name.getText().toString());
            student.setEmail(binding.email.getText().toString());
            student.setMobile(binding.mobile.getText().toString());
            student.setStudentClass(binding.studentClass.getText().toString());
            student.setGpa(Double.parseDouble(binding.gpa.getText().toString()));
            studentRepository.addStudent(student, documentReference -> {
                String studentId = documentReference.getId();
                Log.d("AddStudentActivity", "Added student: " + studentId);
                Toast.makeText(this, "Added student", Toast.LENGTH_SHORT).show();
                LiveData<List<Student>> listLiveData = viewModel.getStudents();
                if (listLiveData != null) {
                    finish();
                }
            });
        });

        binding.ivBack.setOnClickListener(v -> {
            finish();
        });
    }
}