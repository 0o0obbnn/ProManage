package com.promanage.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.promanage.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 变更请求审批实体类
 *
 * <p>记录变更请求的审批历史，支持多级审批流程
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-03
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("tb_change_request_approval")
@Schema(description = "变更请求审批信息")
public class ChangeRequestApproval extends BaseEntity {

    /** 变更请求ID */
    @Schema(description = "变更请求ID", example = "1")
    private Long changeRequestId;

    /** 审批人ID */
    @Schema(description = "审批人ID", example = "1")
    private Long approverId;

    /** 审批人姓名（冗余字段，便于查询） */
    @Schema(description = "审批人姓名", example = "张三")
    private String approverName;

    /** 审批步骤/级别 */
    @Schema(description = "审批步骤", example = "一级审批")
    private String approvalStep;

    /** 审批状态：PENDING-待审批, APPROVED-已批准, REJECTED-已拒绝 */
    @Schema(description = "审批状态", example = "APPROVED")
    private String status;

    /** 审批意见 */
    @Schema(description = "审批意见", example = "同意此变更请求，建议尽快实施")
    private String comments;

    /** 审批时间 */
    @Schema(description = "审批时间")
    private LocalDateTime approvedAt;

    /** 审批级别（1-一级审批，2-二级审批，等） */
    @Schema(description = "审批级别", example = "1")
    private Integer approvalLevel;

    // ==================== Convenience methods for field name compatibility ====================

    /**
     * Set approval decision (delegates to status field)
     * Decision typically maps to status: APPROVED or REJECTED
     */
    public void setDecision(String decision) {
        this.status = decision;
    }

    /**
     * Set approval comment (delegates to comments field - singular/plural compatibility)
     */
    public void setComment(String comment) {
        this.comments = comment;
    }

    /**
     * Set approval time (delegates to approvedAt field)
     */
    public void setApprovalTime(LocalDateTime approvalTime) {
        this.approvedAt = approvalTime;
    }
}
