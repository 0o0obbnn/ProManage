package com.promanage.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新用户状态请求DTO
 * <p>
 * 用于更新用户状态的请求参数
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-08
 */
@Data
@Schema(description = "更新用户状态请求")
public class UserUpdateStatusRequest {

    @NotNull(message = "状态值不能为空")
    @Min(value = 0, message = "状态值必须为0、1或2")
    @Max(value = 2, message = "状态值必须为0、1或2")
    @Schema(description = "用户状态：0-禁用，1-启用，2-锁定", example = "1", required = true)
    private Integer status;
}