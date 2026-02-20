package com.example.studentmanagement.controller;

import com.example.studentmanagement.dto.StudentRegistrationDto;
import com.example.studentmanagement.dto.TeacherRegistrationDto;
import com.example.studentmanagement.entity.Role;
import com.example.studentmanagement.entity.Student;
import com.example.studentmanagement.entity.Teacher;
import com.example.studentmanagement.security.CustomUserDetailsService;
import com.example.studentmanagement.service.AuthService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private StudentRegistrationDto studentDto;
    private TeacherRegistrationDto teacherDto;
    private Student student;
    private Teacher teacher;

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

        student = new Student();
        student.setId(1L);
        student.setUsername("newstudent");
        student.setEmail("student@example.com");
        student.setFullName("New Student");
        student.setRole(Role.ROLE_STUDENT);
        student.setStudentId("STU001");
        student.setCourses(new HashSet<>());

        teacher = new Teacher();
        teacher.setId(1L);
        teacher.setUsername("newteacher");
        teacher.setEmail("teacher@example.com");
        teacher.setFullName("New Teacher");
        teacher.setRole(Role.ROLE_TEACHER);
        teacher.setEmployeeId("EMP001");
        teacher.setDesignation("Professor");
    }

    @Test
    void registerStudent() throws Exception {
        when(authService.registerStudent(any(StudentRegistrationDto.class))).thenReturn(student);

        mockMvc.perform(post("/api/auth/register/student")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Student registered successfully")));

        verify(authService, times(1)).registerStudent(any(StudentRegistrationDto.class));
    }

    @Test
    void registerStudent_UsernameExists() throws Exception {
        when(authService.registerStudent(any(StudentRegistrationDto.class)))
                .thenThrow(new RuntimeException("Username already exists"));

        mockMvc.perform(post("/api/auth/register/student")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Username already exists")));
    }

    @Test
    void registerStudent_ValidationError_MissingUsername() throws Exception {
        StudentRegistrationDto invalidDto = new StudentRegistrationDto();
        invalidDto.setPassword("password123");
        invalidDto.setEmail("test@example.com");
        invalidDto.setFullName("Test");
        invalidDto.setStudentId("STU999");

        mockMvc.perform(post("/api/auth/register/student")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerStudent_ValidationError_InvalidEmail() throws Exception {
        StudentRegistrationDto invalidDto = new StudentRegistrationDto();
        invalidDto.setUsername("testuser");
        invalidDto.setPassword("password123");
        invalidDto.setEmail("not-an-email");
        invalidDto.setFullName("Test");
        invalidDto.setStudentId("STU999");

        mockMvc.perform(post("/api/auth/register/student")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerStudent_ValidationError_ShortPassword() throws Exception {
        StudentRegistrationDto invalidDto = new StudentRegistrationDto();
        invalidDto.setUsername("testuser");
        invalidDto.setPassword("123");
        invalidDto.setEmail("test@example.com");
        invalidDto.setFullName("Test");
        invalidDto.setStudentId("STU999");

        mockMvc.perform(post("/api/auth/register/student")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerTeacher() throws Exception {
        when(authService.registerTeacher(any(TeacherRegistrationDto.class))).thenReturn(teacher);

        mockMvc.perform(post("/api/auth/register/teacher")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(teacherDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Teacher registered successfully")));

        verify(authService, times(1)).registerTeacher(any(TeacherRegistrationDto.class));
    }

    @Test
    void registerTeacher_UsernameExists() throws Exception {
        when(authService.registerTeacher(any(TeacherRegistrationDto.class)))
                .thenThrow(new RuntimeException("Username already exists"));

        mockMvc.perform(post("/api/auth/register/teacher")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(teacherDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Username already exists")));
    }

    @Test
    void registerTeacher_ValidationError_MissingFields() throws Exception {
        TeacherRegistrationDto invalidDto = new TeacherRegistrationDto();

        mockMvc.perform(post("/api/auth/register/teacher")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "STUDENT")
    void login() throws Exception {
        mockMvc.perform(get("/api/auth/login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Login successful")));
    }
}