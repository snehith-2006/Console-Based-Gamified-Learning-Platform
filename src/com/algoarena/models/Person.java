package com.algoarena.models;

// This is an abstract class.
// You can't create a "new Person()".
// It's only meant to be a parent for other classes (Student, Creator).
public abstract class Person {

    // 'protected' means this class AND its children (Student, Creator)
    // can access these variables directly.
    protected int id;
    protected String username;
    protected String role;

    // This is an abstract method.
    // It's a rule that says "any class that extends Person MUST
    // provide its own version of a showMenu() method."
    // This is how we get Polymorphism.
    public abstract void showMenu();
    
    // --- Standard Getters and Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}