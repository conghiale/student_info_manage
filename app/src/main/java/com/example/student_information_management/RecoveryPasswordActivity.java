package com.example.student_information_management;

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

import com.example.student_information_management.databinding.ActivityLoginBinding;
import com.example.student_information_management.databinding.ActivityRecoveryPasswordBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecoveryPasswordActivity extends AppCompatActivity {

    private ActivityRecoveryPasswordBinding binding;
    private FirebaseAuth auth;
    private ExecutorService executorService;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        binding = ActivityRecoveryPasswordBinding.inflate (getLayoutInflater ());
        setContentView (binding.getRoot ());

        auth = FirebaseAuth.getInstance ();
        executorService = Executors.newFixedThreadPool (1);

        binding.btnRecover.setOnClickListener (v -> {
            if (checkErrorInput ()) {
                String email = Objects.requireNonNull (binding.etEmail.getText ()).toString ().trim ();
                recoveryPasswordInBackground (email);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy ();
        if (executorService != null)
            executorService.shutdown ();
    }

    private void recoveryPasswordInBackground(String email) {
        executorService.execute (() -> {
            auth.sendPasswordResetEmail (email)
                    .addOnCompleteListener (task -> {
                        if (task.isSuccessful ()) {
                            runOnUiThread (() -> showSuccessAlertDialog("Recovery password link has been sent to your registered email"));
                        } else {
                            runOnUiThread (() -> {
                                String errorMessage = Objects.requireNonNull (task.getException ()).getMessage ();
                                showErrorAlertDialog(errorMessage);
                            });
                        }
                    })
                    .addOnCanceledListener (() -> runOnUiThread (() -> showErrorAlertDialog("The Recovery Password task has been cancelled. Please Forgot Password again later")));
        });
    }
    private boolean checkErrorInput() {
        String email = Objects.requireNonNull (binding.etEmail.getText ()).toString ().trim ();

        if (email.isEmpty ())
            binding.etEmail.setError ("Please enter your email");
        else if (!Patterns.EMAIL_ADDRESS.matcher (email).matches())
            binding.etEmail.setError ("Invalid email");
        else
            return true;
        return false;
    }
    private void showErrorAlertDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder (RecoveryPasswordActivity.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from (RecoveryPasswordActivity.this).inflate (
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

    private void showSuccessAlertDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder (RecoveryPasswordActivity.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from (RecoveryPasswordActivity.this).inflate (
                R.layout.layout_success_dialog, (ConstraintLayout)findViewById (R.id.layoutDialogContainer));
        builder.setView (view);

        ((TextView) view.findViewById (R.id.tvTitle)).setText ("Success");
        ((TextView) view.findViewById (R.id.tvMessage)).setText (message);
        ((AppCompatButton) view.findViewById (R.id.btnAction)).setText ("Okay");
        ((ImageView) view.findViewById (R.id.ivImageIcon)).setImageResource (R.drawable.baseline_done);

        final AlertDialog alertDialog = builder.create ();
        view.findViewById (R.id.btnAction).setOnClickListener (v -> {
            alertDialog.dismiss ();
            startActivity (new Intent (RecoveryPasswordActivity.this, LoginActivity.class));
            finish ();
        });

        if (alertDialog.getWindow () != null)
            alertDialog.getWindow ().setBackgroundDrawable (new ColorDrawable (0));
        alertDialog.show ();
        alertDialog.setCancelable(false);
    }
}
