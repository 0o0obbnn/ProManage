package com.promanage.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.promanage.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 变更请求审批实体类
 * <p>
 * 记录变更请求的审批历史，支持多级审批流程
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_change_request_approval")
@Schema(description = "变更请求审批信息")
public class ChangeRequestApproval extends BaseEntity {

    /**
     * 审批记录ID
     */
    @TableId(type = IdType.AUTO)
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
     * 审批人姓名（冗余字段，便于查询）
     */
    @Schema(description = "审批人姓名", example = "张三")
    private String approverName;

    /**
     * 审批步骤/级别
     */
    @Schema(description = "审批步骤", example = "一级审批")
    private String approvalStep;

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
     * 审批级别（1-一级审批，2-二级审批，等）
     */
    @Schema(description = "审批级别", example = "1")
    private Integer approvalLevel;
}

