package com.example.student_information_management.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.student_information_management.databinding.ActivityStudentDetailBinding;
import com.example.student_information_management.ui.adapter.CertificateAdapter;
import com.example.student_information_management.ui.viewModel.CertificateViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class StudentDetailActivity extends AppCompatActivity {
    private ActivityStudentDetailBinding binding;
    private CertificateViewModel viewModel;
    private CertificateAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(CertificateViewModel.class);

        RecyclerView rcvCertificate = binding.rcvCertificate;
        rcvCertificate.setLayoutManager(new LinearLayoutManager(this));

        Intent intent = getIntent();
        String idStudent = intent.getStringExtra("idStudent");
        binding.name.setText(intent.getStringExtra("name"));
        binding.email.setText(intent.getStringExtra("email"));
        binding.mobile.setText(intent.getStringExtra("studentClass"));
        binding.studentClass.setText(intent.getStringExtra("phone"));
        binding.gpa.setText(intent.getStringExtra("gpa"));

        viewModel.getCertificates(idStudent).observe(this, certificates -> {
            adapter = new CertificateAdapter(certificates);
            adapter.setContext(this);
            binding.rcvCertificate.setAdapter(adapter);
        });

        binding.ivAdd.setOnClickListener(v -> {
            Intent addIntent = new Intent(this, AddCertificateActivity.class);
            addIntent.putExtra("idStudent", idStudent);
            startActivity(addIntent);
        });

        binding.ivBack.setOnClickListener(v -> {
            finish();
        });

        //Check role
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        DocumentSnapshot document = task.getResult();
                        String role = document.getString("role");
                        if ("Employee".equals(role)) {
                            binding.ivAdd.setVisibility(View.INVISIBLE);
                        } else {
                            binding.ivAdd.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }
}