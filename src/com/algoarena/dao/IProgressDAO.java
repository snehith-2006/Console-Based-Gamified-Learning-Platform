package com.algoarena.dao;

import com.algoarena.models.Level;
import java.util.List;
import com.algoarena.models.Course; 

public interface IProgressDAO {

    boolean completeLevel(int userId, int levelId);
    
    boolean unlockFirstLevel(int userId, int levelId);
    
    boolean isLevelCompleted(int userId, int levelId);
    
    List<Level> getCourseProgress(int userId, int courseId);
    
    List<Course> getCompletedCourses(int userId);
    
    List<Course> getRegisteredCourses(int userId);
    
    // --- NEW METHOD ---
    /**
     * Deletes all progress for a user in a specific course.
     * @param userId The user's ID.
     * @param courseId The course's ID.
     * @return true if successful, false otherwise.
     */
    boolean resetProgress(int userId, int courseId);
}