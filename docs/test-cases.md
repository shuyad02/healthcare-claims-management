# Healthcare Benefits & Claims Management System - Manual Test Suite

**Project**: Healthcare Benefits & Claims Management System  
**Author**: Graduate Engineer Trainee (QA & Software Testing)  
**Version**: 1.0.0  
**Total Test Cases**: 32  

---

## Test Execution Matrix

| Test Case ID | Module | Test Scenario | Precondition | Test Steps | Test Data | Expected Result | Status | Severity |
|---|---|---|---|---|---|---|---|---|
| **TC-AUTH-001** | Authentication | Successful User Registration | User not registered | 1. Navigate to `/register`<br>2. Fill valid employee details<br>3. Click "Complete Registration" | Username: `testuser1`<br>Email: `test1@healthcare.com`<br>Phone: `9876543210` | User account & employee record created. JWT token returned. Redirected to `/dashboard`. | PASS | High |
| **TC-AUTH-002** | Authentication | Registration with Duplicate Email | Email already exists | 1. Navigate to `/register`<br>2. Enter existing email `john.doe@healthcare.com`<br>3. Submit | Email: `john.doe@healthcare.com` | Error banner displayed: "Email is already registered" (HTTP 409 Conflict). | PASS | High |
| **TC-AUTH-003** | Authentication | Registration with Invalid Phone Number | None | 1. Navigate to `/register`<br>2. Enter 8-digit phone number<br>3. Submit | Phone: `98765432` | Validation error: "Phone must be a valid 10-digit number" (HTTP 400 Bad Request). | PASS | Medium |
| **TC-AUTH-004** | Authentication | Successful User Login (Admin Role) | Admin account active | 1. Navigate to `/login`<br>2. Enter admin credentials<br>3. Click "Sign In" | Username: `admin`<br>Password: `AdminPass123!` | Login successful. Admin role badge visible on topbar. Redirected to `/dashboard`. | PASS | High |
| **TC-AUTH-005** | Authentication | Login with Invalid Password | Account exists | 1. Navigate to `/login`<br>2. Enter correct username & wrong pass<br>3. Submit | Username: `admin`<br>Password: `WrongPass!` | Error displayed: "Invalid username or password" (HTTP 401 Unauthorized). | PASS | High |
| **TC-AUTH-006** | Authentication | Login with Non-Existing User | User does not exist | 1. Navigate to `/login`<br>2. Enter unregistered username<br>3. Submit | Username: `ghost_user`<br>Password: `Pass123!` | Error displayed: "Invalid username or password" (HTTP 401 Unauthorized). | PASS | High |
| **TC-EMP-001** | Employee Management | Admin Views Employee List | Logged in as Admin | 1. Navigate to `/employees` | Role: ADMIN | Complete list of registered employees rendered with codes, departments, and status. | PASS | Medium |
| **TC-EMP-002** | Employee Management | Admin Creates New Employee | Logged in as Admin | 1. Click "Add New Employee"<br>2. Fill valid form details<br>3. Click "Save Employee" | Name: `Robert Smith`<br>Email: `robert@healthcare.com`<br>Phone: `9123456789` | Employee added to DB (HTTP 201 Created). Toast notification shown. | PASS | High |
| **TC-EMP-003** | Employee Management | Duplicate Employee Email Creation | Logged in as Admin | 1. Click "Add New Employee"<br>2. Enter existing email<br>3. Submit | Email: `admin@healthcare.com` | Conflict error: "Employee email already exists" (HTTP 409 Conflict). | PASS | High |
| **TC-EMP-004** | Employee Management | Admin Updates Employee Department | Logged in as Admin | 1. Click Edit icon on employee row<br>2. Change department to QA<br>3. Save | Dept: `Quality Assurance` | Record updated in DB (HTTP 200 OK). Updated dept visible in UI. | PASS | Medium |
| **TC-EMP-005** | Employee Management | Admin Deletes Employee | Logged in as Admin | 1. Click Delete icon<br>2. Confirm deletion dialog | Employee ID: `4` | Employee record & associated user removed (HTTP 204 No Content). | PASS | High |
| **TC-EMP-006** | Employee Management | Unauthorized Employee Management Access | Logged in as Employee | 1. Send `POST /api/employees` using Employee JWT token | Role: EMPLOYEE | Access Denied: Forbidden error returned (HTTP 403 Forbidden). | PASS | High |
| **TC-PLAN-001** | Healthcare Plans | View Active Healthcare Plans | Logged in as any user | 1. Navigate to `/plans` | None | Active plans displayed with coverage ceilings, premiums, and eligibility criteria. | PASS | High |
| **TC-PLAN-002** | Healthcare Plans | Admin Creates Healthcare Plan | Logged in as Admin | 1. Click "Create New Plan"<br>2. Fill valid plan details<br>3. Click Publish | Code: `PLAN-DIAMOND-04`<br>Coverage: `75000`<br>Premium: `300` | New plan saved (HTTP 201 Created) and listed under Active Plans. | PASS | High |
| **TC-PLAN-003** | Healthcare Plans | Create Plan with Zero Coverage Amount | Logged in as Admin | 1. Click "Create New Plan"<br>2. Enter `0` as coverage amount<br>3. Submit | Coverage Amount: `0.00` | Validation error: "Coverage amount must be greater than zero" (HTTP 400 Bad Request). | PASS | Medium |
| **TC-PLAN-004** | Healthcare Plans | Create Plan with Negative Premium | Logged in as Admin | 1. Click "Create New Plan"<br>2. Enter `-50` premium<br>3. Submit | Premium: `-50.00` | Validation error: "Premium cannot be negative" (HTTP 400 Bad Request). | PASS | Medium |
| **TC-PLAN-005** | Healthcare Plans | Employee Enrolls in Plan | Logged in as Employee | 1. Navigate to `/plans`<br>2. Click "Enroll in this Plan" | Plan ID: `1` | Enrollment record saved into `employee_plans` table. Success toast displayed. | PASS | High |
| **TC-CLM-001** | Claim Management | Valid Claim Submission | Enrolled in active plan | 1. Navigate to `/claims/new`<br>2. Select Plan Gold<br>3. Amount: $1,250<br>4. Submit | Amount: `1250.00`<br>Type: `OPD` | Claim created in `PENDING` status with generated code `CLM-2026-XXXX` (HTTP 201 Created). | PASS | High |
| **TC-CLM-002** | Claim Management | Submit Claim with Zero Amount | Logged in as Employee | 1. Navigate to `/claims/new`<br>2. Enter `0` as claim amount<br>3. Submit | Amount: `0.00` | Error: "Claim amount must be greater than zero" (HTTP 400 Bad Request). | PASS | High |
| **TC-CLM-003** | Claim Management | Submit Claim with Negative Amount | Logged in as Employee | 1. Navigate to `/claims/new`<br>2. Enter `-250` claim amount<br>3. Submit | Amount: `-250.00` | Error: "Claim amount must be greater than zero" (HTTP 400 Bad Request). | PASS | High |
| **TC-CLM-004** | Claim Management | Submit Claim Exceeding Plan Coverage Ceiling | Logged in as Employee | 1. Select Plan Silver (Coverage $25,000)<br>2. Enter claim amount $35,000<br>3. Submit | Amount: `35000.00` | Validation error: "Claim amount ($35,000) exceeds plan coverage limit ($25,000)" (HTTP 400 Bad Request). | PASS | High |
| **TC-CLM-005** | Claim Management | Submit Duplicate Active Claim | Active claim exists | 1. Submit claim for $1,250 on Plan Gold<br>2. Submit identical claim again while pending | Amount: `1250.00`<br>Plan ID: `1` | Conflict error: "Employee cannot submit duplicate claim with identical plan and amount while processing" (HTTP 409 Conflict). | PASS | High |
| **TC-CLM-006** | Claim Management | Move Claim to UNDER_REVIEW | Logged in as HR Officer | 1. Navigate to `/claims`<br>2. Click "Review" on Pending claim<br>3. Confirm | Claim ID: `1` | Claim status transitions `PENDING` -> `UNDER_REVIEW`. Remark entry logged. | PASS | High |
| **TC-CLM-007** | Claim Management | Approve Claim After Review | Claim status `UNDER_REVIEW` | 1. Click "Approve" on reviewed claim<br>2. Enter approval notes<br>3. Confirm | Officer Remarks: `Verified with hospital` | Claim status updated to `APPROVED`. Total approved claim amount updated in dashboard. | PASS | High |
| **TC-CLM-008** | Claim Management | Approve Claim Without Prior Review | Claim status `PENDING` | 1. Send `PATCH /api/claims/1/approve` directly while status is PENDING | Status: `PENDING` | Workflow Exception: "Claim cannot be approved without prior review" (HTTP 400 Bad Request). | PASS | High |
| **TC-CLM-009** | Claim Management | Reject Claim with Valid Reason | Claim active | 1. Click "Reject" on claim<br>2. Enter rejection reason<br>3. Confirm | Remarks: `Cosmetic treatment excluded under Policy 4.2` | Claim status updated to `REJECTED`. Officer remarks persisted. | PASS | High |
| **TC-CLM-010** | Claim Management | Reject Claim Without Rejection Reason | Claim active | 1. Click "Reject" on claim<br>2. Leave officer remarks empty<br>3. Submit | Remarks: `""` | Error: "Rejected claim must contain a rejection reason in officer remarks" (HTTP 400 Bad Request). | PASS | High |
| **TC-CLM-011** | Claim Management | Access Non-Existing Claim ID | Logged in user | 1. Request `GET /api/claims/9999` | Claim ID: `9999` | Error response: "Claim not found with id: 9999" (HTTP 404 Not Found). | PASS | Medium |
| **TC-SEC-001** | Security & Auth | Access Protected API Without JWT | Unauthenticated | 1. Send `GET /api/claims` without `Authorization` header | Header: None | Error response: HTTP 401 Unauthorized. | PASS | High |
| **TC-SEC-002** | Security & Auth | Employee Access Admin Endpoint | Logged in as Employee | 1. Send `POST /api/plans` using Employee token | Role: EMPLOYEE | Error response: HTTP 403 Forbidden. | PASS | High |
| **TC-DASH-001** | Dashboard & Stats | Dashboard Statistics Calculation | Multiple claims present | 1. Navigate to `/dashboard`<br>2. Verify total counts against DB | DB State | Stats show exact counts of employees, active plans, pending, approved, rejected claims & total amount. | PASS | High |
| **TC-DASH-002** | Dashboard & Stats | Filter Claims by Status | Logged in as HR Officer | 1. Select "APPROVED" status dropdown filter on Claims page | Status: `APPROVED` | Table displays only claims with status `APPROVED`. | PASS | Medium |

---

## Boundary Value Analysis (BVA) Summary

- **Claim Amount Boundary Tests**:
  - `0.00`: Rejected (HTTP 400)
  - `0.01`: Minimum valid claim accepted (HTTP 201)
  - `$50,000.00`: Exact coverage limit accepted (HTTP 201)
  - `$50,000.01`: Exceeds coverage limit, rejected (HTTP 400)
- **Phone Number Boundary Tests**:
  - `9 digits`: Rejected (HTTP 400)
  - `10 digits`: Accepted (HTTP 200/201)
  - `11 digits`: Rejected (HTTP 400)
