package com.algoarena.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.algoarena.dao.IUserDAO;
import com.algoarena.models.Creator;
import com.algoarena.models.Person;
import com.algoarena.models.Student;
import com.algoarena.models.User;
import com.algoarena.util.DBConnector;

// This class 'implements' the contract from IUserDAO
public class UserDAOImpl implements IUserDAO {

    @Override
    public Person login(String username, String password) {
        // 1. Get the single connection
        Connection conn = DBConnector.getConnection();
        
        // 2. The SQL query with placeholders (?)
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        
        Person person = null;

        // 3. Use 'try-with-resources' to auto-close the PreparedStatement
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 4. Safely set the parameters
            pstmt.setString(1, username);
            pstmt.setString(2, password);

            // 5. Execute the query and get the results
            try (ResultSet rs = pstmt.executeQuery()) {
                
                // 6. Check if we found a match
                if (rs.next()) {
                    // A user was found!
                    int id = rs.getInt("id");
                    String role = rs.getString("role");
                    String dbUsername = rs.getString("username");
                    
                    // 7. This is Polymorphism!
                    // We return the correct object (Student or Creator)
                    // based on the role, but the method signature
                    // just says we'll return a 'Person'.
                 // ... inside login() method ...
                    if ("student".equals(role)) {
                        person = new Student(id, dbUsername); // No more 'null'
                    } else if ("creator".equals(role)) {
                        person = new Creator(id, dbUsername); // No more 'null'
                    }
                    // ...
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error during login query:");
            e.printStackTrace();
        }

        // 8. Return the person (or null if login failed)
        return person;
    }

    @Override
    public boolean register(User user) {
        Connection conn = DBConnector.getConnection();
        
        // An INSERT query
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole()); // "student" or "creator"

            // executeUpdate() is used for INSERT, UPDATE, DELETE
            // It returns the number of rows that were changed.
            int rowsAffected = pstmt.executeUpdate();

            // If rowsAffected is 1, it means the INSERT worked!
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            // This will fail if the username is already taken
            // (because of the 'UNIQUE' constraint in MySQL)
            System.err.println("Error during registration:");
            e.printStackTrace();
            return false;
        }
    }
}