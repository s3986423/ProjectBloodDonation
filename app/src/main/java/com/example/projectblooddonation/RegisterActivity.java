package com.example.projectblooddonation;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.projectblooddonation.utilclasses.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText etRegEmail, etRegPassword;
    private RadioGroup rgRole;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Link UI elements
        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPassword = findViewById(R.id.etRegPassword);
        rgRole = findViewById(R.id.rgRole);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etRegEmail.getText().toString().trim();
                String password = etRegPassword.getText().toString().trim();
                int selectedRoleId = rgRole.getCheckedRadioButtonId();

                if (selectedRoleId == -1) {
                    Toast.makeText(RegisterActivity.this, "Please select a role", Toast.LENGTH_SHORT).show();
                    return;
                }

                String role = ((RadioButton) findViewById(selectedRoleId)).getText().toString();

                if (!email.isEmpty() && !password.isEmpty()) {
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    String userId = mAuth.getCurrentUser().getUid();
                                    // Save user role in Firestore
                                    db.collection("users").document(userId).set(new User(email, role))
                                            .addOnSuccessListener(aVoid -> Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show())
                                            .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, "Error saving role: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                    finish();
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
}
