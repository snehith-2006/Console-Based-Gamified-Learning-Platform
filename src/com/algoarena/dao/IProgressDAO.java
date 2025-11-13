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
    
    boolean resetProgress(int userId, int courseId);
}