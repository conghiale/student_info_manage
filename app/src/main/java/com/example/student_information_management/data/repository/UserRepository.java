package com.example.student_information_management.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.example.student_information_management.data.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserRepository {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    public void getUsers(MutableLiveData<List<User>> usersLiveData) {
        db.collection("users")
                .whereNotEqualTo("role", "Admin")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> userList = new ArrayList<>();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        User user = documentSnapshot.toObject(User.class);

                        userList.add(user);
                    }
                    usersLiveData.setValue(userList);
                });
    }
    public void deleteUser(User user, OnCompleteListener<Void> onCompleteListener) {
        db.collection("users").document(user.getUid())
                .delete()
                .addOnCompleteListener(onCompleteListener);
    }

    public void editUser(String userId, Map<String, Object> updatedUser, OnCompleteListener<Void> onCompleteListener) {
        db.collection("users").document(userId)
                .update(updatedUser)
                .addOnCompleteListener(onCompleteListener);
    }
}

