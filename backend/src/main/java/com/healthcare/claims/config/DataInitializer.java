package com.healthcare.claims.config;

import com.healthcare.claims.entity.*;
import com.healthcare.claims.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final HealthcarePlanRepository planRepository;
    private final EmployeePlanRepository employeePlanRepository;
    private final ClaimRepository claimRepository;
    private final ClaimRemarkRepository remarkRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           EmployeeRepository employeeRepository,
                           HealthcarePlanRepository planRepository,
                           EmployeePlanRepository employeePlanRepository,
                           ClaimRepository claimRepository,
                           ClaimRemarkRepository remarkRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.planRepository = planRepository;
        this.employeePlanRepository = employeePlanRepository;
        this.claimRepository = claimRepository;
        this.remarkRepository = remarkRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() == 0) {
            seedInitialData();
        }
    }

    private void seedInitialData() {
        // 1. Seed Users
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@healthcare.com");
        adminUser.setPassword(passwordEncoder.encode("AdminPass123!"));
        adminUser.setRole(Role.ROLE_ADMIN);
        adminUser = userRepository.save(adminUser);

        User hrUser = new User();
        hrUser.setUsername("hrofficer");
        hrUser.setEmail("hr@healthcare.com");
        hrUser.setPassword(passwordEncoder.encode("OfficerPass123!"));
        hrUser.setRole(Role.ROLE_HR_OFFICER);
        hrUser = userRepository.save(hrUser);

        User empUser1 = new User();
        empUser1.setUsername("employee1");
        empUser1.setEmail("john.doe@healthcare.com");
        empUser1.setPassword(passwordEncoder.encode("EmpPass123!"));
        empUser1.setRole(Role.ROLE_EMPLOYEE);
        empUser1 = userRepository.save(empUser1);

        User empUser2 = new User();
        empUser2.setUsername("employee2");
        empUser2.setEmail("jane.smith@healthcare.com");
        empUser2.setPassword(passwordEncoder.encode("EmpPass123!"));
        empUser2.setRole(Role.ROLE_EMPLOYEE);
        empUser2 = userRepository.save(empUser2);

        // 2. Seed Employees
        Employee emp1 = new Employee(null, "EMP1001", "John Doe", "john.doe@healthcare.com", "9876543210", "Engineering", LocalDate.of(2023, 1, 15), "ACTIVE", empUser1);
        emp1 = employeeRepository.save(emp1);

        Employee emp2 = new Employee(null, "EMP1002", "Jane Smith", "jane.smith@healthcare.com", "9876543211", "Quality Assurance", LocalDate.of(2023, 3, 10), "ACTIVE", empUser2);
        emp2 = employeeRepository.save(emp2);

        Employee adminEmp = new Employee(null, "EMP1000", "System Admin", "admin@healthcare.com", "9876543212", "Management", LocalDate.of(2022, 1, 1), "ACTIVE", adminUser);
        employeeRepository.save(adminEmp);

        Employee hrEmp = new Employee(null, "EMP1003", "HR Officer Sarah", "hr@healthcare.com", "9876543213", "Human Resources", LocalDate.of(2022, 5, 20), "ACTIVE", hrUser);
        employeeRepository.save(hrEmp);

        // 3. Seed Healthcare Plans
        HealthcarePlan plan1 = new HealthcarePlan(null, "PLAN-GOLD-01", "Comprehensive Medical Gold", "Full coverage for OPD, IPD, and dental care up to $50,000", new BigDecimal("50000.00"), new BigDecimal("250.00"), "Full-time employees after probation", true);
        plan1 = planRepository.save(plan1);

        HealthcarePlan plan2 = new HealthcarePlan(null, "PLAN-SILVER-02", "Standard Care Silver", "Standard coverage for hospitalization and emergencies up to $25,000", new BigDecimal("25000.00"), new BigDecimal("120.00"), "All permanent employees", true);
        plan2 = planRepository.save(plan2);

        HealthcarePlan plan3 = new HealthcarePlan(null, "PLAN-PLATINUM-03", "Executive Platinum Care", "Premium zero-deductible worldwide medical coverage up to $100,000", new BigDecimal("100000.00"), new BigDecimal("450.00"), "Senior staff & executives", true);
        plan3 = planRepository.save(plan3);

        // 4. Enroll Employees in Plans
        EmployeePlan empPlan1 = new EmployeePlan(null, emp1, plan1, LocalDate.of(2023, 2, 1), "ENROLLED");
        employeePlanRepository.save(empPlan1);

        EmployeePlan empPlan2 = new EmployeePlan(null, emp2, plan2, LocalDate.of(2023, 4, 1), "ENROLLED");
        employeePlanRepository.save(empPlan2);

        // 5. Seed Claims
        Claim c1 = new Claim(null, "CLM-2026-0001", emp1, plan1, new BigDecimal("1250.00"), LocalDate.now().minusDays(10), "OPD Consultation & Diagnostics", "Routine annual medical checkup and MRI scan", "/docs/receipt_001.pdf", ClaimStatus.PENDING, null, null, null);
        c1 = claimRepository.save(c1);

        Claim c2 = new Claim(null, "CLM-2026-0002", emp2, plan2, new BigDecimal("3400.00"), LocalDate.now().minusDays(20), "Dental Surgery", "Root canal treatment and dental crown placement", "/docs/receipt_002.pdf", ClaimStatus.UNDER_REVIEW, "Documents verified with hospital desk", null, null);
        c2 = claimRepository.save(c2);

        Claim c3 = new Claim(null, "CLM-2026-0003", emp1, plan1, new BigDecimal("4800.00"), LocalDate.now().minusDays(30), "Emergency Room", "Emergency appendectomy procedures", "/docs/receipt_003.pdf", ClaimStatus.APPROVED, "Verified and approved per policy limits", null, null);
        c3 = claimRepository.save(c3);

        Claim c4 = new Claim(null, "CLM-2026-0004", emp2, plan2, new BigDecimal("15000.00"), LocalDate.now().minusDays(40), "Cosmetic Surgery", "Elective aesthetic procedure", "/docs/receipt_004.pdf", ClaimStatus.REJECTED, "Elective cosmetic procedures are excluded under Plan Silver terms", null, null);
        c4 = claimRepository.save(c4);

        // 6. Seed Remarks
        remarkRepository.save(new ClaimRemark(null, c1, "John Doe", "EMPLOYEE", "Claim submitted for OPD diagnostic checkup.", null));
        remarkRepository.save(new ClaimRemark(null, c2, "HR Officer Sarah", "HR/CLAIMS OFFICER", "Invoices verified with Dental Clinic desk.", null));
        remarkRepository.save(new ClaimRemark(null, c3, "HR Officer Sarah", "HR/CLAIMS OFFICER", "Emergency hospitalization verified and approved.", null));
        remarkRepository.save(new ClaimRemark(null, c4, "HR Officer Sarah", "HR/CLAIMS OFFICER", "REJECTED: Policy Section 4.2 excludes non-prescribed elective cosmetic care.", null));
    }
}
