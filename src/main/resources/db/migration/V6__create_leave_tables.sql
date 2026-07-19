-- V6__create_leave_tables.sql

CREATE TABLE leave_requests
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT       NOT NULL,
    approved_by BIGINT,
    leave_type  VARCHAR(20)  NOT NULL,
    start_date  DATE         NOT NULL,
    end_date    DATE         NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    reason      TEXT,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                      ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_leave_employee
        FOREIGN KEY (employee_id) REFERENCES employees (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_leave_approved_by
        FOREIGN KEY (approved_by) REFERENCES employees (id)
            ON DELETE SET NULL,

    CONSTRAINT chk_leave_type CHECK (leave_type IN (
        'ANNUAL', 'SICK', 'CASUAL', 'MATERNITY', 'PATERNITY', 'UNPAID'
    )),

    CONSTRAINT chk_leave_status CHECK (status IN (
        'PENDING', 'APPROVED', 'REJECTED', 'CANCELLED'
    )),

    CONSTRAINT chk_leave_dates CHECK (end_date >= start_date)
) COMMENT = 'Employee leave requests with approval workflow';

CREATE INDEX idx_leave_employee    ON leave_requests (employee_id);
CREATE INDEX idx_leave_status      ON leave_requests (status);
CREATE INDEX idx_leave_dates       ON leave_requests (start_date, end_date);
CREATE INDEX idx_leave_approved_by ON leave_requests (approved_by);

-- Leave balances — pre-computed remaining days per employee per leave type per year
-- Denormalised for read performance on the leave dashboard
CREATE TABLE leave_balances
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT      NOT NULL,
    leave_type  VARCHAR(20) NOT NULL,
    year        INT         NOT NULL,
    total_days  INT         NOT NULL DEFAULT 0,
    used_days   INT         NOT NULL DEFAULT 0,
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
                                     ON UPDATE CURRENT_TIMESTAMP,

    -- One balance record per employee per leave type per year
    CONSTRAINT uq_leave_balance UNIQUE (employee_id, leave_type, year),

    CONSTRAINT fk_balance_employee
        FOREIGN KEY (employee_id) REFERENCES employees (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_balance_leave_type CHECK (leave_type IN (
        'ANNUAL', 'SICK', 'CASUAL', 'MATERNITY', 'PATERNITY', 'UNPAID'
    )),

    CONSTRAINT chk_used_days CHECK (used_days >= 0),
    CONSTRAINT chk_total_days CHECK (total_days >= 0)
) COMMENT = 'Pre-computed leave balances per employee per year';

CREATE INDEX idx_balance_employee ON leave_balances (employee_id);
CREATE INDEX idx_balance_year     ON leave_balances (year);

-- Seed leave balances for 2025 for all active employees
-- Standard company policy: 18 annual, 12 sick, 6 casual days per year
INSERT INTO leave_balances (employee_id, leave_type, year, total_days, used_days)
SELECT e.id, 'ANNUAL',  2026, 18, 0 FROM employees e WHERE e.status = 'ACTIVE'
UNION ALL
SELECT e.id, 'SICK',    2026, 12, 0 FROM employees e WHERE e.status = 'ACTIVE'
UNION ALL
SELECT e.id, 'CASUAL',  2026,  6, 0 FROM employees e WHERE e.status = 'ACTIVE';