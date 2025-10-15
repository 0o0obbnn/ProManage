package com.promanage.service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文档下载信息
 */
@Data
@Schema(description = "文档下载信息")
public class DocumentDownloadInfo {

    @Schema(description = "文件URL")
    private String fileUrl;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "文件大小")
    private Long fileSize;

    @Schema(description = "内容类型")
    private String contentType;
}
