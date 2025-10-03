-- ================================================================
-- Update Admin Password
-- Migration Version: V1.0.2
-- Description: Update admin user password to admin123
-- Password hash: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2ELM5u7Nwes7Gfv.MjYc66W
-- Plain text password: admin123
-- ================================================================

UPDATE tb_user
SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2ELM5u7Nwes7Gfv.MjYc66W',
    updated_at = CURRENT_TIMESTAMP
WHERE username = 'admin';
