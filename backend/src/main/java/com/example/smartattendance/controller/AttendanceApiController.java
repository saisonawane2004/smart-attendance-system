package com.example.smartattendance.controller;

import com.example.smartattendance.dto.MarkAttendanceRequest;
import com.example.smartattendance.service.AttendanceService;
import com.example.smartattendance.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/attendance")
public class AttendanceApiController {
    private final AttendanceService attendanceService;
    private final AuthService authService;

    public AttendanceApiController(AttendanceService attendanceService, AuthService authService) {
        this.attendanceService = attendanceService;
        this.authService = authService;
    }

    @PostMapping("/mark")
    public ResponseEntity<Map<String, Object>> mark(@Valid @RequestBody MarkAttendanceRequest request) {
        var attendance = attendanceService.markAttendance(authService.currentUser(), request);
        return ResponseEntity.ok(Map.of("id", attendance.getId(), "status", attendance.getStatus().name()));
    }
}
