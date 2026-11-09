-- One-time migration for an EXISTING local database created before archiving was added.
-- (A fresh database built from db/schema.sql already has these columns; do not run it there.)
--   mysql -u root employee_directory < db/migrations/2026-11-09-employee-archive.sql
ALTER TABLE employee
    ADD COLUMN archived_at TIMESTAMP    NULL AFTER status,
    ADD COLUMN archived_by VARCHAR(128) NULL AFTER archived_at;
