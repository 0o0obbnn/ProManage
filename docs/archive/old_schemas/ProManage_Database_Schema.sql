-- ProManage 项目管理系统数据库模式设计
-- 版本: V1.0
-- 创建日期: 2024-12-30
-- 架构师: Senior Full-Stack Architect

-- ================================
-- 数据库扩展和配置
-- ================================

-- 启用必要的 PostgreSQL 扩展
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";           -- UUID 生成
CREATE EXTENSION IF NOT EXISTS "pgcrypto";            -- 加密支持
CREATE EXTENSION IF NOT EXISTS "pg_trgm";             -- 模糊匹配
CREATE EXTENSION IF NOT EXISTS "ltree";               -- 层次数据
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";   -- SQL 性能统计

-- ================================
-- 枚举类型定义
-- ================================

-- 用户状态
CREATE TYPE user_status AS ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'PENDING_VERIFICATION');

-- 项目状态
CREATE TYPE project_status AS ENUM ('PLANNING', 'ACTIVE', 'ON_HOLD', 'COMPLETED', 'CANCELLED');

-- 文档状态
CREATE TYPE document_status AS ENUM ('DRAFT', 'UNDER_REVIEW', 'APPROVED', 'ARCHIVED', 'DEPRECATED');

-- 变更请求状态
CREATE TYPE change_request_status AS ENUM ('DRAFT', 'SUBMITTED', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'IMPLEMENTED', 'CLOSED');

-- 变更影响程度
CREATE TYPE impact_level AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL');

-- 任务状态
CREATE TYPE task_status AS ENUM ('TODO', 'IN_PROGRESS', 'UNDER_REVIEW', 'COMPLETED', 'CANCELLED', 'BLOCKED');

-- 任务优先级
CREATE TYPE task_priority AS ENUM ('LOW', 'NORMAL', 'HIGH', 'URGENT');

-- 测试用例状态
CREATE TYPE test_case_status AS ENUM ('DRAFT', 'ACTIVE', 'DEPRECATED', 'ARCHIVED');

-- 测试执行状态
CREATE TYPE test_execution_status AS ENUM ('PENDING', 'RUNNING', 'PASSED', 'FAILED', 'SKIPPED', 'BLOCKED');

-- 通知类型
CREATE TYPE notification_type AS ENUM ('SYSTEM', 'PROJECT', 'TASK', 'DOCUMENT', 'CHANGE_REQUEST', 'TEST', 'MENTION');

-- 通知渠道
CREATE TYPE notification_channel AS ENUM ('IN_APP', 'EMAIL', 'SLACK', 'TEAMS', 'WEBHOOK');

-- 通知状态
CREATE TYPE notification_status AS ENUM ('PENDING', 'SENT', 'DELIVERED', 'READ', 'FAILED');

-- 文件存储类型
CREATE TYPE file_storage_type AS ENUM ('LOCAL', 'MINIO', 'AWS_S3', 'AZURE_BLOB');

-- ================================
-- 核心业务表
-- ================================

-- 组织表（多租户支持）
CREATE TABLE organizations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    website VARCHAR(500),
    logo_url VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    settings JSONB DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE NULL
);

-- 用户表
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id),
    username VARCHAR(50) NOT NULL,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    avatar_url VARCHAR(500),
    phone VARCHAR(20),
    timezone VARCHAR(50) DEFAULT 'UTC',
    locale VARCHAR(10) DEFAULT 'zh_CN',
    status user_status DEFAULT 'ACTIVE',
    last_login_at TIMESTAMP WITH TIME ZONE,
    email_verified_at TIMESTAMP WITH TIME ZONE,
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    preferences JSONB DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE NULL,

    CONSTRAINT users_email_org_unique UNIQUE (organization_id, email),
    CONSTRAINT users_username_org_unique UNIQUE (organization_id, username)
);

-- 角色表
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id),
    name VARCHAR(50) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    is_system_role BOOLEAN DEFAULT FALSE,
    permissions JSONB DEFAULT '[]',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE NULL,

    CONSTRAINT roles_name_org_unique UNIQUE (organization_id, name)
);

-- 权限表
CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    resource VARCHAR(50) NOT NULL,        -- 资源类型: DOCUMENT, TASK, CHANGE_REQUEST 等
    action VARCHAR(50) NOT NULL,          -- 操作: CREATE, READ, UPDATE, DELETE
    scope VARCHAR(50) NOT NULL,           -- 范围: GLOBAL, PROJECT, SELF
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT permissions_unique UNIQUE (resource, action, scope)
);

-- 角色权限关联表
CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (role_id, permission_id)
);

-- 项目表
CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id),
    name VARCHAR(200) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    description TEXT,
    status project_status DEFAULT 'PLANNING',
    owner_id BIGINT NOT NULL REFERENCES users(id),
    start_date DATE,
    end_date DATE,
    budget DECIMAL(15,2),
    avatar_url VARCHAR(500),
    settings JSONB DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE NULL,

    CONSTRAINT projects_slug_org_unique UNIQUE (organization_id, slug)
);

