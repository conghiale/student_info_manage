package com.example.student_information_management.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.student_information_management.MainActivity;
import com.example.student_information_management.R;
import com.example.student_information_management.data.model.User;
import com.example.student_information_management.databinding.ActivityProvideInfoUserBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class ProvideInfoUserActivity extends AppCompatActivity {

    private ActivityProvideInfoUserBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ExecutorService executorService;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        binding = ActivityProvideInfoUserBinding.inflate (getLayoutInflater ());
        setContentView (binding.getRoot ());

        db = FirebaseFirestore.getInstance ();
        auth = FirebaseAuth.getInstance ();
        executorService = Executors.newFixedThreadPool (1);

        init();
    }

    private void init() {
        String[] statuses = getResources ().getStringArray (R.array.status);
        ArrayAdapter<String> adapterStatus = new ArrayAdapter<>(this, R.layout.dropdown_item, statuses);
        binding.status.setAdapter (adapterStatus);

        String[] roles = getResources ().getStringArray (R.array.role);
        ArrayAdapter<String> adapterRole = new ArrayAdapter<>(this, R.layout.dropdown_item, roles);
        binding.role.setAdapter (adapterRole);

        binding.btnSave.setOnClickListener (v -> {
            String uid = Objects.requireNonNull (FirebaseAuth.getInstance ().getCurrentUser ()).getUid ();
            String name = Objects.requireNonNull (binding.etName.getText ()).toString ().trim ();
            String age = Objects.requireNonNull (binding.etAge.getText ()).toString ().trim ();
            String phoneNumber = Objects.requireNonNull (binding.etPhoneNumber.getText ()).toString ().trim ();
            String status = Objects.requireNonNull (binding.status.getText ()).toString ().trim ();
            String role = Objects.requireNonNull (binding.role.getText ()).toString ().trim ();

            Intent intent = getIntent ();
            if (intent != null) {
                String email = intent.getStringExtra ("EMAIL");
                String password = intent.getStringExtra ("PASSWORD");
                if (email != null && password != null && checkErrorInput (name, age, phoneNumber, status)) {
                    User user = new User(uid);
                    user.setName(name);
                    user.setAge(Integer.parseInt(age));
                    user.setPhoneNumber(phoneNumber);
                    user.setStatus(status);
                    user.setRole(role);
                    user.setEmail(email);
                    user.setPassword(password);
                    user.setAvatar("https://firebasestorage.googleapis.com/v0/b/student-info-management-3547f.appspot.com/o/5b204239-6154-4c8a-bc7f-3418993af028.jpg?alt=media&token=b3d1fbc9-8b45-44c4-9a6d-034770bd14c9");
                    providerInfoUserInBackground (user);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy ();
        if (executorService != null)
            executorService.shutdown ();
    }

    private void providerInfoUserInBackground(User user) {
        executorService.execute (() -> {
            String id = Objects.requireNonNull (FirebaseAuth.getInstance ().getCurrentUser ()).getUid ();

            db.collection ("users")
                    .document (id)
                    .set (user)
                    .addOnSuccessListener (unused -> {
                        reLogin (user.getName (), user.getUid ());
                    })
                    .addOnFailureListener (e ->     {
                        runOnUiThread (() -> {
                            String errorMessage = e.getMessage ();
                            showErrorAlertDialog(errorMessage);
                        });
                    })
                    .addOnCanceledListener (() -> {
                        runOnUiThread (() -> {
                            showErrorAlertDialog ("The create info user task has been cancelled. Please Register again later");
                        });
                    });
        });
    }

    private void reLogin(String name, String id) {
        AtomicReference<String> email = new AtomicReference<> ();
        AtomicReference<String> password = new AtomicReference<> ();

        MainActivity.model.getCurrentUser ().observe (this, user -> {
            email.set (user.getEmail ());
            password.set (user.getPassword ());
        });

        executorService.execute (() -> {
            auth.signInWithEmailAndPassword (email.get (), password.get ())
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            runOnUiThread (() -> {
                                FirebaseUser user = auth.getCurrentUser();
                                if (user != null) {
                                    Toast.makeText (this, "User " + name +" added with \nID: " + id, Toast.LENGTH_LONG).show ();
                                    startActivity (new Intent (this, MainActivity.class));
                                    finish ();
                                }
                            });
                        } else {
                            runOnUiThread (() -> {
                                String errorMessage = Objects.requireNonNull (task.getException ()).getMessage ();
                                showErrorAlertDialog(errorMessage);
                            });
                        }
                    }).addOnCanceledListener (() ->
                            runOnUiThread (() -> {
                                showErrorAlertDialog("The system is maintenance. The Login task has been cancelled. Please Login again later");
                            })
                    );
        });
    }

    private boolean checkErrorInput(String name, String age, String phoneNumber, String status) {
        if (name.isEmpty ())
            binding.etName.setError ("Please enter user name");
        else if (age.isEmpty ())
            binding.etAge.setError ("Please enter user age");
        else if (!isDigit (age))
            binding.etAge.setError ("Age must be a number");
        else if (phoneNumber.isEmpty ())
            binding.etPhoneNumber.setError ("Please enter user phone number");
        else if (!isDigit(phoneNumber))
            binding.etPhoneNumber.setError ("Phone number must be a number");
        else if (status.isEmpty ())
            binding.status.setError ("Please enter user status");
        else if (!binding.cbVerify.isChecked ()) {
            binding.cbVerify.setError ("Please verify that all the above information is correct");
        } else
            return true;
        return false;
    }
    private boolean isDigit(String str) {
        boolean hasDigit = true;

        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                hasDigit = false;
            }
        }

        return hasDigit;
    }
    @SuppressLint("SetTextI18n")
    private void showErrorAlertDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder (ProvideInfoUserActivity.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from (ProvideInfoUserActivity.this).inflate (
                R.layout.layout_error_dialog, (ConstraintLayout)findViewById (R.id.layoutDialogContainer));
        builder.setView (view);

        ((TextView) view.findViewById (R.id.tvTitle)).setText ("Error");
        ((TextView) view.findViewById (R.id.tvMessage)).setText (message);
        ((AppCompatButton) view.findViewById (R.id.btnAction)).setText ("Okay");
        ((ImageView) view.findViewById (R.id.ivImageIcon)).setImageResource (R.drawable.baseline_error);

        final AlertDialog alertDialog = builder.create ();
        view.findViewById (R.id.btnAction).setOnClickListener (v -> {
            alertDialog.dismiss ();
        });

        if (alertDialog.getWindow () != null)
            alertDialog.getWindow ().setBackgroundDrawable (new ColorDrawable (0));
        alertDialog.show ();
        alertDialog.setCancelable(false);
    }
}
