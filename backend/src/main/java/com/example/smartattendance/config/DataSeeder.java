package com.example.smartattendance.config;

import com.example.smartattendance.entity.*;
import com.example.smartattendance.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.UUID;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seed(UserRepository userRepository, ClassroomRepository classroomRepository,
                           FacultyRepository facultyRepository, StudentRepository studentRepository,
                           SubjectRepository subjectRepository, AttendanceSessionRepository sessionRepository,
                           AttendanceRepository attendanceRepository) {
        return args -> {
            User admin = userRepository.findByEmail("admin@smart.com").orElseGet(() -> {
                User user = new User();
                user.setName("System Admin");
                user.setEmail("admin@smart.com");
                user.setPassword("1234");
                user.setRole(Role.ADMIN);
                return userRepository.save(user);
            });

            User facultyUser = userRepository.findByEmail("faculty@smart.com").orElseGet(() -> {
                User user = new User();
                user.setName("Faculty One");
                user.setEmail("faculty@smart.com");
                user.setPassword("1234");
                user.setRole(Role.FACULTY);
                return userRepository.save(user);
            });

            Faculty faculty = facultyRepository.findByUser(facultyUser).orElseGet(() -> {
                Faculty row = new Faculty();
                row.setUser(facultyUser);
                row.setDepartment("CSE");
                return facultyRepository.save(row);
            });

            Classroom classroom = classroomRepository.findById(1L).orElseGet(() -> {
                Classroom row = new Classroom();
                row.setName("BSc CS");
                row.setSection("A");
                row.setYearLevel(1);
                return classroomRepository.save(row);
            });

            User studentUser = userRepository.findByEmail("student@smart.com").orElseGet(() -> {
                User user = new User();
                user.setName("Student One");
                user.setEmail("student@smart.com");
                user.setPassword("1234");
                user.setRole(Role.STUDENT);
                return userRepository.save(user);
            });

            Student student = studentRepository.findByUser(studentUser).orElseGet(() -> {
                Student row = new Student();
                row.setUser(studentUser);
                row.setRollNumber("CS001");
                row.setClassroom(classroom);
                return studentRepository.save(row);
            });

            User studentUser2 = userRepository.findByEmail("student2@smart.com").orElseGet(() -> {
                User user = new User();
                user.setName("Student Two");
                user.setEmail("student2@smart.com");
                user.setPassword("1234");
                user.setRole(Role.STUDENT);
                return userRepository.save(user);
            });

            Student student2 = studentRepository.findByUser(studentUser2).orElseGet(() -> {
                Student row = new Student();
                row.setUser(studentUser2);
                row.setRollNumber("CS002");
                row.setClassroom(classroom);
                return studentRepository.save(row);
            });

            if (subjectRepository.findByFaculty(faculty).isEmpty()) {
                Subject subject = new Subject();
                subject.setName("Software Engineering");
                subject.setFaculty(faculty);
                subject.setClassroom(classroom);
                subjectRepository.save(subject);

                Subject subject2 = new Subject();
                subject2.setName("Database Systems");
                subject2.setFaculty(faculty);
                subject2.setClassroom(classroom);
                subjectRepository.save(subject2);
            }

            if (attendanceRepository.count() == 0) {
                Subject firstSubject = subjectRepository.findByFaculty(faculty).get(0);
                Subject secondSubject = subjectRepository.findByFaculty(faculty).size() > 1
                        ? subjectRepository.findByFaculty(faculty).get(1)
                        : firstSubject;

                for (int i = 1; i <= 12; i++) {
                    AttendanceSession session = new AttendanceSession();
                    session.setSubject(i % 2 == 0 ? firstSubject : secondSubject);
                    session.setQrToken(UUID.randomUUID().toString());
                    session.setExpiryTime(LocalDateTime.now().minusDays(i).plusMinutes(5));
                    session.setLatitude(18.5204);
                    session.setLongitude(73.8567);
                    session.setRadius(100.0);
                    sessionRepository.save(session);

                    Attendance a1 = new Attendance();
                    a1.setStudent(student);
                    a1.setSession(session);
                    a1.setTimestamp(LocalDateTime.now().minusDays(i));
                    a1.setStatus(AttendanceStatus.PRESENT);
                    attendanceRepository.save(a1);

                    if (i % 3 != 0) {
                        Attendance a2 = new Attendance();
                        a2.setStudent(student2);
                        a2.setSession(session);
                        a2.setTimestamp(LocalDateTime.now().minusDays(i));
                        a2.setStatus(AttendanceStatus.PRESENT);
                        attendanceRepository.save(a2);
                    }
                }
            }
        };
    }
}
