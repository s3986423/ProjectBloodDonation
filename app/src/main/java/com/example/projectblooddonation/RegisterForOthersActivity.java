package com.example.projectblooddonation;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RegisterForOthersActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String siteId, address;
    private DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    private EditText inputName, etSiteAddress, etRegEmail, etRegPassword;
    private RadioGroup genderGroup, bloodTypeGroup, rhFactorGroup;
    private TextView tvSelectedDate;
    private Button btnRegister, btnPickDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_for_others);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        navigationView = findViewById(R.id.navigation_view);
        // pass the Open and Close toggle for the drawer layout listener
        // to toggle the button
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // Bind views
        inputName = findViewById(R.id.inputName);
        etSiteAddress = findViewById(R.id.etSiteAddress);
        btnPickDate = findViewById(R.id.btnPickDate);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPassword = findViewById(R.id.etRegPassword);
        genderGroup = findViewById(R.id.genderGroup);
        bloodTypeGroup = findViewById(R.id.bloodTypeGroup);
        rhFactorGroup = findViewById(R.id.rhFactorGroup);
        btnRegister = findViewById(R.id.btnRegister);

        setupDatePicker();

        // Get siteId from intent
        siteId = getIntent().getStringExtra("siteId");
        address = getIntent().getStringExtra("address");

        // Auto-fill the address if provided
        if (address != null) {
            etSiteAddress.setText(address);
            etSiteAddress.setEnabled(false);
        }

        // to make the Navigation drawer icon always appear on the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setupNavigationDrawer();

        // Register button logic
        btnRegister.setOnClickListener(v -> registerForOther());
    }

    private void setupDatePicker() {
        btnPickDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
                String selectedDate = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                tvSelectedDate.setText(selectedDate);
            }, year, month, day);

            datePickerDialog.show();
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void registerForOther() {
        String name = inputName.getText().toString().trim();
        String dob = tvSelectedDate.getText().toString().trim();
        String email = etRegEmail.getText().toString().trim();
        String password = etRegPassword.getText().toString().trim();
        String address = etSiteAddress.getText().toString().trim();
        int genderId = genderGroup.getCheckedRadioButtonId();
        int bloodTypeId = bloodTypeGroup.getCheckedRadioButtonId();
        int rhFactorId = rhFactorGroup.getCheckedRadioButtonId();

        // Validate inputs
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) ||
                TextUtils.isEmpty(address) || dob.equals("No date selected") || genderId == -1 || bloodTypeId == -1 || rhFactorId == -1) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String gender = ((RadioButton) findViewById(genderId)).getText().toString();
        String bloodType = ((RadioButton) findViewById(bloodTypeId)).getText().toString() +
                ((RadioButton) findViewById(rhFactorId)).getText().toString();

        // Check if coming from MapsActivity or manual input
        if (siteId == null) {
            // Validate address manually
            db.collection("donation_sites")
                    .whereEqualTo("address", address)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Address found, retrieve siteId
                            siteId = queryDocumentSnapshots.getDocuments().get(0).getId();
                            // Proceed with registration
                            createUserAndRegister(name, email, password, gender, bloodType, dob);
                        } else {
                            Toast.makeText(this, "Address does not match any donation site.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to validate address: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            // Directly proceed with registration if siteId is already available
            createUserAndRegister(name, email, password, gender, bloodType, dob);
        }
    }

    private void createUserAndRegister(String name, String email, String password, String gender, String bloodType, String dob) {
        // Create user with FirebaseAuth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser newUser = task.getResult().getUser();
                        if (newUser != null) {
                            String userId = newUser.getUid();

                            // Save user data to Firestore
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("name", name);
                            userData.put("dateOfBirth", dob);
                            userData.put("email", email);
                            userData.put("gender", gender);
                            userData.put("bloodType", bloodType);
                            userData.put("role", "Donor");

                            db.collection("users").document(userId).set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        if (siteId != null) {
                                            // Add user ID to the site's registeredDonors
                                            db.collection("donation_sites").document(siteId)
                                                    .update("registeredDonors", FieldValue.arrayUnion(userId))
                                                    .addOnSuccessListener(aVoid1 -> {
                                                        Toast.makeText(this, "Successfully registered!", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    })
                                                    .addOnFailureListener(e ->
                                                            Toast.makeText(this, "Failed to register at site: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                        } else {
                                            Toast.makeText(this, "User created without registering at a site.", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Failed to save user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        Toast.makeText(this, "Failed to create user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
                        Toast.makeText(RegisterForOthersActivity.this, "User role not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(RegisterForOthersActivity.this, "Error fetching role: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        // Handle navigation item clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId(); // Get the clicked item ID

            if (id == R.id.nav_profile) {
//                // Navigate to profile
//                startActivity(new Intent(RegisterForOthersActivity.this, ProfileActivity.class));
                finish();
            } else if (id == R.id.nav_map) {
                // Navigate to map
                startActivity(new Intent(RegisterForOthersActivity.this, MapsActivity.class));
            } else if (id == R.id.nav_register_donation) {
                // Already on donation registration, do nothing
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
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