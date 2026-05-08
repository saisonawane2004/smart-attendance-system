package com.example.smartattendance.controller;

import com.example.smartattendance.dto.SessionCreateRequest;
import com.example.smartattendance.dto.ManualAttendanceRequest;
import com.example.smartattendance.entity.AttendanceSession;
import com.example.smartattendance.service.AuthService;
import com.example.smartattendance.service.FacultyService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.YearMonth;

@Controller
@RequestMapping("/faculty")
public class FacultyController {
    private final FacultyService facultyService;
    private final AuthService authService;

    public FacultyController(FacultyService facultyService, AuthService authService) {
        this.facultyService = facultyService;
        this.authService = authService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        populateDashboardModel(model);
        return "faculty/dashboard";
    }

    @PostMapping("/attendance/session")
    public String createSession(@Valid SessionCreateRequest request, Model model) {
        AttendanceSession session = facultyService.createSession(authService.currentUser(), request);
        model.addAttribute("session", session);
        model.addAttribute("qrBase64", facultyService.qrBase64(session));
        model.addAttribute("markUrl", facultyService.markAttendanceUrl(session));
        model.addAttribute("expiryTimeMs",
                session.getExpiryTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        return "faculty/qr-display";
    }

    @GetMapping("/classes/{classId}/students")
    public String studentsForClass(@PathVariable Long classId, Model model) {
        model.addAttribute("students", facultyService.studentsForClass(classId));
        model.addAttribute("classId", classId);
        populateDashboardModel(model);
        return "faculty/dashboard";
    }

    @PostMapping("/attendance/manual")
    public String markManualAttendance(@Valid ManualAttendanceRequest request, RedirectAttributes ra) {
        try {
            facultyService.markManualAttendance(authService.currentUser(), request);
            ra.addFlashAttribute("successMessage", "Student added successfully.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/faculty/dashboard";
    }

    @GetMapping("/attendance/manual")
    public String manualAttendanceRedirect() {
        return "redirect:/faculty/dashboard";
    }

    @PostMapping("/classes/{classId}/students/upload")
    public String uploadStudents(@PathVariable Long classId, @RequestParam("file") MultipartFile file, RedirectAttributes ra) {
        var result = facultyService.importStudentsFromExcel(authService.currentUser(), classId, file);
        ra.addFlashAttribute("successMessage", "Import complete: created=" + result.created() + ", updated=" + result.updated() + ", skipped=" + result.skipped());
        return "redirect:/faculty/classes/" + classId + "/students";
    }

    @PostMapping("/classes/{classId}/students/{studentId}/remove")
    public String removeStudent(@PathVariable Long classId, @PathVariable Long studentId, RedirectAttributes ra) {
        facultyService.removeStudentFromClass(authService.currentUser(), classId, studentId);
        ra.addFlashAttribute("successMessage", "Student removed.");
        return "redirect:/faculty/classes/" + classId + "/students";
    }

    @GetMapping("/attendance/report")
    public String report(@RequestParam Long subjectId,
                         @RequestParam(defaultValue = "daily") String periodType,
                         @RequestParam(required = false) String date,
                         @RequestParam(required = false) Integer month,
                         @RequestParam(required = false) Integer year,
                         @RequestParam(required = false) Long studentId,
                         Model model) {
        LocalDateTime start;
        LocalDateTime end;

        if ("monthly".equalsIgnoreCase(periodType)) {
            int y = year == null ? LocalDate.now().getYear() : year;
            int m = month == null ? LocalDate.now().getMonthValue() : month;
            YearMonth ym = YearMonth.of(y, m);
            start = ym.atDay(1).atStartOfDay();
            end = ym.atEndOfMonth().atTime(23, 59, 59);
        } else if ("yearly".equalsIgnoreCase(periodType)) {
            int y = year == null ? LocalDate.now().getYear() : year;
            start = LocalDate.of(y, 1, 1).atStartOfDay();
            end = LocalDate.of(y, 12, 31).atTime(23, 59, 59);
        } else {
            LocalDate selected = date == null ? LocalDate.now() : LocalDate.parse(date);
            start = selected.atStartOfDay();
            end = selected.atTime(23, 59, 59);
        }

        model.addAttribute("attendances", facultyService.attendanceBySubject(subjectId, start, end, studentId));
        model.addAttribute("summary", facultyService.subjectSummary(subjectId, start, end, studentId));
        model.addAttribute("subjects", facultyService.mySubjects(authService.currentUser()));
        model.addAttribute("students", facultyService.studentsForSubject(subjectId));
        model.addAttribute("selectedSubjectId", subjectId);
        model.addAttribute("selectedStudentId", studentId);
        model.addAttribute("periodType", periodType);
        model.addAttribute("selectedDate", date == null ? LocalDate.now().toString() : date);
        model.addAttribute("selectedMonth", month == null ? LocalDate.now().getMonthValue() : month);
        model.addAttribute("selectedYear", year == null ? LocalDate.now().getYear() : year);
        return "faculty/report";
    }

    private void populateDashboardModel(Model model) {
        model.addAttribute("subjects", facultyService.mySubjects(authService.currentUser()));
        model.addAttribute("classSnapshots", facultyService.myClassSnapshots(authService.currentUser()));
    }
}
