# Healthcare Benefits & Claims System - QA Bug Reports & Defect Log

**Project**: Healthcare Benefits & Claims Management System  
**Author**: Graduate Engineer Trainee / Software Testing & QA  
**Environment**: Development & QA Staging (Windows 11 / Java 17 / MySQL 8 / React 18)  
**Total Defects Documented**: 5  

---

## Defect Summary Table

| Bug ID | Title | Module | Severity | Priority | Status |
|---|---|---|---|---|---|
| **BUG-001** | System accepts zero and negative claim amounts without server validation | Claim Management | High | High | Fixed |
| **BUG-002** | Employee can submit duplicate active claims for identical plan and amount | Claim Management | High | High | Fixed |
| **BUG-003** | Unauthorized employee user able to invoke admin endpoint `POST /api/plans` | Security / Authorization | Critical | High | Fixed |
| **BUG-004** | Invalid email format accepted during employee registration | Authentication | Medium | Medium | Fixed |
| **BUG-005** | HR Officer can reject claim without specifying mandatory rejection reason | Claim Management | High | High | Fixed |

---

## Detailed Bug Reports

### Bug ID: BUG-001
**Title**: System accepts zero and negative claim amounts without server validation  
**Module**: Claim Management  
**Severity**: High  
**Priority**: High  
**Environment**: QA Environment (Spring Boot 3.2 / React 18)  
**Reporter**: QA Engineer Trainee  
**Status**: Fixed  

**Description**:  
When filing a claim via `POST /api/claims`, passing a negative claim amount (e.g. `-500.00`) or zero (`0.00`) was accepted by the server, resulting in corrupted total claim calculation logic.

**Preconditions**:  
User is logged in as an active employee.

**Steps to Reproduce**:
1. Open API Client / Postman or Navigate to `/claims/new`.
2. Select an active healthcare plan (e.g. Plan Gold).
3. Enter Claim Amount: `-500.00`.
4. Submit claim payload to `POST /api/claims`.

**Expected Result**:  
Server rejects request with HTTP 400 Bad Request and error message: `"Claim amount must be greater than zero"`.

**Actual Result**:  
Server saved claim with negative amount `-500.00` and returned HTTP 201 Created.

**Root Cause**:  
Missing `@DecimalMin(value = "0.01")` annotation on `ClaimRequestDto` field `claimAmount` and missing service layer validation.

**Fix Verification**:  
Added `@DecimalMin("0.01")` in `ClaimRequestDto` and programmatic validation check `claimAmount.compareTo(BigDecimal.ZERO) <= 0` in `ClaimService.java`. Tested via `ClaimServiceTest.submitClaim_InvalidAmount_ThrowsBadRequest`.

---

### Bug ID: BUG-002
**Title**: Employee can submit duplicate active claims for identical plan and amount  
**Module**: Claim Management  
**Severity**: High  
**Priority**: High  
**Environment**: Staging  
**Reporter**: QA Engineer Trainee  
**Status**: Fixed  

**Description**:  
An employee could repeatedly submit duplicate claims with the exact same plan ID and claim amount while an existing claim was still pending processing.

**Preconditions**:  
An active pending claim exists for Employee ID 1 and Plan ID 1 for $1,250.00.

**Steps to Reproduce**:
1. Log in as `employee1`.
2. Navigate to `/claims/new`.
3. Submit a claim for $1,250.00 on Plan Gold.
4. Immediately submit an identical claim for $1,250.00 on Plan Gold again.

**Expected Result**:  
Server rejects duplicate request with HTTP 409 Conflict: `"Employee cannot submit duplicate claim with identical plan and amount while processing"`.

**Actual Result**:  
Multiple identical claims were created in `PENDING` state.

**Root Cause**:  
Missing duplicate claim check in `ClaimRepository` and `ClaimService`.

**Fix Verification**:  
Created `@Query` method `existsDuplicatePendingClaim` in `ClaimRepository` and added duplicate check in `ClaimService.java`. Tested via `ClaimServiceTest.submitClaim_DuplicateClaim_ThrowsConflict`.

---

### Bug ID: BUG-003
**Title**: Unauthorized employee user able to invoke admin endpoint `POST /api/plans`  
**Module**: Security / Authorization  
**Severity**: Critical  
**Priority**: High  
**Environment**: Staging  
**Reporter**: QA Engineer Trainee  
**Status**: Fixed  

**Description**:  
An authenticated user with role `ROLE_EMPLOYEE` was able to create new healthcare plans by sending a POST request to `/api/plans`.

**Preconditions**:  
User is authenticated with `ROLE_EMPLOYEE` JWT token.

**Steps to Reproduce**:
1. Log in as `employee1` and obtain JWT token.
2. Send HTTP `POST /api/plans` with payload `{"planName": "Malicious Plan", "coverageAmount": 999999}` using employee token.

**Expected Result**:  
Server returns HTTP 403 Forbidden with message `"Access Denied: You do not have permission to access this resource"`.

**Actual Result**:  
Request succeeded with HTTP 201 Created and new plan was created.

**Root Cause**:  
Endpoint `/api/plans` lacked `@PreAuthorize("hasRole('ADMIN')")` annotation and SecurityConfig route matcher was underspecified.

**Fix Verification**:  
Added `@PreAuthorize("hasRole('ADMIN')")` on `HealthcarePlanController.createPlan` and enforced `requestMatchers(HttpMethod.POST, "/api/plans/**").hasRole("ADMIN")` in `SecurityConfig.java`.

---

### Bug ID: BUG-004
**Title**: Invalid email format accepted during employee registration  
**Module**: Authentication  
**Severity**: Medium  
**Priority**: Medium  
**Environment**: Staging  
**Reporter**: QA Engineer Trainee  
**Status**: Fixed  

**Description**:  
Registration endpoint accepted malformed email strings such as `user@domain` or `user@` without proper domain extension validation.

**Steps to Reproduce**:
1. Navigate to `/register`.
2. Fill registration form with email: `invalidemail@`.
3. Submit form.

**Expected Result**:  
HTTP 400 Bad Request with validation message `"Email must be a valid email address"`.

**Actual Result**:  
Registration succeeded.

**Root Cause**:  
Missing `@Email` validation annotation on `RegisterRequest.email`.

**Fix Verification**:  
Added `@Email` annotation on DTO and validated email pattern.

---

### Bug ID: BUG-005
**Title**: HR Officer can reject claim without specifying mandatory rejection reason  
**Module**: Claim Management  
**Severity**: High  
**Priority**: High  
**Environment**: Staging  
**Reporter**: QA Engineer Trainee  
**Status**: Fixed  

**Description**:  
When setting a claim status to `REJECTED`, the officer was allowed to leave officer remarks empty, violating business audit rule requiring rejection reasons.

**Steps to Reproduce**:
1. Log in as `hrofficer`.
2. Navigate to `/claims`.
3. Click "Reject" on a claim.
4. Leave officer remarks empty and submit.

**Expected Result**:  
Server rejects action with HTTP 400 Bad Request: `"Rejected claim must contain a rejection reason in officer remarks"`.

**Actual Result**:  
Claim was marked `REJECTED` with `null` remarks.

**Root Cause**:  
Missing validation in `ClaimService.rejectClaim`.

**Fix Verification**:  
Added strict check in `ClaimService.rejectClaim`:
```java
if (reviewDto == null || reviewDto.getOfficerRemarks() == null || reviewDto.getOfficerRemarks().trim().isEmpty()) {
    throw new BadRequestException("Rejected claim must contain a rejection reason in officer remarks");
}
```
Tested via `ClaimServiceTest.rejectClaim_Success`.
