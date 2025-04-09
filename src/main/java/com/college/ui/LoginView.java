package com.college.ui;

import com.college.utils.DatabaseConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.sql.*;

public class LoginView {
    private StackPane root;
    private VBox contentRoot;
    private TextField usernameField;
    private PasswordField passwordField;
    private ComboBox<String> roleComboBox;
    private Label messageLabel;
    private Button loginButton;
    private MenuButton adminMenuButton;

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
        // Create main root with background
        root = new StackPane();
        root.setPrefSize(1920, 1080);
        root.setMinSize(1920, 1080);
        root.setMaxSize(1920, 1080);

        // Create admin menu button
        adminMenuButton = new MenuButton("☰");
        adminMenuButton.getStyleClass().add("hamburger-button");
        MenuItem adminLoginItem = new MenuItem("Admin Login");
        adminLoginItem.setOnAction(e -> showAdminLogin());
        adminMenuButton.getItems().add(adminLoginItem);

        // Position admin menu in top-left corner
        StackPane.setAlignment(adminMenuButton, Pos.TOP_LEFT);
        StackPane.setMargin(adminMenuButton, new Insets(20));

        // Load and set background image
        try {
            Image backgroundImage = new Image(getClass().getResourceAsStream("/images/image1.jpeg"), 1920, 1080, true, true);
            ImageView backgroundView = new ImageView(backgroundImage);
            backgroundView.setFitWidth(1920);
            backgroundView.setFitHeight(1080);
            backgroundView.setPreserveRatio(false);
            backgroundView.setSmooth(true);
            root.getChildren().add(backgroundView);
        } catch (Exception e) {
            System.err.println("Error loading background image: " + e.getMessage());
        }

        // Create content root for form
        contentRoot = new VBox();
        contentRoot.setAlignment(Pos.CENTER);
        contentRoot.getStyleClass().add("login-container");

        // Create form container
        VBox formContainer = new VBox(25);
        formContainer.getStyleClass().add("form-container");
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setMaxSize(450, 600);

        // Title with icon
        Label titleLabel = new Label("College Management System");
        titleLabel.getStyleClass().add("title-label");
        titleLabel.setWrapText(true);
        titleLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // Create fields container
        VBox fieldsContainer = new VBox(20);
        fieldsContainer.setAlignment(Pos.CENTER);
        fieldsContainer.setMaxWidth(400);

        // Username field with icon
        HBox usernameBox = new HBox(15);
        usernameBox.setAlignment(Pos.CENTER);
        Label userIcon = new Label("👤");
        userIcon.getStyleClass().add("field-icon");
        usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefWidth(300);
        usernameField.getStyleClass().add("input-field");
        usernameBox.getChildren().addAll(userIcon, usernameField);

        // Password field with icon
        HBox passwordBox = new HBox(15);
        passwordBox.setAlignment(Pos.CENTER);
        Label lockIcon = new Label("🔒");
        lockIcon.getStyleClass().add("field-icon");
        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefWidth(300);
        passwordField.getStyleClass().add("input-field");
        passwordBox.getChildren().addAll(lockIcon, passwordField);

        // Role selection with icon
        HBox roleBox = new HBox(15);
        roleBox.setAlignment(Pos.CENTER);
        Label roleIcon = new Label("👥");
        roleIcon.getStyleClass().add("field-icon");
        roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("Student", "Teacher");
        roleComboBox.setValue("Student");
        roleComboBox.setPrefWidth(300);
        roleComboBox.getStyleClass().add("input-field");
        roleBox.getChildren().addAll(roleIcon, roleComboBox);

        fieldsContainer.getChildren().addAll(usernameBox, passwordBox, roleBox);

        // Login button
        loginButton = new Button("Login");
        loginButton.setPrefWidth(300);
        loginButton.getStyleClass().addAll("login-button", "primary-button");

        messageLabel = new Label();
        messageLabel.getStyleClass().add("message-label");
        messageLabel.setWrapText(true);
        messageLabel.setAlignment(Pos.CENTER);
        messageLabel.setMaxWidth(350);

        formContainer.getChildren().addAll(
            titleLabel,
            fieldsContainer,
            loginButton,
            messageLabel
        );

        // Add form to content root
        contentRoot.getChildren().add(formContainer);
        
