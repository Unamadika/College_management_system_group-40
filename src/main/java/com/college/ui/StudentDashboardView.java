package com.college.ui;

import com.college.utils.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.*;
import java.time.LocalDate;
import java.util.Arrays;

public class StudentDashboardView extends BaseDashboardView {
    private TableView<Course> coursesTable;
    private TableView<Attendance> attendanceTable;
    private TableView<Transaction> financialTable;
    private ChatView chatView;
    
    public StudentDashboardView(String username) {
        super(username);
    }
    
    @Override
    protected void setupSidebar() {
        Button coursesBtn = new Button("My Courses");
        coursesBtn.setMaxWidth(Double.MAX_VALUE);
        coursesBtn.getStyleClass().add("sidebar-button");
        coursesBtn.setOnAction(e -> showCourses());
        
        Button attendanceBtn = new Button("My Attendance");
        attendanceBtn.setMaxWidth(Double.MAX_VALUE);
        attendanceBtn.getStyleClass().add("sidebar-button");
        attendanceBtn.setOnAction(e -> showAttendance());
        
        Button financialBtn = new Button("Financial Records");
        financialBtn.setMaxWidth(Double.MAX_VALUE);
        financialBtn.getStyleClass().add("sidebar-button");
        financialBtn.setOnAction(e -> showFinancialRecords());
        
        Button chatBtn = new Button("Teacher Chat");
        chatBtn.setMaxWidth(Double.MAX_VALUE);
        chatBtn.getStyleClass().add("sidebar-button");
        chatBtn.setOnAction(e -> showChat());
        
        sidebar.getChildren().addAll(coursesBtn, attendanceBtn, financialBtn, chatBtn);
    }

    private void showChat() {
        if (chatView == null) {
            chatView = new ChatView(username, "Student");
        }
        setContent(chatView.getView());
    }
    
    @Override
    protected void setupContentArea() {
        showCourses(); // Default view
    }
    
    private void showCourses() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        
        // Courses table
        coursesTable = new TableView<>();
        coursesTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        
        TableColumn<Course, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        
        TableColumn<Course, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        TableColumn<Course, String> gradeCol = new TableColumn<>("Grade");
        gradeCol.setCellValueFactory(new PropertyValueFactory<>("grade"));
        
        coursesTable.getColumns().addAll(Arrays.asList(codeCol, nameCol, gradeCol));
        
        content.getChildren().addAll(new Label("My Courses"), coursesTable);
        setContent(content);
        loadCourses();
    }
    
    private void showAttendance() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        
        // Course filter
        ComboBox<String> courseComboBox = new ComboBox<>();
        loadCoursesList(courseComboBox);
        
        // Attendance table
        attendanceTable = new TableView<>();
        attendanceTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        
        TableColumn<Attendance, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(new PropertyValueFactory<>("course"));
        
        TableColumn<Attendance, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        
        TableColumn<Attendance, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        attendanceTable.getColumns().addAll(Arrays.asList(courseCol, dateCol, statusCol));
        
        courseComboBox.setOnAction(e -> loadAttendance(courseComboBox.getValue()));
        
        content.getChildren().addAll(
            new Label("Select Course:"), courseComboBox,
            new Label("Attendance Records"), attendanceTable
        );
        setContent(content);
        
        if (!courseComboBox.getItems().isEmpty()) {
            courseComboBox.setValue(courseComboBox.getItems().get(0));
        }
    }
    
    private void showFinancialRecords() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        
        // Financial records table
        financialTable = new TableView<>();
        financialTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        
        TableColumn<Transaction, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        
        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        
        TableColumn<Transaction, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        
        financialTable.getColumns().addAll(Arrays.asList(dateCol, typeCol, amountCol));
        
        content.getChildren().addAll(new Label("Financial Records"), financialTable);
        setContent(content);
        loadFinancialRecords();
    }
    
    private void loadCourses() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT c.code, c.name, sc.grade FROM courses c " +
                          "JOIN student_courses sc ON c.code = sc.course_code " +
                          "JOIN users u ON sc.student_id = u.id " +
                          "WHERE u.username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            ObservableList<Course> courses = FXCollections.observableArrayList();
            while (rs.next()) {
                courses.add(new Course(
                    rs.getString("code"),
                    rs.getString("name"),
                    rs.getString("grade")
                ));
            }
            
            coursesTable.setItems(courses);
            
        } catch (SQLException e) {
            showError("Error loading courses: " + e.getMessage());
        }
    }
    
    private void loadCoursesList(ComboBox<String> courseComboBox) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT c.code FROM courses c " +
                          "JOIN student_courses sc ON c.code = sc.course_code " +
                          "JOIN users u ON sc.student_id = u.id " +
                          "WHERE u.username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            ObservableList<String> courses = FXCollections.observableArrayList();
            while (rs.next()) {
                courses.add(rs.getString("code"));
            }
            
            courseComboBox.setItems(courses);
            
        } catch (SQLException e) {
            showError("Error loading courses: " + e.getMessage());
        }
    }
    
    private void loadAttendance(String courseCode) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT c.code, a.date, a.status FROM attendance a " +
                          "JOIN courses c ON a.course_code = c.code " +
                          "JOIN users u ON a.student_id = u.id " +
                          "WHERE u.username = ? AND c.code = ? " +
                          "ORDER BY a.date DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, courseCode);
            ResultSet rs = stmt.executeQuery();
            
            ObservableList<Attendance> attendance = FXCollections.observableArrayList();
            while (rs.next()) {
                attendance.add(new Attendance(
                    rs.getString("code"),
                    rs.getDate("date").toLocalDate(),
                    rs.getString("status")
                ));
            }
            
            attendanceTable.setItems(attendance);
            
        } catch (SQLException e) {
            showError("Error loading attendance: " + e.getMessage());
        }
    }
    
    private void loadFinancialRecords() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT transaction_date, transaction_type, amount FROM financial_records fr " +
                          "JOIN users u ON fr.student_id = u.id " +
                          "WHERE u.username = ? " +
                          "ORDER BY transaction_date DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            ObservableList<Transaction> transactions = FXCollections.observableArrayList();
            while (rs.next()) {
                transactions.add(new Transaction(
                    rs.getTimestamp("transaction_date").toLocalDateTime().toLocalDate(),
                    rs.getString("transaction_type"),
                    rs.getDouble("amount")
                ));
            }
            
            financialTable.setItems(transactions);
            
        } catch (SQLException e) {
            showError("Error loading financial records: " + e.getMessage());
        }
    }
    
    // Data classes for tables
    public static class Course {
        private final String code;
        private final String name;
        private final String grade;
        
        public Course(String code, String name, String grade) {
            this.code = code;
            this.name = name;
            this.grade = grade;
        }
        
        public String getCode() { return code; }
        public String getName() { return name; }
        public String getGrade() { return grade; }
    }
    
    public static class Attendance {
        private final String course;
        private final LocalDate date;
        private final String status;
        
        public Attendance(String course, LocalDate date, String status) {
            this.course = course;
            this.date = date;
            this.status = status;
        }
        
        public String getCourse() { return course; }
        public LocalDate getDate() { return date; }
        public String getStatus() { return status; }
    }
    
    public static class Transaction {
        private final LocalDate date;
        private final String type;
        private final double amount;
        
        public Transaction(LocalDate date, String type, double amount) {
            this.date = date;
            this.type = type;
            this.amount = amount;
        }
        
        public LocalDate getDate() { return date; }
        public String getType() { return type; }
        public double getAmount() { return amount; }
    }
}
