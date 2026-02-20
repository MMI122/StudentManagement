package com.example.studentmanagement.controller;

import com.example.studentmanagement.dto.ApiResponse;
import com.example.studentmanagement.dto.StudentRegistrationDto;
import com.example.studentmanagement.dto.TeacherRegistrationDto;
import com.example.studentmanagement.entity.Student;
import com.example.studentmanagement.entity.Teacher;
import com.example.studentmanagement.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/student")
    public ResponseEntity<ApiResponse> registerStudent(@Valid @RequestBody StudentRegistrationDto dto) {
        try {
            Student student = authService.registerStudent(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Student registered successfully", student));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PostMapping("/register/teacher")
    public ResponseEntity<ApiResponse> registerTeacher(@Valid @RequestBody TeacherRegistrationDto dto) {
        try {
            Teacher teacher = authService.registerTeacher(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Teacher registered successfully", teacher));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/login")
    public ResponseEntity<ApiResponse> login() {
        // This endpoint will be protected by HTTP Basic Auth
        // If user reaches here, they are authenticated
        return ResponseEntity.ok(new ApiResponse(true, "Login successful"));
    }
}
