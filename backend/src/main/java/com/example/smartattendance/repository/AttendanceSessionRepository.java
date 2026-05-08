package com.example.smartattendance.repository;

import com.example.smartattendance.entity.AttendanceSession;
import com.example.smartattendance.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {
    Optional<AttendanceSession> findByQrToken(String qrToken);
    List<AttendanceSession> findBySubject(Subject subject);
    Optional<AttendanceSession> findFirstBySubjectAndExpiryTimeBetweenOrderByExpiryTimeDesc(
            Subject subject, LocalDateTime start, LocalDateTime end
    );
    long countBySubjectAndExpiryTimeBetween(Subject subject, LocalDateTime start, LocalDateTime end);
    long countBySubjectInAndExpiryTimeBetween(List<Subject> subjects, LocalDateTime start, LocalDateTime end);
}
