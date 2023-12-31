package com.example.student_information_management.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.student_information_management.MainActivity;
import com.example.student_information_management.databinding.FragmentCreateAccountBinding;
import com.example.student_information_management.ui.activity.ProvideInfoUserActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateAccountFragment extends Fragment {

    private FragmentCreateAccountBinding binding;
    private FirebaseAuth auth;
    private ExecutorService executorService;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        auth = FirebaseAuth.getInstance ();
        executorService = Executors.newFixedThreadPool (1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentCreateAccountBinding.inflate (inflater, container, false);

        binding.btnRegister.setOnClickListener (v -> {
            if (checkErrorInput ()) {
                String email = Objects.requireNonNull (binding.etEmail.getText ()).toString ().trim ();
                String password = Objects.requireNonNull (binding.etPassword.getText ()).toString ().trim ();
                registerAccountInBackground(email, password);
            }
        });
        return binding.getRoot ();
    }


    @Override
    public void onDestroy() {
        super.onDestroy ();
        if (executorService != null)
            executorService.shutdown();
    }

    private void registerAccountInBackground(String email, String password) {
        executorService.execute (() -> {
            auth.createUserWithEmailAndPassword (email, password)
                    .addOnCompleteListener (requireActivity (), task -> {
                        if (task.isSuccessful ()) {
                            requireActivity ().runOnUiThread (() -> {
                                binding.etEmail.setText ("");
                                binding.etPassword.setText ("");
                                binding.etRePassword.setText ("");

                                Intent intent = new Intent (getContext (), ProvideInfoUserActivity.class);
                                intent.putExtra ("EMAIL", email);
                                intent.putExtra ("PASSWORD", password);
                                startActivity (intent);
                            });
                        } else {
                            requireActivity ().runOnUiThread (() -> {
                                String errorMessage = Objects.requireNonNull (task.getException ()).getMessage ();
                                if (requireActivity () instanceof MainActivity)
                                    ((MainActivity) requireActivity ()).showErrorAlertDialog (errorMessage, requireContext ());
                            });
                        }
                    })
                    .addOnCanceledListener (() -> {
                        requireActivity ().runOnUiThread (() -> {
                            String errorMessage = "The Register task has been cancelled. Please Register again later";
                            if (requireActivity () instanceof MainActivity)
                                ((MainActivity) requireActivity ()).showErrorAlertDialog (errorMessage, requireContext ());
                        });
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
}