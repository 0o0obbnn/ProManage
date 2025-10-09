-- =====================================================
-- ProManage Database Schema - PostgreSQL 15+
-- =====================================================
-- Project Management System with Document Management,
-- Change Control, Test Management, and RBAC
-- =====================================================

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
CREATE EXTENSION IF NOT EXISTS "unaccent";
CREATE EXTENSION IF NOT EXISTS "btree_gin";

-- =====================================================
-- DOMAIN TYPES AND ENUMS
-- =====================================================

-- User status enumeration
CREATE TYPE user_status_enum AS ENUM (
    'ACTIVE',
    'INACTIVE',
    'PENDING',
    'SUSPENDED'
);

-- Document status enumeration
CREATE TYPE document_status_enum AS ENUM (
    'DRAFT',
    'REVIEW',
    'APPROVED',
    'PUBLISHED',
    'ARCHIVED'
);

-- Change request status enumeration
CREATE TYPE change_request_status_enum AS ENUM (
    'DRAFT',
    'PENDING_APPROVAL',
    'UNDER_REVIEW',
    'APPROVED',
    'REJECTED',
    'IMPLEMENTED',
    'CLOSED'
);

-- Change priority enumeration
CREATE TYPE change_priority_enum AS ENUM (
    'LOW',
    'MEDIUM',
    'HIGH',
    'CRITICAL'
);

-- Task status enumeration
CREATE TYPE task_status_enum AS ENUM (
    'TODO',
    'IN_PROGRESS',
    'REVIEW',
    'TESTING',
    'DONE',
    'CANCELLED'
);

-- Task priority enumeration
CREATE TYPE task_priority_enum AS ENUM (
    'LOW',
    'MEDIUM',
    'HIGH',
    'URGENT'
);

-- Test case status enumeration
CREATE TYPE test_case_status_enum AS ENUM (
    'DRAFT',
    'ACTIVE',
    'DEPRECATED',
    'ARCHIVED'
);

-- Test execution status enumeration
CREATE TYPE test_execution_status_enum AS ENUM (
    'NOT_RUN',
    'PASSED',
    'FAILED',
    'BLOCKED',
    'SKIPPED'
);

-- Notification type enumeration
CREATE TYPE notification_type_enum AS ENUM (
    'DOCUMENT_CREATED',
    'DOCUMENT_UPDATED',
    'DOCUMENT_COMMENTED',
    'CHANGE_REQUEST_CREATED',
    'CHANGE_REQUEST_APPROVED',
    'CHANGE_REQUEST_REJECTED',
    'TASK_ASSIGNED',
    'TASK_COMPLETED',
    'TEST_EXECUTION_FAILED',
    'MENTION',
    'SYSTEM_ALERT'
);

-- Notification channel enumeration
CREATE TYPE notification_channel_enum AS ENUM (
    'IN_APP',
    'EMAIL',
    'SLACK',
    'TEAMS',
    'SMS'
);

-- File storage provider enumeration
CREATE TYPE storage_provider_enum AS ENUM (
    'LOCAL',
    'MINIO',
    'AWS_S3',
    'AZURE_BLOB',
    'GCP_STORAGE'
);

-- =====================================================
-- AUDIT FUNCTIONS
-- =====================================================

-- Audit trigger function
CREATE OR REPLACE FUNCTION audit_trigger()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'UPDATE' THEN
        NEW.updated_at = CURRENT_TIMESTAMP;
        NEW.updated_by = COALESCE(NEW.updated_by, OLD.updated_by);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Soft delete trigger function
CREATE OR REPLACE FUNCTION soft_delete_trigger()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        UPDATE TABLE_NAME SET
            deleted_at = CURRENT_TIMESTAMP,
            deleted_by = current_setting('app.current_user_id', true)::BIGINT
        WHERE id = OLD.id;
        RETURN NULL; -- Prevent actual deletion
    END IF;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- CORE TABLES
-- =====================================================

-- Organizations/Tenants Table
CREATE TABLE organizations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    logo_url TEXT,
    website_url TEXT,
    contact_email VARCHAR(255),
    settings JSONB DEFAULT '{}',
    is_active BOOLEAN DEFAULT TRUE,
    subscription_plan VARCHAR(50) DEFAULT 'FREE',
    subscription_expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    deleted_by BIGINT
);

-- Users Table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id),
    username VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    display_name VARCHAR(200),
    avatar_url TEXT,
    phone VARCHAR(20),
    timezone VARCHAR(50) DEFAULT 'UTC',
    locale VARCHAR(10) DEFAULT 'en_US',
    status user_status_enum DEFAULT 'PENDING',
    last_login_at TIMESTAMP,
    last_active_at TIMESTAMP,
    email_verified_at TIMESTAMP,
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    two_factor_secret VARCHAR(32),
    preferences JSONB DEFAULT '{}',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    deleted_by BIGINT,
    UNIQUE(organization_id, username),
    UNIQUE(organization_id, email)
);

-- System Roles Table
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT REFERENCES organizations(id),
    name VARCHAR(50) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    is_system_role BOOLEAN DEFAULT FALSE,
    permissions JSONB DEFAULT '[]',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    deleted_by BIGINT,
    UNIQUE(organization_id, name)
);

-- Permissions Table
CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    resource VARCHAR(50) NOT NULL,  -- DOCUMENT, TASK, CHANGE, PROJECT, USER
    action VARCHAR(50) NOT NULL,    -- CREATE, READ, UPDATE, DELETE, APPROVE
    scope VARCHAR(50) NOT NULL,     -- GLOBAL, PROJECT, SELF
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(resource, action, scope)
);

-- Role Permissions Junction Table
CREATE TABLE role_permissions (
    role_id BIGINT REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT REFERENCES permissions(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, permission_id)
);

