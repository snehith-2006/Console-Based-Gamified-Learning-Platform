package com.algoarena.models;

public class Creator extends Person {

    public Creator(int id, String username) {
        this.id = id;
        this.username = username;
        this.role = "creator";
    }

    @Override
    public void showMenu() {
        System.out.println("\n--- 🛠️ Creator Dashboard ---");
        System.out.println("Welcome, " + this.username + "!");
        System.out.println("1. Create New Course");
        System.out.println("2. View My Courses");
        System.out.println("3. Add Level to Course");
        System.out.println("4. Add Question to Level (with Hint)");
        System.out.println("5. View Course Analytics");
        System.out.println("6. Delete a Course");
        System.out.println("7. Logout");
        System.out.print("Choose an option: ");
    }
}