package com.healthcare.claims.service;

import com.healthcare.claims.dto.AuthRequest;
import com.healthcare.claims.dto.AuthResponse;
import com.healthcare.claims.dto.RegisterRequest;
import com.healthcare.claims.entity.*;
import com.healthcare.claims.exception.BadRequestException;
import com.healthcare.claims.exception.ConflictException;
import com.healthcare.claims.repository.EmployeePlanRepository;
import com.healthcare.claims.repository.EmployeeRepository;
import com.healthcare.claims.repository.HealthcarePlanRepository;
import com.healthcare.claims.repository.UserRepository;
import com.healthcare.claims.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final HealthcarePlanRepository planRepository;
    private final EmployeePlanRepository employeePlanRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UserRepository userRepository,
                                 EmployeeRepository employeeRepository,
                                 HealthcarePlanRepository planRepository,
                                 EmployeePlanRepository employeePlanRepository,
                                 PasswordEncoder passwordEncoder,
                                 JwtTokenProvider tokenProvider,
                                 AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.planRepository = planRepository;
        this.employeePlanRepository = employeePlanRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email is already registered");
        }

        if (request.getPhone() != null && !request.getPhone().matches("^[0-9]{10}$")) {
            throw new BadRequestException("Phone number must contain exactly 10 digits");
        }

        Role role = request.getRole() != null ? request.getRole() : Role.ROLE_EMPLOYEE;

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);

        User savedUser = userRepository.save(user);

        Employee savedEmployee = null;
        if (role == Role.ROLE_EMPLOYEE || role == Role.ROLE_ADMIN || role == Role.ROLE_HR_OFFICER) {
            Employee employee = new Employee();
            employee.setEmployeeCode("EMP" + (1000 + savedUser.getId()));
            employee.setName(request.getName());
            employee.setEmail(request.getEmail());
            employee.setPhone(request.getPhone() != null ? request.getPhone() : "9876543210");
            employee.setDepartment(request.getDepartment() != null ? request.getDepartment() : "General");
            employee.setDateOfJoining(request.getDateOfJoining() != null ? request.getDateOfJoining() : LocalDate.now());
            employee.setStatus("ACTIVE");
            employee.setUser(savedUser);

            savedEmployee = employeeRepository.save(employee);

            // Auto-enroll in first active plan if available
            List<HealthcarePlan> activePlans = planRepository.findByActive(true);
            if (!activePlans.isEmpty()) {
                EmployeePlan empPlan = new EmployeePlan();
                empPlan.setEmployee(savedEmployee);
                empPlan.setPlan(activePlans.get(0));
                empPlan.setEnrollmentDate(LocalDate.now());
                empPlan.setStatus("ENROLLED");
                employeePlanRepository.save(empPlan);
            }
        }

        String token = tokenProvider.generateTokenFromUsername(savedUser.getUsername(), savedUser.getRole().name(), savedUser.getId());

        Long empId = savedEmployee != null ? savedEmployee.getId() : null;
        String empCode = savedEmployee != null ? savedEmployee.getEmployeeCode() : null;

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRole(),
                empId,
                empCode,
                request.getName()
        );
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String token = tokenProvider.generateToken(authentication);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseGet(() -> userRepository.findByEmail(request.getUsername())
                .orElseThrow(() -> new BadRequestException("User not found")));

        Optional<Employee> employeeOpt = employeeRepository.findByUserId(user.getId());

        Long empId = employeeOpt.map(Employee::getId).orElse(null);
        String empCode = employeeOpt.map(Employee::getEmployeeCode).orElse(null);
        String empName = employeeOpt.map(Employee::getName).orElse(user.getUsername());

        return new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                empId,
                empCode,
                empName
        );
    }
}
