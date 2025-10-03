-- Add version column to tb_user table for optimistic locking
ALTER TABLE tb_user ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE tb_user ADD COLUMN IF NOT EXISTS creator_id BIGINT;
ALTER TABLE tb_user ADD COLUMN IF NOT EXISTS updater_id BIGINT;
ALTER TABLE tb_user ADD COLUMN IF NOT EXISTS create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE tb_user ADD COLUMN IF NOT EXISTS update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Add User entity fields
ALTER TABLE tb_user ADD COLUMN IF NOT EXISTS department_id BIGINT;
ALTER TABLE tb_user ADD COLUMN IF NOT EXISTS position VARCHAR(100);
ALTER TABLE tb_user ADD COLUMN IF NOT EXISTS last_login_time TIMESTAMP;
ALTER TABLE tb_user ADD COLUMN IF NOT EXISTS last_login_ip VARCHAR(50);

-- Update existing records to have version = 0
UPDATE tb_user SET version = 0 WHERE version IS NULL;
