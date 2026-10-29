package com.healthcare.claims.service;

import com.healthcare.claims.dto.AuthRequest;
import com.healthcare.claims.dto.AuthResponse;
import com.healthcare.claims.dto.RegisterRequest;
import com.healthcare.claims.entity.Employee;
import com.healthcare.claims.entity.Role;
import com.healthcare.claims.entity.User;
import com.healthcare.claims.exception.BadRequestException;
import com.healthcare.claims.exception.ConflictException;
import com.healthcare.claims.repository.EmployeePlanRepository;
import com.healthcare.claims.repository.EmployeeRepository;
import com.healthcare.claims.repository.HealthcarePlanRepository;
import com.healthcare.claims.repository.UserRepository;
import com.healthcare.claims.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private HealthcarePlanRepository planRepository;

    @Mock
    private EmployeePlanRepository employeePlanRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User sampleUser;
    private Employee sampleEmployee;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1L, "john_doe", "john@example.com", "encodedPassword", Role.ROLE_EMPLOYEE, null);
        sampleEmployee = new Employee(10L, "EMP1001", "John Doe", "john@example.com", "9876543210", "IT", LocalDate.now(), "ACTIVE", sampleUser);
    }

    @Test
    @DisplayName("Test 1: Successful Login")
    void login_Success() {
        // Arrange
        AuthRequest request = new AuthRequest("john_doe", "password123");
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt_mock_token");
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(sampleUser));
        when(employeeRepository.findByUserId(1L)).thenReturn(Optional.of(sampleEmployee));

        // Act
        AuthResponse response = authenticationService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("jwt_mock_token", response.getToken());
        assertEquals("john_doe", response.getUsername());
        assertEquals(Role.ROLE_EMPLOYEE, response.getRole());
        assertEquals(10L, response.getEmployeeId());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenProvider).generateToken(authentication);
    }

    @Test
    @DisplayName("Test 2: Invalid Login - Bad Credentials")
    void login_InvalidCredentials_ThrowsException() {
        // Arrange
        AuthRequest request = new AuthRequest("john_doe", "wrongpassword");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authenticationService.login(request));
        verify(tokenProvider, never()).generateToken(any());
    }

    @Test
    @DisplayName("Successful Registration")
    void register_Success() {
        // Arrange
        RegisterRequest request = new RegisterRequest("john_doe", "john@example.com", "password123", Role.ROLE_EMPLOYEE, "John Doe", "9876543210", "IT", LocalDate.now());

        when(userRepository.existsByUsername("john_doe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(employeeRepository.save(any(Employee.class))).thenReturn(sampleEmployee);
        when(planRepository.findByActive(true)).thenReturn(Collections.emptyList());
        when(tokenProvider.generateTokenFromUsername(anyString(), anyString(), anyLong())).thenReturn("jwt_token");

        // Act
        AuthResponse response = authenticationService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals("john_doe", response.getUsername());
        assertEquals("jwt_token", response.getToken());
    }

    @Test
    @DisplayName("Registration - Duplicate Username Exception")
    void register_DuplicateUsername_ThrowsConflict() {
        // Arrange
        RegisterRequest request = new RegisterRequest("john_doe", "john@example.com", "password123", Role.ROLE_EMPLOYEE, "John Doe", "9876543210", "IT", LocalDate.now());
        when(userRepository.existsByUsername("john_doe")).thenReturn(true);

        // Act & Assert
        assertThrows(ConflictException.class, () -> authenticationService.register(request));
    }
}
