package com.example.student_information_management.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.student_information_management.ui.activity.IntroActivity;
import com.example.student_information_management.MainActivity;
import com.example.student_information_management.databinding.FragmentHomeBinding;
import com.example.student_information_management.ui.adapter.UserAdapter;
import com.example.student_information_management.ui.viewModel.MyViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.student_information_management.ui.activity.UserActivity;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private Context context;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ExecutorService executorService;
    private MyViewModel model;
    private UserAdapter userAdapter;

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
            Intent intent = new Intent(v.getContext(), UserActivity.class);
            v.getContext().startActivity(intent);
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

    public void backUserList(){
        Intent intent = new Intent(this.getContext(), UserActivity.class);
        startActivity(intent);
    }
}