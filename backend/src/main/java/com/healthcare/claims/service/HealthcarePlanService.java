package com.healthcare.claims.service;

import com.healthcare.claims.dto.HealthcarePlanDto;
import com.healthcare.claims.entity.Employee;
import com.healthcare.claims.entity.EmployeePlan;
import com.healthcare.claims.entity.HealthcarePlan;
import com.healthcare.claims.exception.BadRequestException;
import com.healthcare.claims.exception.ConflictException;
import com.healthcare.claims.exception.ResourceNotFoundException;
import com.healthcare.claims.repository.EmployeePlanRepository;
import com.healthcare.claims.repository.EmployeeRepository;
import com.healthcare.claims.repository.HealthcarePlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HealthcarePlanService {

    private final HealthcarePlanRepository planRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeePlanRepository employeePlanRepository;

    public HealthcarePlanService(HealthcarePlanRepository planRepository,
                                 EmployeeRepository employeeRepository,
                                 EmployeePlanRepository employeePlanRepository) {
        this.planRepository = planRepository;
        this.employeeRepository = employeeRepository;
        this.employeePlanRepository = employeePlanRepository;
    }

    public List<HealthcarePlanDto> getAllPlans() {
        return planRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<HealthcarePlanDto> getActivePlans() {
        return planRepository.findByActive(true).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public HealthcarePlanDto getPlanById(Long id) {
        HealthcarePlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Healthcare plan not found with id: " + id));
        return mapToDto(plan);
    }

    @Transactional
    public HealthcarePlanDto createPlan(HealthcarePlanDto dto) {
        if (dto.getPlanCode() != null && planRepository.existsByPlanCode(dto.getPlanCode())) {
            throw new ConflictException("Plan code already exists: " + dto.getPlanCode());
        }

        if (dto.getCoverageAmount() == null || dto.getCoverageAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Coverage amount must be greater than zero");
        }

        if (dto.getPremium() == null || dto.getPremium().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Premium cannot be negative");
        }

        HealthcarePlan plan = new HealthcarePlan();
        plan.setPlanCode(dto.getPlanCode() != null ? dto.getPlanCode() : "PLAN-" + System.currentTimeMillis() % 10000);
        plan.setPlanName(dto.getPlanName());
        plan.setDescription(dto.getDescription());
        plan.setCoverageAmount(dto.getCoverageAmount());
        plan.setPremium(dto.getPremium());
        plan.setEligibility(dto.getEligibility());
        plan.setActive(dto.isActive());

        HealthcarePlan savedPlan = planRepository.save(plan);
        return mapToDto(savedPlan);
    }

    @Transactional
    public HealthcarePlanDto updatePlan(Long id, HealthcarePlanDto dto) {
        HealthcarePlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Healthcare plan not found with id: " + id));

        if (dto.getCoverageAmount() != null && dto.getCoverageAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Coverage amount must be greater than zero");
        }

        if (dto.getPremium() != null && dto.getPremium().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Premium cannot be negative");
        }

        plan.setPlanName(dto.getPlanName());
        plan.setDescription(dto.getDescription());
        if (dto.getCoverageAmount() != null) plan.setCoverageAmount(dto.getCoverageAmount());
        if (dto.getPremium() != null) plan.setPremium(dto.getPremium());
        plan.setEligibility(dto.getEligibility());
        plan.setActive(dto.isActive());

        HealthcarePlan updatedPlan = planRepository.save(plan);
        return mapToDto(updatedPlan);
    }

    @Transactional
    public void deletePlan(Long id) {
        HealthcarePlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Healthcare plan not found with id: " + id));

        planRepository.delete(plan);
    }

    @Transactional
    public void enrollEmployeeInPlan(Long employeeId, Long planId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        HealthcarePlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Healthcare plan not found with id: " + planId));

        if (!plan.isActive()) {
            throw new BadRequestException("Cannot enroll in an inactive healthcare plan");
        }

        if (employeePlanRepository.existsByEmployeeIdAndPlanIdAndStatus(employeeId, planId, "ENROLLED")) {
            throw new ConflictException("Employee is already enrolled in this healthcare plan");
        }

        EmployeePlan empPlan = new EmployeePlan();
        empPlan.setEmployee(employee);
        empPlan.setPlan(plan);
        empPlan.setEnrollmentDate(LocalDate.now());
        empPlan.setStatus("ENROLLED");

        employeePlanRepository.save(empPlan);
    }

    public HealthcarePlanDto mapToDto(HealthcarePlan plan) {
        return new HealthcarePlanDto(
                plan.getId(),
                plan.getPlanCode(),
                plan.getPlanName(),
                plan.getDescription(),
                plan.getCoverageAmount(),
                plan.getPremium(),
                plan.getEligibility(),
                plan.isActive()
        );
    }
}
