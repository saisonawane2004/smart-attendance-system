package com.example.smartattendance.controller;

import com.example.smartattendance.dto.MarkAttendanceRequest;
import com.example.smartattendance.service.AttendanceService;
import com.example.smartattendance.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/student")
public class StudentController {
    private final AttendanceService attendanceService;
    private final AuthService authService;

    public StudentController(AttendanceService attendanceService, AuthService authService) {
        this.attendanceService = attendanceService;
        this.authService = authService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, @RequestParam(required = false) String token) {
        model.addAttribute("history", attendanceService.attendanceForStudent(authService.currentUser()));
        model.addAttribute("token", token == null ? "" : token);
        return "student/dashboard";
    }

    @PostMapping("/attendance/mark")
    public String mark(@Valid MarkAttendanceRequest request, RedirectAttributes redirectAttributes) {
        try {
            attendanceService.markAttendance(authService.currentUser(), request);
            return "redirect:/student/dashboard?success=Attendance+marked";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/student/dashboard";
        }
    }

    @GetMapping("/reports")
    public String reports(Model model,
                          @RequestParam(defaultValue = "daily") String periodType,
                          @RequestParam(required = false) String date,
                          @RequestParam(required = false) Integer month,
                          @RequestParam(required = false) Integer year) {
        LocalDate d = date == null ? LocalDate.now() : LocalDate.parse(date);
        model.addAttribute("rows", attendanceService.studentReport(authService.currentUser(), periodType, d, month, year));
        model.addAttribute("summary", attendanceService.studentSummary(authService.currentUser(), periodType, d, month, year));
        model.addAttribute("periodType", periodType);
        model.addAttribute("selectedDate", d);
        model.addAttribute("selectedMonth", month == null ? LocalDate.now().getMonthValue() : month);
        model.addAttribute("selectedYear", year == null ? LocalDate.now().getYear() : year);
        return "student/reports";
    }

    @GetMapping("/reports/csv")
    public ResponseEntity<String> reportsCsv(@RequestParam(defaultValue = "daily") String periodType,
                                             @RequestParam(required = false) String date,
                                             @RequestParam(required = false) Integer month,
                                             @RequestParam(required = false) Integer year) {
        LocalDate d = date == null ? LocalDate.now() : LocalDate.parse(date);
        String csv = attendanceService.exportCsv(attendanceService.studentReport(authService.currentUser(), periodType, d, month, year));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance-" + periodType + ".csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(csv);
    }
}
