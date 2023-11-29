package com.example.student_information_management.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.example.student_information_management.data.model.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    public void getUsers(MutableLiveData<List<User>> usersLiveData, MutableLiveData<Exception> errorLiveData) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> userList = new ArrayList<>();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        User user = documentSnapshot.toObject(User.class);

                        userList.add(user);
                    }
                    usersLiveData.setValue(userList);
                })
                //Method Reference
                .addOnFailureListener(errorLiveData::setValue);
    }
}

