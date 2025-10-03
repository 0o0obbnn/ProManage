package com.promanage.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 变更请求响应DTO
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-03
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "变更请求响应")
public class ChangeRequestResponse {

    @Schema(description = "变更请求ID", example = "1")
    private Long id;

    @Schema(description = "变更标题", example = "用户界面重新设计")
    private String title;

    @Schema(description = "变更描述", example = "重新设计用户登录界面，提升用户体验")
    private String description;

    @Schema(description = "变更原因", example = "当前界面用户体验不佳，需要根据新需求重新设计")
    private String reason;

    @Schema(description = "变更状态 (DRAFT-草稿, SUBMITTED-已提交, UNDER_REVIEW-审核中, APPROVED-已批准, REJECTED-已拒绝, IMPLEMENTED-已实施, CLOSED-已关闭)", example = "UNDER_REVIEW")
    private String status;

    @Schema(description = "变更优先级 (1-低, 2-中, 3-高, 4-紧急)", example = "2")
    private Integer priority;

    @Schema(description = "影响程度 (LOW-低, MEDIUM-中, HIGH-高, CRITICAL-关键)", example = "MEDIUM")
    private String impactLevel;

    @Schema(description = "请求人ID", example = "1")
    private Long requesterId;

    @Schema(description = "请求人姓名", example = "张三")
    private String requesterName;

    @Schema(description = "请求人头像", example = "https://example.com/avatar/user1.jpg")
    private String requesterAvatar;

    @Schema(description = "指派人ID", example = "2")
    private Long assigneeId;

    @Schema(description = "指派人姓名", example = "李四")
    private String assigneeName;

    @Schema(description = "指派人头像", example = "https://example.com/avatar/user2.jpg")
    private String assigneeAvatar;

    @Schema(description = "审核人ID", example = "3")
    private Long reviewerId;

    @Schema(description = "审核人姓名", example = "王五")
    private String reviewerName;

    @Schema(description = "审核人头像", example = "https://example.com/avatar/user3.jpg")
    private String reviewerAvatar;

    @Schema(description = "项目ID", example = "1")
    private Long projectId;

    @Schema(description = "项目名称", example = "ProManage系统开发")
    private String projectName;

    @Schema(description = "预估工时（小时）", example = "16")
    private Integer estimatedEffort;

    @Schema(description = "实际工时（小时）", example = "20")
    private Integer actualEffort;

    @Schema(description = "建议实施日期", example = "2025-10-15")
    private LocalDate implementationDate;

    @Schema(description = "标签，多个标签用逗号分隔", example = "UI,用户体验,前端")
    private String tags;

    @Schema(description = "提交时间", example = "2025-10-01T09:00:00")
    private LocalDateTime submittedAt;

    @Schema(description = "批准时间", example = "2025-10-03T14:30:00")
    private LocalDateTime approvedAt;

    @Schema(description = "实施完成时间", example = "2025-10-15T16:00:00")
    private LocalDateTime implementedAt;

    @Schema(description = "创建时间", example = "2025-09-30T10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-10-03T14:30:00")
    private LocalDateTime updateTime;

    @Schema(description = "评论数量", example = "5")
    private Integer commentCount;

    @Schema(description = "影响分析结果数量", example = "3")
    private Integer impactCount;
}