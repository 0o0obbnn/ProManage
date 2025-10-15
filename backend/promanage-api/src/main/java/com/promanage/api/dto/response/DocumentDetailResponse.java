package com.promanage.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.promanage.service.entity.Document;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 文档详情响应DTO
 * <p>
 * 扩展文档响应，包含完整内容和版本历史
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
@EqualsAndHashCode(callSuper = true)
@Schema(description = "文档详情响应")
public class DocumentDetailResponse extends DocumentResponse {

    @Schema(description = "文档内容（Markdown格式）")
    private String content;

    @JsonProperty("content_type")
    @Schema(description = "内容类型", example = "markdown")
    private String contentType;

    @Schema(description = "版本历史列表")
    private List<DocumentVersionResponse> versions;

    @Schema(description = "附件列表")
    private List<DocumentAttachmentResponse> attachments;

    @Schema(description = "关联文档列表")
    private List<DocumentRelationResponse> relations;

    @Schema(description = "文档统计信息")
    private DocumentStatistics statistics;

    @JsonProperty("versions_count")
    @Schema(description = "版本数量", example = "4")
    private Integer versionsCount;

    @JsonProperty("comments_count")
    @Schema(description = "评论数量", example = "12")
    private Integer commentsCount;

    /**
     * 从Document实体创建DocumentDetailResponse
     *
     * @param document Document实体
     * @return DocumentDetailResponse对象
     */
    public static DocumentDetailResponse fromEntity(Document document) {
        if (document == null) {
            return null;
        }

        DocumentResponse baseResponse = DocumentResponse.fromEntity(document);

        return DocumentDetailResponse.builder()
                .id(baseResponse.getId())
                .title(baseResponse.getTitle())
                .slug(baseResponse.getSlug())
                .summary(baseResponse.getSummary())
                .type(baseResponse.getType())
                .status(baseResponse.getStatus())
                .projectId(baseResponse.getProjectId())
                .projectName(baseResponse.getProjectName())
                .folderId(baseResponse.getFolderId())
                .folderName(baseResponse.getFolderName())
                .fileUrl(baseResponse.getFileUrl())
                .fileSize(baseResponse.getFileSize())
                .currentVersion(baseResponse.getCurrentVersion())
                .viewCount(baseResponse.getViewCount())
                .likeCount(baseResponse.getLikeCount())
                .tags(baseResponse.getTags())
                .template(baseResponse.getTemplate())
                .priority(baseResponse.getPriority())
                .author(baseResponse.getAuthor())
                .reviewer(baseResponse.getReviewer())
                .category(baseResponse.getCategory())
                .creatorId(baseResponse.getCreatorId())
                .reviewerId(baseResponse.getReviewerId())
                .creatorName(baseResponse.getCreatorName())
                .creatorAvatar(baseResponse.getCreatorAvatar())
                .createTime(baseResponse.getCreateTime())
                .updateTime(baseResponse.getUpdateTime())
                .publishedAt(baseResponse.getPublishedAt())
                .updaterName(baseResponse.getUpdaterName())
                .updaterId(baseResponse.getUpdaterId())
                .content(document.getContent())
                .contentType(document.getContentType())
                .attachments(Collections.emptyList())
                .relations(Collections.emptyList())
                .versions(Collections.emptyList())
                .statistics(null)
                .versionsCount(null)
                .commentsCount(null)
                .build();
    }
}