package com.example.student_information_management.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.student_information_management.IntroActivity;
import com.example.student_information_management.MainActivity;
import com.example.student_information_management.R;
import com.example.student_information_management.databinding.FragmentHomeBinding;
import com.example.student_information_management.model.User;
import com.example.student_information_management.view_model.MyViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private Context context;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ExecutorService executorService;
    private MyViewModel model;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach (context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        auth = FirebaseAuth.getInstance ();
        db = FirebaseFirestore.getInstance ();
        executorService = Executors.newFixedThreadPool (1);
        model = new ViewModelProvider (requireActivity ()).get (MyViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

//        Inflate the layout for this fragment
//        View view = inflater.inflate (R.layout.fragment_home, container, false);
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        model.getCurrentUser ().observe (getViewLifecycleOwner (), user -> {
            if (user != null) {
                binding.tvUserName.setText (user.getName ());
                binding.tvUserEmail.setText (user.getEmail ());

                if (!user.getRole ().equals ("Admin")) {
                    binding.containerCreateAccount.setVisibility (View.GONE);
                }
            }
        });

        binding.tvHistoryUser.setOnClickListener (v -> {

        });

        binding.tvProfilePicture.setOnClickListener (v -> {

        });

        binding.tvUserList.setOnClickListener (v -> {

        });

        binding.tvStudentList.setOnClickListener (v -> {

        });

        binding.tvChangePassword.setOnClickListener (v -> {
            if (requireActivity () instanceof MainActivity) {
                ((MainActivity) requireActivity ()).replaceFragment (new ChangePasswordFragment ());
            }
        });

        binding.tvChangeEmail.setOnClickListener (v -> {
            if (requireActivity () instanceof MainActivity) {
                ((MainActivity) requireActivity ()).replaceFragment (new ChangeEmailFragment ());
            }
        });

        binding.tvCreateAccount.setOnClickListener (v -> {
            if (requireActivity () instanceof MainActivity) {
                ((MainActivity) requireActivity ()).replaceFragment (new CreateAccountFragment ());
            }
        });

        binding.tvLogout.setOnClickListener (v -> {
            auth.signOut ();
            startActivity (new Intent (requireContext (), IntroActivity.class));
            requireActivity ().finish ();
        });

        return binding.getRoot ();
    }
}