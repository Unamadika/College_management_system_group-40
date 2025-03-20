package com.college;

import com.college.ui.LoginView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            LoginView loginView = new LoginView();
            Scene scene = new Scene(loginView.getView(), 800, 600);
            scene.getStylesheets().add(getClass().getResource("/styles/login.css").toExternalForm());
            
            primaryStage.setScene(scene);
            primaryStage.setTitle("College Management System");
            primaryStage.setResizable(false);
            primaryStage.show();
            
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            Platform.exit();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
