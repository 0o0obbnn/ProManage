-- 创建通知表
CREATE TABLE IF NOT EXISTS tb_notification (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    related_id BIGINT,
    related_type VARCHAR(50),
    is_read BOOLEAN DEFAULT FALSE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    creator_id BIGINT,
    deleted BOOLEAN DEFAULT FALSE
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_notification_user_id ON tb_notification(user_id);
CREATE INDEX IF NOT EXISTS idx_notification_type ON tb_notification(type);
CREATE INDEX IF NOT EXISTS idx_notification_related ON tb_notification(related_id, related_type);
CREATE INDEX IF NOT EXISTS idx_notification_is_read ON tb_notification(is_read);
CREATE INDEX IF NOT EXISTS idx_notification_create_time ON tb_notification(create_time);

-- 添加注释
COMMENT ON TABLE tb_notification IS '通知表';
COMMENT ON COLUMN tb_notification.id IS '通知ID';
COMMENT ON COLUMN tb_notification.user_id IS '接收者ID';
COMMENT ON COLUMN tb_notification.type IS '通知类型';
COMMENT ON COLUMN tb_notification.title IS '通知标题';
COMMENT ON COLUMN tb_notification.content IS '通知内容';
COMMENT ON COLUMN tb_notification.related_id IS '相关数据ID（如项目ID、任务ID等）';
COMMENT ON COLUMN tb_notification.related_type IS '相关数据类型';
COMMENT ON COLUMN tb_notification.is_read IS '是否已读';
COMMENT ON COLUMN tb_notification.create_time IS '创建时间';
COMMENT ON COLUMN tb_notification.update_time IS '更新时间';
COMMENT ON COLUMN tb_notification.creator_id IS '创建者ID';
COMMENT ON COLUMN tb_notification.deleted IS '是否删除';