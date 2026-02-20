package com.example.studentmanagement.service;

import com.example.studentmanagement.entity.Teacher;
import com.example.studentmanagement.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;

    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll();
    }

    public Teacher getTeacherById(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + id));
    }

    public Teacher getTeacherByUsername(String username) {
        return teacherRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Teacher not found with username: " + username));
    }

    @Transactional
    public Teacher updateTeacher(Long id, Teacher updatedTeacher) {
        Teacher teacher = getTeacherById(id);
        teacher.setFullName(updatedTeacher.getFullName());
        teacher.setEmail(updatedTeacher.getEmail());
        teacher.setPhoneNumber(updatedTeacher.getPhoneNumber());
        teacher.setDesignation(updatedTeacher.getDesignation());
        return teacherRepository.save(teacher);
    }

    @Transactional
    public void deleteTeacher(Long id) {
        Teacher teacher = getTeacherById(id);
        teacherRepository.delete(teacher);
    }
}
