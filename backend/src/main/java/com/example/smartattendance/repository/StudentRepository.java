package com.example.smartattendance.repository;

import com.example.smartattendance.entity.Classroom;
import com.example.smartattendance.entity.Student;
import com.example.smartattendance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUser(User user);
    Optional<Student> findByUserEmail(String email);
    Optional<Student> findByRollNumber(String rollNumber);
    List<Student> findByClassroom(Classroom classroom);
    List<Student> findByClassroomId(Long classroomId);
}
