package com.example.studentmanagement.service;

import com.example.studentmanagement.entity.Course;
import com.example.studentmanagement.entity.Department;
import com.example.studentmanagement.entity.Role;
import com.example.studentmanagement.entity.Student;
import com.example.studentmanagement.repository.CourseRepository;
import com.example.studentmanagement.repository.DepartmentRepository;
import com.example.studentmanagement.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private StudentService studentService;

    private Student student1;
    private Student student2;
    private Department department;
    private Course course;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setId(1L);
        department.setName("Computer Science");
        department.setDescription("CS Department");

        student1 = new Student();
        student1.setId(1L);
        student1.setUsername("student1");
        student1.setPassword("encoded_password");
        student1.setEmail("student1@example.com");
        student1.setFullName("Student One");
        student1.setRole(Role.ROLE_STUDENT);
        student1.setStudentId("STU001");
        student1.setPhoneNumber("1234567890");
        student1.setDepartment(department);
        student1.setCourses(new HashSet<>());

        student2 = new Student();
        student2.setId(2L);
        student2.setUsername("student2");
        student2.setPassword("encoded_password");
        student2.setEmail("student2@example.com");
        student2.setFullName("Student Two");
        student2.setRole(Role.ROLE_STUDENT);
        student2.setStudentId("STU002");
        student2.setPhoneNumber("0987654321");

        course = new Course();
        course.setId(1L);
        course.setCourseCode("CS101");
        course.setCourseName("Intro to CS");
        course.setCredits(3);
        course.setDepartment(department);
    }

    @Test
    void getAllStudents() {
        when(studentRepository.findAll()).thenReturn(Arrays.asList(student1, student2));

        List<Student> students = studentService.getAllStudents();

        assertThat(students).hasSize(2);
        verify(studentRepository, times(1)).findAll();
    }

    @Test
    void getAllStudents_EmptyList() {
        when(studentRepository.findAll()).thenReturn(List.of());

        List<Student> students = studentService.getAllStudents();

        assertThat(students).isEmpty();
    }

    @Test
    void getStudentById() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));

        Student found = studentService.getStudentById(1L);

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(1L);
        assertThat(found.getStudentId()).isEqualTo("STU001");
        verify(studentRepository, times(1)).findById(1L);
    }

    @Test
    void getStudentById_NotFound() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.getStudentById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Student not found with id: 99");
    }

    @Test
    void getStudentByUsername() {
        when(studentRepository.findByUsername("student1")).thenReturn(Optional.of(student1));

        Student found = studentService.getStudentByUsername("student1");

        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo("student1");
        verify(studentRepository, times(1)).findByUsername("student1");
    }

    @Test
    void getStudentByUsername_NotFound() {
        when(studentRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.getStudentByUsername("unknown"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Student not found with username: unknown");
    }

    @Test
    void getStudentsByDepartment() {
        when(studentRepository.findByDepartmentId(1L)).thenReturn(List.of(student1));

        List<Student> students = studentService.getStudentsByDepartment(1L);

        assertThat(students).hasSize(1);
        assertThat(students.getFirst().getStudentId()).isEqualTo("STU001");
        verify(studentRepository, times(1)).findByDepartmentId(1L);
    }

    @Test
    void updateStudent() {
        Student updatedData = new Student();
        updatedData.setFullName("Updated Name");
        updatedData.setEmail("updated@example.com");
        updatedData.setPhoneNumber("1111111111");

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Student updated = studentService.updateStudent(1L, updatedData);

        assertThat(updated.getFullName()).isEqualTo("Updated Name");
        assertThat(updated.getEmail()).isEqualTo("updated@example.com");
        assertThat(updated.getPhoneNumber()).isEqualTo("1111111111");
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void updateStudent_NotFound() {
        Student updatedData = new Student();
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.updateStudent(99L, updatedData))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Student not found with id: 99");
    }

    @Test
    void assignDepartment() {
        student1.setDepartment(null); // Start without a department
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Student result = studentService.assignDepartment(1L, 1L);

        assertThat(result.getDepartment()).isNotNull();
        assertThat(result.getDepartment().getName()).isEqualTo("Computer Science");
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void assignDepartment_StudentNotFound() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.assignDepartment(99L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Student not found with id: 99");
    }

    @Test
    void assignDepartment_DepartmentNotFound() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.assignDepartment(1L, 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Department not found with id: 99");
    }

    @Test
    void enrollInCourse() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Student result = studentService.enrollInCourse(1L, 1L);

        assertThat(result.getCourses()).contains(course);
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void enrollInCourse_NoDepartmentAssigned() {
        student1.setDepartment(null);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> studentService.enrollInCourse(1L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Student must be assigned to a department before enrolling in courses");
    }

    @Test
    void enrollInCourse_CourseNoDepartment() {
        course.setDepartment(null);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> studentService.enrollInCourse(1L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Course is not assigned to any department");
    }

    @Test
    void enrollInCourse_DifferentDepartment() {
        Department otherDept = new Department();
        otherDept.setId(2L);
        otherDept.setName("Mathematics");
        course.setDepartment(otherDept);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> studentService.enrollInCourse(1L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("You can only enroll in courses from your department");
    }

    @Test
    void enrollInCourse_CourseNotFound() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.enrollInCourse(1L, 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Course not found with id: 99");
    }

    @Test
    void dropCourse() {
        student1.getCourses().add(course);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Student result = studentService.dropCourse(1L, 1L);

        assertThat(result.getCourses()).doesNotContain(course);
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void dropCourse_CourseNotFound() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.dropCourse(1L, 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Course not found with id: 99");
    }

    @Test
    void deleteStudent() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
        doNothing().when(studentRepository).delete(student1);

        studentService.deleteStudent(1L);

        verify(studentRepository, times(1)).findById(1L);
        verify(studentRepository, times(1)).delete(student1);
    }

    @Test
    void deleteStudent_NotFound() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.deleteStudent(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Student not found with id: 99");
    }
}