-- V3__create_departments_table.sql
-- Converts Department from application-level enum to a database table
-- Business reason: HR must be able to add new departments without a code deployment

CREATE TABLE departments
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                      ON UPDATE CURRENT_TIMESTAMP
) COMMENT = 'Business departments. Managed by HR, not by developers.';

-- Seed departments matching the current enum values exactly
-- Names must match the VARCHAR values already in the employees.department column
-- V4 migration will use these names to set department_id on existing employees
INSERT INTO departments (name, description)
VALUES ('ENGINEERING',     'Software engineering and product development'),
       ('HUMAN_RESOURCES', 'People operations, hiring, and employee relations'),
       ('FINANCE',         'Finance, accounting, and payroll'),
       ('MARKETING',       'Marketing, growth, and brand'),
       ('OPERATIONS',      'Business operations and project management'),
       ('LEGAL',           'Legal, compliance, and contracts');