-- V2__seed_employee_data.sql
-- Development seed data — gives us realistic data to query against

INSERT IGNORE INTO employees (name, email, designation, department, salary, joining_date, status)
VALUES
('Priya Sharma',    'priya@hrms.com',     'HR Manager',              'HUMAN_RESOURCES', 75000.00, '2021-06-01', 'ACTIVE'),
('Rohit Verma',     'rohit@hrms.com',     'Finance Analyst',         'FINANCE',         65000.00, '2023-03-20', 'ON_LEAVE'),
('Ananya Singh',    'ananya@hrms.com',    'Senior Engineer',         'ENGINEERING',    120000.00, '2019-09-10', 'ACTIVE'),
('Vikram Mehta',    'vikram@hrms.com',    'Marketing Lead',          'MARKETING',       70000.00, '2020-11-05', 'ACTIVE'),
('Neha Kulkarni',   'neha@hrms.com',      'Legal Counsel',           'LEGAL',           90000.00, '2020-03-15', 'ACTIVE'),
('Arjun Patel',     'arjun@hrms.com',     'DevOps Engineer',         'ENGINEERING',     85000.00, '2021-08-20', 'ACTIVE'),
('Deepika Rao',     'deepika@hrms.com',   'Product Manager',         'OPERATIONS',      95000.00, '2018-12-01', 'ACTIVE'),
('Suresh Kumar',    'suresh@hrms.com',    'Junior Engineer',         'ENGINEERING',     45000.00, '2024-02-10', 'ACTIVE'),
('Kavita Joshi',    'kavita@hrms.com',    'Recruitment Specialist',  'HUMAN_RESOURCES', 55000.00, '2023-07-01', 'RESIGNED');