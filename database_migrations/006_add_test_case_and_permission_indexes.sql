-- Migration: Add indexes to test case and permission tables for performance optimization
-- Date: 2025-10-08
-- Description: Add indexes to improve test case and permission query performance

-- Check if indexes exist before creating them
DO $$
BEGIN
    -- Test Case Table Indexes
    -- Index on project_id and status for filtering test cases by project and status
    IF NOT EXISTS (
        SELECT 1 FROM pg_class c 
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relname = 'idx_test_cases_project_status' AND n.nspname = 'public'
    ) THEN
        CREATE INDEX idx_test_cases_project_status ON tb_test_case(project_id, status) WHERE deleted = false;
        RAISE NOTICE 'Created index idx_test_cases_project_status';
    ELSE
        RAISE NOTICE 'Index idx_test_cases_project_status already exists';
    END IF;

    -- Index on creator_id for filtering test cases by creator
    IF NOT EXISTS (
        SELECT 1 FROM pg_class c 
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relname = 'idx_test_cases_creator' AND n.nspname = 'public'
    ) THEN
        CREATE INDEX idx_test_cases_creator ON tb_test_case(creator_id) WHERE deleted = false;
        RAISE NOTICE 'Created index idx_test_cases_creator';
    ELSE
        RAISE NOTICE 'Index idx_test_cases_creator already exists';
    END IF;

    -- Index on assignee_id for filtering test cases by assignee
    IF NOT EXISTS (
        SELECT 1 FROM pg_class c 
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relname = 'idx_test_cases_assignee' AND n.nspname = 'public'
    ) THEN
        CREATE INDEX idx_test_cases_assignee ON tb_test_case(assignee_id) WHERE deleted = false;
        RAISE NOTICE 'Created index idx_test_cases_assignee';
    ELSE
        RAISE NOTICE 'Index idx_test_cases_assignee already exists';
    END IF;

    -- Index on type and priority for filtering test cases by type and priority
    IF NOT EXISTS (
        SELECT 1 FROM pg_class c 
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relname = 'idx_test_cases_type_priority' AND n.nspname = 'public'
    ) THEN
        CREATE INDEX idx_test_cases_type_priority ON tb_test_case(type, priority) WHERE deleted = false;
        RAISE NOTICE 'Created index idx_test_cases_type_priority';
    ELSE
        RAISE NOTICE 'Index idx_test_cases_type_priority already exists';
    END IF;

    -- Index on execution_date for execution history queries
    IF NOT EXISTS (
        SELECT 1 FROM pg_class c 
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relname = 'idx_test_cases_execution_date' AND n.nspname = 'public'
    ) THEN
        CREATE INDEX idx_test_cases_execution_date ON tb_test_case(execution_date DESC) WHERE deleted = false;
        RAISE NOTICE 'Created index idx_test_cases_execution_date';
    ELSE
        RAISE NOTICE 'Index idx_test_cases_execution_date already exists';
    END IF;

    -- Full text search index on title using GIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_class c 
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relname = 'idx_test_cases_title_gin' AND n.nspname = 'public'
    ) THEN
        CREATE INDEX idx_test_cases_title_gin ON tb_test_case USING gin(to_tsvector('simple', title)) WHERE deleted = false;
        RAISE NOTICE 'Created index idx_test_cases_title_gin';
    ELSE
        RAISE NOTICE 'Index idx_test_cases_title_gin already exists';
    END IF;

    -- Permission Table Indexes
    -- Index on permission_code for unique lookups
    IF NOT EXISTS (
        SELECT 1 FROM pg_class c 
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relname = 'idx_permissions_code' AND n.nspname = 'public'
    ) THEN
        CREATE INDEX idx_permissions_code ON tb_permission(permission_code) WHERE deleted = false;
        RAISE NOTICE 'Created index idx_permissions_code';
    ELSE
        RAISE NOTICE 'Index idx_permissions_code already exists';
    END IF;

    -- Index on type for filtering permissions by type
    IF NOT EXISTS (
        SELECT 1 FROM pg_class c 
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relname = 'idx_permissions_type' AND n.nspname = 'public'
    ) THEN
        CREATE INDEX idx_permissions_type ON tb_permission(type) WHERE deleted = false;
        RAISE NOTICE 'Created index idx_permissions_type';
    ELSE
        RAISE NOTICE 'Index idx_permissions_type already exists';
    END IF;

    -- Index on parent_id and sort for building permission tree
    IF NOT EXISTS (
        SELECT 1 FROM pg_class c 
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relname = 'idx_permissions_parent_sort' AND n.nspname = 'public'
    ) THEN
        CREATE INDEX idx_permissions_parent_sort ON tb_permission(parent_id, sort) WHERE deleted = false;
        RAISE NOTICE 'Created index idx_permissions_parent_sort';
    ELSE
        RAISE NOTICE 'Index idx_permissions_parent_sort already exists';
    END IF;

    -- Index on status for filtering active permissions
    IF NOT EXISTS (
        SELECT 1 FROM pg_class c 
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relname = 'idx_permissions_status' AND n.nspname = 'public'
    ) THEN
        CREATE INDEX idx_permissions_status ON tb_permission(status) WHERE deleted = false;
        RAISE NOTICE 'Created index idx_permissions_status';
    ELSE
        RAISE NOTICE 'Index idx_permissions_status already exists';
    END IF;

    -- Role-Permission Mapping Table Indexes
    -- Index on role_id for getting permissions by role
    IF NOT EXISTS (
        SELECT 1 FROM pg_class c 
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relname = 'idx_role_permissions_role' AND n.nspname = 'public'
    ) THEN
        CREATE INDEX idx_role_permissions_role ON tb_role_permission(role_id);
        RAISE NOTICE 'Created index idx_role_permissions_role';
    ELSE
        RAISE NOTICE 'Index idx_role_permissions_role already exists';
    END IF;

    -- Index on permission_id for getting roles by permission
    IF NOT EXISTS (
        SELECT 1 FROM pg_class c 
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relname = 'idx_role_permissions_permission' AND n.nspname = 'public'
    ) THEN
        CREATE INDEX idx_role_permissions_permission ON tb_role_permission(permission_id);
        RAISE NOTICE 'Created index idx_role_permissions_permission';
    ELSE
        RAISE NOTICE 'Index idx_role_permissions_permission already exists';
    END IF;

    -- Composite index on role_id and permission_id for unique constraint checks
    IF NOT EXISTS (
        SELECT 1 FROM pg_class c 
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relname = 'idx_role_permissions_role_permission' AND n.nspname = 'public'
    ) THEN
        CREATE INDEX idx_role_permissions_role_permission ON tb_role_permission(role_id, permission_id);
        RAISE NOTICE 'Created index idx_role_permissions_role_permission';
    ELSE
        RAISE NOTICE 'Index idx_role_permissions_role_permission already exists';
    END IF;

    -- User-Role Mapping Table Indexes
    -- Index on user_id for getting roles by user
    IF NOT EXISTS (
        SELECT 1 FROM pg_class c 
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relname = 'idx_user_roles_user' AND n.nspname = 'public'
    ) THEN
        CREATE INDEX idx_user_roles_user ON tb_user_role(user_id);
        RAISE NOTICE 'Created index idx_user_roles_user';
    ELSE
        RAISE NOTICE 'Index idx_user_roles_user already exists';
    END IF;

    -- Index on role_id for getting users by role
    IF NOT EXISTS (
        SELECT 1 FROM pg_class c 
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relname = 'idx_user_roles_role' AND n.nspname = 'public'
    ) THEN
        CREATE INDEX idx_user_roles_role ON tb_user_role(role_id);
        RAISE NOTICE 'Created index idx_user_roles_role';
    ELSE
        RAISE NOTICE 'Index idx_user_roles_role already exists';
    END IF;

    -- Composite index on user_id and role_id for unique constraint checks
    IF NOT EXISTS (
        SELECT 1 FROM pg_class c 
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relname = 'idx_user_roles_user_role' AND n.nspname = 'public'
    ) THEN
        CREATE INDEX idx_user_roles_user_role ON tb_user_role(user_id, role_id);
        RAISE NOTICE 'Created index idx_user_roles_user_role';
    ELSE
        RAISE NOTICE 'Index idx_user_roles_user_role already exists';
    END IF;

END $$;

-- Analyze tables to update statistics
ANALYZE tb_test_case;
ANALYZE tb_permission;
ANALYZE tb_role_permission;
ANALYZE tb_user_role;