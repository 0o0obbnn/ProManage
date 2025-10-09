-- ================================================================
-- ProManage 数据库完整初始化脚本
-- Version: 1.0
-- Description: 一键执行完整数据库初始化（所有表+初始数据）
-- Author: ProManage Team
-- Date: 2025-10-09
-- ================================================================
--
-- 使用说明:
-- 1. 确保已创建数据库 promanage
-- 2. 使用 psql 执行此脚本:
--    psql -h 192.168.2.144 -U postgres -d promanage -f COMPLETE_DATABASE_INIT.sql
-- 3. 或在DBeaver/pgAdmin等工具中直接执行
--
-- 此脚本包含:
-- - 扩展安装
-- - 所有表结构创建
-- - 索引创建
-- - 初始数据（角色、权限、管理员用户）
-- ================================================================

-- ================================================================
-- Part 1: 扩展和基础设置
-- ================================================================

-- 创建UUID扩展（用于生成唯一标识符）
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 创建pg_trgm扩展（用于模糊搜索）
CREATE EXTENSION IF NOT EXISTS pg_trgm;


-- ================================================================
-- Part 2: 用户和权限表 (V1.0.0)
-- ================================================================

-- 用户表
CREATE TABLE IF NOT EXISTS tb_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    real_name VARCHAR(50),
    avatar VARCHAR(500),
    status INTEGER NOT NULL DEFAULT 0,
    last_login_time TIMESTAMP,
    last_login_ip VARCHAR(50),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    creator_id BIGINT,
    updater_id BIGINT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE tb_user IS '用户表';
COMMENT ON COLUMN tb_user.status IS '状态: 0-正常, 1-禁用, 2-锁定';

