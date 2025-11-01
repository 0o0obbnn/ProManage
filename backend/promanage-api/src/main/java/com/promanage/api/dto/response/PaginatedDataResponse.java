package com.promanage.api.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 与OpenAPI中的PaginatedResponse结构对齐的分页响应包装 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页数据响应")
public class PaginatedDataResponse<T> {

  @Schema(description = "分页数据列表")
  private List<T> data;

  @Schema(description = "总记录数", example = "100")
  private Long total;

  @Schema(description = "当前页码", example = "1")
  private Integer page;

  @Schema(description = "每页大小", example = "20")
  private Integer pageSize;

  @Schema(description = "总页数", example = "5")
  private Integer totalPages;

  @Schema(description = "是否存在下一页", example = "false")
  private Boolean hasNext;

  @Schema(description = "是否存在上一页", example = "false")
  private Boolean hasPrevious;
}
