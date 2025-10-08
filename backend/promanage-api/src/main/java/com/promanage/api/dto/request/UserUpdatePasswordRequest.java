package com.promanage.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新用户密码请求DTO
 * <p>
 * 用于管理员修改用户密码的请求参数
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-08
 */
@Data
@Schema(description = "更新用户密码请求")
public class UserUpdatePasswordRequest {

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    @Schema(description = "新密码", example = "123456", required = true)
    private String newPassword;
}