package com.healthcare.claims.repository;

import com.healthcare.claims.entity.HealthcarePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HealthcarePlanRepository extends JpaRepository<HealthcarePlan, Long> {
    Optional<HealthcarePlan> findByPlanCode(String planCode);
    List<HealthcarePlan> findByActive(boolean active);
    boolean existsByPlanCode(String planCode);
}
