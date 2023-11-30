package com.example.student_information_management.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import com.example.student_information_management.R;
import com.example.student_information_management.data.model.User;
import com.example.student_information_management.data.repository.UserRepository;
import com.example.student_information_management.databinding.ActivityEditProfileBinding;
import com.example.student_information_management.databinding.ActivityProvideInfoUserBinding;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Objects;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    private ActivityEditProfileBinding binding;
    private FirebaseFirestore db;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        binding = ActivityEditProfileBinding.inflate (getLayoutInflater ());
        setContentView (binding.getRoot ());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        String[] statuses = getResources ().getStringArray (R.array.status);
        ArrayAdapter<String> adapterStatus = new ArrayAdapter<>(this, R.layout.dropdown_item, statuses);
        binding.status.setAdapter (adapterStatus);

        String[] roles = getResources ().getStringArray (R.array.role);
        ArrayAdapter<String> adapterRole = new ArrayAdapter<>(this, R.layout.dropdown_item, roles);
        binding.role.setAdapter (adapterRole);

        Intent intent = getIntent ();
        if (intent != null) {
            String updateUID = intent.getStringExtra("uid");
            String name = intent.getStringExtra("name");
            String password = intent.getStringExtra("password");
            String email = intent.getStringExtra("email");
            String age = intent.getStringExtra("age");
            String phone = intent.getStringExtra("phone");
            String role = intent.getStringExtra("role");
            String status = intent.getStringExtra("status");

            binding.layoutName.setHint(name);
            binding.layoutEmail.setHint(email);
            binding.layoutAge.setHint(age);
            binding.layoutPhone.setHint(phone);
            binding.layoutRole.setHint(role);
            binding.layoutStatus.setHint(status);

            binding.etName.setText(name);
            binding.etEmail.setText(email);
            binding.etAge.setText(age);
            binding.etPhoneNumber.setText(phone);
            binding.status.setText(role);
            binding.role.setText(status);

            binding.ivSave.setOnClickListener(v -> {
                String newName = binding.etName.getText().toString().trim();
                String newEmail =  binding.etEmail.getText().toString().trim();
                String newAge = binding.etAge.getText().toString().trim();
                String newPhone = binding.etPhoneNumber.getText().toString().trim();
                String newStatus = binding.status.getText().toString().trim();
                String newRole = binding.role.getText().toString().trim();

                Map<String, Object> updatedUser = new HashMap<>();
                updatedUser.put("email", newEmail);
                updatedUser.put("name", newName);
                updatedUser.put("age", Integer.parseInt(newAge));
                updatedUser.put("phoneNumber", newPhone);
                updatedUser.put("role", newRole);
                updatedUser.put("status", newStatus);

                UserRepository userRepository = new UserRepository();
                userRepository.editUser(updateUID, updatedUser, task -> {
                    if (task.isSuccessful()) {
                        if (!email.equals(newEmail)){
                            db.collection("users").document(auth.getCurrentUser().getUid())
                                    .get()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful() && task1.getResult() != null && task1.getResult().exists()) {
                                            DocumentSnapshot document = task1.getResult();
                                            String currentEmail = document.getString("email");
                                            String currentPassword = document.getString("password");
                                            auth.signInWithEmailAndPassword(email, password)
                                                    .addOnSuccessListener(authResult -> {
                                                        //Change Email
                                                        //reLogin
                                                    });
                                        }
                                    });
                        }
                        finish();
                    }
                });
            });
        }
    }
}