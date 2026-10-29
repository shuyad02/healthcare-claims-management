package com.healthcare.claims.service;

import com.healthcare.claims.dto.ClaimRequestDto;
import com.healthcare.claims.dto.ClaimResponseDto;
import com.healthcare.claims.dto.ClaimReviewDto;
import com.healthcare.claims.entity.*;
import com.healthcare.claims.exception.BadRequestException;
import com.healthcare.claims.exception.ConflictException;
import com.healthcare.claims.exception.ResourceNotFoundException;
import com.healthcare.claims.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final EmployeeRepository employeeRepository;
    private final HealthcarePlanRepository planRepository;
    private final EmployeePlanRepository employeePlanRepository;
    private final ClaimRemarkRepository remarkRepository;

    public ClaimService(ClaimRepository claimRepository,
                        EmployeeRepository employeeRepository,
                        HealthcarePlanRepository planRepository,
                        EmployeePlanRepository employeePlanRepository,
                        ClaimRemarkRepository remarkRepository) {
        this.claimRepository = claimRepository;
        this.employeeRepository = employeeRepository;
        this.planRepository = planRepository;
        this.employeePlanRepository = employeePlanRepository;
        this.remarkRepository = remarkRepository;
    }

    public List<ClaimResponseDto> getAllClaims() {
        return claimRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public ClaimResponseDto getClaimById(Long id) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + id));
        return mapToDto(claim);
    }

    public List<ClaimResponseDto> getClaimsByEmployee(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with id: " + employeeId);
        }
        return claimRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<ClaimResponseDto> getClaimsByStatus(ClaimStatus status) {
        return claimRepository.findByStatus(status).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<ClaimResponseDto> searchClaims(ClaimStatus status, Long employeeId) {
        return claimRepository.searchClaims(status, employeeId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClaimResponseDto submitClaim(ClaimRequestDto dto) {
        // Validation 1: Amount must be > 0
        if (dto.getClaimAmount() == null || dto.getClaimAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Claim amount must be greater than zero");
        }

        // Validation 2: Employee existence
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + dto.getEmployeeId()));

        // Validation 3: Healthcare Plan existence and active status
        HealthcarePlan plan = planRepository.findById(dto.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Healthcare plan not found with id: " + dto.getPlanId()));

        if (!plan.isActive()) {
            throw new BadRequestException("Selected healthcare plan is inactive or invalid");
        }

        // Validation 4: Active plan enrollment check
        boolean isEnrolled = employeePlanRepository.existsByEmployeeIdAndPlanIdAndStatus(employee.getId(), plan.getId(), "ENROLLED");
        if (!isEnrolled) {
            // Auto-enroll if plan is active to streamline dev/test experience, OR throw if strict
            EmployeePlan empPlan = new EmployeePlan();
            empPlan.setEmployee(employee);
            empPlan.setPlan(plan);
            empPlan.setEnrollmentDate(LocalDate.now());
            empPlan.setStatus("ENROLLED");
            employeePlanRepository.save(empPlan);
        }

        // Validation 5: Check coverage limit
        if (dto.getClaimAmount().compareTo(plan.getCoverageAmount()) > 0) {
            throw new BadRequestException("Claim amount ($" + dto.getClaimAmount() + ") exceeds plan coverage limit ($" + plan.getCoverageAmount() + ")");
        }

        // Validation 6: Duplicate claim check
        boolean hasDuplicate = claimRepository.existsDuplicatePendingClaim(dto.getEmployeeId(), dto.getPlanId(), dto.getClaimAmount());
        if (hasDuplicate) {
            throw new ConflictException("Employee cannot submit duplicate claim with identical plan and amount while processing");
        }

        Claim claim = new Claim();
        String claimNumber = "CLM-2026-" + String.format("%04d", new Random().nextInt(10000));
        claim.setClaimNumber(claimNumber);
        claim.setEmployee(employee);
        claim.setPlan(plan);
        claim.setClaimAmount(dto.getClaimAmount());
        claim.setClaimDate(dto.getClaimDate() != null ? dto.getClaimDate() : LocalDate.now());
        claim.setClaimType(dto.getClaimType());
        claim.setDescription(dto.getDescription());
        claim.setSupportingDocumentUrl(dto.getSupportingDocumentUrl() != null ? dto.getSupportingDocumentUrl() : "/docs/sample-receipt.pdf");
        claim.setStatus(ClaimStatus.PENDING);

        Claim savedClaim = claimRepository.save(claim);

        // Add initial remark
        ClaimRemark remark = new ClaimRemark();
        remark.setClaim(savedClaim);
        remark.setAuthorName(employee.getName());
        remark.setAuthorRole("EMPLOYEE");
        remark.setRemark("Claim submitted for processing.");
        remarkRepository.save(remark);

        return mapToDto(savedClaim);
    }

    @Transactional
    public ClaimResponseDto reviewClaim(Long claimId, ClaimReviewDto reviewDto) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));

        if (claim.getStatus() == ClaimStatus.APPROVED || claim.getStatus() == ClaimStatus.REJECTED) {
            throw new BadRequestException("Cannot review a claim that has already been final-processed (APPROVED or REJECTED)");
        }

        claim.setStatus(ClaimStatus.UNDER_REVIEW);
        if (reviewDto != null && reviewDto.getOfficerRemarks() != null && !reviewDto.getOfficerRemarks().isBlank()) {
            claim.setOfficerRemarks(reviewDto.getOfficerRemarks());
        }

        Claim updatedClaim = claimRepository.save(claim);

        ClaimRemark remark = new ClaimRemark();
        remark.setClaim(updatedClaim);
        remark.setAuthorName("HR Officer");
        remark.setAuthorRole("HR/CLAIMS OFFICER");
        remark.setRemark(reviewDto != null && reviewDto.getOfficerRemarks() != null ? reviewDto.getOfficerRemarks() : "Claim status moved to UNDER_REVIEW");
        remarkRepository.save(remark);

        return mapToDto(updatedClaim);
    }

    @Transactional
    public ClaimResponseDto approveClaim(Long claimId, ClaimReviewDto reviewDto) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));

        // Workflow requirement: Claim cannot be approved without review
        if (claim.getStatus() != ClaimStatus.UNDER_REVIEW) {
            throw new BadRequestException("Claim cannot be approved without prior review. Current status is " + claim.getStatus() + ". Please review first.");
        }

        claim.setStatus(ClaimStatus.APPROVED);
        if (reviewDto != null && reviewDto.getOfficerRemarks() != null && !reviewDto.getOfficerRemarks().isBlank()) {
            claim.setOfficerRemarks(reviewDto.getOfficerRemarks());
        }

        Claim updatedClaim = claimRepository.save(claim);

        ClaimRemark remark = new ClaimRemark();
        remark.setClaim(updatedClaim);
        remark.setAuthorName("HR Officer");
        remark.setAuthorRole("HR/CLAIMS OFFICER");
        remark.setRemark(reviewDto != null && reviewDto.getOfficerRemarks() != null ? reviewDto.getOfficerRemarks() : "Claim has been APPROVED");
        remarkRepository.save(remark);

        return mapToDto(updatedClaim);
    }

    @Transactional
    public ClaimResponseDto rejectClaim(Long claimId, ClaimReviewDto reviewDto) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));

        // Validation rule: Rejected claim must contain a rejection reason
        if (reviewDto == null || reviewDto.getOfficerRemarks() == null || reviewDto.getOfficerRemarks().trim().isEmpty()) {
            throw new BadRequestException("Rejected claim must contain a rejection reason in officer remarks");
        }

        claim.setStatus(ClaimStatus.REJECTED);
        claim.setOfficerRemarks(reviewDto.getOfficerRemarks());

        Claim updatedClaim = claimRepository.save(claim);

        ClaimRemark remark = new ClaimRemark();
        remark.setClaim(updatedClaim);
        remark.setAuthorName("HR Officer");
        remark.setAuthorRole("HR/CLAIMS OFFICER");
        remark.setRemark("REJECTED: " + reviewDto.getOfficerRemarks());
        remarkRepository.save(remark);

        return mapToDto(updatedClaim);
    }

    @Transactional
    public ClaimResponseDto updateClaim(Long claimId, ClaimRequestDto dto) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));

        if (dto.getClaimAmount() != null && dto.getClaimAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Claim amount must be greater than zero");
        }

        if (dto.getClaimAmount() != null) claim.setClaimAmount(dto.getClaimAmount());
        if (dto.getClaimType() != null) claim.setClaimType(dto.getClaimType());
        if (dto.getDescription() != null) claim.setDescription(dto.getDescription());
        if (dto.getSupportingDocumentUrl() != null) claim.setSupportingDocumentUrl(dto.getSupportingDocumentUrl());

        Claim updatedClaim = claimRepository.save(claim);
        return mapToDto(updatedClaim);
    }

    public ClaimResponseDto mapToDto(Claim claim) {
        ClaimResponseDto dto = new ClaimResponseDto();
        dto.setId(claim.getId());
        dto.setClaimNumber(claim.getClaimNumber());
        if (claim.getEmployee() != null) {
            dto.setEmployeeId(claim.getEmployee().getId());
            dto.setEmployeeCode(claim.getEmployee().getEmployeeCode());
            dto.setEmployeeName(claim.getEmployee().getName());
            dto.setEmployeeDepartment(claim.getEmployee().getDepartment());
        }
        if (claim.getPlan() != null) {
            dto.setPlanId(claim.getPlan().getId());
            dto.setPlanCode(claim.getPlan().getPlanCode());
            dto.setPlanName(claim.getPlan().getPlanName());
        }
        dto.setClaimAmount(claim.getClaimAmount());
        dto.setClaimDate(claim.getClaimDate());
        dto.setClaimType(claim.getClaimType());
        dto.setDescription(claim.getDescription());
        dto.setSupportingDocumentUrl(claim.getSupportingDocumentUrl());
        dto.setStatus(claim.getStatus());
        dto.setOfficerRemarks(claim.getOfficerRemarks());
        dto.setCreatedAt(claim.getCreatedAt());
        dto.setUpdatedAt(claim.getUpdatedAt());
        return dto;
    }
}
