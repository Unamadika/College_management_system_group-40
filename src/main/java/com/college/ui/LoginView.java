package com.college.ui;

import com.college.utils.DatabaseConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.shape.SVGPath;
import java.sql.*;

public class LoginView {
    private VBox root;
    private TextField usernameField;
    private PasswordField passwordField;
    private ComboBox<String> roleComboBox;
    private Label messageLabel;
    private Button loginButton;
    private Button registerButton;

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    public LoginView() {
        createView();
        setupEventHandlers();
    }

    public Parent getView() {
        return root;
    }

    private void createView() {
        root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setPrefSize(800, 600);
        root.getStyleClass().add("login-container");

        // Create hamburger menu
        MenuButton hamburgerMenu = createHamburgerMenu();
        hamburgerMenu.getStyleClass().add("hamburger-menu");

        // Top bar with hamburger
        HBox topBar = new HBox(hamburgerMenu);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("College Management System");
        titleLabel.getStyleClass().add("title-label");

        usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(300);
        usernameField.getStyleClass().add("input-field");

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(300);
        passwordField.getStyleClass().add("input-field");

        roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("Student", "Teacher");
        roleComboBox.setValue("Student");
        roleComboBox.setMaxWidth(300);
        roleComboBox.getStyleClass().add("role-combo");

        loginButton = new Button("Login");
        loginButton.setMaxWidth(300);
        loginButton.getStyleClass().add("login-button");

        registerButton = new Button("Register");
        registerButton.setMaxWidth(300);
        registerButton.getStyleClass().add("register-button");

        messageLabel = new Label();
        messageLabel.getStyleClass().add("message-label");

        VBox contentBox = new VBox(15);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.getChildren().addAll(
            titleLabel,
            new Label("Username:"),
            usernameField,
            new Label("Password:"),
            passwordField,
            new Label("Role:"),
            roleComboBox,
            loginButton,
            registerButton,
            messageLabel
        );

        root.getChildren().addAll(topBar, contentBox);
    }

    private MenuButton createHamburgerMenu() {
        // Create hamburger icon using SVG
        SVGPath hamburgerIcon = new SVGPath();
        hamburgerIcon.setContent("M3,6H21V8H3V6M3,11H21V13H3V11M3,16H21V18H3V16Z");
        hamburgerIcon.setScaleX(0.8);
        hamburgerIcon.setScaleY(0.8);

        MenuButton menuButton = new MenuButton();
        menuButton.setGraphic(hamburgerIcon);
        
        MenuItem adminLoginItem = new MenuItem("Admin Login");
        adminLoginItem.setOnAction(e -> handleAdminLogin());
        
        menuButton.getItems().add(adminLoginItem);
        return menuButton;
    }

    private void setupEventHandlers() {
        loginButton.setOnAction(e -> handleLogin());
        registerButton.setOnAction(e -> handleRegister());
        usernameField.setOnAction(e -> handleLogin());
        passwordField.setOnAction(e -> handleLogin());
    }

    private void handleRegister() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Register New User");

        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));
        content.setMinWidth(300);
        content.setMaxWidth(300);

        TextField regUsername = new TextField();
        regUsername.setPromptText("Username");
        regUsername.setPrefWidth(200);

        PasswordField regPassword = new PasswordField();
        regPassword.setPromptText("Password");
        regPassword.setPrefWidth(200);

        PasswordField confirmPassword = new PasswordField();
        confirmPassword.setPromptText("Confirm Password");
        confirmPassword.setPrefWidth(200);

        ComboBox<String> regRole = new ComboBox<>();
        regRole.getItems().addAll("Student", "Teacher");
        regRole.setValue("Student");
        regRole.setPrefWidth(200);

        Button registerBtn = new Button("Register");
        registerBtn.setPrefWidth(200);
        registerBtn.getStyleClass().add("action-button");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");
        errorLabel.setWrapText(true);

        registerBtn.setOnAction(e -> {
            String username = regUsername.getText().trim();
            String password = regPassword.getText();
            String confirm = confirmPassword.getText();
            String role = regRole.getValue();

            if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                errorLabel.setText("Please fill in all fields");
                return;
            }

            if (!password.equals(confirm)) {
                errorLabel.setText("Passwords do not match");
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                // Check if username already exists
                String checkQuery = "SELECT username FROM users WHERE username = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    errorLabel.setText("Username already exists");
                    return;
                }

                // Insert new user
                String insertQuery = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setString(1, username);
                insertStmt.setString(2, password);
                insertStmt.setString(3, role);
                insertStmt.executeUpdate();

                dialog.close();
                messageLabel.setText("Registration successful! Please login.");
                messageLabel.setStyle("-fx-text-fill: green;");

            } catch (SQLException ex) {
                errorLabel.setText("Error during registration: " + ex.getMessage());
            }
        });

        content.getChildren().addAll(
            new Label("Register New User"),
            new Label("Username:"),
            regUsername,
            new Label("Password:"),
            regPassword,
            new Label("Confirm Password:"),
            confirmPassword,
            new Label("Role:"),
            regRole,
            registerBtn,
            errorLabel
        );

        Scene scene = new Scene(content);
        scene.getStylesheets().add(getClass().getResource("/styles/login.css").toExternalForm());
        
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both username and password");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT id, username, role FROM users WHERE username = ? AND password = ? AND role = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                openDashboard(role, username);
            } else {
                messageLabel.setText("Invalid credentials");
                messageLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (Exception e) {
            messageLabel.setText("Database error: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void handleAdminLogin() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Admin Login");

        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));
        content.setMinWidth(300);
        content.setMaxWidth(300);

        TextField adminUsername = new TextField();
        adminUsername.setPromptText("Admin Username");
        adminUsername.setPrefWidth(200);

        PasswordField adminPassword = new PasswordField();
        adminPassword.setPromptText("Admin Password");
        adminPassword.setPrefWidth(200);

        Button loginBtn = new Button("Login");
        loginBtn.setPrefWidth(200);
        loginBtn.getStyleClass().add("action-button");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");
        errorLabel.setWrapText(true);

        loginBtn.setOnAction(e -> {
            if (adminUsername.getText().equals(ADMIN_USERNAME) && 
                adminPassword.getText().equals(ADMIN_PASSWORD)) {
                dialog.close();
                openDashboard("Admin", adminUsername.getText());
            } else {
                errorLabel.setText("Invalid admin credentials");
            }
        });

        content.getChildren().addAll(
            new Label("Admin Login"),
            new Label("Username:"),
            adminUsername,
            new Label("Password:"),
            adminPassword,
            loginBtn,
            errorLabel
        );

        Scene scene = new Scene(content);
        scene.getStylesheets().add(getClass().getResource("/styles/login.css").toExternalForm());
        
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void openDashboard(String role, String username) {
        try {
            Parent dashboard;
            switch (role) {
                case "Admin":
                    dashboard = new AdminDashboardView(username).getView();
                    break;
                case "Teacher":
                    dashboard = new TeacherDashboardView(username).getView();
                    break;
                case "Student":
                    dashboard = new StudentDashboardView(username).getView();
                    break;
                default:
                    throw new IllegalArgumentException("Invalid role");
            }

            Scene scene = new Scene(dashboard, 1024, 768);
            scene.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

            Stage stage = (Stage) root.getScene().getWindow();
            stage.setScene(scene);
            stage.setResizable(true);
            stage.centerOnScreen();

        } catch (Exception e) {
            messageLabel.setText("Error opening dashboard: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }
}