        // Add content root and admin menu to main root
        root.getChildren().addAll(contentRoot, adminMenuButton);
    }

    private void showAdminLogin() {
        // Create admin login dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Admin Login");
        dialog.setHeaderText(null);
        
        // Get the dialog pane and add styling
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/styles/login.css").toExternalForm());
        dialogPane.getStyleClass().add("admin-dialog");
        
        // Create the content
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setMaxWidth(400);
        content.getStyleClass().add("admin-login-content");

        // Username field with icon
        HBox usernameBox = new HBox(10);
        usernameBox.setAlignment(Pos.CENTER_LEFT);
        Label userIcon = new Label("👤");
        userIcon.getStyleClass().add("field-icon");
        TextField adminUsername = new TextField();
        adminUsername.setPromptText("Admin Username");
        adminUsername.setPrefWidth(300);
        adminUsername.getStyleClass().add("input-field");
        usernameBox.getChildren().addAll(userIcon, adminUsername);

        // Password field with icon
        HBox passwordBox = new HBox(10);
        passwordBox.setAlignment(Pos.CENTER_LEFT);
        Label lockIcon = new Label("🔒");
        lockIcon.getStyleClass().add("field-icon");
        PasswordField adminPassword = new PasswordField();
        adminPassword.setPromptText("Admin Password");
        adminPassword.setPrefWidth(300);
        adminPassword.getStyleClass().add("input-field");
        passwordBox.getChildren().addAll(lockIcon, adminPassword);

        Label titleLabel = new Label("Admin Login");
        titleLabel.getStyleClass().add("title-label");

        content.getChildren().addAll(
            titleLabel,
            usernameBox,
            passwordBox
        );

        dialogPane.setContent(content);
        dialogPane.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

        // Style the buttons
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.getStyleClass().addAll("login-button", "primary-button");
        okButton.setText("Login");
        
        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.getStyleClass().addAll("login-button", "secondary-button");

        // Add enter key handler
        adminPassword.setOnAction(e -> {
            if (adminUsername.getText().equals(ADMIN_USERNAME) && 
                adminPassword.getText().equals(ADMIN_PASSWORD)) {
                dialog.setResult(ButtonType.OK);
                dialog.close();
                openDashboard("Admin", ADMIN_USERNAME);
            } else {
                showError("Invalid admin credentials");
            }
        });

        // Handle the result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                if (adminUsername.getText().equals(ADMIN_USERNAME) && 
                    adminPassword.getText().equals(ADMIN_PASSWORD)) {
                    openDashboard("Admin", ADMIN_USERNAME);
                } else {
                    showError("Invalid admin credentials");
                }
            }
            return dialogButton;
        });

        dialog.showAndWait();
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        // Try to login
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM users WHERE username = ? AND password = ? AND role = ?"
            );
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                showSuccess("Login successful!");
                openDashboard(role, username);
            } else {
                showError("Invalid credentials");
            }
        } catch (SQLException e) {
            showError("Error during login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().removeAll("error-label", "success-label");
        messageLabel.getStyleClass().add("success-label");
    }

    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().removeAll("error-label", "success-label");
        messageLabel.getStyleClass().add("error-label");
    }

    private void openDashboard(String role, String username) {
        try {
            // Create fade out transition
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), root);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            fadeOut.setOnFinished(e -> {
                try {
                    Parent dashboard;
                    Stage stage = (Stage) root.getScene().getWindow();
                    
                    switch (role) {
                        case "Admin":
                            AdminDashboardView adminView = new AdminDashboardView(username);
                            dashboard = adminView.getView();
                            break;
                        case "Teacher":
                            TeacherDashboardView teacherView = new TeacherDashboardView(username);
                            dashboard = teacherView.getView();
                            break;
                        case "Student":
                            StudentDashboardView studentView = new StudentDashboardView(username);
                            dashboard = studentView.getView();
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid role");
                    }

                    Scene scene = new Scene(dashboard, 1920, 1080);
                    scene.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());
                    
                    // Create fade in transition for new scene
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(500), dashboard);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    
                    stage.setScene(scene);
                    fadeIn.play();

                } catch (Exception ex) {
                    showError("Error opening dashboard: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });

            fadeOut.play();

        } catch (Exception e) {
            showError("Error opening dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupEventHandlers() {
        loginButton.setOnAction(e -> handleLogin());

        // Add enter key handlers for main login
        usernameField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> handleLogin());
    }
}
