package com.example.studentmanagement.controller;

import com.example.studentmanagement.entity.Department;
import com.example.studentmanagement.entity.Role;
import com.example.studentmanagement.entity.Student;
import com.example.studentmanagement.security.CustomUserDetailsService;
import com.example.studentmanagement.service.StudentService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
@Import(StudentControllerTest.TestSecurityConfig.class)
class StudentControllerTest {

    @TestConfiguration
    @EnableWebSecurity
    @EnableMethodSecurity(prePostEnabled = true)
    static class TestSecurityConfig {
        @Bean
        @Primary
        @Order(1)
        SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/departments/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/courses/**").hasRole("TEACHER")
                    .requestMatchers(HttpMethod.PUT, "/api/courses/**").hasRole("TEACHER")
                    .requestMatchers(HttpMethod.DELETE, "/api/courses/**").hasRole("TEACHER")
                    .requestMatchers(HttpMethod.GET, "/api/courses/**").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/departments/**").hasRole("TEACHER")
                    .requestMatchers(HttpMethod.PUT, "/api/departments/**").hasRole("TEACHER")
                    .requestMatchers(HttpMethod.DELETE, "/api/departments/**").hasRole("TEACHER")
                    .requestMatchers(HttpMethod.DELETE, "/api/students/{id}").hasRole("TEACHER")
                    .requestMatchers("/api/students/**").authenticated()
                    .requestMatchers("/api/teachers/**").authenticated()
                    .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((request, response, authException) ->
                        response.setStatus(401))
                );
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StudentService studentService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private Student student1;
    private Student student2;

    @BeforeEach
    void setUp() {
        student1 = new Student();
        student1.setId(1L);
        student1.setUsername("student1");
        student1.setEmail("student1@example.com");
        student1.setFullName("Student One");
        student1.setRole(Role.ROLE_STUDENT);
        student1.setStudentId("STU001");
        student1.setPhoneNumber("1234567890");
        student1.setCourses(new HashSet<>());

        student2 = new Student();
        student2.setId(2L);
        student2.setUsername("student2");
        student2.setEmail("student2@example.com");
        student2.setFullName("Student Two");
        student2.setRole(Role.ROLE_STUDENT);
        student2.setStudentId("STU002");
        student2.setPhoneNumber("0987654321");
        student2.setCourses(new HashSet<>());
    }

    @Test
    void getAllStudents() throws Exception {
        when(studentService.getAllStudents()).thenReturn(Arrays.asList(student1, student2));

        mockMvc.perform(get("/api/students").with(user("student").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Students retrieved successfully")))
                .andExpect(jsonPath("$.data", hasSize(2)));

        verify(studentService, times(1)).getAllStudents();
    }

    @Test
    void getStudentById() throws Exception {
        when(studentService.getStudentById(1L)).thenReturn(student1);

        mockMvc.perform(get("/api/students/1").with(user("student").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.fullName", is("Student One")))
                .andExpect(jsonPath("$.data.studentId", is("STU001")));

        verify(studentService, times(1)).getStudentById(1L);
    }

    @Test
    void getStudentById_NotFound() throws Exception {
        when(studentService.getStudentById(99L)).thenThrow(new RuntimeException("Student not found with id: 99"));

        mockMvc.perform(get("/api/students/99").with(user("student").roles("STUDENT")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Student not found")));
    }

    @Test
    void getStudentsByDepartment() throws Exception {
        when(studentService.getStudentsByDepartment(1L)).thenReturn(List.of(student1));

        mockMvc.perform(get("/api/students/department/1").with(user("student").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)));

        verify(studentService, times(1)).getStudentsByDepartment(1L);
    }

    @Test
    void updateStudent() throws Exception {
        Student updatedStudent = new Student();
        updatedStudent.setId(1L);
        updatedStudent.setFullName("Updated Name");
        updatedStudent.setEmail("updated@example.com");
        updatedStudent.setPhoneNumber("1111111111");
        updatedStudent.setCourses(new HashSet<>());

        when(studentService.updateStudent(eq(1L), any(Student.class))).thenReturn(updatedStudent);

        mockMvc.perform(put("/api/students/1")
                        .with(user("student").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedStudent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Student updated successfully")))
                .andExpect(jsonPath("$.data.fullName", is("Updated Name")));

        verify(studentService, times(1)).updateStudent(eq(1L), any(Student.class));
    }

    @Test
    void updateStudent_NotFound() throws Exception {
        when(studentService.updateStudent(eq(99L), any(Student.class)))
                .thenThrow(new RuntimeException("Student not found with id: 99"));

        mockMvc.perform(put("/api/students/99")
                        .with(user("student").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(student1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void assignDepartment() throws Exception {
        Department dept = new Department();
        dept.setId(1L);
        dept.setName("Computer Science");
        student1.setDepartment(dept);

        when(studentService.assignDepartment(1L, 1L)).thenReturn(student1);

        mockMvc.perform(put("/api/students/1/department/1").with(user("student").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Department assigned successfully")));

        verify(studentService, times(1)).assignDepartment(1L, 1L);
    }

    @Test
    void assignDepartment_DepartmentNotFound() throws Exception {
        when(studentService.assignDepartment(1L, 99L))
                .thenThrow(new RuntimeException("Department not found with id: 99"));

        mockMvc.perform(put("/api/students/1/department/99").with(user("student").roles("STUDENT")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void enrollInCourse() throws Exception {
        when(studentService.enrollInCourse(1L, 1L)).thenReturn(student1);

        mockMvc.perform(post("/api/students/1/courses/1").with(user("student").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Enrolled in course successfully")));

        verify(studentService, times(1)).enrollInCourse(1L, 1L);
    }

    @Test
    void enrollInCourse_Failure() throws Exception {
        when(studentService.enrollInCourse(1L, 99L))
                .thenThrow(new RuntimeException("Course not found with id: 99"));

        mockMvc.perform(post("/api/students/1/courses/99").with(user("student").roles("STUDENT")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void dropCourse() throws Exception {
        when(studentService.dropCourse(1L, 1L)).thenReturn(student1);

        mockMvc.perform(delete("/api/students/1/courses/1").with(user("student").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Dropped course successfully")));

        verify(studentService, times(1)).dropCourse(1L, 1L);
    }

    @Test
    void deleteStudent() throws Exception {
        doNothing().when(studentService).deleteStudent(1L);

        mockMvc.perform(delete("/api/students/1").with(user("teacher").roles("TEACHER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Student deleted successfully")));

        verify(studentService, times(1)).deleteStudent(1L);
    }

    @Test
    void deleteStudent_NotFound() throws Exception {
        doThrow(new RuntimeException("Student not found with id: 99")).when(studentService).deleteStudent(99L);

        mockMvc.perform(delete("/api/students/99").with(user("teacher").roles("TEACHER")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void deleteStudent_ForbiddenForStudent() throws Exception {
        mockMvc.perform(delete("/api/students/1").with(user("student").roles("STUDENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllStudents_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isUnauthorized());
    }
}


