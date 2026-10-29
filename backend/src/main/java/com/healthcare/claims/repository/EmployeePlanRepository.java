package com.healthcare.claims.repository;

import com.healthcare.claims.entity.EmployeePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeePlanRepository extends JpaRepository<EmployeePlan, Long> {
    List<EmployeePlan> findByEmployeeId(Long employeeId);
    Optional<EmployeePlan> findByEmployeeIdAndPlanIdAndStatus(Long employeeId, Long planId, String status);
    boolean existsByEmployeeIdAndPlanIdAndStatus(Long employeeId, Long planId, String status);
}
