package com.example.studentmanagement.service;

import com.example.studentmanagement.dto.DepartmentDto;
import com.example.studentmanagement.entity.Department;
import com.example.studentmanagement.repository.DepartmentRepository;
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
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentService departmentService;

    private Department department1;
    private Department department2;

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
    }

    @Test
    void getAllDepartments() {
        when(departmentRepository.findAll()).thenReturn(Arrays.asList(department1, department2));

        List<Department> departments = departmentService.getAllDepartments();

        assertThat(departments).hasSize(2);
        assertThat(departments).containsExactly(department1, department2);
        verify(departmentRepository, times(1)).findAll();
    }

    @Test
    void getAllDepartments_EmptyList() {
        when(departmentRepository.findAll()).thenReturn(List.of());

        List<Department> departments = departmentService.getAllDepartments();

        assertThat(departments).isEmpty();
    }

    @Test
    void getDepartmentById() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department1));

        Department found = departmentService.getDepartmentById(1L);

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(1L);
        assertThat(found.getName()).isEqualTo("Computer Science");
        verify(departmentRepository, times(1)).findById(1L);
    }

    @Test
    void getDepartmentById_NotFound() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.getDepartmentById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Department not found with id: 99");
    }

    @Test
    void getDepartmentByName() {
        when(departmentRepository.findByName("Computer Science")).thenReturn(Optional.of(department1));

        Department found = departmentService.getDepartmentByName("Computer Science");

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Computer Science");
        verify(departmentRepository, times(1)).findByName("Computer Science");
    }

    @Test
    void getDepartmentByName_NotFound() {
        when(departmentRepository.findByName("Unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.getDepartmentByName("Unknown"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Department not found with name: Unknown");
    }

    @Test
    void createDepartment() {
        DepartmentDto dto = new DepartmentDto();
        dto.setName("Physics");
        dto.setDescription("Physics Department");

        when(departmentRepository.existsByName("Physics")).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> {
            Department saved = invocation.getArgument(0);
            saved.setId(3L);
            return saved;
        });

        Department created = departmentService.createDepartment(dto);

        assertThat(created).isNotNull();
        assertThat(created.getName()).isEqualTo("Physics");
        assertThat(created.getDescription()).isEqualTo("Physics Department");
        verify(departmentRepository, times(1)).save(any(Department.class));
    }

    @Test
    void createDepartment_DuplicateName() {
        DepartmentDto dto = new DepartmentDto();
        dto.setName("Computer Science");

        when(departmentRepository.existsByName("Computer Science")).thenReturn(true);

        assertThatThrownBy(() -> departmentService.createDepartment(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Department with this name already exists");

        verify(departmentRepository, never()).save(any());
    }

    @Test
    void updateDepartment() {
        DepartmentDto dto = new DepartmentDto();
        dto.setName("Updated CS");
        dto.setDescription("Updated Description");

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department1));
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Department updated = departmentService.updateDepartment(1L, dto);

        assertThat(updated.getName()).isEqualTo("Updated CS");
        assertThat(updated.getDescription()).isEqualTo("Updated Description");
        verify(departmentRepository, times(1)).save(any(Department.class));
    }

    @Test
    void updateDepartment_NotFound() {
        DepartmentDto dto = new DepartmentDto();
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.updateDepartment(99L, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Department not found with id: 99");
    }

    @Test
    void deleteDepartment() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department1));
        doNothing().when(departmentRepository).delete(department1);

        departmentService.deleteDepartment(1L);

        verify(departmentRepository, times(1)).findById(1L);
        verify(departmentRepository, times(1)).delete(department1);
    }

    @Test
    void deleteDepartment_NotFound() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.deleteDepartment(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Department not found with id: 99");
    }
}