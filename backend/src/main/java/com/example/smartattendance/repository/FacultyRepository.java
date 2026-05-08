package com.example.smartattendance.repository;

import com.example.smartattendance.entity.Faculty;
import com.example.smartattendance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    Optional<Faculty> findByUser(User user);
}
