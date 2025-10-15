package com.promanage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.Map;

/**
 * 组织设置DTO
 * <p>
 * 用于组织级别的配置管理
 * </p>
 *
 * @author ProManage Team
 * @since 2025-10-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "组织设置信息")
public class OrganizationSettingsDTO {

    /**
     * 通知配置
     */
    @Valid
    @Schema(description = "通知配置")
    private NotificationSettings notification;

    /**
     * 安全配置
     */
    @Valid
    @Schema(description = "安全配置")
    private SecuritySettings security;

    /**
     * 项目配置
     */
    @Valid
    @Schema(description = "项目配置")
    private ProjectSettings project;

    /**
     * 自定义配置
     */
    @Schema(description = "自定义配置")
    private Map<String, Object> custom;

    /**
     * 通知设置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "通知设置")
    public static class NotificationSettings {
        /**
         * 邮件通知开关
         */
        @NotNull(message = "邮件通知开关不能为空")
        @Schema(description = "邮件通知开关", example = "true")
        private Boolean emailEnabled;

        /**
         * 站内信通知开关
         */
        @NotNull(message = "站内信通知开关不能为空")
        @Schema(description = "站内信通知开关", example = "true")
        private Boolean inAppEnabled;

        /**
         * WebSocket实时通知开关
         */
        @NotNull(message = "WebSocket实时通知开关不能为空")
        @Schema(description = "WebSocket实时通知开关", example = "true")
        private Boolean websocketEnabled;

        /**
         * 通知摘要频率（天）
         */
        @Min(value = 1, message = "通知摘要频率不能小于1天")
        @Max(value = 30, message = "通知摘要频率不能大于30天")
        @Schema(description = "通知摘要频率（天）", example = "1")
        private Integer digestFrequencyDays;
    }

    /**
     * 安全设置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "安全设置")
    public static class SecuritySettings {
        /**
         * 密码最小长度
         */
        @Min(value = 6, message = "密码最小长度不能小于6")
        @Max(value = 128, message = "密码最小长度不能大于128")
        @Schema(description = "密码最小长度", example = "8")
        private Integer passwordMinLength;

        /**
         * 密码是否需要特殊字符
         */
        @NotNull(message = "密码是否需要特殊字符不能为空")
        @Schema(description = "密码是否需要特殊字符", example = "true")
        private Boolean passwordRequireSpecialChar;

        /**
         * 会话超时时间（分钟）
         */
        @Min(value = 5, message = "会话超时时间不能小于5分钟")
        @Max(value = 1440, message = "会话超时时间不能大于1440分钟（24小时）")
        @Schema(description = "会话超时时间（分钟）", example = "60")
        private Integer sessionTimeoutMinutes;

        /**
         * 是否开启双因素认证
         */
        @NotNull(message = "是否开启双因素认证不能为空")
        @Schema(description = "是否开启双因素认证", example = "false")
        private Boolean twoFactorAuthEnabled;

        /**
         * IP白名单
         */
        @Schema(description = "IP白名单")
        private java.util.List<String> ipWhitelist;
    }

    /**
     * 项目设置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "项目设置")
    public static class ProjectSettings {
        /**
         * 默认项目可见性
         */
        @NotBlank(message = "默认项目可见性不能为空")
        @Pattern(regexp = "^(PUBLIC|PRIVATE|INTERNAL)$", message = "项目可见性只能是PUBLIC、PRIVATE或INTERNAL")
        @Schema(description = "默认项目可见性", example = "PRIVATE")
        private String defaultVisibility;

        /**
         * 是否允许创建公开项目
         */
        @NotNull(message = "是否允许创建公开项目不能为空")
        @Schema(description = "是否允许创建公开项目", example = "true")
        private Boolean allowPublicProjects;

        /**
         * 项目数量限制
         */
        @Min(value = 1, message = "项目数量限制不能小于1")
        @Max(value = 1000, message = "项目数量限制不能大于1000")
        @Schema(description = "项目数量限制", example = "100")
        private Integer maxProjects;

        /**
         * 单个项目成员数量限制
         */
        @Min(value = 1, message = "单个项目成员数量限制不能小于1")
        @Max(value = 500, message = "单个项目成员数量限制不能大于500")
        @Schema(description = "单个项目成员数量限制", example = "50")
        private Integer maxMembersPerProject;

        /**
         * 文件存储配额（MB）
         */
        @Min(value = 100, message = "文件存储配额不能小于100MB")
        @Max(value = 1024000, message = "文件存储配额不能大于1TB")
        @Schema(description = "文件存储配额（MB）", example = "10240")
        private Long storageLimitMb;
    }
}