-- Users table
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL
);

-- Courses table
CREATE TABLE IF NOT EXISTS courses (
    code VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

-- Student Courses table
CREATE TABLE IF NOT EXISTS student_courses (
    student_id INT,
    course_code VARCHAR(20),
    grade VARCHAR(5),
    PRIMARY KEY (student_id, course_code),
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (course_code) REFERENCES courses(code)
);

-- Messages table
CREATE TABLE IF NOT EXISTS messages (
    id INT PRIMARY KEY AUTO_INCREMENT,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    message_text TEXT NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id)
);

-- Financial Records table
CREATE TABLE IF NOT EXISTS financial_records (
    id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    transaction_type ENUM('Payment', 'Fee', 'Fine', 'Scholarship') NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (student_id) REFERENCES users(id)
);

-- Insert test data if not exists
INSERT IGNORE INTO users (username, password, role) VALUES
('student1', 'password123', 'Student'),
('teacher1', 'password123', 'Teacher');

INSERT IGNORE INTO courses (code, name) VALUES
('CS101', 'Introduction to Programming'),
('CS102', 'Data Structures');

-- Enroll student1 in courses
INSERT IGNORE INTO student_courses (student_id, course_code, grade)
SELECT u.id, c.code, 'A'
FROM users u, courses c
WHERE u.username = 'student1'
AND c.code IN ('CS101', 'CS102');

-- Insert test financial records for student1
INSERT IGNORE INTO financial_records (student_id, transaction_type, amount)
SELECT u.id, 'Fee', 5000.00
FROM users u
WHERE u.username = 'student1';

INSERT IGNORE INTO financial_records (student_id, transaction_type, amount)
SELECT u.id, 'Fee', 100.00
FROM users u
WHERE u.username = 'student1';
