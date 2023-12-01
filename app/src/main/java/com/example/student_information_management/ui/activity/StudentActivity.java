package com.example.student_information_management.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.student_information_management.R;
import com.example.student_information_management.data.model.Student;
import com.example.student_information_management.data.repository.StudentRepository;
import com.example.student_information_management.databinding.ActivityStudentBinding;
import com.example.student_information_management.ui.adapter.StudentAdapter;
import com.example.student_information_management.ui.viewModel.StudentViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class StudentActivity extends AppCompatActivity {
    private ActivityStudentBinding binding;
    private StudentViewModel viewModel;
    private StudentAdapter adapter;
    private StudentRepository studentRepository;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(StudentViewModel.class);

        RecyclerView rcvStudent = binding.rcvStudent;
        rcvStudent.setLayoutManager(new LinearLayoutManager(this));

        viewModel.getStudents().observe(this, students -> {
            adapter = new StudentAdapter(students);
            adapter.setContext(this);
            binding.rcvStudent.setAdapter(adapter);
        });

        binding.ivAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddStudentActivity.class);
            startActivity(intent);
        });

        binding.ivBack.setOnClickListener(v -> finish());

        binding.searchStudent.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });

        binding.ivSort.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), binding.ivSort);
            popupMenu.getMenuInflater().inflate(R.menu.sort_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.name) {
                    adapter.sortStudents("name");
                    return true;
                } else if (itemId == R.id.email) {
                    adapter.sortStudents("email");
                    return true;
                } else if (itemId == R.id.studentClass) {
                    adapter.sortStudents("class");
                    return true;
                } else if (itemId == R.id.gpa) {
                    adapter.sortStudents("gpa");
                    return true;
                } else {
                    return false;
                }
            });
            popupMenu.show();
        });

        binding.ivImEx.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), binding.ivImEx);
            popupMenu.getMenuInflater().inflate(R.menu.import_export_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.importStudent) {
                    importStudentsFromCsv();
                    return true;
                } else if (itemId == R.id.exportStudent) {
                    exportStudentsToFile();
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
                            binding.ivAdd.setVisibility(View.INVISIBLE);
                            binding.ivImEx.setVisibility(View.INVISIBLE);
                        } else {
                            binding.ivAdd.setVisibility(View.VISIBLE);
                            binding.ivImEx.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

//    public void pickFile() {
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        intent.setType("*/*");
//        startActivityForResult(intent, 1);
//    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
//            Uri fileUri = data.getData();
//            importStudentsFromCsv(fileUri);
//        }
//    }

    public void importStudentsFromCsv() {
        try {
            InputStream inputStream = getAssets().open("importStudent.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                // format: name, email, mobile, class, gpa
                String[] tokens = line.split(",");
                Student student = new Student(tokens[0], tokens[1], tokens[2], tokens[3], tokens[4], Double.parseDouble(tokens[5]));

                studentRepository.addStudent(student, documentReference -> {
                    Toast.makeText(this, "Import Successful", Toast.LENGTH_SHORT).show();
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Import Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void exportStudentsToFile() {
        String fileName = "exportedStudents.csv";
        File file = new File(getFilesDir(), fileName);
//        Log.e("getFilesDir", getFilesDir().toString());

        try (FileWriter fileWriter = new FileWriter(file);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {

            List<Student> students = getStudentList();

            for (Student student : students) {
                Log.e("Student", student.getName().toString());
                String line = student.getName() + "," + student.getEmail() + "," + student.getMobile()
                        + "," + student.getStudentClass() + "," + student.getGpa() + "\n";
                bufferedWriter.write(line);
            }

            Toast.makeText(this, "Export Successful", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Export Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private List<Student> getStudentList() {
        db = FirebaseFirestore.getInstance();
        List<Student> studentList = new ArrayList<>();
        db.collection("students")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Student student = documentSnapshot.toObject(Student.class);
                        studentList.add(student);
                    }
                });
        return studentList;
    }


}