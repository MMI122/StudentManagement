package com.example.studentmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teachers")
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"courses"})
@NoArgsConstructor
@AllArgsConstructor
public class Teacher extends User {

    @Column(unique = true)
    private String employeeId;

    private String phoneNumber;

    private String designation;  // Professor, Associate Professor, etc.

    // One Teacher can teach Many Courses
    @JsonIgnore
    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL)
    private List<Course> courses = new ArrayList<>();
}
