package com.promanage.api.dto.request;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建时间追踪请求DTO
 *
 * <p>用于创建时间追踪记录的请求数据传输对象
 *
 * @author ProManage Team
 * @since 2025-10-05
 */
@Data
@Schema(description = "创建时间追踪请求")
public class CreateTimeTrackingRequest {

  /** 任务ID */
  @NotNull(message = "任务ID不能为空")
  @Schema(description = "任务ID", example = "100", required = true)
  private Long taskId;

  /** 预估工时 */
  @Min(value = 0, message = "预估工时不能小于0")
  @Max(value = 999, message = "预估工时不能超过999")
  @Schema(description = "预估工时(小时)", example = "8.0")
  private Double estimatedHours;

  /** 工作日期 */
  @Schema(description = "工作日期", example = "2025-10-05")
  private LocalDate workDate;

  /** 工作描述 */
  @Schema(description = "工作描述", example = "完成了登录界面UI开发")
  private String workDescription;
}
