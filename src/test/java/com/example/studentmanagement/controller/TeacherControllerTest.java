package com.example.studentmanagement.controller;

import com.example.studentmanagement.entity.Role;
import com.example.studentmanagement.entity.Teacher;
import com.example.studentmanagement.security.CustomUserDetailsService;
import com.example.studentmanagement.service.TeacherService;
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

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TeacherController.class)
@Import(TeacherControllerTest.TestSecurityConfig.class)
class TeacherControllerTest {

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
    private TeacherService teacherService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private Teacher teacher1;
    private Teacher teacher2;

    @BeforeEach
    void setUp() {
        teacher1 = new Teacher();
        teacher1.setId(1L);
        teacher1.setUsername("teacher1");
        teacher1.setEmail("teacher1@example.com");
        teacher1.setFullName("Teacher One");
        teacher1.setRole(Role.ROLE_TEACHER);
        teacher1.setEmployeeId("EMP001");
        teacher1.setPhoneNumber("1234567890");
        teacher1.setDesignation("Professor");

        teacher2 = new Teacher();
        teacher2.setId(2L);
        teacher2.setUsername("teacher2");
        teacher2.setEmail("teacher2@example.com");
        teacher2.setFullName("Teacher Two");
        teacher2.setRole(Role.ROLE_TEACHER);
        teacher2.setEmployeeId("EMP002");
        teacher2.setPhoneNumber("0987654321");
        teacher2.setDesignation("Associate Professor");
    }

    @Test
    void getAllTeachers() throws Exception {
        when(teacherService.getAllTeachers()).thenReturn(Arrays.asList(teacher1, teacher2));

        mockMvc.perform(get("/api/teachers").with(user("teacher").roles("TEACHER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Teachers retrieved successfully")))
                .andExpect(jsonPath("$.data", hasSize(2)));

        verify(teacherService, times(1)).getAllTeachers();
    }

    @Test
    void getTeacherById() throws Exception {
        when(teacherService.getTeacherById(1L)).thenReturn(teacher1);

        mockMvc.perform(get("/api/teachers/1").with(user("teacher").roles("TEACHER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.fullName", is("Teacher One")));

        verify(teacherService, times(1)).getTeacherById(1L);
    }

    @Test
    void getTeacherById_NotFound() throws Exception {
        when(teacherService.getTeacherById(99L)).thenThrow(new RuntimeException("Teacher not found with id: 99"));

        mockMvc.perform(get("/api/teachers/99").with(user("teacher").roles("TEACHER")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Teacher not found")));
    }

    @Test
    void updateTeacher() throws Exception {
        Teacher updatedTeacher = new Teacher();
        updatedTeacher.setId(1L);
        updatedTeacher.setFullName("Updated Name");
        updatedTeacher.setEmail("updated@example.com");
        updatedTeacher.setPhoneNumber("1111111111");
        updatedTeacher.setDesignation("Senior Professor");

        when(teacherService.updateTeacher(eq(1L), any(Teacher.class))).thenReturn(updatedTeacher);

        mockMvc.perform(put("/api/teachers/1")
                        .with(user("teacher").roles("TEACHER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTeacher)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Teacher updated successfully")))
                .andExpect(jsonPath("$.data.fullName", is("Updated Name")));

        verify(teacherService, times(1)).updateTeacher(eq(1L), any(Teacher.class));
    }

    @Test
    void updateTeacher_NotFound() throws Exception {
        when(teacherService.updateTeacher(eq(99L), any(Teacher.class)))
                .thenThrow(new RuntimeException("Teacher not found with id: 99"));

        mockMvc.perform(put("/api/teachers/99")
                        .with(user("teacher").roles("TEACHER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(teacher1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void updateTeacher_ForbiddenForStudent() throws Exception {
        mockMvc.perform(put("/api/teachers/1")
                        .with(user("student").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(teacher1)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteTeacher() throws Exception {
        doNothing().when(teacherService).deleteTeacher(1L);

        mockMvc.perform(delete("/api/teachers/1").with(user("teacher").roles("TEACHER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Teacher deleted successfully")));

        verify(teacherService, times(1)).deleteTeacher(1L);
    }

    @Test
    void deleteTeacher_NotFound() throws Exception {
        doThrow(new RuntimeException("Teacher not found with id: 99")).when(teacherService).deleteTeacher(99L);

        mockMvc.perform(delete("/api/teachers/99").with(user("teacher").roles("TEACHER")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void deleteTeacher_ForbiddenForStudent() throws Exception {
        mockMvc.perform(delete("/api/teachers/1").with(user("student").roles("STUDENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllTeachers_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/teachers"))
                .andExpect(status().isUnauthorized());
    }
}

