-- ================================================================
-- ProManage Initial Data Migration
-- Version: 1.0.1
-- Description: Inserts initial system data including admin user, roles, and permissions
-- Author: ProManage Team
-- Date: 2025-09-30
-- ================================================================

-- ================================================================
-- Section 1: Insert Super Admin User
-- ================================================================

-- Insert super admin user
-- Username: admin
-- Password: admin123 (BCrypt encrypted)
-- BCrypt hash: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2ELM5u7Nwes7Gfv.MjYc66W
INSERT INTO tb_user (username, password, email, real_name, status)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2ELM5u7Nwes7Gfv.MjYc66W', 'admin@promanage.com', '系统管理员', 0);

-- ================================================================
-- Section 2: Insert Default Roles
-- ================================================================

INSERT INTO tb_role (role_name, role_code, description, sort, status) VALUES
('超级管理员', 'ROLE_SUPER_ADMIN', '系统超级管理员，拥有所有权限', 1, 0),
('项目经理', 'ROLE_PROJECT_MANAGER', '项目经理，负责项目管理和团队协调', 2, 0),
('开发人员', 'ROLE_DEVELOPER', '开发人员，负责软件开发工作', 3, 0),
('测试人员', 'ROLE_TESTER', '测试人员，负责软件测试工作', 4, 0),
('UI设计师', 'ROLE_DESIGNER', 'UI/UX设计师，负责界面和交互设计', 5, 0),
('运维人员', 'ROLE_OPS', '运维人员，负责系统部署和运维', 6, 0),
('第三方人员', 'ROLE_EXTERNAL', '第三方人员，只读权限', 7, 0);

-- ================================================================
-- Section 3: Insert Default Permissions
-- ================================================================

-- User Management Permissions
INSERT INTO tb_permission (permission_name, permission_code, type, url, method, parent_id, sort, status) VALUES
('用户管理', 'user', 'menu', NULL, NULL, 0, 1, 0),
('查看用户列表', 'user:list', 'api', '/api/users', 'GET', 1, 1, 0),
('查看用户详情', 'user:view', 'api', '/api/users/*', 'GET', 1, 2, 0),
('创建用户', 'user:create', 'api', '/api/users', 'POST', 1, 3, 0),
('更新用户', 'user:update', 'api', '/api/users/*', 'PUT', 1, 4, 0),
('删除用户', 'user:delete', 'api', '/api/users/*', 'DELETE', 1, 5, 0),
('重置密码', 'user:reset_password', 'api', '/api/users/*/reset-password', 'POST', 1, 6, 0),
('分配角色', 'user:assign_role', 'api', '/api/users/*/roles', 'POST', 1, 7, 0);

-- Role Management Permissions
INSERT INTO tb_permission (permission_name, permission_code, type, url, method, parent_id, sort, status) VALUES
('角色管理', 'role', 'menu', NULL, NULL, 0, 2, 0),
('查看角色列表', 'role:list', 'api', '/api/roles', 'GET', 9, 1, 0),
('查看角色详情', 'role:view', 'api', '/api/roles/*', 'GET', 9, 2, 0),
('创建角色', 'role:create', 'api', '/api/roles', 'POST', 9, 3, 0),
('更新角色', 'role:update', 'api', '/api/roles/*', 'PUT', 9, 4, 0),
('删除角色', 'role:delete', 'api', '/api/roles/*', 'DELETE', 9, 5, 0),
('分配权限', 'role:assign_permission', 'api', '/api/roles/*/permissions', 'POST', 9, 6, 0);

-- Permission Management Permissions
INSERT INTO tb_permission (permission_name, permission_code, type, url, method, parent_id, sort, status) VALUES
('权限管理', 'permission', 'menu', NULL, NULL, 0, 3, 0),
('查看权限列表', 'permission:list', 'api', '/api/permissions', 'GET', 16, 1, 0),
('查看权限详情', 'permission:view', 'api', '/api/permissions/*', 'GET', 16, 2, 0),
('创建权限', 'permission:create', 'api', '/api/permissions', 'POST', 16, 3, 0),
('更新权限', 'permission:update', 'api', '/api/permissions/*', 'PUT', 16, 4, 0),
('删除权限', 'permission:delete', 'api', '/api/permissions/*', 'DELETE', 16, 5, 0);

