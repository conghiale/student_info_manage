package com.example.student_information_management.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.student_information_management.MainActivity;
import com.example.student_information_management.databinding.FragmentChangeEmailBinding;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChangeEmailFragment extends Fragment {
    private FragmentChangeEmailBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
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
        db = FirebaseFirestore.getInstance ();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentChangeEmailBinding.inflate (inflater, container, false);

        binding.btnChange.setOnClickListener (v -> {
            String email = Objects.requireNonNull (binding.etEmail.getText ()).toString ().trim ();
            String currentPassword = Objects.requireNonNull (binding.etCurrentPassword.getText ()).toString ().trim ();
            String newEmail = Objects.requireNonNull (binding.etNewEmail.getText ()).toString ().trim ();
            String reNewEmail = Objects.requireNonNull (binding.etReNewEmail.getText ()).toString ().trim ();

            if (checkErrorInput (email, currentPassword, newEmail, reNewEmail)) {
                reAuthenticate (email, currentPassword, newEmail);
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

    private void reAuthenticate(String email, String currentEmail, String newEmail) {
        executorService.execute (() -> {
            FirebaseUser user = auth.getCurrentUser ();
            AuthCredential credential = EmailAuthProvider.getCredential (email, currentEmail);

            if (user != null) {
                user.reauthenticate (credential)
                        .addOnCompleteListener (task -> {
                            if (task.isSuccessful ()) {
                                changeEmail (newEmail);
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
    private void changeEmail(String newEmail) {
        executorService.execute (() -> {
            FirebaseUser user = auth.getCurrentUser ();
            if (user != null) {
                user.verifyBeforeUpdateEmail (newEmail)
                        .addOnCompleteListener (task -> {
                            if (task.isSuccessful ()) {
                                updateEmailInFireStore (newEmail);
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
                                String errorMessage = "The Change Email task has been cancelled. Please Update Email again later";
                                if (requireActivity () instanceof MainActivity)
                                    ((MainActivity) requireActivity ()).showErrorAlertDialog (errorMessage, context);
                            });
                        });
            }
        });
    }

    private void updateEmailInFireStore (String newEmail) {
        executorService.execute (() -> {
            db.collection ("users")
                    .document (Objects.requireNonNull (auth.getUid ()))
                    .update ("email", newEmail)
                    .addOnSuccessListener (unused -> {
                        requireActivity ().runOnUiThread (() -> {
                            String successMessage = "Your email has been updated successfully";
                            String toastMessage = "EMAIL UPDATED";

                            if (requireActivity () instanceof MainActivity)
                                ((MainActivity) requireActivity ()).showSuccessAlertDialog (successMessage, context, toastMessage);
                        });
                    })
                    .addOnFailureListener (e -> {
                        String errorMessage = Objects.requireNonNull (e).getMessage ();
                        if (requireActivity () instanceof MainActivity)
                            ((MainActivity) requireActivity ()).showErrorAlertDialog (errorMessage, context);
                    })
                    .addOnCanceledListener (() -> {
                        requireActivity ().runOnUiThread (() -> {
                            String errorMessage = "The get Email of current user task has been cancelled. Please again later";
                            if (requireActivity () instanceof MainActivity)
                                ((MainActivity) requireActivity ()).showErrorAlertDialog (errorMessage, context);
                        });
                    });
        });
    }
    private boolean checkErrorInput(String email, String currentPassword, String newEmail, String reNewEmail) {

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
        else if (newEmail.isEmpty ())
            binding.etNewEmail.setError ("Please enter your new Email");
        else if (!Patterns.EMAIL_ADDRESS.matcher (newEmail).matches())
            binding.etNewEmail.setError ("Invalid new email");
        else if (reNewEmail.isEmpty ())
            binding.etReNewEmail.setError ("Please re-enter your new email");
        else if (!Patterns.EMAIL_ADDRESS.matcher (reNewEmail).matches())
            binding.etReNewEmail.setError ("Invalid new email");
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