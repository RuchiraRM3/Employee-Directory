-- Fake seed data only. No real employee or customer data.
-- Run after schema.sql:  mysql -u root employee_directory < db/seed.sql
-- Some hire dates are relative to today so the "last 90 days" query
-- always has rows. "Customer Success" is intentionally left with zero
-- employees so the "departments with no employees" query has a result.

INSERT INTO department (id, name) VALUES
    (1, 'Engineering'),
    (2, 'Human Resources'),
    (3, 'Finance'),
    (4, 'Customer Success');

INSERT INTO employee (id, name, email, job_title, department_id, hire_date, status) VALUES
    (1,  'Avery Quill',      'avery.quill@example.test',      'Senior Software Engineer', 1, '2021-03-15', 'ACTIVE'),
    (2,  'Bram Holloway',    'bram.holloway@example.test',    'Software Engineer',        1, '2023-07-01', 'ACTIVE'),
    (3,  'Cleo Marsh',       'cleo.marsh@example.test',       'QA Engineer',              1, '2022-11-20', 'ON_LEAVE'),
    (4,  'Dorian Vale',      'dorian.vale@example.test',      'Engineering Manager',      1, '2019-05-06', 'ACTIVE'),
    (5,  'Esme Thornbury',   'esme.thornbury@example.test',   'Software Engineer',        1, CURDATE() - INTERVAL 20 DAY, 'ACTIVE'),
    (6,  'Felix Granger',    'felix.granger@example.test',    'HR Executive',             2, '2020-09-14', 'ACTIVE'),
    (7,  'Greta Lindqvist',  'greta.lindqvist@example.test',  'HR Business Partner',      2, '2022-02-28', 'ACTIVE'),
    (8,  'Hugo Ashdown',     'hugo.ashdown@example.test',     'Recruiter',                2, CURDATE() - INTERVAL 45 DAY, 'ACTIVE'),
    (9,  'Isla Penrose',     'isla.penrose@example.test',     'HR Assistant',             2, '2018-01-08', 'TERMINATED'),
    (10, 'Jonah Featherby',  'jonah.featherby@example.test',  'Financial Analyst',        3, '2021-06-21', 'ACTIVE'),
    (11, 'Kira Odell',       'kira.odell@example.test',       'Accountant',               3, '2023-10-02', 'ACTIVE'),
    (12, 'Leon Brightwater', 'leon.brightwater@example.test', 'Finance Manager',          3, '2017-04-17', 'ON_LEAVE'),
    (13, 'Mina Calloway',    'mina.calloway@example.test',    'Payroll Specialist',       3, CURDATE() - INTERVAL 75 DAY, 'ACTIVE'),
    (14, 'Nico Ferreira',    'nico.ferreira@example.test',    'Junior Accountant',        3, CURDATE() - INTERVAL 120 DAY, 'ACTIVE'),
    (15, 'Opal Whitcombe',   'opal.whitcombe@example.test',   'DevOps Engineer',          1, '2020-12-01', 'TERMINATED'),
    (16, 'Piers Danvers',    'piers.danvers@example.test',    'Software Engineer',        1, '2024-04-15', 'ACTIVE');

-- Change history: each row records old/new department and/or job title.
INSERT INTO employee_change
    (employee_id, old_department_id, new_department_id, old_job_title, new_job_title, changed_at, changed_by) VALUES
    (1,  NULL, NULL, 'Software Engineer', 'Senior Software Engineer', '2023-04-01 09:00:00', 'seed'),
    (7,  3,    2,    NULL,                NULL,                       '2023-08-15 10:30:00', 'seed'),
    (4,  NULL, NULL, 'Senior Software Engineer', 'Engineering Manager', '2022-01-10 14:00:00', 'seed'),
    (15, NULL, NULL, 'Systems Administrator', 'DevOps Engineer',      '2021-05-05 11:00:00', 'seed'),
    (11, 2,    3,    'HR Assistant',      'Accountant',               '2024-02-20 16:45:00', 'seed');
