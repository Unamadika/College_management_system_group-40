package com.college;

import com.college.ui.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            LoginView loginView = new LoginView();
            Scene scene = new Scene(loginView.getView());
            scene.getStylesheets().add(getClass().getResource("/styles/login.css").toExternalForm());
            
            // Get screen dimensions
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            
            // Set stage properties
            primaryStage.setTitle("College Management System");
            primaryStage.setScene(scene);
            
            // Set window to exact 1920x1080 size
            primaryStage.setWidth(1920);
            primaryStage.setHeight(1080);
            
            // Center the window
            primaryStage.setX((bounds.getWidth() - 1920) / 2);
            primaryStage.setY((bounds.getHeight() - 1080) / 2);
            
            // Disable resizing for consistent display
            primaryStage.setResizable(false);
            
            primaryStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            javafx.application.Platform.exit();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
