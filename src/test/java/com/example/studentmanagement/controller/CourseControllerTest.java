package com.example.studentmanagement.controller;

import com.example.studentmanagement.dto.CourseDto;
import com.example.studentmanagement.entity.Course;
import com.example.studentmanagement.entity.Department;
import com.example.studentmanagement.entity.Role;
import com.example.studentmanagement.entity.Teacher;
import com.example.studentmanagement.security.CustomUserDetailsService;
import com.example.studentmanagement.service.CourseService;
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

@WebMvcTest(CourseController.class)
@Import(CourseControllerTest.TestSecurityConfig.class)
class CourseControllerTest {

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
    private CourseService courseService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private Course course1;
    private Course course2;
    private CourseDto courseDto;

    @BeforeEach
    void setUp() {
        Teacher teacher = new Teacher();
        teacher.setId(1L);
        teacher.setUsername("teacher1");
        teacher.setFullName("Teacher One");
        teacher.setRole(Role.ROLE_TEACHER);
        teacher.setEmployeeId("EMP001");

        Department department = new Department();
        department.setId(1L);
        department.setName("Computer Science");

        course1 = new Course();
        course1.setId(1L);
        course1.setCourseCode("CS101");
        course1.setCourseName("Intro to CS");
        course1.setDescription("Introduction to Computer Science");
        course1.setCredits(3);
        course1.setTeacher(teacher);
        course1.setDepartment(department);

        course2 = new Course();
        course2.setId(2L);
        course2.setCourseCode("CS201");
        course2.setCourseName("Data Structures");
        course2.setDescription("Data Structures and Algorithms");
        course2.setCredits(4);
        course2.setTeacher(teacher);

        courseDto = new CourseDto();
        courseDto.setCourseCode("CS301");
        courseDto.setCourseName("Algorithms");
        courseDto.setDescription("Algorithm Design");
        courseDto.setCredits(3);
        courseDto.setDepartmentId(1L);
    }

    @Test
    void getAllCourses() throws Exception {
        when(courseService.getAllCourses()).thenReturn(Arrays.asList(course1, course2));

        mockMvc.perform(get("/api/courses").with(user("student").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Courses retrieved successfully")))
                .andExpect(jsonPath("$.data", hasSize(2)));

        verify(courseService, times(1)).getAllCourses();
    }

    @Test
    void getCourseById() throws Exception {
        when(courseService.getCourseById(1L)).thenReturn(course1);

        mockMvc.perform(get("/api/courses/1").with(user("student").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.courseCode", is("CS101")))
                .andExpect(jsonPath("$.data.courseName", is("Intro to CS")));

        verify(courseService, times(1)).getCourseById(1L);
    }

    @Test
    void getCourseById_NotFound() throws Exception {
        when(courseService.getCourseById(99L)).thenThrow(new RuntimeException("Course not found with id: 99"));

        mockMvc.perform(get("/api/courses/99").with(user("student").roles("STUDENT")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Course not found")));
    }

    @Test
    void getCoursesByTeacher() throws Exception {
        when(courseService.getCoursesByTeacher(1L)).thenReturn(Arrays.asList(course1, course2));

        mockMvc.perform(get("/api/courses/teacher/1").with(user("student").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(2)));

        verify(courseService, times(1)).getCoursesByTeacher(1L);
    }

    @Test
    void createCourse() throws Exception {
        when(courseService.createCourse(any(CourseDto.class))).thenReturn(course1);

        mockMvc.perform(post("/api/courses")
                        .with(user("teacher").roles("TEACHER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Course created successfully")));

        verify(courseService, times(1)).createCourse(any(CourseDto.class));
    }

    @Test
    void createCourse_DuplicateCode() throws Exception {
        when(courseService.createCourse(any(CourseDto.class)))
                .thenThrow(new RuntimeException("Course with this code already exists"));

        mockMvc.perform(post("/api/courses")
                        .with(user("teacher").roles("TEACHER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    void createCourse_ValidationError() throws Exception {
        CourseDto invalidDto = new CourseDto();

        mockMvc.perform(post("/api/courses")
                        .with(user("teacher").roles("TEACHER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCourse_ForbiddenForStudent() throws Exception {
        mockMvc.perform(post("/api/courses")
                        .with(user("student").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateCourse() throws Exception {
        course1.setCourseName("Updated Course");
        when(courseService.updateCourse(eq(1L), any(CourseDto.class))).thenReturn(course1);

        mockMvc.perform(put("/api/courses/1")
                        .with(user("teacher").roles("TEACHER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Course updated successfully")));

        verify(courseService, times(1)).updateCourse(eq(1L), any(CourseDto.class));
    }

    @Test
    void updateCourse_NotFound() throws Exception {
        when(courseService.updateCourse(eq(99L), any(CourseDto.class)))
                .thenThrow(new RuntimeException("Course not found with id: 99"));

        mockMvc.perform(put("/api/courses/99")
                        .with(user("teacher").roles("TEACHER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void updateCourse_ForbiddenForStudent() throws Exception {
        mockMvc.perform(put("/api/courses/1")
                        .with(user("student").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteCourse() throws Exception {
        doNothing().when(courseService).deleteCourse(1L);

        mockMvc.perform(delete("/api/courses/1").with(user("teacher").roles("TEACHER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Course deleted successfully")));

        verify(courseService, times(1)).deleteCourse(1L);
    }

    @Test
    void deleteCourse_NotFound() throws Exception {
        doThrow(new RuntimeException("Course not found with id: 99")).when(courseService).deleteCourse(99L);

        mockMvc.perform(delete("/api/courses/99").with(user("teacher").roles("TEACHER")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void deleteCourse_ForbiddenForStudent() throws Exception {
        mockMvc.perform(delete("/api/courses/1").with(user("student").roles("STUDENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllCourses_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isUnauthorized());
    }
}

