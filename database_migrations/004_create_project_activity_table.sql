-- 数据库迁移脚本: 创建项目活动表

CREATE TABLE project_activity (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT,
    activity_type VARCHAR(255) NOT NULL,
    content TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_project
        FOREIGN KEY(project_id) 
        REFERENCES projects(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_user
        FOREIGN KEY(user_id) 
        REFERENCES users(id)
        ON DELETE SET NULL
);

COMMENT ON TABLE project_activity IS '项目活动记录表';
COMMENT ON COLUMN project_activity.id IS '活动ID';
COMMENT ON COLUMN project_activity.project_id IS '关联的项目ID';
COMMENT ON COLUMN project_activity.user_id IS '操作用户的ID';
COMMENT ON COLUMN project_activity.activity_type IS '活动类型 (例如: MEMBER_ADDED, MEMBER_REMOVED)';
COMMENT ON COLUMN project_activity.content IS '活动内容的详细描述';
COMMENT ON COLUMN project_activity.created_at IS '活动创建时间';

CREATE INDEX idx_project_activity_project_id ON project_activity(project_id);
CREATE INDEX idx_project_activity_created_at ON project_activity(created_at);
