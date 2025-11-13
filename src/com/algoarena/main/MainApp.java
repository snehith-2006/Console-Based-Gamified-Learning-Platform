package com.algoarena.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import com.algoarena.dao.*;
import com.algoarena.impl.*;
import com.algoarena.models.*;
import com.algoarena.util.DBConnector;

public class MainApp {

    // DAOs
    private static IUserDAO userDAO = new UserDAOImpl();
    private static ICourseDAO courseDAO = new CourseDAOImpl();
    private static ILevelDAO levelDAO = new LevelDAOImpl();
    private static IProgressDAO progressDAO = new ProgressDAOImpl();

    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        if (DBConnector.getConnection() == null) {
            System.err.println("Database connection failed. Exiting.");
            return;
        } else {
            System.out.println("Database connection successful!");
        }
        
        runApp();
    }

    public static void runApp() {
        while (true) {
            System.out.println("\n--- Welcome to AlgoArena ---");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            int choice = getIntInput();

            switch (choice) {
                case 1: handleLogin(); break;
                case 2: handleRegister(); break;
                case 3:
                    System.out.println("Goodbye!");
                    scanner.close(); 
                    return; 
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void handleLogin() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        Person person = userDAO.login(username, password);

        if (person != null) {
            System.out.println("\nLogin successful! Welcome, " + person.getUsername());
            showDashboard(person);
        } else {
            System.out.println("Login failed. Invalid username or password.");
        }
    }

    private static void handleRegister() {
        System.out.print("Enter new username: ");
        String username = scanner.nextLine();
        System.out.print("Enter new password: ");
        String password = scanner.nextLine();
        
        String role = "";
        while (true) {
            System.out.print("Register as (1) Student or (2) Creator: ");
            int roleChoice = getIntInput();
            if (roleChoice == 1) { role = "student"; break; }
            else if (roleChoice == 2) { role = "creator"; break; }
            else { System.out.println("Invalid choice."); }
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setRole(role);

        if (userDAO.register(newUser)) {
            System.out.println("Registration successful! Please log in.");
        } else {
            System.out.println("Registration failed. Username might be taken.");
        }
    }

    private static void showDashboard(Person person) {
        boolean loggedIn = true;
        while (loggedIn) {
            person.showMenu();
            
            int choice = getIntInput();

            if (person instanceof Student) {
                loggedIn = handleStudentMenu((Student) person, choice);
            } else if (person instanceof Creator) {
                loggedIn = handleCreatorMenu((Creator) person, choice);
            }
        }
    }

    // --- STUDENT MENU HANDLERS (ALL NEW) ---

    private static boolean handleStudentMenu(Student student, int choice) {
        switch (choice) {
            case 1:
                viewAllStudentCourses(student); 
                break;
            case 2:
                viewRegisteredCourses(student);
                break;
            case 3:
                viewCompletedCourses(student);
                break;
            case 4:
                handleRandomQuiz(student);
                break;
            case 5:
                handleResetProgress(student);
                break;
            case 6:
                handleProfile(student);
                break;
            case 7:
                System.out.println("Logging out...");
                return false; 
            default:
                System.out.println("Invalid option.");
        }
        return true; 
    }

    private static void viewAllStudentCourses(Student student) {
        System.out.println("\n--- All Courses ---");
        List<Course> allCourses = courseDAO.getAllCourses();
        
        if (allCourses.isEmpty()) {
            System.out.println("No courses available yet.");
            return;
        }

        List<Course> registeredCourses = progressDAO.getRegisteredCourses(student.getId());
        List<Integer> registeredCourseIds = new ArrayList<>();
        for (Course c : registeredCourses) {
            registeredCourseIds.add(c.getId());
        }

        for (int i = 0; i < allCourses.size(); i++) {
            Course course = allCourses.get(i);
            String status = registeredCourseIds.contains(course.getId()) ? "[ENROLLED]" : "";
            System.out.printf("%d. %s %s\n", (i + 1), course.getTitle(), status);
        }
        
        System.out.print("Select a course to view (0 to cancel): ");
        int choice = getIntInput();
        if (choice > 0 && choice <= allCourses.size()) {
            Course selectedCourse = allCourses.get(choice - 1);

            if (registeredCourseIds.contains(selectedCourse.getId())) {
                showCourseProgress(student, selectedCourse);
            } else {
                System.out.print("You are not enrolled in this course. Enroll now? (y/n): ");
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("y")) {
                    List<Level> levels = levelDAO.getLevelsByCourse(selectedCourse.getId());
                    if (levels.isEmpty()) {
                        System.out.println("Cannot enroll, this course has no levels.");
                        return;
                    }
                    Level firstLevel = levels.get(0);
                    progressDAO.unlockFirstLevel(student.getId(), firstLevel.getId());
                    System.out.println("Successfully enrolled! You can now start the first level.");
                    showCourseProgress(student, selectedCourse);
                }
            }
        }
    }

    private static void viewRegisteredCourses(Student student) {
        System.out.println("\n--- My Registered Courses ---");
        List<Course> registeredCourses = progressDAO.getRegisteredCourses(student.getId());

        if (registeredCourses.isEmpty()) {
            System.out.println("You are not registered in any courses yet.");
            return;
        }

        for (int i = 0; i < registeredCourses.size(); i++) {
            System.out.printf("%d. %s\n", (i + 1), registeredCourses.get(i).getTitle());
        }

        System.out.print("Select a course to view progress (0 to cancel): ");
        int choice = getIntInput();
        if (choice > 0 && choice <= registeredCourses.size()) {
            Course selectedCourse = registeredCourses.get(choice - 1);
            showCourseProgress(student, selectedCourse);
        }
    }
    
    private static void viewCompletedCourses(Student student) {
        System.out.println("\n--- My Completed Courses ---");
        List<Course> completed = progressDAO.getCompletedCourses(student.getId());
        
        if (completed.isEmpty()) {
            System.out.println("You haven't completed any courses yet.");
        } else {
            for (Course course : completed) {
                System.out.println("- " + course.getTitle());
            }
        }
    }

    private static void handleRandomQuiz(Student student) {
        System.out.println("\n--- 🎲 Random Quiz ---");
        System.out.print("How many questions would you like (e.g., 5 or 10)? ");
        int limit = getIntInput();
        
        List<Question> questions = levelDAO.getRandomQuestions(student.getId(), limit);
        
        if (questions.isEmpty()) {
            System.out.println("No questions found. Enroll in some courses first!");
            return;
        }
        
        // Shuffle the list just in case the SQL RAND() wasn't perfect
        Collections.shuffle(questions); 
        
        int score = 0;
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            System.out.printf("\nQuestion %d/%d: %s\n", (i + 1), questions.size(), q.getQuestionText());
            System.out.println("1. " + q.getOption1());
            System.out.println("2. " + q.getOption2());
            System.out.println("3. " + q.getOption3());
            System.out.println("4. " + q.getOption4());
            System.out.print("Your answer (1-4): ");
            
            int answer = getIntInput();
            if (answer == q.getCorrectOption()) {
                score++;
                System.out.println("Correct!");
            } else {
                System.out.println("Incorrect. The correct answer was " + q.getCorrectOption());
                if (q.getHint() != null && !q.getHint().isEmpty()) {
                    System.out.println("Hint: " + q.getHint());
                }
            }
        }
        
        double percentage = (double) score / questions.size() * 100;
        System.out.printf("\nQuiz finished! Your score: %d/%d (%.0f%%)\n", score, questions.size(), percentage);
    }
    
    private static void handleResetProgress(Student student) {
        System.out.println("\n--- Reset Course Progress ---");
        List<Course> registeredCourses = progressDAO.getRegisteredCourses(student.getId());

        if (registeredCourses.isEmpty()) {
            System.out.println("You are not registered in any courses.");
            return;
        }

        for (int i = 0; i < registeredCourses.size(); i++) {
            System.out.printf("%d. %s\n", (i + 1), registeredCourses.get(i).getTitle());
        }

        System.out.print("Select a course to RESET (0 to cancel): ");
        int choice = getIntInput();
        if (choice > 0 && choice <= registeredCourses.size()) {
            Course selectedCourse = registeredCourses.get(choice - 1);
            
            System.out.print("ARE YOU SURE? This will delete all progress for '" + selectedCourse.getTitle() + "'. (y/n): ");
            String confirm = scanner.nextLine();
            
            if (confirm.equalsIgnoreCase("y")) {
                if (progressDAO.resetProgress(student.getId(), selectedCourse.getId())) {
                    System.out.println("Progress has been reset.");
                } else {
                    System.out.println("Failed to reset progress.");
                }
            } else {
                System.out.println("Reset cancelled.");
            }
        }
    }
    
    private static void handleProfile(Student student) {
        System.out.println("\n--- 👤 My Profile ---");
        System.out.println("User ID: " + student.getId());
        System.out.println("Username: " + student.getUsername());
        System.out.println("Role: " + student.getRole());
    }

    private static void showCourseProgress(Student student, Course course) {
        System.out.println("\n--- Progress for: " + course.getTitle() + " ---");
        List<Level> levels = progressDAO.getCourseProgress(student.getId(), course.getId());
        
        if (levels.isEmpty()) {
            System.out.println("This course has no levels yet.");
            return;
        }

        boolean previousLevelCompleted = true;
        Level firstUnlockedLevel = null;

        for (Level level : levels) {
            String status;
            if (level.isCompleted()) {
                status = "[COMPLETED]";
            } else if (previousLevelCompleted) {
                status = "[UNLOCKED]";
                if (firstUnlockedLevel == null) firstUnlockedLevel = level;
                previousLevelCompleted = false; 
            } else {
                status = "[LOCKED]";
            }
            System.out.printf("%d. %s %s\n", level.getLevelOrder(), level.getTitle(), status);
        }

        if (firstUnlockedLevel != null) {
            System.out.print("Start level '" + firstUnlockedLevel.getTitle() + "'? (y/n): ");
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("y")) {
                startLevel(student, firstUnlockedLevel);
            }
        } else if (!levels.isEmpty()) {
            System.out.println("You have completed this course!");
        }
    }

    private static void startLevel(Student student, Level level) {
        System.out.println("\n--- 📖 Learning: " + level.getTitle() + " ---");
        System.out.println(level.getLearningText()); 
        System.out.println("\nPress Enter to start the quiz...");
        scanner.nextLine(); 

        List<Question> questions = levelDAO.getQuestionsByLevel(level.getId());
        if (questions.isEmpty()) {
            System.out.println("This level has no questions. Marking as complete.");
            progressDAO.completeLevel(student.getId(), level.getId());
            unlockNextLevel(student, level.getCourseId(), level.getLevelOrder() + 1);
            return;
        }

        int score = 0;
        for (Question q : questions) {
            System.out.println("\nQuestion: " + q.getQuestionText());
            System.out.println("1. " + q.getOption1());
            System.out.println("2. " + q.getOption2());
            System.out.println("3. " + q.getOption3());
            System.out.println("4. " + q.getOption4());
            System.out.print("Your answer (1-4): ");
            
            int answer = getIntInput();
            if (answer == q.getCorrectOption()) {
                score++;
                System.out.println("Correct!");
            } else {
                System.out.println("Incorrect. The correct answer was " + q.getCorrectOption());
                // --- NEW: SHOW HINT ---
                if (q.getHint() != null && !q.getHint().isEmpty()) {
                    System.out.println("Hint: " + q.getHint());
                }
            }
        }

        double percentage = (double) score / questions.size() * 100;
        System.out.printf("\nQuiz finished! Your score: %d/%d (%.0f%%)\n", score, questions.size(), percentage);

        if (percentage >= 80) {
            System.out.println("Congratulations, you passed!");
            progressDAO.completeLevel(student.getId(), level.getId());
            unlockNextLevel(student, level.getCourseId(), level.getLevelOrder() + 1);
        } else {
            System.out.println("You did not pass. Please try again later.");
        }
    }
    
    private static void unlockNextLevel(Student student, int courseId, int nextLevelOrder) {
        List<Level> allLevels = levelDAO.getLevelsByCourse(courseId);
        Level nextLevel = null;
        for (Level l : allLevels) {
            if (l.getLevelOrder() == nextLevelOrder) {
                nextLevel = l;
                break;
            }
        }

        if (nextLevel != null) {
            progressDAO.unlockFirstLevel(student.getId(), nextLevel.getId());
            System.out.println("You have unlocked: " + nextLevel.getTitle());
        } else {
            System.out.println("You have completed all levels in this course!");
        }
    }


    // --- CREATOR MENU HANDLERS (ALL NEW) ---

    private static boolean handleCreatorMenu(Creator creator, int choice) {
        switch (choice) {
            case 1:
                createCourse(creator);
                break;
            case 2:
                viewCreatorCourses(creator);
                break;
            case 3:
                addLevelToCourse(creator);
                break;
            case 4:
                addQuestionToLevel(creator);
                break;
            case 5:
                handleCourseAnalytics(creator);
                break;
            case 6:
                handleDeleteCourse(creator);
                break;
            case 7:
                System.out.println("Logging out...");
                return false; 
            default:
                System.out.println("Invalid option.");
        }
        return true; 
    }
    
    private static void createCourse(Creator creator) {
        System.out.println("\n--- Create New Course ---");
        System.out.print("Enter course title: ");
        String title = scanner.nextLine();
        System.out.print("Enter course description: ");
        String description = scanner.nextLine();

        Course newCourse = new Course();
        newCourse.setTitle(title);
        newCourse.setDescription(description);
        newCourse.setCreatorId(creator.getId()); 

        if (courseDAO.createCourse(newCourse)) {
            System.out.println("Course created successfully!");
        } else {
            System.out.println("Failed to create course.");
        }
    }

    private static void viewCreatorCourses(Creator creator) {
        System.out.println("\n--- My Created Courses ---");
        List<Course> myCourses = courseDAO.getCoursesByCreator(creator.getId());
        
        if (myCourses.isEmpty()) {
            System.out.println("You haven't created any courses yet.");
            return;
        }
        
        for (Course course : myCourses) {
            System.out.printf("- %s (ID: %d)\n", course.getTitle(), course.getId());
        }
    }
    
    private static void addLevelToCourse(Creator creator) {
        System.out.println("\n--- Add Level to Course ---");
        Course selectedCourse = selectCreatorCourse(creator);
        if (selectedCourse == null) return;

        System.out.print("Enter new level title: ");
        String title = scanner.nextLine();
        System.out.print("Enter learning text (the lesson): ");
        String text = scanner.nextLine();
        
        Level newLevel = new Level();
        newLevel.setCourseId(selectedCourse.getId());
        newLevel.setTitle(title);
        newLevel.setLearningText(text);

        if (levelDAO.addLevel(newLevel)) {
            System.out.println("Level added successfully!");
        } else {
            System.out.println("Failed to add level.");
        }
    }
    
    private static void addQuestionToLevel(Creator creator) {
        System.out.println("\n--- Add Question to Level ---");
        
        Course selectedCourse = selectCreatorCourse(creator);
        if (selectedCourse == null) return;

        Level selectedLevel = selectCourseLevel(selectedCourse);
        if (selectedLevel == null) return;

        // 3. Get question details
        System.out.print("Enter the question: ");
        String qText = scanner.nextLine();
        System.out.print("Enter option 1: ");
        String o1 = scanner.nextLine();
        System.out.print("Enter option 2: ");
        String o2 = scanner.nextLine();
        System.out.print("Enter option 3: ");
        String o3 = scanner.nextLine();
        System.out.print("Enter option 4: ");
        String o4 = scanner.nextLine();
        System.out.print("Enter the correct answer number (1-4): ");
        int correct = getIntInput();
        
        // --- NEW: Ask for optional hint ---
        System.out.print("Enter an optional hint (or press Enter to skip): ");
        String hint = scanner.nextLine();

        Question newQuestion = new Question();
        newQuestion.setLevelId(selectedLevel.getId());
        newQuestion.setQuestionText(qText);
        newQuestion.setOption1(o1);
        newQuestion.setOption2(o2);
        newQuestion.setOption3(o3);
        newQuestion.setOption4(o4);
        newQuestion.setCorrectOption(correct);
        newQuestion.setHint(hint); // Set the hint

        if (levelDAO.addQuestion(newQuestion)) {
            System.out.println("Question added successfully!");
        } else {
            System.out.println("Failed to add question.");
        }
    }

    private static void handleCourseAnalytics(Creator creator) {
        System.out.println("\n--- 📊 Course Analytics ---");
        Course selectedCourse = selectCreatorCourse(creator);
        if (selectedCourse == null) return;
        
        int levelCount = levelDAO.getLevelsByCourse(selectedCourse.getId()).size();
        int enrollmentCount = courseDAO.getEnrollmentCount(selectedCourse.getId());
        
        System.out.println("\nStats for '" + selectedCourse.getTitle() + "':");
        System.out.println("- Total Levels: " + levelCount);
        System.out.println("- Total Students Enrolled: " + enrollmentCount);
    }
    
    private static void handleDeleteCourse(Creator creator) {
        System.out.println("\n--- ⛔ Delete Course ---");
        Course selectedCourse = selectCreatorCourse(creator);
        if (selectedCourse == null) return;
        
        System.out.println("WARNING: This will permanently delete this course,");
        System.out.println("all its levels, all its questions, and all student progress.");
        System.out.print("To confirm, type the course title ('" + selectedCourse.getTitle() + "'): ");
        String confirm = scanner.nextLine();
        
        if (confirm.equals(selectedCourse.getTitle())) {
            if (courseDAO.deleteCourse(selectedCourse.getId())) {
                System.out.println("Course deleted successfully.");
            } else {
                System.out.println("Failed to delete course.");
            }
        } else {
            System.out.println("Name did not match. Deletion cancelled.");
        }
    }


    // --- HELPER UTILITIES ---
    
    // (A new helper to reduce repeated code)
    private static Course selectCreatorCourse(Creator creator) {
        List<Course> myCourses = courseDAO.getCoursesByCreator(creator.getId());
        if (myCourses.isEmpty()) {
            System.out.println("You must create a course first.");
            return null;
        }

        for (int i = 0; i < myCourses.size(); i++) {
            System.out.printf("%d. %s\n", (i + 1), myCourses.get(i).getTitle());
        }
        
        System.out.print("Choose a course (0 to cancel): ");
        int choice = getIntInput();
        if (choice > 0 && choice <= myCourses.size()) {
            return myCourses.get(choice - 1);
        }
        return null; // Cancelled
    }
    
    // (A new helper to reduce repeated code)
    private static Level selectCourseLevel(Course course) {
        List<Level> levels = levelDAO.getLevelsByCourse(course.getId());
        if (levels.isEmpty()) {
            System.out.println("This course has no levels. Add a level first.");
            return null;
        }
        
        for (int i = 0; i < levels.size(); i++) {
            System.out.printf("%d. %s\n", (i + 1), levels.get(i).getTitle());
        }
        
        System.out.print("Choose a level (0 to cancel): ");
        int levelChoice = getIntInput();
        if (levelChoice > 0 && levelChoice <= levels.size()) {
            return levels.get(levelChoice - 1);
        }
        return null; // Cancelled
    }

    private static int getIntInput() {
        while (true) {
            try {
                String line = scanner.nextLine();
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a number: ");
            }
        }
    }
}