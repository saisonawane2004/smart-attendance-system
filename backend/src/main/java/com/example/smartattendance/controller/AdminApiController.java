package com.example.smartattendance.controller;

import com.example.smartattendance.entity.*;
import com.example.smartattendance.repository.*;
import com.example.smartattendance.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/api")
public class AdminApiController {
    private final AdminService adminService;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final ClassroomRepository classroomRepository;
    private final SubjectRepository subjectRepository;

    public AdminApiController(AdminService adminService, UserRepository userRepository, StudentRepository studentRepository, FacultyRepository facultyRepository,
                              ClassroomRepository classroomRepository, SubjectRepository subjectRepository) {
        this.adminService = adminService;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.classroomRepository = classroomRepository;
        this.subjectRepository = subjectRepository;
    }

    @GetMapping("/students")
    public List<Student> students() { return studentRepository.findAll(); }

    @DeleteMapping("/students/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        adminService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/faculty")
    public List<Faculty> faculty() { return facultyRepository.findAll(); }

    @DeleteMapping("/faculty/{id}")
    public ResponseEntity<Void> deleteFaculty(@PathVariable Long id) {
        adminService.deleteFaculty(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/subjects")
    public List<Subject> subjects() { return subjectRepository.findAll(); }

    @DeleteMapping("/subjects/{id}")
    public ResponseEntity<Void> deleteSubject(@PathVariable Long id) { subjectRepository.deleteById(id); return ResponseEntity.noContent().build(); }

    @GetMapping("/classes")
    public List<Classroom> classesList() { return classroomRepository.findAll(); }

    @DeleteMapping("/classes/{id}")
    public ResponseEntity<Void> deleteClass(@PathVariable Long id) { classroomRepository.deleteById(id); return ResponseEntity.noContent().build(); }
}
