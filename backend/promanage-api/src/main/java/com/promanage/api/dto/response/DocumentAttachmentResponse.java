package com.promanage.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文档附件信息")
public class DocumentAttachmentResponse {

    @Schema(description = "附件ID", example = "101")
    private Long id;

    @Schema(description = "文件名", example = "flow-chart.png")
    private String filename;

    @JsonProperty("original_name")
    @Schema(description = "原始文件名", example = "流程图.png")
    private String originalName;

    @Schema(description = "附件URL", example = "https://cdn.example.com/files/flow-chart.png")
    private String url;

    @JsonProperty("file_size")
    @Schema(description = "文件大小（字节）", example = "204800")
    private Long fileSize;

    @JsonProperty("content_type")
    @Schema(description = "文件类型", example = "image/png")
    private String contentType;

    @JsonProperty("uploaded_by")
    @Schema(description = "上传者信息")
    private DocumentUserSummary uploadedBy;

    @JsonProperty("uploaded_at")
    @Schema(description = "上传时间", example = "2025-09-20T11:12:00")
    private LocalDateTime uploadedAt;
}
