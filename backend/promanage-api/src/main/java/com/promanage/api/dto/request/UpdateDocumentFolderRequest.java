package com.promanage.api.dto.request;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新文档文件夹请求DTO
 *
 * @author ProManage Team
 * @date 2025-10-04
 */
@Data
@Schema(description = "更新文档文件夹请求")
public class UpdateDocumentFolderRequest implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 文件夹名称 */
  @Schema(description = "文件夹名称", example = "技术文档")
  private String name;

  /** 文件夹描述 */
  @Schema(description = "文件夹描述", example = "存放所有技术相关文档")
  private String description;

  /** 所属项目ID */
  @Schema(description = "所属项目ID", example = "1")
  private Long projectId;

  /** 父文件夹ID */
  @Schema(description = "父文件夹ID", example = "0")
  private Long parentId;

  /** 排序 */
  @Schema(description = "排序", example = "1")
  private Integer sortOrder;
}
