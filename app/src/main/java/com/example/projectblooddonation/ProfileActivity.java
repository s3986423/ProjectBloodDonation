package com.example.projectblooddonation;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

public class ProfileActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private EditText etName, etEmail, etPassword;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        rgGender = findViewById(R.id.rg_gender);
        rbMale = findViewById(R.id.rb_male);
        rbFemale = findViewById(R.id.rb_female);
        btnSave = findViewById(R.id.btn_save);

        // Populate fields with user data (Mocked here; replace with actual user data)
        loadUserProfile();

        // Save button logic
        btnSave.setOnClickListener(v -> saveUserProfile());

        // Set up Navigation Drawer
        setupNavigationDrawer();
    }

    private void loadUserProfile() {
        // Replace with actual user data retrieval logic
        etName.setText("John Doe");
        etEmail.setText("john.doe@example.com");
        etPassword.setText("password123");
        rbMale.setChecked(true); // Example: Set gender
    }

    private void saveUserProfile() {
        String name = etName.getText().toString();
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        String gender = rbMale.isChecked() ? "Male" : "Female";

        // Replace with actual user profile saving logic
        Toast.makeText(this, "Profile Saved: " + name + ", " + gender, Toast.LENGTH_SHORT).show();
    }

    private void setupNavigationDrawer() {
        // Set up navigation item visibility based on user role
        Menu menu = navigationView.getMenu();
        String userRole = getUserRole(); // Mock user role; replace with actual role logic

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

    private String getUserRole() {
        // Replace with actual logic to retrieve user role
        return "Donor"; // Example: Hardcoded role
    }
}