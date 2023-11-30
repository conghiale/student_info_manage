package com.example.student_information_management.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.student_information_management.R;
import com.example.student_information_management.databinding.ActivityUserBinding;
import com.example.student_information_management.ui.adapter.UserAdapter;
import com.example.student_information_management.ui.viewModel.UserViewModel;

public class UserActivity extends AppCompatActivity {

    private ActivityUserBinding binding;
    private UserViewModel viewModel;
    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(UserViewModel.class);


        RecyclerView rcvUser = binding.rcvUser;
        rcvUser.setLayoutManager(new LinearLayoutManager(this));

        viewModel.getUsers().observe(this, users -> {
            adapter = new UserAdapter(users);
            adapter.setContext(this);
            binding.rcvUser.setAdapter(adapter);
        });

        binding.ivBack.setOnClickListener(v -> {
            finish();
        });
    }

}