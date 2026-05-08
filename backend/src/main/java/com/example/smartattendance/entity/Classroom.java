package com.example.smartattendance.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "classes")
public class Classroom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String section;

    @Column(nullable = false)
    private Integer yearLevel = 1;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public Integer getYearLevel() { return yearLevel; }
    public void setYearLevel(Integer yearLevel) { this.yearLevel = yearLevel; }
}
