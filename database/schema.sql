-- MySQL schema for Smart Attendance System
CREATE DATABASE IF NOT EXISTS dbstudattendance;
USE dbstudattendance;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS attendance;
DROP TABLE IF EXISTS attendance_sessions;
DROP TABLE IF EXISTS subjects;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS faculty;
DROP TABLE IF EXISTS classes;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(190) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    INDEX idx_user_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE classes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    section VARCHAR(50) NOT NULL,
    year_level TINYINT NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE faculty (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    department VARCHAR(120) NOT NULL,
    CONSTRAINT fk_faculty_user FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    roll_number VARCHAR(60) NOT NULL UNIQUE,
    class_id BIGINT NOT NULL,
    CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_student_class FOREIGN KEY (class_id) REFERENCES classes(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_student_roll (roll_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE subjects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    faculty_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    CONSTRAINT fk_subject_faculty FOREIGN KEY (faculty_id) REFERENCES faculty(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_subject_class FOREIGN KEY (class_id) REFERENCES classes(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE attendance_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subject_id BIGINT NOT NULL,
    qr_token VARCHAR(255) NOT NULL UNIQUE,
    expiry_time DATETIME NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    radius DOUBLE NOT NULL,
    CONSTRAINT fk_session_subject FOREIGN KEY (subject_id) REFERENCES subjects(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_session_token (qr_token),
    INDEX idx_session_expiry (expiry_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    session_id BIGINT NOT NULL,
    timestamp DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) REFERENCES students(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_attendance_session FOREIGN KEY (session_id) REFERENCES attendance_sessions(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT uk_student_session UNIQUE (student_id, session_id),
    INDEX idx_attendance_time (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