-- Project Management Permissions
INSERT INTO tb_permission (permission_name, permission_code, type, url, method, parent_id, sort, status) VALUES
('项目管理', 'project', 'menu', NULL, NULL, 0, 4, 0),
('查看项目列表', 'project:list', 'api', '/api/projects', 'GET', 22, 1, 0),
('查看项目详情', 'project:view', 'api', '/api/projects/*', 'GET', 22, 2, 0),
('创建项目', 'project:create', 'api', '/api/projects', 'POST', 22, 3, 0),
('更新项目', 'project:update', 'api', '/api/projects/*', 'PUT', 22, 4, 0),
('删除项目', 'project:delete', 'api', '/api/projects/*', 'DELETE', 22, 5, 0),
('归档项目', 'project:archive', 'api', '/api/projects/*/archive', 'POST', 22, 6, 0),
('添加项目成员', 'project:add_member', 'api', '/api/projects/*/members', 'POST', 22, 7, 0),
('移除项目成员', 'project:remove_member', 'api', '/api/projects/*/members/*', 'DELETE', 22, 8, 0),
('查看项目成员', 'project:view_members', 'api', '/api/projects/*/members', 'GET', 22, 9, 0);

-- Document Management Permissions
INSERT INTO tb_permission (permission_name, permission_code, type, url, method, parent_id, sort, status) VALUES
('文档管理', 'document', 'menu', NULL, NULL, 0, 5, 0),
('查看文档列表', 'document:list', 'api', '/api/documents', 'GET', 32, 1, 0),
('查看文档详情', 'document:view', 'api', '/api/documents/*', 'GET', 32, 2, 0),
('创建文档', 'document:create', 'api', '/api/documents', 'POST', 32, 3, 0),
('更新文档', 'document:update', 'api', '/api/documents/*', 'PUT', 32, 4, 0),
('删除文档', 'document:delete', 'api', '/api/documents/*', 'DELETE', 32, 5, 0),
('发布文档', 'document:publish', 'api', '/api/documents/*/publish', 'POST', 32, 6, 0),
('归档文档', 'document:archive', 'api', '/api/documents/*/archive', 'POST', 32, 7, 0),
('查看文档版本', 'document:view_versions', 'api', '/api/documents/*/versions', 'GET', 32, 8, 0),
('创建文档版本', 'document:create_version', 'api', '/api/documents/*/versions', 'POST', 32, 9, 0);

-- System Management Permissions
INSERT INTO tb_permission (permission_name, permission_code, type, url, method, parent_id, sort, status) VALUES
('系统管理', 'system', 'menu', NULL, NULL, 0, 6, 0),
('查看系统日志', 'system:view_logs', 'api', '/api/system/logs', 'GET', 42, 1, 0),
('系统监控', 'system:monitor', 'api', '/api/actuator/**', 'GET', 42, 2, 0),
('清理缓存', 'system:clear_cache', 'api', '/api/system/cache/clear', 'POST', 42, 3, 0),
('系统配置', 'system:config', 'api', '/api/system/config', 'GET', 42, 4, 0);

-- Authentication and Profile Permissions (Available to all authenticated users)
INSERT INTO tb_permission (permission_name, permission_code, type, url, method, parent_id, sort, status) VALUES
('个人中心', 'profile', 'menu', NULL, NULL, 0, 7, 0),
('查看个人信息', 'profile:view', 'api', '/api/auth/profile', 'GET', 47, 1, 0),
('更新个人信息', 'profile:update', 'api', '/api/auth/profile', 'PUT', 47, 2, 0),
('修改密码', 'profile:change_password', 'api', '/api/auth/change-password', 'POST', 47, 3, 0),
('上传头像', 'profile:upload_avatar', 'api', '/api/auth/avatar', 'POST', 47, 4, 0);

-- ================================================================
-- Section 4: Assign All Permissions to Super Admin Role
-- ================================================================

-- Assign all permissions to super admin role (role_id = 1)
INSERT INTO tb_role_permission (role_id, permission_id)
SELECT 1, id FROM tb_permission WHERE deleted = FALSE;

