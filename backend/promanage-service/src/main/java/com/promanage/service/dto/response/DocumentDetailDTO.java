package com.promanage.service.dto.response;

import com.promanage.service.entity.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 文档详情响应DTO（服务层）
 * <p>
 * Service layer DTO for document details without API annotations
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDetailDTO {

    // === 基本信息 ===
    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String type;
    private String status;
    private Long projectId;
    private String projectName;
    private Long folderId;
    private String folderName;

    // === 文件信息 ===
    private String fileUrl;
    private Long fileSize;
    private String currentVersion;
    private String content;
    private String contentType;

    // === 统计信息 ===
    private Integer viewCount;
    private Integer likeCount;

    // === 元数据 ===
    private List<String> tags;
    private Boolean template;
    private Integer priority;

    // === 用户信息 ===
    private Long creatorId;
    private String creatorName;
    private String creatorAvatar;
    private Long reviewerId;
    private String reviewerName;
    private String reviewerAvatar;
    private Long updaterId;
    private String updaterName;

    // === 时间信息 ===
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime publishedAt;

    // === 关联信息 ===
    private List<DocumentVersionDTO> versions;
    private DocumentStatisticsDTO statistics;

    /**
     * 从Document实体创建DocumentDetailDTO
     *
     * @param document Document实体
     * @return DocumentDetailDTO对象
     */
    public static DocumentDetailDTO fromEntity(Document document) {
        if (document == null) {
            return null;
        }

        return DocumentDetailDTO.builder()
                .id(document.getId())
                .title(document.getTitle())
                .slug(null)
                .summary(document.getSummary())
                .type(document.getType())
                .status(document.getStatus() != null ? document.getStatus().toString() : null)
                .projectId(document.getProjectId())
                .folderId(document.getFolderId())
                .fileUrl(document.getFileUrl())
                .fileSize(document.getFileSize())
                .currentVersion(document.getCurrentVersion())
                .content(document.getContent())
                .contentType(document.getContentType())
                .viewCount(document.getViewCount())
                .likeCount(null)
                .tags(Collections.emptyList())
                .template(document.getIsTemplate())
                .priority(document.getPriority())
                .creatorId(document.getCreatorId())
                .reviewerId(document.getReviewerId())
                .updaterId(document.getUpdaterId())
                .createTime(document.getCreateTime())
                .updateTime(document.getUpdateTime())
                .publishedAt(document.getPublishedAt())
                .versions(Collections.emptyList())
                .statistics(null)
                .build();
    }
}
