package com.healthcare.claims.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class HealthcarePlanDto {

    private Long id;
    private String planCode;

    @NotBlank(message = "Plan name is required")
    private String planName;

    private String description;

    @NotNull(message = "Coverage amount is required")
    @DecimalMin(value = "0.01", message = "Coverage amount must be greater than zero")
    private BigDecimal coverageAmount;

    @NotNull(message = "Premium is required")
    @DecimalMin(value = "0.00", message = "Premium cannot be negative")
    private BigDecimal premium;

    private String eligibility;
    private boolean active = true;

    public HealthcarePlanDto() {
    }

    public HealthcarePlanDto(Long id, String planCode, String planName, String description, BigDecimal coverageAmount, BigDecimal premium, String eligibility, boolean active) {
        this.id = id;
        this.planCode = planCode;
        this.planName = planName;
        this.description = description;
        this.coverageAmount = coverageAmount;
        this.premium = premium;
        this.eligibility = eligibility;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlanCode() {
        return planCode;
    }

    public void setPlanCode(String planCode) {
        this.planCode = planCode;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getCoverageAmount() {
        return coverageAmount;
    }

    public void setCoverageAmount(BigDecimal coverageAmount) {
        this.coverageAmount = coverageAmount;
    }

    public BigDecimal getPremium() {
        return premium;
    }

    public void setPremium(BigDecimal premium) {
        this.premium = premium;
    }

    public String getEligibility() {
        return eligibility;
    }

    public void setEligibility(String eligibility) {
        this.eligibility = eligibility;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
