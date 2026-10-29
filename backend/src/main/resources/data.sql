-- Seed Data for Healthcare Benefits & Claims Management System

-- Insert Users (Passwords are BCrypt hashed for 'AdminPass123!', 'OfficerPass123!', 'EmpPass123!')
INSERT INTO `users` (`id`, `username`, `email`, `password`, `role`, `created_at`) VALUES
(1, 'admin', 'admin@healthcare.com', '$2a$10$eD2gqA4B4VjF5LdE3y/bO.OqG2kR6wL3xH4N5M6P7Q8R9S0T1U2V3', 'ROLE_ADMIN', NOW()),
(2, 'hrofficer', 'hr@healthcare.com', '$2a$10$eD2gqA4B4VjF5LdE3y/bO.OqG2kR6wL3xH4N5M6P7Q8R9S0T1U2V3', 'ROLE_HR_OFFICER', NOW()),
(3, 'employee1', 'john.doe@healthcare.com', '$2a$10$eD2gqA4B4VjF5LdE3y/bO.OqG2kR6wL3xH4N5M6P7Q8R9S0T1U2V3', 'ROLE_EMPLOYEE', NOW()),
(4, 'employee2', 'jane.smith@healthcare.com', '$2a$10$eD2gqA4B4VjF5LdE3y/bO.OqG2kR6wL3xH4N5M6P7Q8R9S0T1U2V3', 'ROLE_EMPLOYEE', NOW());

-- Insert Employees
INSERT INTO `employees` (`id`, `employee_code`, `name`, `email`, `phone`, `department`, `date_of_joining`, `status`, `user_id`) VALUES
(1, 'EMP1000', 'System Admin', 'admin@healthcare.com', '9876543212', 'Management', '2022-01-01', 'ACTIVE', 1),
(2, 'EMP1003', 'HR Officer Sarah', 'hr@healthcare.com', '9876543213', 'Human Resources', '2022-05-20', 'ACTIVE', 2),
(3, 'EMP1001', 'John Doe', 'john.doe@healthcare.com', '9876543210', 'Engineering', '2023-01-15', 'ACTIVE', 3),
(4, 'EMP1002', 'Jane Smith', 'jane.smith@healthcare.com', '9876543211', 'Quality Assurance', '2023-03-10', 'ACTIVE', 4);

-- Insert Healthcare Plans
INSERT INTO `healthcare_plans` (`id`, `plan_code`, `plan_name`, `description`, `coverage_amount`, `premium`, `eligibility`, `active`) VALUES
(1, 'PLAN-GOLD-01', 'Comprehensive Medical Gold', 'Full coverage for OPD, IPD, and dental care up to $50,000', 50000.00, 250.00, 'Full-time employees after probation', TRUE),
(2, 'PLAN-SILVER-02', 'Standard Care Silver', 'Standard coverage for hospitalization and emergencies up to $25,000', 25000.00, 120.00, 'All permanent employees', TRUE),
(3, 'PLAN-PLATINUM-03', 'Executive Platinum Care', 'Premium zero-deductible worldwide medical coverage up to $100,000', 100000.00, 450.00, 'Senior staff & executives', TRUE);

-- Insert Employee Plans
INSERT INTO `employee_plans` (`id`, `employee_id`, `plan_id`, `enrollment_date`, `status`) VALUES
(1, 3, 1, '2023-02-01', 'ENROLLED'),
(2, 4, 2, '2023-04-01', 'ENROLLED');

-- Insert Claims
INSERT INTO `claims` (`id`, `claim_number`, `employee_id`, `plan_id`, `claim_amount`, `claim_date`, `claim_type`, `description`, `supporting_document_url`, `status`, `officer_remarks`, `created_at`, `updated_at`) VALUES
(1, 'CLM-2026-0001', 3, 1, 1250.00, '2026-09-14', 'OPD Consultation & Diagnostics', 'Routine annual medical checkup and MRI scan', '/docs/receipt_001.pdf', 'PENDING', NULL, NOW(), NOW()),
(2, 'CLM-2026-0002', 4, 2, 3400.00, '2026-09-04', 'Dental Surgery', 'Root canal treatment and dental crown placement', '/docs/receipt_002.pdf', 'UNDER_REVIEW', 'Documents verified with hospital desk', NOW(), NOW()),
(3, 'CLM-2026-0003', 3, 1, 4800.00, '2026-08-25', 'Emergency Room', 'Emergency appendectomy procedures', '/docs/receipt_003.pdf', 'APPROVED', 'Verified and approved per policy limits', NOW(), NOW()),
(4, 'CLM-2026-0004', 4, 2, 15000.00, '2026-08-15', 'Cosmetic Surgery', 'Elective aesthetic procedure', '/docs/receipt_004.pdf', 'REJECTED', 'Elective cosmetic procedures are excluded under Plan Silver terms', NOW(), NOW());

-- Insert Claim Remarks
INSERT INTO `claim_remarks` (`id`, `claim_id`, `author_name`, `author_role`, `remark`, `created_at`) VALUES
(1, 1, 'John Doe', 'EMPLOYEE', 'Claim submitted for OPD diagnostic checkup.', NOW()),
(2, 2, 'HR Officer Sarah', 'HR/CLAIMS OFFICER', 'Invoices verified with Dental Clinic desk.', NOW()),
(3, 3, 'HR Officer Sarah', 'HR/CLAIMS OFFICER', 'Emergency hospitalization verified and approved.', NOW()),
(4, 4, 'HR Officer Sarah', 'HR/CLAIMS OFFICER', 'REJECTED: Policy Section 4.2 excludes non-prescribed elective cosmetic care.', NOW());
