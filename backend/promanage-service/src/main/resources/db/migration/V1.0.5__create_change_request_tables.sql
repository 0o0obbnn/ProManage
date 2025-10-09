-- ================================================================
-- ProManage Database Migration V1.0.5
-- Description: Create change request tables for MVP Phase 1
-- Author: ProManage Team
-- Date: 2025-10-09
-- ================================================================

CREATE TABLE tb_change_request (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    reason TEXT,
    impact_analysis TEXT,
    status INTEGER NOT NULL DEFAULT 0,
    priority INTEGER NOT NULL DEFAULT 2,
    change_type VARCHAR(50),
    requester_id BIGINT NOT NULL,
    reviewer_id BIGINT,
    approved_by BIGINT,
    approved_at TIMESTAMP,
    rejected_at TIMESTAMP,
    rejection_reason TEXT,
    estimated_effort_hours DECIMAL(10,2),
    actual_effort_hours DECIMAL(10,2),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_change_request_project
        FOREIGN KEY(project_id)
        REFERENCES tb_project(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_change_request_requester
        FOREIGN KEY(requester_id)
        REFERENCES tb_user(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_change_request_reviewer
        FOREIGN KEY(reviewer_id)
        REFERENCES tb_user(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_change_request_approver
        FOREIGN KEY(approved_by)
        REFERENCES tb_user(id)
        ON DELETE SET NULL
);

COMMENT ON TABLE tb_change_request IS '变更请求表';
COMMENT ON COLUMN tb_change_request.id IS '变更请求ID';
COMMENT ON COLUMN tb_change_request.project_id IS '所属项目ID';
COMMENT ON COLUMN tb_change_request.title IS '变更标题';
COMMENT ON COLUMN tb_change_request.description IS '变更描述';
COMMENT ON COLUMN tb_change_request.reason IS '变更原因';
COMMENT ON COLUMN tb_change_request.impact_analysis IS '影响分析';
COMMENT ON COLUMN tb_change_request.status IS '状态: 0-草稿, 1-待审批, 2-评估中, 3-已批准, 4-已驳回, 5-已关闭';
COMMENT ON COLUMN tb_change_request.priority IS '优先级: 1-低, 2-中, 3-高, 4-紧急';
COMMENT ON COLUMN tb_change_request.change_type IS '变更类型: FEATURE, BUG_FIX, ENHANCEMENT, REQUIREMENT';
COMMENT ON COLUMN tb_change_request.requester_id IS '申请人ID';
COMMENT ON COLUMN tb_change_request.reviewer_id IS '审核人ID';
COMMENT ON COLUMN tb_change_request.approved_by IS '批准人ID';
COMMENT ON COLUMN tb_change_request.approved_at IS '批准时间';
COMMENT ON COLUMN tb_change_request.rejected_at IS '驳回时间';
COMMENT ON COLUMN tb_change_request.rejection_reason IS '驳回原因';
COMMENT ON COLUMN tb_change_request.estimated_effort_hours IS '预估工时（小时）';
COMMENT ON COLUMN tb_change_request.actual_effort_hours IS '实际工时（小时）';

-- Indexes for performance
CREATE INDEX idx_change_request_project_id ON tb_change_request(project_id) WHERE deleted = FALSE;
CREATE INDEX idx_change_request_requester_id ON tb_change_request(requester_id) WHERE deleted = FALSE;
CREATE INDEX idx_change_request_reviewer_id ON tb_change_request(reviewer_id) WHERE deleted = FALSE;
CREATE INDEX idx_change_request_status ON tb_change_request(status) WHERE deleted = FALSE;
CREATE INDEX idx_change_request_priority ON tb_change_request(priority) WHERE deleted = FALSE;
CREATE INDEX idx_change_request_create_time ON tb_change_request(create_time DESC);


-- ================================================================
-- Change Request Impact Table (关联受影响的项)
-- ================================================================

CREATE TABLE tb_change_request_impact (
    id BIGSERIAL PRIMARY KEY,
    change_request_id BIGINT NOT NULL,
    impact_type VARCHAR(50) NOT NULL,
    impact_target_id BIGINT NOT NULL,
    impact_description TEXT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_change_impact_request
        FOREIGN KEY(change_request_id)
        REFERENCES tb_change_request(id)
        ON DELETE CASCADE
);

COMMENT ON TABLE tb_change_request_impact IS '变更影响表';
COMMENT ON COLUMN tb_change_request_impact.change_request_id IS '变更请求ID';
COMMENT ON COLUMN tb_change_request_impact.impact_type IS '影响类型: TASK, DOCUMENT, TEST_CASE';
COMMENT ON COLUMN tb_change_request_impact.impact_target_id IS '受影响项的ID';
COMMENT ON COLUMN tb_change_request_impact.impact_description IS '影响描述';

CREATE INDEX idx_change_impact_request_id ON tb_change_request_impact(change_request_id) WHERE deleted = FALSE;
CREATE INDEX idx_change_impact_type_target ON tb_change_request_impact(impact_type, impact_target_id) WHERE deleted = FALSE;


-- ================================================================
-- Change Request Approval Table (审批记录)
-- ================================================================

CREATE TABLE tb_change_request_approval (
    id BIGSERIAL PRIMARY KEY,
    change_request_id BIGINT NOT NULL,
    approver_id BIGINT NOT NULL,
    approval_status INTEGER NOT NULL,
    approval_comment TEXT,
    approved_at TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_change_approval_request
        FOREIGN KEY(change_request_id)
        REFERENCES tb_change_request(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_change_approval_approver
        FOREIGN KEY(approver_id)
        REFERENCES tb_user(id)
        ON DELETE CASCADE
);

COMMENT ON TABLE tb_change_request_approval IS '变更审批记录表';
COMMENT ON COLUMN tb_change_request_approval.change_request_id IS '变更请求ID';
COMMENT ON COLUMN tb_change_request_approval.approver_id IS '审批人ID';
COMMENT ON COLUMN tb_change_request_approval.approval_status IS '审批状态: 0-待审批, 1-同意, 2-拒绝';
COMMENT ON COLUMN tb_change_request_approval.approval_comment IS '审批意见';
COMMENT ON COLUMN tb_change_request_approval.approved_at IS '审批时间';

CREATE INDEX idx_change_approval_request_id ON tb_change_request_approval(change_request_id) WHERE deleted = FALSE;
CREATE INDEX idx_change_approval_approver_id ON tb_change_request_approval(approver_id) WHERE deleted = FALSE;
CREATE INDEX idx_change_approval_status ON tb_change_request_approval(approval_status) WHERE deleted = FALSE;
