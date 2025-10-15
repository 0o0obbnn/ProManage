-- =====================================================
-- ProManage 组织模块重构数据迁移脚本
-- Version: 1.1.0
-- Description: 修复组织模块的租户隔离、软删除和索引优化
-- Author: ProManage Team
-- Date: 2025-01-XX
-- =====================================================

-- 1. 添加租户ID字段(如果不存在)
-- =====================================================
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'organizations' AND column_name = 'tenant_id'
    ) THEN
        ALTER TABLE organizations ADD COLUMN tenant_id BIGINT;
        COMMENT ON COLUMN organizations.tenant_id IS '租户ID,用于多租户隔离';
    END IF;
END $$;

-- 2. 为现有数据填充tenant_id(使用组织ID作为租户ID)
-- =====================================================
UPDATE organizations 
SET tenant_id = id 
WHERE tenant_id IS NULL AND deleted_at IS NULL;

-- 3. 添加NOT NULL约束
-- =====================================================
ALTER TABLE organizations 
ALTER COLUMN tenant_id SET NOT NULL;

-- 4. 创建优化索引
-- =====================================================

-- 租户隔离索引(最重要)
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_org_tenant_active 
ON organizations(tenant_id, deleted_at) 
WHERE deleted_at IS NULL;

-- 软删除过滤索引
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_org_deleted_at 
ON organizations(deleted_at) 
WHERE deleted_at IS NOT NULL;

-- slug唯一性索引(仅活跃组织)
DROP INDEX IF EXISTS idx_org_slug;
CREATE UNIQUE INDEX CONCURRENTLY IF NOT EXISTS idx_org_slug_unique_active 
ON organizations(slug) 
WHERE deleted_at IS NULL;

-- 订阅查询索引
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_org_subscription_expires 
ON organizations(subscription_expires_at, deleted_at) 
WHERE deleted_at IS NULL AND subscription_expires_at IS NOT NULL;

-- 激活状态索引
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_org_active_status 
ON organizations(is_active, deleted_at) 
WHERE deleted_at IS NULL;

-- 5. 修复settings字段类型(TEXT -> JSONB)
-- =====================================================
DO $$
BEGIN
    -- 检查字段类型
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'organizations' 
        AND column_name = 'settings' 
        AND data_type = 'text'
    ) THEN
        -- 备份现有数据
        ALTER TABLE organizations ADD COLUMN settings_backup TEXT;
        UPDATE organizations SET settings_backup = settings;
        
        -- 转换为JSONB
        ALTER TABLE organizations ALTER COLUMN settings TYPE JSONB 
        USING CASE 
            WHEN settings IS NULL OR settings = '' THEN NULL
            WHEN settings::text ~ '^\s*\{' THEN settings::jsonb
            ELSE NULL
        END;
        
        COMMENT ON COLUMN organizations.settings IS '组织设置(JSONB格式)';
    END IF;
END $$;

-- 6. 添加默认设置(如果为空)
-- =====================================================
UPDATE organizations 
SET settings = '{
    "notification": {
        "emailEnabled": true,
        "inAppEnabled": true,
        "websocketEnabled": true,
        "digestFrequencyDays": 1
    },
    "security": {
        "passwordMinLength": 8,
        "passwordRequireSpecialChar": true,
        "sessionTimeoutMinutes": 60,
        "twoFactorAuthEnabled": false,
        "ipWhitelist": []
    },
    "project": {
        "defaultVisibility": "PRIVATE",
        "allowPublicProjects": true,
        "maxProjects": 100,
        "maxMembersPerProject": 50,
        "storageLimitMb": 10240
    },
    "custom": {}
}'::jsonb
WHERE settings IS NULL AND deleted_at IS NULL;

-- 7. 添加版本控制字段(乐观锁)
-- =====================================================
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'organizations' AND column_name = 'version'
    ) THEN
        ALTER TABLE organizations ADD COLUMN version BIGINT DEFAULT 0 NOT NULL;
        COMMENT ON COLUMN organizations.version IS '版本号,用于乐观锁';
    END IF;
END $$;

-- 8. 添加审计日志表
-- =====================================================
CREATE TABLE IF NOT EXISTS organization_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    actor_id BIGINT NOT NULL,
    actor_name VARCHAR(100),
    changes JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_org_audit_organization FOREIGN KEY (organization_id) 
        REFERENCES organizations(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_org_audit_org_id 
ON organization_audit_logs(organization_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_org_audit_tenant 
ON organization_audit_logs(tenant_id, created_at DESC);

COMMENT ON TABLE organization_audit_logs IS '组织操作审计日志';

-- 9. 添加约束
-- =====================================================

-- slug格式约束
ALTER TABLE organizations 
ADD CONSTRAINT chk_org_slug_format 
CHECK (slug ~ '^[a-z0-9-]{3,30}$');

-- 订阅计划枚举约束
ALTER TABLE organizations 
ADD CONSTRAINT chk_org_subscription_plan 
CHECK (subscription_plan IN ('FREE', 'BASIC', 'PROFESSIONAL', 'ENTERPRISE'));

-- 10. 数据完整性检查
-- =====================================================

-- 确保所有活跃组织都有订阅计划
UPDATE organizations 
SET subscription_plan = 'FREE' 
WHERE subscription_plan IS NULL AND deleted_at IS NULL;

-- 确保所有组织都有激活状态
UPDATE organizations 
SET is_active = true 
WHERE is_active IS NULL AND deleted_at IS NULL;

-- 11. 性能统计更新
-- =====================================================
ANALYZE organizations;
ANALYZE organization_audit_logs;

-- 12. 回滚脚本(保存到单独文件)
-- =====================================================
-- 见 V1.1.0__organization_module_refactor_rollback.sql
