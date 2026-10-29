package com.healthcare.claims.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ClaimRequestDto {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Healthcare Plan ID is required")
    private Long planId;

    @NotNull(message = "Claim amount is required")
    @DecimalMin(value = "0.01", message = "Claim amount must be greater than zero")
    private BigDecimal claimAmount;

    @NotNull(message = "Claim date is required")
    private LocalDate claimDate;

    @NotBlank(message = "Claim type is required")
    private String claimType;

    @NotBlank(message = "Claim description is required")
    private String description;

    private String supportingDocumentUrl;

    public ClaimRequestDto() {
    }

    public ClaimRequestDto(Long employeeId, Long planId, BigDecimal claimAmount, LocalDate claimDate, String claimType, String description, String supportingDocumentUrl) {
        this.employeeId = employeeId;
        this.planId = planId;
        this.claimAmount = claimAmount;
        this.claimDate = claimDate;
        this.claimType = claimType;
        this.description = description;
        this.supportingDocumentUrl = supportingDocumentUrl;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public BigDecimal getClaimAmount() {
        return claimAmount;
    }

    public void setClaimAmount(BigDecimal claimAmount) {
        this.claimAmount = claimAmount;
    }

    public LocalDate getClaimDate() {
        return claimDate;
    }

    public void setClaimDate(LocalDate claimDate) {
        this.claimDate = claimDate;
    }

    public String getClaimType() {
        return claimType;
    }

    public void setClaimType(String claimType) {
        this.claimType = claimType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSupportingDocumentUrl() {
        return supportingDocumentUrl;
    }

    public void setSupportingDocumentUrl(String supportingDocumentUrl) {
        this.supportingDocumentUrl = supportingDocumentUrl;
    }
}
