package com.example.student_information_management.ui.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.student_information_management.MainActivity;
import com.example.student_information_management.databinding.FragmentChangePasswordBinding;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChangePasswordFragment extends Fragment {
    private FragmentChangePasswordBinding binding;
    private FirebaseAuth auth;
    private ExecutorService executorService;
    private Context context;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach (context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        auth = FirebaseAuth.getInstance ();
        executorService = Executors.newFixedThreadPool (1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentChangePasswordBinding.inflate (inflater, container, false);

        binding.btnChange.setOnClickListener (v -> {
            String email = Objects.requireNonNull (binding.etEmail.getText ()).toString ().trim ();
            String currentPassword = Objects.requireNonNull (binding.etCurrentPassword.getText ()).toString ().trim ();
            String newPassword = Objects.requireNonNull (binding.etNewPassword.getText ()).toString ().trim ();
            String reNewPassword = Objects.requireNonNull (binding.etReNewPassword.getText ()).toString ().trim ();

            if (checkErrorInput (email, currentPassword, newPassword, reNewPassword)) {
                reAuthenticate (email, currentPassword, newPassword);
            }
        });

        return binding.getRoot ();
    }

    @Override
    public void onDestroy() {
        super.onDestroy ();
        if (executorService != null)
            executorService.shutdown ();
    }

    private void reAuthenticate(String email, String currentPassword, String newPassword) {
        executorService.execute (() -> {
            FirebaseUser user = auth.getCurrentUser ();
            AuthCredential credential = EmailAuthProvider.getCredential (email, currentPassword);

            if (user != null) {
                user.reauthenticate (credential)
                        .addOnCompleteListener (task -> {
                            if (task.isSuccessful ()) {
                                changePassword (newPassword);
                            } else {
                                requireActivity ().runOnUiThread (() -> {
                                    String errorMessage = Objects.requireNonNull (task.getException ()).getMessage ();
                                    if (requireActivity () instanceof MainActivity)
                                        ((MainActivity) requireActivity ()).showErrorAlertDialog (errorMessage, context);
                                });
                            }
                        })
                        .addOnCanceledListener (() -> {
                            requireActivity ().runOnUiThread (() -> {
                                String errorMessage = "Email or Current Password invalid. Please authentication your account again later";
                                if (requireActivity () instanceof MainActivity)
                                    ((MainActivity) requireActivity ()).showErrorAlertDialog (errorMessage, context);
                            });
                });
            }
        });
    }
    private void changePassword(String password) {
        executorService.execute (() -> {
            FirebaseUser user = auth.getCurrentUser ();
            if (user != null) {
                user.updatePassword (password)
                        .addOnCompleteListener (task -> {
                            if (task.isSuccessful ()) {
                                requireActivity ().runOnUiThread (() -> {
                                    String successMessage = "Your password has been updated successfully";
                                    String toastMessage = "PASSWORD UPDATED";
                                    if (requireActivity () instanceof MainActivity)
                                        ((MainActivity) requireActivity ()).showSuccessAlertDialog (successMessage, context, toastMessage);
                                });
                            } else {
                                requireActivity ().runOnUiThread (() -> {
                                    String errorMessage = Objects.requireNonNull (task.getException ()).getMessage ();
                                    if (requireActivity () instanceof MainActivity)
                                        ((MainActivity) requireActivity ()).showErrorAlertDialog (errorMessage, context);
                                });
                            }
                        })
                        .addOnCanceledListener (() -> {
                            requireActivity ().runOnUiThread (() -> {
                                String errorMessage = "The Change Password task has been cancelled. Please Update Password again later";
                                if (requireActivity () instanceof MainActivity)
                                    ((MainActivity) requireActivity ()).showErrorAlertDialog (errorMessage, context);
                            });
                        });
            }
        });
    }
    private boolean checkErrorInput(String email, String currentPassword, String newPassword, String reNewPassword) {
        if (email.isEmpty ())
            binding.etEmail.setError ("Please enter your email");
        else if (!Patterns.EMAIL_ADDRESS.matcher (email).matches())
            binding.etEmail.setError ("Invalid email");
        else if (currentPassword.isEmpty ())
            binding.etCurrentPassword.setError ("Please enter your password");
        else if (currentPassword.length () < 6)
            binding.etCurrentPassword.setError ("Password must contain at least 6 characters");
        else if (!containsLetterAndDigit(currentPassword))
            binding.etCurrentPassword.setError ("Password must contain at least one letter or number");
        else if (newPassword.isEmpty ())
            binding.etNewPassword.setError ("Please enter your new password");
        else if (newPassword.length () < 6)
            binding.etNewPassword.setError ("New Password must contain at least 6 characters");
        else if (!containsLetterAndDigit(newPassword))
            binding.etNewPassword.setError ("New Password must contain at least one letter or number");
        else if (reNewPassword.isEmpty ())
            binding.etReNewPassword.setError ("Please re-enter your new password");
        else if (reNewPassword.length () < 6)
            binding.etReNewPassword.setError ("New Password must contain at least 6 characters");
        else if (!containsLetterAndDigit(reNewPassword))
            binding.etReNewPassword.setError ("New Password must contain at least one letter and number");
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

}