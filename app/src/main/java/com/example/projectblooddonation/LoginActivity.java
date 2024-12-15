package com.example.projectblooddonation;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import com.example.projectblooddonation.utilclasses.FirebaseUtil;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Link UI elements
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        // Set up Login button click listener
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (!email.isEmpty() && !password.isEmpty()) {
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    String userId = mAuth.getCurrentUser().getUid();
                                    db.collection("users").document(userId).get()
                                            .addOnSuccessListener(documentSnapshot -> {
                                                if (documentSnapshot.exists()) {
                                                    String role = documentSnapshot.getString("role");
                                                    Toast.makeText(LoginActivity.this, "Welcome, " + role, Toast.LENGTH_SHORT).show();

                                                    // Navigate based on role
                                                    if ("Donor".equals(role)) {
                                                        // Start Donor Activity
                                                        Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
                                                        startActivity(intent);
                                                        finish(); // Close login activity
                                                    } else if ("Site Manager".equals(role)) {
                                                        // Start Site Manager Activity
                                                    } else if ("Super User".equals(role)) {
                                                        // Start Super User Activity
                                                    }
                                                } else {
                                                    Toast.makeText(LoginActivity.this, "User role not found", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(e -> Toast.makeText(LoginActivity.this, "Error fetching role: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                } else {
                                    Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set up Register text click listener
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}