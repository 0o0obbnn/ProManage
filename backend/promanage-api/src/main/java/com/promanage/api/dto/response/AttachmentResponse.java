package com.promanage.api.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 附件响应DTO
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-03
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "附件响应")
public class AttachmentResponse {

  @Schema(description = "附件ID", example = "1")
  private Long id;

  @Schema(description = "文件名", example = "需求文档.pdf")
  private String filename;

  @Schema(description = "原始文件名", example = "项目需求文档V1.0.pdf")
  private String originalName;

  @Schema(description = "文件大小（字节）", example = "1048576")
  private Long fileSize;

  @Schema(description = "文件大小（人类可读）", example = "1.0 MB")
  private String fileSizeHuman;

  @Schema(description = "MIME类型", example = "application/pdf")
  private String mimeType;

  @Schema(description = "文件类型图标", example = "pdf")
  private String fileType;

  @Schema(description = "下载URL", example = "https://example.com/api/v1/attachments/1/download")
  private String downloadUrl;

  @Schema(description = "预览URL（如果支持）", example = "https://example.com/api/v1/attachments/1/preview")
  private String previewUrl;

  @Schema(
      description = "缩略图URL（如果是图片）",
      example = "https://example.com/api/v1/attachments/1/thumbnail")
  private String thumbnailUrl;

  @Schema(description = "上传用户ID", example = "1")
  private Long uploadedById;

  @Schema(description = "上传用户姓名", example = "张三")
  private String uploadedByName;

  @Schema(description = "上传用户头像", example = "https://example.com/avatar/user1.jpg")
  private String uploadedByAvatar;

  @Schema(description = "关联实体类型", example = "TASK")
  private String entityType;

  @Schema(description = "关联实体ID", example = "123")
  private Long entityId;

  @Schema(description = "文件描述", example = "用户登录界面设计稿")
  private String description;

  @Schema(description = "上传时间", example = "2025-10-02T10:30:00")
  private LocalDateTime uploadTime;

  @Schema(description = "是否可预览", example = "true")
  private Boolean isPreviewable;

  @Schema(description = "是否可编辑", example = "false")
  private Boolean isEditable;

  @Schema(description = "文件版本", example = "1")
  private Integer version;

  @Schema(description = "文件状态", example = "ACTIVE")
  private String status;

  @Schema(description = "文件校验和（MD5）", example = "d41d8cd98f00b204e9800998ecf8427e")
  private String checksum;
}
