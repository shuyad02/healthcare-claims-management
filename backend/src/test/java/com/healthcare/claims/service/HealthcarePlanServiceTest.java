package com.healthcare.claims.service;

import com.healthcare.claims.dto.HealthcarePlanDto;
import com.healthcare.claims.entity.HealthcarePlan;
import com.healthcare.claims.exception.BadRequestException;
import com.healthcare.claims.exception.ConflictException;
import com.healthcare.claims.exception.ResourceNotFoundException;
import com.healthcare.claims.repository.EmployeePlanRepository;
import com.healthcare.claims.repository.EmployeeRepository;
import com.healthcare.claims.repository.HealthcarePlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthcarePlanServiceTest {

    @Mock
    private HealthcarePlanRepository planRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeePlanRepository employeePlanRepository;

    @InjectMocks
    private HealthcarePlanService planService;

    private HealthcarePlan samplePlan;

    @BeforeEach
    void setUp() {
        samplePlan = new HealthcarePlan(1L, "PLAN-GOLD-01", "Gold Plan", "Comprehensive coverage", new BigDecimal("50000.00"), new BigDecimal("200.00"), "Full Time", true);
    }

    @Test
    @DisplayName("Test 5: Healthcare Plan Creation")
    void createPlan_Success() {
        // Arrange
        HealthcarePlanDto dto = new HealthcarePlanDto(null, "PLAN-GOLD-01", "Gold Plan", "Comprehensive coverage", new BigDecimal("50000.00"), new BigDecimal("200.00"), "Full Time", true);

        when(planRepository.existsByPlanCode("PLAN-GOLD-01")).thenReturn(false);
        when(planRepository.save(any(HealthcarePlan.class))).thenReturn(samplePlan);

        // Act
        HealthcarePlanDto result = planService.createPlan(dto);

        // Assert
        assertNotNull(result);
        assertEquals("PLAN-GOLD-01", result.getPlanCode());
        assertEquals("Gold Plan", result.getPlanName());
        assertEquals(new BigDecimal("50000.00"), result.getCoverageAmount());

        verify(planRepository).save(any(HealthcarePlan.class));
    }

    @Test
    @DisplayName("Healthcare Plan Creation - Duplicate Plan Code Exception")
    void createPlan_DuplicateCode_ThrowsConflict() {
        // Arrange
        HealthcarePlanDto dto = new HealthcarePlanDto(null, "PLAN-GOLD-01", "Gold Plan", "Comprehensive coverage", new BigDecimal("50000.00"), new BigDecimal("200.00"), "Full Time", true);
        when(planRepository.existsByPlanCode("PLAN-GOLD-01")).thenReturn(true);

        // Act & Assert
        assertThrows(ConflictException.class, () -> planService.createPlan(dto));
    }

    @Test
    @DisplayName("Healthcare Plan Creation - Invalid Coverage Amount Exception")
    void createPlan_InvalidCoverage_ThrowsBadRequest() {
        // Arrange
        HealthcarePlanDto dto = new HealthcarePlanDto(null, "PLAN-INVALID", "Zero Coverage Plan", "Invalid", new BigDecimal("0.00"), new BigDecimal("200.00"), "Full Time", true);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> planService.createPlan(dto));
    }

    @Test
    @DisplayName("Get Plan By ID - Success")
    void getPlanById_Success() {
        // Arrange
        when(planRepository.findById(1L)).thenReturn(Optional.of(samplePlan));

        // Act
        HealthcarePlanDto result = planService.getPlanById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Gold Plan", result.getPlanName());
    }

    @Test
    @DisplayName("Get Plan By ID - Not Found Exception")
    void getPlanById_NotFound_ThrowsException() {
        // Arrange
        when(planRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> planService.getPlanById(99L));
    }
}
