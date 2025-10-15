package com.promanage.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.promanage.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 变更请求影响分析实体类
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_change_request_impact")
@Schema(description = "变更请求影响分析信息")
public class ChangeRequestImpact extends BaseEntity {

    /**
     * 变更请求ID
     */
    @Schema(description = "变更请求ID", example = "1")
    private Long changeRequestId;

    /**
     * 受影响实体类型
     * DOCUMENT-文档, TASK-任务, PROJECT-项目, USER-用户, SYSTEM-系统
     */
    @Schema(description = "受影响实体类型 (DOCUMENT, TASK, PROJECT, USER, SYSTEM)", example = "DOCUMENT")
    private String entityType;

    /**
     * 受影响实体ID
     */
    @Schema(description = "受影响实体ID", example = "123")
    private Long entityId;

    /**
     * 受影响实体标题
     */
    @Schema(description = "受影响实体标题", example = "用户登录界面设计文档")
    private String entityTitle;

    /**
     * 影响程度
     * LOW-低, MEDIUM-中, HIGH-高, CRITICAL-关键
     */
    @Schema(description = "影响程度 (LOW-低, MEDIUM-中, HIGH-高, CRITICAL-关键)", example = "MEDIUM")
    private String impactLevel;

    /**
     * 影响描述
     */
    @Schema(description = "影响描述", example = "界面重新设计会影响此文档的结构")
    private String impactDescription;

    /**
     * 置信度分数 (0.0-1.0)
     */
    @Schema(description = "置信度分数 (0.0-1.0)", example = "0.85")
    private Double confidenceScore;

    /**
     * 是否已人工验证
     */
    @Schema(description = "是否已人工验证", example = "false")
    private Boolean isVerified;

    /**
     * 验证人ID
     */
    @Schema(description = "验证人ID", example = "1")
    private Long verifiedBy;

    /**
     * 验证时间
     */
    @Schema(description = "验证时间")
    private java.time.LocalDateTime verifiedAt;

    /**
     * 分析算法版本
     */
    @Schema(description = "分析算法版本", example = "v1.0")
    private String analysisVersion;

    /**
     * 分析详情（JSON格式）
     */
    @Schema(description = "分析详情（JSON格式）")
    private String analysisDetails;

    /**
     * 是否有效
     */
    @Schema(description = "是否有效", example = "true")
    private Boolean isValid;
}