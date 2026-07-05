-- V1__create_employees_table.sql
-- MySQL 8.0 syntax

CREATE TABLE employees
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100)   NOT NULL,
    email        VARCHAR(150)   NOT NULL UNIQUE,
    designation  VARCHAR(100)   NOT NULL,
    department   VARCHAR(50)    NOT NULL,
    salary       DECIMAL(15, 2) NOT NULL,
    joining_date DATE           NOT NULL,
    status       VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE',
    created_at   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP
                                         ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT chk_employee_status CHECK (status IN (
        'ACTIVE', 'INACTIVE', 'ON_LEAVE', 'RESIGNED', 'TERMINATED'
    )),
    CONSTRAINT chk_employee_salary CHECK (salary >= 0)
) COMMENT = 'Core employee master data for the HRMS';

CREATE INDEX idx_employees_department ON employees (department);
CREATE INDEX idx_employees_status     ON employees (status);