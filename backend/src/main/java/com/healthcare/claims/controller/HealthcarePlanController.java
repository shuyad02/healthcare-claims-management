package com.healthcare.claims.controller;

import com.healthcare.claims.dto.HealthcarePlanDto;
import com.healthcare.claims.service.HealthcarePlanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
public class HealthcarePlanController {

    private final HealthcarePlanService planService;

    public HealthcarePlanController(HealthcarePlanService planService) {
        this.planService = planService;
    }

    @GetMapping
    public ResponseEntity<List<HealthcarePlanDto>> getAllPlans() {
        return ResponseEntity.ok(planService.getAllPlans());
    }

    @GetMapping("/active")
    public ResponseEntity<List<HealthcarePlanDto>> getActivePlans() {
        return ResponseEntity.ok(planService.getActivePlans());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HealthcarePlanDto> getPlanById(@PathVariable Long id) {
        return ResponseEntity.ok(planService.getPlanById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HealthcarePlanDto> createPlan(@Valid @RequestBody HealthcarePlanDto dto) {
        HealthcarePlanDto created = planService.createPlan(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HealthcarePlanDto> updatePlan(@PathVariable Long id, @Valid @RequestBody HealthcarePlanDto dto) {
        return ResponseEntity.ok(planService.updatePlan(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePlan(@PathVariable Long id) {
        planService.deletePlan(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{planId}/enroll/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'HR_OFFICER')")
    public ResponseEntity<Void> enrollEmployee(@PathVariable Long planId, @PathVariable Long employeeId) {
        planService.enrollEmployeeInPlan(employeeId, planId);
        return ResponseEntity.ok().build();
    }
}
