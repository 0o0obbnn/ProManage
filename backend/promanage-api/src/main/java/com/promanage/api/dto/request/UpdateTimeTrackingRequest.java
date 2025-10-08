package com.promanage.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.time.LocalDate;

/**
 * 更新时间追踪请求DTO
 * <p>
 * 用于更新时间追踪记录的请求数据传输对象
 * </p>
 *
 * @author ProManage Team
 * @since 2025-10-05
 */
@Data
@Schema(description = "更新时间追踪请求")
public class UpdateTimeTrackingRequest {

    /**
     * 预估工时
     */
    @Min(value = 0, message = "预估工时不能小于0")
    @Max(value = 999, message = "预估工时不能超过999")
    @Schema(description = "预估工时(小时)", example = "8.0")
    private Double estimatedHours;

    /**
     * 实际工时
     */
    @Min(value = 0, message = "实际工时不能小于0")
    @Max(value = 999, message = "实际工时不能超过999")
    @Schema(description = "实际工时(小时)", example = "6.5")
    private Double actualHours;

    /**
     * 工作日期
     */
    @Schema(description = "工作日期", example = "2025-10-05")
    private LocalDate workDate;

    /**
     * 工作描述
     */
    @Schema(description = "工作描述", example = "完成了登录界面UI开发")
    private String workDescription;
}