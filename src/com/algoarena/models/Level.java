package com.algoarena.models;

// Matches the 'levels' table
public class Level {

    private int id;
    private int courseId;
    private String title;
    private String learningText;
    private int levelOrder;

    // --- THIS IS THE NEW PART ---
    // 'transient' means it's temporary and doesn't exist in the database table.
    // It's only used to hold data for the progress view.
    private transient boolean completed;
    // ----------------------------

    
    // --- Getters and Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLearningText() {
        return learningText;
    }

    public void setLearningText(String learningText) {
        this.learningText = learningText;
    }

    public int getLevelOrder() {
        return levelOrder;
    }

    public void setLevelOrder(int levelOrder) {
        this.levelOrder = levelOrder;
    }

    // --- GETTER/SETTER FOR THE NEW FIELD ---
    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    // -------------------------------------
    
}