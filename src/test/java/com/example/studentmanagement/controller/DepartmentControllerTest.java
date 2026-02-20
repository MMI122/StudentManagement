package com.example.studentmanagement.controller;

import com.example.studentmanagement.dto.DepartmentDto;
import com.example.studentmanagement.entity.Department;
import com.example.studentmanagement.security.CustomUserDetailsService;
import com.example.studentmanagement.service.DepartmentService;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepartmentController.class)
@Import(DepartmentControllerTest.TestSecurityConfig.class)
class DepartmentControllerTest {

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
    private DepartmentService departmentService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private Department department1;
    private Department department2;
    private DepartmentDto departmentDto;

    @BeforeEach
    void setUp() {
        department1 = new Department();
        department1.setId(1L);
        department1.setName("Computer Science");
        department1.setDescription("CS Department");

        department2 = new Department();
        department2.setId(2L);
        department2.setName("Mathematics");
        department2.setDescription("Math Department");

        departmentDto = new DepartmentDto();
        departmentDto.setName("Physics");
        departmentDto.setDescription("Physics Department");
    }

    @Test
    void getAllDepartments() throws Exception {
        when(departmentService.getAllDepartments()).thenReturn(Arrays.asList(department1, department2));

        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Departments retrieved successfully")))
                .andExpect(jsonPath("$.data", hasSize(2)));

        verify(departmentService, times(1)).getAllDepartments();
    }

    @Test
    void getDepartmentById() throws Exception {
        when(departmentService.getDepartmentById(1L)).thenReturn(department1);

        mockMvc.perform(get("/api/departments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is("Computer Science")));

        verify(departmentService, times(1)).getDepartmentById(1L);
    }

    @Test
    void getDepartmentById_NotFound() throws Exception {
        when(departmentService.getDepartmentById(99L))
                .thenThrow(new RuntimeException("Department not found with id: 99"));

        mockMvc.perform(get("/api/departments/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Department not found")));
    }

    @Test
    void createDepartment() throws Exception {
        when(departmentService.createDepartment(any(DepartmentDto.class))).thenReturn(department1);

        mockMvc.perform(post("/api/departments")
                        .with(user("teacher").roles("TEACHER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(departmentDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Department created successfully")));

        verify(departmentService, times(1)).createDepartment(any(DepartmentDto.class));
    }

    @Test
    void createDepartment_DuplicateName() throws Exception {
        when(departmentService.createDepartment(any(DepartmentDto.class)))
                .thenThrow(new RuntimeException("Department with this name already exists"));

        mockMvc.perform(post("/api/departments")
                        .with(user("teacher").roles("TEACHER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(departmentDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    void createDepartment_ValidationError() throws Exception {
        DepartmentDto invalidDto = new DepartmentDto();

        mockMvc.perform(post("/api/departments")
                        .with(user("teacher").roles("TEACHER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createDepartment_ForbiddenForStudent() throws Exception {
        mockMvc.perform(post("/api/departments")
                        .with(user("student").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(departmentDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createDepartment_Unauthenticated() throws Exception {
        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(departmentDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateDepartment() throws Exception {
        department1.setName("Updated CS");
        when(departmentService.updateDepartment(eq(1L), any(DepartmentDto.class))).thenReturn(department1);

        mockMvc.perform(put("/api/departments/1")
                        .with(user("teacher").roles("TEACHER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(departmentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Department updated successfully")));

        verify(departmentService, times(1)).updateDepartment(eq(1L), any(DepartmentDto.class));
    }

    @Test
    void updateDepartment_NotFound() throws Exception {
        when(departmentService.updateDepartment(eq(99L), any(DepartmentDto.class)))
                .thenThrow(new RuntimeException("Department not found with id: 99"));

        mockMvc.perform(put("/api/departments/99")
                        .with(user("teacher").roles("TEACHER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(departmentDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void updateDepartment_ForbiddenForStudent() throws Exception {
        mockMvc.perform(put("/api/departments/1")
                        .with(user("student").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(departmentDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteDepartment() throws Exception {
        doNothing().when(departmentService).deleteDepartment(1L);

        mockMvc.perform(delete("/api/departments/1").with(user("teacher").roles("TEACHER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Department deleted successfully")));

        verify(departmentService, times(1)).deleteDepartment(1L);
    }

    @Test
    void deleteDepartment_NotFound() throws Exception {
        doThrow(new RuntimeException("Department not found with id: 99"))
                .when(departmentService).deleteDepartment(99L);

        mockMvc.perform(delete("/api/departments/99").with(user("teacher").roles("TEACHER")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void deleteDepartment_ForbiddenForStudent() throws Exception {
        mockMvc.perform(delete("/api/departments/1").with(user("student").roles("STUDENT")))
                .andExpect(status().isForbidden());
    }
}