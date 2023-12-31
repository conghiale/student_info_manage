package com.example.student_information_management.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.student_information_management.data.model.User;

public class MyViewModel extends ViewModel {
    private final MutableLiveData<User> currentUser;
    public MyViewModel() {
        currentUser = new MutableLiveData<> ();
    }
    public LiveData<User> getCurrentUser() {
        return this.currentUser;
    }
    public void setCurrentUser(User user) {
        this.currentUser.postValue (user);
    }
}
