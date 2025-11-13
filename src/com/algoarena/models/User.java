package com.algoarena.models;

// This is a simple helper class.
// We use it ONLY to pass registration data around.
// It doesn't extend Person, it's just a data bucket.
public class User {
    
    private String username;
    private String password;
    private String role;
    
    // --- Getters and Setters ---
    
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
}