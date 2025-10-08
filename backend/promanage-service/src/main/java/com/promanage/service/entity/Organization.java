package com.promanage.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.promanage.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 组织实体类
 * <p>
 * 组织/租户信息实体，包含组织的基本信息和订阅状态
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("organizations")
@Schema(description = "组织信息")
public class Organization extends BaseEntity {

    /**
     * 组织ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "组织ID", example = "1")
    private Long id;

    /**
     * 组织名称
     */
    @Schema(description = "组织名称", example = "示例科技有限公司")
    private String name;

    /**
     * 组织标识符（唯一）
     */
    @Schema(description = "组织标识符", example = "demo-org")
    private String slug;

    /**
     * 组织描述
     */
    @Schema(description = "组织描述", example = "一个示例组织的描述信息")
    private String description;

    /**
     * 组织Logo URL
     */
    @Schema(description = "组织Logo URL", example = "https://example.com/logo.png")
    private String logoUrl;

    /**
     * 组织网站URL
     */
    @Schema(description = "组织网站URL", example = "https://example.com")
    private String websiteUrl;

    /**
     * 联系邮箱
     */
    @Schema(description = "联系邮箱", example = "contact@example.com")
    private String contactEmail;

    /**
     * 组织设置（JSON格式）
     */
    @Schema(description = "组织设置")
    private String settings;

    /**
     * 是否激活
     */
    @Schema(description = "是否激活", example = "true")
    private Boolean isActive;

    /**
     * 订阅计划
     */
    @Schema(description = "订阅计划", example = "FREE")
    private String subscriptionPlan;

    /**
     * 订阅过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "订阅过期时间")
    private LocalDateTime subscriptionExpiresAt;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID", example = "1")
    private Long createdBy;

    /**
     * 更新人ID
     */
    @Schema(description = "更新人ID", example = "1")
    private Long updatedBy;

    /**
     * 逻辑删除时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "逻辑删除时间")
    private LocalDateTime deletedAt;

    /**
     * 删除人ID
     */
    @Schema(description = "删除人ID", example = "1")
    private Long deletedBy;
}