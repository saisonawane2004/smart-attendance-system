-- MySQL sample data for Smart Attendance System
USE dbstudattendance;

-- Plain password for all demo users: '1234'
INSERT INTO users (id, name, email, password, role) VALUES
(1, 'System Admin', 'admin@smart.com', '1234', 'ADMIN'),
(2, 'Faculty One', 'faculty@smart.com', '1234', 'FACULTY'),
(3, 'Student One', 'student@smart.com', '1234', 'STUDENT'),
(4, 'Student Two', 'student2@smart.com', '1234', 'STUDENT');

INSERT INTO classes (id, name, section, year_level) VALUES
(1, 'BSc CS', 'A', 1);

INSERT INTO faculty (id, user_id, department) VALUES
(1, 2, 'CSE');

INSERT INTO students (id, user_id, roll_number, class_id) VALUES
(1, 3, 'CS001', 1),
(2, 4, 'CS002', 1);

INSERT INTO subjects (id, name, faculty_id, class_id) VALUES
(1, 'Software Engineering', 1, 1),
(2, 'Database Systems', 1, 1);

INSERT INTO attendance_sessions (id, subject_id, qr_token, expiry_time, latitude, longitude, radius) VALUES
(1, 1, 'demo-token-1', DATE_ADD(NOW(), INTERVAL 5 MINUTE), 18.5204, 73.8567, 100),
(2, 2, 'demo-token-2', DATE_ADD(NOW(), INTERVAL 5 MINUTE), 18.5204, 73.8567, 100);

INSERT INTO attendance (student_id, session_id, timestamp, status) VALUES
(1, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), 'PRESENT'),
(2, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), 'PRESENT'),
(1, 2, DATE_SUB(NOW(), INTERVAL 2 DAY), 'PRESENT');
