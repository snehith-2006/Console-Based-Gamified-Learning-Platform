package com.algoarena.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.algoarena.dao.ILevelDAO;
import com.algoarena.models.Level;
import com.algoarena.models.Question;
import com.algoarena.util.DBConnector;

public class LevelDAOImpl implements ILevelDAO {

    @Override
    public boolean addLevel(Level level) {
        Connection conn = DBConnector.getConnection();
        String countSql = "SELECT COUNT(*) AS level_count FROM levels WHERE course_id = ?";
        String insertSql = "INSERT INTO levels (course_id, title, learning_text, level_order) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement countPstmt = conn.prepareStatement(countSql)) {
            countPstmt.setInt(1, level.getCourseId());
            int nextLevelOrder = 1; 
            
            try (ResultSet rs = countPstmt.executeQuery()) {
                if (rs.next()) {
                    nextLevelOrder = rs.getInt("level_count") + 1;
                }
            }
            
            try (PreparedStatement insertPstmt = conn.prepareStatement(insertSql)) {
                insertPstmt.setInt(1, level.getCourseId());
                insertPstmt.setString(2, level.getTitle());
                insertPstmt.setString(3, level.getLearningText());
                insertPstmt.setInt(4, nextLevelOrder); 
                
                int rowsAffected = insertPstmt.executeUpdate();
                return rowsAffected > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error adding level:");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean addQuestion(Question question) {
        Connection conn = DBConnector.getConnection();
        // --- UPDATED SQL ---
        String sql = "INSERT INTO questions (level_id, question_text, option1, option2, option3, option4, correct_option, hint) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, question.getLevelId());
            pstmt.setString(2, question.getQuestionText());
            pstmt.setString(3, question.getOption1());
            pstmt.setString(4, question.getOption2());
            pstmt.setString(5, question.getOption3());
            pstmt.setString(6, question.getOption4());
            pstmt.setInt(7, question.getCorrectOption());
            pstmt.setString(8, question.getHint()); // --- NEW LINE ---
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error adding question:");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Level getLevelById(int levelId) {
        // (This method is unchanged, but included for completeness)
        Connection conn = DBConnector.getConnection();
        String sql = "SELECT * FROM levels WHERE id = ?";
        Level level = null;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, levelId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    level = new Level();
                    level.setId(rs.getInt("id"));
                    level.setCourseId(rs.getInt("course_id"));
                    level.setTitle(rs.getString("title"));
                    level.setLearningText(rs.getString("learning_text"));
                    level.setLevelOrder(rs.getInt("level_order"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting level by ID:");
            e.printStackTrace();
        }
        return level;
    }

    @Override
    public List<Level> getLevelsByCourse(int courseId) {
        // (This method is unchanged, but included for completeness)
        List<Level> levels = new ArrayList<>();
        Connection conn = DBConnector.getConnection();
        String sql = "SELECT * FROM levels WHERE course_id = ? ORDER BY level_order ASC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Level level = new Level();
                    level.setId(rs.getInt("id"));
                    level.setCourseId(rs.getInt("course_id"));
                    level.setTitle(rs.getString("title"));
                    level.setLearningText(rs.getString("learning_text"));
                    level.setLevelOrder(rs.getInt("level_order"));
                    levels.add(level);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting levels by course:");
            e.printStackTrace();
        }
        return levels;
    }

    @Override
    public List<Question> getQuestionsByLevel(int levelId) {
        // (This method is unchanged, but updated to include hint)
        List<Question> questions = new ArrayList<>();
        Connection conn = DBConnector.getConnection();
        String sql = "SELECT * FROM questions WHERE level_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, levelId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Question q = new Question();
                    q.setId(rs.getInt("id"));
                    q.setLevelId(rs.getInt("level_id"));
                    q.setQuestionText(rs.getString("question_text"));
                    q.setOption1(rs.getString("option1"));
                    q.setOption2(rs.getString("option2"));
                    q.setOption3(rs.getString("option3"));
                    q.setOption4(rs.getString("option4"));
                    q.setCorrectOption(rs.getInt("correct_option"));
                    q.setHint(rs.getString("hint")); // --- NEW LINE ---
                    questions.add(q);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting questions by level:");
            e.printStackTrace();
        }
        return questions;
    }
    
    // --- NEW METHOD IMPLEMENTATION ---
    @Override
    public List<Question> getRandomQuestions(int userId, int limit) {
        List<Question> questions = new ArrayList<>();
        Connection conn = DBConnector.getConnection();
        
        // This query finds questions from levels the user is enrolled in,
        // shuffles them randomly, and takes the 'limit'.
        String sql = "SELECT q.* FROM questions q " +
                     "JOIN levels l ON q.level_id = l.id " +
                     "JOIN user_progress up ON l.id = up.level_id " +
                     "WHERE up.user_id = ? " +
                     "ORDER BY RAND() " + // Note: RAND() is slow on large dbs, but fine here
                     "LIMIT ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, limit);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Question q = new Question();
                    q.setId(rs.getInt("id"));
                    q.setLevelId(rs.getInt("level_id"));
                    q.setQuestionText(rs.getString("question_text"));
                    q.setOption1(rs.getString("option1"));
                    q.setOption2(rs.getString("option2"));
                    q.setOption3(rs.getString("option3"));
                    q.setOption4(rs.getString("option4"));
                    q.setCorrectOption(rs.getInt("correct_option"));
                    q.setHint(rs.getString("hint"));
                    questions.add(q);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting random questions:");
            e.printStackTrace();
        }
        return questions;
    }
}