package com.healthcare.claims.dto;

import java.math.BigDecimal;

public class DashboardStatsDto {

    private long totalEmployees;
    private long activePlans;
    private long pendingClaims;
    private long approvedClaims;
    private long rejectedClaims;
    private BigDecimal totalClaimAmount;
    private BigDecimal totalApprovedAmount;

    public DashboardStatsDto() {
    }

    public DashboardStatsDto(long totalEmployees, long activePlans, long pendingClaims, long approvedClaims, long rejectedClaims, BigDecimal totalClaimAmount, BigDecimal totalApprovedAmount) {
        this.totalEmployees = totalEmployees;
        this.activePlans = activePlans;
        this.pendingClaims = pendingClaims;
        this.approvedClaims = approvedClaims;
        this.rejectedClaims = rejectedClaims;
        this.totalClaimAmount = totalClaimAmount;
        this.totalApprovedAmount = totalApprovedAmount;
    }

    public long getTotalEmployees() {
        return totalEmployees;
    }

    public void setTotalEmployees(long totalEmployees) {
        this.totalEmployees = totalEmployees;
    }

    public long getActivePlans() {
        return activePlans;
    }

    public void setActivePlans(long activePlans) {
        this.activePlans = activePlans;
    }

    public long getPendingClaims() {
        return pendingClaims;
    }

    public void setPendingClaims(long pendingClaims) {
        this.pendingClaims = pendingClaims;
    }

    public long getApprovedClaims() {
        return approvedClaims;
    }

    public void setApprovedClaims(long approvedClaims) {
        this.approvedClaims = approvedClaims;
    }

    public long getRejectedClaims() {
        return rejectedClaims;
    }

    public void setRejectedClaims(long rejectedClaims) {
        this.rejectedClaims = rejectedClaims;
    }

    public BigDecimal getTotalClaimAmount() {
        return totalClaimAmount;
    }

    public void setTotalClaimAmount(BigDecimal totalClaimAmount) {
        this.totalClaimAmount = totalClaimAmount;
    }

    public BigDecimal getTotalApprovedAmount() {
        return totalApprovedAmount;
    }

    public void setTotalApprovedAmount(BigDecimal totalApprovedAmount) {
        this.totalApprovedAmount = totalApprovedAmount;
    }
}
