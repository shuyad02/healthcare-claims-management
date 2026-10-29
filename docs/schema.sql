-- Healthcare Benefits & Claims Management System
-- Database Schema for MySQL 8.0

CREATE DATABASE IF NOT EXISTS `healthcare_claims_db`;
USE `healthcare_claims_db`;

-- Drop existing tables in reverse dependency order
DROP TABLE IF EXISTS `claim_remarks`;
DROP TABLE IF EXISTS `claim_documents`;
DROP TABLE IF EXISTS `claims`;
DROP TABLE IF EXISTS `employee_plans`;
DROP TABLE IF EXISTS `healthcare_plans`;
DROP TABLE IF EXISTS `employees`;
DROP TABLE IF EXISTS `users`;

-- 1. Users Table
CREATE TABLE `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `role` VARCHAR(30) NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_users_username` (`username`),
    INDEX `idx_users_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Employees Table
CREATE TABLE `employees` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `employee_code` VARCHAR(30) NOT NULL UNIQUE,
    `name` VARCHAR(100) NOT NULL,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `phone` VARCHAR(20) NOT NULL,
    `department` VARCHAR(50) NOT NULL,
    `date_of_joining` DATE NOT NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    `user_id` BIGINT UNIQUE,
    CONSTRAINT `fk_employee_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    INDEX `idx_employees_code` (`employee_code`),
    INDEX `idx_employees_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Healthcare Plans Table
CREATE TABLE `healthcare_plans` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `plan_code` VARCHAR(50) NOT NULL UNIQUE,
    `plan_name` VARCHAR(100) NOT NULL,
    `description` TEXT,
    `coverage_amount` DECIMAL(12,2) NOT NULL,
    `premium` DECIMAL(10,2) NOT NULL,
    `eligibility` VARCHAR(255),
    `active` BOOLEAN NOT NULL DEFAULT TRUE,
    INDEX `idx_plans_code` (`plan_code`),
    INDEX `idx_plans_active` (`active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Employee Plans Junction Table
CREATE TABLE `employee_plans` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `employee_id` BIGINT NOT NULL,
    `plan_id` BIGINT NOT NULL,
    `enrollment_date` DATE NOT NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'ENROLLED',
    CONSTRAINT `fk_employee_plan_emp` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_employee_plan_plan` FOREIGN KEY (`plan_id`) REFERENCES `healthcare_plans` (`id`) ON DELETE CASCADE,
    INDEX `idx_emp_plans_emp` (`employee_id`),
    INDEX `idx_emp_plans_plan` (`plan_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Claims Table
CREATE TABLE `claims` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `claim_number` VARCHAR(50) NOT NULL UNIQUE,
    `employee_id` BIGINT NOT NULL,
    `plan_id` BIGINT NOT NULL,
    `claim_amount` DECIMAL(12,2) NOT NULL,
    `claim_date` DATE NOT NULL,
    `claim_type` VARCHAR(50) NOT NULL,
    `description` TEXT,
    `supporting_document_url` VARCHAR(255),
    `status` VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    `officer_remarks` TEXT,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_claim_employee` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`),
    CONSTRAINT `fk_claim_plan` FOREIGN KEY (`plan_id`) REFERENCES `healthcare_plans` (`id`),
    INDEX `idx_claims_number` (`claim_number`),
    INDEX `idx_claims_status` (`status`),
    INDEX `idx_claims_employee` (`employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Claim Documents Table
CREATE TABLE `claim_documents` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `claim_id` BIGINT NOT NULL,
    `document_name` VARCHAR(100) NOT NULL,
    `document_type` VARCHAR(50) NOT NULL,
    `file_url` VARCHAR(255) NOT NULL,
    `uploaded_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_document_claim` FOREIGN KEY (`claim_id`) REFERENCES `claims` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Claim Remarks Table
CREATE TABLE `claim_remarks` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `claim_id` BIGINT NOT NULL,
    `author_name` VARCHAR(100) NOT NULL,
    `author_role` VARCHAR(50) NOT NULL,
    `remark` TEXT NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_remark_claim` FOREIGN KEY (`claim_id`) REFERENCES `claims` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
