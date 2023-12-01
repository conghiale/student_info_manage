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
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.student_information_management.MainActivity;
import com.example.student_information_management.R;
import com.example.student_information_management.data.model.User;
import com.example.student_information_management.databinding.ActivityLoginBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ExecutorService executorService;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        binding = ActivityLoginBinding.inflate (getLayoutInflater ());
        setContentView (binding.getRoot ());

        auth = FirebaseAuth.getInstance ();
        db = FirebaseFirestore.getInstance ();
        executorService = Executors.newFixedThreadPool (1);

        binding.btnLogin.setOnClickListener (v -> {
            if (checkErrorInput ()) {
                String email = Objects.requireNonNull (binding.etEmail.getText ()).toString ().trim ();
                String password = Objects.requireNonNull (binding.etPassword.getText ()).toString ().trim ();
                db.collection("users")
                        .whereEqualTo("email", email)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if(queryDocumentSnapshots.isEmpty()){
                                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                            } else {
                                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                    User user = documentSnapshot.toObject(User.class);
                                    if (user.getStatus().equals("Locked")){
                                        Toast.makeText(this, "Account has been locked", Toast.LENGTH_SHORT).show();
                                    } else {
                                        loginAccountInBackground(email, password);
                                    }
                                }
                            }
                        });



            }
        });

        binding.tvForgotPassword.setOnClickListener (v -> {
            startActivity (new Intent (this, RecoveryPasswordActivity.class));
            finish ();
        });
    }

    @Override
    protected void onStart() {
        super.onStart ();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent (this, MainActivity.class);
            startActivity (intent);
            finish ();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy ();
        if (executorService != null)
            executorService.shutdown ();
    }
    private void loginAccountInBackground(String email, String password) {
        executorService.execute (() -> {
            auth.signInWithEmailAndPassword (email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            runOnUiThread (() -> {
                                FirebaseUser user = auth.getCurrentUser();
                                if (user != null) {
                                    Timestamp signInTime = new Timestamp(new Date());
                                    String userId = user.getUid();
                                    // Cập nhật lịch sử đăng nhập
                                    db.collection("users").document(userId)
                                            .update("loginHistory", FieldValue.arrayUnion(signInTime))
                                            .addOnSuccessListener(aVoid -> {
                                                //
                                            })
                                            .addOnFailureListener(e -> {
                                                //
                                            });
                                    Intent intent = new Intent (LoginActivity.this, MainActivity.class);
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
                    }).addOnCanceledListener (() ->
                        runOnUiThread (() -> {
                            showErrorAlertDialog("The system is maintenance. The Login task has been cancelled. Please Login again later");
                        })

                    );
        });
    }
    private boolean checkErrorInput() {
        String email = Objects.requireNonNull (binding.etEmail.getText ()).toString ().trim ();
        String password = Objects.requireNonNull (binding.etPassword.getText ()).toString ().trim ();

        if (email.isEmpty ())
            binding.etEmail.setError ("Please enter your email");
        else if (!Patterns.EMAIL_ADDRESS.matcher (email).matches())
            binding.etEmail.setError ("Invalid email");
        else if (password.isEmpty ())
            binding.etPassword.setError ("Please enter your password");
        else if (password.length () < 6)
            binding.etPassword.setError ("Password must contain at least 6 characters");
        else if (!containsLetterAndDigit(password))
            binding.etPassword.setError ("Password must contain at least one letter and number");
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
        AlertDialog.Builder builder = new AlertDialog.Builder (LoginActivity.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from (LoginActivity.this).inflate (
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
