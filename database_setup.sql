-- =====================================================
-- ProManage 数据库初始化脚本
-- 版本: 1.0.0
-- 描述: 创建数据库角色和基础设置
-- =====================================================

-- 创建应用用户角色（用于行级安全策略）
CREATE ROLE IF NOT EXISTS application_user;

-- 创建只读用户角色（用于报表）
CREATE ROLE IF NOT EXISTS readonly_user;

-- 授予基础权限
GRANT CONNECT ON DATABASE promanage TO application_user;
GRANT USAGE ON SCHEMA public TO application_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO application_user;

GRANT CONNECT ON DATABASE promanage TO readonly_user;
GRANT USAGE ON SCHEMA public TO readonly_user;

-- =====================================================
-- 启用必要的 PostgreSQL 扩展
-- =====================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";           -- UUID 生成
CREATE EXTENSION IF NOT EXISTS "pgcrypto";            -- 加密支持
CREATE EXTENSION IF NOT EXISTS "pg_trgm";             -- 模糊匹配
CREATE EXTENSION IF NOT EXISTS "unaccent";            -- 去除重音符号
CREATE EXTENSION IF NOT EXISTS "btree_gin";           -- GIN索引支持
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";   -- SQL 性能统计

COMMENT ON DATABASE promanage IS 'ProManage 项目管理系统数据库';

NOTICE: 'ProManage 数据库基础设置完成';