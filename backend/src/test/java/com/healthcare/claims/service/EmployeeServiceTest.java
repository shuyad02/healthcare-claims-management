package com.healthcare.claims.service;

import com.healthcare.claims.dto.EmployeeDto;
import com.healthcare.claims.entity.Employee;
import com.healthcare.claims.entity.Role;
import com.healthcare.claims.entity.User;
import com.healthcare.claims.exception.ConflictException;
import com.healthcare.claims.exception.ResourceNotFoundException;
import com.healthcare.claims.repository.EmployeeRepository;
import com.healthcare.claims.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee sampleEmployee;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1L, "alice", "alice@example.com", "encodedPass", Role.ROLE_EMPLOYEE, null);
        sampleEmployee = new Employee(5L, "EMP1005", "Alice Smith", "alice@example.com", "9876543210", "HR", LocalDate.now(), "ACTIVE", sampleUser);
    }

    @Test
    @DisplayName("Test 3: Employee Creation")
    void createEmployee_Success() {
        // Arrange
        EmployeeDto inputDto = new EmployeeDto(null, "EMP1005", "Alice Smith", "alice@example.com", "9876543210", "HR", LocalDate.now(), "ACTIVE", null);

        when(employeeRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(employeeRepository.save(any(Employee.class))).thenReturn(sampleEmployee);

        // Act
        EmployeeDto result = employeeService.createEmployee(inputDto);

        // Assert
        assertNotNull(result);
        assertEquals("Alice Smith", result.getName());
        assertEquals("EMP1005", result.getEmployeeCode());
        assertEquals("alice@example.com", result.getEmail());

        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test 4: Duplicate Employee Creation")
    void createEmployee_DuplicateEmail_ThrowsConflict() {
        // Arrange
        EmployeeDto inputDto = new EmployeeDto(null, "EMP1005", "Alice Smith", "alice@example.com", "9876543210", "HR", LocalDate.now(), "ACTIVE", null);
        when(employeeRepository.existsByEmail("alice@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(ConflictException.class, () -> employeeService.createEmployee(inputDto));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Get Employee By ID - Success")
    void getEmployeeById_Success() {
        // Arrange
        when(employeeRepository.findById(5L)).thenReturn(Optional.of(sampleEmployee));

        // Act
        EmployeeDto result = employeeService.getEmployeeById(5L);

        // Assert
        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals("Alice Smith", result.getName());
    }

    @Test
    @DisplayName("Get Employee By ID - Not Found Exception")
    void getEmployeeById_NotFound_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById(99L));
    }
}
