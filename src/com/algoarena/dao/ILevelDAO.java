package com.algoarena.dao;

import com.algoarena.models.Level;
import com.algoarena.models.Question;
import java.util.List;

public interface ILevelDAO {

    boolean addLevel(Level level);
    
    boolean addQuestion(Question question);
    
    Level getLevelById(int levelId);
    
    List<Level> getLevelsByCourse(int courseId);
    
    List<Question> getQuestionsByLevel(int levelId);
    
    // --- NEW METHOD ---
    /**
     * Gets a random list of questions from courses a user is enrolled in.
     * @param userId The user's ID.
     * @param limit The number of questions to get.
     * @return A List of random Question objects.
     */
    List<Question> getRandomQuestions(int userId, int limit);
}