# Healthcare Claims Management - API Testing Guide

**Project**: Healthcare Benefits & Claims Management System  
**Author**: Graduate Engineer Trainee / Software Testing & QA  

---

## 1. Overview & Postman Import Instructions

This document provides complete instructions for executing API automation and manual verification tests using Postman or cURL.

1. Open **Postman**.
2. Click **Import** -> Select `Healthcare-Claims-API.postman_collection.json` located inside `docs/`.
3. Set Environment Variable: `base_url` = `http://localhost:8080/api`.

---

## 2. API Endpoints Reference Matrix

| Endpoint | Method | Role Allowed | Description | Expected Status |
|---|---|---|---|---|
| `/api/auth/register` | POST | Public | Self-registration for employees | 201 Created |
| `/api/auth/login` | POST | Public | Authenticates credentials and returns JWT | 200 OK |
| `/api/employees` | GET | ADMIN, HR_OFFICER | List all employee records | 200 OK |
| `/api/employees` | POST | ADMIN | Create new employee profile | 201 Created |
| `/api/plans` | GET | Public | List all healthcare insurance plans | 200 OK |
| `/api/plans` | POST | ADMIN | Create new healthcare plan | 201 Created |
| `/api/plans/{id}/enroll/{empId}` | POST | ALL | Enroll employee in healthcare plan | 200 OK |
| `/api/claims` | GET | ADMIN, HR_OFFICER | Search & list claim filings | 200 OK |
| `/api/claims` | POST | EMPLOYEE, ADMIN | Submit new reimbursement claim | 201 Created |
| `/api/claims/{id}/review` | PATCH | HR_OFFICER, ADMIN | Move claim status to UNDER_REVIEW | 200 OK |
| `/api/claims/{id}/approve` | PATCH | HR_OFFICER, ADMIN | Approve claim payout | 200 OK |
| `/api/claims/{id}/reject` | PATCH | HR_OFFICER, ADMIN | Reject claim with officer remarks | 200 OK |

---

## 3. Sample cURL Command Examples

### A. User Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "AdminPass123!"}'
```

### B. Submit Healthcare Claim
```bash
curl -X POST http://localhost:8080/api/claims \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>" \
  -d '{
    "employeeId": 3,
    "planId": 1,
    "claimAmount": 1250.00,
    "claimDate": "2026-09-20",
    "claimType": "OPD",
    "description": "Annual diagnostic MRI scan",
    "supportingDocumentUrl": "/docs/receipt_001.pdf"
  }'
```

### C. Reject Claim (Negative Test Validation)
```bash
curl -X PATCH http://localhost:8080/api/claims/4/reject \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>" \
  -d '{"officerRemarks": "Elective cosmetic care excluded under Policy 4.2"}'
```
