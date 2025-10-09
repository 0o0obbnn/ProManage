-- ================================================================
-- ProManage Database Migration V1.0.4
-- Description: Create tasks table for MVP Phase 1
-- Author: ProManage Team
-- Date: 2025-10-09
-- ================================================================

CREATE TABLE tb_task (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status INTEGER NOT NULL DEFAULT 0,
    priority INTEGER NOT NULL DEFAULT 2,
    task_type VARCHAR(50),
    assignee_id BIGINT,
    creator_id BIGINT NOT NULL,
    parent_task_id BIGINT,
    start_date DATE,
    due_date DATE,
    estimated_hours DECIMAL(10,2),
    actual_hours DECIMAL(10,2),
    completion_percentage INTEGER DEFAULT 0,
    tags VARCHAR(500),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_task_project
        FOREIGN KEY(project_id)
        REFERENCES tb_project(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_task_assignee
        FOREIGN KEY(assignee_id)
        REFERENCES tb_user(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_task_creator
        FOREIGN KEY(creator_id)
        REFERENCES tb_user(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_task_parent
        FOREIGN KEY(parent_task_id)
        REFERENCES tb_task(id)
        ON DELETE SET NULL,

    CONSTRAINT chk_completion_percentage
        CHECK (completion_percentage >= 0 AND completion_percentage <= 100)
);

COMMENT ON TABLE tb_task IS '任务表';
COMMENT ON COLUMN tb_task.id IS '任务ID';
COMMENT ON COLUMN tb_task.project_id IS '所属项目ID';
COMMENT ON COLUMN tb_task.title IS '任务标题';
COMMENT ON COLUMN tb_task.description IS '任务描述';
COMMENT ON COLUMN tb_task.status IS '状态: 0-待处理, 1-进行中, 2-已完成, 3-已取消, 4-阻塞';
COMMENT ON COLUMN tb_task.priority IS '优先级: 1-低, 2-中, 3-高, 4-紧急';
COMMENT ON COLUMN tb_task.task_type IS '任务类型: FEATURE, BUG, REFACTOR, TEST, DOC';
COMMENT ON COLUMN tb_task.assignee_id IS '负责人ID';
COMMENT ON COLUMN tb_task.creator_id IS '创建人ID';
COMMENT ON COLUMN tb_task.parent_task_id IS '父任务ID（用于子任务）';
COMMENT ON COLUMN tb_task.start_date IS '开始日期';
COMMENT ON COLUMN tb_task.due_date IS '截止日期';
COMMENT ON COLUMN tb_task.estimated_hours IS '预估工时（小时）';
COMMENT ON COLUMN tb_task.actual_hours IS '实际工时（小时）';
COMMENT ON COLUMN tb_task.completion_percentage IS '完成百分比 (0-100)';
COMMENT ON COLUMN tb_task.tags IS '标签（逗号分隔）';
COMMENT ON COLUMN tb_task.deleted IS '逻辑删除标志';
COMMENT ON COLUMN tb_task.create_time IS '创建时间';
COMMENT ON COLUMN tb_task.update_time IS '更新时间';

-- Indexes for performance
CREATE INDEX idx_task_project_id ON tb_task(project_id) WHERE deleted = FALSE;
CREATE INDEX idx_task_assignee_id ON tb_task(assignee_id) WHERE deleted = FALSE;
CREATE INDEX idx_task_creator_id ON tb_task(creator_id) WHERE deleted = FALSE;
CREATE INDEX idx_task_status ON tb_task(status) WHERE deleted = FALSE;
CREATE INDEX idx_task_priority ON tb_task(priority) WHERE deleted = FALSE;
CREATE INDEX idx_task_due_date ON tb_task(due_date) WHERE deleted = FALSE AND status != 2;
CREATE INDEX idx_task_parent_id ON tb_task(parent_task_id) WHERE deleted = FALSE;
CREATE INDEX idx_task_create_time ON tb_task(create_time DESC);


-- ================================================================
-- Task Dependencies Table
-- ================================================================

CREATE TABLE tb_task_dependency (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    depends_on_task_id BIGINT NOT NULL,
    dependency_type VARCHAR(20) NOT NULL DEFAULT 'FINISH_TO_START',
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_task_dependency_task
        FOREIGN KEY(task_id)
        REFERENCES tb_task(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_task_dependency_depends_on
        FOREIGN KEY(depends_on_task_id)
        REFERENCES tb_task(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_no_self_dependency
        CHECK (task_id != depends_on_task_id),

    CONSTRAINT uq_task_dependency
        UNIQUE (task_id, depends_on_task_id)
);

COMMENT ON TABLE tb_task_dependency IS '任务依赖关系表';
COMMENT ON COLUMN tb_task_dependency.task_id IS '任务ID';
COMMENT ON COLUMN tb_task_dependency.depends_on_task_id IS '依赖的任务ID';
COMMENT ON COLUMN tb_task_dependency.dependency_type IS '依赖类型: FINISH_TO_START, START_TO_START, FINISH_TO_FINISH, START_TO_FINISH';

CREATE INDEX idx_task_dependency_task_id ON tb_task_dependency(task_id) WHERE deleted = FALSE;
CREATE INDEX idx_task_dependency_depends_on ON tb_task_dependency(depends_on_task_id) WHERE deleted = FALSE;


-- ================================================================
-- Task Comments Table
-- ================================================================

CREATE TABLE tb_task_comment (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    parent_comment_id BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_task_comment_task
        FOREIGN KEY(task_id)
        REFERENCES tb_task(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_task_comment_user
        FOREIGN KEY(user_id)
        REFERENCES tb_user(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_task_comment_parent
        FOREIGN KEY(parent_comment_id)
        REFERENCES tb_task_comment(id)
        ON DELETE CASCADE
);

COMMENT ON TABLE tb_task_comment IS '任务评论表';
COMMENT ON COLUMN tb_task_comment.task_id IS '任务ID';
COMMENT ON COLUMN tb_task_comment.user_id IS '评论用户ID';
COMMENT ON COLUMN tb_task_comment.content IS '评论内容';
COMMENT ON COLUMN tb_task_comment.parent_comment_id IS '父评论ID（用于回复）';

CREATE INDEX idx_task_comment_task_id ON tb_task_comment(task_id) WHERE deleted = FALSE;
CREATE INDEX idx_task_comment_user_id ON tb_task_comment(user_id) WHERE deleted = FALSE;
CREATE INDEX idx_task_comment_create_time ON tb_task_comment(create_time DESC);
