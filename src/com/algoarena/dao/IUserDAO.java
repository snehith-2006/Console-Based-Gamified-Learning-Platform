package com.algoarena.dao;

import com.algoarena.models.Person; // We use Person for polymorphism
import com.algoarena.models.User; // We need this for registration

// This is the contract for our UserDAO.
// Any class that implements this MUST provide these two methods.
public interface IUserDAO {

    /**
     * Tries to log a user in.
     * @param username The username to check.
     * @param password The password to check.
     * @return A Person object (Student or Creator) if successful, or null if failed.
     */
    Person login(String username, String password);
    
    /**
     * Tries to register a new user.
     * @param user A User object containing username, password, and role.
     * @return true if registration was successful, false if it failed (e.g., username taken).
     */
    boolean register(User user);
    
    // Note: We're using a plain 'User' object from the models package 
    // for registration, but you'll need to create that model file.
    // Let's create `User.java` in `com.algoarena.models` right after this.
}