# SQL verification (TASK-009 / TASK-010)

Run on 2026-11-09 against local MySQL 8.4.13, database `employee_directory`, freshly rebuilt from `db/schema.sql` + `db/seed.sql`.

## Rebuild + counts
`department=4, employee=16, employee_change=5` ✅

## Negative integrity checks (all rejected as expected)
| Attempt | Result |
|---|---|
| Employee with `department_id = 999` | `ERROR 1452 … fk_employee_department` ✅ |
| Email `AVERY.QUILL@example.test` (existing, different case) | `ERROR 1062 Duplicate entry … uk_employee_email` ✅ |
| Status `RETIRED` | `ERROR 3819 Check constraint 'ck_employee_status' is violated` ✅ |

## Two results checked by hand
1. **Headcount per department** returned Customer Success 0, Engineering 7, Finance 5, Human Resources 4.
   I counted the `department_id` values in `seed.sql`: employees 1–5, 15 and 16 are Engineering (7); 6–9 are HR (4); 10–14 are Finance (5); nobody is in Customer Success. The total is 16, which matches the employee count. ✅
2. **Hires in the last 90 days** returned Esme Thornbury (today −20 days), Hugo Ashdown (−45) and Mina Calloway (−75).
   Nico Ferreira (−120) is correctly excluded, and every other hire date is a fixed date in 2024 or earlier. ✅

Queries 3 to 5 also ran. Query 3 returned employee 11's HR→Finance + HR Assistant→Accountant change. Query 4 returned ACTIVE 12, ON_LEAVE 2, TERMINATED 2. Query 5 returned Customer Success.
