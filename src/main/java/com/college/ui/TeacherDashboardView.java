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

public class TeacherDashboardView extends BaseDashboardView {
    private TableView<StudentGrade> gradeTable;
    private TableView<StudentAttendance> attendanceTable;
    private ComboBox<String> courseComboBox;
    private ChatView chatView;
    
    public TeacherDashboardView(String username) {
        super(username);
    }
    
    @Override
    protected void setupSidebar() {
        Button gradesBtn = new Button("Manage Grades");
        gradesBtn.setMaxWidth(Double.MAX_VALUE);
        gradesBtn.getStyleClass().add("sidebar-button");
        gradesBtn.setOnAction(e -> showGradesManagement());
        
        Button attendanceBtn = new Button("Manage Attendance");
        attendanceBtn.setMaxWidth(Double.MAX_VALUE);
        attendanceBtn.getStyleClass().add("sidebar-button");
        attendanceBtn.setOnAction(e -> showAttendanceManagement());
        
        Button chatBtn = new Button("Student Chat");
        chatBtn.setMaxWidth(Double.MAX_VALUE);
        chatBtn.getStyleClass().add("sidebar-button");
        chatBtn.setOnAction(e -> showChat());
        
        sidebar.getChildren().addAll(gradesBtn, attendanceBtn, chatBtn);
    }

    private void showChat() {
        if (chatView == null) {
            chatView = new ChatView(username, "Teacher");
        }
        setContent(chatView.getView());
    }
    
    @Override
    protected void setupContentArea() {
        showGradesManagement(); // Default view
    }
    
    private void showGradesManagement() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        
        // Course selection
        courseComboBox = new ComboBox<>();
        loadCourses();
        
