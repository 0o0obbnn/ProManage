package com.promanage.api.dto.response;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.promanage.common.entity.User;
import com.promanage.service.entity.DocumentVersion;
import com.promanage.service.service.IUserService;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 文档版本响应 */

/**
 * 文档版本响应DTO
 *
 * <p>返回文档的版本历史信息
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-09-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文档版本响应")
public class DocumentVersionResponse {

  private static final Logger log = LoggerFactory.getLogger(DocumentVersionResponse.class);

  @Schema(description = "版本ID", example = "1")
  private Long id;

  @Schema(description = "文档ID", example = "10")
  private Long documentId;

  @Schema(description = "版本号", example = "v1.2.0")
  private String version;

  @Schema(description = "版本标题", example = "新增变更管理章节")
  private String title;

  @JsonProperty("changelog")
  @Schema(description = "变更说明", example = "添加了新的功能需求，修改了部分业务逻辑")
  private String changeLog;

  @Schema(description = "文件URL", example = "https://storage.example.com/documents/doc1_v1.2.0.md")
  private String fileUrl;

  @Schema(description = "文件大小（字节）", example = "10240")
  private Long fileSize;

  @Schema(description = "内容哈希值", example = "a3b2c1d4e5f6...")
  private String contentHash;

  @Schema(hidden = true)
  private Long creatorId;

  @Schema(hidden = true)
  private String creatorName;

  @Schema(hidden = true)
  private String creatorAvatar;

  @JsonProperty("author")
  @Schema(description = "版本作者信息")
  private DocumentUserSummary author;

  @JsonProperty("created_at")
  @Schema(description = "创建时间", example = "2025-09-25T16:20:00")
  private LocalDateTime createTime;

  @Schema(description = "是否为当前版本", example = "true")
  private Boolean isCurrent;

  /**
   * 从DocumentVersion实体创建DocumentVersionResponse
   *
   * @param documentVersion DocumentVersion实体
   * @return DocumentVersionResponse对象
   */
  public static DocumentVersionResponse fromEntity(DocumentVersion documentVersion) {
    if (documentVersion == null) {
      return null;
    }

    return DocumentVersionResponse.builder()
        .id(documentVersion.getId())
        .documentId(documentVersion.getDocumentId())
        .version(documentVersion.getVersionNumber())
        .title(documentVersion.getTitle())
        .changeLog(documentVersion.getChangeLog())
        .fileUrl(documentVersion.getFileUrl())
        .fileSize(documentVersion.getFileSize())
        .contentHash(documentVersion.getContentHash())
        .creatorId(documentVersion.getCreatorId())
        .author(null)
        // 创建者名称/头像仅在fromEntityWithUser提供
        .creatorName(null)
        .creatorAvatar(null)
        .createTime(documentVersion.getCreateTime())
        .isCurrent(documentVersion.getIsCurrent())
        .build();
  }

  /**
   * 从DocumentVersion实体创建DocumentVersionResponse，包含创建者信息（使用Map）
   *
   * <p>推荐使用此方法，避免N+1查询问题。用户信息应通过批量查询获取后传入。
   *
   * @param documentVersion DocumentVersion实体
   * @param userMap 用户ID到User对象的映射（通过批量查询获取）
   * @return DocumentVersionResponse对象
   */
  public static DocumentVersionResponse fromEntityWithUserMap(
      DocumentVersion documentVersion, Map<Long, User> userMap) {
    if (documentVersion == null) {
      return null;
    }

    // 从Map中获取创建者信息
    User creator =
        Optional.ofNullable(documentVersion.getCreatorId())
            .map(userMap::get)
            .orElse(null);

    String creatorName = null;
    String creatorAvatar = null;
    DocumentUserSummary authorInfo = null;

    if (creator != null) {
      creatorName = creator.getRealName();
      creatorAvatar = creator.getAvatar();
      authorInfo =
          DocumentUserSummary.builder()
              .id(creator.getId())
              .username(creator.getUsername())
              .displayName(creatorName != null ? creatorName : creator.getUsername())
              .avatar(creatorAvatar)
              .build();
    }

    return DocumentVersionResponse.builder()
        .id(documentVersion.getId())
        .documentId(documentVersion.getDocumentId())
        .version(documentVersion.getVersionNumber())
        .title(documentVersion.getTitle())
        .changeLog(documentVersion.getChangeLog())
        .fileUrl(documentVersion.getFileUrl())
        .fileSize(documentVersion.getFileSize())
        .contentHash(documentVersion.getContentHash())
        .creatorId(documentVersion.getCreatorId())
        .creatorName(creatorName)
        .creatorAvatar(creatorAvatar)
        .author(authorInfo)
        .createTime(documentVersion.getCreateTime())
        .isCurrent(documentVersion.getIsCurrent())
        .build();
  }

  /**
   * 从DocumentVersion实体创建DocumentVersionResponse，包含创建者信息
   *
   * <p><b>已废弃</b>：此方法会导致N+1查询问题。请使用 {@link #fromEntityWithUserMap(DocumentVersion, Map)}
   * 方法，并在服务层通过批量查询获取用户信息。
   *
   * @param documentVersion DocumentVersion实体
   * @param userService 用户服务（用于获取创建者信息）
   * @return DocumentVersionResponse对象
   * @deprecated 使用 {@link #fromEntityWithUserMap(DocumentVersion, Map)} 替代，避免N+1查询
   */
  @Deprecated(since = "2025-11-01", forRemoval = true)
  public static DocumentVersionResponse fromEntityWithUser(
      DocumentVersion documentVersion, IUserService userService) {
    if (documentVersion == null) {
      return null;
    }

    // 获取创建者信息
    String creatorName = null;
    String creatorAvatar = null;
    DocumentUserSummary authorInfo = null;

    if (documentVersion.getCreatorId() != null) {
      try {
        User creator = userService.getById(documentVersion.getCreatorId());
        if (creator != null) {
          creatorName = creator.getRealName();
          creatorAvatar = creator.getAvatar();
          authorInfo =
              DocumentUserSummary.builder()
                  .id(creator.getId())
                  .username(creator.getUsername())
                  .displayName(creatorName != null ? creatorName : creator.getUsername())
                  .avatar(creatorAvatar)
                  .build();
        }
      } catch (Exception e) {
        // 如果获取用户信息失败，保持为null
        log.error("Failed to fetch user info for creatorId: {}", documentVersion.getCreatorId(), e);
      }
    }

    return DocumentVersionResponse.builder()
        .id(documentVersion.getId())
        .documentId(documentVersion.getDocumentId())
        .version(documentVersion.getVersionNumber())
        .title(documentVersion.getTitle())
        .changeLog(documentVersion.getChangeLog())
        .fileUrl(documentVersion.getFileUrl())
        .fileSize(documentVersion.getFileSize())
        .contentHash(documentVersion.getContentHash())
        .creatorId(documentVersion.getCreatorId())
        .creatorName(creatorName)
        .creatorAvatar(creatorAvatar)
        .author(authorInfo)
        .createTime(documentVersion.getCreateTime())
        .isCurrent(documentVersion.getIsCurrent())
        .build();
  }
}
