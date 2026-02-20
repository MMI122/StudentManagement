package com.example.studentmanagement.service;

import com.example.studentmanagement.dto.StudentRegistrationDto;
import com.example.studentmanagement.dto.TeacherRegistrationDto;
import com.example.studentmanagement.entity.Department;
import com.example.studentmanagement.entity.Role;
import com.example.studentmanagement.entity.Student;
import com.example.studentmanagement.entity.Teacher;
import com.example.studentmanagement.repository.DepartmentRepository;
import com.example.studentmanagement.repository.StudentRepository;
import com.example.studentmanagement.repository.TeacherRepository;
import com.example.studentmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Student registerStudent(StudentRegistrationDto dto) {
        // Check if username already exists
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Check if student ID already exists
        if (studentRepository.existsByStudentId(dto.getStudentId())) {
            throw new RuntimeException("Student ID already exists");
        }

        Student student = new Student();
        student.setUsername(dto.getUsername());
        student.setPassword(passwordEncoder.encode(dto.getPassword()));
        student.setEmail(dto.getEmail());
        student.setFullName(dto.getFullName());
        student.setRole(Role.ROLE_STUDENT);
        student.setStudentId(dto.getStudentId());
        student.setPhoneNumber(dto.getPhoneNumber());

        // Set department if provided
        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            student.setDepartment(department);
        }

        return studentRepository.save(student);
    }

    @Transactional
    public Teacher registerTeacher(TeacherRegistrationDto dto) {
        // Check if username already exists
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Check if employee ID already exists
        if (teacherRepository.existsByEmployeeId(dto.getEmployeeId())) {
            throw new RuntimeException("Employee ID already exists");
        }

        Teacher teacher = new Teacher();
        teacher.setUsername(dto.getUsername());
        teacher.setPassword(passwordEncoder.encode(dto.getPassword()));
        teacher.setEmail(dto.getEmail());
        teacher.setFullName(dto.getFullName());
        teacher.setRole(Role.ROLE_TEACHER);
        teacher.setEmployeeId(dto.getEmployeeId());
        teacher.setPhoneNumber(dto.getPhoneNumber());
        teacher.setDesignation(dto.getDesignation());

        return teacherRepository.save(teacher);
    }
}
