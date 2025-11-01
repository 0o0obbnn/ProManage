package com.promanage.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文档上传请求的数据传输对象 (DTO)
 *
 * <p>用于封装文件上传时附带的元数据。 注意：此DTO不包含文件本身，文件将作为MultipartFile处理。
 *
 * @author ProManage Team
 * @since 2025-10-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文档上传请求的元数据")
public class DocumentUploadRequest {

  @NotNull(message = "项目ID不能为空")
  @Schema(description = "文档所属的项目ID", required = true, example = "1")
  private Long projectId;

  @Schema(description = "文档所属的文件夹ID (可选, 如果不提供则上传至项目根目录)", example = "10")
  private Long folderId;

  @Size(max = 1000, message = "变更日志长度不能超过1000个字符")
  @Schema(description = "本次上传的变更日志 (例如: '修复了第三章的拼写错误')", example = "初始版本")
  private String changeLog;

  @Size(max = 500, message = "描述信息长度不能超过500个字符")
  @Schema(description = "对文档的简短描述 (可选)", example = "这是关于新功能的技术设计文档")
  private String description;
}
