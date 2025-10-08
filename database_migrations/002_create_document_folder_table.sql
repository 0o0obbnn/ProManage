-- Migration: Create document folder table
-- Date: 2025-10-04
-- Description: Create tb_document_folder table to support document folder functionality

-- Create document folder table
CREATE TABLE IF NOT EXISTS tb_document_folder (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    project_id BIGINT NOT NULL,
    parent_id BIGINT DEFAULT 0,
    sort_order INTEGER DEFAULT 0,
    creator_id BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    
    CONSTRAINT fk_document_folder_project FOREIGN KEY (project_id) REFERENCES tb_project(id) ON DELETE CASCADE,
    CONSTRAINT fk_document_folder_creator FOREIGN KEY (creator_id) REFERENCES tb_user(id) ON DELETE CASCADE
);

-- Add indexes
CREATE INDEX IF NOT EXISTS idx_document_folder_project_id ON tb_document_folder(project_id);
CREATE INDEX IF NOT EXISTS idx_document_folder_parent_id ON tb_document_folder(parent_id);
CREATE INDEX IF NOT EXISTS idx_document_folder_creator_id ON tb_document_folder(creator_id);

-- Add comments
COMMENT ON TABLE tb_document_folder IS '文档文件夹表';
COMMENT ON COLUMN tb_document_folder.id IS '文件夹ID';
COMMENT ON COLUMN tb_document_folder.name IS '文件夹名称';
COMMENT ON COLUMN tb_document_folder.description IS '文件夹描述';
COMMENT ON COLUMN tb_document_folder.project_id IS '所属项目ID';
COMMENT ON COLUMN tb_document_folder.parent_id IS '父文件夹ID';
COMMENT ON COLUMN tb_document_folder.sort_order IS '排序';
COMMENT ON COLUMN tb_document_folder.creator_id IS '创建人ID';
COMMENT ON COLUMN tb_document_folder.create_time IS '创建时间';
COMMENT ON COLUMN tb_document_folder.update_time IS '更新时间';
COMMENT ON COLUMN tb_document_folder.deleted IS '删除标识';

-- Update document table to support folder_id
ALTER TABLE tb_document 
ADD COLUMN IF NOT EXISTS folder_id BIGINT DEFAULT 0;

COMMENT ON COLUMN tb_document.folder_id IS '所属文件夹ID';