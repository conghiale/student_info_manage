package com.example.student_information_management.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import com.example.student_information_management.R;
import com.example.student_information_management.data.model.User;
import com.example.student_information_management.data.repository.StorageRepository;
import com.example.student_information_management.data.repository.UserRepository;
import com.example.student_information_management.databinding.ActivityEditProfileBinding;
import com.example.student_information_management.ui.viewModel.UserViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    private ActivityEditProfileBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    UserViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        if (intent != null) {
            String updateUID = intent.getStringExtra("uid");
            String name = intent.getStringExtra("name");
            String password = intent.getStringExtra("password");
            String email = intent.getStringExtra("email");
            String age = intent.getStringExtra("age");
            String phone = intent.getStringExtra("phone");
            String role = intent.getStringExtra("role");
            String status = intent.getStringExtra("status");
            String avatar = intent.getStringExtra("avatar");

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
            binding.status.setText(status);
            binding.role.setText(role);

            Picasso.get()
                    .load(avatar)
                    .resize(400, 400)
                    .into(binding.profileAvatar);

            String[] statuses = getResources().getStringArray(R.array.status);
            ArrayAdapter<String> adapterStatus = new ArrayAdapter<>(this, R.layout.dropdown_item, statuses);
            binding.status.setAdapter(adapterStatus);

            String[] roles = getResources().getStringArray(R.array.role);
            ArrayAdapter<String> adapterRole = new ArrayAdapter<>(this, R.layout.dropdown_item, roles);
            binding.role.setAdapter(adapterRole);

            binding.ivSave.setOnClickListener(v -> {
                String newName = binding.etName.getText().toString().trim();
                String newEmail = binding.etEmail.getText().toString().trim();
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
                        if (!email.equals(newEmail)) {
                            Log.e("email", email);
                            Log.e("email", newEmail);
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
                        LiveData<List<User>> listLiveData = viewModel.getUsers();
                        if (listLiveData != null) {
                            finish();
                        }

                    }
                });
            });
        }

        binding.editAvatar.setOnClickListener(v -> {
            assert intent != null;
            String updateUID = intent.getStringExtra("uid");
            Intent intentImage = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intentImage.putExtra("uid", updateUID);
            startActivityForResult(intentImage, 1);
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
                        if ("Admin".equals(role)) {
                            binding.etName.setEnabled(true);
                            binding.etEmail.setEnabled(true);
                            binding.etPhoneNumber.setEnabled(true);
                            binding.etAge.setEnabled(true);
                            binding.status.setEnabled(true);
                            binding.role.setEnabled(true);
                        } else {
                            binding.etName.setEnabled(false);
                            binding.etEmail.setEnabled(false);
                            binding.etPhoneNumber.setEnabled(false);
                            binding.etAge.setEnabled(false);
                            binding.status.setEnabled(false);
                            binding.role.setEnabled(false);
                        }
                    }
                });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri selectedImageUri = data.getData();
                data = getIntent();
                String uid = data.getStringExtra("uid");

                StorageRepository.uploadImageToFirebase(selectedImageUri, uri -> {
                    String downloadUri = uri.toString();

                    Map<String, Object> updatedUser = new HashMap<>();
                    updatedUser.put("avatar", downloadUri);

                    Log.e("uid", uid);
                    UserRepository userRepository = new UserRepository();
                    userRepository.editUser(uid, updatedUser, task -> {
                        if (!task.isSuccessful()){
                            Log.e("ImageError", "update image failed");
                        }
                    });

                    Picasso.get()
                            .load(downloadUri)
                            .resize(400, 400)
                            .into(binding.profileAvatar);
                }, e -> {
                    Log.e("ImageError", "update image failed");
                });
            }
        }
    }
}

