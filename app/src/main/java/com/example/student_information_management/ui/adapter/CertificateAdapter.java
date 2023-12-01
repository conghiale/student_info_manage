package com.example.student_information_management.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.recyclerview.widget.RecyclerView;

import com.example.student_information_management.R;
import com.example.student_information_management.data.model.Certificate;
import com.example.student_information_management.data.repository.CertificateRepository;
import com.example.student_information_management.databinding.CertificateItemLayoutBinding;
import com.example.student_information_management.ui.activity.EditCertificateActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CertificateAdapter extends RecyclerView.Adapter<CertificateAdapter.CertificateViewHolder> {

    private static List<Certificate> certificateList;
    private Context context;

    public CertificateAdapter(List<Certificate> certificateList) {
        CertificateAdapter.certificateList = certificateList;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public CertificateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        CertificateItemLayoutBinding itemBinding = CertificateItemLayoutBinding.inflate(layoutInflater, parent, false);
        return new CertificateViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull CertificateViewHolder holder, int position) {
        Certificate certificate = certificateList.get(position);
        holder.bind(certificate, position);
    }

    @Override
    public int getItemCount() {
        return certificateList.size();
    }

    class CertificateViewHolder extends RecyclerView.ViewHolder {
        private CertificateItemLayoutBinding binding;

        CertificateViewHolder(CertificateItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Certificate certificate, int position) {
            binding.tvName.setText(certificate.getName());
            binding.tvDate.setText(certificate.getDateOfExam());
            binding.tvResult.setText(certificate.getResult());

            binding.ivMenu.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(v.getContext(), binding.ivMenu);
                popupMenu.getMenuInflater().inflate(R.menu.user_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.menu_edit) {
                        Intent intent = new Intent(context, EditCertificateActivity.class);
                        intent.putExtra("studentId", certificate.getIdStudent());
                        intent.putExtra("id", certificate.getId());
                        intent.putExtra("name", certificate.getName());
                        intent.putExtra("dateOfExam", certificate.getDateOfExam());
                        intent.putExtra("result", certificate.getResult());
                        context.startActivity(intent);
                        return true;
                    } else if (itemId == R.id.menu_delete) {
                        CertificateRepository certificateRepository = new CertificateRepository();
                        certificateRepository.deleteCertificate(certificate, task -> {
                            if (task.isSuccessful()) {
                                certificateList.remove(position);
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

            //Check role
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            db.collection("users").document(currentUser.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                            DocumentSnapshot document = task.getResult();
                            String role = document.getString("role");
                            if ("Employee".equals(role)) {
                                binding.ivMenu.setVisibility(View.INVISIBLE);
                            } else {
                                binding.ivMenu.setVisibility(View.VISIBLE);
                            }
                        }
                    });
        }


    }
}
