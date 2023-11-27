package com.example.student_information_management.view_model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.student_information_management.model.User;

public class MyViewModel extends ViewModel {

    private final MutableLiveData<String> userEmailRegister;
    private final MutableLiveData<User> currentUser;

    public MyViewModel() {
        userEmailRegister = new MutableLiveData<> ();
        currentUser = new MutableLiveData<> ();
    }

    public LiveData<String> getUserEmailRegister() {
        return this.userEmailRegister;
    }
    public LiveData<User> getCurrentUser() {
        return this.currentUser;
    }

    public void setUserEmailRegister(String email) {
        this.userEmailRegister.postValue (email);
    }
    public void setCurrentUser(User user) {
        this.currentUser.postValue (user);
    }
}
