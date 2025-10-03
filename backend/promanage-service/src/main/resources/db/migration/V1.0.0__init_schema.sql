-- ================================================================
-- ProManage Database Schema Initialization
-- Version: 1.0.0
-- Description: Creates all core tables for ProManage system
-- Author: ProManage Team
-- Date: 2025-09-30
-- ================================================================

-- ================================================================
-- Section 1: User Management Tables
-- ================================================================

-- User table
CREATE TABLE tb_user (
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
COMMENT ON COLUMN tb_user.id IS '用户ID';
COMMENT ON COLUMN tb_user.username IS '用户名';
COMMENT ON COLUMN tb_user.password IS '密码(BCrypt加密)';
COMMENT ON COLUMN tb_user.email IS '邮箱';
COMMENT ON COLUMN tb_user.phone IS '手机号';
COMMENT ON COLUMN tb_user.real_name IS '真实姓名';
COMMENT ON COLUMN tb_user.avatar IS '头像URL';
COMMENT ON COLUMN tb_user.status IS '状态: 0-正常, 1-禁用, 2-锁定';
COMMENT ON COLUMN tb_user.last_login_time IS '最后登录时间';
COMMENT ON COLUMN tb_user.last_login_ip IS '最后登录IP';
COMMENT ON COLUMN tb_user.deleted IS '逻辑删除标志';
COMMENT ON COLUMN tb_user.creator_id IS '创建人ID';
COMMENT ON COLUMN tb_user.updater_id IS '更新人ID';
COMMENT ON COLUMN tb_user.create_time IS '创建时间';
COMMENT ON COLUMN tb_user.update_time IS '更新时间';

-- ================================================================
-- Section 2: Role and Permission Tables
-- ================================================================

-- Role table
CREATE TABLE tb_role (
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
COMMENT ON COLUMN tb_role.id IS '角色ID';
COMMENT ON COLUMN tb_role.role_name IS '角色名称';
COMMENT ON COLUMN tb_role.role_code IS '角色编码';
COMMENT ON COLUMN tb_role.description IS '角色描述';
COMMENT ON COLUMN tb_role.sort IS '排序号';
COMMENT ON COLUMN tb_role.status IS '状态: 0-正常, 1-禁用';
COMMENT ON COLUMN tb_role.deleted IS '逻辑删除标志';
COMMENT ON COLUMN tb_role.creator_id IS '创建人ID';
COMMENT ON COLUMN tb_role.updater_id IS '更新人ID';
COMMENT ON COLUMN tb_role.create_time IS '创建时间';
COMMENT ON COLUMN tb_role.update_time IS '更新时间';

-- Permission table
CREATE TABLE tb_permission (
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
COMMENT ON COLUMN tb_permission.id IS '权限ID';
COMMENT ON COLUMN tb_permission.permission_name IS '权限名称';
COMMENT ON COLUMN tb_permission.permission_code IS '权限编码';
COMMENT ON COLUMN tb_permission.type IS '类型: menu-菜单, button-按钮, api-接口';
COMMENT ON COLUMN tb_permission.url IS '权限URL';
COMMENT ON COLUMN tb_permission.method IS 'HTTP方法: GET/POST/PUT/DELETE';
COMMENT ON COLUMN tb_permission.parent_id IS '父权限ID, 0表示顶级权限';
COMMENT ON COLUMN tb_permission.sort IS '排序号';
COMMENT ON COLUMN tb_permission.icon IS '图标';
COMMENT ON COLUMN tb_permission.status IS '状态: 0-正常, 1-禁用';
COMMENT ON COLUMN tb_permission.deleted IS '逻辑删除标志';
COMMENT ON COLUMN tb_permission.creator_id IS '创建人ID';
COMMENT ON COLUMN tb_permission.updater_id IS '更新人ID';
COMMENT ON COLUMN tb_permission.create_time IS '创建时间';
COMMENT ON COLUMN tb_permission.update_time IS '更新时间';

-- User-Role mapping table
CREATE TABLE tb_user_role (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, role_id)
);

COMMENT ON TABLE tb_user_role IS '用户角色关联表';
COMMENT ON COLUMN tb_user_role.id IS '主键ID';
COMMENT ON COLUMN tb_user_role.user_id IS '用户ID';
COMMENT ON COLUMN tb_user_role.role_id IS '角色ID';
COMMENT ON COLUMN tb_user_role.create_time IS '创建时间';

-- Role-Permission mapping table
CREATE TABLE tb_role_permission (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (role_id, permission_id)
);

COMMENT ON TABLE tb_role_permission IS '角色权限关联表';
COMMENT ON COLUMN tb_role_permission.id IS '主键ID';
COMMENT ON COLUMN tb_role_permission.role_id IS '角色ID';
COMMENT ON COLUMN tb_role_permission.permission_id IS '权限ID';
COMMENT ON COLUMN tb_role_permission.create_time IS '创建时间';

-- ================================================================
-- Section 3: Project Management Tables
-- ================================================================

-- Project table
CREATE TABLE tb_project (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(1000),
    status INTEGER NOT NULL DEFAULT 0,
    owner_id BIGINT NOT NULL,
    start_date DATE,
    end_date DATE,
    actual_end_date DATE,
    icon VARCHAR(500),
    color VARCHAR(20),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    creator_id BIGINT,
    updater_id BIGINT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE tb_project IS '项目表';
COMMENT ON COLUMN tb_project.id IS '项目ID';
COMMENT ON COLUMN tb_project.name IS '项目名称';
COMMENT ON COLUMN tb_project.code IS '项目编码';
COMMENT ON COLUMN tb_project.description IS '项目描述';
COMMENT ON COLUMN tb_project.status IS '状态: 0-规划中, 1-进行中, 2-已完成, 3-已归档';
COMMENT ON COLUMN tb_project.owner_id IS '项目负责人ID';
COMMENT ON COLUMN tb_project.start_date IS '计划开始日期';
COMMENT ON COLUMN tb_project.end_date IS '计划结束日期';
COMMENT ON COLUMN tb_project.actual_end_date IS '实际结束日期';
COMMENT ON COLUMN tb_project.icon IS '项目图标URL';
COMMENT ON COLUMN tb_project.color IS '项目颜色标识';
COMMENT ON COLUMN tb_project.deleted IS '逻辑删除标志';
COMMENT ON COLUMN tb_project.creator_id IS '创建人ID';
COMMENT ON COLUMN tb_project.updater_id IS '更新人ID';
COMMENT ON COLUMN tb_project.create_time IS '创建时间';
COMMENT ON COLUMN tb_project.update_time IS '更新时间';

-- Project member table
CREATE TABLE tb_project_member (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    join_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status INTEGER NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    creator_id BIGINT,
    updater_id BIGINT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (project_id, user_id)
);

COMMENT ON TABLE tb_project_member IS '项目成员表';
COMMENT ON COLUMN tb_project_member.id IS '主键ID';
COMMENT ON COLUMN tb_project_member.project_id IS '项目ID';
COMMENT ON COLUMN tb_project_member.user_id IS '用户ID';
COMMENT ON COLUMN tb_project_member.role_id IS '项目角色ID';
COMMENT ON COLUMN tb_project_member.join_time IS '加入时间';
COMMENT ON COLUMN tb_project_member.status IS '状态: 0-正常, 1-已退出';
COMMENT ON COLUMN tb_project_member.deleted IS '逻辑删除标志';
COMMENT ON COLUMN tb_project_member.creator_id IS '创建人ID';
COMMENT ON COLUMN tb_project_member.updater_id IS '更新人ID';
COMMENT ON COLUMN tb_project_member.create_time IS '创建时间';
COMMENT ON COLUMN tb_project_member.update_time IS '更新时间';

-- ================================================================
-- Section 4: Document Management Tables
-- ================================================================

-- Document table
CREATE TABLE tb_document (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    summary VARCHAR(500),
    type VARCHAR(50) NOT NULL,
    status INTEGER NOT NULL DEFAULT 0,
    project_id BIGINT NOT NULL,
    folder_id BIGINT NOT NULL DEFAULT 0,
    file_url VARCHAR(500),
    file_size BIGINT,
    current_version VARCHAR(20) NOT NULL DEFAULT '1.0.0',
    view_count INTEGER NOT NULL DEFAULT 0,
    creator_id BIGINT NOT NULL,
    updater_id BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE tb_document IS '文档表';
COMMENT ON COLUMN tb_document.id IS '文档ID';
COMMENT ON COLUMN tb_document.title IS '文档标题';
COMMENT ON COLUMN tb_document.content IS '文档内容';
COMMENT ON COLUMN tb_document.summary IS '文档摘要';
COMMENT ON COLUMN tb_document.type IS '文档类型: PRD-需求文档, Design-设计文档, API-接口文档, Test-测试文档, Other-其他';
COMMENT ON COLUMN tb_document.status IS '状态: 0-草稿, 1-审核中, 2-已发布, 3-已归档';
COMMENT ON COLUMN tb_document.project_id IS '所属项目ID';
COMMENT ON COLUMN tb_document.folder_id IS '文件夹ID, 0表示根目录';
COMMENT ON COLUMN tb_document.file_url IS '附件URL';
COMMENT ON COLUMN tb_document.file_size IS '附件大小(字节)';
COMMENT ON COLUMN tb_document.current_version IS '当前版本号';
COMMENT ON COLUMN tb_document.view_count IS '浏览次数';
COMMENT ON COLUMN tb_document.creator_id IS '创建人ID';
COMMENT ON COLUMN tb_document.updater_id IS '更新人ID';
COMMENT ON COLUMN tb_document.deleted IS '逻辑删除标志';
COMMENT ON COLUMN tb_document.create_time IS '创建时间';
COMMENT ON COLUMN tb_document.update_time IS '更新时间';

-- Document version table
CREATE TABLE tb_document_version (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    version VARCHAR(20) NOT NULL,
    content TEXT,
    change_log VARCHAR(1000),
    file_url VARCHAR(500),
    creator_id BIGINT NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (document_id, version)
);

COMMENT ON TABLE tb_document_version IS '文档版本表';
COMMENT ON COLUMN tb_document_version.id IS '版本ID';
COMMENT ON COLUMN tb_document_version.document_id IS '文档ID';
COMMENT ON COLUMN tb_document_version.version IS '版本号';
COMMENT ON COLUMN tb_document_version.content IS '版本内容';
COMMENT ON COLUMN tb_document_version.change_log IS '变更日志';
COMMENT ON COLUMN tb_document_version.file_url IS '附件URL';
COMMENT ON COLUMN tb_document_version.creator_id IS '创建人ID';
COMMENT ON COLUMN tb_document_version.deleted IS '逻辑删除标志';
COMMENT ON COLUMN tb_document_version.create_time IS '创建时间';
COMMENT ON COLUMN tb_document_version.update_time IS '更新时间';

-- ================================================================
-- Section 5: Create Indexes for Performance Optimization
-- ================================================================

-- User table indexes
CREATE INDEX idx_user_username ON tb_user(username) WHERE deleted = FALSE;
CREATE INDEX idx_user_email ON tb_user(email) WHERE deleted = FALSE AND email IS NOT NULL;
CREATE INDEX idx_user_status ON tb_user(status) WHERE deleted = FALSE;
CREATE INDEX idx_user_create_time ON tb_user(create_time DESC);

-- Role table indexes
CREATE INDEX idx_role_code ON tb_role(role_code) WHERE deleted = FALSE;
CREATE INDEX idx_role_status ON tb_role(status) WHERE deleted = FALSE;

-- Permission table indexes
CREATE INDEX idx_permission_code ON tb_permission(permission_code) WHERE deleted = FALSE;
CREATE INDEX idx_permission_parent_id ON tb_permission(parent_id) WHERE deleted = FALSE;
CREATE INDEX idx_permission_type ON tb_permission(type) WHERE deleted = FALSE;

-- User-Role indexes
CREATE INDEX idx_user_role_user_id ON tb_user_role(user_id);
CREATE INDEX idx_user_role_role_id ON tb_user_role(role_id);

-- Role-Permission indexes
CREATE INDEX idx_role_permission_role_id ON tb_role_permission(role_id);
CREATE INDEX idx_role_permission_permission_id ON tb_role_permission(permission_id);

-- Project table indexes
CREATE INDEX idx_project_code ON tb_project(code) WHERE deleted = FALSE;
CREATE INDEX idx_project_owner_id ON tb_project(owner_id) WHERE deleted = FALSE;
CREATE INDEX idx_project_status ON tb_project(status) WHERE deleted = FALSE;
CREATE INDEX idx_project_create_time ON tb_project(create_time DESC);

-- Project member indexes
CREATE INDEX idx_project_member_project_id ON tb_project_member(project_id) WHERE deleted = FALSE;
CREATE INDEX idx_project_member_user_id ON tb_project_member(user_id) WHERE deleted = FALSE;
CREATE INDEX idx_project_member_role_id ON tb_project_member(role_id) WHERE deleted = FALSE;

-- Document table indexes
CREATE INDEX idx_document_title ON tb_document(title) WHERE deleted = FALSE;
CREATE INDEX idx_document_project_id ON tb_document(project_id) WHERE deleted = FALSE;
CREATE INDEX idx_document_folder_id ON tb_document(folder_id) WHERE deleted = FALSE;
CREATE INDEX idx_document_creator_id ON tb_document(creator_id) WHERE deleted = FALSE;
CREATE INDEX idx_document_status ON tb_document(status) WHERE deleted = FALSE;
CREATE INDEX idx_document_type ON tb_document(type) WHERE deleted = FALSE;
CREATE INDEX idx_document_create_time ON tb_document(create_time DESC);

-- Document version indexes
CREATE INDEX idx_document_version_document_id ON tb_document_version(document_id) WHERE deleted = FALSE;
CREATE INDEX idx_document_version_version ON tb_document_version(document_id, version) WHERE deleted = FALSE;
CREATE INDEX idx_document_version_create_time ON tb_document_version(create_time DESC);

-- ================================================================
-- Section 6: Add Foreign Key Constraints
-- ================================================================

-- User-Role foreign keys
ALTER TABLE tb_user_role
    ADD CONSTRAINT fk_user_role_user
    FOREIGN KEY (user_id) REFERENCES tb_user(id) ON DELETE CASCADE;

ALTER TABLE tb_user_role
    ADD CONSTRAINT fk_user_role_role
    FOREIGN KEY (role_id) REFERENCES tb_role(id) ON DELETE CASCADE;

-- Role-Permission foreign keys
ALTER TABLE tb_role_permission
    ADD CONSTRAINT fk_role_permission_role
    FOREIGN KEY (role_id) REFERENCES tb_role(id) ON DELETE CASCADE;

ALTER TABLE tb_role_permission
    ADD CONSTRAINT fk_role_permission_permission
    FOREIGN KEY (permission_id) REFERENCES tb_permission(id) ON DELETE CASCADE;

-- Project foreign keys
ALTER TABLE tb_project
    ADD CONSTRAINT fk_project_owner
    FOREIGN KEY (owner_id) REFERENCES tb_user(id);

-- Project member foreign keys
ALTER TABLE tb_project_member
    ADD CONSTRAINT fk_project_member_project
    FOREIGN KEY (project_id) REFERENCES tb_project(id) ON DELETE CASCADE;

ALTER TABLE tb_project_member
    ADD CONSTRAINT fk_project_member_user
    FOREIGN KEY (user_id) REFERENCES tb_user(id) ON DELETE CASCADE;

ALTER TABLE tb_project_member
    ADD CONSTRAINT fk_project_member_role
    FOREIGN KEY (role_id) REFERENCES tb_role(id);

-- Document foreign keys
ALTER TABLE tb_document
    ADD CONSTRAINT fk_document_project
    FOREIGN KEY (project_id) REFERENCES tb_project(id) ON DELETE CASCADE;

ALTER TABLE tb_document
    ADD CONSTRAINT fk_document_creator
    FOREIGN KEY (creator_id) REFERENCES tb_user(id);

-- Document version foreign keys
ALTER TABLE tb_document_version
    ADD CONSTRAINT fk_document_version_document
    FOREIGN KEY (document_id) REFERENCES tb_document(id) ON DELETE CASCADE;

ALTER TABLE tb_document_version
    ADD CONSTRAINT fk_document_version_creator
    FOREIGN KEY (creator_id) REFERENCES tb_user(id);

-- ================================================================
-- Section 7: Create Update Trigger for update_time
-- ================================================================

-- Function to automatically update update_time
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply trigger to all tables with update_time column
CREATE TRIGGER update_user_modtime
    BEFORE UPDATE ON tb_user
    FOR EACH ROW EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER update_role_modtime
    BEFORE UPDATE ON tb_role
    FOR EACH ROW EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER update_permission_modtime
    BEFORE UPDATE ON tb_permission
    FOR EACH ROW EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER update_project_modtime
    BEFORE UPDATE ON tb_project
    FOR EACH ROW EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER update_project_member_modtime
    BEFORE UPDATE ON tb_project_member
    FOR EACH ROW EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER update_document_modtime
    BEFORE UPDATE ON tb_document
    FOR EACH ROW EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER update_document_version_modtime
    BEFORE UPDATE ON tb_document_version
    FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- ================================================================
-- Migration Complete
-- ================================================================