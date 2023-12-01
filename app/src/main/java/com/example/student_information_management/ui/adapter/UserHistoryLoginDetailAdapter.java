package com.example.student_information_management.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.student_information_management.databinding.UserHistoryLoginItemDetailBinding;
import com.google.firebase.Timestamp;

import java.util.List;

public class UserHistoryLoginDetailAdapter extends RecyclerView.Adapter<UserHistoryLoginDetailAdapter.UserHLDetailViewHolder> {

    private final Context context;
    private final List<Timestamp> historyLoginDetails;

    public UserHistoryLoginDetailAdapter(Context context, List<Timestamp> historyLoginDetails) {
        this.context = context;
        this.historyLoginDetails = historyLoginDetails;
    }

    @NonNull
    @Override
    public UserHLDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        UserHistoryLoginItemDetailBinding itemBinding = UserHistoryLoginItemDetailBinding.inflate(layoutInflater, parent, false);
        return new UserHLDetailViewHolder (itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserHLDetailViewHolder holder, int position) {
        Timestamp historyLogin = historyLoginDetails.get (position);
        holder.bind (position, historyLogin.toDate ().toString ());
    }

    @Override
    public int getItemCount() {
        return (historyLoginDetails != null) ? historyLoginDetails.size () : 0;
    }

    public static final class UserHLDetailViewHolder extends RecyclerView.ViewHolder {

        private final UserHistoryLoginItemDetailBinding binding;

        public UserHLDetailViewHolder(UserHistoryLoginItemDetailBinding binding) {
            super (binding.getRoot ());
            this.binding = binding;
        }

        public void bind(int stt, String historyLoginDetail) {
            binding.tvSTT.setText (String.valueOf (stt));
            binding.tvTimeLogin.setText (historyLoginDetail);
        }
    }
}