-- 项目成员表
CREATE TABLE project_members (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id),
    joined_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    invited_by BIGINT REFERENCES users(id),
    is_active BOOLEAN DEFAULT TRUE,

    CONSTRAINT project_members_unique UNIQUE (project_id, user_id)
);

-- 文档分类表
CREATE TABLE document_categories (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    parent_id BIGINT REFERENCES document_categories(id),
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE NULL,

    CONSTRAINT doc_categories_name_project_unique UNIQUE (project_id, name, parent_id)
);

-- 文档表
CREATE TABLE documents (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    category_id BIGINT REFERENCES document_categories(id),
    title VARCHAR(500) NOT NULL,
    slug VARCHAR(200) NOT NULL,
    content TEXT,
    content_type VARCHAR(50) DEFAULT 'markdown',   -- markdown, html, rich_text
    status document_status DEFAULT 'DRAFT',
    version VARCHAR(20) DEFAULT '1.0.0',
    author_id BIGINT NOT NULL REFERENCES users(id),
    reviewer_id BIGINT REFERENCES users(id),
    tags VARCHAR(100)[] DEFAULT '{}',
    metadata JSONB DEFAULT '{}',
    search_vector tsvector,                        -- 全文搜索向量
    view_count INTEGER DEFAULT 0,
    like_count INTEGER DEFAULT 0,
    is_template BOOLEAN DEFAULT FALSE,
    template_data JSONB DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP WITH TIME ZONE,
    deleted_at TIMESTAMP WITH TIME ZONE NULL,

    CONSTRAINT documents_slug_project_unique UNIQUE (project_id, slug)
);

-- 文档版本表
CREATE TABLE document_versions (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    version VARCHAR(20) NOT NULL,
    title VARCHAR(500) NOT NULL,
    content TEXT,
    content_type VARCHAR(50) NOT NULL,
    author_id BIGINT NOT NULL REFERENCES users(id),
    changelog TEXT,
    file_size BIGINT,
    checksum VARCHAR(64),
    is_current BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT doc_versions_unique UNIQUE (document_id, version)
);

-- 文档附件表
CREATE TABLE document_attachments (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    filename VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(1000) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    storage_type file_storage_type DEFAULT 'LOCAL',
    uploaded_by BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE NULL
);

-- 文档关联表（文档间关系）
CREATE TABLE document_relations (
    id BIGSERIAL PRIMARY KEY,
    source_document_id BIGINT NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    target_document_id BIGINT NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    relation_type VARCHAR(50) NOT NULL,              -- REFERENCES, DEPENDS_ON, SUPERSEDES, RELATED
    description TEXT,
    created_by BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT doc_relations_unique UNIQUE (source_document_id, target_document_id, relation_type),
    CONSTRAINT doc_relations_no_self_ref CHECK (source_document_id != target_document_id)
);

-- 变更请求表
CREATE TABLE change_requests (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    title VARCHAR(500) NOT NULL,
    description TEXT NOT NULL,
    reason TEXT,
    status change_request_status DEFAULT 'DRAFT',
    priority task_priority DEFAULT 'NORMAL',
    impact_level impact_level DEFAULT 'MEDIUM',
    requester_id BIGINT NOT NULL REFERENCES users(id),
    assignee_id BIGINT REFERENCES users(id),
    reviewer_id BIGINT REFERENCES users(id),
    estimated_effort INTEGER,                         -- 预估工时（小时）
    actual_effort INTEGER,                           -- 实际工时（小时）
    implementation_date DATE,
    tags VARCHAR(100)[] DEFAULT '{}',
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    submitted_at TIMESTAMP WITH TIME ZONE,
    approved_at TIMESTAMP WITH TIME ZONE,
    implemented_at TIMESTAMP WITH TIME ZONE,
    deleted_at TIMESTAMP WITH TIME ZONE NULL
);

