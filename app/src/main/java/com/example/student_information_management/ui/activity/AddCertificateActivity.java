package com.example.student_information_management.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.student_information_management.data.model.Certificate;
import com.example.student_information_management.data.repository.CertificateRepository;
import com.example.student_information_management.databinding.ActivityAddCertificateBinding;
import com.example.student_information_management.ui.viewModel.CertificateViewModel;

public class AddCertificateActivity extends AppCompatActivity {
    private CertificateRepository certificateRepository;
    private ActivityAddCertificateBinding binding;

    CertificateViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddCertificateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        certificateRepository = new CertificateRepository();

        Intent intent = getIntent();
        String idStudent = intent.getStringExtra("idStudent");

        binding.add.setOnClickListener(v -> {
            Certificate certificate = new Certificate();
            certificate.setIdStudent(idStudent);
            certificate.setName(binding.name.getText().toString());
            certificate.setDateOfExam(binding.dateOfExam.getText().toString());
            certificate.setResult(binding.result.getText().toString());
            certificateRepository.addCertificate(certificate, documentReference -> {
                String certificateId = documentReference.getId();
                Log.d("AddCertificateActivity", "Added certificate: " + certificateId);
                Toast.makeText(this, "Added certificate", Toast.LENGTH_SHORT).show();
                viewModel.getCertificates(idStudent).observe(this, certificates -> {
                    finish();
                });
            });
        });

        binding.ivBack.setOnClickListener(v -> {
            finish();
        });
    }
}
