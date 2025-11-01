package com.promanage.api.dto.request;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建文档文件夹请求DTO
 *
 * @author ProManage Team
 * @date 2025-10-04
 */
@Data
@Schema(description = "创建文档文件夹请求")
public class CreateDocumentFolderRequest implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 文件夹名称 (不能为空) */
  @NotBlank(message = "文件夹名称不能为空")
  @Schema(description = "文件夹名称", example = "技术文档", required = true)
  private String name;

  /** 文件夹描述 */
  @Schema(description = "文件夹描述", example = "存放所有技术相关文档")
  private String description;

  /** 所属项目ID (不能为空) */
  @NotNull(message = "项目ID不能为空")
  @Schema(description = "所属项目ID", example = "1", required = true)
  private Long projectId;

  /**
   * 父文件夹ID
   *
   * <p>0表示根目录
   */
  @Schema(description = "父文件夹ID", example = "0", defaultValue = "0")
  private Long parentId;

  /** 排序 */
  @Schema(description = "排序", example = "1", defaultValue = "0")
  private Integer sortOrder;
}