-- 变更影响分析表
CREATE TABLE change_request_impacts (
    id BIGSERIAL PRIMARY KEY,
    change_request_id BIGINT NOT NULL REFERENCES change_requests(id) ON DELETE CASCADE,
    entity_type VARCHAR(50) NOT NULL,                -- DOCUMENT, TASK, TEST_CASE, USER_STORY
    entity_id BIGINT NOT NULL,
    impact_level impact_level NOT NULL,
    impact_description TEXT,
    confidence_score DECIMAL(3,2) DEFAULT 0.5,       -- 影响分析置信度 0-1
    is_verified BOOLEAN DEFAULT FALSE,               -- 是否已人工验证
    verified_by BIGINT REFERENCES users(id),
    verified_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 变更审批流程表
CREATE TABLE change_approvals (
    id BIGSERIAL PRIMARY KEY,
    change_request_id BIGINT NOT NULL REFERENCES change_requests(id) ON DELETE CASCADE,
    approver_id BIGINT NOT NULL REFERENCES users(id),
    approval_step INTEGER NOT NULL,                  -- 审批步骤顺序
    status VARCHAR(20) DEFAULT 'PENDING',            -- PENDING, APPROVED, REJECTED
    comments TEXT,
    approved_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT change_approvals_unique UNIQUE (change_request_id, approver_id, approval_step)
);

-- 任务表
CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    parent_task_id BIGINT REFERENCES tasks(id),      -- 支持任务层次结构
    title VARCHAR(500) NOT NULL,
    description TEXT,
    status task_status DEFAULT 'TODO',
    priority task_priority DEFAULT 'NORMAL',
    assignee_id BIGINT REFERENCES users(id),
    reporter_id BIGINT NOT NULL REFERENCES users(id),
    estimated_hours DECIMAL(6,2),
    actual_hours DECIMAL(6,2),
    progress_percentage INTEGER DEFAULT 0 CHECK (progress_percentage >= 0 AND progress_percentage <= 100),
    start_date DATE,
    due_date DATE,
    completed_at TIMESTAMP WITH TIME ZONE,
    tags VARCHAR(100)[] DEFAULT '{}',
    labels JSONB DEFAULT '[]',
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE NULL
);

-- 任务依赖关系表
CREATE TABLE task_dependencies (
    id BIGSERIAL PRIMARY KEY,
    prerequisite_task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    dependent_task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    dependency_type VARCHAR(20) DEFAULT 'FINISH_TO_START',  -- FINISH_TO_START, START_TO_START, FINISH_TO_FINISH, START_TO_FINISH
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT task_deps_unique UNIQUE (prerequisite_task_id, dependent_task_id),
    CONSTRAINT task_deps_no_self_ref CHECK (prerequisite_task_id != dependent_task_id)
);

-- 任务评论表
CREATE TABLE task_comments (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    author_id BIGINT NOT NULL REFERENCES users(id),
    content TEXT NOT NULL,
    parent_comment_id BIGINT REFERENCES task_comments(id),
    is_internal BOOLEAN DEFAULT FALSE,               -- 内部评论，外部用户不可见
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE NULL
);

-- 测试用例表
CREATE TABLE test_cases (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    preconditions TEXT,
    test_steps JSONB NOT NULL,                       -- JSON 格式的测试步骤
    expected_results TEXT,
    status test_case_status DEFAULT 'DRAFT',
    priority task_priority DEFAULT 'NORMAL',
    test_type VARCHAR(50) DEFAULT 'FUNCTIONAL',      -- FUNCTIONAL, INTEGRATION, PERFORMANCE, SECURITY
    automation_level VARCHAR(20) DEFAULT 'MANUAL',   -- MANUAL, SEMI_AUTO, AUTOMATED
    author_id BIGINT NOT NULL REFERENCES users(id),
    reviewer_id BIGINT REFERENCES users(id),
    tags VARCHAR(100)[] DEFAULT '{}',
    reuse_count INTEGER DEFAULT 0,                   -- 复用次数统计
    template_id BIGINT REFERENCES test_cases(id),    -- 基于模板创建
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE NULL
);

-- 测试用例关联表（支持测试用例复用关系）
CREATE TABLE test_case_relations (
    id BIGSERIAL PRIMARY KEY,
    source_test_case_id BIGINT NOT NULL REFERENCES test_cases(id) ON DELETE CASCADE,
    target_test_case_id BIGINT NOT NULL REFERENCES test_cases(id) ON DELETE CASCADE,
    relation_type VARCHAR(50) NOT NULL,              -- DERIVED_FROM, SIMILAR_TO, SUPERSEDES
    similarity_score DECIMAL(3,2),                   -- 相似度分数 0-1
    created_by BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT test_case_relations_unique UNIQUE (source_test_case_id, target_test_case_id, relation_type),
    CONSTRAINT test_case_relations_no_self_ref CHECK (source_test_case_id != target_test_case_id)
);

-- 测试执行表
CREATE TABLE test_executions (
    id BIGSERIAL PRIMARY KEY,
    test_case_id BIGINT NOT NULL REFERENCES test_cases(id) ON DELETE CASCADE,
    executor_id BIGINT NOT NULL REFERENCES users(id),
    status test_execution_status DEFAULT 'PENDING',
    actual_results TEXT,
    failure_reason TEXT,
    execution_time INTEGER,                          -- 执行时间（秒）
    environment VARCHAR(100),                        -- 执行环境
    browser_version VARCHAR(100),                    -- 浏览器版本（如适用）
    screenshots JSONB DEFAULT '[]',                  -- 截图文件列表
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 通知订阅表
CREATE TABLE subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    entity_type VARCHAR(50) NOT NULL,                -- PROJECT, DOCUMENT, TASK, CHANGE_REQUEST
    entity_id BIGINT NOT NULL,
    notification_types notification_type[] DEFAULT '{}',
    channels notification_channel[] DEFAULT '{IN_APP}',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT subscriptions_unique UNIQUE (user_id, entity_type, entity_id)
);

