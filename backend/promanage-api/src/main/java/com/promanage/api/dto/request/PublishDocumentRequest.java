package com.promanage.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发布文档请求DTO
 * <p>
 * 用于将草稿或审核中的文档发布为正式版本
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-09-30
 */
@Data
@Schema(description = "发布文档请求")
public class PublishDocumentRequest {

    /**
     * 文档ID
     * <p>
     * 必填项，要发布的文档ID
     * </p>
     */
    @NotNull(message = "文档ID不能为空")
    @Schema(description = "文档ID", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long documentId;

    /**
     * 版本变更说明
     * <p>
     * 必填项，描述本次发布的变更内容
     * </p>
     */
    @NotBlank(message = "变更说明不能为空")
    @Schema(description = "版本变更说明", example = "正式发布用户管理模块需求文档v1.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private String changeLog;

    /**
     * 版本号
     * <p>
     * 可选项，自定义版本号，如不指定则自动生成
     * </p>
     */
    @Schema(description = "版本号", example = "v1.0.0")
    private String version;

    /**
     * 是否通知项目成员
     * <p>
     * 可选项，默认为true
     * </p>
     */
    @Schema(description = "是否通知项目成员", example = "true", defaultValue = "true")
    private Boolean notifyMembers = true;
}