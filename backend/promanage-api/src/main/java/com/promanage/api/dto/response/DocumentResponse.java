package com.promanage.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.promanage.service.entity.Document;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 文档响应DTO
 * <p>
 * 返回文档基本信息
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-09-30
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文档响应")
public class DocumentResponse {

    @Schema(description = "文档ID", example = "1")
    private Long id;

    @Schema(description = "文档标题", example = "用户管理模块需求文档")
    private String title;

    @Schema(description = "文档唯一标识", example = "user-management-guidelines")
    private String slug;

    @Schema(description = "文档摘要", example = "本文档描述了用户管理模块的功能需求")
    private String summary;

    @Schema(description = "文档类型：PRD, Design, API, Test, Other", example = "PRD")
    private String type;

    @Schema(description = "文档状态：DRAFT, UNDER_REVIEW, APPROVED, ARCHIVED, DEPRECATED", example = "APPROVED")
    private String status;

    @JsonProperty("project_id")
    @Schema(description = "所属项目ID", example = "1")
    private Long projectId;

    @Schema(description = "所属项目名称", example = "ProManage项目管理系统")
    private String projectName;

    @Schema(description = "文件夹ID", example = "0")
    private Long folderId;

    @Schema(description = "文件夹名称", example = "需求文档")
    private String folderName;

    @Schema(description = "文件URL", example = "https://storage.example.com/documents/doc1.md")
    private String fileUrl;

    @Schema(description = "文件大小（字节）", example = "10240")
    private Long fileSize;

    @JsonProperty("version")
    @Schema(description = "当前版本号", example = "v1.2.0")
    private String currentVersion;

    @JsonProperty("view_count")
    @Schema(description = "浏览次数", example = "150")
    private Integer viewCount;

    @JsonProperty("like_count")
    @Schema(description = "点赞次数", example = "45")
    private Integer likeCount;

    @Schema(description = "标签列表", example = "[\"需求\",\"后端\",\"用户模块\"]")
    private List<String> tags;

    @JsonProperty("is_template")
    @Schema(description = "是否为模板", example = "false")
    private Boolean template;

    @Schema(description = "优先级：1-低，2-中，3-高", example = "2")
    private Integer priority;

    @JsonProperty("author")
    @Schema(description = "作者信息")
    private DocumentUserSummary author;

    @JsonProperty("reviewer")
    @Schema(description = "审核人信息")
    private DocumentUserSummary reviewer;

    @Schema(description = "分类信息")
    private DocumentCategorySummary category;

    @Schema(hidden = true)
    private Long creatorId;

    @Schema(hidden = true)
    private Long reviewerId;

    @Schema(hidden = true)
    private String creatorName;

    @Schema(hidden = true)
    private String creatorAvatar;

    @JsonProperty("created_at")
    @Schema(description = "创建时间", example = "2025-01-15T14:30:00")
    private LocalDateTime createTime;

    @JsonProperty("updated_at")
    @Schema(description = "更新时间", example = "2025-09-30T10:30:00")
    private LocalDateTime updateTime;

    @JsonProperty("published_at")
    @Schema(description = "发布时间", example = "2025-10-01T09:00:00")
    private LocalDateTime publishedAt;

    @Schema(description = "最后更新者姓名", example = "李开发")
    private String updaterName;

    @Schema(hidden = true)
    private Long updaterId;

    /**
     * 从Document实体创建DocumentResponse
     *
     * @param document Document实体
     * @return DocumentResponse对象
     */
    public static DocumentResponse fromEntity(Document document) {
        if (document == null) {
            return null;
        }

        return DocumentResponse.builder()
                .id(document.getId())
                .title(document.getTitle())
                .slug(null)
                .summary(document.getSummary())
                .type(document.getType())
                .status(com.promanage.service.enums.DocumentStatus.toApiName(document.getStatus()))
                .projectId(document.getProjectId())
                .folderId(document.getFolderId())
                .fileUrl(document.getFileUrl())
                .fileSize(document.getFileSize())
                .currentVersion(document.getCurrentVersion())
                .viewCount(document.getViewCount())
                .likeCount(null)
                .tags(Collections.emptyList())
                .template(document.getIsTemplate())
                .priority(document.getPriority())
                .creatorId(document.getCreatorId())
                .reviewerId(document.getReviewerId())
                .creatorName(null) // 需要在Controller层通过UserService填充
                .creatorAvatar(null) // 需要在Controller层通过UserService填充
                .createTime(document.getCreateTime())
                .updateTime(document.getUpdateTime())
                .publishedAt(document.getPublishedAt())
                .updaterName(null) // 需要在Controller层通过UserService填充
                .updaterId(document.getUpdaterId())
                .category(document.getCategoryId() != null
                        ? DocumentCategorySummary.builder().id(document.getCategoryId()).build()
                        : null)
                .build();
    }
}