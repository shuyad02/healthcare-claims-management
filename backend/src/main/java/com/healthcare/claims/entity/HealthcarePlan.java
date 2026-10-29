package com.healthcare.claims.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "healthcare_plans")
public class HealthcarePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_code", unique = true, nullable = false)
    private String planCode;

    @Column(name = "plan_name", nullable = false)
    private String planName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "coverage_amount", nullable = false)
    private BigDecimal coverageAmount;

    @Column(nullable = false)
    private BigDecimal premium;

    private String eligibility;

    @Column(nullable = false)
    private boolean active = true;

    public HealthcarePlan() {
    }

    public HealthcarePlan(Long id, String planCode, String planName, String description, BigDecimal coverageAmount, BigDecimal premium, String eligibility, boolean active) {
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
