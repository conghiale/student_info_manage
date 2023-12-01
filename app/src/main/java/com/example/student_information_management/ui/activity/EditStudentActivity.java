package com.example.student_information_management.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import com.example.student_information_management.data.model.Student;
import com.example.student_information_management.data.repository.StudentRepository;
import com.example.student_information_management.databinding.ActivityEditStudentBinding;
import com.example.student_information_management.ui.viewModel.StudentViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditStudentActivity extends AppCompatActivity {
    private ActivityEditStudentBinding binding;
    private StudentRepository studentRepository = new StudentRepository();
    StudentViewModel viewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditStudentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.ivBack.setOnClickListener(v -> {
            finish();
        });

        Intent intent = getIntent();
        if (intent != null) {
            String id = intent.getStringExtra("id");
            String name = intent.getStringExtra("name");
            String email = intent.getStringExtra("email");
            String phone = intent.getStringExtra("phone");
            String studentClass = intent.getStringExtra("studentClass");
            String gpa = intent.getStringExtra("gpa");

            binding.name.setText(name);
            binding.email.setText(email);
            binding.studentClass.setText(studentClass);
            binding.mobile.setText(phone);
            binding.gpa.setText(gpa);

            binding.save.setOnClickListener(v -> {
                Map<String, Object> updatedStudent = new HashMap<>();
                updatedStudent.put("email", binding.email.getText().toString());
                updatedStudent.put("name", binding.name.getText().toString());
                updatedStudent.put("studentClass", binding.studentClass.getText().toString());
                updatedStudent.put("mobile", binding.mobile.getText().toString());
                updatedStudent.put("gpa", Double.parseDouble(binding.mobile.getText().toString()));


                studentRepository.editStudent(id, updatedStudent, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "save success", Toast.LENGTH_SHORT).show();
                        LiveData<List<Student>> listLiveData = viewModel.getStudents();
                        if (listLiveData != null) {
                            finish();
                        }
                    }
                });

            });
        }
    }
}