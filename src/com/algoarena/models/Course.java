package com.algoarena.models;

// This class just holds data. It matches the 'courses' table.
public class Course {

    private int id;
    private String title;
    private String description;
    private int creatorId;

    // --- Getters and Setters ---
    // We use these to practice Encapsulation.
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }
}