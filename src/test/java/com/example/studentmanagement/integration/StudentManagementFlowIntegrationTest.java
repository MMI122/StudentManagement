package com.example.studentmanagement.integration;

import com.example.studentmanagement.dto.DepartmentDto;
import com.example.studentmanagement.dto.StudentRegistrationDto;
import com.example.studentmanagement.dto.TeacherRegistrationDto;
import com.example.studentmanagement.dto.CourseDto;
import com.example.studentmanagement.repository.CourseRepository;
import com.example.studentmanagement.repository.DepartmentRepository;
import com.example.studentmanagement.repository.StudentRepository;
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
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the full Student Management workflow.
 * Tests the complete flow: register → create department → create course → enroll student.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StudentManagementFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

    @BeforeEach
    void setUp() {
        courseRepository.deleteAll();
        studentRepository.deleteAll();
        departmentRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void fullWorkflow_RegisterCreateAndEnroll() throws Exception {
        // 1. Register a teacher
        TeacherRegistrationDto teacherDto = new TeacherRegistrationDto();
        teacherDto.setUsername("prof_smith");
        teacherDto.setPassword("password123");
        teacherDto.setEmail("smith@university.com");
        teacherDto.setFullName("Prof Smith");
        teacherDto.setEmployeeId("EMP001");
        teacherDto.setDesignation("Professor");

        mockMvc.perform(post("/api/auth/register/teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(teacherDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)));

        // 2. Register a student
        StudentRegistrationDto studentDto = new StudentRegistrationDto();
        studentDto.setUsername("john_doe");
        studentDto.setPassword("password123");
        studentDto.setEmail("john@university.com");
        studentDto.setFullName("John Doe");
        studentDto.setStudentId("STU001");

        mockMvc.perform(post("/api/auth/register/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)));

        // 3. Teacher creates a department
        DepartmentDto deptDto = new DepartmentDto();
        deptDto.setName("Computer Science");
        deptDto.setDescription("CS Department");

        MvcResult deptResult = mockMvc.perform(post("/api/departments")
                        .with(user("prof_smith").roles("TEACHER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deptDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is("Computer Science")))
                .andReturn();

        // Extract department ID from response
        String deptResponseBody = deptResult.getResponse().getContentAsString();
        Long departmentId = objectMapper.readTree(deptResponseBody).get("data").get("id").asLong();

        // 4. Anyone can GET departments (public)
        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name", is("Computer Science")));

        // 5. Teacher creates a course
        CourseDto courseDto = new CourseDto();
        courseDto.setCourseCode("CS101");
        courseDto.setCourseName("Intro to Programming");
        courseDto.setDescription("Learn basics of programming");
        courseDto.setCredits(3);
        courseDto.setDepartmentId(departmentId);

        MvcResult courseResult = mockMvc.perform(post("/api/courses")
                        .with(user("prof_smith").roles("TEACHER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.courseCode", is("CS101")))
                .andReturn();

        String courseResponseBody = courseResult.getResponse().getContentAsString();
        Long courseId = objectMapper.readTree(courseResponseBody).get("data").get("id").asLong();

        // 6. Student can view courses
        mockMvc.perform(get("/api/courses")
                        .with(user("john_doe").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));

        // 7. Get student ID
        MvcResult studentsResult = mockMvc.perform(get("/api/students")
                        .with(user("john_doe").roles("STUDENT")))
                .andExpect(status().isOk())
                .andReturn();

        String studentsBody = studentsResult.getResponse().getContentAsString();
        Long studentId = objectMapper.readTree(studentsBody).get("data").get(0).get("id").asLong();

        // 8. Student assigns to a department
        mockMvc.perform(put("/api/students/" + studentId + "/department/" + departmentId)
                        .with(user("john_doe").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Department assigned successfully")));

        // 9. Student enrolls in a course
        mockMvc.perform(post("/api/students/" + studentId + "/courses/" + courseId)
                        .with(user("john_doe").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Enrolled in course successfully")));

        // 10. Student drops the course
        mockMvc.perform(delete("/api/students/" + studentId + "/courses/" + courseId)
                        .with(user("john_doe").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Dropped course successfully")));
    }

    @Test
    void securityRules_StudentCannotCreateCourse() throws Exception {
        // Register student first
        StudentRegistrationDto studentDto = new StudentRegistrationDto();
        studentDto.setUsername("hacker_student");
        studentDto.setPassword("password123");
        studentDto.setEmail("hacker@test.com");
        studentDto.setFullName("Hacker Student");
        studentDto.setStudentId("STU999");

        mockMvc.perform(post("/api/auth/register/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentDto)))
                .andExpect(status().isCreated());

        // Student tries to create a course — should be forbidden
        CourseDto courseDto = new CourseDto();
        courseDto.setCourseCode("HACK101");
        courseDto.setCourseName("Hacking 101");
        courseDto.setCredits(3);

        mockMvc.perform(post("/api/courses")
                        .with(user("hacker_student").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void securityRules_StudentCannotDeleteStudent() throws Exception {
        // Register a student
        StudentRegistrationDto dto = new StudentRegistrationDto();
        dto.setUsername("student_a");
        dto.setPassword("password123");
        dto.setEmail("a@test.com");
        dto.setFullName("Student A");
        dto.setStudentId("STU_A");

        mockMvc.perform(post("/api/auth/register/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        // Student tries to delete another student — should be forbidden
        mockMvc.perform(delete("/api/students/1")
                        .with(user("student_a").roles("STUDENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void securityRules_UnauthenticatedCannotAccessStudents() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void teacherCRUD_FullLifecycle() throws Exception {
        // Register teacher
        TeacherRegistrationDto dto = new TeacherRegistrationDto();
        dto.setUsername("teacher_crud");
        dto.setPassword("password123");
        dto.setEmail("crud@test.com");
        dto.setFullName("CRUD Teacher");
        dto.setEmployeeId("EMP_CRUD");
        dto.setDesignation("Lecturer");

        mockMvc.perform(post("/api/auth/register/teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        // Get all teachers
        MvcResult result = mockMvc.perform(get("/api/teachers")
                        .with(user("teacher_crud").roles("TEACHER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        Long teacherId = objectMapper.readTree(body).get("data").get(0).get("id").asLong();

        // Get teacher by ID
        mockMvc.perform(get("/api/teachers/" + teacherId)
                        .with(user("teacher_crud").roles("TEACHER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName", is("CRUD Teacher")));

        // Delete teacher
        mockMvc.perform(delete("/api/teachers/" + teacherId)
                        .with(user("teacher_crud").roles("TEACHER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    void departmentCRUD_FullLifecycle() throws Exception {
        // Create
        DepartmentDto dto = new DepartmentDto();
        dto.setName("Mathematics");
        dto.setDescription("Math Dept");

        MvcResult createResult = mockMvc.perform(post("/api/departments")
                        .with(user("admin").roles("TEACHER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        Long deptId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("data").get("id").asLong();

        // Read
        mockMvc.perform(get("/api/departments/" + deptId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name", is("Mathematics")));

        // Update
        DepartmentDto updateDto = new DepartmentDto();
        updateDto.setName("Applied Mathematics");
        updateDto.setDescription("Applied Math Dept");

        mockMvc.perform(put("/api/departments/" + deptId)
                        .with(user("admin").roles("TEACHER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name", is("Applied Mathematics")));

        // Delete
        mockMvc.perform(delete("/api/departments/" + deptId)
                        .with(user("admin").roles("TEACHER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // Verify deleted
        mockMvc.perform(get("/api/departments/" + deptId))
                .andExpect(status().isBadRequest());
    }
}


