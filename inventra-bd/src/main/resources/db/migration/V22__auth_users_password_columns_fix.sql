ALTER TABLE auth.users
    ADD COLUMN IF NOT EXISTS must_change_password BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE auth.users
    ADD COLUMN IF NOT EXISTS last_password_changed_at TIMESTAMP;

UPDATE auth.users
SET last_password_changed_at = COALESCE(last_password_changed_at, created_at, NOW())
WHERE last_password_changed_at IS NULL;