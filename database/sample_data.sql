USE college_db;

-- Insert sample users
INSERT INTO users (username, password, role) VALUES
('admin', 'admin123', 'Admin'),
('teacher1', 'teacher123', 'Teacher'),
('teacher2', 'teacher123', 'Teacher'),
('student1', 'student123', 'Student'),
('student2', 'student123', 'Student'),
('student3', 'student123', 'Student');

-- Insert sample courses
INSERT INTO courses (code, name, description, created_by) VALUES
('CS101', 'Introduction to Programming', 'Basic programming concepts using Java', 'admin'),
('CS102', 'Data Structures', 'Advanced programming concepts and data structures', 'admin'),
('MATH101', 'Calculus I', 'Introduction to differential calculus', 'admin'),
('ENG101', 'English Composition', 'Academic writing and communication', 'admin');

-- Enroll students in courses with grades
INSERT INTO student_courses (student_id, course_code, grade) VALUES
(4, 'CS101', 'A'),
(4, 'MATH101', 'B+'),
(5, 'CS101', 'B'),
(5, 'CS102', 'A-'),
(6, 'ENG101', 'A'),
(6, 'MATH101', 'B');

-- Insert attendance records
INSERT INTO attendance (student_id, course_code, date, status) VALUES
(4, 'CS101', '2025-03-15', 'Present'),
(4, 'MATH101', '2025-03-15', 'Present'),
(5, 'CS101', '2025-03-15', 'Late'),
(5, 'CS102', '2025-03-15', 'Present'),
(6, 'ENG101', '2025-03-15', 'Absent'),
(6, 'MATH101', '2025-03-15', 'Present'),
(4, 'CS101', '2025-03-16', 'Present'),
(5, 'CS102', '2025-03-16', 'Present'),
(6, 'ENG101', '2025-03-16', 'Present');

-- Insert sample messages
INSERT INTO messages (sender_id, receiver_id, message_text, sent_at) VALUES
(4, 2, 'Hello teacher, I have a question about the Java assignment', '2025-03-15 10:30:00'),
(2, 4, 'Sure, what would you like to know?', '2025-03-15 10:35:00'),
(5, 2, 'When is the next quiz?', '2025-03-16 09:15:00'),
(2, 5, 'The quiz will be next Monday', '2025-03-16 09:20:00'),
(6, 3, 'I will be late for tomorrow''s class', '2025-03-16 18:45:00'),
(3, 6, 'Thanks for letting me know', '2025-03-16 19:00:00');

-- Insert financial records
INSERT INTO financial_records (student_id, transaction_date, transaction_type, amount) VALUES
(4, '2025-03-01 10:00:00', 'Fee', 5000.00),
(4, '2025-03-05 11:30:00', 'Payment', -2500.00),
(5, '2025-03-01 09:30:00', 'Fee', 5000.00),
(5, '2025-03-03 14:15:00', 'Scholarship', -1000.00),
(5, '2025-03-05 16:45:00', 'Payment', -4000.00),
(6, '2025-03-01 11:15:00', 'Fee', 5000.00),
(6, '2025-03-02 13:20:00', 'Fine', 100.00),
(6, '2025-03-10 15:30:00', 'Payment', -5100.00);
