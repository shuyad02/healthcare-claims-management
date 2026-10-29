package com.healthcare.claims.repository;

import com.healthcare.claims.entity.Claim;
import com.healthcare.claims.entity.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {
    Optional<Claim> findByClaimNumber(String claimNumber);
    List<Claim> findByEmployeeId(Long employeeId);
    List<Claim> findByStatus(ClaimStatus status);

    @Query("SELECT c FROM Claim c WHERE " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:employeeId IS NULL OR c.employee.id = :employeeId)")
    List<Claim> searchClaims(@Param("status") ClaimStatus status, @Param("employeeId") Long employeeId);

    // Duplicate check: active pending/under_review claim for same employee, plan, and amount
    @Query("SELECT COUNT(c) > 0 FROM Claim c WHERE c.employee.id = :employeeId " +
           "AND c.plan.id = :planId AND c.claimAmount = :claimAmount " +
           "AND c.status IN ('PENDING', 'UNDER_REVIEW')")
    boolean existsDuplicatePendingClaim(@Param("employeeId") Long employeeId,
                                        @Param("planId") Long planId,
                                        @Param("claimAmount") BigDecimal claimAmount);

    long countByStatus(ClaimStatus status);

    @Query("SELECT COALESCE(SUM(c.claimAmount), 0) FROM Claim c WHERE c.status = 'APPROVED'")
    BigDecimal calculateTotalApprovedClaimAmount();

    @Query("SELECT COALESCE(SUM(c.claimAmount), 0) FROM Claim c")
    BigDecimal calculateTotalClaimAmount();
}
