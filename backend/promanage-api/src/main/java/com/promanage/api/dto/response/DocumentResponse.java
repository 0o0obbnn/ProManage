package com.promanage.api.dto.response;

import com.promanage.service.entity.Document;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

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

    @Schema(description = "文档摘要", example = "本文档描述了用户管理模块的功能需求")
    private String summary;

    @Schema(description = "文档类型：PRD, Design, API, Test, Other", example = "PRD")
    private String type;

    @Schema(description = "文档状态：0-草稿，1-审核中，2-已发布，3-已归档", example = "2")
    private Integer status;

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

    @Schema(description = "当前版本号", example = "v1.2.0")
    private String currentVersion;

    @Schema(description = "浏览次数", example = "150")
    private Integer viewCount;

    @Schema(description = "标签列表", example = "需求,后端,用户模块")
    private String tags;

    @Schema(description = "优先级：1-低，2-中，3-高", example = "2")
    private Integer priority;

    @Schema(description = "创建者ID", example = "3")
    private Long creatorId;

    @Schema(description = "创建者姓名", example = "王产品")
    private String creatorName;

    @Schema(description = "创建者头像", example = "https://example.com/avatar/user3.jpg")
    private String creatorAvatar;

    @Schema(description = "创建时间", example = "2025-01-15T14:30:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-09-30T10:30:00")
    private LocalDateTime updateTime;

    @Schema(description = "最后更新者姓名", example = "李开发")
    private String updaterName;

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
                .summary(document.getSummary())
                .type(document.getType())
                .status(document.getStatus())
                .projectId(document.getProjectId())
                .folderId(document.getFolderId())
                .fileUrl(document.getFileUrl())
                .fileSize(document.getFileSize())
                .currentVersion(document.getCurrentVersion())
                .viewCount(document.getViewCount())
                .priority(document.getPriority())
                .creatorId(document.getCreatorId())
                .creatorName(null) // 需要在Controller层通过UserService填充
                .creatorAvatar(null) // 需要在Controller层通过UserService填充
                .createTime(document.getCreateTime())
                .updateTime(document.getUpdateTime())
                .updaterName(null) // 需要在Controller层通过UserService填充
                .build();
    }
}