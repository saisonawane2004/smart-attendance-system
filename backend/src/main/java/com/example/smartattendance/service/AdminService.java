package com.example.smartattendance.service;

import com.example.smartattendance.entity.*;
import com.example.smartattendance.repository.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminService {
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final ClassroomRepository classroomRepository;
    private final SubjectRepository subjectRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendanceRepository attendanceRepository;

    public AdminService(UserRepository userRepository, StudentRepository studentRepository, FacultyRepository facultyRepository,
                        ClassroomRepository classroomRepository, SubjectRepository subjectRepository,
                        AttendanceSessionRepository attendanceSessionRepository, AttendanceRepository attendanceRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.classroomRepository = classroomRepository;
        this.subjectRepository = subjectRepository;
        this.attendanceSessionRepository = attendanceSessionRepository;
        this.attendanceRepository = attendanceRepository;
    }

    public Page<Student> students(int page, int size) { return studentRepository.findAll(PageRequest.of(page, size)); }
    public Page<Faculty> faculty(int page, int size) { return facultyRepository.findAll(PageRequest.of(page, size)); }
    public Page<Subject> subjects(int page, int size) { return subjectRepository.findAll(PageRequest.of(page, size)); }
    public Page<Classroom> classrooms(int page, int size) { return classroomRepository.findAll(PageRequest.of(page, size)); }

    public Student addStudent(String name, String email, String password, String rollNumber, Long classId) {
        String normalizedName = name == null ? "" : name.trim();
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        String normalizedRollNumber = rollNumber == null ? "" : rollNumber.trim();

        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("A user with this email already exists.");
        }
        if (studentRepository.findByRollNumber(normalizedRollNumber).isPresent()) {
            throw new IllegalArgumentException("A student with this roll number already exists.");
        }
        try {
            User user = new User();
            user.setName(normalizedName);
            user.setEmail(normalizedEmail);
            user.setPassword(password);
            user.setRole(Role.STUDENT);
            userRepository.saveAndFlush(user);

            Student student = new Student();
            student.setUser(user);
            student.setRollNumber(normalizedRollNumber);
            student.setClassroom(classroomRepository.findById(classId).orElseThrow());
            return studentRepository.saveAndFlush(student);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("Student details already exist. Try a different email or roll number.");
        }
    }

    public Faculty addFaculty(String name, String email, String password, String department) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(Role.FACULTY);
        userRepository.save(user);

        Faculty faculty = new Faculty();
        faculty.setUser(user);
        faculty.setDepartment(department);
        return facultyRepository.save(faculty);
    }

    public Classroom addClassroom(String name, String section, Integer yearLevel) {
        Classroom classroom = new Classroom();
        classroom.setName(name);
        classroom.setSection(section);
        classroom.setYearLevel(yearLevel == null ? 1 : yearLevel);
        return classroomRepository.save(classroom);
    }

    public Subject addSubject(String name, Long facultyId, Long classId) {
        Subject subject = new Subject();
        subject.setName(name);
        subject.setFaculty(facultyRepository.findById(facultyId).orElseThrow());
        subject.setClassroom(classroomRepository.findById(classId).orElseThrow());
        return subjectRepository.save(subject);
    }

    public void deleteStudent(Long studentId) {
        Student student = studentRepository.findById(studentId).orElseThrow();
        attendanceRepository.deleteAll(attendanceRepository.findByStudent(student));
        attendanceRepository.flush();
        User user = student.getUser();
        studentRepository.delete(student);
        studentRepository.flush();
        userRepository.delete(user);
        userRepository.flush();
    }

    public void deleteFaculty(Long facultyId) {
        Faculty faculty = facultyRepository.findById(facultyId).orElseThrow();
        User user = faculty.getUser();
        List<Subject> facultySubjects = subjectRepository.findByFaculty(faculty);
        Set<Long> affectedClassroomIds = facultySubjects.stream()
                .map(subject -> subject.getClassroom().getId())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (Subject subject : facultySubjects) {
            attendanceRepository.deleteAll(attendanceRepository.findBySubject(subject));
            attendanceRepository.flush();
            attendanceSessionRepository.deleteAll(attendanceSessionRepository.findBySubject(subject));
            attendanceSessionRepository.flush();
            subjectRepository.delete(subject);
            subjectRepository.flush();
        }

        for (Long classroomId : affectedClassroomIds) {
            List<Student> students = studentRepository.findByClassroomId(classroomId);
            for (Student student : students) {
                attendanceRepository.deleteAll(attendanceRepository.findByStudent(student));
                attendanceRepository.flush();
                User studentUser = student.getUser();
                studentRepository.delete(student);
                studentRepository.flush();
                userRepository.delete(studentUser);
                userRepository.flush();
            }

            if (!subjectRepository.existsByClassroomId(classroomId)) {
                classroomRepository.findById(classroomId).ifPresent(classroom -> {
                    classroomRepository.delete(classroom);
                    classroomRepository.flush();
                });
            }
        }

        facultyRepository.delete(faculty);
        facultyRepository.flush();
        userRepository.delete(user);
        userRepository.flush();
    }
}
