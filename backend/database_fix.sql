-- ========================================
-- ProManage 数据库Schema修复脚本
-- ========================================
-- 日期: 2025-10-09
-- 目的: 修复tb_user表缺失的字段
-- 执行方式: 在PostgreSQL中执行此脚本
-- ========================================

\c promanage

-- 1. 添加version字段（乐观锁）- 修改为BIGINT类型
ALTER TABLE tb_user ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
COMMENT ON COLUMN tb_user.version IS '乐观锁版本号';

-- 2. 添加department_id字段（部门ID）
ALTER TABLE tb_user ADD COLUMN IF NOT EXISTS department_id BIGINT;
COMMENT ON COLUMN tb_user.department_id IS '部门ID';

-- 3. 添加organization_id字段（组织ID）
ALTER TABLE tb_user ADD COLUMN IF NOT EXISTS organization_id BIGINT;
COMMENT ON COLUMN tb_user.organization_id IS '组织ID';

-- 4. 添加position字段（职位）
ALTER TABLE tb_user ADD COLUMN IF NOT EXISTS position VARCHAR(100);
COMMENT ON COLUMN tb_user.position IS '职位';

-- 5. 添加deleted_at字段（删除时间）
ALTER TABLE tb_user ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
COMMENT ON COLUMN tb_user.deleted_at IS '删除时间';

-- 6. 添加creator_id字段（创建人ID）
ALTER TABLE tb_user ADD COLUMN IF NOT EXISTS creator_id BIGINT;
COMMENT ON COLUMN tb_user.creator_id IS '创建人ID';

-- 7. 添加updater_id字段（更新人ID）
ALTER TABLE tb_user ADD COLUMN IF NOT EXISTS updater_id BIGINT;
COMMENT ON COLUMN tb_user.updater_id IS '更新人ID';

-- 验证字段是否添加成功
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'tb_user'
  AND column_name IN ('version', 'department_id', 'organization_id', 'position', 'deleted_at', 'creator_id', 'updater_id')
ORDER BY column_name;

-- 完成
\echo '✅ 数据库Schema修复完成！'
