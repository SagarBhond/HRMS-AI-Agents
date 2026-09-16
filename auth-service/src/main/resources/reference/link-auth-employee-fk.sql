-- Run this ONCE, manually, after BOTH services have started at least once
-- (so that `employee` (from mcp-server) and `auth_user` (from auth-service)
-- both exist in hrms_db). Hibernate ddl-auto=update on auth-service creates
-- the `employee_id` column but can't add a real FK constraint to a table
-- owned by a different service's entity model.

USE hrms_db;

ALTER TABLE auth_user
  ADD CONSTRAINT fk_auth_user_employee
  FOREIGN KEY (employee_id) REFERENCES employee(employee_id);
