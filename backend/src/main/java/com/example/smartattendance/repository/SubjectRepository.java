package com.example.smartattendance.repository;

import com.example.smartattendance.entity.Classroom;
import com.example.smartattendance.entity.Faculty;
import com.example.smartattendance.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findByFaculty(Faculty faculty);
    List<Subject> findByClassroom(Classroom classroom);
    boolean existsByClassroomId(Long classroomId);
}
