-- Migration: Add content_type column to tb_document table
-- Date: 2025-10-04
-- Description: Add content_type column to support different document content types

-- Check if column exists before adding
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'tb_document' 
        AND column_name = 'content_type'
    ) THEN
        -- Add content_type column
        ALTER TABLE tb_document 
        ADD COLUMN content_type VARCHAR(50) DEFAULT 'text/html';
        
        -- Add comment
        COMMENT ON COLUMN tb_document.content_type IS 'Document content type (text/html, text/markdown, text/plain, etc.)';
        
        RAISE NOTICE 'Column content_type added to tb_document table';
    ELSE
        RAISE NOTICE 'Column content_type already exists in tb_document table';
    END IF;
END $$;

-- Update existing records to have default content_type
UPDATE tb_document 
SET content_type = 'text/html' 
WHERE content_type IS NULL;

