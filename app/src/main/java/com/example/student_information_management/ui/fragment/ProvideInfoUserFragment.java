package com.example.student_information_management.ui.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.student_information_management.MainActivity;
import com.example.student_information_management.R;
import com.example.student_information_management.databinding.FragmentProvideInfoUserBinding;
import com.example.student_information_management.data.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProvideInfoUserFragment extends Fragment {

    private FragmentProvideInfoUserBinding binding;
    private FirebaseFirestore db;
    private ExecutorService executorService;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        db = FirebaseFirestore.getInstance ();
        executorService = Executors.newFixedThreadPool (1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProvideInfoUserBinding.inflate (inflater, container, false);

        init();
        return binding.getRoot ();
    }
    @Override
    public void onDestroy() {
        super.onDestroy ();
        if (executorService != null)
            executorService.shutdown ();
    }
    private void init() {
        String[] statuses = getResources ().getStringArray (R.array.status);
        ArrayAdapter<String> adapterStatus = new ArrayAdapter<>(requireContext (), R.layout.dropdown_item, statuses);
        binding.status.setAdapter (adapterStatus);

        String[] roles = getResources ().getStringArray (R.array.role);
        ArrayAdapter<String> adapterRole = new ArrayAdapter<>(requireContext (), R.layout.dropdown_item, roles);
        binding.role.setAdapter (adapterRole);

        binding.btnSave.setOnClickListener (v -> {
            String uid = Objects.requireNonNull (FirebaseAuth.getInstance ().getCurrentUser ()).getUid ();
            String name = Objects.requireNonNull (binding.etName.getText ()).toString ().trim ();
            String age = Objects.requireNonNull (binding.etAge.getText ()).toString ().trim ();
            String phoneNumber = Objects.requireNonNull (binding.etPhoneNumber.getText ()).toString ().trim ();
            String status = Objects.requireNonNull (binding.status.getText ()).toString ().trim ();
            String role = Objects.requireNonNull (binding.role.getText ()).toString ().trim ();

//            AtomicReference<String> email = new AtomicReference<> ();
//            MyViewModel model = new ViewModelProvider (requireActivity ()).get (MyViewModel.class);
//            model.getCurrentPassword ().observe (getViewLifecycleOwner (), email::set);
//
//            if (email.get () != null && checkErrorInput (name, age, phoneNumber, status)) {
//                int intAge = Integer.parseInt (age);
//                providerInfoUserInBackground (new User (uid, email.get (), name, intAge, phoneNumber, status, role));
//            }
        });
    }
    private void providerInfoUserInBackground(User user) {
        executorService.execute (() -> {
            String id = Objects.requireNonNull (FirebaseAuth.getInstance ().getCurrentUser ()).getUid ();

            db.collection ("users")
                    .document (id)
                    .set (user)
                    .addOnSuccessListener (unused -> {
                        requireActivity ().runOnUiThread (() -> {
                            FirebaseAuth.getInstance ().signOut ();
                            Toast.makeText (requireActivity (), "User " + user.getName () +" created with \nID: " + id, Toast.LENGTH_LONG).show ();
                            if (requireActivity () instanceof MainActivity) {
                                ((MainActivity) requireActivity ()).replaceFragment (new HomeFragment ());
                            }
                        });
                    })
                    .addOnFailureListener (e ->     {
                        requireActivity ().runOnUiThread (() -> {
                            String errorMessage = e.getMessage ();
                            if (requireActivity () instanceof MainActivity)
                                ((MainActivity) requireActivity ()).showErrorAlertDialog (errorMessage, requireContext ());
                        });
                    })
                    .addOnCanceledListener (() -> {
                        requireActivity ().runOnUiThread (() -> {
                            String errorMessage = "The create info user task has been cancelled. Please Register again later";
                            if (requireActivity () instanceof MainActivity)
                                ((MainActivity) requireActivity ()).showErrorAlertDialog (errorMessage, requireContext ());
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
//        else if (status.isEmpty ())
//            binding.status.setError ("Please enter user status");
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
}