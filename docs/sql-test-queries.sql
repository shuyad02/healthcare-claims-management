-- Healthcare Benefits & Claims Management System
-- SQL Testing & Database Verification Queries Suite

USE `healthcare_claims_db`;

-- 1. Query: Verify all registered employees with linked user roles
SELECT 
    e.id AS employee_db_id,
    e.employee_code,
    e.name AS employee_name,
    e.email,
    e.department,
    e.status AS employee_status,
    u.username,
    u.role
FROM employees e
INNER JOIN users u ON e.user_id = u.id
ORDER BY e.id ASC;

-- 2. Query: Check for duplicate claims submitted by same employee for identical plan and amount
SELECT 
    employee_id,
    plan_id,
    claim_amount,
    COUNT(*) AS duplicate_count
FROM claims
WHERE status IN ('PENDING', 'UNDER_REVIEW')
GROUP BY employee_id, plan_id, claim_amount
HAVING COUNT(*) > 1;

-- 3. Query: Count of claims grouped by status (PENDING, UNDER_REVIEW, APPROVED, REJECTED)
SELECT 
    status,
    COUNT(*) AS total_claims,
    SUM(claim_amount) AS total_amount
FROM claims
GROUP BY status;

-- 4. Query: Retrieve all APPROVED claims with employee & plan details
SELECT 
    c.claim_number,
    e.name AS employee_name,
    e.department,
    p.plan_name,
    c.claim_amount,
    c.claim_date,
    c.officer_remarks
FROM claims c
JOIN employees e ON c.employee_id = e.id
JOIN healthcare_plans p ON c.plan_id = p.id
WHERE c.status = 'APPROVED'
ORDER BY c.claim_date DESC;

-- 5. Query: Retrieve all REJECTED claims and verify rejection reason presence
SELECT 
    c.claim_number,
    e.name AS employee_name,
    p.plan_name,
    c.claim_amount,
    c.officer_remarks AS rejection_reason
FROM claims c
JOIN employees e ON c.employee_id = e.id
JOIN healthcare_plans p ON c.plan_id = p.id
WHERE c.status = 'REJECTED';

-- 6. Query: Calculate overall total claim amount and total approved reimbursement
SELECT 
    COUNT(*) AS total_claims_filed,
    SUM(claim_amount) AS grand_total_filed_amount,
    SUM(CASE WHEN status = 'APPROVED' THEN claim_amount ELSE 0 END) AS total_approved_payout,
    SUM(CASE WHEN status = 'REJECTED' THEN claim_amount ELSE 0 END) AS total_rejected_amount,
    SUM(CASE WHEN status = 'PENDING' THEN claim_amount ELSE 0 END) AS total_pending_amount
FROM claims;

-- 7. Query: Retrieve claims filed by a specific employee (e.g. Employee Code 'EMP1001')
SELECT 
    c.id AS claim_id,
    c.claim_number,
    c.claim_type,
    c.claim_amount,
    c.claim_date,
    c.status,
    c.officer_remarks
FROM claims c
JOIN employees e ON c.employee_id = e.id
WHERE e.employee_code = 'EMP1001'
ORDER BY c.claim_date DESC;

-- 8. Query: Retrieve claims submitted within a specific date range
SELECT 
    c.claim_number,
    e.name AS employee_name,
    c.claim_type,
    c.claim_amount,
    c.claim_date,
    c.status
FROM claims c
JOIN employees e ON c.employee_id = e.id
WHERE c.claim_date BETWEEN '2026-08-01' AND '2026-09-30'
ORDER BY c.claim_date DESC;

-- 9. Query: Verify active employee healthcare plan enrollments
SELECT 
    e.employee_code,
    e.name AS employee_name,
    p.plan_code,
    p.plan_name,
    p.coverage_amount,
    ep.enrollment_date,
    ep.status AS enrollment_status
FROM employee_plans ep
JOIN employees e ON ep.employee_id = e.id
JOIN healthcare_plans p ON ep.plan_id = p.id
WHERE ep.status = 'ENROLLED';

-- 10. Query: Audit trail of remarks logged for a claim
SELECT 
    r.id,
    c.claim_number,
    r.author_name,
    r.author_role,
    r.remark,
    r.created_at
FROM claim_remarks r
JOIN claims c ON r.claim_id = c.id
ORDER BY r.created_at DESC;
