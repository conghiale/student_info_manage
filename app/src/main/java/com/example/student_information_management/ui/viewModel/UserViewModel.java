package com.example.student_information_management.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.student_information_management.data.model.User;
import com.example.student_information_management.data.repository.UserRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserViewModel extends ViewModel {
    private MutableLiveData<List<User>> users = new MutableLiveData<>();
    private UserRepository userRepository = new UserRepository();

    public LiveData<List<User>> getUsers() {
        loadUsers();
        return users;
    }

    public void loadUsers() {
        userRepository.getUsers(users);
    }
}


