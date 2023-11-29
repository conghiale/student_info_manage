package com.example.student_information_management.ui.activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.student_information_management.MainActivity;
import com.example.student_information_management.R;
import com.example.student_information_management.databinding.ActivityChangePasswordBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChangePasswordActivity extends AppCompatActivity {

    private ActivityChangePasswordBinding binding;
    private FirebaseAuth auth;
    private ExecutorService executorService;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate (getLayoutInflater ());
        setContentView (binding.getRoot ());

        auth = FirebaseAuth.getInstance ();
        executorService = Executors.newFixedThreadPool (1);

        binding.btnRecover.setOnClickListener (v -> {
            if (checkErrorInput ()) {
                String newPassword = Objects.requireNonNull (binding.etNewPassword.getText ()).toString ().trim ();
                changePassword (newPassword);
            }
        });

        binding.ivBack.setOnClickListener (v -> {
            startActivity (new Intent (this, MainActivity.class));
            finish ();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy ();
        if (executorService != null)
            executorService.shutdown ();
    }

    private void changePassword(String password) {
        executorService.execute (() -> {
            FirebaseUser user = auth.getCurrentUser ();
            if (user != null) {
                user.updatePassword (password)
                        .addOnCompleteListener (task -> {
                            if (task.isSuccessful ()) {
                                runOnUiThread (() -> {
                                    showSuccessAlertDialog("Your password has been updated successfully");
                                });
                            } else {
                                runOnUiThread (() -> {
                                    String errorMessage = Objects.requireNonNull (task.getException ()).getMessage ();
                                    showErrorAlertDialog(errorMessage);
                                });
                            }
                        })
                        .addOnCanceledListener (() -> {
                            runOnUiThread (() -> {
                                showErrorAlertDialog("The Change Password task has been cancelled. Please Update Password again later");
                            });
                        });
            }
        });
    }
    private boolean checkErrorInput() {
        String currentPassword = Objects.requireNonNull (binding.etCurrentPassword.getText ()).toString ().trim ();
        String newPassword = Objects.requireNonNull (binding.etNewPassword.getText ()).toString ().trim ();
        String reNewPassword = Objects.requireNonNull (binding.etReNewPassword.getText ()).toString ().trim ();

        if (currentPassword.isEmpty ())
            binding.etCurrentPassword.setError ("Please enter your password");
        else if (currentPassword.length () < 6)
            binding.etCurrentPassword.setError ("Password must contain at least 6 characters");
        else if (!containsLetterAndDigit(currentPassword))
            binding.etCurrentPassword.setError ("Password must contain at least one letter or number");
        else if (newPassword.isEmpty ())
            binding.etNewPassword.setError ("Please enter your password");
        else if (newPassword.length () < 6)
            binding.etNewPassword.setError ("Password must contain at least 6 characters");
        else if (!containsLetterAndDigit(newPassword))
            binding.etNewPassword.setError ("Password must contain at least one letter or number");
        else if (reNewPassword.isEmpty ())
            binding.etReNewPassword.setError ("Please re-enter your password");
        else if (reNewPassword.length () < 6)
            binding.etReNewPassword.setError ("Password must contain at least 6 characters");
        else if (!containsLetterAndDigit(reNewPassword))
            binding.etReNewPassword.setError ("Password must contain at least one letter and number");
        else if (!reNewPassword.equals (newPassword))
            binding.etReNewPassword.setError ("Invalid new password");
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

        return hasLetter && hasDigit;
    }
    private void showErrorAlertDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder (ChangePasswordActivity.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from (ChangePasswordActivity.this).inflate (
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
        AlertDialog.Builder builder = new AlertDialog.Builder (ChangePasswordActivity.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from (ChangePasswordActivity.this).inflate (
                R.layout.layout_success_dialog, (ConstraintLayout)findViewById (R.id.layoutDialogContainer));
        builder.setView (view);

        ((TextView) view.findViewById (R.id.tvTitle)).setText ("Success");
        ((TextView) view.findViewById (R.id.tvMessage)).setText (message);
        ((AppCompatButton) view.findViewById (R.id.btnAction)).setText ("Okay");
        ((ImageView) view.findViewById (R.id.ivImageIcon)).setImageResource (R.drawable.baseline_done);

        final AlertDialog alertDialog = builder.create ();
        view.findViewById (R.id.btnAction).setOnClickListener (v -> {
            alertDialog.dismiss ();
            Intent intent = new Intent (ChangePasswordActivity.this, MainActivity.class); // xử lý re_auth
            intent.putExtra ("PASSWORD_UPDATED", "true");
            startActivity (intent);
            finish ();
        });

        if (alertDialog.getWindow () != null)
            alertDialog.getWindow ().setBackgroundDrawable (new ColorDrawable (0));
        alertDialog.show ();
        alertDialog.setCancelable(false);
    }
}
