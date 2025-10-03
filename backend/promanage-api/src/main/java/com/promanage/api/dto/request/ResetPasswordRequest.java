package com.promanage.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 重置密码请求DTO
 * <p>
 * 用于忘记密码场景下通过邮箱验证码重置密码
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-09-30
 */
@Data
@Schema(description = "重置密码请求")
public class ResetPasswordRequest {

    /**
     * 电子邮箱
     * <p>
     * 必填项，用于接收验证码
     * </p>
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "电子邮箱", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    /**
     * 邮箱验证码
     * <p>
     * 必填项，6位数字验证码
     * </p>
     */
    @NotBlank(message = "验证码不能为空")
    @Size(min = 6, max = 6, message = "验证码必须为6位")
    @Schema(description = "邮箱验证码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String verificationCode;

    /**
     * 新密码
     * <p>
     * 必填项，长度限制为6-100个字符
     * </p>
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 100, message = "新密码长度必须在6-100个字符之间")
    @Schema(description = "新密码", example = "NewSecure@123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;

    /**
     * 确认新密码
     * <p>
     * 必填项，必须与新密码字段一致
     * </p>
     */
    @NotBlank(message = "确认密码不能为空")
    @Schema(description = "确认新密码", example = "NewSecure@123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String confirmPassword;
}