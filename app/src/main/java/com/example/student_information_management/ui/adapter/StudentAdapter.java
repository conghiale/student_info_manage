package com.example.student_information_management.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.student_information_management.R;
import com.example.student_information_management.data.model.Student;
import com.example.student_information_management.data.repository.StudentRepository;
import com.example.student_information_management.databinding.StudentItemLayoutBinding;
import com.example.student_information_management.ui.activity.EditStudentActivity;
import com.example.student_information_management.ui.activity.StudentDetailActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> implements Filterable {

    private static List<Student> studentList;
    private List<Student> studentListFull;
    private Context context;
    public StudentAdapter(List<Student> studentList) {
        this.studentList = studentList;
        this.studentListFull = new ArrayList<>(studentList);
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        StudentItemLayoutBinding itemBinding = StudentItemLayoutBinding.inflate(layoutInflater, parent, false);
        return new StudentViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = studentList.get(position);
        holder.bind(student, position);
        holder.itemView.setOnClickListener(v -> {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.scale_animation);
            holder.itemView.startAnimation(animation);
            Intent intent = new Intent(context, StudentDetailActivity.class);
            intent.putExtra("idStudent", student.getId());
            intent.putExtra("name", student.getName());
            intent.putExtra("email", student.getEmail());
            intent.putExtra("studentClass", student.getStudentClass());
            intent.putExtra("phone", student.getMobile());
            intent.putExtra("gpa", String.valueOf(student.getGpa()));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    @Override
    public Filter getFilter() {
        return studentFilter;
    }
    private Filter studentFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Student> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(studentListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Student item : studentListFull) {
                    if (item.getName().toLowerCase().contains(filterPattern) ||
                            item.getEmail().toLowerCase().contains(filterPattern) ||
                            item.getMobile().toLowerCase().contains(filterPattern) ||
                            item.getStudentClass().toLowerCase().contains(filterPattern) ||
                            Double.toString(item.getGpa()).contains(filterPattern) ||
                            item.getId().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            studentList.clear();
            studentList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public void sortStudents(String sortBy) {
        Comparator<Student> comparator = null;

        switch (sortBy) {
            case "name":
                comparator = (s1, s2) -> s1.getName().compareToIgnoreCase(s2.getName());
                break;
            case "email":
                comparator = (s1, s2) -> s1.getEmail().compareToIgnoreCase(s2.getEmail());
                break;
            case "gpa":
                comparator = Comparator.comparingDouble(Student::getGpa);
                break;
            case "class":
                comparator = (s1, s2) -> s1.getStudentClass().compareToIgnoreCase(s2.getStudentClass());
                break;
        }

        if (comparator != null) {
            Collections.sort(studentList, comparator);
            studentListFull = new ArrayList<>(studentList);
            notifyDataSetChanged();
        }
    }

    class StudentViewHolder extends RecyclerView.ViewHolder {
        private StudentItemLayoutBinding binding;

        StudentViewHolder(StudentItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Student student, int position) {
            binding.tvName.setText(student.getName());
            binding.tvEmail.setText(student.getEmail());
            binding.tvClass.setText(student.getStudentClass());
            binding.tvGpa.setText(String.valueOf(student.getGpa()));
            binding.tvPhone.setText(student.getMobile());

            binding.ivMenu.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(v.getContext(), binding.ivMenu);
                popupMenu.getMenuInflater().inflate(R.menu.user_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.menu_edit) {
                        Intent intent = new Intent(context, EditStudentActivity.class);
                        intent.putExtra("id", student.getId());
                        intent.putExtra("name", student.getName());
                        intent.putExtra("email", student.getEmail());
                        intent.putExtra("studentClass", student.getStudentClass());
                        intent.putExtra("phone", student.getMobile());
                        intent.putExtra("gpa", String.valueOf(student.getGpa()));
                        context.startActivity(intent);
                        return true;
                    } else if (itemId == R.id.menu_delete) {
                        StudentRepository studentRepository = new StudentRepository();
                        studentRepository.deleteStudent(student, task -> {
                            if (task.isSuccessful()) {
                                studentList.remove(position);
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


