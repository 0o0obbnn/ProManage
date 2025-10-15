-- Create tags table if not exists (used by com.promanage.service.entity.Tag -> @TableName("tags"))
CREATE TABLE IF NOT EXISTS tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    color VARCHAR(16),
    description VARCHAR(255),
    project_id BIGINT,
    creator_id BIGINT,
    usage_count INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    deleted_by BIGINT
);

CREATE INDEX IF NOT EXISTS idx_tags_project_id ON tags(project_id);
CREATE INDEX IF NOT EXISTS idx_tags_is_active ON tags(is_active);

-- Create document_tags table if not exists (used by com.promanage.service.entity.DocumentTag -> @TableName("document_tags"))
CREATE TABLE IF NOT EXISTS document_tags (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    creator_id BIGINT,
    CONSTRAINT uq_document_tag UNIQUE (document_id, tag_id)
);

CREATE INDEX IF NOT EXISTS idx_document_tags_document_id ON document_tags(document_id);
CREATE INDEX IF NOT EXISTS idx_document_tags_tag_id ON document_tags(tag_id);

-- Create tb_document_comment table if not exists (used by com.promanage.service.entity.DocumentComment -> @TableName("tb_document_comment"))
CREATE TABLE IF NOT EXISTS tb_document_comment (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_tb_document_comment_document_id ON tb_document_comment(document_id);
CREATE INDEX IF NOT EXISTS idx_tb_document_comment_user_id ON tb_document_comment(user_id);
CREATE INDEX IF NOT EXISTS idx_tb_document_comment_deleted ON tb_document_comment(deleted);


