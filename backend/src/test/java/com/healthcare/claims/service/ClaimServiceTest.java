package com.healthcare.claims.service;

import com.healthcare.claims.dto.ClaimRequestDto;
import com.healthcare.claims.dto.ClaimResponseDto;
import com.healthcare.claims.dto.ClaimReviewDto;
import com.healthcare.claims.entity.*;
import com.healthcare.claims.exception.BadRequestException;
import com.healthcare.claims.exception.ConflictException;
import com.healthcare.claims.exception.ResourceNotFoundException;
import com.healthcare.claims.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private HealthcarePlanRepository planRepository;

    @Mock
    private EmployeePlanRepository employeePlanRepository;

    @Mock
    private ClaimRemarkRepository remarkRepository;

    @InjectMocks
    private ClaimService claimService;

    private Employee sampleEmployee;
    private HealthcarePlan samplePlan;
    private Claim sampleClaim;

    @BeforeEach
    void setUp() {
        sampleEmployee = new Employee(1L, "EMP1001", "John Doe", "john@example.com", "9876543210", "IT", LocalDate.now(), "ACTIVE", null);
        samplePlan = new HealthcarePlan(1L, "PLAN-GOLD-01", "Gold Plan", "Description", new BigDecimal("50000.00"), new BigDecimal("200.00"), "Full Time", true);

        sampleClaim = new Claim(100L, "CLM-2026-0001", sampleEmployee, samplePlan, new BigDecimal("1500.00"), LocalDate.now(), "OPD", "Consultation", "/doc.pdf", ClaimStatus.PENDING, null, null, null);
    }

    @Test
    @DisplayName("Test 6: Valid Claim Submission")
    void submitClaim_Success() {
        // Arrange
        ClaimRequestDto dto = new ClaimRequestDto(1L, 1L, new BigDecimal("1500.00"), LocalDate.now(), "OPD", "Consultation", "/doc.pdf");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));
        when(planRepository.findById(1L)).thenReturn(Optional.of(samplePlan));
        when(employeePlanRepository.existsByEmployeeIdAndPlanIdAndStatus(1L, 1L, "ENROLLED")).thenReturn(true);
        when(claimRepository.existsDuplicatePendingClaim(1L, 1L, new BigDecimal("1500.00"))).thenReturn(false);
        when(claimRepository.save(any(Claim.class))).thenReturn(sampleClaim);

        // Act
        ClaimResponseDto response = claimService.submitClaim(dto);

        // Assert
        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(ClaimStatus.PENDING, response.getStatus());
        assertEquals(new BigDecimal("1500.00"), response.getClaimAmount());

        verify(claimRepository).save(any(Claim.class));
    }

    @Test
    @DisplayName("Test 7: Invalid Claim Amount (Negative or Zero)")
    void submitClaim_InvalidAmount_ThrowsBadRequest() {
        // Arrange
        ClaimRequestDto dto = new ClaimRequestDto(1L, 1L, new BigDecimal("-500.00"), LocalDate.now(), "OPD", "Invalid Amount", null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> claimService.submitClaim(dto));
        assertTrue(exception.getMessage().contains("Claim amount must be greater than zero"));
        verify(claimRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test 8: Duplicate Claim Submission")
    void submitClaim_DuplicateClaim_ThrowsConflict() {
        // Arrange
        ClaimRequestDto dto = new ClaimRequestDto(1L, 1L, new BigDecimal("1500.00"), LocalDate.now(), "OPD", "Consultation", "/doc.pdf");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));
        when(planRepository.findById(1L)).thenReturn(Optional.of(samplePlan));
        when(employeePlanRepository.existsByEmployeeIdAndPlanIdAndStatus(1L, 1L, "ENROLLED")).thenReturn(true);
        when(claimRepository.existsDuplicatePendingClaim(1L, 1L, new BigDecimal("1500.00"))).thenReturn(true);

        // Act & Assert
        ConflictException exception = assertThrows(ConflictException.class, () -> claimService.submitClaim(dto));
        assertTrue(exception.getMessage().contains("duplicate claim"));
        verify(claimRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test 9: Claim Approval")
    void approveClaim_Success() {
        // Arrange
        sampleClaim.setStatus(ClaimStatus.UNDER_REVIEW);
        when(claimRepository.findById(100L)).thenReturn(Optional.of(sampleClaim));

        Claim approvedClaim = new Claim(100L, "CLM-2026-0001", sampleEmployee, samplePlan, new BigDecimal("1500.00"), LocalDate.now(), "OPD", "Consultation", "/doc.pdf", ClaimStatus.APPROVED, "Verified documents", null, null);
        when(claimRepository.save(any(Claim.class))).thenReturn(approvedClaim);

        ClaimReviewDto reviewDto = new ClaimReviewDto("Verified documents");

        // Act
        ClaimResponseDto result = claimService.approveClaim(100L, reviewDto);

        // Assert
        assertNotNull(result);
        assertEquals(ClaimStatus.APPROVED, result.getStatus());
        assertEquals("Verified documents", result.getOfficerRemarks());
    }

    @Test
    @DisplayName("Test 10: Claim Rejection with Reason")
    void rejectClaim_Success() {
        // Arrange
        when(claimRepository.findById(100L)).thenReturn(Optional.of(sampleClaim));

        Claim rejectedClaim = new Claim(100L, "CLM-2026-0001", sampleEmployee, samplePlan, new BigDecimal("1500.00"), LocalDate.now(), "OPD", "Consultation", "/doc.pdf", ClaimStatus.REJECTED, "Invalid medical bill format", null, null);
        when(claimRepository.save(any(Claim.class))).thenReturn(rejectedClaim);

        ClaimReviewDto reviewDto = new ClaimReviewDto("Invalid medical bill format");

        // Act
        ClaimResponseDto result = claimService.rejectClaim(100L, reviewDto);

        // Assert
        assertNotNull(result);
        assertEquals(ClaimStatus.REJECTED, result.getStatus());
        assertEquals("Invalid medical bill format", result.getOfficerRemarks());
    }

    @Test
    @DisplayName("Test 11: Unauthorized Claim Approval Without Prior Review")
    void approveClaim_WithoutReview_ThrowsBadRequest() {
        // Arrange (Claim status is PENDING, not UNDER_REVIEW)
        sampleClaim.setStatus(ClaimStatus.PENDING);
        when(claimRepository.findById(100L)).thenReturn(Optional.of(sampleClaim));

        ClaimReviewDto reviewDto = new ClaimReviewDto("Attempt direct approval");

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> claimService.approveClaim(100L, reviewDto));
        assertTrue(exception.getMessage().contains("cannot be approved without prior review"));
    }

    @Test
    @DisplayName("Test 12: Non-Existing Claim Access")
    void getClaimById_NonExisting_ThrowsResourceNotFound() {
        // Arrange
        when(claimRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> claimService.getClaimById(999L));
        assertTrue(exception.getMessage().contains("Claim not found with id: 999"));
    }
}
