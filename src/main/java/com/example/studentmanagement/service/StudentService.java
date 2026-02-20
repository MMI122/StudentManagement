package com.example.studentmanagement.service;

import com.example.studentmanagement.entity.Course;
import com.example.studentmanagement.entity.Department;
import com.example.studentmanagement.entity.Student;
import com.example.studentmanagement.repository.CourseRepository;
import com.example.studentmanagement.repository.DepartmentRepository;
import com.example.studentmanagement.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));
    }

    public Student getStudentByUsername(String username) {
        return studentRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Student not found with username: " + username));
    }

    public List<Student> getStudentsByDepartment(Long departmentId) {
        return studentRepository.findByDepartmentId(departmentId);
    }

    @Transactional
    public Student updateStudent(Long id, Student updatedStudent) {
        Student student = getStudentById(id);
        student.setFullName(updatedStudent.getFullName());
        student.setEmail(updatedStudent.getEmail());
        student.setPhoneNumber(updatedStudent.getPhoneNumber());
        return studentRepository.save(student);
    }

    @Transactional
    public Student assignDepartment(Long studentId, Long departmentId) {
        Student student = getStudentById(studentId);
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + departmentId));
        student.setDepartment(department);
        return studentRepository.save(student);
    }

    @Transactional
    public Student enrollInCourse(Long studentId, Long courseId) {
        Student student = getStudentById(studentId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

        // Check if student has a department assigned
        if (student.getDepartment() == null) {
            throw new RuntimeException("Student must be assigned to a department before enrolling in courses");
        }

        // Check if course belongs to student's department
        if (course.getDepartment() == null) {
            throw new RuntimeException("Course is not assigned to any department");
        }

        if (!course.getDepartment().getId().equals(student.getDepartment().getId())) {
            throw new RuntimeException("You can only enroll in courses from your department (" +
                student.getDepartment().getName() + ")");
        }

        student.getCourses().add(course);
        return studentRepository.save(student);
    }

    @Transactional
    public Student dropCourse(Long studentId, Long courseId) {
        Student student = getStudentById(studentId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));
        student.getCourses().remove(course);
        return studentRepository.save(student);
    }

    // Only Teachers can delete students
    @Transactional
    public void deleteStudent(Long id) {
        Student student = getStudentById(id);
        studentRepository.delete(student);
    }
}
