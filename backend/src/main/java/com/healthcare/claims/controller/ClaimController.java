package com.healthcare.claims.controller;

import com.healthcare.claims.dto.ClaimRequestDto;
import com.healthcare.claims.dto.ClaimResponseDto;
import com.healthcare.claims.dto.ClaimReviewDto;
import com.healthcare.claims.entity.ClaimStatus;
import com.healthcare.claims.service.ClaimService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_OFFICER')")
    public ResponseEntity<List<ClaimResponseDto>> getAllClaims(
            @RequestParam(required = false) ClaimStatus status,
            @RequestParam(required = false) Long employeeId) {
        if (status != null || employeeId != null) {
            return ResponseEntity.ok(claimService.searchClaims(status, employeeId));
        }
        return ResponseEntity.ok(claimService.getAllClaims());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_OFFICER', 'EMPLOYEE')")
    public ResponseEntity<ClaimResponseDto> getClaimById(@PathVariable Long id) {
        return ResponseEntity.ok(claimService.getClaimById(id));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_OFFICER', 'EMPLOYEE')")
    public ResponseEntity<List<ClaimResponseDto>> getClaimsByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(claimService.getClaimsByEmployee(employeeId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<ClaimResponseDto> submitClaim(@Valid @RequestBody ClaimRequestDto dto) {
        ClaimResponseDto created = claimService.submitClaim(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<ClaimResponseDto> updateClaim(@PathVariable Long id, @RequestBody ClaimRequestDto dto) {
        return ResponseEntity.ok(claimService.updateClaim(id, dto));
    }

    @PatchMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('HR_OFFICER', 'ADMIN')")
    public ResponseEntity<ClaimResponseDto> reviewClaim(@PathVariable Long id, @RequestBody(required = false) ClaimReviewDto dto) {
        return ResponseEntity.ok(claimService.reviewClaim(id, dto));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('HR_OFFICER', 'ADMIN')")
    public ResponseEntity<ClaimResponseDto> approveClaim(@PathVariable Long id, @RequestBody(required = false) ClaimReviewDto dto) {
        return ResponseEntity.ok(claimService.approveClaim(id, dto));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('HR_OFFICER', 'ADMIN')")
    public ResponseEntity<ClaimResponseDto> rejectClaim(@PathVariable Long id, @RequestBody ClaimReviewDto dto) {
        return ResponseEntity.ok(claimService.rejectClaim(id, dto));
    }
}
