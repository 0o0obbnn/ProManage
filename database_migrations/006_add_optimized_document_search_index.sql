-- Migration: Add optimized search index for documents table
-- Date: 2025-10-12
-- Description: Add a composite index to optimize document searching and listing, as specified in the architecture design.

CREATE INDEX CONCURRENTLY idx_documents_search_optimized 
ON tb_document(project_id, status, create_time DESC) 
INCLUDE(title, creator_id) 
WHERE deleted = false;