-- 通知表
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    recipient_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type notification_type NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    entity_type VARCHAR(50),
    entity_id BIGINT,
    sender_id BIGINT REFERENCES users(id),
    priority VARCHAR(10) DEFAULT 'NORMAL',            -- LOW, NORMAL, HIGH, URGENT
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP WITH TIME ZONE,
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 通知发送记录表
CREATE TABLE notification_deliveries (
    id BIGSERIAL PRIMARY KEY,
    notification_id BIGINT NOT NULL REFERENCES notifications(id) ON DELETE CASCADE,
    channel notification_channel NOT NULL,
    status notification_status DEFAULT 'PENDING',
    recipient_address VARCHAR(500),                  -- 邮箱地址、Slack 频道等
    delivery_attempts INTEGER DEFAULT 0,
    last_attempt_at TIMESTAMP WITH TIME ZONE,
    delivered_at TIMESTAMP WITH TIME ZONE,
    error_message TEXT,
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 活动日志表（审计日志）
CREATE TABLE activity_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    organization_id BIGINT NOT NULL REFERENCES organizations(id),
    project_id BIGINT REFERENCES projects(id),
    action VARCHAR(100) NOT NULL,                    -- CREATE, UPDATE, DELETE, VIEW, APPROVE, etc.
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    old_values JSONB,
    new_values JSONB,
    ip_address INET,
    user_agent TEXT,
    session_id VARCHAR(255),
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 系统配置表
CREATE TABLE system_settings (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT REFERENCES organizations(id),  -- NULL 表示全局设置
    setting_key VARCHAR(100) NOT NULL,
    setting_value JSONB NOT NULL,
    description TEXT,
    is_public BOOLEAN DEFAULT FALSE,                 -- 是否对普通用户可见
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT system_settings_unique UNIQUE (organization_id, setting_key)
);

-- ================================
-- 索引优化
-- ================================

-- 用户表索引
CREATE INDEX CONCURRENTLY idx_users_organization_id ON users(organization_id) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_users_email ON users(email) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_users_username ON users(username) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_users_status ON users(status) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_users_last_login ON users(last_login_at DESC) WHERE deleted_at IS NULL;

-- 项目表索引
CREATE INDEX CONCURRENTLY idx_projects_organization_id ON projects(organization_id) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_projects_owner_id ON projects(owner_id) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_projects_status ON projects(status) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_projects_created_at ON projects(created_at DESC) WHERE deleted_at IS NULL;

-- 项目成员索引
CREATE INDEX CONCURRENTLY idx_project_members_project_id ON project_members(project_id) WHERE is_active = TRUE;
CREATE INDEX CONCURRENTLY idx_project_members_user_id ON project_members(user_id) WHERE is_active = TRUE;
CREATE INDEX CONCURRENTLY idx_project_members_role_id ON project_members(role_id) WHERE is_active = TRUE;

-- 文档表索引
CREATE INDEX CONCURRENTLY idx_documents_project_id ON documents(project_id) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_documents_category_id ON documents(category_id) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_documents_author_id ON documents(author_id) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_documents_status ON documents(status) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_documents_created_at ON documents(created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_documents_updated_at ON documents(updated_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_documents_search_vector ON documents USING gin(search_vector);
CREATE INDEX CONCURRENTLY idx_documents_tags ON documents USING gin(tags);
CREATE INDEX CONCURRENTLY idx_documents_is_template ON documents(is_template) WHERE is_template = TRUE AND deleted_at IS NULL;

-- 复合索引优化常用查询
CREATE INDEX CONCURRENTLY idx_documents_project_status_created
ON documents(project_id, status, created_at DESC) WHERE deleted_at IS NULL;

-- 文档版本索引
CREATE INDEX CONCURRENTLY idx_document_versions_document_id ON document_versions(document_id);
CREATE INDEX CONCURRENTLY idx_document_versions_is_current ON document_versions(document_id, is_current) WHERE is_current = TRUE;

-- 变更请求索引
CREATE INDEX CONCURRENTLY idx_change_requests_project_id ON change_requests(project_id) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_change_requests_requester_id ON change_requests(requester_id) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_change_requests_assignee_id ON change_requests(assignee_id) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_change_requests_status ON change_requests(status) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_change_requests_priority ON change_requests(priority) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_change_requests_created_at ON change_requests(created_at DESC) WHERE deleted_at IS NULL;

-- 任务表索引
CREATE INDEX CONCURRENTLY idx_tasks_project_id ON tasks(project_id) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_tasks_assignee_id ON tasks(assignee_id) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_tasks_reporter_id ON tasks(reporter_id) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_tasks_status ON tasks(status) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_tasks_priority ON tasks(priority) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_tasks_parent_task_id ON tasks(parent_task_id) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_tasks_due_date ON tasks(due_date) WHERE deleted_at IS NULL AND due_date IS NOT NULL;

-- 测试用例索引
CREATE INDEX CONCURRENTLY idx_test_cases_project_id ON test_cases(project_id) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_test_cases_author_id ON test_cases(author_id) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_test_cases_status ON test_cases(status) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_test_cases_test_type ON test_cases(test_type) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_test_cases_template_id ON test_cases(template_id) WHERE template_id IS NOT NULL AND deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_test_cases_reuse_count ON test_cases(reuse_count DESC) WHERE deleted_at IS NULL;

-- 通知索引
CREATE INDEX CONCURRENTLY idx_notifications_recipient_id ON notifications(recipient_id);
CREATE INDEX CONCURRENTLY idx_notifications_is_read ON notifications(recipient_id, is_read, created_at DESC);
CREATE INDEX CONCURRENTLY idx_notifications_type ON notifications(type);
CREATE INDEX CONCURRENTLY idx_notifications_entity ON notifications(entity_type, entity_id);

-- 活动日志索引
CREATE INDEX CONCURRENTLY idx_activity_logs_user_id ON activity_logs(user_id);
CREATE INDEX CONCURRENTLY idx_activity_logs_organization_id ON activity_logs(organization_id);
CREATE INDEX CONCURRENTLY idx_activity_logs_project_id ON activity_logs(project_id);
CREATE INDEX CONCURRENTLY idx_activity_logs_entity ON activity_logs(entity_type, entity_id);
CREATE INDEX CONCURRENTLY idx_activity_logs_created_at ON activity_logs(created_at DESC);

-- 时间范围查询优化
CREATE INDEX CONCURRENTLY idx_activity_logs_date_range
ON activity_logs(organization_id, created_at DESC) WHERE created_at >= CURRENT_DATE - INTERVAL '30 days';

-- ================================
-- 触发器和函数
-- ================================

-- 更新时间戳函数
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 为需要的表添加更新时间戳触发器
CREATE TRIGGER update_organizations_updated_at BEFORE UPDATE ON organizations
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_projects_updated_at BEFORE UPDATE ON projects
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_documents_updated_at BEFORE UPDATE ON documents
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_change_requests_updated_at BEFORE UPDATE ON change_requests
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_tasks_updated_at BEFORE UPDATE ON tasks
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_test_cases_updated_at BEFORE UPDATE ON test_cases
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 文档搜索向量更新函数
CREATE OR REPLACE FUNCTION update_document_search_vector()
RETURNS TRIGGER AS $$
BEGIN
    NEW.search_vector := to_tsvector('simple',
        COALESCE(NEW.title, '') || ' ' ||
        COALESCE(NEW.content, '') || ' ' ||
        COALESCE(array_to_string(NEW.tags, ' '), '')
    );
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 文档搜索向量触发器
CREATE TRIGGER update_documents_search_vector
BEFORE INSERT OR UPDATE OF title, content, tags ON documents
FOR EACH ROW EXECUTE FUNCTION update_document_search_vector();

-- 测试用例复用计数更新函数
CREATE OR REPLACE FUNCTION update_test_case_reuse_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE test_cases
        SET reuse_count = reuse_count + 1
        WHERE id = NEW.source_test_case_id;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE test_cases
        SET reuse_count = GREATEST(0, reuse_count - 1)
        WHERE id = OLD.source_test_case_id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ language 'plpgsql';

-- 测试用例复用计数触发器
CREATE TRIGGER update_test_case_reuse_count_trigger
AFTER INSERT OR DELETE ON test_case_relations
FOR EACH ROW EXECUTE FUNCTION update_test_case_reuse_count();

-- 活动日志记录函数
CREATE OR REPLACE FUNCTION log_activity()
RETURNS TRIGGER AS $$
DECLARE
    action_name TEXT;
    old_data JSONB;
    new_data JSONB;
BEGIN
    -- 确定操作类型
    IF TG_OP = 'INSERT' THEN
        action_name := 'CREATE';
        old_data := NULL;
        new_data := to_jsonb(NEW);
    ELSIF TG_OP = 'UPDATE' THEN
        action_name := 'UPDATE';
        old_data := to_jsonb(OLD);
        new_data := to_jsonb(NEW);
    ELSIF TG_OP = 'DELETE' THEN
        action_name := 'DELETE';
        old_data := to_jsonb(OLD);
        new_data := NULL;
    END IF;

    -- 插入活动日志
    INSERT INTO activity_logs (
        user_id,
        organization_id,
        project_id,
        action,
        entity_type,
        entity_id,
        old_values,
        new_values,
        created_at
    ) VALUES (
        COALESCE(
            current_setting('app.current_user_id', true)::BIGINT,
            CASE WHEN TG_OP = 'DELETE' THEN NULL ELSE NEW.created_by END,
            CASE WHEN TG_OP = 'DELETE' THEN NULL ELSE NEW.author_id END,
            CASE WHEN TG_OP = 'DELETE' THEN NULL ELSE NEW.user_id END
        ),
        COALESCE(
            CASE WHEN TG_OP = 'DELETE' THEN OLD.organization_id ELSE NEW.organization_id END,
            current_setting('app.current_organization_id', true)::BIGINT
        ),
        CASE WHEN TG_OP = 'DELETE' THEN OLD.project_id ELSE NEW.project_id END,
        action_name,
        TG_TABLE_NAME,
        CASE WHEN TG_OP = 'DELETE' THEN OLD.id ELSE NEW.id END,
        old_data,
        new_data,
        CURRENT_TIMESTAMP
    );

    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    END IF;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- ================================
-- 行级安全策略 (RLS)
-- ================================

-- 启用行级安全
ALTER TABLE organizations ENABLE ROW LEVEL SECURITY;
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE projects ENABLE ROW LEVEL SECURITY;
ALTER TABLE documents ENABLE ROW LEVEL SECURITY;
ALTER TABLE tasks ENABLE ROW LEVEL SECURITY;
ALTER TABLE change_requests ENABLE ROW LEVEL SECURITY;
ALTER TABLE test_cases ENABLE ROW LEVEL SECURITY;

-- 组织级别的安全策略
CREATE POLICY organization_isolation ON organizations
FOR ALL TO application_user
USING (id = current_setting('app.current_organization_id', true)::BIGINT);

-- 用户访问策略
CREATE POLICY user_access ON users
FOR ALL TO application_user
USING (organization_id = current_setting('app.current_organization_id', true)::BIGINT);

-- 项目访问策略
CREATE POLICY project_member_access ON projects
FOR ALL TO application_user
USING (
    organization_id = current_setting('app.current_organization_id', true)::BIGINT
    AND (
        id IN (
            SELECT project_id FROM project_members
            WHERE user_id = current_setting('app.current_user_id', true)::BIGINT
            AND is_active = TRUE
        )
        OR owner_id = current_setting('app.current_user_id', true)::BIGINT
    )
);

-- 文档访问策略
CREATE POLICY document_project_access ON documents
FOR ALL TO application_user
USING (
    project_id IN (
        SELECT project_id FROM project_members
        WHERE user_id = current_setting('app.current_user_id', true)::BIGINT
        AND is_active = TRUE
    )
);

-- ================================
-- 初始数据插入
-- ================================

-- 插入基础权限
INSERT INTO permissions (resource, action, scope, description) VALUES
-- 文档权限
('DOCUMENT', 'CREATE', 'PROJECT', '在项目中创建文档'),
('DOCUMENT', 'READ', 'PROJECT', '阅读项目文档'),
('DOCUMENT', 'UPDATE', 'PROJECT', '更新项目文档'),
('DOCUMENT', 'DELETE', 'PROJECT', '删除项目文档'),
('DOCUMENT', 'UPDATE', 'SELF', '更新自己创建的文档'),
('DOCUMENT', 'DELETE', 'SELF', '删除自己创建的文档'),

-- 任务权限
('TASK', 'CREATE', 'PROJECT', '在项目中创建任务'),
('TASK', 'READ', 'PROJECT', '查看项目任务'),
('TASK', 'UPDATE', 'PROJECT', '更新项目任务'),
('TASK', 'DELETE', 'PROJECT', '删除项目任务'),
('TASK', 'ASSIGN', 'PROJECT', '分配项目任务'),

-- 变更请求权限
('CHANGE_REQUEST', 'CREATE', 'PROJECT', '创建变更请求'),
('CHANGE_REQUEST', 'READ', 'PROJECT', '查看变更请求'),
('CHANGE_REQUEST', 'UPDATE', 'PROJECT', '更新变更请求'),
('CHANGE_REQUEST', 'APPROVE', 'PROJECT', '审批变更请求'),
('CHANGE_REQUEST', 'IMPLEMENT', 'PROJECT', '实施变更请求'),

-- 测试用例权限
('TEST_CASE', 'CREATE', 'PROJECT', '创建测试用例'),
('TEST_CASE', 'READ', 'PROJECT', '查看测试用例'),
('TEST_CASE', 'UPDATE', 'PROJECT', '更新测试用例'),
('TEST_CASE', 'DELETE', 'PROJECT', '删除测试用例'),
('TEST_CASE', 'EXECUTE', 'PROJECT', '执行测试用例'),

-- 项目管理权限
('PROJECT', 'CREATE', 'GLOBAL', '创建新项目'),
('PROJECT', 'READ', 'PROJECT', '查看项目信息'),
('PROJECT', 'UPDATE', 'PROJECT', '更新项目设置'),
('PROJECT', 'DELETE', 'PROJECT', '删除项目'),
('PROJECT', 'MANAGE_MEMBERS', 'PROJECT', '管理项目成员'),

-- 用户管理权限
('USER', 'READ', 'GLOBAL', '查看用户列表'),
('USER', 'UPDATE', 'GLOBAL', '更新用户信息'),
('USER', 'DELETE', 'GLOBAL', '删除用户'),
('USER', 'INVITE', 'GLOBAL', '邀请新用户');

-- 创建默认组织（示例数据）
INSERT INTO organizations (name, slug, description) VALUES
('示例组织', 'example-org', '这是一个示例组织');

-- 获取组织ID用于后续插入
DO $$
DECLARE
    org_id BIGINT;
BEGIN
    SELECT id INTO org_id FROM organizations WHERE slug = 'example-org';

    -- 创建系统角色
    INSERT INTO roles (organization_id, name, display_name, description, is_system_role) VALUES
    (org_id, 'SUPER_ADMIN', '超级管理员', '系统最高权限角色', TRUE),
    (org_id, 'PROJECT_MANAGER', '项目经理', '项目管理和协调角色', TRUE),
    (org_id, 'DEVELOPER', '开发人员', '软件开发角色', TRUE),
    (org_id, 'TESTER', '测试人员', '软件测试角色', TRUE),
    (org_id, 'UI_DESIGNER', 'UI设计师', '用户界面设计角色', TRUE),
    (org_id, 'DEVOPS', '运维人员', '系统运维角色', TRUE),
    (org_id, 'THIRD_PARTY', '第三方人员', '外部合作伙伴角色', TRUE);
END $$;

-- ================================
-- 数据库维护函数
-- ================================

-- 清理过期通知
CREATE OR REPLACE FUNCTION cleanup_old_notifications()
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    -- 删除30天前的已读通知
    DELETE FROM notifications
    WHERE is_read = TRUE
    AND read_at < CURRENT_TIMESTAMP - INTERVAL '30 days';

    GET DIAGNOSTICS deleted_count = ROW_COUNT;

    -- 删除90天前的未读通知
    DELETE FROM notifications
    WHERE is_read = FALSE
    AND created_at < CURRENT_TIMESTAMP - INTERVAL '90 days';

    GET DIAGNOSTICS deleted_count = deleted_count + ROW_COUNT;

    RETURN deleted_count;
END;
$$ language 'plpgsql';

-- 重建搜索向量
CREATE OR REPLACE FUNCTION rebuild_search_vectors()
RETURNS INTEGER AS $$
DECLARE
    updated_count INTEGER;
BEGIN
    UPDATE documents
    SET search_vector = to_tsvector('simple',
        COALESCE(title, '') || ' ' ||
        COALESCE(content, '') || ' ' ||
        COALESCE(array_to_string(tags, ' '), '')
    )
    WHERE deleted_at IS NULL;

    GET DIAGNOSTICS updated_count = ROW_COUNT;
    RETURN updated_count;
END;
$$ language 'plpgsql';

-- 数据库健康检查函数
CREATE OR REPLACE FUNCTION database_health_check()
RETURNS TABLE (
    metric_name TEXT,
    metric_value TEXT,
    status TEXT
) AS $$
BEGIN
    -- 检查连接数
    RETURN QUERY
    SELECT
        'active_connections'::TEXT,
        (SELECT count(*)::TEXT FROM pg_stat_activity WHERE state = 'active'),
        CASE
            WHEN (SELECT count(*) FROM pg_stat_activity WHERE state = 'active') > 80 THEN 'WARNING'
            ELSE 'OK'
        END;

    -- 检查数据库大小
    RETURN QUERY
    SELECT
        'database_size_mb'::TEXT,
        (SELECT round(pg_database_size(current_database())/1024/1024)::TEXT),
        'INFO'::TEXT;

    -- 检查最大表大小
    RETURN QUERY
    SELECT
        'largest_table'::TEXT,
        (SELECT schemaname||'.'||tablename FROM pg_tables
         WHERE schemaname = 'public'
         ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC
         LIMIT 1),
        'INFO'::TEXT;
END;
$$ language 'plpgsql';

-- ================================
-- 性能监控视图
-- ================================

-- 创建性能监控视图
CREATE VIEW performance_metrics AS
SELECT
    'documents_per_project' AS metric_name,
    p.name AS project_name,
    COUNT(d.id)::TEXT AS metric_value
FROM projects p
LEFT JOIN documents d ON p.id = d.project_id AND d.deleted_at IS NULL
WHERE p.deleted_at IS NULL
GROUP BY p.id, p.name

UNION ALL

SELECT
    'active_users_last_30_days' AS metric_name,
    'global' AS project_name,
    COUNT(DISTINCT user_id)::TEXT AS metric_value
FROM activity_logs
WHERE created_at >= CURRENT_TIMESTAMP - INTERVAL '30 days'

UNION ALL

SELECT
    'change_requests_by_status' AS metric_name,
    status::TEXT AS project_name,
    COUNT(*)::TEXT AS metric_value
FROM change_requests
WHERE deleted_at IS NULL
GROUP BY status;

-- 创建用户活动统计视图
CREATE VIEW user_activity_stats AS
SELECT
    u.id,
    u.username,
    u.email,
    COUNT(al.id) AS total_activities,
    MAX(al.created_at) AS last_activity,
    COUNT(DISTINCT al.project_id) AS projects_involved
FROM users u
LEFT JOIN activity_logs al ON u.id = al.user_id
WHERE u.deleted_at IS NULL
GROUP BY u.id, u.username, u.email;

-- ================================
-- 注释说明
-- ================================

COMMENT ON DATABASE postgres IS 'ProManage 项目管理系统数据库';

COMMENT ON TABLE organizations IS '组织表 - 支持多租户架构';
COMMENT ON TABLE users IS '用户表 - 存储系统用户信息';
COMMENT ON TABLE roles IS '角色表 - 定义用户角色和权限';
COMMENT ON TABLE permissions IS '权限表 - 定义系统权限';
COMMENT ON TABLE projects IS '项目表 - 存储项目基本信息';
COMMENT ON TABLE project_members IS '项目成员表 - 用户与项目的关联关系';
COMMENT ON TABLE documents IS '文档表 - 存储项目文档信息';
COMMENT ON TABLE document_versions IS '文档版本表 - 文档版本控制';
COMMENT ON TABLE change_requests IS '变更请求表 - 管理项目变更';
COMMENT ON TABLE change_request_impacts IS '变更影响分析表 - 智能影响分析数据';
COMMENT ON TABLE tasks IS '任务表 - 项目任务管理';
COMMENT ON TABLE test_cases IS '测试用例表 - 测试用例管理和复用';
COMMENT ON TABLE notifications IS '通知表 - 系统通知消息';
COMMENT ON TABLE activity_logs IS '活动日志表 - 系统操作审计';

COMMENT ON FUNCTION update_updated_at_column() IS '自动更新 updated_at 字段的触发器函数';
COMMENT ON FUNCTION update_document_search_vector() IS '自动更新文档搜索向量的触发器函数';
COMMENT ON FUNCTION cleanup_old_notifications() IS '清理过期通知的维护函数';
COMMENT ON FUNCTION database_health_check() IS '数据库健康检查函数';

-- ================================
-- 数据库配置建议
-- ================================

/*
生产环境 PostgreSQL 配置建议:

postgresql.conf:
```
# 连接和认证
max_connections = 200
listen_addresses = '*'

# 内存配置
shared_buffers = 256MB          # 25% of RAM
effective_cache_size = 1GB      # 75% of RAM
work_mem = 4MB
maintenance_work_mem = 64MB

# 检查点配置
checkpoint_completion_target = 0.9
wal_buffers = 16MB
default_statistics_target = 100

# 查询规划器
random_page_cost = 1.1          # SSD优化
effective_io_concurrency = 200  # SSD优化

# 日志配置
logging_collector = on
log_destination = 'csvlog'
log_directory = 'pg_log'
log_filename = 'postgresql-%Y-%m-%d_%H%M%S.log'
log_min_duration_statement = 1000ms
log_line_prefix = '%t [%p]: [%l-1] user=%u,db=%d,app=%a,client=%h '

# 慢查询日志
log_statement = 'mod'
log_duration = on

# 复制配置（主从复制）
wal_level = replica
max_wal_senders = 3
max_replication_slots = 3
hot_standby = on
```

pg_hba.conf:
```
# TYPE  DATABASE        USER            ADDRESS                 METHOD
local   all             postgres                                peer
local   all             all                                     md5
host    all             all             127.0.0.1/32            md5
host    all             all             ::1/128                 md5
host    replication     replicator      192.168.1.0/24          md5
```

连接池配置 (PgBouncer):
```
[databases]
promanage = host=localhost port=5432 dbname=promanage

[pgbouncer]
listen_addr = *
listen_port = 6432
auth_type = md5
auth_file = /etc/pgbouncer/userlist.txt
admin_users = pgbouncer
pool_mode = transaction
max_client_conn = 1000
default_pool_size = 25
min_pool_size = 5
reserve_pool_size = 5
reserve_pool_timeout = 5
max_db_connections = 50
max_user_connections = 50
server_reset_query = DISCARD ALL
server_check_query = SELECT 1
server_check_delay = 30
```

监控建议:
- 使用 pg_stat_statements 扩展监控慢查询
- 监控连接数、缓存命中率、磁盘使用情况
- 设置 CPU、内存、磁盘 I/O 告警
- 定期执行 VACUUM 和 ANALYZE
- 监控复制延迟（如果使用主从复制）

备份策略:
- 每日全量备份
- 连续 WAL 归档
- 定期测试恢复流程
- 异地备份存储
*/