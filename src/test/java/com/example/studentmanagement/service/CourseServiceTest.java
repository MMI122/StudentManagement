package com.example.studentmanagement.service;

import com.example.studentmanagement.dto.CourseDto;
import com.example.studentmanagement.entity.Course;
import com.example.studentmanagement.entity.Department;
import com.example.studentmanagement.entity.Role;
import com.example.studentmanagement.entity.Teacher;
import com.example.studentmanagement.repository.CourseRepository;
import com.example.studentmanagement.repository.DepartmentRepository;
import com.example.studentmanagement.repository.TeacherRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private CourseService courseService;

    private Course course1;
    private Course course2;
    private Teacher teacher;
    private Department department;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setId(1L);
        department.setName("Computer Science");
        department.setDescription("CS Department");

        teacher = new Teacher();
        teacher.setId(1L);
        teacher.setUsername("teacher1");
        teacher.setPassword("encoded_password");
        teacher.setEmail("teacher1@example.com");
        teacher.setFullName("Teacher One");
        teacher.setRole(Role.ROLE_TEACHER);
        teacher.setEmployeeId("EMP001");

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
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllCourses() {
        when(courseRepository.findAll()).thenReturn(Arrays.asList(course1, course2));

        List<Course> courses = courseService.getAllCourses();

        assertThat(courses).hasSize(2);
        assertThat(courses).containsExactly(course1, course2);
        verify(courseRepository, times(1)).findAll();
    }

    @Test
    void getAllCourses_EmptyList() {
        when(courseRepository.findAll()).thenReturn(List.of());

        List<Course> courses = courseService.getAllCourses();

        assertThat(courses).isEmpty();
    }

    @Test
    void getCourseById() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course1));

        Course found = courseService.getCourseById(1L);

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(1L);
        assertThat(found.getCourseCode()).isEqualTo("CS101");
        verify(courseRepository, times(1)).findById(1L);
    }

    @Test
    void getCourseById_NotFound() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getCourseById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Course not found with id: 99");
    }

    @Test
    void getCourseByCourseCode() {
        when(courseRepository.findByCourseCode("CS101")).thenReturn(Optional.of(course1));

        Course found = courseService.getCourseByCourseCode("CS101");

        assertThat(found).isNotNull();
        assertThat(found.getCourseCode()).isEqualTo("CS101");
        verify(courseRepository, times(1)).findByCourseCode("CS101");
    }

    @Test
    void getCourseByCourseCode_NotFound() {
        when(courseRepository.findByCourseCode("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getCourseByCourseCode("UNKNOWN"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Course not found with code: UNKNOWN");
    }

    @Test
    void getCoursesByTeacher() {
        when(courseRepository.findByTeacherId(1L)).thenReturn(Arrays.asList(course1, course2));

        List<Course> courses = courseService.getCoursesByTeacher(1L);

        assertThat(courses).hasSize(2);
        verify(courseRepository, times(1)).findByTeacherId(1L);
    }

    @Test
    void createCourse() {
        // Set up SecurityContext
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("teacher1", "password", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        CourseDto dto = new CourseDto();
        dto.setCourseCode("CS301");
        dto.setCourseName("Algorithms");
        dto.setDescription("Algorithm Design");
        dto.setCredits(3);
        dto.setDepartmentId(1L);

        when(courseRepository.existsByCourseCode("CS301")).thenReturn(false);
        when(teacherRepository.findByUsername("teacher1")).thenReturn(Optional.of(teacher));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
            Course saved = invocation.getArgument(0);
            saved.setId(3L);
            return saved;
        });

        Course created = courseService.createCourse(dto);

        assertThat(created).isNotNull();
        assertThat(created.getCourseCode()).isEqualTo("CS301");
        assertThat(created.getCourseName()).isEqualTo("Algorithms");
        assertThat(created.getTeacher()).isEqualTo(teacher);
        assertThat(created.getDepartment()).isEqualTo(department);
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    void createCourse_WithoutDepartment() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("teacher1", "password", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        CourseDto dto = new CourseDto();
        dto.setCourseCode("CS301");
        dto.setCourseName("Algorithms");
        dto.setDescription("Algorithm Design");
        dto.setCredits(3);
        // No departmentId set

        when(courseRepository.existsByCourseCode("CS301")).thenReturn(false);
        when(teacherRepository.findByUsername("teacher1")).thenReturn(Optional.of(teacher));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Course created = courseService.createCourse(dto);

        assertThat(created).isNotNull();
        assertThat(created.getDepartment()).isNull();
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    void createCourse_DuplicateCode() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("teacher1", "password", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        CourseDto dto = new CourseDto();
        dto.setCourseCode("CS101");

        when(courseRepository.existsByCourseCode("CS101")).thenReturn(true);

        assertThatThrownBy(() -> courseService.createCourse(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Course with this code already exists");

        verify(courseRepository, never()).save(any());
    }

    @Test
    void createCourse_TeacherNotFound() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("unknown", "password", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        CourseDto dto = new CourseDto();
        dto.setCourseCode("CS301");
        dto.setCourseName("Algorithms");
        dto.setCredits(3);

        when(courseRepository.existsByCourseCode("CS301")).thenReturn(false);
        when(teacherRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.createCourse(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Teacher not found");
    }

    @Test
    void createCourse_DepartmentNotFound() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("teacher1", "password", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        CourseDto dto = new CourseDto();
        dto.setCourseCode("CS301");
        dto.setCourseName("Algorithms");
        dto.setCredits(3);
        dto.setDepartmentId(99L);

        when(courseRepository.existsByCourseCode("CS301")).thenReturn(false);
        when(teacherRepository.findByUsername("teacher1")).thenReturn(Optional.of(teacher));
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.createCourse(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Department not found");
    }

    @Test
    void updateCourse() {
        CourseDto dto = new CourseDto();
        dto.setCourseName("Updated Course");
        dto.setDescription("Updated Description");
        dto.setCredits(5);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course1));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Course updated = courseService.updateCourse(1L, dto);

        assertThat(updated.getCourseName()).isEqualTo("Updated Course");
        assertThat(updated.getDescription()).isEqualTo("Updated Description");
        assertThat(updated.getCredits()).isEqualTo(5);
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    void updateCourse_NotFound() {
        CourseDto dto = new CourseDto();
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.updateCourse(99L, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Course not found with id: 99");
    }

    @Test
    void deleteCourse() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course1));
        doNothing().when(courseRepository).delete(course1);

        courseService.deleteCourse(1L);

        verify(courseRepository, times(1)).findById(1L);
        verify(courseRepository, times(1)).delete(course1);
    }

    @Test
    void deleteCourse_NotFound() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.deleteCourse(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Course not found with id: 99");
    }
}