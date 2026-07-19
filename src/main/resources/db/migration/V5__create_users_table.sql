-- V5__create_users_table.sql
-- Authentication users — separate from employees by design
-- An employee is an HR record. A user is a system account.
-- They are linked but not the same concept.

CREATE TABLE users
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    email       VARCHAR(150)  NOT NULL UNIQUE,
    password    VARCHAR(255)  NOT NULL,
    role        VARCHAR(20)   NOT NULL DEFAULT 'EMPLOYEE',
    is_active   BOOLEAN       NOT NULL DEFAULT TRUE,
    employee_id BIGINT UNIQUE,
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
                                       ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_users_employee
        FOREIGN KEY (employee_id) REFERENCES employees (id)
            ON DELETE SET NULL,

    CONSTRAINT chk_user_role CHECK (role IN ('ADMIN', 'HR', 'EMPLOYEE'))
) COMMENT = 'System authentication accounts. Linked to employees but not identical.';

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_role  ON users (role);

-- Seed: create an ADMIN user account linked to no employee
-- Password is BCrypt hash of 'admin123' — we will hash it properly in the app
-- This is a dev-only seed. Production admins are created through onboarding.
INSERT INTO users (email, password, role, is_active, employee_id)
VALUES ('admin@hrms.com',
        '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj0og4NtrVq2',
        'ADMIN',
        TRUE,
        NULL);