-- Projects Table
CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    description TEXT,
    objectives TEXT,
    start_date DATE,
    end_date DATE,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    priority task_priority_enum DEFAULT 'MEDIUM',
    budget DECIMAL(15,2),
    currency VARCHAR(3) DEFAULT 'USD',
    project_manager_id BIGINT REFERENCES users(id),
    parent_project_id BIGINT REFERENCES projects(id),
    settings JSONB DEFAULT '{}',
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id),
    deleted_at TIMESTAMP,
    deleted_by BIGINT REFERENCES users(id),
    UNIQUE(organization_id, slug)
);

-- Project Members Junction Table
CREATE TABLE project_members (
    project_id BIGINT REFERENCES projects(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT REFERENCES roles(id),
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    invited_by BIGINT REFERENCES users(id),
    is_active BOOLEAN DEFAULT TRUE,
    PRIMARY KEY (project_id, user_id)
);

-- Document Categories Table
CREATE TABLE document_categories (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id),
    project_id BIGINT REFERENCES projects(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    parent_category_id BIGINT REFERENCES document_categories(id),
    sort_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id),
    deleted_at TIMESTAMP,
    deleted_by BIGINT REFERENCES users(id)
);

-- Documents Table
CREATE TABLE documents (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id),
    project_id BIGINT REFERENCES projects(id),
    category_id BIGINT REFERENCES document_categories(id),
    title VARCHAR(500) NOT NULL,
    content TEXT,
    content_type VARCHAR(50) DEFAULT 'text/html',
    version VARCHAR(20) DEFAULT '1.0.0',
    status document_status_enum DEFAULT 'DRAFT',
    priority task_priority_enum DEFAULT 'MEDIUM',
    tags TEXT[] DEFAULT '{}',
    file_path TEXT,
    file_size BIGINT,
    file_mime_type VARCHAR(100),
    file_checksum VARCHAR(64),
    storage_provider storage_provider_enum DEFAULT 'LOCAL',
    is_template BOOLEAN DEFAULT FALSE,
    template_id BIGINT REFERENCES documents(id),
    parent_document_id BIGINT REFERENCES documents(id),
    published_at TIMESTAMP,
    expires_at TIMESTAMP,
    review_due_at TIMESTAMP,
    search_vector tsvector,
    metadata JSONB DEFAULT '{}',
    settings JSONB DEFAULT '{}',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id),
    deleted_at TIMESTAMP,
    deleted_by BIGINT REFERENCES users(id)
);

-- Document Versions Table
CREATE TABLE document_versions (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    version VARCHAR(20) NOT NULL,
    title VARCHAR(500) NOT NULL,
    content TEXT,
    content_type VARCHAR(50) DEFAULT 'text/html',
    file_path TEXT,
    file_size BIGINT,
    file_checksum VARCHAR(64),
    change_summary TEXT,
    is_major_version BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL REFERENCES users(id),
    UNIQUE(document_id, version)
);

-- Document Permissions Table (for fine-grained access control)
CREATE TABLE document_permissions (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id),
    role_id BIGINT REFERENCES roles(id),
    permission_type VARCHAR(20) NOT NULL, -- READ, WRITE, COMMENT, APPROVE
    granted_by BIGINT REFERENCES users(id),
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    CHECK ((user_id IS NOT NULL) OR (role_id IS NOT NULL))
);

-- Document Links/Relations Table
CREATE TABLE document_relations (
    id BIGSERIAL PRIMARY KEY,
    source_document_id BIGINT NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    target_document_id BIGINT NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    relation_type VARCHAR(50) NOT NULL, -- DEPENDS_ON, REFERENCES, SUPERSEDES, RELATED
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES users(id),
    UNIQUE(source_document_id, target_document_id, relation_type)
);

-- Change Requests Table
CREATE TABLE change_requests (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id),
    project_id BIGINT REFERENCES projects(id),
    title VARCHAR(500) NOT NULL,
    description TEXT NOT NULL,
    justification TEXT,
    impact_analysis TEXT,
    implementation_plan TEXT,
    rollback_plan TEXT,
    status change_request_status_enum DEFAULT 'DRAFT',
    priority change_priority_enum DEFAULT 'MEDIUM',
    category VARCHAR(100), -- FEATURE, BUG_FIX, IMPROVEMENT, SECURITY, etc.
    estimated_effort_hours INTEGER,
    actual_effort_hours INTEGER,
    cost_estimate DECIMAL(15,2),
    risk_level VARCHAR(20) DEFAULT 'MEDIUM',
    business_value TEXT,
    technical_requirements TEXT,
    acceptance_criteria TEXT,
    requested_by BIGINT NOT NULL REFERENCES users(id),
    assigned_to BIGINT REFERENCES users(id),
    reviewer_id BIGINT REFERENCES users(id),
    approver_id BIGINT REFERENCES users(id),
    implemented_by BIGINT REFERENCES users(id),
    requested_date DATE NOT NULL DEFAULT CURRENT_DATE,
    target_date DATE,
    approved_at TIMESTAMP,
    implemented_at TIMESTAMP,
    closed_at TIMESTAMP,
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id),
    deleted_at TIMESTAMP,
    deleted_by BIGINT REFERENCES users(id)
);

