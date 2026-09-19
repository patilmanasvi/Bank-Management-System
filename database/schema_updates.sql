-- =======================================================
-- SCHEMA UPDATE: Adds 'FROZEN' as a valid account status
-- Required for: Admin/Employee Panel - Freeze/Activate feature
-- Run this once against the existing bank_db database.
-- =======================================================

USE bank_db;

ALTER TABLE account DROP CHECK chk_account_status;

ALTER TABLE account
  ADD CONSTRAINT chk_account_status
  CHECK (status IN ('ACTIVE', 'FROZEN', 'DORMANT', 'CLOSED'));
