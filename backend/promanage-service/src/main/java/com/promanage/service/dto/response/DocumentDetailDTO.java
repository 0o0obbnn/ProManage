package com.promanage.service.dto.response;

import java.util.Collections;

import com.promanage.service.entity.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文档详情响应DTO（服务层）- 使用组合模式减少字段数量
 *
 * <p>Service layer DTO for document details without API annotations
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

  /** 文档基本信息 */
  private DocumentDetailBasicInfo basicInfo;

  /** 文档文件信息 */
  private DocumentDetailFileInfo fileInfo;

  /** 文档统计信息 */
  private DocumentDetailStatsInfo statsInfo;

  /** 文档用户信息 */
  private DocumentDetailUserInfo userInfo;

  /** 文档时间信息 */
  private DocumentDetailTimeInfo timeInfo;

  /** 文档关联信息 */
  private DocumentDetailRelationInfo relationInfo;

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
        .basicInfo(DocumentDetailBasicInfo.builder()
            .id(document.getId())
            .title(document.getTitle())
            .slug(null)
            .summary(document.getSummary())
            .type(document.getType())
            .status(document.getStatus() != null ? document.getStatus().toString() : null)
            .projectId(document.getProjectId())
            .projectName(null)
            .folderId(document.getFolderId())
            .folderName(null)
            .build())
        .fileInfo(DocumentDetailFileInfo.builder()
            .fileUrl(document.getFileUrl())
            .fileSize(document.getFileSize())
            .currentVersion(document.getCurrentVersion())
            .content(document.getContent())
            .contentType(document.getContentType())
            .build())
        .statsInfo(DocumentDetailStatsInfo.builder()
            .viewCount(document.getViewCount())
            .likeCount(null)
            .tags(Collections.emptyList())
            .template(document.getIsTemplate())
            .priority(document.getPriority())
            .build())
        .userInfo(DocumentDetailUserInfo.builder()
            .creatorId(document.getCreatorId())
            .creatorName(null)
            .creatorAvatar(null)
            .reviewerId(document.getReviewerId())
            .reviewerName(null)
            .reviewerAvatar(null)
            .updaterId(document.getUpdaterId())
            .updaterName(null)
            .build())
        .timeInfo(DocumentDetailTimeInfo.builder()
            .createTime(document.getCreateTime())
            .updateTime(document.getUpdateTime())
            .publishedAt(document.getPublishedAt())
            .build())
        .relationInfo(DocumentDetailRelationInfo.builder()
            .versions(Collections.emptyList())
            .statistics(null)
            .build())
        .build();
  }

  // ==================== Convenience delegation methods ====================

  /**
   * Get creator ID (delegates to userInfo)
   */
  public Long getCreatorId() {
    return userInfo != null ? userInfo.getCreatorId() : null;
  }

  /**
   * Set creator name (delegates to userInfo)
   */
  public void setCreatorName(String creatorName) {
    if (userInfo != null) {
      userInfo.setCreatorName(creatorName);
    }
  }

  /**
   * Set creator avatar (delegates to userInfo)
   */
  public void setCreatorAvatar(String creatorAvatar) {
    if (userInfo != null) {
      userInfo.setCreatorAvatar(creatorAvatar);
    }
  }

  /**
   * Set document versions (delegates to relationInfo)
   */
  public void setVersions(java.util.List<DocumentVersionDTO> versions) {
    if (relationInfo != null) {
      relationInfo.setVersions(versions);
    }
  }

  /**
   * Set document statistics (delegates to relationInfo)
   */
  public void setStatistics(DocumentStatisticsDTO statistics) {
    if (relationInfo != null) {
      relationInfo.setStatistics(statistics);
    }
  }
}