-- Change Request Approvals Table
CREATE TABLE change_request_approvals (
    id BIGSERIAL PRIMARY KEY,
    change_request_id BIGINT NOT NULL REFERENCES change_requests(id) ON DELETE CASCADE,
    approver_id BIGINT NOT NULL REFERENCES users(id),
    status VARCHAR(20) NOT NULL, -- PENDING, APPROVED, REJECTED
    comments TEXT,
    approval_level INTEGER DEFAULT 1,
    decision_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Change Request Impacts Table (for impact analysis)
CREATE TABLE change_request_impacts (
    id BIGSERIAL PRIMARY KEY,
    change_request_id BIGINT NOT NULL REFERENCES change_requests(id) ON DELETE CASCADE,
    impacted_type VARCHAR(50) NOT NULL, -- DOCUMENT, TASK, TEST_CASE, SYSTEM
    impacted_id BIGINT NOT NULL,
    impact_level VARCHAR(20) DEFAULT 'MEDIUM', -- LOW, MEDIUM, HIGH
    impact_description TEXT,
    remediation_required BOOLEAN DEFAULT FALSE,
    remediation_notes TEXT,
    verified BOOLEAN DEFAULT FALSE,
    verified_by BIGINT REFERENCES users(id),
    verified_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES users(id)
);

-- Tasks Table
CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id),
    project_id BIGINT REFERENCES projects(id),
    parent_task_id BIGINT REFERENCES tasks(id),
    change_request_id BIGINT REFERENCES change_requests(id),
    title VARCHAR(500) NOT NULL,
    description TEXT,
    acceptance_criteria TEXT,
    status task_status_enum DEFAULT 'TODO',
    priority task_priority_enum DEFAULT 'MEDIUM',
    type VARCHAR(50) DEFAULT 'TASK', -- TASK, STORY, BUG, EPIC, SUBTASK
    story_points INTEGER,
    estimated_hours DECIMAL(8,2),
    actual_hours DECIMAL(8,2),
    remaining_hours DECIMAL(8,2),
    progress_percentage INTEGER DEFAULT 0 CHECK (progress_percentage >= 0 AND progress_percentage <= 100),
    assigned_to BIGINT REFERENCES users(id),
    reporter_id BIGINT REFERENCES users(id),
    sprint_id BIGINT, -- Reference to external sprint system
    epic_id BIGINT REFERENCES tasks(id),
    start_date DATE,
    due_date DATE,
    completed_at TIMESTAMP,
    tags TEXT[] DEFAULT '{}',
    labels JSONB DEFAULT '[]',
    custom_fields JSONB DEFAULT '{}',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id),
    deleted_at TIMESTAMP,
    deleted_by BIGINT REFERENCES users(id)
);

-- Task Dependencies Table
CREATE TABLE task_dependencies (
    id BIGSERIAL PRIMARY KEY,
    predecessor_task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    successor_task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    dependency_type VARCHAR(20) DEFAULT 'FINISH_TO_START', -- FS, SS, FF, SF
    lag_days INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES users(id),
    UNIQUE(predecessor_task_id, successor_task_id)
);

-- Task Watchers Table
CREATE TABLE task_watchers (
    task_id BIGINT REFERENCES tasks(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (task_id, user_id)
);

-- Task Time Tracking Table
CREATE TABLE task_time_tracking (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id),
    hours_spent DECIMAL(8,2) NOT NULL,
    logged_date DATE NOT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES users(id)
);

-- Test Cases Table
CREATE TABLE test_cases (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id),
    project_id BIGINT REFERENCES projects(id),
    title VARCHAR(500) NOT NULL,
    description TEXT,
    preconditions TEXT,
    test_steps JSONB NOT NULL, -- Array of {step: string, expected: string}
    expected_result TEXT,
    priority task_priority_enum DEFAULT 'MEDIUM',
    status test_case_status_enum DEFAULT 'DRAFT',
    type VARCHAR(50) DEFAULT 'FUNCTIONAL', -- FUNCTIONAL, INTEGRATION, PERFORMANCE, SECURITY
    category VARCHAR(100),
    automation_status VARCHAR(20) DEFAULT 'MANUAL', -- MANUAL, AUTOMATED, TO_AUTOMATE
    automation_script TEXT,
    estimated_duration INTEGER, -- in minutes
    tags TEXT[] DEFAULT '{}',
    requirements JSONB DEFAULT '[]', -- Related requirements/documents
    test_data JSONB DEFAULT '{}',
    environment_requirements TEXT,
    is_template BOOLEAN DEFAULT FALSE,
    template_id BIGINT REFERENCES test_cases(id),
    reuse_count INTEGER DEFAULT 0,
    last_execution_date TIMESTAMP,
    last_execution_status test_execution_status_enum,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id),
    deleted_at TIMESTAMP,
    deleted_by BIGINT REFERENCES users(id)
);

-- Test Case Relations Table (for reusability tracking)
CREATE TABLE test_case_relations (
    id BIGSERIAL PRIMARY KEY,
    source_test_case_id BIGINT NOT NULL REFERENCES test_cases(id) ON DELETE CASCADE,
    target_test_case_id BIGINT NOT NULL REFERENCES test_cases(id) ON DELETE CASCADE,
    relation_type VARCHAR(50) NOT NULL, -- CLONED_FROM, VARIANT_OF, RELATED_TO
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES users(id)
);

-- Test Executions Table
CREATE TABLE test_executions (
    id BIGSERIAL PRIMARY KEY,
    test_case_id BIGINT NOT NULL REFERENCES test_cases(id) ON DELETE CASCADE,
    project_id BIGINT REFERENCES projects(id),
    executed_by BIGINT NOT NULL REFERENCES users(id),
    status test_execution_status_enum NOT NULL,
    execution_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    duration_minutes INTEGER,
    environment VARCHAR(100),
    build_version VARCHAR(100),
    actual_result TEXT,
    notes TEXT,
    attachments JSONB DEFAULT '[]',
    defects_found JSONB DEFAULT '[]', -- Array of defect references
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Test Case Document Relations Table
CREATE TABLE test_case_document_relations (
    test_case_id BIGINT REFERENCES test_cases(id) ON DELETE CASCADE,
    document_id BIGINT REFERENCES documents(id) ON DELETE CASCADE,
    relation_type VARCHAR(50) NOT NULL, -- TESTS, VALIDATES, COVERS
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES users(id),
    PRIMARY KEY (test_case_id, document_id)
);

-- Comments Table (for documents, tasks, change requests)
CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL, -- DOCUMENT, TASK, CHANGE_REQUEST, TEST_CASE
    entity_id BIGINT NOT NULL,
    parent_comment_id BIGINT REFERENCES comments(id),
    content TEXT NOT NULL,
    content_type VARCHAR(20) DEFAULT 'text/plain',
    is_internal BOOLEAN DEFAULT FALSE,
    mentions BIGINT[] DEFAULT '{}', -- Array of mentioned user IDs
    attachments JSONB DEFAULT '[]',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id),
    deleted_at TIMESTAMP,
    deleted_by BIGINT REFERENCES users(id)
);

