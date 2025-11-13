package com.algoarena.models;

public class Student extends Person {

    public Student(int id, String username) {
        this.id = id;
        this.username = username;
        this.role = "student";
    }

    @Override
    public void showMenu() {
        System.out.println("\n--- 🎓 Student Dashboard ---");
        System.out.println("Welcome, " + this.username + "!");
        System.out.println("1. View All Courses (Enroll)");
        System.out.println("2. View My Registered Courses");
        System.out.println("3. View Completed Courses");
        System.out.println("4. Take a Random Quiz");       // NEW
        System.out.println("5. Reset Course Progress"); // NEW
        System.out.println("6. View Profile");           // NEW
        System.out.println("7. Logout");                 // Re-numbered
        System.out.print("Choose an option: ");
    }
}