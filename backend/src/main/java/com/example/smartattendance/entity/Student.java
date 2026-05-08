package com.example.smartattendance.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "students", indexes = {
        @Index(name = "idx_student_roll", columnList = "rollNumber", unique = true)
})
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, unique = true)
    private String rollNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "class_id", nullable = false)
    private Classroom classroom;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    public Classroom getClassroom() { return classroom; }
    public void setClassroom(Classroom classroom) { this.classroom = classroom; }
}
