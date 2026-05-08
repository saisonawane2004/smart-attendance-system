package com.example.smartattendance.service;

import com.example.smartattendance.dto.SessionCreateRequest;
import com.example.smartattendance.dto.ManualAttendanceRequest;
import com.example.smartattendance.entity.*;
import com.example.smartattendance.repository.AttendanceRepository;
import com.example.smartattendance.repository.AttendanceSessionRepository;
import com.example.smartattendance.repository.ClassroomRepository;
import com.example.smartattendance.repository.FacultyRepository;
import com.example.smartattendance.repository.StudentRepository;
import com.example.smartattendance.repository.SubjectRepository;
import com.example.smartattendance.repository.UserRepository;
import com.example.smartattendance.utils.QrCodeUtil;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FacultyService {
    private final SubjectRepository subjectRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final FacultyRepository facultyRepository;
    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final QrCodeUtil qrCodeUtil;

    public FacultyService(SubjectRepository subjectRepository, AttendanceSessionRepository sessionRepository,
                          FacultyRepository facultyRepository, AttendanceRepository attendanceRepository,
                          StudentRepository studentRepository, ClassroomRepository classroomRepository,
                          UserRepository userRepository, QrCodeUtil qrCodeUtil) {
        this.subjectRepository = subjectRepository;
        this.sessionRepository = sessionRepository;
        this.facultyRepository = facultyRepository;
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
        this.classroomRepository = classroomRepository;
        this.userRepository = userRepository;
        this.qrCodeUtil = qrCodeUtil;
    }

    public List<Subject> mySubjects(com.example.smartattendance.entity.User user) {
        Faculty faculty = facultyRepository.findByUser(user).orElseThrow();
        return subjectRepository.findByFaculty(faculty);
    }

    public AttendanceSession createSession(com.example.smartattendance.entity.User user, SessionCreateRequest request) {
        Faculty faculty = facultyRepository.findByUser(user).orElseThrow();
        Subject subject = subjectRepository.findById(request.getSubjectId()).orElseThrow();
        if (!subject.getFaculty().getId().equals(faculty.getId())) {
            throw new IllegalArgumentException("You can only create sessions for your own class/subject.");
        }
        AttendanceSession session = new AttendanceSession();
        session.setSubject(subject);
        session.setQrToken(UUID.randomUUID().toString());
        session.setExpiryTime(LocalDateTime.now().plusMinutes(1));
        session.setLatitude(request.getLatitude());
        session.setLongitude(request.getLongitude());
        session.setRadius(request.getRadius());
        return sessionRepository.save(session);
    }

    public Attendance markManualAttendance(com.example.smartattendance.entity.User user, ManualAttendanceRequest request) {
        Faculty faculty = facultyRepository.findByUser(user).orElseThrow();
        Subject subject = subjectRepository.findById(request.getSubjectId()).orElseThrow();
        if (!subject.getFaculty().getId().equals(faculty.getId())) {
            throw new IllegalArgumentException("You can only mark attendance for your own subject.");
        }

        Student student = studentRepository.findByRollNumber(request.getRollNumber().trim())
                .orElseThrow(() -> new IllegalArgumentException("Student roll number not found."));
        if (!student.getClassroom().getId().equals(subject.getClassroom().getId())) {
            throw new IllegalArgumentException("Selected student is not part of this subject's class.");
        }

        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        LocalDateTime dayEnd = LocalDate.now().atTime(LocalTime.MAX);
        AttendanceSession session = sessionRepository
                .findFirstBySubjectAndExpiryTimeBetweenOrderByExpiryTimeDesc(subject, dayStart, dayEnd)
                .orElseGet(() -> {
                    AttendanceSession manualSession = new AttendanceSession();
                    manualSession.setSubject(subject);
                    manualSession.setQrToken("manual-" + UUID.randomUUID());
                    manualSession.setExpiryTime(LocalDateTime.now());
                    manualSession.setLatitude(0.0);
                    manualSession.setLongitude(0.0);
                    manualSession.setRadius(0.0);
                    return sessionRepository.save(manualSession);
                });

        if (attendanceRepository.existsByStudentAndSession(student, session)) {
            throw new IllegalArgumentException("Attendance already marked for this lecture.");
        }

        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setSession(session);
        attendance.setTimestamp(LocalDateTime.now());
        attendance.setStatus(AttendanceStatus.PRESENT);
        return attendanceRepository.save(attendance);
    }

    public String qrBase64(AttendanceSession session) {
        return qrCodeUtil.generateBase64Png(session.getQrToken());
    }

    public String markAttendanceUrl(AttendanceSession session) {
        return "/student/dashboard?token=" + session.getQrToken();
    }

    public List<Attendance> attendanceBySubject(Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId).orElseThrow();
        return attendanceRepository.findBySubject(subject);
    }

    public List<Attendance> attendanceBySubject(Long subjectId, LocalDateTime start, LocalDateTime end, Long studentId) {
        Subject subject = subjectRepository.findById(subjectId).orElseThrow();
        List<Attendance> rows = attendanceRepository.findBySessionSubjectAndTimestampBetween(subject, start, end);
        if (studentId == null) {
            return rows;
        }
        return rows.stream().filter(a -> a.getStudent().getId().equals(studentId)).toList();
    }

    public FacultySummary subjectSummary(Long subjectId, LocalDateTime start, LocalDateTime end, Long studentId) {
        Subject subject = subjectRepository.findById(subjectId).orElseThrow();
        long totalSessions = sessionRepository.countBySubjectAndExpiryTimeBetween(subject, start, end);
        long presents;
        if (studentId == null) {
            presents = attendanceRepository.findBySessionSubjectAndTimestampBetween(subject, start, end).size();
            return new FacultySummary(totalSessions, presents, 0, 0.0);
        }
        Student student = studentRepository.findById(studentId).orElseThrow();
        presents = attendanceRepository.countByStudentAndSessionSubjectAndTimestampBetween(student, subject, start, end);
        long leaves = Math.max(totalSessions - presents, 0);
        double percentage = totalSessions == 0 ? 0.0 : (presents * 100.0) / totalSessions;
        return new FacultySummary(totalSessions, presents, leaves, percentage);
    }

    public List<Student> studentsForSubject(Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId).orElseThrow();
        return studentRepository.findByClassroom(subject.getClassroom());
    }

    public List<Student> studentsForClass(Long classId) {
        Classroom classroom = classroomRepository.findById(classId).orElseThrow();
        return studentRepository.findByClassroom(classroom);
    }

    public ImportResult importStudentsFromExcel(com.example.smartattendance.entity.User user, Long classId, MultipartFile file) {
        Faculty faculty = facultyRepository.findByUser(user).orElseThrow();
        Classroom classroom = classroomRepository.findById(classId).orElseThrow();
        boolean facultyOwnsClass = subjectRepository.findByFaculty(faculty).stream()
                .anyMatch(s -> s.getClassroom().getId().equals(classroom.getId()));
        if (!facultyOwnsClass) {
            throw new IllegalArgumentException("You are not assigned to this class.");
        }

        int created = 0;
        int updated = 0;
        int skipped = 0;
        try (InputStream in = file.getInputStream(); Workbook workbook = WorkbookFactory.create(in)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String name = cell(row, 0);
                String email = cell(row, 1);
                String roll = cell(row, 2);
                if (name.isBlank() || email.isBlank()) {
                    skipped++;
                    continue;
                }

                User userRow = userRepository.findByEmail(email).orElse(null);
                if (userRow == null) {
                    userRow = new User();
                    userRow.setName(name);
                    userRow.setEmail(email);
                    userRow.setPassword("1234");
                    userRow.setRole(Role.STUDENT);
                    userRepository.save(userRow);

                    Student student = new Student();
                    student.setUser(userRow);
                    student.setRollNumber(roll.isBlank() ? "AUTO" + System.nanoTime() : roll);
                    student.setClassroom(classroom);
                    studentRepository.save(student);
                    created++;
                } else if (userRow.getRole() == Role.STUDENT) {
                    userRow.setName(name);
                    userRow.setPassword("1234");
                    userRepository.save(userRow);

                    Student student = studentRepository.findByUser(userRow).orElseGet(Student::new);
                    student.setUser(userRow);
                    if (!roll.isBlank()) {
                        student.setRollNumber(roll);
                    } else if (student.getRollNumber() == null || student.getRollNumber().isBlank()) {
                        student.setRollNumber("AUTO" + System.nanoTime());
                    }
                    student.setClassroom(classroom);
                    studentRepository.save(student);
                    updated++;
                } else {
                    skipped++;
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Excel file. Expected columns: name, email, rollNumber", e);
        }
        return new ImportResult(created, updated, skipped);
    }

    public void removeStudentFromClass(com.example.smartattendance.entity.User user, Long classId, Long studentId) {
        Faculty faculty = facultyRepository.findByUser(user).orElseThrow();
        boolean facultyOwnsClass = subjectRepository.findByFaculty(faculty).stream()
                .anyMatch(s -> s.getClassroom().getId().equals(classId));
        if (!facultyOwnsClass) {
            throw new IllegalArgumentException("You are not assigned to this class.");
        }

        Student student = studentRepository.findById(studentId).orElseThrow();
        if (!student.getClassroom().getId().equals(classId)) {
            throw new IllegalArgumentException("Student is not in selected class.");
        }
        studentRepository.delete(student);
        userRepository.delete(student.getUser());
    }

    public List<ClassSnapshot> myClassSnapshots(com.example.smartattendance.entity.User user) {
        Faculty faculty = facultyRepository.findByUser(user).orElseThrow();
        int daysCompleted = java.time.LocalDate.now().getDayOfYear();
        return subjectRepository.findByFaculty(faculty).stream()
                .map(Subject::getClassroom)
                .distinct()
                .map(c -> new ClassSnapshot(c.getId(), c.getName(), c.getSection(), c.getYearLevel(),
                        studentRepository.findByClassroom(c).size(), daysCompleted, Year.now().length()))
                .toList();
    }

    private String cell(Row row, int i) {
        Cell c = row.getCell(i);
        if (c == null) return "";
        c.setCellType(CellType.STRING);
        return c.getStringCellValue().trim();
    }

    public record FacultySummary(long totalSessions, long presents, long leaves, double percentage) {}
    public record ImportResult(int created, int updated, int skipped) {}
    public record ClassSnapshot(Long classId, String className, String section, Integer yearLevel,
                                int totalStudents, int daysCompleted, int totalDaysInYear) {}
}