        // Grade form
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(20));
        
        ComboBox<String> studentComboBox = new ComboBox<>();
        ComboBox<String> gradeComboBox = new ComboBox<>();
        gradeComboBox.getItems().addAll("A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "F");
        
        form.addRow(0, new Label("Student:"), studentComboBox);
        form.addRow(1, new Label("Grade:"), gradeComboBox);
        
        Button assignGradeBtn = new Button("Assign Grade");
        assignGradeBtn.getStyleClass().add("action-button");
        form.addRow(2, new Label(""), assignGradeBtn);
        
        // Grades table
        gradeTable = new TableView<>();
        gradeTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        
        TableColumn<StudentGrade, String> studentCol = new TableColumn<>("Student");
        studentCol.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        
        TableColumn<StudentGrade, String> gradeCol = new TableColumn<>("Grade");
        gradeCol.setCellValueFactory(new PropertyValueFactory<>("grade"));
        
        gradeTable.getColumns().addAll(Arrays.asList(studentCol, gradeCol));
        
        // Course selection action
        courseComboBox.setOnAction(e -> {
            loadStudents(studentComboBox);
            loadGrades();
        });
        
        // Assign grade action
        assignGradeBtn.setOnAction(e -> {
            String student = studentComboBox.getValue();
            String grade = gradeComboBox.getValue();
            String course = courseComboBox.getValue();
            
            if (student == null || grade == null || course == null) {
                showError("Please select all fields");
                return;
            }
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "UPDATE student_courses SET grade = ? WHERE student_id = (SELECT id FROM users WHERE username = ?) AND course_code = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, grade);
                stmt.setString(2, student);
                stmt.setString(3, course);
                stmt.executeUpdate();
                
                showSuccess("Grade assigned successfully");
                loadGrades();
                
            } catch (SQLException ex) {
                showError("Error assigning grade: " + ex.getMessage());
            }
        });
        
        content.getChildren().addAll(
            new Label("Select Course:"), courseComboBox,
            new Label("Assign Grade"), form,
            new Label("Grades List"), gradeTable
        );
        setContent(content);
    }
    
    private void showAttendanceManagement() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        
        // Course selection
        courseComboBox = new ComboBox<>();
        loadCourses();
        
        // Attendance form
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(20));
        
        ComboBox<String> studentComboBox = new ComboBox<>();
        DatePicker datePicker = new DatePicker(LocalDate.now());
        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("Present", "Absent", "Late");
        
        form.addRow(0, new Label("Student:"), studentComboBox);
        form.addRow(1, new Label("Date:"), datePicker);
        form.addRow(2, new Label("Status:"), statusComboBox);
        
        Button markAttendanceBtn = new Button("Mark Attendance");
        markAttendanceBtn.getStyleClass().add("action-button");
        form.addRow(3, new Label(""), markAttendanceBtn);
        
        // Attendance table
        attendanceTable = new TableView<>();
        attendanceTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        
        TableColumn<StudentAttendance, String> studentCol = new TableColumn<>("Student");
        studentCol.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        
        TableColumn<StudentAttendance, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        
        TableColumn<StudentAttendance, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        attendanceTable.getColumns().addAll(Arrays.asList(studentCol, dateCol, statusCol));
        
        // Course selection action
        courseComboBox.setOnAction(e -> {
            loadStudents(studentComboBox);
            loadAttendance();
        });
        
        // Mark attendance action
        markAttendanceBtn.setOnAction(e -> {
            String student = studentComboBox.getValue();
            LocalDate date = datePicker.getValue();
            String status = statusComboBox.getValue();
            String course = courseComboBox.getValue();
            
            if (student == null || date == null || status == null || course == null) {
                showError("Please fill in all fields");
                return;
            }
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "INSERT INTO attendance (student_id, course_code, date, status) VALUES ((SELECT id FROM users WHERE username = ?), ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, student);
                stmt.setString(2, course);
                stmt.setDate(3, java.sql.Date.valueOf(date));
                stmt.setString(4, status);
                stmt.executeUpdate();
                
                showSuccess("Attendance marked successfully");
                loadAttendance();
                
            } catch (SQLException ex) {
                showError("Error marking attendance: " + ex.getMessage());
            }
        });
        
        content.getChildren().addAll(
            new Label("Select Course:"), courseComboBox,
            new Label("Mark Attendance"), form,
            new Label("Attendance List"), attendanceTable
        );
        setContent(content);
    }
    
    private void loadCourses() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT code, name FROM courses";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            ObservableList<String> courses = FXCollections.observableArrayList();
            while (rs.next()) {
                courses.add(rs.getString("code"));
            }
            
            courseComboBox.setItems(courses);
            if (!courses.isEmpty()) {
                courseComboBox.setValue(courses.get(0));
            }
            
        } catch (SQLException e) {
            showError("Error loading courses: " + e.getMessage());
        }
    }
    
    private void loadStudents(ComboBox<String> studentComboBox) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT u.username FROM users u JOIN student_courses sc ON u.id = sc.student_id WHERE sc.course_code = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, courseComboBox.getValue());
            ResultSet rs = stmt.executeQuery();
            
            ObservableList<String> students = FXCollections.observableArrayList();
            while (rs.next()) {
                students.add(rs.getString("username"));
            }
            
            studentComboBox.setItems(students);
            
        } catch (SQLException e) {
            showError("Error loading students: " + e.getMessage());
        }
    }
    
    private void loadGrades() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT u.username, sc.grade FROM users u JOIN student_courses sc ON u.id = sc.student_id WHERE sc.course_code = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, courseComboBox.getValue());
            ResultSet rs = stmt.executeQuery();
            
            ObservableList<StudentGrade> grades = FXCollections.observableArrayList();
            while (rs.next()) {
                grades.add(new StudentGrade(
                    rs.getString("username"),
                    rs.getString("grade")
                ));
            }
            
            gradeTable.setItems(grades);
            
        } catch (SQLException e) {
            showError("Error loading grades: " + e.getMessage());
        }
    }
    
    private void loadAttendance() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT u.username, a.date, a.status FROM users u JOIN attendance a ON u.id = a.student_id WHERE a.course_code = ? ORDER BY a.date DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, courseComboBox.getValue());
            ResultSet rs = stmt.executeQuery();
            
            ObservableList<StudentAttendance> attendance = FXCollections.observableArrayList();
            while (rs.next()) {
                attendance.add(new StudentAttendance(
                    rs.getString("username"),
                    rs.getDate("date").toLocalDate(),
                    rs.getString("status")
                ));
            }
            
            attendanceTable.setItems(attendance);
            
        } catch (SQLException e) {
            showError("Error loading attendance: " + e.getMessage());
        }
    }
    
    // Data classes for tables
    public static class StudentGrade {
        private final String studentName;
        private final String grade;
        
        public StudentGrade(String studentName, String grade) {
            this.studentName = studentName;
            this.grade = grade;
        }
        
        public String getStudentName() { return studentName; }
        public String getGrade() { return grade; }
    }
    
    public static class StudentAttendance {
        private final String studentName;
        private final LocalDate date;
        private final String status;
        
        public StudentAttendance(String studentName, LocalDate date, String status) {
            this.studentName = studentName;
            this.date = date;
            this.status = status;
        }
        
        public String getStudentName() { return studentName; }
        public LocalDate getDate() { return date; }
        public String getStatus() { return status; }
    }
}
