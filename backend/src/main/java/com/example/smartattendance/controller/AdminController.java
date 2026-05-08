package com.example.smartattendance.controller;

import com.example.smartattendance.repository.*;
import com.example.smartattendance.service.AdminService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;
    private final FacultyRepository facultyRepository;
    private final ClassroomRepository classroomRepository;

    public AdminController(AdminService adminService, FacultyRepository facultyRepository, ClassroomRepository classroomRepository) {
        this.adminService = adminService;
        this.facultyRepository = facultyRepository;
        this.classroomRepository = classroomRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, @RequestParam(defaultValue = "0") int page) {
        model.addAttribute("students", adminService.students(page, 10));
        model.addAttribute("faculty", adminService.faculty(page, 10));
        model.addAttribute("subjects", adminService.subjects(page, 10));
        model.addAttribute("classes", adminService.classrooms(page, 10));
        model.addAttribute("allFaculty", facultyRepository.findAll());
        model.addAttribute("allClasses", classroomRepository.findAll());
        return "admin/dashboard";
    }

    @PostMapping("/classes")
    public String addClass(@RequestParam String name, @RequestParam String section, @RequestParam Integer yearLevel) {
        adminService.addClassroom(name, section, yearLevel);
        return "redirect:/admin/dashboard?success=Class+created";
    }

    @PostMapping("/students")
    public String addStudent(@RequestParam String name, @RequestParam String email, @RequestParam String password,
                             @RequestParam String rollNumber, @RequestParam Long classId) {
        try {
            adminService.addStudent(name, email, password, rollNumber, classId);
            return "redirect:/admin/dashboard?success=Student+created";
        } catch (IllegalArgumentException ex) {
            return "redirect:/admin/dashboard?error=" + ex.getMessage().replace(" ", "+");
        }
    }

    @PostMapping("/faculty")
    public String addFaculty(@RequestParam String name, @RequestParam String email, @RequestParam String password,
                             @RequestParam String department) {
        adminService.addFaculty(name, email, password, department);
        return "redirect:/admin/dashboard?success=Faculty+created";
    }

    @PostMapping("/subjects")
    public String addSubject(@RequestParam String name, @RequestParam Long facultyId, @RequestParam Long classId) {
        adminService.addSubject(name, facultyId, classId);
        return "redirect:/admin/dashboard?success=Subject+created";
    }
}
