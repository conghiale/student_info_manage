package com.example.student_information_management.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.student_information_management.R;
import com.example.student_information_management.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth auth;
    private ExecutorService executorService;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        binding = ActivityRegisterBinding.inflate (getLayoutInflater ());
        setContentView (binding.getRoot ());

        auth = FirebaseAuth.getInstance ();
        executorService = Executors.newFixedThreadPool (1);

        binding.btnRegister.setOnClickListener (v -> {
            if (checkErrorInput ()) {
                String email = Objects.requireNonNull (binding.etEmail.getText ()).toString ().trim ();
                String password = Objects.requireNonNull (binding.etPassword.getText ()).toString ().trim ();
                registerAccountInBackground(email, password);
            }
        });

        binding.tvLogin.setOnClickListener (v -> {
            startActivity (new Intent (this, LoginActivity.class));
            finish ();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy ();
        if (executorService != null)
            executorService.shutdown();
    }

    private void registerAccountInBackground(String email, String password) {
        executorService.execute (() -> {
            auth.createUserWithEmailAndPassword (email, password)
                    .addOnCompleteListener (this, task -> {
                        if (task.isSuccessful ()) {
                            runOnUiThread (() -> {
                                FirebaseUser user = auth.getCurrentUser();
                                if (user != null) {
                                    Intent intent = new Intent (RegisterActivity.this, ProvideInfoUserActivity.class);
                                    intent.putExtra ("EMAIL", email);
                                    startActivity (intent);
                                    finish ();
                                }
                            });
                        } else {
                            runOnUiThread (() -> {
                                String errorMessage = Objects.requireNonNull (task.getException ()).getMessage ();
                                showErrorAlertDialog(errorMessage);
                            });
                        }
                    })
                    .addOnCanceledListener (() -> {
                        runOnUiThread (() -> showErrorAlertDialog ("The Register task has been cancelled. Please Register again later"));
                    });
        });
    }

    private boolean checkErrorInput() {
        String email = Objects.requireNonNull (binding.etEmail.getText ()).toString ().trim ();
        String password = Objects.requireNonNull (binding.etPassword.getText ()).toString ().trim ();
        String rePassword = Objects.requireNonNull (binding.etRePassword.getText ()).toString ().trim ();

        if (email.isEmpty ())
            binding.etEmail.setError ("Please enter your email");
        else if (!Patterns.EMAIL_ADDRESS.matcher (email).matches())
            binding.etEmail.setError ("Invalid email");
        else if (password.isEmpty ())
            binding.etPassword.setError ("Please enter your password");
        else if (password.length () < 6)
            binding.etPassword.setError ("Password must contain at least 6 characters");
        else if (!containsLetterAndDigit(password))
            binding.etPassword.setError ("Password must contain at least one letter or number");
        else if (rePassword.isEmpty ())
            binding.etRePassword.setError ("Please re-enter your password");
        else if (rePassword.length () < 6)
            binding.etRePassword.setError ("Password must contain at least 6 characters");
        else if (!containsLetterAndDigit(password))
            binding.etRePassword.setError ("Password must contain at least one letter and number");
        else if (!rePassword.equals (password))
            binding.etRePassword.setError ("Invalid password");
        else
            return true;
        return false;
    }
    private boolean containsLetterAndDigit(String str) {
        boolean hasLetter = false;
        boolean hasDigit = false;

        for (int i = 0; i < str.length(); i++) {
            if (Character.isLetter(str.charAt(i))) {
                hasLetter = true;
            } else if (Character.isDigit(str.charAt(i))) {
                hasDigit = true;
            }
        }

        return (hasLetter && hasDigit);
    }

    @SuppressLint("SetTextI18n")
    private void showErrorAlertDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder (RegisterActivity.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from (RegisterActivity.this).inflate (
                R.layout.layout_error_dialog, (ConstraintLayout)findViewById (R.id.layoutDialogContainer));
        builder.setView (view);

        ((TextView) view.findViewById (R.id.tvTitle)).setText ("Error");
        ((TextView) view.findViewById (R.id.tvMessage)).setText (message);
        ((AppCompatButton) view.findViewById (R.id.btnAction)).setText ("Okay");
        ((ImageView) view.findViewById (R.id.ivImageIcon)).setImageResource (R.drawable.baseline_error);

        final AlertDialog alertDialog = builder.create ();
        view.findViewById (R.id.btnAction).setOnClickListener (v -> {
            alertDialog.dismiss ();
        });

        if (alertDialog.getWindow () != null)
            alertDialog.getWindow ().setBackgroundDrawable (new ColorDrawable (0));
        alertDialog.show ();
        alertDialog.setCancelable(false);
    }
}
