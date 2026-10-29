# 🏥 Healthcare Benefits & Claims Management System

> **A full-stack enterprise-grade portfolio project demonstrating Software Testing, QA Engineering, REST API development, Spring Boot security, and React.js dashboard development.**

---

## 📋 Project Overview

The **Healthcare Benefits & Claims Management System** is a complete CRUD-based enterprise web application that automates the lifecycle of medical insurance claims from submission through approval/rejection. It is designed as a **portfolio project for Graduate Engineer Trainee / Software Testing / QA roles**.

### Problem Statement

Manual claim processing in corporate healthcare departments is error-prone, lacks audit trails, and creates compliance risks. This system solves:
- Duplicate claim submissions leading to financial fraud
- Missing rejection reasons (audit compliance failures)
- No role-based access control on sensitive medical HR data
- Lack of real-time claim status visibility for employees

---

## ✨ Features

### Multi-Role Authentication
- 🔑 JWT token-based authentication
- Role-specific dashboards and API authorization
- BCrypt password hashing — zero plain-text password storage

### Admin Portal
- Manage employees, healthcare plans, and all claims
- Dashboard statistics with real-time counts
- Approve or reject any claim with full audit log

### Employee Self-Service
- Register and manage personal healthcare enrollment
- Submit reimbursement claims with supporting documents
- Track personal claim status history

### HR / Claims Officer
- Review pending claims and transition to UNDER_REVIEW
- Approve verified claims with approval notes
- Reject with mandatory rejection reason for compliance

### Claim Workflow Engine
```
EMPLOYEE SUBMITS CLAIM
        ↓
    PENDING  
        ↓ (HR reviews)
  UNDER_REVIEW
        ↓
APPROVED ← → REJECTED
```

---

## 🛠️ Technology Stack

| Layer | Technology | Version |
|---|---|---|
| **Frontend** | React.js + Vite | React 18, Vite 5 |
| **Routing** | React Router DOM | v6 |
| **Backend** | Spring Boot | 3.2.4 |
| **Language** | Java | 17 (LTS) |
| **ORM** | Spring Data JPA / Hibernate | 3.x |
| **Security** | Spring Security + JWT (jjwt) | 0.11.5 |
| **Database** | MySQL 8 / H2 (embedded dev) | 8.0+ |
| **Build Tool** | Maven | 3.9.x |
| **Unit Testing** | JUnit 5 + Mockito | 5.x |
| **API Testing** | Postman | 10+ |

---

## 🏗️ Architecture

```
healthcare-claims-management/
├── backend/                       # Spring Boot REST API
│   ├── src/main/java/com/healthcare/claims/
│   │   ├── controller/            # REST endpoint layer
│   │   ├── service/               # Business logic layer
│   │   ├── repository/            # Spring Data JPA interfaces
│   │   ├── entity/                # JPA entity models
│   │   ├── dto/                   # Data Transfer Objects
│   │   ├── security/              # JWT auth & filters
│   │   ├── exception/             # Custom exception handling
│   │   └── config/                # Security, CORS, seed data
│   └── src/test/                  # JUnit 5 + Mockito unit tests
│
├── frontend/                      # React 18 + Vite Dashboard
│   └── src/
│       ├── pages/                 # Page-level route components
│       ├── components/            # Reusable UI components
│       ├── services/              # API fetch layer
│       └── context/               # Auth state management
│
└── docs/                          # QA & Testing Documentation
    ├── test-cases.md              # 32 manual test cases
    ├── bug-report.md              # 5 defect reports
    ├── sql-test-queries.sql       # 10 database validation queries
    ├── api-testing.md             # API testing guide
    └── Healthcare-Claims-API.postman_collection.json
```

---

## 🗄️ Database Design

### Schema: `healthcare_claims_db`

**Table Relationships:**
```
users (1) ──── (1) employees
employees (N) ──── (N) healthcare_plans  [via employee_plans]
employees (1) ──── (N) claims
healthcare_plans (1) ──── (N) claims
claims (1) ──── (N) claim_documents
claims (1) ──── (N) claim_remarks
```

**Tables:**
| Table | Key Columns | Purpose |
|---|---|---|
| `users` | id, username, email, password (BCrypt), role | Authentication & authorization |
| `employees` | id, employee_code, name, department, user_id | Employee profiles |
| `healthcare_plans` | id, plan_code, coverage_amount, premium, active | Insurance packages |
| `employee_plans` | employee_id, plan_id, status | Plan enrollment junction |
| `claims` | claim_number, claim_amount, status, officer_remarks | Claims workflow |
| `claim_documents` | claim_id, file_url | Supporting documents |
| `claim_remarks` | claim_id, author_role, remark | Audit log trail |

