package com.example.student_information_management.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import com.example.student_information_management.data.model.Certificate;
import com.example.student_information_management.data.repository.CertificateRepository;
import com.example.student_information_management.databinding.ActivityEditCertificateBinding;
import com.example.student_information_management.ui.viewModel.CertificateViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditCertificateActivity extends AppCompatActivity {
    private ActivityEditCertificateBinding binding;
    private CertificateRepository certificateRepository = new CertificateRepository();
    CertificateViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditCertificateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.ivBack.setOnClickListener(v -> finish());

        Intent intent = getIntent();
        if (intent != null) {
            String studentId = intent.getStringExtra("studentId");
            String id = intent.getStringExtra("id");
            String name = intent.getStringExtra("name");
            String dateOfExam = intent.getStringExtra("dateOfExam");
            String result = intent.getStringExtra("result");

            binding.name.setText(name);
            binding.dateOfExam.setText(dateOfExam);
            binding.result.setText(result);

            binding.save.setOnClickListener(v -> {
                Map<String, Object> updatedCertificate = new HashMap<>();
                updatedCertificate.put("name", binding.name.getText().toString());
                updatedCertificate.put("dateOfExam", binding.dateOfExam.getText().toString());
                updatedCertificate.put("result", binding.result.getText().toString());

                certificateRepository.editCertificate(id, updatedCertificate, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Save successful", Toast.LENGTH_SHORT).show();
                        LiveData<List<Certificate>> listLiveData = viewModel.getCertificates(studentId);
                        if (listLiveData != null) {
                            finish();
                        }
                    }
                });
            });
        }
    }
}