-- ================================================================
-- Section 5: Assign Permissions to Project Manager Role
-- ================================================================

-- Project Manager has most project and document permissions
INSERT INTO tb_role_permission (role_id, permission_id)
SELECT 2, id FROM tb_permission
WHERE permission_code IN (
    -- Project permissions
    'project', 'project:list', 'project:view', 'project:create', 'project:update', 'project:archive',
    'project:add_member', 'project:remove_member', 'project:view_members',
    -- Document permissions
    'document', 'document:list', 'document:view', 'document:create', 'document:update',
    'document:delete', 'document:publish', 'document:archive', 'document:view_versions', 'document:create_version',
    -- Profile permissions
    'profile', 'profile:view', 'profile:update', 'profile:change_password', 'profile:upload_avatar'
);

-- ================================================================
-- Section 6: Assign Permissions to Developer Role
-- ================================================================

-- Developer has project and document access but limited management
INSERT INTO tb_role_permission (role_id, permission_id)
SELECT 3, id FROM tb_permission
WHERE permission_code IN (
    -- Project permissions (view only)
    'project', 'project:list', 'project:view', 'project:view_members',
    -- Document permissions
    'document', 'document:list', 'document:view', 'document:create', 'document:update',
    'document:view_versions', 'document:create_version',
    -- Profile permissions
    'profile', 'profile:view', 'profile:update', 'profile:change_password', 'profile:upload_avatar'
);

-- ================================================================
-- Section 7: Assign Permissions to Tester Role
-- ================================================================

-- Tester has similar permissions to Developer
INSERT INTO tb_role_permission (role_id, permission_id)
SELECT 4, id FROM tb_permission
WHERE permission_code IN (
    -- Project permissions (view only)
    'project', 'project:list', 'project:view', 'project:view_members',
    -- Document permissions
    'document', 'document:list', 'document:view', 'document:create', 'document:update',
    'document:view_versions', 'document:create_version',
    -- Profile permissions
    'profile', 'profile:view', 'profile:update', 'profile:change_password', 'profile:upload_avatar'
);

-- ================================================================
-- Section 8: Assign Permissions to Designer Role
-- ================================================================

-- Designer has project and document access
INSERT INTO tb_role_permission (role_id, permission_id)
SELECT 5, id FROM tb_permission
WHERE permission_code IN (
    -- Project permissions (view only)
    'project', 'project:list', 'project:view', 'project:view_members',
    -- Document permissions
    'document', 'document:list', 'document:view', 'document:create', 'document:update',
    'document:view_versions', 'document:create_version',
    -- Profile permissions
    'profile', 'profile:view', 'profile:update', 'profile:change_password', 'profile:upload_avatar'
);

-- ================================================================
-- Section 9: Assign Permissions to Ops Role
-- ================================================================

-- Ops has project view and system monitoring permissions
INSERT INTO tb_role_permission (role_id, permission_id)
SELECT 6, id FROM tb_permission
WHERE permission_code IN (
    -- Project permissions (view only)
    'project', 'project:list', 'project:view', 'project:view_members',
    -- Document permissions (view only)
    'document', 'document:list', 'document:view', 'document:view_versions',
    -- System permissions
    'system', 'system:view_logs', 'system:monitor', 'system:clear_cache',
    -- Profile permissions
    'profile', 'profile:view', 'profile:update', 'profile:change_password', 'profile:upload_avatar'
);

-- ================================================================
-- Section 10: Assign Permissions to External Role
-- ================================================================

-- External users have read-only access
INSERT INTO tb_role_permission (role_id, permission_id)
SELECT 7, id FROM tb_permission
WHERE permission_code IN (
    -- Project permissions (view only)
    'project', 'project:list', 'project:view', 'project:view_members',
    -- Document permissions (view only)
    'document', 'document:list', 'document:view', 'document:view_versions',
    -- Profile permissions
    'profile', 'profile:view', 'profile:update', 'profile:change_password', 'profile:upload_avatar'
);

-- ================================================================
-- Section 11: Assign Super Admin Role to Admin User
-- ================================================================

-- Assign super admin role to admin user (user_id = 1, role_id = 1)
INSERT INTO tb_user_role (user_id, role_id) VALUES (1, 1);

