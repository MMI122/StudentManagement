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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private StudentRegistrationDto studentDto;
    private TeacherRegistrationDto teacherDto;
    private Department department;

    @BeforeEach
    void setUp() {
        studentDto = new StudentRegistrationDto();
        studentDto.setUsername("newstudent");
        studentDto.setPassword("password123");
        studentDto.setEmail("student@example.com");
        studentDto.setFullName("New Student");
        studentDto.setStudentId("STU001");
        studentDto.setPhoneNumber("1234567890");

        teacherDto = new TeacherRegistrationDto();
        teacherDto.setUsername("newteacher");
        teacherDto.setPassword("password123");
        teacherDto.setEmail("teacher@example.com");
        teacherDto.setFullName("New Teacher");
        teacherDto.setEmployeeId("EMP001");
        teacherDto.setPhoneNumber("0987654321");
        teacherDto.setDesignation("Professor");

        department = new Department();
        department.setId(1L);
        department.setName("Computer Science");
        department.setDescription("CS Department");
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void registerStudent() {
        when(userRepository.existsByUsername("newstudent")).thenReturn(false);
        when(userRepository.existsByEmail("student@example.com")).thenReturn(false);
        when(studentRepository.existsByStudentId("STU001")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> {
            Student saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Student result = authService.registerStudent(studentDto);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("newstudent");
        assertThat(result.getEmail()).isEqualTo("student@example.com");
        assertThat(result.getRole()).isEqualTo(Role.ROLE_STUDENT);
        assertThat(result.getStudentId()).isEqualTo("STU001");
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void registerStudent_WithDepartment() {
        studentDto.setDepartmentId(1L);

        when(userRepository.existsByUsername("newstudent")).thenReturn(false);
        when(userRepository.existsByEmail("student@example.com")).thenReturn(false);
        when(studentRepository.existsByStudentId("STU001")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> {
            Student saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Student result = authService.registerStudent(studentDto);

        assertThat(result).isNotNull();
        assertThat(result.getDepartment()).isEqualTo(department);
        verify(departmentRepository, times(1)).findById(1L);
    }

    @Test
    void registerStudent_DepartmentNotFound() {
        studentDto.setDepartmentId(99L);

        when(userRepository.existsByUsername("newstudent")).thenReturn(false);
        when(userRepository.existsByEmail("student@example.com")).thenReturn(false);
        when(studentRepository.existsByStudentId("STU001")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.registerStudent(studentDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Department not found");
    }

    @Test
    void registerStudent_UsernameExists() {
        when(userRepository.existsByUsername("newstudent")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerStudent(studentDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Username already exists");

        verify(studentRepository, never()).save(any());
    }

    @Test
    void registerStudent_EmailExists() {
        when(userRepository.existsByUsername("newstudent")).thenReturn(false);
        when(userRepository.existsByEmail("student@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerStudent(studentDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already exists");

        verify(studentRepository, never()).save(any());
    }

    @Test
    void registerStudent_StudentIdExists() {
        when(userRepository.existsByUsername("newstudent")).thenReturn(false);
        when(userRepository.existsByEmail("student@example.com")).thenReturn(false);
        when(studentRepository.existsByStudentId("STU001")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerStudent(studentDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Student ID already exists");

        verify(studentRepository, never()).save(any());
    }

    @Test
    void registerTeacher() {
        when(userRepository.existsByUsername("newteacher")).thenReturn(false);
        when(userRepository.existsByEmail("teacher@example.com")).thenReturn(false);
        when(teacherRepository.existsByEmployeeId("EMP001")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(teacherRepository.save(any(Teacher.class))).thenAnswer(invocation -> {
            Teacher saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Teacher result = authService.registerTeacher(teacherDto);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("newteacher");
        assertThat(result.getEmail()).isEqualTo("teacher@example.com");
        assertThat(result.getRole()).isEqualTo(Role.ROLE_TEACHER);
        assertThat(result.getEmployeeId()).isEqualTo("EMP001");
        assertThat(result.getDesignation()).isEqualTo("Professor");
        verify(teacherRepository, times(1)).save(any(Teacher.class));
    }

    @Test
    void registerTeacher_UsernameExists() {
        when(userRepository.existsByUsername("newteacher")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerTeacher(teacherDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Username already exists");

        verify(teacherRepository, never()).save(any());
    }

    @Test
    void registerTeacher_EmailExists() {
        when(userRepository.existsByUsername("newteacher")).thenReturn(false);
        when(userRepository.existsByEmail("teacher@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerTeacher(teacherDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already exists");

        verify(teacherRepository, never()).save(any());
    }

    @Test
    void registerTeacher_EmployeeIdExists() {
        when(userRepository.existsByUsername("newteacher")).thenReturn(false);
        when(userRepository.existsByEmail("teacher@example.com")).thenReturn(false);
        when(teacherRepository.existsByEmployeeId("EMP001")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerTeacher(teacherDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Employee ID already exists");

        verify(teacherRepository, never()).save(any());
    }
}