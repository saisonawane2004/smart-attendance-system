package com.example.smartattendance.service;

import com.example.smartattendance.dto.MarkAttendanceRequest;
import com.example.smartattendance.entity.*;
import com.example.smartattendance.repository.*;
import com.example.smartattendance.utils.LocationUtil;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@Transactional
public class AttendanceService {
    private final StudentRepository studentRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRepository attendanceRepository;
    private final SubjectRepository subjectRepository;
    private final LocationUtil locationUtil;

    public AttendanceService(StudentRepository studentRepository, AttendanceSessionRepository sessionRepository,
                             AttendanceRepository attendanceRepository, SubjectRepository subjectRepository, LocationUtil locationUtil) {
        this.studentRepository = studentRepository;
        this.sessionRepository = sessionRepository;
        this.attendanceRepository = attendanceRepository;
        this.subjectRepository = subjectRepository;
        this.locationUtil = locationUtil;
    }

    public Attendance markAttendance(User user, MarkAttendanceRequest request) {
        Student student = studentRepository.findByUser(user).orElseThrow();
        AttendanceSession session = sessionRepository.findByQrToken(request.getQrToken()).orElseThrow();

        if (session.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("QR code expired");
        }

        if (attendanceRepository.existsByStudentAndSession(student, session)) {
            throw new IllegalArgumentException("Attendance already marked");
        }

        double distance = locationUtil.distanceMeters(request.getLatitude(), request.getLongitude(),
                session.getLatitude(), session.getLongitude());
        if (distance > session.getRadius()) {
            throw new IllegalArgumentException("You are outside allowed location radius");
        }

        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setSession(session);
        attendance.setTimestamp(LocalDateTime.now());
        attendance.setStatus(AttendanceStatus.PRESENT);
        return attendanceRepository.save(attendance);
    }

    public List<Attendance> attendanceForStudent(User user) {
        Student student = studentRepository.findByUser(user).orElseThrow();
        return attendanceRepository.findByStudent(student);
    }

    public List<Attendance> dailyReport(LocalDate date) {
        return attendanceRepository.findByDateRange(date.atStartOfDay(), date.plusDays(1).atStartOfDay().minusNanos(1));
    }

    public List<Attendance> monthlyReport(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);
        return attendanceRepository.findByDateRange(start.atStartOfDay(), end.atTime(23, 59, 59));
    }

    public List<Attendance> yearlyReport(int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);
        return attendanceRepository.findByDateRange(start.atStartOfDay(), end.atTime(23, 59, 59));
    }

    public List<Attendance> studentReport(User user, String periodType, LocalDate date, Integer month, Integer year) {
        Student student = studentRepository.findByUser(user).orElseThrow();
        LocalDateTime start;
        LocalDateTime end;
        if ("monthly".equalsIgnoreCase(periodType)) {
            int reportYear = year == null ? LocalDate.now().getYear() : year;
            int reportMonth = month == null ? LocalDate.now().getMonthValue() : month;
            YearMonth ym = YearMonth.of(reportYear, reportMonth);
            start = ym.atDay(1).atStartOfDay();
            end = ym.atEndOfMonth().atTime(23, 59, 59);
        } else if ("yearly".equalsIgnoreCase(periodType)) {
            int reportYear = year == null ? LocalDate.now().getYear() : year;
            start = LocalDate.of(reportYear, 1, 1).atStartOfDay();
            end = LocalDate.of(reportYear, 12, 31).atTime(23, 59, 59);
        } else {
            LocalDate selected = date == null ? LocalDate.now() : date;
            start = selected.atStartOfDay();
            end = selected.atTime(23, 59, 59);
        }
        return attendanceRepository.findByStudentAndTimestampBetween(student, start, end);
    }

    public AttendanceSummary studentSummary(User user, String periodType, LocalDate date, Integer month, Integer year) {
        Student student = studentRepository.findByUser(user).orElseThrow();
        LocalDateTime start;
        LocalDateTime end;
        if ("monthly".equalsIgnoreCase(periodType)) {
            int reportYear = year == null ? LocalDate.now().getYear() : year;
            int reportMonth = month == null ? LocalDate.now().getMonthValue() : month;
            YearMonth ym = YearMonth.of(reportYear, reportMonth);
            start = ym.atDay(1).atStartOfDay();
            end = ym.atEndOfMonth().atTime(23, 59, 59);
        } else if ("yearly".equalsIgnoreCase(periodType)) {
            int reportYear = year == null ? LocalDate.now().getYear() : year;
            start = LocalDate.of(reportYear, 1, 1).atStartOfDay();
            end = LocalDate.of(reportYear, 12, 31).atTime(23, 59, 59);
        } else {
            LocalDate selected = date == null ? LocalDate.now() : date;
            start = selected.atStartOfDay();
            end = selected.atTime(23, 59, 59);
        }

        LocalDate startDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();
        long daysCompleted = Math.max(java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1, 0);
        long presentDays = attendanceRepository.findByStudentAndTimestampBetween(student, start, end)
                .stream()
                .map(a -> a.getTimestamp().toLocalDate())
                .distinct()
                .count();
        long absentDays = Math.max(daysCompleted - presentDays, 0);
        double percentage = daysCompleted == 0 ? 0.0 : (presentDays * 100.0) / daysCompleted;
        return new AttendanceSummary(daysCompleted, presentDays, absentDays, percentage);
    }

    public record AttendanceSummary(long daysCompleted, long presents, long absents, double percentage) {}

    public String exportCsv(List<Attendance> rows) {
        try {
            StringWriter sw = new StringWriter();
            CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withHeader("Student", "Roll", "Subject", "Time", "Status"));
            for (Attendance a : rows) {
                printer.printRecord(
                        a.getStudent().getUser().getName(),
                        a.getStudent().getRollNumber(),
                        a.getSession().getSubject().getName(),
                        a.getTimestamp(),
                        a.getStatus()
                );
            }
            printer.flush();
            return sw.toString();
        } catch (Exception e) {
            throw new IllegalStateException("CSV generation failed", e);
        }
    }
}
