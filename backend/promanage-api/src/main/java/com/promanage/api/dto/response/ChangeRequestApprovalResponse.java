package com.promanage.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 变更请求审批响应DTO
 * <p>
 * 返回变更请求审批历史信息
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-05
 */
@Data
@Builder
@Schema(description = "变更请求审批响应")
public class ChangeRequestApprovalResponse {

    /**
     * 审批记录ID
     */
    @Schema(description = "审批记录ID", example = "1")
    private Long id;

    /**
     * 变更请求ID
     */
    @Schema(description = "变更请求ID", example = "1")
    private Long changeRequestId;

    /**
     * 审批人ID
     */
    @Schema(description = "审批人ID", example = "1")
    private Long approverId;

    /**
     * 审批人姓名
     */
    @Schema(description = "审批人姓名", example = "张三")
    private String approverName;

    /**
     * 审批人头像
     */
    @Schema(description = "审批人头像URL")
    private String approverAvatar;

    /**
     * 审批步骤/级别
     */
    @Schema(description = "审批步骤", example = "一级审批")
    private String approvalStep;

    /**
     * 审批级别（1-一级审批，2-二级审批，等）
     */
    @Schema(description = "审批级别", example = "1")
    private Integer approvalLevel;

    /**
     * 审批状态：PENDING-待审批, APPROVED-已批准, REJECTED-已拒绝
     */
    @Schema(description = "审批状态", example = "APPROVED")
    private String status;

    /**
     * 审批意见
     */
    @Schema(description = "审批意见", example = "同意此变更请求，建议尽快实施")
    private String comments;

    /**
     * 审批时间
     */
    @Schema(description = "审批时间")
    private LocalDateTime approvedAt;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
