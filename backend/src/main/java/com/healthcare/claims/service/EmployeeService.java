package com.healthcare.claims.service;

import com.healthcare.claims.dto.EmployeeDto;
import com.healthcare.claims.entity.Employee;
import com.healthcare.claims.entity.Role;
import com.healthcare.claims.entity.User;
import com.healthcare.claims.exception.BadRequestException;
import com.healthcare.claims.exception.ConflictException;
import com.healthcare.claims.exception.ResourceNotFoundException;
import com.healthcare.claims.repository.EmployeeRepository;
import com.healthcare.claims.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeRepository employeeRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<EmployeeDto> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public EmployeeDto getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return mapToDto(employee);
    }

    public EmployeeDto getEmployeeByUserId(Long userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found for user id: " + userId));
        return mapToDto(employee);
    }

    @Transactional
    public EmployeeDto createEmployee(EmployeeDto dto) {
        if (employeeRepository.existsByEmail(dto.getEmail())) {
            throw new ConflictException("Employee email already exists: " + dto.getEmail());
        }

        if (dto.getPhone() != null && !dto.getPhone().matches("^[0-9]{10}$")) {
            throw new BadRequestException("Phone number must be a valid 10-digit number");
        }

        User user = new User();
        String username = dto.getEmail().split("@")[0].replaceAll("[^a-zA-Z0-9]", "");
        if (userRepository.existsByUsername(username)) {
            username = username + System.currentTimeMillis() % 1000;
        }

        user.setUsername(username);
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode("DefaultPass123!"));
        user.setRole(Role.ROLE_EMPLOYEE);

        User savedUser = userRepository.save(user);

        Employee employee = new Employee();
        employee.setEmployeeCode(dto.getEmployeeCode() != null ? dto.getEmployeeCode() : "EMP" + (1000 + savedUser.getId()));
        employee.setName(dto.getName());
        employee.setEmail(dto.getEmail());
        employee.setPhone(dto.getPhone());
        employee.setDepartment(dto.getDepartment());
        employee.setDateOfJoining(dto.getDateOfJoining());
        employee.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        employee.setUser(savedUser);

        Employee savedEmployee = employeeRepository.save(employee);
        return mapToDto(savedEmployee);
    }

    @Transactional
    public EmployeeDto updateEmployee(Long id, EmployeeDto dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        if (!employee.getEmail().equalsIgnoreCase(dto.getEmail()) && employeeRepository.existsByEmail(dto.getEmail())) {
            throw new ConflictException("Email already in use by another employee: " + dto.getEmail());
        }

        if (dto.getPhone() != null && !dto.getPhone().matches("^[0-9]{10}$")) {
            throw new BadRequestException("Phone number must be a valid 10-digit number");
        }

        employee.setName(dto.getName());
        employee.setEmail(dto.getEmail());
        employee.setPhone(dto.getPhone());
        employee.setDepartment(dto.getDepartment());
        employee.setDateOfJoining(dto.getDateOfJoining());
        if (dto.getStatus() != null) {
            employee.setStatus(dto.getStatus());
        }

        Employee updatedEmployee = employeeRepository.save(employee);
        return mapToDto(updatedEmployee);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        employeeRepository.delete(employee);
        if (employee.getUser() != null) {
            userRepository.delete(employee.getUser());
        }
    }

    public EmployeeDto mapToDto(Employee employee) {
        return new EmployeeDto(
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getName(),
                employee.getEmail(),
                employee.getPhone(),
                employee.getDepartment(),
                employee.getDateOfJoining(),
                employee.getStatus(),
                employee.getUser() != null ? employee.getUser().getId() : null
        );
    }
}
