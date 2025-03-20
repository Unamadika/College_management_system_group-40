package com.college.ui;

import com.college.utils.DatabaseConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

public class ChatView {
    private final VBox root;
    private final String currentUsername;
    private final String currentRole;
    private ListView<String> messagesList;
    private ComboBox<String> userComboBox;
    private TextField messageField;
    private Timer refreshTimer;
    private String selectedUser;

    public ChatView(String username, String role) {
        this.currentUsername = username;
        this.currentRole = role;
        this.root = new VBox(10);
        createView();
        setupRefreshTimer();
    }

    public VBox getView() {
        return root;
    }

    private void createView() {
        root.setPadding(new Insets(10));
        root.setSpacing(10);

        // User selection
        HBox userSelectionBox = new HBox(10);
        userSelectionBox.setAlignment(Pos.CENTER_LEFT);
        Label selectLabel = new Label("Chat with:");
        userComboBox = new ComboBox<>();
        loadUsers();
        userSelectionBox.getChildren().addAll(selectLabel, userComboBox);

        // Messages area
        messagesList = new ListView<>();
        messagesList.setPrefHeight(400);
        messagesList.setStyle("-fx-font-size: 14px;");

        // Message input area
        HBox inputBox = new HBox(10);
        inputBox.setAlignment(Pos.CENTER);
        messageField = new TextField();
        messageField.setPrefWidth(500);
        messageField.setPromptText("Type your message...");
        Button sendButton = new Button("Send");
        sendButton.getStyleClass().add("action-button");
        inputBox.getChildren().addAll(messageField, sendButton);

        // Event handlers
        userComboBox.setOnAction(e -> {
            selectedUser = userComboBox.getValue();
            if (selectedUser != null) {
                loadMessages();
            }
        });

        sendButton.setOnAction(e -> sendMessage());
        messageField.setOnAction(e -> sendMessage());

        root.getChildren().addAll(userSelectionBox, messagesList, inputBox);
    }

    private void loadUsers() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query;
            if (currentRole.equals("Teacher")) {
                // Teachers can chat with all students
                query = "SELECT username FROM users WHERE role = 'Student'";
            } else {
                // Students can chat with all teachers
                query = "SELECT username FROM users WHERE role = 'Teacher'";
            }
            
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            ObservableList<String> users = FXCollections.observableArrayList();
            while (rs.next()) {
                users.add(rs.getString("username"));
            }
            
            userComboBox.setItems(users);
            
        } catch (SQLException e) {
            showError("Error loading users: " + e.getMessage());
        }
    }

    private void loadMessages() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT m.message_text, m.sent_at, u1.username as sender " +
                          "FROM messages m " +
                          "JOIN users u1 ON m.sender_id = u1.id " +
                          "JOIN users u2 ON m.receiver_id = u2.id " +
                          "WHERE (u1.username = ? AND u2.username = ?) " +
                          "OR (u1.username = ? AND u2.username = ?) " +
                          "ORDER BY m.sent_at";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, currentUsername);
            stmt.setString(2, selectedUser);
            stmt.setString(3, selectedUser);
            stmt.setString(4, currentUsername);
            
            ResultSet rs = stmt.executeQuery();
            
            ObservableList<String> messages = FXCollections.observableArrayList();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            while (rs.next()) {
                String sender = rs.getString("sender");
                String message = rs.getString("message_text");
                Timestamp timestamp = rs.getTimestamp("sent_at");
                String time = timestamp.toLocalDateTime().format(formatter);
                
                String formattedMessage = String.format("[%s] %s: %s",
                    time,
                    sender.equals(currentUsername) ? "You" : sender,
                    message
                );
                messages.add(formattedMessage);
            }
            
            messagesList.setItems(messages);
            messagesList.scrollTo(messages.size() - 1);
            
        } catch (SQLException e) {
            showError("Error loading messages: " + e.getMessage());
        }
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (message.isEmpty() || selectedUser == null) {
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO messages (sender_id, receiver_id, message_text) " +
                          "SELECT u1.id, u2.id, ? " +
                          "FROM users u1, users u2 " +
                          "WHERE u1.username = ? AND u2.username = ?";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, message);
            stmt.setString(2, currentUsername);
            stmt.setString(3, selectedUser);
            stmt.executeUpdate();
            
            messageField.clear();
            loadMessages();
            
        } catch (SQLException e) {
            showError("Error sending message: " + e.getMessage());
        }
    }

    private void setupRefreshTimer() {
        refreshTimer = new Timer(true);
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (selectedUser != null) {
                    Platform.runLater(() -> loadMessages());
                }
            }
        }, 5000, 5000); // Refresh every 5 seconds
    }

    public void cleanup() {
        if (refreshTimer != null) {
            refreshTimer.cancel();
        }
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
