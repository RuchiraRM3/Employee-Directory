-- Employee Directory schema (MySQL 8).
-- Run against a LOCAL database you selected explicitly, e.g.:
--   mysql -u root employee_directory < db/schema.sql
-- This script DROPS and recreates the three tables, so it is rerunnable
-- and destructive. Never run it against a shared or production database.

DROP TABLE IF EXISTS employee_change;
DROP TABLE IF EXISTS employee;
DROP TABLE IF EXISTS department;

CREATE TABLE department (
    id   BIGINT       NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_department_name UNIQUE (name)
);

CREATE TABLE employee (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(160) NOT NULL,
    -- Default collation utf8mb4_0900_ai_ci is case-insensitive, so the unique
    -- key also rejects "A@x.test" when "a@x.test" exists.
    email         VARCHAR(254) NOT NULL,
    job_title     VARCHAR(120) NOT NULL,
    department_id BIGINT       NOT NULL,
    hire_date     DATE         NOT NULL,
    status        VARCHAR(30)  NOT NULL,
    -- "Delete" in the app archives instead: the row and its change history are kept.
    -- NULL = in the directory. The email stays reserved by the unique key.
    archived_at   TIMESTAMP    NULL,
    archived_by   VARCHAR(128) NULL, -- Firebase UID of the HR user who archived the employee
    PRIMARY KEY (id),
    CONSTRAINT uk_employee_email UNIQUE (email),
    -- Prevents employees pointing at a department that does not exist.
    -- MySQL also creates an index on department_id for this FK, which serves
    -- the department filter and the headcount query.
    CONSTRAINT fk_employee_department FOREIGN KEY (department_id) REFERENCES department (id),
    -- PROVISIONAL status values (implementation.md D-002).
    CONSTRAINT ck_employee_status CHECK (status IN ('ACTIVE', 'ON_LEAVE', 'TERMINATED'))
);

-- Supports "hires in the last 90 days" (range scan on hire_date).
CREATE INDEX idx_employee_hire_date ON employee (hire_date);

CREATE TABLE employee_change (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    employee_id       BIGINT       NOT NULL,
    old_department_id BIGINT       NULL,
    new_department_id BIGINT       NULL,
    old_job_title     VARCHAR(120) NULL,
    new_job_title     VARCHAR(120) NULL,
    changed_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    changed_by        VARCHAR(128) NULL, -- Firebase UID of the HR user, when known
    PRIMARY KEY (id),
    CONSTRAINT fk_change_employee       FOREIGN KEY (employee_id)       REFERENCES employee (id),
    CONSTRAINT fk_change_old_department FOREIGN KEY (old_department_id) REFERENCES department (id),
    CONSTRAINT fk_change_new_department FOREIGN KEY (new_department_id) REFERENCES department (id)
);

-- Supports "one employee's change history" ordered by time; it also covers
-- the employee_id foreign key, so MySQL does not add a separate index.
CREATE INDEX idx_change_employee_time ON employee_change (employee_id, changed_at);