-- ================================================================
-- Section 12: Insert Demo Project (Optional)
-- ================================================================

-- Insert a demo project owned by admin
INSERT INTO tb_project (name, code, description, status, owner_id, start_date, end_date, color) VALUES
('ProManage演示项目', 'DEMO-001', '这是一个演示项目，用于展示ProManage的功能特性', 1, 1, CURRENT_DATE, CURRENT_DATE + INTERVAL '30 days', '#1890ff');

-- Add admin as project member
INSERT INTO tb_project_member (project_id, user_id, role_id) VALUES (1, 1, 1);

-- Insert demo documents
INSERT INTO tb_document (title, content, summary, type, status, project_id, folder_id, current_version, creator_id) VALUES
('项目需求文档', '# ProManage项目需求文档\n\n## 1. 项目背景\nProManage是一个现代化的项目管理系统...\n\n## 2. 功能需求\n### 2.1 用户管理\n- 用户注册和登录\n- 用户权限管理\n\n### 2.2 项目管理\n- 项目创建和配置\n- 项目成员管理',
 '本文档描述了ProManage系统的核心功能需求', 'PRD', 2, 1, 0, '1.0.0', 1),

('API接口设计文档', '# ProManage API设计文档\n\n## 1. 认证接口\n### 1.1 用户登录\n- URL: /api/auth/login\n- Method: POST\n- Request Body:\n```json\n{\n  "username": "admin",\n  "password": "admin123"\n}\n```\n\n## 2. 用户管理接口\n### 2.1 获取用户列表\n- URL: /api/users\n- Method: GET',
 '本文档定义了ProManage系统的RESTful API接口规范', 'API', 2, 1, 0, '1.0.0', 1),

('系统测试计划', '# ProManage测试计划\n\n## 1. 测试目标\n确保系统的功能完整性和稳定性\n\n## 2. 测试范围\n### 2.1 功能测试\n- 用户管理模块测试\n- 项目管理模块测试\n- 文档管理模块测试\n\n### 2.2 性能测试\n- 并发用户测试\n- 数据库性能测试',
 '本文档描述了ProManage系统的测试计划和测试用例', 'Test', 1, 1, 0, '1.0.0', 1);

-- Insert document versions
INSERT INTO tb_document_version (document_id, version, content, change_log, creator_id) VALUES
(1, '1.0.0', '# ProManage项目需求文档\n\n## 1. 项目背景\nProManage是一个现代化的项目管理系统...', '初始版本', 1),
(2, '1.0.0', '# ProManage API设计文档\n\n## 1. 认证接口\n### 1.1 用户登录...', '初始版本', 1),
(3, '1.0.0', '# ProManage测试计划\n\n## 1. 测试目标\n确保系统的功能完整性和稳定性...', '初始版本', 1);

-- ================================================================
-- Data Migration Complete
-- ================================================================

-- Display summary information
DO $$
DECLARE
    user_count INTEGER;
    role_count INTEGER;
    permission_count INTEGER;
    project_count INTEGER;
    document_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO user_count FROM tb_user WHERE deleted = FALSE;
    SELECT COUNT(*) INTO role_count FROM tb_role WHERE deleted = FALSE;
    SELECT COUNT(*) INTO permission_count FROM tb_permission WHERE deleted = FALSE;
    SELECT COUNT(*) INTO project_count FROM tb_project WHERE deleted = FALSE;
    SELECT COUNT(*) INTO document_count FROM tb_document WHERE deleted = FALSE;

    RAISE NOTICE '================================================';
    RAISE NOTICE 'ProManage Initial Data Migration Completed';
    RAISE NOTICE '================================================';
    RAISE NOTICE 'Users Created: %', user_count;
    RAISE NOTICE 'Roles Created: %', role_count;
    RAISE NOTICE 'Permissions Created: %', permission_count;
    RAISE NOTICE 'Projects Created: %', project_count;
    RAISE NOTICE 'Documents Created: %', document_count;
    RAISE NOTICE '================================================';
    RAISE NOTICE 'Default Admin Credentials:';
    RAISE NOTICE '  Username: admin';
    RAISE NOTICE '  Password: admin123';
    RAISE NOTICE '================================================';
END $$;