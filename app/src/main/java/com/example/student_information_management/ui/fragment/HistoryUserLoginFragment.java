package com.example.student_information_management.ui.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.student_information_management.MainActivity;
import com.example.student_information_management.R;
import com.example.student_information_management.data.model.User;
import com.example.student_information_management.databinding.FragmentHistoryUserLoginBinding;
import com.example.student_information_management.ui.adapter.UserHistoryLoginAdapter;
import com.example.student_information_management.ui.viewModel.MyViewModel;
import com.example.student_information_management.ui.viewModel.UserViewModel;

import java.util.ArrayList;
import java.util.List;

public class HistoryUserLoginFragment extends Fragment {
    private FragmentHistoryUserLoginBinding binding;
    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHistoryUserLoginBinding.inflate (inflater, container, false);

        UserHistoryLoginAdapter userHistoryLoginAdapter = new UserHistoryLoginAdapter (requireContext (), new ArrayList<> ());

        binding.rcvUsers.setHasFixedSize (true);
        binding.rcvUsers.setLayoutManager (new LinearLayoutManager (requireContext (), LinearLayoutManager.VERTICAL, false));
        binding.rcvUsers.addItemDecoration (new DividerItemDecoration (requireContext (), DividerItemDecoration.VERTICAL));
        binding.rcvUsers.setAdapter (userHistoryLoginAdapter);

        MainActivity.model.getCurrentUser ().observe (getViewLifecycleOwner (), user -> {
            if (user.getRole ().equals ("Admin")) {
                UserViewModel model = new ViewModelProvider (this).get (UserViewModel.class);
                model.getUsers ().observe (getViewLifecycleOwner (), users -> {
                    userHistoryLoginAdapter.setUsers (users);
                    userHistoryLoginAdapter.notifyDataSetChanged ();
                });
            } else {
                List<User> users = new ArrayList<> ();
                users.add (user);
                userHistoryLoginAdapter.setUsers (users);
                userHistoryLoginAdapter.notifyDataSetChanged ();
            }
        });

        return binding.getRoot ();
    }
}