package com.example.student_information_management.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.student_information_management.data.model.User;
import com.example.student_information_management.databinding.UserHistoryLoginItemBinding;

import java.util.List;

public class UserHistoryLoginAdapter extends RecyclerView.Adapter<UserHistoryLoginAdapter.UserHLViewHolder> {

    private final Context context;
    private List<User> users;

    public UserHistoryLoginAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public UserHLViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        UserHistoryLoginItemBinding itemBinding = UserHistoryLoginItemBinding.inflate(layoutInflater, parent, false);
        return new UserHLViewHolder (itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserHLViewHolder holder, int position) {
        User user = users.get (position);
        holder.binding.tvUserName.setText (user.getName ());

        UserHistoryLoginDetailAdapter userHistoryLoginDetailAdapter = new UserHistoryLoginDetailAdapter (context, user.getLoginHistory ());

        holder.binding.rcvHistoryLoginUser.setHasFixedSize (true);
        holder.binding.rcvHistoryLoginUser.setLayoutManager (new LinearLayoutManager (context, LinearLayoutManager.VERTICAL, false));
        holder.binding.rcvHistoryLoginUser.addItemDecoration (new DividerItemDecoration (context, DividerItemDecoration.VERTICAL));
        holder.binding.rcvHistoryLoginUser.setAdapter (userHistoryLoginDetailAdapter);
    }

    @Override
    public int getItemCount() {
        return (users != null) ? users.size () : 0;
    }

    public void setUsers(List<User> users) {
        this.users.addAll (users);
    }

    public static final class UserHLViewHolder extends RecyclerView.ViewHolder {
        private final UserHistoryLoginItemBinding binding;
        public UserHLViewHolder(UserHistoryLoginItemBinding binding) {
            super (binding.getRoot ());
            this.binding = binding;
        }
    }
}
