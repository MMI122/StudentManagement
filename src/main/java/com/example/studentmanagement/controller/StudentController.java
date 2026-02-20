package com.example.studentmanagement.controller;

import com.example.studentmanagement.dto.ApiResponse;
import com.example.studentmanagement.entity.Student;
import com.example.studentmanagement.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllStudents() {
        List<Student> students = studentService.getAllStudents();
        return ResponseEntity.ok(new ApiResponse(true, "Students retrieved successfully", students));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getStudentById(@PathVariable Long id) {
        try {
            Student student = studentService.getStudentById(id);
            return ResponseEntity.ok(new ApiResponse(true, "Student retrieved successfully", student));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<ApiResponse> getStudentsByDepartment(@PathVariable Long departmentId) {
        List<Student> students = studentService.getStudentsByDepartment(departmentId);
        return ResponseEntity.ok(new ApiResponse(true, "Students retrieved successfully", students));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateStudent(@PathVariable Long id, @RequestBody Student student) {
        try {
            Student updatedStudent = studentService.updateStudent(id, student);
            return ResponseEntity.ok(new ApiResponse(true, "Student updated successfully", updatedStudent));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PutMapping("/{studentId}/department/{departmentId}")
    public ResponseEntity<ApiResponse> assignDepartment(
            @PathVariable Long studentId,
            @PathVariable Long departmentId) {
        try {
            Student student = studentService.assignDepartment(studentId, departmentId);
            return ResponseEntity.ok(new ApiResponse(true, "Department assigned successfully", student));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PostMapping("/{studentId}/courses/{courseId}")
    public ResponseEntity<ApiResponse> enrollInCourse(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        try {
            Student student = studentService.enrollInCourse(studentId, courseId);
            return ResponseEntity.ok(new ApiResponse(true, "Enrolled in course successfully", student));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @DeleteMapping("/{studentId}/courses/{courseId}")
    public ResponseEntity<ApiResponse> dropCourse(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        try {
            Student student = studentService.dropCourse(studentId, courseId);
            return ResponseEntity.ok(new ApiResponse(true, "Dropped course successfully", student));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    // Only TEACHER can delete students - Students CANNOT delete their own account
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse> deleteStudent(@PathVariable Long id) {
        try {
            studentService.deleteStudent(id);
            return ResponseEntity.ok(new ApiResponse(true, "Student deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }
}
