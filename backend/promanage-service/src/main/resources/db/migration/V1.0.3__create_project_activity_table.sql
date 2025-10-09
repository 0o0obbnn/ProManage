-- ================================================================
-- ProManage Database Migration V1.0.3
-- Description: Create project activity tracking table
-- Author: ProManage Team
-- Date: 2025-10-09
-- ================================================================

CREATE TABLE tb_project_activity (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT,
    activity_type VARCHAR(50) NOT NULL,
    content TEXT,
    metadata JSONB,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_project_activity_project
        FOREIGN KEY(project_id)
        REFERENCES tb_project(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_project_activity_user
        FOREIGN KEY(user_id)
        REFERENCES tb_user(id)
        ON DELETE SET NULL
);

COMMENT ON TABLE tb_project_activity IS '项目活动记录表';
COMMENT ON COLUMN tb_project_activity.id IS '活动ID';
COMMENT ON COLUMN tb_project_activity.project_id IS '关联的项目ID';
COMMENT ON COLUMN tb_project_activity.user_id IS '操作用户ID';
COMMENT ON COLUMN tb_project_activity.activity_type IS '活动类型: MEMBER_ADDED, MEMBER_REMOVED, STATUS_CHANGED, etc.';
COMMENT ON COLUMN tb_project_activity.content IS '活动内容详细描述';
COMMENT ON COLUMN tb_project_activity.metadata IS '扩展元数据(JSON格式)';
COMMENT ON COLUMN tb_project_activity.deleted IS '逻辑删除标志';
COMMENT ON COLUMN tb_project_activity.create_time IS '活动创建时间';

-- Indexes for performance
CREATE INDEX idx_project_activity_project_id ON tb_project_activity(project_id) WHERE deleted = FALSE;
CREATE INDEX idx_project_activity_user_id ON tb_project_activity(user_id) WHERE deleted = FALSE;
CREATE INDEX idx_project_activity_create_time ON tb_project_activity(create_time DESC);
CREATE INDEX idx_project_activity_type ON tb_project_activity(activity_type) WHERE deleted = FALSE;
