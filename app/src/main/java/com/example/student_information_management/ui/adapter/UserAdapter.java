package com.example.student_information_management.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.student_information_management.data.model.User;
import com.example.student_information_management.databinding.UserItemLayoutBinding;

import java.util.List;


public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;

    public UserAdapter(List<User> userList) {
        this.userList = userList;
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
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private UserItemLayoutBinding binding;

        UserViewHolder(UserItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(User user) {
            binding.tvName.setText(user.getName());
            binding.tvEmail.setText(user.getEmail());
            binding.tvPhone.setText(user.getPhoneNumber());
            binding.tvAge.setText(String.valueOf(user.getAge()));
            binding.tvRole.setText(user.getRole());
            binding.tvStatus.setText(user.getStatus());
            // Set image,  binding.ivProfile
        }
    }
}
