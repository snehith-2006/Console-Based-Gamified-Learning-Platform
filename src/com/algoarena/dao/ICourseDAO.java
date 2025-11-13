package com.algoarena.dao;

import com.algoarena.models.Course;
import java.util.List;

public interface ICourseDAO {

    boolean createCourse(Course course);
    
    List<Course> getCoursesByCreator(int creatorId);
    
    List<Course> getAllCourses();
    
    int getEnrollmentCount(int courseId);
    
    boolean deleteCourse(int courseId);
}