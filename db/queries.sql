-- Five required report queries. Run against the seeded LOCAL database:
--   mysql -u root employee_directory < db/queries.sql
-- Archived ("deleted") employees (archived_at IS NOT NULL) are left out of the
-- directory reports 1, 2, 4 and 5; their change history (query 3) is kept.

-- 1. Headcount per department.
--    LEFT JOIN so departments with zero employees still appear with 0.
--    Counts every employee regardless of status. The archive filter sits in the ON
--    clause (not WHERE) so departments with only archived employees still show 0.
SELECT d.id, d.name, COUNT(e.id) AS headcount
FROM department d
LEFT JOIN employee e ON e.department_id = d.id AND e.archived_at IS NULL
GROUP BY d.id, d.name
ORDER BY d.name;

-- 2. Hires in the last 90 days (inclusive of today).
--    Uses idx_employee_hire_date for the range scan.
SELECT e.id, e.name, e.hire_date, d.name AS department
FROM employee e
JOIN department d ON d.id = e.department_id
WHERE e.hire_date >= CURDATE() - INTERVAL 90 DAY
  AND e.archived_at IS NULL
ORDER BY e.hire_date DESC;

-- 3. One employee's change history, newest first (employee id 11 here).
--    LEFT JOINs because a title-only change has NULL department columns.
--    Uses idx_change_employee_time.
SELECT c.changed_at,
       od.name AS old_department,
       nd.name AS new_department,
       c.old_job_title,
       c.new_job_title,
       c.changed_by
FROM employee_change c
LEFT JOIN department od ON od.id = c.old_department_id
LEFT JOIN department nd ON nd.id = c.new_department_id
WHERE c.employee_id = 11
ORDER BY c.changed_at DESC;

-- 4. Employees by status: one count per status value.
SELECT e.status, COUNT(*) AS employees
FROM employee e
WHERE e.archived_at IS NULL
GROUP BY e.status
ORDER BY e.status;

-- 5. Departments with no employees (anti-join).
SELECT d.id, d.name
FROM department d
LEFT JOIN employee e ON e.department_id = d.id AND e.archived_at IS NULL
WHERE e.id IS NULL
ORDER BY d.name;
