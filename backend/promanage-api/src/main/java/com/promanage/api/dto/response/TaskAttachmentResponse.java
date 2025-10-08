package com.promanage.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 任务附件响应DTO
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-07
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "任务附件响应")
public class TaskAttachmentResponse {

    @Schema(description = "附件ID", example = "1")
    private Long id;

    @Schema(description = "文件名", example = "设计文档.pdf")
    private String fileName;

    @Schema(description = "文件路径/URL", example = "/uploads/design_doc.pdf")
    private String filePath;

    @Schema(description = "文件大小（字节）", example = "102400")
    private Long fileSize;

    @Schema(description = "文件类型/MIME类型", example = "application/pdf")
    private String mimeType;

    @Schema(description = "上传用户ID", example = "1")
    private Long uploaderId;

    @Schema(description = "是否为图片", example = "false")
    private Boolean isImage;

    @Schema(description = "创建时间", example = "2025-10-02T10:30:00")
    private LocalDateTime createTime;
}