-- 角色表
CREATE TABLE IF NOT EXISTS tb_role (
    id BIGSERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(500),
    sort INTEGER NOT NULL DEFAULT 0,
    status INTEGER NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    creator_id BIGINT,
    updater_id BIGINT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE tb_role IS '角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS tb_permission (
    id BIGSERIAL PRIMARY KEY,
    permission_name VARCHAR(100) NOT NULL,
    permission_code VARCHAR(100) NOT NULL UNIQUE,
    type VARCHAR(20) NOT NULL DEFAULT 'api',
    url VARCHAR(500),
    method VARCHAR(10),
    parent_id BIGINT NOT NULL DEFAULT 0,
    sort INTEGER NOT NULL DEFAULT 0,
    icon VARCHAR(100),
    status INTEGER NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    creator_id BIGINT,
    updater_id BIGINT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE tb_permission IS '权限表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS tb_user_role (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_role_user FOREIGN KEY(user_id) REFERENCES tb_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY(role_id) REFERENCES tb_role(id) ON DELETE CASCADE,
    CONSTRAINT uq_user_role UNIQUE(user_id, role_id)
);

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS tb_role_permission (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_role_permission_role FOREIGN KEY(role_id) REFERENCES tb_role(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permission_permission FOREIGN KEY(permission_id) REFERENCES tb_permission(id) ON DELETE CASCADE,
    CONSTRAINT uq_role_permission UNIQUE(role_id, permission_id)
);


-- ================================================================
-- Part 3: 项目管理表 (V1.0.0)
-- ================================================================

-- 项目表
CREATE TABLE IF NOT EXISTS tb_project (
    id BIGSERIAL PRIMARY KEY,
    project_name VARCHAR(100) NOT NULL,
    project_code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    status INTEGER NOT NULL DEFAULT 0,
    start_date DATE,
    end_date DATE,
    owner_id BIGINT NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    creator_id BIGINT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_project_owner FOREIGN KEY(owner_id) REFERENCES tb_user(id) ON DELETE RESTRICT
);

COMMENT ON TABLE tb_project IS '项目表';
COMMENT ON COLUMN tb_project.status IS '状态: 0-规划中, 1-进行中, 2-已完成, 3-已取消';

-- 项目成员表
CREATE TABLE IF NOT EXISTS tb_project_member (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_project_member_project FOREIGN KEY(project_id) REFERENCES tb_project(id) ON DELETE CASCADE,
    CONSTRAINT fk_project_member_user FOREIGN KEY(user_id) REFERENCES tb_user(id) ON DELETE CASCADE,
    CONSTRAINT uq_project_member UNIQUE(project_id, user_id)
);

COMMENT ON TABLE tb_project_member IS '项目成员表';


-- ================================================================
-- Part 4: 文档管理表 (V1.0.0)
-- ================================================================

-- 文档表
CREATE TABLE IF NOT EXISTS tb_document (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    document_type VARCHAR(50),
    file_url VARCHAR(500),
    file_size BIGINT,
    version VARCHAR(20) DEFAULT '1.0',
    status INTEGER NOT NULL DEFAULT 0,
    creator_id BIGINT NOT NULL,
    updater_id BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_document_project FOREIGN KEY(project_id) REFERENCES tb_project(id) ON DELETE CASCADE,
    CONSTRAINT fk_document_creator FOREIGN KEY(creator_id) REFERENCES tb_user(id) ON DELETE RESTRICT
);

COMMENT ON TABLE tb_document IS '文档表';

-- 文档版本表
CREATE TABLE IF NOT EXISTS tb_document_version (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    version VARCHAR(20) NOT NULL,
    content TEXT,
    file_url VARCHAR(500),
    change_log TEXT,
    creator_id BIGINT NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_document_version_document FOREIGN KEY(document_id) REFERENCES tb_document(id) ON DELETE CASCADE,
    CONSTRAINT fk_document_version_creator FOREIGN KEY(creator_id) REFERENCES tb_user(id) ON DELETE RESTRICT
);


-- ================================================================
-- Part 5: 任务管理表 (V1.0.4)
-- ================================================================

\ir backend/promanage-service/src/main/resources/db/migration/V1.0.4__create_task_tables.sql


-- ================================================================
-- Part 6: 变更请求表 (V1.0.5)
-- ================================================================

\ir backend/promanage-service/src/main/resources/db/migration/V1.0.5__create_change_request_tables.sql


-- ================================================================
-- Part 7: 测试用例表 (V1.0.6)
-- ================================================================

\ir backend/promanage-service/src/main/resources/db/migration/V1.0.6__create_test_case_tables.sql


-- ================================================================
-- Part 8: 通知表 (database_migrations/005)
-- ================================================================

CREATE TABLE IF NOT EXISTS tb_notification (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    type VARCHAR(50) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    related_id BIGINT,
    related_type VARCHAR(50),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY(user_id) REFERENCES tb_user(id) ON DELETE CASCADE
);

COMMENT ON TABLE tb_notification IS '通知表';


-- ================================================================
-- Part 9: 文档文件夹表 (database_migrations/002)
-- ================================================================

CREATE TABLE IF NOT EXISTS tb_document_folder (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    folder_name VARCHAR(100) NOT NULL,
    parent_id BIGINT,
    path VARCHAR(500),
    sort INTEGER DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    creator_id BIGINT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_document_folder_project FOREIGN KEY(project_id) REFERENCES tb_project(id) ON DELETE CASCADE,
    CONSTRAINT fk_document_folder_parent FOREIGN KEY(parent_id) REFERENCES tb_document_folder(id) ON DELETE CASCADE
);


-- ================================================================
-- Part 10: 项目活动表 (V1.0.3)
-- ================================================================

\ir backend/promanage-service/src/main/resources/db/migration/V1.0.3__create_project_activity_table.sql


-- ================================================================
-- Part 11: 性能索引 (database_migrations/003)
-- ================================================================

-- 用户表索引
CREATE INDEX IF NOT EXISTS idx_user_username ON tb_user(username) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_user_email ON tb_user(email) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_user_status ON tb_user(status) WHERE deleted = FALSE;

-- 项目表索引
CREATE INDEX IF NOT EXISTS idx_project_code ON tb_project(project_code) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_project_owner_id ON tb_project(owner_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_project_status ON tb_project(status) WHERE deleted = FALSE;

-- 文档表索引
CREATE INDEX IF NOT EXISTS idx_document_project_id ON tb_document(project_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_document_creator_id ON tb_document(creator_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_document_type ON tb_document(document_type) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_document_title_trgm ON tb_document USING gin(title gin_trgm_ops) WHERE deleted = FALSE;

-- 通知表索引
CREATE INDEX IF NOT EXISTS idx_notification_user_id ON tb_notification(user_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_notification_is_read ON tb_notification(is_read) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_notification_create_time ON tb_notification(create_time DESC);


-- ================================================================
-- Part 12: 初始数据 (V1.0.1)
-- ================================================================

-- 插入默认角色
INSERT INTO tb_role (id, role_name, role_code, description, sort, status) VALUES
(1, '超级管理员', 'SUPER_ADMIN', '系统超级管理员，拥有所有权限', 1, 0),
(2, '普通用户', 'NORMAL_USER', '普通用户角色', 2, 0),
(3, '项目经理', 'PROJECT_MANAGER', '项目经理角色', 3, 0),
(4, '开发人员', 'DEVELOPER', '开发人员角色', 4, 0),
(5, '测试人员', 'TESTER', '测试人员角色', 5, 0)
ON CONFLICT (role_code) DO NOTHING;

-- 重置角色ID序列
SELECT setval('tb_role_id_seq', (SELECT MAX(id) FROM tb_role));

-- 插入默认管理员用户 (密码: admin123)
INSERT INTO tb_user (id, username, password, email, real_name, status) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin@promanage.com', '系统管理员', 0)
ON CONFLICT (username) DO NOTHING;

-- 重置用户ID序列
SELECT setval('tb_user_id_seq', (SELECT MAX(id) FROM tb_user));

-- 为管理员分配超级管理员角色
INSERT INTO tb_user_role (user_id, role_id) VALUES (1, 1)
ON CONFLICT (user_id, role_id) DO NOTHING;

-- 插入基础权限
INSERT INTO tb_permission (permission_name, permission_code, type, sort, status) VALUES
('系统管理', 'system:manage', 'menu', 1, 0),
('用户管理', 'user:manage', 'menu', 2, 0),
('用户查询', 'user:query', 'api', 3, 0),
('用户新增', 'user:add', 'api', 4, 0),
('用户编辑', 'user:edit', 'api', 5, 0),
('用户删除', 'user:delete', 'api', 6, 0),
('项目管理', 'project:manage', 'menu', 7, 0),
('项目查询', 'project:query', 'api', 8, 0),
('项目新增', 'project:add', 'api', 9, 0),
('项目编辑', 'project:edit', 'api', 10, 0),
('项目删除', 'project:delete', 'api', 11, 0),
('文档管理', 'document:manage', 'menu', 12, 0),
('文档查询', 'document:query', 'api', 13, 0),
('文档新增', 'document:add', 'api', 14, 0),
('文档编辑', 'document:edit', 'api', 15, 0),
('文档删除', 'document:delete', 'api', 16, 0)
ON CONFLICT (permission_code) DO NOTHING;

-- 为超级管理员角色分配所有权限
INSERT INTO tb_role_permission (role_id, permission_id)
SELECT 1, id FROM tb_permission
ON CONFLICT (role_id, permission_id) DO NOTHING;


-- ================================================================
-- Part 13: 完成提示
-- ================================================================

DO $$
BEGIN
    RAISE NOTICE '================================================================';
    RAISE NOTICE 'ProManage 数据库初始化完成！';
    RAISE NOTICE '================================================================';
    RAISE NOTICE '默认管理员账户:';
    RAISE NOTICE '  用户名: admin';
    RAISE NOTICE '  密码: admin123';
    RAISE NOTICE '================================================================';
    RAISE NOTICE '数据库统计:';
    RAISE NOTICE '  用户数: %', (SELECT COUNT(*) FROM tb_user);
    RAISE NOTICE '  角色数: %', (SELECT COUNT(*) FROM tb_role);
    RAISE NOTICE '  权限数: %', (SELECT COUNT(*) FROM tb_permission);
    RAISE NOTICE '================================================================';
END $$;
