package com.algoarena.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.algoarena.dao.IProgressDAO;
import com.algoarena.models.Course; 
import com.algoarena.models.Level;
import com.algoarena.util.DBConnector;

public class ProgressDAOImpl implements IProgressDAO {

    @Override
    public boolean completeLevel(int userId, int levelId) {
        Connection conn = DBConnector.getConnection();
        String sql = "UPDATE user_progress SET completed = true WHERE user_id = ? AND level_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, levelId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error completing level:");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean unlockFirstLevel(int userId, int levelId) {
        Connection conn = DBConnector.getConnection();
        String sql = "INSERT IGNORE INTO user_progress (user_id, level_id, completed) VALUES (?, ?, false)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, levelId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected >= 0; 
        } catch (SQLException e) {
            System.err.println("Error unlocking first level:");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isLevelCompleted(int userId, int levelId) {
        Connection conn = DBConnector.getConnection();
        String sql = "SELECT completed FROM user_progress WHERE user_id = ? AND level_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, levelId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("completed");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking level completion:");
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Level> getCourseProgress(int userId, int courseId) {
        List<Level> levels = new ArrayList<>();
        Connection conn = DBConnector.getConnection();

        String sql = "SELECT " +
                     "    l.id, " +
                     "    l.title, " +
                     "    l.learning_text, " +
                     "    l.level_order, " +
                     "    MAX(up.completed) AS completed_status " + 
                     "FROM levels l " +
                     "LEFT JOIN user_progress up ON l.id = up.level_id AND up.user_id = ? " +
                     "WHERE l.course_id = ? " +
                     "GROUP BY l.id, l.title, l.learning_text, l.level_order " +
                     "ORDER BY l.level_order ASC";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, courseId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Level level = new Level();
                    level.setId(rs.getInt("id"));
                    level.setTitle(rs.getString("title"));
                    level.setLearningText(rs.getString("learning_text")); 
                    level.setLevelOrder(rs.getInt("level_order"));
                    level.setCompleted(rs.getBoolean("completed_status")); 
                    levels.add(level);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting course progress:");
            e.printStackTrace();
        }
        return levels;
    }
    
    @Override
    public List<Course> getRegisteredCourses(int userId) {
        List<Course> registeredCourses = new ArrayList<>();
        Connection conn = DBConnector.getConnection();
        
        String sql = "SELECT DISTINCT c.* FROM courses c " +
                     "JOIN levels l ON c.id = l.course_id " +
                     "JOIN user_progress up ON l.id = up.level_id " +
                     "WHERE up.user_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Course course = new Course();
                    course.setId(rs.getInt("id"));
                    course.setTitle(rs.getString("title"));
                    course.setDescription(rs.getString("description"));
                    course.setCreatorId(rs.getInt("creator_id"));
                    registeredCourses.add(course);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting registered courses:");
            e.printStackTrace();
        }
        return registeredCourses;
    }
    
    @Override
    public List<Course> getCompletedCourses(int userId) {
        List<Course> completedCourses = new ArrayList<>();
        Connection conn = DBConnector.getConnection();
        
        String sql = "SELECT c.id, c.title " +
                     "FROM courses c " +
                     "JOIN levels l ON c.id = l.course_id " +
                     "JOIN user_progress up ON l.id = up.level_id " +
                     "WHERE up.user_id = ? AND up.completed = true " +
                     "GROUP BY c.id, c.title " +
                     "HAVING COUNT(l.id) = ( " +
                     "    SELECT COUNT(*) FROM levels l2 WHERE l2.course_id = c.id " +
                     ")";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Course course = new Course();
                    course.setId(rs.getInt("id"));
                    course.setTitle(rs.getString("title"));
                    completedCourses.add(course);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching completed courses:");
            e.printStackTrace();
        }
        return completedCourses;
    }

    // --- NEW METHOD IMPLEMENTATION ---
    @Override
    public boolean resetProgress(int userId, int courseId) {
        Connection conn = DBConnector.getConnection();
        // This query finds all level_ids for the course, then deletes
        // all user_progress entries for that user and those levels.
        String sql = "DELETE FROM user_progress " +
                     "WHERE user_id = ? " +
                     "AND level_id IN (SELECT id FROM levels WHERE course_id = ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, courseId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0; // Return true if any rows were deleted
            
        } catch (SQLException e) {
            System.err.println("Error resetting progress:");
            e.printStackTrace();
            return false;
        }
    }
}