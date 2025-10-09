package com.promanage.api.dto.response;

import com.promanage.service.entity.DocumentVersion;
import com.promanage.service.service.IUserService;
import com.promanage.common.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文档版本响应DTO
 * <p>
 * 返回文档的版本历史信息
 * </p>
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

    @Schema(description = "版本ID", example = "1")
    private Long id;

    @Schema(description = "文档ID", example = "10")
    private Long documentId;

    @Schema(description = "版本号", example = "v1.2.0")
    private String version;

    @Schema(description = "变更说明", example = "添加了新的功能需求，修改了部分业务逻辑")
    private String changeLog;

    @Schema(description = "文件URL", example = "https://storage.example.com/documents/doc1_v1.2.0.md")
    private String fileUrl;

    @Schema(description = "文件大小（字节）", example = "10240")
    private Long fileSize;

    @Schema(description = "内容哈希值", example = "a3b2c1d4e5f6...")
    private String contentHash;

    @Schema(description = "创建者ID", example = "5")
    private Long creatorId;

    @Schema(description = "创建者姓名", example = "李开发")
    private String creatorName;

    @Schema(description = "创建者头像", example = "https://example.com/avatar/user5.jpg")
    private String creatorAvatar;

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
                .changeLog(documentVersion.getChangeLog())
                .fileUrl(documentVersion.getFileUrl())
                .fileSize(documentVersion.getFileSize())
                .contentHash(documentVersion.getContentHash())
                .creatorId(documentVersion.getCreatorId())
                .creatorName(null) // TODO: 需要从用户服务获取 - 使用fromEntityWithUser方法
                .creatorAvatar(null) // TODO: 需要从用户服务获取 - 使用fromEntityWithUser方法
                .createTime(documentVersion.getCreateTime())
                .isCurrent(documentVersion.getIsCurrent())
                .build();
    }

    /**
     * 从DocumentVersion实体创建DocumentVersionResponse，包含创建者信息
     *
     * @param documentVersion DocumentVersion实体
     * @param userService     用户服务（用于获取创建者信息）
     * @return DocumentVersionResponse对象
     */
    public static DocumentVersionResponse fromEntityWithUser(DocumentVersion documentVersion, IUserService userService) {
        if (documentVersion == null) {
            return null;
        }

        // 获取创建者信息
        String creatorName = null;
        String creatorAvatar = null;

        if (documentVersion.getCreatorId() != null) {
            try {
                User creator = userService.getById(documentVersion.getCreatorId());
                if (creator != null) {
                    creatorName = creator.getRealName();
                    creatorAvatar = creator.getAvatar();
                }
            } catch (Exception e) {
                // 如果获取用户信息失败，保持为null
            }
        }

        return DocumentVersionResponse.builder()
                .id(documentVersion.getId())
                .documentId(documentVersion.getDocumentId())
                .version(documentVersion.getVersionNumber())
                .changeLog(documentVersion.getChangeLog())
                .fileUrl(documentVersion.getFileUrl())
                .fileSize(documentVersion.getFileSize())
                .contentHash(documentVersion.getContentHash())
                .creatorId(documentVersion.getCreatorId())
                .creatorName(creatorName)
                .creatorAvatar(creatorAvatar)
                .createTime(documentVersion.getCreateTime())
                .isCurrent(documentVersion.getIsCurrent())
                .build();
    }
}