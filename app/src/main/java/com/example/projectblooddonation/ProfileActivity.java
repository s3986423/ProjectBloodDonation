package com.example.projectblooddonation;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DrawerLayout drawerLayout;
    private TextView tRole, tBloodType, tDOB;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    private EditText etName, etEmail;
    private RadioGroup rgGender;
    private static final int LOCATION_REQUEST_CODE = 1001;
    private RadioButton rbMale, rbFemale;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        navigationView = findViewById(R.id.navigation_view);

        tRole = findViewById(R.id.roleText);
        etName = findViewById(R.id.et_name);
        tBloodType = findViewById(R.id.tBloodType);
        tDOB = findViewById(R.id.dobText);
        etEmail = findViewById(R.id.et_email);
        rgGender = findViewById(R.id.rg_gender);
        rbMale = findViewById(R.id.rb_male);
        rbFemale = findViewById(R.id.rb_female);
        btnSave = findViewById(R.id.btn_save);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check location permissions
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
        }

        // Populate fields with user data (Mocked here; replace with actual user data)
        loadUserProfile();

        // pass the Open and Close toggle for the drawer layout listener
        // to toggle the button
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // to make the Navigation drawer icon always appear on the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Save button logic
        btnSave.setOnClickListener(v -> saveUserProfile());

        // Set up Navigation Drawer
        setupNavigationDrawer();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadUserProfile() {
        db.collection("users").document(mAuth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        etName.setText(documentSnapshot.getString("name"));
                        etEmail.setText(documentSnapshot.getString("email"));
                        if (documentSnapshot.getString("gender").equals("Male")) {
                            rbMale.setChecked(true);
                        } else {
                            rbFemale.setChecked(true);
                        }
                        tRole.setText(documentSnapshot.getString("role"));
                        tDOB.setText(documentSnapshot.getString("dateOfBirth"));
                        tBloodType.setText(documentSnapshot.getString("bloodType"));
                    } else {
                        Toast.makeText(ProfileActivity.this, "User role not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Error fetching role: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveUserProfile() {
        String name = etName.getText().toString();
        String email = etEmail.getText().toString();
        String gender = rbMale.isChecked() ? "Male" : "Female";

        // Replace with actual user profile saving logic
        Toast.makeText(this, "Profile Saved: " + name + ", " + gender, Toast.LENGTH_SHORT).show();
    }

    private void setupNavigationDrawer() {
        // Set up navigation item visibility based on user role
        Menu menu = navigationView.getMenu();

        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userRole = documentSnapshot.getString("role");
                        switch (userRole) {
                            case "Donor":
                                menu.findItem(R.id.nav_register_donation).setVisible(true);
                                menu.findItem(R.id.nav_manage_sites).setVisible(false);
                                menu.findItem(R.id.nav_view_donors).setVisible(false);
                                menu.findItem(R.id.nav_reports).setVisible(false);
                                break;
                            case "Site Manager":
                                menu.findItem(R.id.nav_register_donation).setVisible(false);
                                menu.findItem(R.id.nav_manage_sites).setVisible(true);
                                menu.findItem(R.id.nav_view_donors).setVisible(true);
                                menu.findItem(R.id.nav_reports).setVisible(false);
                                break;
                            case "Super User":
                                menu.findItem(R.id.nav_register_donation).setVisible(false);
                                menu.findItem(R.id.nav_manage_sites).setVisible(false);
                                menu.findItem(R.id.nav_view_donors).setVisible(false);
                                menu.findItem(R.id.nav_reports).setVisible(true);
                                break;
                        }
                    } else {
                        Toast.makeText(ProfileActivity.this, "User role not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Error fetching role: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        // Handle navigation item clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId(); // Get the clicked item ID

            if (id == R.id.nav_profile) {
                // Already on profile, do nothing
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;

            } else if (id == R.id.nav_map) {
                // Navigate to map
                startActivity(new Intent(ProfileActivity.this, MapsActivity.class));
            } else if (id == R.id.nav_register_donation) {
//                // Navigate to donation registration
//                startActivity(new Intent(ProfileActivity.this, DonationRegistrationActivity.class));
            } else if (id == R.id.nav_manage_sites) {
//                // Navigate to manage sites
//                startActivity(new Intent(ProfileActivity.this, ManageSitesActivity.class));
            } else if (id == R.id.nav_reports) {
//                // Navigate to reports
//                startActivity(new Intent(ProfileActivity.this, ReportsActivity.class));
            }
            drawerLayout.closeDrawer(GravityCompat.START); // Close the drawer
            return true;
        });
    }
}