---

## 📡 REST API Documentation

| Method | Endpoint | Role | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register employee account |
| POST | `/api/auth/login` | Public | Login and get JWT token |
| GET | `/api/employees` | ADMIN, HR_OFFICER | List all employees |
| POST | `/api/employees` | ADMIN | Create employee |
| PUT | `/api/employees/{id}` | ADMIN | Update employee |
| DELETE | `/api/employees/{id}` | ADMIN | Delete employee |
| GET | `/api/plans` | Public | List healthcare plans |
| POST | `/api/plans` | ADMIN | Create plan |
| POST | `/api/plans/{id}/enroll/{empId}` | All Auth | Enroll in plan |
| GET | `/api/claims` | ADMIN, HR_OFFICER | List all claims |
| POST | `/api/claims` | EMPLOYEE, ADMIN | Submit claim |
| GET | `/api/claims/{id}` | All Auth | View claim details |
| PATCH | `/api/claims/{id}/review` | HR_OFFICER, ADMIN | Move to UNDER_REVIEW |
| PATCH | `/api/claims/{id}/approve` | HR_OFFICER, ADMIN | Approve claim |
| PATCH | `/api/claims/{id}/reject` | HR_OFFICER, ADMIN | Reject claim (reason required) |
| GET | `/api/stats/dashboard` | All Auth | Dashboard statistics |

---

## 🧪 Testing Strategy

### A. Unit Tests (JUnit 5 + Mockito)

**20 tests across 4 service classes — all PASSED ✅**

| Test Class | Tests | Description |
|---|---|---|
| `AuthenticationServiceTest` | 4 | Login success, invalid login, registration, duplicate username |
| `ClaimServiceTest` | 7 | Valid claim, negative amount, duplicate claim, approve, reject, missing review, non-existent claim |
| `EmployeeServiceTest` | 4 | Create employee, duplicate email, get by ID, not found |
| `HealthcarePlanServiceTest` | 5 | Create plan, duplicate code, zero coverage, get by ID, not found |

Test pattern: **Arrange → Act → Assert**

```java
@Test
@DisplayName("Test 7: Invalid Claim Amount (Negative or Zero)")
void submitClaim_InvalidAmount_ThrowsBadRequest() {
    // Arrange
    ClaimRequestDto dto = new ClaimRequestDto(..., new BigDecimal("-500.00"), ...);

    // Act & Assert
    BadRequestException exception = assertThrows(BadRequestException.class, 
        () -> claimService.submitClaim(dto));
    assertTrue(exception.getMessage().contains("Claim amount must be greater than zero"));
    verify(claimRepository, never()).save(any());
}
```

### B. Manual Test Cases

**32 manual test cases** covering:
- Authentication & Registration (positive + negative)
- Employee CRUD (positive + negative + boundary)
- Healthcare Plan management
- Claims workflow (all status transitions)
- Security & authorization violations
- Boundary Value Analysis

### C. API Testing (Postman)

Import `docs/Healthcare-Claims-API.postman_collection.json` and run:
- Auth flows (register, login for all 3 roles)
- Employee CRUD scenarios
- Full claims workflow: submit → review → approve/reject

### D. SQL Database Validation

10 SQL queries in `docs/sql-test-queries.sql` for verifying:
- Employee records integrity
- Duplicate claim detection
- Claim status transition validation
- Total approved payout calculations
- Audit remarks trail

---

## 🐛 Key Bug Examples (Defect Identification)

| Bug ID | Issue | Severity | Root Cause Fixed |
|---|---|---|---|
| BUG-001 | System accepted negative claim amounts | High | Added `@DecimalMin("0.01")` + service validation |
| BUG-002 | Duplicate active claims allowed | High | Added `existsDuplicatePendingClaim` DB query check |
| BUG-003 | Employee could create healthcare plans (ADMIN-only) | Critical | Added `@PreAuthorize("hasRole('ADMIN')")` |
| BUG-004 | Invalid email format accepted | Medium | Added `@Email` validation annotation |
| BUG-005 | Rejection without mandatory reason allowed | High | Added blank-check in `rejectClaim` service method |

---

## 🚀 How to Run Locally

### Prerequisites

