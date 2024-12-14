package com.example.projectblooddonation.utilclasses;

public class User {
    public String email;
    public String role;

    public User(String email, String role) {
        this.email = email;
        this.role = role;
    }

    // Default constructor (required by Firestore)
    public User() {}
}
