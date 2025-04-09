package com.college.ui;

import com.college.utils.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.*;
import java.util.Arrays;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class AdminDashboardView extends BaseDashboardView {
    private TableView<User> userTable;
    private TableView<Course> courseTable;
    private TextField usernameField;
    private PasswordField passwordField;
    private ComboBox<String> roleComboBox;
    private TextField courseCodeField;
    private TextField courseNameField;
    private TextArea courseDescField;
    private ComboBox<String> teacherComboBox;

    public AdminDashboardView(String username) {
        super(username);
    }

    @Override
    protected void setupSidebar() {
        Button manageUsersBtn = new Button("Manage Users");
        manageUsersBtn.setMaxWidth(Double.MAX_VALUE);
        manageUsersBtn.getStyleClass().add("sidebar-button");
        manageUsersBtn.setOnAction(e -> showUserManagement());

        Button manageCoursesBtn = new Button("Manage Courses");
        manageCoursesBtn.setMaxWidth(Double.MAX_VALUE);
        manageCoursesBtn.getStyleClass().add("sidebar-button");
        manageCoursesBtn.setOnAction(e -> showCourseManagement());

        sidebar.getChildren().addAll(manageUsersBtn, manageCoursesBtn);
    }

    @Override
    protected void setupContentArea() {
        showUserManagement(); // Default view
    }

    private void showUserManagement() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // User input fields
        usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(300);

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(300);

        roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("Student", "Teacher");
        roleComboBox.setValue("Student");
        roleComboBox.setMaxWidth(300);

        Button addUserBtn = new Button("Add User");
        addUserBtn.setMaxWidth(300);

        HBox buttonBox = new HBox(10);
        Button editUserBtn = new Button("Edit Selected");
        Button deleteUserBtn = new Button("Delete Selected");
        buttonBox.getChildren().addAll(addUserBtn, editUserBtn, deleteUserBtn);

        // Create users table
        userTable = new TableView<>();
        userTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        userTable.getColumns().addAll(Arrays.asList(usernameCol, roleCol));

        // Add user button action
        addUserBtn.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String role = roleComboBox.getValue();

            if (username.isEmpty() || password.isEmpty()) {
                showAlert("Error", "Please fill in all fields");
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                // Check if username already exists
                PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT username FROM users WHERE username = ?"
                );
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    showAlert("Error", "Username already exists");
                    return;
                }

                // Insert new user
                PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO users (username, password, role) VALUES (?, ?, ?)"
                );
                stmt.setString(1, username);
                stmt.setString(2, password);
                stmt.setString(3, role);
                stmt.executeUpdate();

                loadUsers();
                clearUserFields();
            } catch (SQLException ex) {
                showAlert("Error", "Failed to add user: " + ex.getMessage());
            }
        });

        // Edit user button action
        editUserBtn.setOnAction(e -> {
            User selectedUser = userTable.getSelectionModel().getSelectedItem();
            if (selectedUser == null) {
                showAlert("Error", "Please select a user to edit");
                return;
            }

            String newUsername = usernameField.getText();
            String newPassword = passwordField.getText();
            String newRole = roleComboBox.getValue();

            if (newUsername.isEmpty()) {
                showAlert("Error", "Username cannot be empty");
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                PreparedStatement stmt;
                if (!newPassword.isEmpty()) {
                    stmt = conn.prepareStatement(
                        "UPDATE users SET username = ?, password = ?, role = ? WHERE username = ?"
                    );
                    stmt.setString(1, newUsername);
                    stmt.setString(2, newPassword);
                    stmt.setString(3, newRole);
                    stmt.setString(4, selectedUser.getUsername());
                } else {
                    stmt = conn.prepareStatement(
                        "UPDATE users SET username = ?, role = ? WHERE username = ?"
                    );
                    stmt.setString(1, newUsername);
                    stmt.setString(2, newRole);
                    stmt.setString(3, selectedUser.getUsername());
                }
                stmt.executeUpdate();

                loadUsers();
                clearUserFields();
            } catch (SQLException ex) {
                showAlert("Error", "Failed to update user: " + ex.getMessage());
            }
        });

        // Delete user button action
        deleteUserBtn.setOnAction(e -> {
            User selectedUser = userTable.getSelectionModel().getSelectedItem();
            if (selectedUser == null) {
                showAlert("Error", "Please select a user to delete");
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Delete");
            alert.setHeaderText("Delete User");
            alert.setContentText("Are you sure you want to delete user: " + selectedUser.getUsername() + "?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM users WHERE username = ?"
                    );
                    stmt.setString(1, selectedUser.getUsername());
                    stmt.executeUpdate();

                    loadUsers();
                    clearUserFields();
                } catch (SQLException ex) {
                    showAlert("Error", "Failed to delete user: " + ex.getMessage());
                }
            }
        });

        // User table selection listener
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                usernameField.setText(newSelection.getUsername());
                roleComboBox.setValue(newSelection.getRole());
                passwordField.clear(); // Don't show password
            }
        });

        content.getChildren().addAll(
            new Label("User Management"),
            new Label("Username:"),
            usernameField,
            new Label("Password:"),
            passwordField,
            new Label("Role:"),
            roleComboBox,
            buttonBox,
            userTable
        );

        loadUsers();
        setContent(content);
    }

    private void showCourseManagement() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Course input fields
        courseCodeField = new TextField();
        courseCodeField.setPromptText("Course Code");
        courseCodeField.setMaxWidth(300);

        courseNameField = new TextField();
        courseNameField.setPromptText("Course Name");
        courseNameField.setMaxWidth(300);

        courseDescField = new TextArea();
        courseDescField.setPromptText("Course Description");
        courseDescField.setMaxWidth(300);
        courseDescField.setPrefRowCount(3);

        // Teacher selection
        teacherComboBox = new ComboBox<>();
        teacherComboBox.setPromptText("Select Teacher");
        teacherComboBox.setMaxWidth(300);
        loadTeachers();

        // Student enrollment section
        Label enrollmentLabel = new Label("Enroll Students");
        enrollmentLabel.getStyleClass().add("section-label");

        ComboBox<String> studentComboBox = new ComboBox<>();
        studentComboBox.setPromptText("Select Student");
        studentComboBox.setMaxWidth(300);
        loadAvailableStudents(studentComboBox);

        Button enrollStudentBtn = new Button("Enroll Student");
        enrollStudentBtn.setMaxWidth(300);
        enrollStudentBtn.getStyleClass().add("action-button");

        // Course management buttons
        Button addCourseBtn = new Button("Add Course");
        addCourseBtn.setMaxWidth(300);

        HBox buttonBox = new HBox(10);
        Button editCourseBtn = new Button("Edit Selected");
        Button deleteCourseBtn = new Button("Delete Selected");
        buttonBox.getChildren().addAll(addCourseBtn, editCourseBtn, deleteCourseBtn);

        // Create courses table
        courseTable = new TableView<>();
        courseTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        TableColumn<Course, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));

        TableColumn<Course, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Course, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<Course, String> teacherCol = new TableColumn<>("Assigned Teacher");
        teacherCol.setCellValueFactory(new PropertyValueFactory<>("teacher"));

        courseTable.getColumns().addAll(Arrays.asList(codeCol, nameCol, descriptionCol, teacherCol));

        // Create enrolled students table
        TableView<EnrolledStudent> enrolledStudentsTable = new TableView<>();
        enrolledStudentsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        TableColumn<EnrolledStudent, String> studentCol = new TableColumn<>("Student");
        studentCol.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<EnrolledStudent, String> gradeCol = new TableColumn<>("Grade");
        gradeCol.setCellValueFactory(new PropertyValueFactory<>("grade"));

        enrolledStudentsTable.getColumns().addAll(Arrays.asList(studentCol, gradeCol));

        // Course selection listener to update enrolled students
        courseTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                loadEnrolledStudents(enrolledStudentsTable, newSelection.getCode());
            }
        });

        // Enroll student button action
        enrollStudentBtn.setOnAction(e -> {
            Course selectedCourse = courseTable.getSelectionModel().getSelectedItem();
            String selectedStudent = studentComboBox.getValue();

            if (selectedCourse == null || selectedStudent == null) {
                showAlert("Error", "Please select both a course and a student");
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                // Check if student is already enrolled
                PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT 1 FROM student_courses sc " +
                    "JOIN users u ON sc.student_id = u.id " +
                    "WHERE u.username = ? AND sc.course_code = ?"
                );
                checkStmt.setString(1, selectedStudent);
                checkStmt.setString(2, selectedCourse.getCode());
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    showAlert("Error", "Student is already enrolled in this course");
                    return;
                }

                // Enroll the student
                PreparedStatement enrollStmt = conn.prepareStatement(
                    "INSERT INTO student_courses (student_id, course_code) " +
                    "SELECT u.id, ? FROM users u WHERE u.username = ?"
                );
                enrollStmt.setString(1, selectedCourse.getCode());
                enrollStmt.setString(2, selectedStudent);
                enrollStmt.executeUpdate();

                showAlert("Success", "Student enrolled successfully");
                loadEnrolledStudents(enrolledStudentsTable, selectedCourse.getCode());
                loadAvailableStudents(studentComboBox); // Refresh available students
            } catch (SQLException ex) {
                showAlert("Error", "Failed to enroll student: " + ex.getMessage());
            }
        });

        // Add all components to the content
        content.getChildren().addAll(
            new Label("Course Management"),
            new Label("Course Code:"),
            courseCodeField,
            new Label("Course Name:"),
            courseNameField,
            new Label("Description:"),
            courseDescField,
            new Label("Assign Teacher:"),
            teacherComboBox,
            buttonBox,
            new Label("Course List"),
            courseTable,
            enrollmentLabel,
            new Label("Select Student:"),
            studentComboBox,
            enrollStudentBtn,
            new Label("Enrolled Students"),
            enrolledStudentsTable
        );

        loadCourses();
        setContent(content);
    }

    private void loadTeachers() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT username FROM users WHERE role = 'Teacher' ORDER BY username"
            );
            ResultSet rs = stmt.executeQuery();

            ObservableList<String> teachers = FXCollections.observableArrayList();
            while (rs.next()) {
                teachers.add(rs.getString("username"));
            }
            teacherComboBox.setItems(teachers);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load teachers: " + e.getMessage());
        }
    }

    private void loadCourses() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT c.code, c.name, c.description, u.username as teacher " +
                "FROM courses c " +
                "LEFT JOIN teacher_courses tc ON c.code = tc.course_code " +
                "LEFT JOIN users u ON tc.teacher_id = u.id"
            );
            ResultSet rs = stmt.executeQuery();

            ObservableList<Course> courses = FXCollections.observableArrayList();
            while (rs.next()) {
                courses.add(new Course(
                    rs.getString("code"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("teacher")
                ));
            }
            courseTable.setItems(courses);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load courses: " + e.getMessage());
        }
    }

    private void loadUsers() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT username, role FROM users WHERE role != 'admin'"
            );
            ResultSet rs = stmt.executeQuery();

            ObservableList<User> users = FXCollections.observableArrayList();
            while (rs.next()) {
                users.add(new User(
                    rs.getString("username"),
                    rs.getString("role")
                ));
            }
            userTable.setItems(users);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load users: " + e.getMessage());
        }
    }

    private void clearUserFields() {
        usernameField.clear();
        passwordField.clear();
        roleComboBox.setValue("Student");
    }

    private void clearCourseFields() {
        courseCodeField.clear();
        courseNameField.clear();
        courseDescField.clear();
        teacherComboBox.setValue(null);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void loadAvailableStudents(ComboBox<String> studentComboBox) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT username FROM users WHERE role = 'Student' ORDER BY username"
            );
            ResultSet rs = stmt.executeQuery();

            ObservableList<String> students = FXCollections.observableArrayList();
            while (rs.next()) {
                students.add(rs.getString("username"));
            }
            studentComboBox.setItems(students);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load students: " + e.getMessage());
        }
    }

    private void loadEnrolledStudents(TableView<EnrolledStudent> table, String courseCode) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT u.username, sc.grade FROM users u " +
                "JOIN student_courses sc ON u.id = sc.student_id " +
                "WHERE sc.course_code = ? ORDER BY u.username"
            );
            stmt.setString(1, courseCode);
            ResultSet rs = stmt.executeQuery();

            ObservableList<EnrolledStudent> students = FXCollections.observableArrayList();
            while (rs.next()) {
                students.add(new EnrolledStudent(
                    rs.getString("username"),
                    rs.getString("grade")
                ));
            }
            table.setItems(students);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load enrolled students: " + e.getMessage());
        }
    }

    // Data classes
    public static class User {
        private final String username;
        private final String role;

        public User(String username, String role) {
            this.username = username;
            this.role = role;
        }

        public String getUsername() { return username; }
        public String getRole() { return role; }
    }

    public static class Course {
        private final String code;
        private final String name;
        private final String description;
        private final String teacher;

        public Course(String code, String name, String description, String teacher) {
            this.code = code;
            this.name = name;
            this.description = description;
            this.teacher = teacher;
        }

        public String getCode() { return code; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getTeacher() { return teacher; }
    }

    public static class EnrolledStudent {
        private final String username;
        private final String grade;

        public EnrolledStudent(String username, String grade) {
            this.username = username;
            this.grade = grade;
        }

        public String getUsername() { return username; }
        public String getGrade() { return grade; }
    }
}
