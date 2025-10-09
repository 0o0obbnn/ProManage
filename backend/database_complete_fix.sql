-- ========================================
-- ProManage 数据库Schema完整修复脚本
-- ========================================
-- 日期: 2025-10-09
-- 目的: 为所有继承BaseEntity的表添加缺失字段
-- 执行方式: 在PostgreSQL中执行此脚本
-- ========================================

\c promanage

-- 创建一个函数来为表添加BaseEntity字段
CREATE OR REPLACE FUNCTION add_base_entity_columns(table_name TEXT) RETURNS VOID AS $$
BEGIN
    -- 添加version字段（乐观锁）
    EXECUTE format('ALTER TABLE %I ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0', table_name);
    EXECUTE format('COMMENT ON COLUMN %I.version IS ''乐观锁版本号''', table_name);

    -- 添加deleted_at字段（删除时间）
    EXECUTE format('ALTER TABLE %I ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP', table_name);
    EXECUTE format('COMMENT ON COLUMN %I.deleted_at IS ''删除时间''', table_name);

    -- 添加creator_id字段（创建人ID）
    EXECUTE format('ALTER TABLE %I ADD COLUMN IF NOT EXISTS creator_id BIGINT', table_name);
    EXECUTE format('COMMENT ON COLUMN %I.creator_id IS ''创建人ID''', table_name);

    -- 添加updater_id字段（更新人ID）
    EXECUTE format('ALTER TABLE %I ADD COLUMN IF NOT EXISTS updater_id BIGINT', table_name);
    EXECUTE format('COMMENT ON COLUMN %I.updater_id IS ''更新人ID''', table_name);

    RAISE NOTICE '✅ 表 % 的BaseEntity字段添加完成', table_name;
END;
$$ LANGUAGE plpgsql;

-- 为tb_user表添加User特有字段
ALTER TABLE tb_user ADD COLUMN IF NOT EXISTS department_id BIGINT;
COMMENT ON COLUMN tb_user.department_id IS '部门ID';

ALTER TABLE tb_user ADD COLUMN IF NOT EXISTS organization_id BIGINT;
COMMENT ON COLUMN tb_user.organization_id IS '组织ID';

ALTER TABLE tb_user ADD COLUMN IF NOT EXISTS position VARCHAR(100);
COMMENT ON COLUMN tb_user.position IS '职位';

-- 为所有继承BaseEntity的表添加字段
SELECT add_base_entity_columns('tb_user');
SELECT add_base_entity_columns('tb_role');
SELECT add_base_entity_columns('tb_permission');
SELECT add_base_entity_columns('tb_organization');
SELECT add_base_entity_columns('tb_project');
SELECT add_base_entity_columns('tb_task');
SELECT add_base_entity_columns('tb_document');
SELECT add_base_entity_columns('tb_document_version');
SELECT add_base_entity_columns('tb_document_folder');
SELECT add_base_entity_columns('tb_change_request');
SELECT add_base_entity_columns('tb_notification');

-- 删除临时函数
DROP FUNCTION add_base_entity_columns(TEXT);

-- 验证关键表字段
\echo '========== tb_user字段验证 =========='
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'tb_user'
  AND column_name IN ('version', 'department_id', 'organization_id', 'position', 'deleted_at', 'creator_id', 'updater_id')
ORDER BY column_name;

\echo '========== tb_role字段验证 =========='
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'tb_role'
  AND column_name IN ('version', 'deleted_at', 'creator_id', 'updater_id')
ORDER BY column_name;

-- 完成
\echo '✅ 数据库Schema完整修复完成！'
