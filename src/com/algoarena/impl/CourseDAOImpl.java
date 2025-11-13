package com.algoarena.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.algoarena.dao.ICourseDAO;
import com.algoarena.models.Course;
import com.algoarena.util.DBConnector;

public class CourseDAOImpl implements ICourseDAO {

    @Override
    public boolean createCourse(Course course) {
        Connection conn = DBConnector.getConnection();
        String sql = "INSERT INTO courses (title, description, creator_id) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, course.getTitle());
            pstmt.setString(2, course.getDescription());
            pstmt.setInt(3, course.getCreatorId());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error creating course:");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Course> getCoursesByCreator(int creatorId) {
        List<Course> courses = new ArrayList<>();
        Connection conn = DBConnector.getConnection();
        String sql = "SELECT * FROM courses WHERE creator_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, creatorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Course course = new Course();
                    course.setId(rs.getInt("id"));
                    course.setTitle(rs.getString("title"));
                    course.setDescription(rs.getString("description"));
                    course.setCreatorId(rs.getInt("creator_id"));
                    courses.add(course);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching courses by creator:");
            e.printStackTrace();
        }
        return courses;
    }

    @Override
    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        Connection conn = DBConnector.getConnection();
        String sql = "SELECT * FROM courses";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Course course = new Course();
                    course.setId(rs.getInt("id"));
                    course.setTitle(rs.getString("title"));
                    course.setDescription(rs.getString("description"));
                    course.setCreatorId(rs.getInt("creator_id"));
                    courses.add(course);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all courses:");
            e.printStackTrace();
        }
        return courses;
    }
    
    @Override
    public int getEnrollmentCount(int courseId) {
        Connection conn = DBConnector.getConnection();
        String sql = "SELECT COUNT(DISTINCT user_id) AS count " +
                     "FROM user_progress up " +
                     "JOIN levels l ON up.level_id = l.id " +
                     "WHERE l.course_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting enrollment count:");
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean deleteCourse(int courseId) {
        Connection conn = DBConnector.getConnection();
        
        String[] deleteQueries = {
            "DELETE FROM user_progress WHERE level_id IN (SELECT id FROM levels WHERE course_id = ?)",
            "DELETE FROM questions WHERE level_id IN (SELECT id FROM levels WHERE course_id = ?)",
            "DELETE FROM levels WHERE course_id = ?",
            "DELETE FROM courses WHERE id = ?"
        };
        
        try {
            conn.setAutoCommit(false);
            
            for (String sql : deleteQueries) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, courseId);
                    pstmt.executeUpdate();
                }
            }
            
            conn.commit();
            conn.setAutoCommit(true);
            return true;
            
        } catch (SQLException e) {
            System.err.println("Error deleting course (rolling back):");
            e.printStackTrace();
            try {
                conn.rollback();
            } catch (SQLException se) {
                System.err.println("Error rolling back:");
                se.printStackTrace();
            }
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}