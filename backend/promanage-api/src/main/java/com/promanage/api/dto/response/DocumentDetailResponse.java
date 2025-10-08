package com.promanage.api.dto.response;

import com.promanage.service.entity.Document;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

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

    @Schema(description = "版本历史列表")
    private List<DocumentVersionResponse> versions;

    @Schema(description = "关联文档列表")
    private List<DocumentResponse> relatedDocuments;

    @Schema(description = "文档统计信息")
    private DocumentStatistics statistics;

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
                .tags(baseResponse.getTags())
                .priority(baseResponse.getPriority())
                .creatorId(baseResponse.getCreatorId())
                .creatorName(baseResponse.getCreatorName())
                .creatorAvatar(baseResponse.getCreatorAvatar())
                .createTime(baseResponse.getCreateTime())
                .updateTime(baseResponse.getUpdateTime())
                .updaterName(baseResponse.getUpdaterName())
                .content(document.getContent())
                .versions(null) // TODO: 从版本服务获取版本历史
                .relatedDocuments(null) // TODO: 从关联服务获取关联文档
                .statistics(null) // TODO: 从统计服务获取统计信息
                .build();
    }
}