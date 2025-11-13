package com.algoarena.dao;

import com.algoarena.models.Course;
import java.util.List;

public interface ICourseDAO {

    boolean createCourse(Course course);
    
    List<Course> getCoursesByCreator(int creatorId);
    
    List<Course> getAllCourses();
    
    // --- NEW METHODS ---
    /**
     * Counts how many unique students are enrolled in a course.
     * @param courseId The course's ID.
     * @return The number of students.
     */
    int getEnrollmentCount(int courseId);
    
    /**
     * Deletes a course and all its related levels, questions, and progress.
     * @param courseId The course's ID.
     * @return true if successful, false otherwise.
     */
    boolean deleteCourse(int courseId);
}