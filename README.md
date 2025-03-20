# College Management System

A JavaFX-based College Management System with role-based access for Students, Teachers, and Administrators.

## Features

### Admin Features
- User Management (Add/Edit/Delete Students and Teachers)
- Course Management (Add/Edit/Delete Courses)

### Teacher Features
- Grade Management
- Attendance Tracking
- Student Communication via Chat

### Student Features
- View Courses and Grades
- View Attendance
- Chat with Teachers
- View Financial History

## Setup Instructions

1. Make sure you have Java 17 or later installed
2. Install MySQL and create a database named `college_management`
3. Run the SQL script in `src/main/resources/database/schema.sql` to set up the database
4. Build the project using Maven:
   ```bash
   mvn clean install
   ```
5. Run the application:
   ```bash
   mvn javafx:run
   ```

## Default Admin Credentials
- Username: admin
- Password: admin123

## Database Configuration
Update the database connection details in `DatabaseConnection.java` if needed:
- Default Host: localhost
- Default Port: 3306
- Default Database: college_management
- Default Username: root
- Default Password: root
