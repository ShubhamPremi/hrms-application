-- V4__add_department_fk_to_employees.sql
-- Migrates employees.department VARCHAR → employees.department_id BIGINT FK
-- This migration is the reason V3 seed names must exactly match V2 seed data

-- Step 1: Add the new column, nullable first — we will fill it before making it NOT NULL
ALTER TABLE employees
    ADD COLUMN department_id BIGINT;

-- Step 2: Populate department_id by matching the existing string values
--         JOIN departments on the exact name string that V2 seeded
--         After this UPDATE, every employee row has a valid department_id
UPDATE employees e
    JOIN departments d ON d.name = e.department
SET e.department_id = d.id;

-- Step 3: Now that all rows have a value, enforce NOT NULL
ALTER TABLE employees
    MODIFY COLUMN department_id BIGINT NOT NULL;

-- Step 4: Add the foreign key constraint
--         ON DELETE RESTRICT → cannot delete a department that has employees
--         This protects data integrity — HR cannot accidentally delete Engineering
--         and orphan 50 employees
ALTER TABLE employees
    ADD CONSTRAINT fk_employees_department
        FOREIGN KEY (department_id) REFERENCES departments (id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE;

-- Step 5: Drop the old string column — it is now redundant
--         Must drop FK first if needed, but since we are dropping the old column
--         (not the FK column) this is safe
ALTER TABLE employees
    DROP COLUMN department;

-- Step 6: Add index for join performance
--         Every time we JOIN employees → departments this index is used
CREATE INDEX idx_employees_department_id ON employees (department_id);