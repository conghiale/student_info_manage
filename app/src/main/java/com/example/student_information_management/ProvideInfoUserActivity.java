package com.example.student_information_management;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.student_information_management.databinding.ActivityLoginBinding;
import com.example.student_information_management.databinding.ActivityProvideInfoUserBinding;
import com.example.student_information_management.model.User;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.SuccessContinuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProvideInfoUserActivity extends AppCompatActivity {

    private ActivityProvideInfoUserBinding binding;
    private FirebaseFirestore db;
    private ExecutorService executorService;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        binding = ActivityProvideInfoUserBinding.inflate (getLayoutInflater ());
        setContentView (binding.getRoot ());

        db = FirebaseFirestore.getInstance ();
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
            String email = "";
            if (intent != null)
                email = intent.getStringExtra ("EMAIL");

            if (email != null && checkErrorInput (name, age, phoneNumber, status)) {
                int intAge = Integer.parseInt (age);
                providerInfoUserInBackground (new User (uid, email, name, intAge, phoneNumber, status, role));
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
//            Map<String, Object> user = new HashMap<> ();
//            user.put ("email", email);
//            user.put ("name", name);
//            user.put ("age", age);
//            user.put ("phoneNumber", phoneNumber);
//            user.put ("status", status);

            String id = Objects.requireNonNull (FirebaseAuth.getInstance ().getCurrentUser ()).getUid ();

            db.collection ("users")
                    .document (id)
                    .set (user)
                    .addOnSuccessListener (unused -> {
                        runOnUiThread (() -> {
                            FirebaseAuth.getInstance ().signOut ();
                            Toast.makeText (this, "User " + user.getName () +" added with \nID: " + id, Toast.LENGTH_LONG).show ();
                            startActivity (new Intent (this, LoginActivity.class));
                            finish ();
                        });
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
