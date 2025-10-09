package com.promanage.api.dto.response;

import com.promanage.service.entity.Document;
import com.promanage.service.entity.DocumentVersion;
import com.promanage.service.service.IDocumentService;
import com.promanage.service.service.IUserService;
import com.promanage.service.mapper.DocumentVersionMapper;
import com.promanage.common.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.stream.Collectors;

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
                .versions(null) // TODO: 从版本服务获取版本历史 - 使用fromEntityWithDetails方法
                .relatedDocuments(null) // TODO: 从关联服务获取关联文档 - 使用fromEntityWithDetails方法
                .statistics(null) // TODO: 从统计服务获取统计信息 - 使用fromEntityWithDetails方法
                .build();
    }

    /**
     * 从Document实体创建DocumentDetailResponse，包含完整的版本历史和统计信息
     *
     * @param document              Document实体
     * @param documentService       文档服务（用于获取版本历史和统计）
     * @param userService           用户服务（用于获取创建者信息）
     * @param documentVersionMapper 文档版本Mapper（用于统计版本数）
     * @return DocumentDetailResponse对象
     */
    public static DocumentDetailResponse fromEntityWithDetails(Document document, IDocumentService documentService,
                                                               IUserService userService, DocumentVersionMapper documentVersionMapper) {
        if (document == null) {
            return null;
        }

        DocumentDetailResponse response = fromEntity(document);

        // 获取版本历史（包含创建者信息）
        try {
            List<DocumentVersion> documentVersions = documentService.listVersions(document.getId());
            response.setVersions(documentVersions.stream()
                    .map(version -> DocumentVersionResponse.fromEntityWithUser(version, userService))
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            // 如果获取版本历史失败，保持为null
            response.setVersions(null);
        }

        // 获取统计信息
        try {
            DocumentStatistics stats = new DocumentStatistics();
            stats.setTotalViews(document.getViewCount());
            stats.setFavoriteCount(documentService.getFavoriteCount(document.getId()));
            stats.setWeekViews(documentService.getWeekViewCount(document.getId()));

            // 统计版本总数
            stats.setTotalVersions(documentVersionMapper.countByDocumentId(document.getId()));

            // TODO: 设置评论数 - 需要实现DocumentComment实体和Mapper
            stats.setCommentCount(0);

            response.setStatistics(stats);
        } catch (Exception e) {
            // 如果获取统计信息失败，保持为null
            response.setStatistics(null);
        }

        // TODO: 获取关联文档 - 需要实现IDocumentRelationService
        response.setRelatedDocuments(null);

        return response;
    }
}