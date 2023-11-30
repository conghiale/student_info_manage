package com.example.student_information_management.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.student_information_management.MainActivity;
import com.example.student_information_management.R;
import com.example.student_information_management.data.model.User;
import com.example.student_information_management.data.repository.UserRepository;
import com.example.student_information_management.databinding.UserItemLayoutBinding;
import com.example.student_information_management.ui.activity.EditProfileActivity;
import com.example.student_information_management.ui.activity.UserActivity;
import com.example.student_information_management.ui.fragment.HomeFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;


public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private static List<User> userList;
    private Context context;
    private FirebaseAuth auth;
    private String currentEmail;
    private String currentPassword;
    public UserAdapter(List<User> userList) {
        this.userList = userList;
        this.auth = FirebaseAuth.getInstance();
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        UserItemLayoutBinding itemBinding = UserItemLayoutBinding.inflate(layoutInflater, parent, false);
        return new UserViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user, position);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private UserItemLayoutBinding binding;

        UserViewHolder(UserItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(User user, int position) {
            binding.tvName.setText(user.getName());
            binding.tvEmail.setText(user.getEmail());
            binding.tvPhone.setText(user.getPhoneNumber());
            binding.tvAge.setText(String.valueOf(user.getAge()));
            binding.tvRole.setText(user.getRole());
            binding.tvStatus.setText(user.getStatus());
            // Set image,  binding.ivProfile

            //Check role
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            db.collection("users").document(currentUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        DocumentSnapshot document = task.getResult();
                        String role = document.getString("role");
                        currentEmail = document.getString("email");
                        currentPassword = document.getString("password");
                        if ("Employee".equals(role)) {
                            binding.ivMenu.setVisibility(View.INVISIBLE);
                        } else {
                            binding.ivMenu.setVisibility(View.VISIBLE);
                        }
                    }
                });

            binding.ivMenu.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(v.getContext(), binding.ivMenu);
                popupMenu.getMenuInflater().inflate(R.menu.user_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.menu_edit) {
                        Intent intent = new Intent(context, EditProfileActivity.class);
                        intent.putExtra ("uid", user.getUid());
                        intent.putExtra ("name", user.getName());
                        intent.putExtra ("password", user.getPassword());
                        intent.putExtra ("email", user.getEmail());
                        intent.putExtra ("age", String.valueOf(user.getAge()));
                        intent.putExtra ("phone", user.getPhoneNumber());
                        intent.putExtra ("status", user.getStatus());
                        intent.putExtra ("role", user.getRole());
                        context.startActivity(intent);
                        return true;
                    } else if (itemId == R.id.menu_delete) {
                        auth.signInWithEmailAndPassword(user.getEmail(), user.getPassword())
                                .addOnSuccessListener(authResult -> {
                                    auth.getCurrentUser().delete()
                                        .addOnSuccessListener(result -> {
                                            auth.signInWithEmailAndPassword(currentEmail, currentPassword);
                                        });
                                });

                        UserRepository userRepository = new UserRepository();
                        userRepository.deleteUser(user, task -> {
                            if (task.isSuccessful()) {
                                //update user list
                                userList.remove(position);
                                notifyItemRemoved(position);
                            }
                        });
                        return true;
                    } else {
                        return false;
                    }
                });
                popupMenu.show();
            });
        }
    }
}
