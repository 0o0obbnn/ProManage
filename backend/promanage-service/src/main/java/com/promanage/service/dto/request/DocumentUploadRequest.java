package com.promanage.service.dto.request;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** 文档上传请求 */
@Data
@Schema(description = "文档上传请求")
public class DocumentUploadRequest {

  @Schema(description = "上传文件", required = true)
  private MultipartFile file;

  @Schema(description = "项目ID", required = true)
  private Long projectId;

  @Schema(description = "文件夹ID")
  private Long folderId;

  @Schema(description = "文档标题")
  private String title;

  @Schema(description = "文档描述")
  private String description;
}
