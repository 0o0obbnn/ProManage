package com.promanage.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.promanage.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 变更请求实体类
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_change_request")
@Schema(description = "变更请求信息")
public class ChangeRequest extends BaseEntity {

    /**
     * 变更标题
     */
    @Schema(description = "变更标题", example = "用户界面重新设计")
    private String title;

    /**
     * 变更描述
     */
    @Schema(description = "变更描述", example = "重新设计用户登录界面，提升用户体验")
    private String description;

    /**
     * 变更原因
     */
    @Schema(description = "变更原因", example = "当前界面用户体验不佳，需要根据新需求重新设计")
    private String reason;

    /**
     * 变更状态
     * DRAFT-草稿, SUBMITTED-已提交, UNDER_REVIEW-审核中, APPROVED-已批准, REJECTED-已拒绝, IMPLEMENTED-已实施, CLOSED-已关闭
     */
    @Schema(description = "变更状态 (DRAFT-草稿, SUBMITTED-已提交, UNDER_REVIEW-审核中, APPROVED-已批准, REJECTED-已拒绝, IMPLEMENTED-已实施, CLOSED-已关闭)", example = "DRAFT")
    private String status;

    /**
     * 变更优先级
     * 1-低, 2-中, 3-高, 4-紧急
     */
    @Schema(description = "变更优先级 (1-低, 2-中, 3-高, 4-紧急)", example = "2")
    private Integer priority;

    /**
     * 影响程度
     * LOW-低, MEDIUM-中, HIGH-高, CRITICAL-关键
     */
    @Schema(description = "影响程度 (LOW-低, MEDIUM-中, HIGH-高, CRITICAL-关键)", example = "MEDIUM")
    private String impactLevel;

    /**
     * 请求人ID
     */
    @Schema(description = "请求人ID", example = "1")
    private Long requesterId;

    /**
     * 指派人ID
     */
    @Schema(description = "指派人ID", example = "2")
    private Long assigneeId;

    /**
     * 审核人ID
     */
    @Schema(description = "审核人ID", example = "3")
    private Long reviewerId;

    /**
     * 项目ID
     */
    @Schema(description = "项目ID", example = "1")
    private Long projectId;

    /**
     * 预估工时（小时）
     */
    @Schema(description = "预估工时（小时）", example = "16")
    private Integer estimatedEffort;

    /**
     * 实际工时（小时）
     */
    @Schema(description = "实际工时（小时）", example = "20")
    private Integer actualEffort;

    /**
     * 建议实施日期
     */
    @Schema(description = "建议实施日期", example = "2025-10-15")
    private LocalDate implementationDate;

    /**
     * 标签，多个标签用逗号分隔
     */
    @Schema(description = "标签，多个标签用逗号分隔", example = "UI,用户体验,前端")
    private String tags;

    /**
     * 提交时间
     */
    @Schema(description = "提交时间")
    private java.time.LocalDateTime submittedAt;

    /**
     * 批准时间
     */
    @Schema(description = "批准时间")
    private java.time.LocalDateTime approvedAt;

    /**
     * 实施完成时间
     */
    @Schema(description = "实施完成时间")
    private java.time.LocalDateTime implementedAt;

    /**
     * 关闭时间
     */
    @Schema(description = "关闭时间")
    private java.time.LocalDateTime closedAt;
}