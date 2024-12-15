package com.example.projectblooddonation;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.projectblooddonation.utilclasses.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText inputName, etRegEmail, etRegPassword;
    private RadioGroup genderGroup, bloodTypeGroup, rgRole, rhFactorGroup;
    private Button btnRegister, btnPickDate;
    private TextView tvSelectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Link UI elements
        inputName = findViewById(R.id.inputName);
        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPassword = findViewById(R.id.etRegPassword);
        genderGroup = findViewById(R.id.genderGroup);
        bloodTypeGroup = findViewById(R.id.bloodTypeGroup);
        rgRole = findViewById(R.id.rgRole);
        btnRegister = findViewById(R.id.btnRegister);
        btnPickDate = findViewById(R.id.btnPickDate);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        rhFactorGroup = findViewById(R.id.rhFactorGroup);

        setupDatePicker();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = inputName.getText().toString().trim();
                String email = etRegEmail.getText().toString().trim();
                String password = etRegPassword.getText().toString().trim();
                String dob = tvSelectedDate.getText().toString().trim();

                int selectedRoleId = rgRole.getCheckedRadioButtonId();
                int genderId = genderGroup.getCheckedRadioButtonId();
                int bloodTypeId = bloodTypeGroup.getCheckedRadioButtonId();
                int rhFactorId = rhFactorGroup.getCheckedRadioButtonId();

                if (dob.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Please select your date of birth", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedRoleId == -1) {
                    Toast.makeText(RegisterActivity.this, "Please select a role", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (genderId == -1) {
                    Toast.makeText(RegisterActivity.this, "Please select a gender", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (bloodTypeId == -1 || rhFactorId == -1) {
                    Toast.makeText(RegisterActivity.this, "Please select a blood type", Toast.LENGTH_SHORT).show();
                    return;
                }

                String gender = ((RadioButton) findViewById(genderId)).getText().toString();
                String bloodTypes = ((RadioButton) findViewById(bloodTypeId)).getText().toString();
                String rhFactor = ((RadioButton) findViewById(rhFactorId)).getText().toString();
                String bloodType = bloodTypes + rhFactor;
                String role = ((RadioButton) findViewById(selectedRoleId)).getText().toString();

                if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null) {
                                        saveUserToDatabase(user.getUid(), name, email, dob, gender, bloodType, role);
                                    }
                                } else {
                                    Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
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

    private void saveUserToDatabase(String userId, String name, String email, String dob, String gender, String bloodType, String role) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        userData.put("dateOfBirth", dob);
        userData.put("gender", gender);
        userData.put("bloodType", bloodType);
        userData.put("role", role);

        db.collection("users").document(userId).set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "User registered successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error saving user", e));
    }
}

