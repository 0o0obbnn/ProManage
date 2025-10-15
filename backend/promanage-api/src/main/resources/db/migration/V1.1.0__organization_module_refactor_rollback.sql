-- =====================================================
-- ProManage 组织模块重构回滚脚本
-- Version: 1.1.0 Rollback
-- Description: 回滚组织模块重构的数据库变更
-- Author: ProManage Team
-- Date: 2025-01-XX
-- WARNING: 仅在紧急情况下使用
-- =====================================================

-- 1. 删除审计日志表
DROP TABLE IF EXISTS organization_audit_logs CASCADE;

-- 2. 删除新增索引
DROP INDEX CONCURRENTLY IF EXISTS idx_org_tenant_active;
DROP INDEX CONCURRENTLY IF EXISTS idx_org_deleted_at;
DROP INDEX CONCURRENTLY IF EXISTS idx_org_slug_unique_active;
DROP INDEX CONCURRENTLY IF EXISTS idx_org_subscription_expires;
DROP INDEX CONCURRENTLY IF EXISTS idx_org_active_status;

-- 3. 恢复原始slug索引
CREATE INDEX IF NOT EXISTS idx_org_slug ON organizations(slug);

-- 4. 删除约束
ALTER TABLE organizations DROP CONSTRAINT IF EXISTS chk_org_slug_format;
ALTER TABLE organizations DROP CONSTRAINT IF EXISTS chk_org_subscription_plan;

-- 5. 恢复settings字段类型(JSONB -> TEXT)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'organizations' 
        AND column_name = 'settings_backup'
    ) THEN
        -- 从备份恢复
        ALTER TABLE organizations ALTER COLUMN settings TYPE TEXT;
        UPDATE organizations SET settings = settings_backup;
        ALTER TABLE organizations DROP COLUMN settings_backup;
    ELSE
        -- 直接转换
        ALTER TABLE organizations ALTER COLUMN settings TYPE TEXT 
        USING settings::text;
    END IF;
END $$;

-- 6. 删除version字段
ALTER TABLE organizations DROP COLUMN IF EXISTS version;

-- 7. 删除tenant_id字段(谨慎操作)
-- ALTER TABLE organizations DROP COLUMN IF EXISTS tenant_id;
-- 注意: 如果已有业务数据依赖tenant_id,不要执行此步骤

-- 8. 性能统计更新
ANALYZE organizations;
