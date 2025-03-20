package com.college.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Scene;
import javafx.stage.Stage;

public abstract class BaseDashboardView {
    protected final String username;
    protected BorderPane root;
    protected VBox sidebar;
    protected StackPane contentArea;
    
    public BaseDashboardView(String username) {
        this.username = username;
        createBaseView();
        setupSidebar();
        setupContentArea();
    }
    
    public Parent getView() {
        return root;
    }
    
    private void createBaseView() {
        root = new BorderPane();
        root.getStyleClass().add("dashboard");
        
        // Create top bar
        HBox topBar = createTopBar();
        root.setTop(topBar);
        
        // Create sidebar
        sidebar = new VBox(10);
        sidebar.setPadding(new Insets(10));
        sidebar.setPrefWidth(200);
        sidebar.getStyleClass().add("sidebar");
        root.setLeft(sidebar);
        
        // Create content area
        contentArea = new StackPane();
        contentArea.setPadding(new Insets(20));
        contentArea.getStyleClass().add("content-area");
        root.setCenter(contentArea);
    }
    
    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(10, 20, 10, 20));
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.getStyleClass().add("top-bar");
        
        Label userLabel = new Label("Welcome, " + username);
        userLabel.getStyleClass().add("user-label");
        
        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("logout-button");
        logoutButton.setOnAction(e -> handleLogout());
        
        topBar.getChildren().addAll(userLabel, logoutButton);
        return topBar;
    }
    
    protected void handleLogout() {
        try {
            LoginView loginView = new LoginView();
            Scene scene = new Scene(loginView.getView(), 800, 600);
            scene.getStylesheets().add(getClass().getResource("/styles/login.css").toExternalForm());
            
            Stage stage = (Stage) root.getScene().getWindow();
            stage.setScene(scene);
            stage.setResizable(false);
            stage.centerOnScreen();
        } catch (Exception e) {
            showError("Error during logout: " + e.getMessage());
        }
    }
    
    protected abstract void setupSidebar();
    protected abstract void setupContentArea();
    
    protected void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    protected void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    protected void setContent(Parent content) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(content);
    }
}
