package com.example.smartattendance.repository;

import com.example.smartattendance.entity.Attendance;
import com.example.smartattendance.entity.AttendanceSession;
import com.example.smartattendance.entity.Student;
import com.example.smartattendance.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    boolean existsByStudentAndSession(Student student, AttendanceSession session);
    List<Attendance> findByStudent(Student student);

    @Query("select a from Attendance a where a.session.subject = :subject")
    List<Attendance> findBySubject(Subject subject);

    @Query("select a from Attendance a where a.timestamp between :start and :end")
    List<Attendance> findByDateRange(LocalDateTime start, LocalDateTime end);

    List<Attendance> findBySessionSubjectAndTimestampBetween(Subject subject, LocalDateTime start, LocalDateTime end);
    List<Attendance> findByStudentAndTimestampBetween(Student student, LocalDateTime start, LocalDateTime end);
    long countByStudentAndSessionSubjectAndTimestampBetween(Student student, Subject subject, LocalDateTime start, LocalDateTime end);
    List<Attendance> findByStudentAndSessionSubjectClassroomAndTimestampBetween(
            Student student, com.example.smartattendance.entity.Classroom classroom, LocalDateTime start, LocalDateTime end
    );
}
