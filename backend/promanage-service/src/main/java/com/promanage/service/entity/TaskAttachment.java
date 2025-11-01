package com.promanage.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import com.promanage.common.entity.BaseEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务附件实体类
 *
 * <p>任务相关的附件信息
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-07
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_task_attachment")
@Schema(description = "任务附件信息")
public class TaskAttachment extends BaseEntity {

  /** 任务ID */
  @Schema(description = "任务ID", example = "1")
  private Long taskId;

  /** 项目ID */
  @Schema(description = "项目ID", example = "1")
  private Long projectId;

  /** 文件名 */
  @Schema(description = "文件名", example = "设计文档.pdf")
  private String fileName;

  /** 文件路径/URL */
  @Schema(description = "文件路径/URL", example = "/uploads/design_doc.pdf")
  private String filePath;

  /** 文件大小（字节） */
  @Schema(description = "文件大小（字节）", example = "102400")
  private Long fileSize;

  /** 文件类型/MIME类型 */
  @Schema(description = "文件类型/MIME类型", example = "application/pdf")
  private String mimeType;

  /** 上传用户ID */
  @Schema(description = "上传用户ID", example = "1")
  private Long uploaderId;

  /** 是否为图片 */
  @Schema(description = "是否为图片", example = "false")
  private Boolean isImage;
}
