package com.example.student_information_management;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.student_information_management.data.model.User;
import com.example.student_information_management.databinding.ActivityMainBinding;
import com.example.student_information_management.ui.activity.IntroActivity;
import com.example.student_information_management.ui.activity.LoginActivity;
import com.example.student_information_management.ui.fragment.ChangeEmailFragment;
import com.example.student_information_management.ui.fragment.ChangePasswordFragment;
import com.example.student_information_management.ui.fragment.ChangeProfilePictureFragment;
import com.example.student_information_management.ui.fragment.CreateAccountFragment;
import com.example.student_information_management.ui.fragment.HistoryUserLoginFragment;
import com.example.student_information_management.ui.fragment.HomeFragment;
import com.example.student_information_management.ui.fragment.ProvideInfoUserFragment;
import com.example.student_information_management.ui.fragment.StudentsFragment;
import com.example.student_information_management.ui.fragment.UsersFragment;
import com.example.student_information_management.ui.viewModel.MyViewModel;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private ActivityMainBinding binding;
    private ExecutorService executorService;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    public static MyViewModel model;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        binding = ActivityMainBinding.inflate (getLayoutInflater ());
        setContentView (binding.getRoot ());

        setSupportActionBar (binding.toolbar);
        Objects.requireNonNull (getSupportActionBar ()).setTitle ("");
        model = new ViewModelProvider (this).get (MyViewModel.class);

        init();

        binding.navView.setNavigationItemSelectedListener (this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle (this, binding.drawerLayout,
                binding.toolbar, R.string.open_nav, R.string.close_nav);
        binding.drawerLayout.addDrawerListener (toggle);
        toggle.syncState ();

        if (savedInstanceState == null) {
            getSupportFragmentManager ().beginTransaction ().replace (R.id.fragment_container, new HomeFragment ()).commit ();
            binding.navView.setCheckedItem (R.id.nav_home);
        }

        OnBackPressedCallback callback = new OnBackPressedCallback (true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen (GravityCompat.START))
                    binding.drawerLayout.closeDrawer (GravityCompat.START);
                else
                    MainActivity.super.getOnBackPressedDispatcher ().onBackPressed ();
            }
        };

        getOnBackPressedDispatcher().addCallback(this, callback);
    }
    @Override
    protected void onResume() {
        super.onResume ();
        checkStatus ();
    }
    @Override
    protected void onStart() {
        super.onStart ();
        checkStatus ();
    }
    private void checkStatus() {
        model.getCurrentUser ().observe (this, user -> {
            executorService.execute (() -> {
                db.collection ("users")
                        .whereEqualTo ("email", user.getEmail ())
                        .get ()
                        .addOnSuccessListener (queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.getDocuments ().isEmpty ()) {
                                DocumentSnapshot document = queryDocumentSnapshots.getDocuments ().get (0);
                                String status = document.getString ("status");
                                if (status != null && status.equals ("Locker")) {
                                    showErrorAlertDialog2 ("This account has been locked", this);
                                }
                            }
                        })
                        .addOnFailureListener (e -> {
                            runOnUiThread (() -> {
                                showErrorAlertDialog(e.getMessage (), this);
                            });
                        })
                        .addOnCanceledListener (() -> {
                            runOnUiThread (() -> {
                                showErrorAlertDialog("The system is maintenance. The Login task has been cancelled. Please Login again later", this);
                            });
                        });
            });
        });
    }

    private void init() {
        executorService = Executors.newFixedThreadPool (1);
        db = FirebaseFirestore.getInstance ();
        auth = FirebaseAuth.getInstance ();
        getCurrentUser ();
    }
    private void getCurrentUser() {
        executorService.execute (() -> {
            db.collection ("users")
                    .document (Objects.requireNonNull (FirebaseAuth.getInstance ().getUid ()))
                    .get ()
                    .addOnSuccessListener (documentSnapshot -> {
                        if (documentSnapshot.exists ()) {
                            runOnUiThread (() -> {
                                User user = documentSnapshot.toObject (User.class);
                                if (user != null) {
                                    model.setCurrentUser (user);
                                }
                            });
                        }
                    })
                    .addOnCanceledListener (() -> {
                        runOnUiThread (() -> {
                            String errorMessage = "The get current user task has been cancelled. Please again later";
                            showErrorAlertDialog (errorMessage, this);
                        });
                    });
        });
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemID = item.getItemId ();

        if (itemID == R.id.nav_home) {
            getSupportFragmentManager ().beginTransaction ().replace (R.id.fragment_container, new HomeFragment ()).commit ();
        } else if (itemID == R.id.nav_users) {
            getSupportFragmentManager ().beginTransaction ().replace (R.id.fragment_container, new UsersFragment ()).commit ();
        } else if (itemID == R.id.nav_history_user) {
            getSupportFragmentManager ().beginTransaction ().replace (R.id.fragment_container, new HistoryUserLoginFragment ()).commit ();
        } else if (itemID == R.id.nav_students) {
            getSupportFragmentManager ().beginTransaction ().replace (R.id.fragment_container, new StudentsFragment ()).commit ();
        } else if (itemID == R.id.nav_CreateAccount) {
            getSupportFragmentManager ().beginTransaction ().replace (R.id.fragment_container, new CreateAccountFragment ()).commit ();
        } else if (itemID == R.id.nav_change_password) {
            getSupportFragmentManager ().beginTransaction ().replace (R.id.fragment_container, new ChangePasswordFragment ()).commit ();
            Objects.requireNonNull (getSupportActionBar ()).setBackgroundDrawable (new ColorDrawable (ContextCompat.getColor (this, R.color.purple3)));
        } else if (itemID == R.id.nav_change_email) {
            getSupportFragmentManager ().beginTransaction ().replace (R.id.fragment_container, new ChangeEmailFragment ()).commit ();
        } else if (itemID == R.id.nav_change_profile_picture) {
            getSupportFragmentManager ().beginTransaction ().replace (R.id.fragment_container, new ChangeProfilePictureFragment ()).commit ();
        } else if (itemID == R.id.nav_logout) {
            FirebaseAuth.getInstance ().signOut ();
            startActivity (new Intent (this, IntroActivity.class));
            finish ();
        }
        binding.drawerLayout.closeDrawer (GravityCompat.START);
        return true;
    }
    public void replaceFragment(Fragment fragment) {
        if (fragment instanceof ChangePasswordFragment) {
            Objects.requireNonNull (getSupportActionBar ()).setBackgroundDrawable (new ColorDrawable (ContextCompat.getColor (this, R.color.purple3)));
            binding.navView.setCheckedItem (R.id.nav_change_password);
        } else if (fragment instanceof ChangeEmailFragment) {
            Objects.requireNonNull (getSupportActionBar ()).setBackgroundDrawable (new ColorDrawable (ContextCompat.getColor (this, R.color.purple3)));
            binding.navView.setCheckedItem (R.id.nav_change_email);
        } else if (fragment instanceof CreateAccountFragment) {
            Objects.requireNonNull (getSupportActionBar ()).setBackgroundDrawable (new ColorDrawable (ContextCompat.getColor (this, R.color.purple3)));
            binding.navView.setCheckedItem (R.id.nav_CreateAccount);
        } else if (fragment instanceof ProvideInfoUserFragment) {
            Objects.requireNonNull (getSupportActionBar ()).setBackgroundDrawable (new ColorDrawable (ContextCompat.getColor (this, R.color.purple3)));
        } else if (fragment instanceof HistoryUserLoginFragment) {
            Objects.requireNonNull (getSupportActionBar ()).setBackgroundDrawable (new ColorDrawable (ContextCompat.getColor (this, R.color.lavender)));
            binding.navView.setCheckedItem (R.id.nav_history_user);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
    public void showErrorAlertDialog(String message, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder (context, R.style.AlertDialogTheme);
        View view = LayoutInflater.from (context).inflate (
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
    public void showSuccessAlertDialog(String message, Context context, String toastMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder (context, R.style.AlertDialogTheme);
        View view = LayoutInflater.from (context).inflate (
                R.layout.layout_success_dialog, (ConstraintLayout)findViewById (R.id.layoutDialogContainer));
        builder.setView (view);

        ((TextView) view.findViewById (R.id.tvTitle)).setText ("Success");
        ((TextView) view.findViewById (R.id.tvMessage)).setText (message);
        ((AppCompatButton) view.findViewById (R.id.btnAction)).setText ("Okay");
        ((ImageView) view.findViewById (R.id.ivImageIcon)).setImageResource (R.drawable.baseline_done);

        final AlertDialog alertDialog = builder.create ();
        view.findViewById (R.id.btnAction).setOnClickListener (v -> {
            alertDialog.dismiss ();
            Toast.makeText (this, toastMessage, Toast.LENGTH_LONG).show ();
            replaceFragment (new HomeFragment ());
//            Intent intent = new Intent (context, MainActivity.class); // xử lý re_auth
//            intent.putExtra ("PASSWORD_UPDATED", "true");
//            startActivity (intent);
//            finish ();
        });

        if (alertDialog.getWindow () != null)
            alertDialog.getWindow ().setBackgroundDrawable (new ColorDrawable (0));
        alertDialog.show ();
        alertDialog.setCancelable(false);
    }
    public void showErrorAlertDialog2(String message, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder (context, R.style.AlertDialogTheme);
        View view = LayoutInflater.from (context).inflate (
                R.layout.layout_error_dialog, (ConstraintLayout)findViewById (R.id.layoutDialogContainer));
        builder.setView (view);

        ((TextView) view.findViewById (R.id.tvTitle)).setText ("Error");
        ((TextView) view.findViewById (R.id.tvMessage)).setText (message);
        ((AppCompatButton) view.findViewById (R.id.btnAction)).setText ("Okay");
        ((ImageView) view.findViewById (R.id.ivImageIcon)).setImageResource (R.drawable.baseline_error);

        final AlertDialog alertDialog = builder.create ();
        view.findViewById (R.id.btnAction).setOnClickListener (v -> {
            alertDialog.dismiss ();
            auth.signOut ();
            startActivity (new Intent (this, LoginActivity.class));
            finish ();
        });

        if (alertDialog.getWindow () != null)
            alertDialog.getWindow ().setBackgroundDrawable (new ColorDrawable (0));
        alertDialog.show ();
        alertDialog.setCancelable(false);
    }
}