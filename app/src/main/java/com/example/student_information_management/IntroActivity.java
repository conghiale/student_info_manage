package com.example.student_information_management;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.student_information_management.databinding.ActivityIntroBinding;
import com.example.student_information_management.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class IntroActivity extends AppCompatActivity {

    private ActivityIntroBinding binding;
    private FirebaseAuth auth;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        binding = ActivityIntroBinding.inflate (getLayoutInflater ());
        setContentView (binding.getRoot ());

        auth = FirebaseAuth.getInstance ();

        binding.btnLogin.setOnClickListener (v -> {
            startActivity (new Intent (this, LoginActivity.class));
        });
    }
    @Override
    protected void onStart() {
        super.onStart ();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent (this, MainActivity.class);
            intent.putExtra ("EMAIL", currentUser.getEmail ());
            startActivity (intent);
            finish ();
        }
    }
}

