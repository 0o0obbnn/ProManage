-- Migration: Add indexes to document table for performance optimization
-- Date: 2025-10-04
-- Description: Add indexes to improve document query performance

-- Check if indexes exist before creating them
DO $$
BEGIN
    -- Index on project_id and status for filtering documents by project and status
    IF NOT EXISTS (
        SELECT 1 FROM pg_class c 
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relname = 'idx_documents_project_status' AND n.nspname = 'public'
    ) THEN
        CREATE INDEX idx_documents_project_status ON tb_document(project_id, status) WHERE deleted = false;
        RAISE NOTICE 'Created index idx_documents_project_status';
    ELSE
        RAISE NOTICE 'Index idx_documents_project_status already exists';
    END IF;

    -- Index on creator_id for filtering documents by creator
    IF NOT EXISTS (
        SELECT 1 FROM pg_class c 
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relname = 'idx_documents_creator' AND n.nspname = 'public'
    ) THEN
        CREATE INDEX idx_documents_creator ON tb_document(creator_id) WHERE deleted = false;
        RAISE NOTICE 'Created index idx_documents_creator';
    ELSE
        RAISE NOTICE 'Index idx_documents_creator already exists';
    END IF;

    -- Index on folder_id for filtering documents by folder
    IF NOT EXISTS (
        SELECT 1 FROM pg_class c 
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relname = 'idx_documents_folder' AND n.nspname = 'public'
    ) THEN
        CREATE INDEX idx_documents_folder ON tb_document(folder_id) WHERE deleted = false;
        RAISE NOTICE 'Created index idx_documents_folder';
    ELSE
        RAISE NOTICE 'Index idx_documents_folder already exists';
    END IF;

    -- Index on create_time for time range queries
    IF NOT EXISTS (
        SELECT 1 FROM pg_class c 
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relname = 'idx_documents_create_time' AND n.nspname = 'public'
    ) THEN
        CREATE INDEX idx_documents_create_time ON tb_document(create_time) WHERE deleted = false;
        RAISE NOTICE 'Created index idx_documents_create_time';
    ELSE
        RAISE NOTICE 'Index idx_documents_create_time already exists';
    END IF;

    -- Index on update_time for sorting
    IF NOT EXISTS (
        SELECT 1 FROM pg_class c 
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relname = 'idx_documents_update_time' AND n.nspname = 'public'
    ) THEN
        CREATE INDEX idx_documents_update_time ON tb_document(update_time DESC) WHERE deleted = false;
        RAISE NOTICE 'Created index idx_documents_update_time';
    ELSE
        RAISE NOTICE 'Index idx_documents_update_time already exists';
    END IF;

    -- Index on type for filtering by document type
    IF NOT EXISTS (
        SELECT 1 FROM pg_class c 
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relname = 'idx_documents_type' AND n.nspname = 'public'
    ) THEN
        CREATE INDEX idx_documents_type ON tb_document(type) WHERE deleted = false;
        RAISE NOTICE 'Created index idx_documents_type';
    ELSE
        RAISE NOTICE 'Index idx_documents_type already exists';
    END IF;

    -- Full text search index on title using GIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_class c 
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relname = 'idx_documents_title_gin' AND n.nspname = 'public'
    ) THEN
        CREATE INDEX idx_documents_title_gin ON tb_document USING gin(to_tsvector('simple', title)) WHERE deleted = false;
        RAISE NOTICE 'Created index idx_documents_title_gin';
    ELSE
        RAISE NOTICE 'Index idx_documents_title_gin already exists';
    END IF;

    -- Full text search index on content using GIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_class c 
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relname = 'idx_documents_content_gin' AND n.nspname = 'public'
    ) THEN
        CREATE INDEX idx_documents_content_gin ON tb_document USING gin(to_tsvector('simple', content)) WHERE deleted = false;
        RAISE NOTICE 'Created index idx_documents_content_gin';
    ELSE
        RAISE NOTICE 'Index idx_documents_content_gin already exists';
    END IF;

END $$;