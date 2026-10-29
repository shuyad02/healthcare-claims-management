package com.healthcare.claims.service;

import com.healthcare.claims.dto.DashboardStatsDto;
import com.healthcare.claims.entity.ClaimStatus;
import com.healthcare.claims.repository.ClaimRepository;
import com.healthcare.claims.repository.EmployeeRepository;
import com.healthcare.claims.repository.HealthcarePlanRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class StatisticsService {

    private final EmployeeRepository employeeRepository;
    private final HealthcarePlanRepository planRepository;
    private final ClaimRepository claimRepository;

    public StatisticsService(EmployeeRepository employeeRepository,
                             HealthcarePlanRepository planRepository,
                             ClaimRepository claimRepository) {
        this.employeeRepository = employeeRepository;
        this.planRepository = planRepository;
        this.claimRepository = claimRepository;
    }

    public DashboardStatsDto getDashboardStats() {
        long totalEmployees = employeeRepository.count();
        long activePlans = planRepository.findByActive(true).size();
        long pendingClaims = claimRepository.countByStatus(ClaimStatus.PENDING);
        long approvedClaims = claimRepository.countByStatus(ClaimStatus.APPROVED);
        long rejectedClaims = claimRepository.countByStatus(ClaimStatus.REJECTED);
        BigDecimal totalClaimAmount = claimRepository.calculateTotalClaimAmount();
        BigDecimal totalApprovedAmount = claimRepository.calculateTotalApprovedClaimAmount();

        return new DashboardStatsDto(
                totalEmployees,
                activePlans,
                pendingClaims,
                approvedClaims,
                rejectedClaims,
                totalClaimAmount != null ? totalClaimAmount : BigDecimal.ZERO,
                totalApprovedAmount != null ? totalApprovedAmount : BigDecimal.ZERO
        );
    }
}
