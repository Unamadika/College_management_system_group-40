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

        courseTable.getColumns().addAll(Arrays.asList(codeCol, nameCol, descriptionCol));

        // Add course button action
        addCourseBtn.setOnAction(e -> {
            String code = courseCodeField.getText();
            String name = courseNameField.getText();
            String description = courseDescField.getText();

            if (code.isEmpty() || name.isEmpty()) {
                showAlert("Error", "Course code and name are required");
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                // Check if course code already exists
                PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT code FROM courses WHERE code = ?"
                );
                checkStmt.setString(1, code);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    showAlert("Error", "Course code already exists");
                    return;
                }

                // Insert new course
                PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO courses (code, name, description) VALUES (?, ?, ?)"
                );
                stmt.setString(1, code);
                stmt.setString(2, name);
                stmt.setString(3, description);
                stmt.executeUpdate();

                loadCourses();
                clearCourseFields();
            } catch (SQLException ex) {
                showAlert("Error", "Failed to add course: " + ex.getMessage());
            }
        });

        // Edit course button action
        editCourseBtn.setOnAction(e -> {
            Course selectedCourse = courseTable.getSelectionModel().getSelectedItem();
            if (selectedCourse == null) {
                showAlert("Error", "Please select a course to edit");
                return;
            }

            String newCode = courseCodeField.getText();
            String newName = courseNameField.getText();
            String newDesc = courseDescField.getText();

            if (newCode.isEmpty() || newName.isEmpty()) {
                showAlert("Error", "Course code and name are required");
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE courses SET code = ?, name = ?, description = ? WHERE code = ?"
                );
                stmt.setString(1, newCode);
                stmt.setString(2, newName);
                stmt.setString(3, newDesc);
                stmt.setString(4, selectedCourse.getCode());
                stmt.executeUpdate();

                loadCourses();
                clearCourseFields();
            } catch (SQLException ex) {
                showAlert("Error", "Failed to update course: " + ex.getMessage());
            }
        });

        // Delete course button action
        deleteCourseBtn.setOnAction(e -> {
            Course selectedCourse = courseTable.getSelectionModel().getSelectedItem();
            if (selectedCourse == null) {
                showAlert("Error", "Please select a course to delete");
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Delete");
            alert.setHeaderText("Delete Course");
            alert.setContentText("Are you sure you want to delete course: " + selectedCourse.getCode() + "?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM courses WHERE code = ?"
                    );
                    stmt.setString(1, selectedCourse.getCode());
                    stmt.executeUpdate();

                    loadCourses();
                    clearCourseFields();
                } catch (SQLException ex) {
                    showAlert("Error", "Failed to delete course: " + ex.getMessage());
                }
            }
        });

        // Course table selection listener
        courseTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                courseCodeField.setText(newSelection.getCode());
                courseNameField.setText(newSelection.getName());
                courseDescField.setText(newSelection.getDescription());
            }
        });

        content.getChildren().addAll(
            new Label("Course Management"),
            new Label("Course Code:"),
            courseCodeField,
            new Label("Course Name:"),
            courseNameField,
            new Label("Description:"),
            courseDescField,
            buttonBox,
            courseTable
        );

        loadCourses();
        setContent(content);
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

    private void loadCourses() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT code, name, description FROM courses"
            );
            ResultSet rs = stmt.executeQuery();

            ObservableList<Course> courses = FXCollections.observableArrayList();
            while (rs.next()) {
                courses.add(new Course(
                    rs.getString("code"),
                    rs.getString("name"),
                    rs.getString("description")
                ));
            }
            courseTable.setItems(courses);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load courses: " + e.getMessage());
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
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
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

        public Course(String code, String name, String description) {
            this.code = code;
            this.name = name;
            this.description = description;
        }

        public String getCode() { return code; }
        public String getName() { return name; }
        public String getDescription() { return description; }
    }
}
