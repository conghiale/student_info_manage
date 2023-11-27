package com.example.student_information_management;

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

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.student_information_management.databinding.ActivityMainBinding;
import com.example.student_information_management.fragment.ChangeEmailFragment;
import com.example.student_information_management.fragment.ChangePasswordFragment;
import com.example.student_information_management.fragment.ChangeProfilePictureFragment;
import com.example.student_information_management.fragment.HistoryUserLoginFragment;
import com.example.student_information_management.fragment.HomeFragment;
import com.example.student_information_management.fragment.CreateAccountFragment;
import com.example.student_information_management.fragment.ProvideInfoUserFragment;
import com.example.student_information_management.fragment.StudentsFragment;
import com.example.student_information_management.fragment.UsersFragment;
import com.example.student_information_management.model.User;
import com.example.student_information_management.view_model.MyViewModel;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        binding = ActivityMainBinding.inflate (getLayoutInflater ());
        setContentView (binding.getRoot ());

        setSupportActionBar (binding.toolbar);
        Objects.requireNonNull (getSupportActionBar ()).setTitle ("");

        binding.navView.setNavigationItemSelectedListener (this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle (this, binding.drawerLayout,
                binding.toolbar, R.string.open_nav, R.string.close_nav);
        binding.drawerLayout.addDrawerListener (toggle);
        toggle.syncState ();

        if (savedInstanceState == null) {
            getSupportFragmentManager ().beginTransaction ().replace (R.id.fragment_container, new HomeFragment ()).commit ();
            binding.navView.setCheckedItem (R.id.nav_home);
        }

        init();

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

    private void init() {
        Log.d ("TAG", "init: ");
        executorService = Executors.newFixedThreadPool (1);
        db = FirebaseFirestore.getInstance ();
        getCurrentUser ();
    }
    private void getCurrentUser() {
        Log.d ("TAG", "getCurrentUser: " + Objects.requireNonNull (FirebaseAuth.getInstance ().getUid ()));
        executorService.execute (() -> {
            db.collection ("users")
                    .document (Objects.requireNonNull (FirebaseAuth.getInstance ().getUid ()))
                    .get ()
                    .addOnSuccessListener (documentSnapshot -> {
                        if (documentSnapshot.exists ()) {
                            User user = documentSnapshot.toObject (User.class);
                            Log.d ("TAG", "getCurrentUser: 1");

                            if (user != null) {
                                MyViewModel model = new ViewModelProvider (this).get (MyViewModel.class);
                                model.setCurrentUser (user);
                                runOnUiThread (() -> {
                                    Log.d ("TAG", "getCurrentUser: 2");
                                    if (!user.getRole ().equals ("Admin")) {
                                        binding.navView.getMenu ().findItem (R.id.nav_CreateAccount).setVisible (false);
                                    }
                                });
                            }
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
}