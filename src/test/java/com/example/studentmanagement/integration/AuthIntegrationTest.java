package com.example.studentmanagement.integration;

import com.example.studentmanagement.dto.StudentRegistrationDto;
import com.example.studentmanagement.dto.TeacherRegistrationDto;
import com.example.studentmanagement.repository.UserRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Auth endpoints.
 * Uses H2 in-memory database with the full Spring context.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void registerStudent_Success() throws Exception {
        StudentRegistrationDto dto = new StudentRegistrationDto();
        dto.setUsername("student1");
        dto.setPassword("password123");
        dto.setEmail("student1@test.com");
        dto.setFullName("Test Student");
        dto.setStudentId("STU001");
        dto.setPhoneNumber("1234567890");

        mockMvc.perform(post("/api/auth/register/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Student registered successfully")))
                .andExpect(jsonPath("$.data.username", is("student1")))
                .andExpect(jsonPath("$.data.email", is("student1@test.com")));
    }

    @Test
    void registerStudent_DuplicateUsername() throws Exception {
        StudentRegistrationDto dto = new StudentRegistrationDto();
        dto.setUsername("duplicate_user");
        dto.setPassword("password123");
        dto.setEmail("first@test.com");
        dto.setFullName("First Student");
        dto.setStudentId("STU001");

        // Register first time
        mockMvc.perform(post("/api/auth/register/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        // Register second time with same username
        dto.setEmail("second@test.com");
        dto.setStudentId("STU002");
        mockMvc.perform(post("/api/auth/register/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void registerStudent_ValidationError_MissingUsername() throws Exception {
        StudentRegistrationDto dto = new StudentRegistrationDto();
        dto.setPassword("password123");
        dto.setEmail("student@test.com");
        dto.setFullName("Test Student");
        dto.setStudentId("STU001");

        mockMvc.perform(post("/api/auth/register/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerTeacher_Success() throws Exception {
        TeacherRegistrationDto dto = new TeacherRegistrationDto();
        dto.setUsername("teacher1");
        dto.setPassword("password123");
        dto.setEmail("teacher1@test.com");
        dto.setFullName("Test Teacher");
        dto.setEmployeeId("EMP001");
        dto.setPhoneNumber("9876543210");
        dto.setDesignation("Professor");

        mockMvc.perform(post("/api/auth/register/teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Teacher registered successfully")))
                .andExpect(jsonPath("$.data.username", is("teacher1")))
                .andExpect(jsonPath("$.data.employeeId", is("EMP001")));
    }

    @Test
    void registerTeacher_ValidationError_ShortPassword() throws Exception {
        TeacherRegistrationDto dto = new TeacherRegistrationDto();
        dto.setUsername("teacher1");
        dto.setPassword("123");  // Too short
        dto.setEmail("teacher@test.com");
        dto.setFullName("Test Teacher");
        dto.setEmployeeId("EMP001");

        mockMvc.perform(post("/api/auth/register/teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_WithValidCredentials() throws Exception {
        // First register a student
        StudentRegistrationDto dto = new StudentRegistrationDto();
        dto.setUsername("loginuser");
        dto.setPassword("password123");
        dto.setEmail("login@test.com");
        dto.setFullName("Login User");
        dto.setStudentId("STU999");

        mockMvc.perform(post("/api/auth/register/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        // Then login with Basic Auth
        mockMvc.perform(get("/api/auth/login")
                        .header("Authorization", basicAuth("loginuser", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Login successful")));
    }

    @Test
    void login_WithInvalidCredentials() throws Exception {
        mockMvc.perform(get("/api/auth/login")
                        .header("Authorization", basicAuth("nonexistent", "wrongpass")))
                .andExpect(status().isUnauthorized());
    }

    private String basicAuth(String username, String password) {
        String credentials = username + ":" + password;
        return "Basic " + java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
    }
}