-- Attachments Table
CREATE TABLE attachments (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    filename VARCHAR(500) NOT NULL,
    original_filename VARCHAR(500) NOT NULL,
    file_path TEXT NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    checksum VARCHAR(64),
    storage_provider storage_provider_enum DEFAULT 'LOCAL',
    thumbnail_path TEXT,
    is_public BOOLEAN DEFAULT FALSE,
    download_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL REFERENCES users(id),
    deleted_at TIMESTAMP,
    deleted_by BIGINT REFERENCES users(id)
);

-- Subscriptions Table (for notifications)
CREATE TABLE subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    entity_type VARCHAR(50) NOT NULL, -- PROJECT, DOCUMENT, TASK, CHANGE_REQUEST
    entity_id BIGINT NOT NULL,
    event_types notification_type_enum[] DEFAULT '{}',
    channels notification_channel_enum[] DEFAULT '{IN_APP}',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, entity_type, entity_id)
);

-- Notifications Table
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    recipient_id BIGINT NOT NULL REFERENCES users(id),
    sender_id BIGINT REFERENCES users(id),
    type notification_type_enum NOT NULL,
    title VARCHAR(500) NOT NULL,
    message TEXT NOT NULL,
    entity_type VARCHAR(50),
    entity_id BIGINT,
    data JSONB DEFAULT '{}',
    channels notification_channel_enum[] DEFAULT '{IN_APP}',
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delivery_attempts INTEGER DEFAULT 0,
    delivery_status JSONB DEFAULT '{}', -- Channel-specific delivery status
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Activity Log Table (for audit trail)
CREATE TABLE activity_logs (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT REFERENCES organizations(id),
    user_id BIGINT REFERENCES users(id),
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL, -- CREATE, UPDATE, DELETE, VIEW, APPROVE, etc.
    old_values JSONB,
    new_values JSONB,
    ip_address INET,
    user_agent TEXT,
    session_id VARCHAR(100),
    request_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Search Index Table (for custom search functionality)
CREATE TABLE search_index (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    title VARCHAR(500) NOT NULL,
    content TEXT,
    tags TEXT[] DEFAULT '{}',
    metadata JSONB DEFAULT '{}',
    search_vector tsvector,
    last_indexed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(entity_type, entity_id)
);

-- User Sessions Table (for session management)
CREATE TABLE user_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    refresh_token VARCHAR(500) NOT NULL UNIQUE,
    device_info JSONB DEFAULT '{}',
    ip_address INET,
    user_agent TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- TRIGGERS
-- =====================================================

-- Apply audit trigger to all relevant tables
CREATE TRIGGER audit_organizations BEFORE UPDATE ON organizations
    FOR EACH ROW EXECUTE FUNCTION audit_trigger();

CREATE TRIGGER audit_users BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION audit_trigger();

CREATE TRIGGER audit_projects BEFORE UPDATE ON projects
    FOR EACH ROW EXECUTE FUNCTION audit_trigger();

CREATE TRIGGER audit_documents BEFORE UPDATE ON documents
    FOR EACH ROW EXECUTE FUNCTION audit_trigger();

CREATE TRIGGER audit_change_requests BEFORE UPDATE ON change_requests
    FOR EACH ROW EXECUTE FUNCTION audit_trigger();

CREATE TRIGGER audit_tasks BEFORE UPDATE ON tasks
    FOR EACH ROW EXECUTE FUNCTION audit_trigger();

CREATE TRIGGER audit_test_cases BEFORE UPDATE ON test_cases
    FOR EACH ROW EXECUTE FUNCTION audit_trigger();

-- Document search vector update trigger
CREATE OR REPLACE FUNCTION update_document_search_vector()
RETURNS TRIGGER AS $$
BEGIN
    NEW.search_vector :=
        setweight(to_tsvector('english', COALESCE(NEW.title, '')), 'A') ||
        setweight(to_tsvector('english', COALESCE(NEW.content, '')), 'B') ||
        setweight(to_tsvector('simple', array_to_string(NEW.tags, ' ')), 'C');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_document_search_vector_trigger
    BEFORE INSERT OR UPDATE ON documents
    FOR EACH ROW EXECUTE FUNCTION update_document_search_vector();

-- Update test case reuse count trigger
CREATE OR REPLACE FUNCTION update_test_case_reuse_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE test_cases
        SET reuse_count = reuse_count + 1
        WHERE id = NEW.source_test_case_id;
        RETURN NEW;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_test_case_reuse_count_trigger
    AFTER INSERT ON test_case_relations
    FOR EACH ROW EXECUTE FUNCTION update_test_case_reuse_count();

-- Activity log trigger
CREATE OR REPLACE FUNCTION log_activity()
RETURNS TRIGGER AS $$
DECLARE
    user_id_val BIGINT;
    org_id_val BIGINT;
BEGIN
    -- Get current user ID from session
    user_id_val := NULLIF(current_setting('app.current_user_id', true), '')::BIGINT;

    -- Get organization ID based on table
    CASE TG_TABLE_NAME
        WHEN 'organizations' THEN
            org_id_val := COALESCE(NEW.id, OLD.id);
        WHEN 'users' THEN
            org_id_val := COALESCE(NEW.organization_id, OLD.organization_id);
        WHEN 'projects' THEN
            org_id_val := COALESCE(NEW.organization_id, OLD.organization_id);
        WHEN 'documents' THEN
            org_id_val := COALESCE(NEW.organization_id, OLD.organization_id);
        WHEN 'change_requests' THEN
            org_id_val := COALESCE(NEW.organization_id, OLD.organization_id);
        WHEN 'tasks' THEN
            org_id_val := COALESCE(NEW.organization_id, OLD.organization_id);
        WHEN 'test_cases' THEN
            org_id_val := COALESCE(NEW.organization_id, OLD.organization_id);
        ELSE
            org_id_val := NULL;
    END CASE;

    INSERT INTO activity_logs (
        organization_id,
        user_id,
        entity_type,
        entity_id,
        action,
        old_values,
        new_values,
        ip_address,
        user_agent,
        session_id
    ) VALUES (
        org_id_val,
        user_id_val,
        TG_TABLE_NAME,
        COALESCE(NEW.id, OLD.id),
        TG_OP,
        CASE WHEN TG_OP = 'DELETE' THEN to_jsonb(OLD) ELSE NULL END,
        CASE WHEN TG_OP IN ('INSERT', 'UPDATE') THEN to_jsonb(NEW) ELSE NULL END,
        NULLIF(current_setting('app.client_ip', true), '')::INET,
        NULLIF(current_setting('app.user_agent', true), ''),
        NULLIF(current_setting('app.session_id', true), '')
    );

    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

-- Apply activity log trigger to key tables
CREATE TRIGGER log_activity_organizations AFTER INSERT OR UPDATE OR DELETE ON organizations
    FOR EACH ROW EXECUTE FUNCTION log_activity();

CREATE TRIGGER log_activity_projects AFTER INSERT OR UPDATE OR DELETE ON projects
    FOR EACH ROW EXECUTE FUNCTION log_activity();

CREATE TRIGGER log_activity_documents AFTER INSERT OR UPDATE OR DELETE ON documents
    FOR EACH ROW EXECUTE FUNCTION log_activity();

CREATE TRIGGER log_activity_change_requests AFTER INSERT OR UPDATE OR DELETE ON change_requests
    FOR EACH ROW EXECUTE FUNCTION log_activity();

CREATE TRIGGER log_activity_tasks AFTER INSERT OR UPDATE OR DELETE ON tasks
    FOR EACH ROW EXECUTE FUNCTION log_activity();

-- =====================================================
-- INDEXES FOR PERFORMANCE OPTIMIZATION
-- =====================================================

-- Users table indexes
CREATE INDEX CONCURRENTLY idx_users_organization_email ON users(organization_id, email) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_users_organization_username ON users(organization_id, username) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_users_status ON users(status) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_users_last_active ON users(last_active_at DESC) WHERE deleted_at IS NULL;

-- Projects table indexes
CREATE INDEX CONCURRENTLY idx_projects_organization_id ON projects(organization_id) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_projects_slug ON projects(organization_id, slug) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_projects_manager ON projects(project_manager_id) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_projects_status ON projects(status) WHERE deleted_at IS NULL;

-- Project members indexes
CREATE INDEX CONCURRENTLY idx_project_members_user_id ON project_members(user_id) WHERE is_active = TRUE;
CREATE INDEX CONCURRENTLY idx_project_members_project_role ON project_members(project_id, role_id) WHERE is_active = TRUE;

-- Documents table indexes
CREATE INDEX CONCURRENTLY idx_documents_project_id ON documents(project_id) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_documents_category_id ON documents(category_id) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_documents_status ON documents(status) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_documents_created_at_desc ON documents(created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_documents_updated_at_desc ON documents(updated_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_documents_created_by ON documents(created_by) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_documents_title_gin ON documents USING gin(to_tsvector('english', title)) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_documents_search_vector_gin ON documents USING gin(search_vector) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_documents_tags_gin ON documents USING gin(tags) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_documents_template ON documents(is_template, template_id) WHERE deleted_at IS NULL;

-- Document versions indexes
CREATE INDEX CONCURRENTLY idx_document_versions_document_id ON document_versions(document_id);
CREATE INDEX CONCURRENTLY idx_document_versions_created_at_desc ON document_versions(document_id, created_at DESC);

-- Change requests table indexes
CREATE INDEX CONCURRENTLY idx_change_requests_project_id ON change_requests(project_id) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_change_requests_status ON change_requests(status) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_change_requests_priority ON change_requests(priority) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_change_requests_requested_by ON change_requests(requested_by) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_change_requests_assigned_to ON change_requests(assigned_to) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_change_requests_created_at_desc ON change_requests(created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_change_requests_target_date ON change_requests(target_date) WHERE deleted_at IS NULL;

-- Change request impacts indexes
CREATE INDEX CONCURRENTLY idx_change_request_impacts_change_id ON change_request_impacts(change_request_id);
CREATE INDEX CONCURRENTLY idx_change_request_impacts_impacted ON change_request_impacts(impacted_type, impacted_id);

-- Tasks table indexes
CREATE INDEX CONCURRENTLY idx_tasks_project_id ON tasks(project_id) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_tasks_assigned_to ON tasks(assigned_to) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_tasks_status ON tasks(status) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_tasks_priority ON tasks(priority) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_tasks_created_at_desc ON tasks(created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_tasks_due_date ON tasks(due_date) WHERE deleted_at IS NULL AND due_date IS NOT NULL;
CREATE INDEX CONCURRENTLY idx_tasks_parent_task ON tasks(parent_task_id) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_tasks_change_request ON tasks(change_request_id) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_tasks_epic ON tasks(epic_id) WHERE deleted_at IS NULL;

-- Test cases table indexes
CREATE INDEX CONCURRENTLY idx_test_cases_project_id ON test_cases(project_id) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_test_cases_status ON test_cases(status) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_test_cases_type ON test_cases(type) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_test_cases_created_at_desc ON test_cases(created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_test_cases_template ON test_cases(is_template, template_id) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_test_cases_reuse_count ON test_cases(reuse_count DESC) WHERE deleted_at IS NULL;

-- Test executions indexes
CREATE INDEX CONCURRENTLY idx_test_executions_test_case_id ON test_executions(test_case_id);
CREATE INDEX CONCURRENTLY idx_test_executions_executed_by ON test_executions(executed_by);
CREATE INDEX CONCURRENTLY idx_test_executions_status ON test_executions(status);
CREATE INDEX CONCURRENTLY idx_test_executions_execution_date_desc ON test_executions(execution_date DESC);

-- Comments table indexes
CREATE INDEX CONCURRENTLY idx_comments_entity ON comments(entity_type, entity_id) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_comments_created_by ON comments(created_by) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_comments_created_at_desc ON comments(created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY idx_comments_parent ON comments(parent_comment_id) WHERE deleted_at IS NULL;

-- Notifications table indexes
CREATE INDEX CONCURRENTLY idx_notifications_recipient_id ON notifications(recipient_id);
CREATE INDEX CONCURRENTLY idx_notifications_sent_at_desc ON notifications(sent_at DESC);
CREATE INDEX CONCURRENTLY idx_notifications_is_read ON notifications(recipient_id, is_read, sent_at DESC);
CREATE INDEX CONCURRENTLY idx_notifications_entity ON notifications(entity_type, entity_id);

-- Subscriptions table indexes
CREATE INDEX CONCURRENTLY idx_subscriptions_user_id ON subscriptions(user_id) WHERE is_active = TRUE;
CREATE INDEX CONCURRENTLY idx_subscriptions_entity ON subscriptions(entity_type, entity_id) WHERE is_active = TRUE;

-- Activity logs table indexes
CREATE INDEX CONCURRENTLY idx_activity_logs_organization_id ON activity_logs(organization_id);
CREATE INDEX CONCURRENTLY idx_activity_logs_user_id ON activity_logs(user_id);
CREATE INDEX CONCURRENTLY idx_activity_logs_entity ON activity_logs(entity_type, entity_id);
CREATE INDEX CONCURRENTLY idx_activity_logs_created_at_desc ON activity_logs(created_at DESC);
CREATE INDEX CONCURRENTLY idx_activity_logs_action ON activity_logs(action);

-- Search index table indexes
CREATE INDEX CONCURRENTLY idx_search_index_entity ON search_index(entity_type, entity_id);
CREATE INDEX CONCURRENTLY idx_search_index_search_vector ON search_index USING gin(search_vector);

-- User sessions table indexes
CREATE INDEX CONCURRENTLY idx_user_sessions_user_id ON user_sessions(user_id) WHERE is_active = TRUE;
CREATE INDEX CONCURRENTLY idx_user_sessions_refresh_token ON user_sessions(refresh_token) WHERE is_active = TRUE;
CREATE INDEX CONCURRENTLY idx_user_sessions_expires_at ON user_sessions(expires_at) WHERE is_active = TRUE;

-- =====================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- =====================================================

-- Enable RLS on key tables
ALTER TABLE organizations ENABLE ROW LEVEL SECURITY;
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE projects ENABLE ROW LEVEL SECURITY;
ALTER TABLE documents ENABLE ROW LEVEL SECURITY;
ALTER TABLE change_requests ENABLE ROW LEVEL SECURITY;
ALTER TABLE tasks ENABLE ROW LEVEL SECURITY;
ALTER TABLE test_cases ENABLE ROW LEVEL SECURITY;

-- Organization-level isolation policy
CREATE POLICY organization_isolation_policy ON users
    FOR ALL TO application_user
    USING (organization_id = current_setting('app.current_organization_id')::BIGINT);

CREATE POLICY organization_isolation_policy ON projects
    FOR ALL TO application_user
    USING (organization_id = current_setting('app.current_organization_id')::BIGINT);

CREATE POLICY organization_isolation_policy ON documents
    FOR ALL TO application_user
    USING (organization_id = current_setting('app.current_organization_id')::BIGINT);

CREATE POLICY organization_isolation_policy ON change_requests
    FOR ALL TO application_user
    USING (organization_id = current_setting('app.current_organization_id')::BIGINT);

CREATE POLICY organization_isolation_policy ON tasks
    FOR ALL TO application_user
    USING (organization_id = current_setting('app.current_organization_id')::BIGINT);

CREATE POLICY organization_isolation_policy ON test_cases
    FOR ALL TO application_user
    USING (organization_id = current_setting('app.current_organization_id')::BIGINT);

-- Project-level access policy for documents
CREATE POLICY project_access_policy ON documents
    FOR ALL TO application_user
    USING (
        project_id IS NULL OR
        project_id IN (
            SELECT pm.project_id
            FROM project_members pm
            WHERE pm.user_id = current_setting('app.current_user_id')::BIGINT
            AND pm.is_active = TRUE
        )
    );

-- =====================================================
-- DATABASE ROLES AND PERMISSIONS
-- =====================================================

-- Create application user role
CREATE ROLE application_user;

-- Grant basic permissions
GRANT CONNECT ON DATABASE promanage TO application_user;
GRANT USAGE ON SCHEMA public TO application_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO application_user;

-- Grant table permissions
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO application_user;

-- Create read-only role for reporting
CREATE ROLE readonly_user;
GRANT CONNECT ON DATABASE promanage TO readonly_user;
GRANT USAGE ON SCHEMA public TO readonly_user;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO readonly_user;

-- =====================================================
-- SAMPLE DATA INSERTION
-- =====================================================

-- Insert default permissions
INSERT INTO permissions (resource, action, scope, description) VALUES
('PROJECT', 'CREATE', 'GLOBAL', 'Create new projects'),
('PROJECT', 'READ', 'PROJECT', 'View project details'),
('PROJECT', 'UPDATE', 'PROJECT', 'Edit project settings'),
('PROJECT', 'DELETE', 'PROJECT', 'Delete projects'),
('DOCUMENT', 'CREATE', 'PROJECT', 'Create documents in project'),
('DOCUMENT', 'READ', 'PROJECT', 'View documents in project'),
('DOCUMENT', 'UPDATE', 'SELF', 'Edit own documents'),
('DOCUMENT', 'UPDATE', 'PROJECT', 'Edit any document in project'),
('DOCUMENT', 'DELETE', 'SELF', 'Delete own documents'),
('DOCUMENT', 'DELETE', 'PROJECT', 'Delete any document in project'),
('DOCUMENT', 'APPROVE', 'PROJECT', 'Approve document changes'),
('TASK', 'CREATE', 'PROJECT', 'Create tasks in project'),
('TASK', 'READ', 'PROJECT', 'View tasks in project'),
('TASK', 'UPDATE', 'SELF', 'Edit assigned tasks'),
('TASK', 'UPDATE', 'PROJECT', 'Edit any task in project'),
('TASK', 'DELETE', 'PROJECT', 'Delete tasks'),
('TASK', 'ASSIGN', 'PROJECT', 'Assign tasks to users'),
('CHANGE', 'CREATE', 'PROJECT', 'Create change requests'),
('CHANGE', 'READ', 'PROJECT', 'View change requests'),
('CHANGE', 'UPDATE', 'SELF', 'Edit own change requests'),
('CHANGE', 'UPDATE', 'PROJECT', 'Edit any change request'),
('CHANGE', 'APPROVE', 'PROJECT', 'Approve change requests'),
('TEST', 'CREATE', 'PROJECT', 'Create test cases'),
('TEST', 'READ', 'PROJECT', 'View test cases'),
('TEST', 'UPDATE', 'PROJECT', 'Edit test cases'),
('TEST', 'EXECUTE', 'PROJECT', 'Execute test cases'),
('USER', 'READ', 'PROJECT', 'View project members'),
('USER', 'INVITE', 'PROJECT', 'Invite users to project'),
('USER', 'MANAGE', 'PROJECT', 'Manage project members');

-- Insert default sample organization
INSERT INTO organizations (id, name, slug, description, is_active) VALUES
(1, 'Demo Organization', 'demo-org', 'Sample organization for testing', TRUE);

-- Insert default roles with permissions
INSERT INTO roles (id, organization_id, name, display_name, description, is_system_role) VALUES
(1, NULL, 'SUPER_ADMIN', 'Super Administrator', 'Full system access', TRUE),
(2, 1, 'PROJECT_MANAGER', 'Project Manager', 'Manages projects and resources', FALSE),
(3, 1, 'DEVELOPER', 'Developer', 'Develops and maintains code', FALSE),
(4, 1, 'TESTER', 'Tester', 'Tests software and manages test cases', FALSE),
(5, 1, 'UI_DESIGNER', 'UI Designer', 'Creates and manages design assets', FALSE),
(6, 1, 'THIRD_PARTY', 'Third Party', 'External users with limited access', FALSE),
(7, 1, 'DEVOPS', 'DevOps Engineer', 'Manages infrastructure and deployments', FALSE);

-- Role permissions assignments (PROJECT_MANAGER gets most permissions)
INSERT INTO role_permissions (role_id, permission_id)
SELECT 2, id FROM permissions WHERE scope IN ('PROJECT', 'GLOBAL');

-- DEVELOPER permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT 3, id FROM permissions
WHERE resource IN ('DOCUMENT', 'TASK', 'CHANGE')
AND action IN ('CREATE', 'READ', 'UPDATE')
AND scope IN ('PROJECT', 'SELF');

-- TESTER permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT 4, id FROM permissions
WHERE resource IN ('TEST', 'DOCUMENT', 'TASK')
AND action IN ('CREATE', 'READ', 'UPDATE', 'EXECUTE')
AND scope IN ('PROJECT', 'SELF');

-- Insert sample admin user
INSERT INTO users (id, organization_id, username, email, password_hash, first_name, last_name, status, email_verified_at) VALUES
(1, 1, 'admin', 'admin@demo.com', '$2a$10$dummy.hash.for.demo.purposes', 'System', 'Administrator', 'ACTIVE', CURRENT_TIMESTAMP);

-- =====================================================
-- PERFORMANCE OPTIMIZATION QUERIES
-- =====================================================

-- Create materialized view for user dashboard
CREATE MATERIALIZED VIEW user_dashboard_stats AS
SELECT
    u.id as user_id,
    COUNT(DISTINCT pm.project_id) as project_count,
    COUNT(DISTINCT CASE WHEN t.assigned_to = u.id AND t.status NOT IN ('DONE', 'CANCELLED') THEN t.id END) as active_tasks_count,
    COUNT(DISTINCT CASE WHEN cr.requested_by = u.id AND cr.status NOT IN ('CLOSED', 'IMPLEMENTED') THEN cr.id END) as pending_changes_count,
    COUNT(DISTINCT CASE WHEN n.recipient_id = u.id AND n.is_read = FALSE THEN n.id END) as unread_notifications_count,
    MAX(t.updated_at) as last_task_update,
    MAX(cr.updated_at) as last_change_update
FROM users u
LEFT JOIN project_members pm ON pm.user_id = u.id AND pm.is_active = TRUE
LEFT JOIN tasks t ON t.organization_id = u.organization_id
LEFT JOIN change_requests cr ON cr.organization_id = u.organization_id
LEFT JOIN notifications n ON n.recipient_id = u.id
WHERE u.deleted_at IS NULL
GROUP BY u.id;

CREATE UNIQUE INDEX idx_user_dashboard_stats_user_id ON user_dashboard_stats(user_id);

-- Create materialized view for project statistics
CREATE MATERIALIZED VIEW project_stats AS
SELECT
    p.id as project_id,
    p.organization_id,
    COUNT(DISTINCT pm.user_id) as member_count,
    COUNT(DISTINCT d.id) as document_count,
    COUNT(DISTINCT t.id) as task_count,
    COUNT(DISTINCT CASE WHEN t.status = 'DONE' THEN t.id END) as completed_tasks_count,
    COUNT(DISTINCT cr.id) as change_request_count,
    COUNT(DISTINCT tc.id) as test_case_count,
    AVG(CASE WHEN t.status = 'DONE' AND t.estimated_hours > 0 AND t.actual_hours > 0
             THEN (t.actual_hours / t.estimated_hours) END) as avg_estimation_accuracy,
    EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - p.created_at)) / 86400 as project_age_days
FROM projects p
LEFT JOIN project_members pm ON pm.project_id = p.id AND pm.is_active = TRUE
LEFT JOIN documents d ON d.project_id = p.id AND d.deleted_at IS NULL
LEFT JOIN tasks t ON t.project_id = p.id AND t.deleted_at IS NULL
LEFT JOIN change_requests cr ON cr.project_id = p.id AND cr.deleted_at IS NULL
LEFT JOIN test_cases tc ON tc.project_id = p.id AND tc.deleted_at IS NULL
WHERE p.deleted_at IS NULL
GROUP BY p.id, p.organization_id, p.created_at;

CREATE UNIQUE INDEX idx_project_stats_project_id ON project_stats(project_id);

-- =====================================================
-- DATABASE CONFIGURATION RECOMMENDATIONS
-- =====================================================

/*
PostgreSQL Configuration Recommendations for ProManage:

# Memory Configuration
shared_buffers = 256MB                    # 25% of RAM for dedicated server
effective_cache_size = 1GB               # 75% of available RAM
work_mem = 4MB                           # Per connection working memory
maintenance_work_mem = 64MB              # For maintenance operations

# Checkpoint Configuration
checkpoint_completion_target = 0.9       # Spread checkpoints over time
wal_buffers = 16MB                       # WAL buffer size
checkpoint_timeout = 10min               # Checkpoint frequency

# Connection Configuration
max_connections = 200                     # Based on expected concurrent users
shared_preload_libraries = 'pg_stat_statements'  # Query performance monitoring

# Logging Configuration
log_min_duration_statement = 1000        # Log slow queries (1 second+)
log_checkpoints = on                     # Log checkpoint activity
log_connections = on                     # Log connections
log_disconnections = on                  # Log disconnections
log_lock_waits = on                      # Log lock waits

# Query Planner Configuration
random_page_cost = 1.1                   # For SSD storage
effective_io_concurrency = 200           # For SSD storage

# Full-Text Search Configuration
default_text_search_config = 'pg_catalog.english'

# Connection Pooling (PgBouncer recommended)
# pool_mode = transaction
# max_client_conn = 1000
# default_pool_size = 25
*/

-- =====================================================
-- VACUUM AND MAINTENANCE QUERIES
-- =====================================================

-- Create maintenance function for regular cleanup
CREATE OR REPLACE FUNCTION maintain_database()
RETURNS void AS $$
BEGIN
    -- Refresh materialized views
    REFRESH MATERIALIZED VIEW CONCURRENTLY user_dashboard_stats;
    REFRESH MATERIALIZED VIEW CONCURRENTLY project_stats;

    -- Clean up expired sessions
    DELETE FROM user_sessions
    WHERE expires_at < CURRENT_TIMESTAMP OR last_used_at < CURRENT_TIMESTAMP - INTERVAL '30 days';

    -- Clean up old notifications (older than 90 days)
    DELETE FROM notifications
    WHERE sent_at < CURRENT_TIMESTAMP - INTERVAL '90 days' AND is_read = TRUE;

    -- Clean up old activity logs (older than 1 year)
    DELETE FROM activity_logs
    WHERE created_at < CURRENT_TIMESTAMP - INTERVAL '1 year';

    -- Vacuum analyze key tables
    VACUUM ANALYZE documents;
    VACUUM ANALYZE tasks;
    VACUUM ANALYZE change_requests;
    VACUUM ANALYZE notifications;

    RAISE NOTICE 'Database maintenance completed at %', CURRENT_TIMESTAMP;
END;
$$ LANGUAGE plpgsql;

-- Schedule maintenance (requires pg_cron extension)
-- SELECT cron.schedule('database-maintenance', '0 2 * * 0', 'SELECT maintain_database();');

-- =====================================================
-- BACKUP AND RECOVERY RECOMMENDATIONS
-- =====================================================

/*
Backup Strategy Recommendations:

1. Full Backup (Daily):
   pg_dump -Fc -h localhost -U promanage_user promanage > promanage_backup_$(date +%Y%m%d).dump

2. Incremental Backup (WAL archiving):
   Configure archive_mode = on
   archive_command = 'cp %p /backup/wal_archive/%f'

3. Point-in-Time Recovery:
   Keep WAL files for 7 days minimum
   Test recovery procedures monthly

4. Backup Verification:
   pg_restore --list promanage_backup.dump
   Restore to test environment weekly

5. Cloud Backup (Production):
   Use AWS RDS automated backups
   Cross-region backup replication
   Backup retention: 30 days
*/

-- =====================================================
-- MONITORING AND PERFORMANCE QUERIES
-- =====================================================

-- Query to monitor slow queries
CREATE VIEW slow_queries AS
SELECT
    query,
    calls,
    total_time,
    mean_time,
    stddev_time,
    rows,
    100.0 * shared_blks_hit / nullif(shared_blks_hit + shared_blks_read, 0) AS hit_percent
FROM pg_stat_statements
ORDER BY mean_time DESC;

-- Query to monitor table sizes
CREATE VIEW table_sizes AS
SELECT
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size,
    pg_total_relation_size(schemaname||'.'||tablename) as size_bytes
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- Query to monitor index usage
CREATE VIEW index_usage AS
SELECT
    schemaname,
    tablename,
    indexname,
    idx_scan,
    idx_tup_read,
    idx_tup_fetch,
    pg_size_pretty(pg_relation_size(indexrelid)) as size
FROM pg_stat_user_indexes
ORDER BY idx_scan DESC;

COMMENT ON DATABASE promanage IS 'ProManage Project Management System Database';

-- Final status message
DO $$
BEGIN
    RAISE NOTICE 'ProManage database schema created successfully!';
    RAISE NOTICE 'Total tables created: %', (
        SELECT count(*)
        FROM information_schema.tables
        WHERE table_schema = 'public'
        AND table_type = 'BASE TABLE'
    );
    RAISE NOTICE 'Total indexes created: %', (
        SELECT count(*)
        FROM pg_indexes
        WHERE schemaname = 'public'
    );
    RAISE NOTICE 'Schema is ready for application deployment.';
END $$;