| Tool | Minimum Version |
|---|---|
| Java JDK | 17+ |
| Apache Maven | 3.8+ |
| Node.js | 18+ |
| MySQL | 8.0+ (optional — H2 used by default) |

---

### Step 1: Clone / Open the Project

```bash
# Navigate to project root
cd "C:\Users\Lokesh Yadav\OneDrive\Desktop\Wipro"
```

---

### Step 2: Start the Backend

The backend is pre-configured with **H2 in-memory database** — no MySQL setup needed for development.

```bash
cd backend
mvn spring-boot:run
```

The backend will start on **http://localhost:8080**

> H2 Console: **http://localhost:8080/h2-console**  
> JDBC URL: `jdbc:h2:mem:healthcare_claims_db`  
> Username: `sa` | Password: `password`

**Seed data is automatically loaded on startup:**

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `AdminPass123!` |
| HR Officer | `hrofficer` | `OfficerPass123!` |
| Employee | `employee1` | `EmpPass123!` |
| Employee | `employee2` | `EmpPass123!` |

---

### Step 3: Start the Frontend

```bash
cd frontend
npm install   # if not already done
npm run dev
```

The frontend will start on **http://localhost:3000**

---

### Step 4: Run Backend Unit Tests

```bash
cd backend
mvn test
```

Expected output:
```
Tests run: 20, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

### Step 5: MySQL Production Setup (Optional)

1. Create database in MySQL:
```sql
CREATE DATABASE healthcare_claims_db;
```

2. Edit `backend/src/main/resources/application.properties`:
```properties
# Comment out H2 lines and uncomment MySQL lines
spring.datasource.url=jdbc:mysql://localhost:3306/healthcare_claims_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
```

3. Run `docs/schema.sql` to create tables, then `backend/src/main/resources/data.sql` for sample data.

---

## 🔐 Environment Variables

| Variable | Default | Description |
|---|---|---|
| `app.jwt.secret` | Long secure key in properties | JWT signing key |
| `app.jwt.expiration-ms` | `86400000` (24h) | Token expiry in ms |
| `spring.datasource.url` | H2 in-memory | Database connection URL |
| `server.port` | `8080` | Backend server port |

---

## 📸 Application Pages

| Route | Page | Description |
|---|---|---|
| `/login` | Login Page | Quick role selection + JWT auth |
| `/register` | Registration | Employee self-registration form |
| `/dashboard` | Executive Dashboard | Stats cards + recent claims overview |
| `/employees` | Employee Directory | CRUD table for workforce management |
| `/plans` | Healthcare Plans | Benefit cards with enrollment action |
| `/claims` | Claims Directory | Full table with filter + workflow actions |
| `/claims/new` | Submit Claim | Validated claim filing form |
| `/claims/:id` | Claim Details | Full claim view with workflow buttons |
| `/profile` | User Profile | Personal details + claims history |

---

## 🎯 How This Project Demonstrates QA & Testing Skills

| QA Skill | Implementation |
|---|---|
| **Test Data Preparation** | 4 users, 4 employees, 3 plans, 4 seeded claims with different statuses |
| **Unit Testing** | 20 JUnit 5 tests with Mockito mocks (Arrange → Act → Assert pattern) |
| **API Testing** | Postman collection with positive + negative scenarios |
| **SQL / DB Validation** | 10 verification queries for claim status, duplicates, totals |
| **Defect Identification** | 5 documented bugs with root cause, severity, fix verification |
| **Boundary Value Analysis** | Claim amount = 0, 0.01, max coverage, max+1 |
| **Test Documentation** | 32 manual test cases with pass/fail status, severity, steps |
| **Error Handling** | Global `@RestControllerAdvice` with structured JSON responses |
| **Negative Testing** | Invalid amounts, unauthorized access, missing fields, duplicates |
| **Workflow Validation** | Enforced PENDING → UNDER_REVIEW → APPROVED/REJECTED state machine |

---

## 🚀 Future Enhancements

- [ ] Email notification on claim status change (Spring Mail)
- [ ] Document upload with cloud storage (AWS S3)
- [ ] PDF claim report generation (JasperReports)
- [ ] Docker containerization + docker-compose
- [ ] CI/CD Pipeline with GitHub Actions
- [ ] Integration test suite (Spring Boot Test + MockMvc)
- [ ] Analytics charts with Chart.js on dashboard
- [ ] Mobile responsive improvements

---

## 👨‍💻 Author

**Graduate Engineer Trainee | Software Testing & QA**  
*Built as a comprehensive portfolio demonstration for QA-oriented roles at enterprise software companies.*
