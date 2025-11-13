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


public class UserDAOImpl implements IUserDAO {

    @Override
    public Person login(String username, String password) {
        Connection conn = DBConnector.getConnection();
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        
        Person person = null;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String role = rs.getString("role");
                    String dbUsername = rs.getString("username");
                    if ("student".equals(role)) {
                        person = new Student(id, dbUsername); 
                    } else if ("creator".equals(role)) {
                        person = new Creator(id, dbUsername); 
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error during login query:");
            e.printStackTrace();
        }

        return person;
    }

    @Override
    public boolean register(User user) {
        Connection conn = DBConnector.getConnection();
        
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole()); 
            int rowsAffected = pstmt.executeUpdate();

            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error during registration:");
            e.printStackTrace();
            return false;
        }
    }
}