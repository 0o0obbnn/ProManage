package com.promanage.api.dto.request;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "记录工时请求")
public class LogTimeRequest {

  @NotNull(message = "用户ID不能为空")
  @Schema(description = "用户ID", required = true)
  private Long userId;

  @NotNull(message = "花费时间不能为空")
  @Schema(description = "花费时间（小时）", required = true)
  private Double hoursSpent;

  @NotNull(message = "记录日期不能为空")
  @Schema(description = "记录日期", required = true)
  private LocalDateTime loggedDate;

  @Schema(description = "备注")
  private String notes;
}
