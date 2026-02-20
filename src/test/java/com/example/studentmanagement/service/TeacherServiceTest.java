package com.example.studentmanagement.service;

import com.example.studentmanagement.entity.Role;
import com.example.studentmanagement.entity.Teacher;
import com.example.studentmanagement.repository.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @InjectMocks
    private TeacherService teacherService;

    private Teacher teacher1;
    private Teacher teacher2;

    @BeforeEach
    void setUp() {
        teacher1 = new Teacher();
        teacher1.setId(1L);
        teacher1.setUsername("teacher1");
        teacher1.setPassword("encoded_password");
        teacher1.setEmail("teacher1@example.com");
        teacher1.setFullName("Teacher One");
        teacher1.setRole(Role.ROLE_TEACHER);
        teacher1.setEmployeeId("EMP001");
        teacher1.setPhoneNumber("1234567890");
        teacher1.setDesignation("Professor");

        teacher2 = new Teacher();
        teacher2.setId(2L);
        teacher2.setUsername("teacher2");
        teacher2.setPassword("encoded_password");
        teacher2.setEmail("teacher2@example.com");
        teacher2.setFullName("Teacher Two");
        teacher2.setRole(Role.ROLE_TEACHER);
        teacher2.setEmployeeId("EMP002");
        teacher2.setPhoneNumber("0987654321");
        teacher2.setDesignation("Associate Professor");
    }

    @Test
    void getAllTeachers() {
        when(teacherRepository.findAll()).thenReturn(Arrays.asList(teacher1, teacher2));

        List<Teacher> teachers = teacherService.getAllTeachers();

        assertThat(teachers).hasSize(2);
        assertThat(teachers).containsExactly(teacher1, teacher2);
        verify(teacherRepository, times(1)).findAll();
    }

    @Test
    void getAllTeachers_EmptyList() {
        when(teacherRepository.findAll()).thenReturn(List.of());

        List<Teacher> teachers = teacherService.getAllTeachers();

        assertThat(teachers).isEmpty();
        verify(teacherRepository, times(1)).findAll();
    }

    @Test
    void getTeacherById() {
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher1));

        Teacher found = teacherService.getTeacherById(1L);

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(1L);
        assertThat(found.getFullName()).isEqualTo("Teacher One");
        verify(teacherRepository, times(1)).findById(1L);
    }

    @Test
    void getTeacherById_NotFound() {
        when(teacherRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teacherService.getTeacherById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Teacher not found with id: 99");
    }

    @Test
    void getTeacherByUsername() {
        when(teacherRepository.findByUsername("teacher1")).thenReturn(Optional.of(teacher1));

        Teacher found = teacherService.getTeacherByUsername("teacher1");

        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo("teacher1");
        verify(teacherRepository, times(1)).findByUsername("teacher1");
    }

    @Test
    void getTeacherByUsername_NotFound() {
        when(teacherRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teacherService.getTeacherByUsername("unknown"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Teacher not found with username: unknown");
    }

    @Test
    void updateTeacher() {
        Teacher updatedData = new Teacher();
        updatedData.setFullName("Updated Name");
        updatedData.setEmail("updated@example.com");
        updatedData.setPhoneNumber("1111111111");
        updatedData.setDesignation("Senior Professor");

        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher1));
        when(teacherRepository.save(any(Teacher.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Teacher updated = teacherService.updateTeacher(1L, updatedData);

        assertThat(updated.getFullName()).isEqualTo("Updated Name");
        assertThat(updated.getEmail()).isEqualTo("updated@example.com");
        assertThat(updated.getPhoneNumber()).isEqualTo("1111111111");
        assertThat(updated.getDesignation()).isEqualTo("Senior Professor");
        verify(teacherRepository, times(1)).save(any(Teacher.class));
    }

    @Test
    void updateTeacher_NotFound() {
        Teacher updatedData = new Teacher();
        when(teacherRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teacherService.updateTeacher(99L, updatedData))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Teacher not found with id: 99");
    }

    @Test
    void deleteTeacher() {
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher1));
        doNothing().when(teacherRepository).delete(teacher1);

        teacherService.deleteTeacher(1L);

        verify(teacherRepository, times(1)).findById(1L);
        verify(teacherRepository, times(1)).delete(teacher1);
    }

    @Test
    void deleteTeacher_NotFound() {
        when(teacherRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teacherService.deleteTeacher(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Teacher not found with id: 99");
    }
}