package com.promanage.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 更新变更请求请求DTO
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-03
 */
@Data
@Schema(description = "更新变更请求请求")
public class UpdateChangeRequestRequest {

    @Size(max = 500, message = "变更标题长度不能超过500个字符")
    @Schema(description = "变更标题", example = "用户界面重新设计")
    private String title;

    @Size(max = 2000, message = "变更描述长度不能超过2000个字符")
    @Schema(description = "变更详细描述", example = "重新设计用户登录界面，提升用户体验")
    private String description;

    @Size(max = 1000, message = "变更原因长度不能超过1000个字符")
    @Schema(description = "变更原因", example = "当前界面用户体验不佳，需要根据新需求重新设计")
    private String reason;

    @Schema(description = "变更优先级 (1-低, 2-中, 3-高, 4-紧急)")
    private Integer priority;

    @Schema(description = "影响程度 (LOW-低, MEDIUM-中, HIGH-高, CRITICAL-关键)")
    private String impactLevel;

    @Schema(description = "指派人ID", example = "1")
    private Long assigneeId;

    @Schema(description = "审核人ID", example = "2")
    private Long reviewerId;

    @Schema(description = "预估工时（小时）", example = "16")
    private Integer estimatedEffort;

    @Schema(description = "实际工时（小时）", example = "20")
    private Integer actualEffort;

    @Schema(description = "建议实施日期", example = "2025-10-15")
    private LocalDate implementationDate;

    @Schema(description = "标签，多个标签用逗号分隔", example = "UI,用户体验,前端")
    private String tags;

    @Schema(description = "变更状态 (DRAFT-草稿, SUBMITTED-已提交, UNDER_REVIEW-审核中, APPROVED-已批准, REJECTED-已拒绝, IMPLEMENTED-已实施, CLOSED-已关闭)")
    private String status;
}