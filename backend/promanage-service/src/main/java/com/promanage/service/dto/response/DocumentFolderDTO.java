package com.promanage.service.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** 文档文件夹DTO */
@Data
@Schema(description = "文档文件夹DTO")
public class DocumentFolderDTO {

  @Schema(description = "文件夹ID")
  private Long id;

  @Schema(description = "文件夹名称")
  private String name;

  @Schema(description = "文件夹描述")
  private String description;

  @Schema(description = "父文件夹ID")
  private Long parentId;

  @Schema(description = "所属项目ID")
  private Long projectId;

  @Schema(description = "文档数量")
  private Integer documentCount;

  @Schema(description = "子文件夹列表")
  private List<DocumentFolderDTO> children;
}
