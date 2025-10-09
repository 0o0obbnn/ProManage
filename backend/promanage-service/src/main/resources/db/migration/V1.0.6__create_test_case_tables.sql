-- ================================================================
-- ProManage Database Migration V1.0.6
-- Description: Create test case tables for MVP Phase 1
-- Author: ProManage Team
-- Date: 2025-10-09
-- ================================================================

CREATE TABLE tb_test_case (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    preconditions TEXT,
    steps TEXT,
    expected_result TEXT,
    actual_result TEXT,
    type VARCHAR(50) NOT NULL,
    status INTEGER NOT NULL DEFAULT 0,
    priority INTEGER NOT NULL DEFAULT 2,
    module VARCHAR(100),
    tags VARCHAR(500),
    requirement_id BIGINT,
    task_id BIGINT,
    creator_id BIGINT NOT NULL,
    assignee_id BIGINT,
    reviewer_id BIGINT,
    estimated_time INTEGER,
    actual_time INTEGER,
    execution_environment VARCHAR(200),
    test_data TEXT,
    failure_reason TEXT,
    severity INTEGER,
    last_executed_at TIMESTAMP,
    last_executed_by_id BIGINT,
    version_number VARCHAR(20),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_test_case_project
        FOREIGN KEY(project_id)
        REFERENCES tb_project(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_test_case_creator
        FOREIGN KEY(creator_id)
        REFERENCES tb_user(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_test_case_assignee
        FOREIGN KEY(assignee_id)
        REFERENCES tb_user(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_test_case_reviewer
        FOREIGN KEY(reviewer_id)
        REFERENCES tb_user(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_test_case_task
        FOREIGN KEY(task_id)
        REFERENCES tb_task(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_test_case_last_executed_by
        FOREIGN KEY(last_executed_by_id)
        REFERENCES tb_user(id)
        ON DELETE SET NULL
);

COMMENT ON TABLE tb_test_case IS '测试用例表';
COMMENT ON COLUMN tb_test_case.id IS '测试用例ID';
COMMENT ON COLUMN tb_test_case.project_id IS '所属项目ID';
COMMENT ON COLUMN tb_test_case.title IS '用例标题';
COMMENT ON COLUMN tb_test_case.description IS '用例描述';
COMMENT ON COLUMN tb_test_case.preconditions IS '前置条件';
COMMENT ON COLUMN tb_test_case.steps IS '测试步骤(JSON格式)';
COMMENT ON COLUMN tb_test_case.expected_result IS '预期结果';
COMMENT ON COLUMN tb_test_case.actual_result IS '实际结果';
COMMENT ON COLUMN tb_test_case.type IS '用例类型: FUNCTIONAL, PERFORMANCE, SECURITY, UI, INTEGRATION, REGRESSION, ACCEPTANCE';
COMMENT ON COLUMN tb_test_case.status IS '状态: 0-草稿, 1-待执行, 2-执行中, 3-通过, 4-失败, 5-阻塞, 6-跳过';
COMMENT ON COLUMN tb_test_case.priority IS '优先级: 1-低, 2-中, 3-高, 4-紧急';
COMMENT ON COLUMN tb_test_case.module IS '所属模块';
COMMENT ON COLUMN tb_test_case.tags IS '标签(逗号分隔)';
COMMENT ON COLUMN tb_test_case.requirement_id IS '关联需求ID';
COMMENT ON COLUMN tb_test_case.task_id IS '关联任务ID';
COMMENT ON COLUMN tb_test_case.creator_id IS '创建人ID';
COMMENT ON COLUMN tb_test_case.assignee_id IS '指派人ID';
COMMENT ON COLUMN tb_test_case.reviewer_id IS '审核人ID';
COMMENT ON COLUMN tb_test_case.estimated_time IS '预估执行时间(分钟)';
COMMENT ON COLUMN tb_test_case.actual_time IS '实际执行时间(分钟)';
COMMENT ON COLUMN tb_test_case.execution_environment IS '执行环境';
COMMENT ON COLUMN tb_test_case.test_data IS '测试数据(JSON格式)';
COMMENT ON COLUMN tb_test_case.failure_reason IS '失败原因';
COMMENT ON COLUMN tb_test_case.severity IS '严重程度: 1-轻微, 2-一般, 3-严重, 4-致命';
COMMENT ON COLUMN tb_test_case.last_executed_at IS '最后执行时间';
COMMENT ON COLUMN tb_test_case.last_executed_by_id IS '最后执行人ID';
COMMENT ON COLUMN tb_test_case.version_number IS '版本号';

-- Indexes for performance
CREATE INDEX idx_test_case_project_id ON tb_test_case(project_id) WHERE deleted = FALSE;
CREATE INDEX idx_test_case_creator_id ON tb_test_case(creator_id) WHERE deleted = FALSE;
CREATE INDEX idx_test_case_assignee_id ON tb_test_case(assignee_id) WHERE deleted = FALSE;
CREATE INDEX idx_test_case_status ON tb_test_case(status) WHERE deleted = FALSE;
CREATE INDEX idx_test_case_priority ON tb_test_case(priority) WHERE deleted = FALSE;
CREATE INDEX idx_test_case_type ON tb_test_case(type) WHERE deleted = FALSE;
CREATE INDEX idx_test_case_module ON tb_test_case(module) WHERE deleted = FALSE AND module IS NOT NULL;
CREATE INDEX idx_test_case_task_id ON tb_test_case(task_id) WHERE deleted = FALSE;
CREATE INDEX idx_test_case_create_time ON tb_test_case(create_time DESC);


-- ================================================================
-- Test Case Execution History Table
-- ================================================================

CREATE TABLE tb_test_execution (
    id BIGSERIAL PRIMARY KEY,
    test_case_id BIGINT NOT NULL,
    executor_id BIGINT NOT NULL,
    result INTEGER NOT NULL,
    actual_result TEXT,
    failure_reason TEXT,
    execution_time INTEGER,
    execution_environment VARCHAR(200),
    notes TEXT,
    attachments TEXT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_test_execution_case
        FOREIGN KEY(test_case_id)
        REFERENCES tb_test_case(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_test_execution_executor
        FOREIGN KEY(executor_id)
        REFERENCES tb_user(id)
        ON DELETE CASCADE
);

COMMENT ON TABLE tb_test_execution IS '测试执行历史表';
COMMENT ON COLUMN tb_test_execution.test_case_id IS '测试用例ID';
COMMENT ON COLUMN tb_test_execution.executor_id IS '执行人ID';
COMMENT ON COLUMN tb_test_execution.result IS '执行结果: 0-通过, 1-失败, 2-阻塞, 3-跳过';
COMMENT ON COLUMN tb_test_execution.actual_result IS '实际结果';
COMMENT ON COLUMN tb_test_execution.failure_reason IS '失败原因';
COMMENT ON COLUMN tb_test_execution.execution_time IS '执行时长(分钟)';
COMMENT ON COLUMN tb_test_execution.execution_environment IS '执行环境';
COMMENT ON COLUMN tb_test_execution.notes IS '备注';
COMMENT ON COLUMN tb_test_execution.attachments IS '附件URL(JSON格式)';

CREATE INDEX idx_test_execution_case_id ON tb_test_execution(test_case_id) WHERE deleted = FALSE;
CREATE INDEX idx_test_execution_executor_id ON tb_test_execution(executor_id) WHERE deleted = FALSE;
CREATE INDEX idx_test_execution_result ON tb_test_execution(result) WHERE deleted = FALSE;
CREATE INDEX idx_test_execution_create_time ON tb_test_execution(create_time DESC);
