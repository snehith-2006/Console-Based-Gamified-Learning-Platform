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
    
    List<Question> getRandomQuestions(int userId, int limit);
}