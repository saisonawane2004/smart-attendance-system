package com.example.smartattendance.controller;

import com.example.smartattendance.entity.Role;
import com.example.smartattendance.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String redirectDashboard() {
        Role role = authService.currentUser().getRole();
        if (role == Role.ADMIN) return "redirect:/admin/dashboard";
        if (role == Role.FACULTY) return "redirect:/faculty/dashboard";
        return "redirect:/student/dashboard";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }
}
