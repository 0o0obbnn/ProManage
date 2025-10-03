package com.promanage.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户登录请求DTO
 * <p>
 * 用于用户通过用户名和密码进行身份验证登录
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-09-30
 */
@Data
@Schema(description = "登录请求")
public class LoginRequest {

    /**
     * 用户名
     * <p>
     * 必填项，长度限制为3-50个字符
     * </p>
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Schema(description = "用户名", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    /**
     * 密码
     * <p>
     * 必填项，长度限制为6-100个字符
     * 前端需对密码进行加密传输（建议使用RSA或AES加密）
     * </p>
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
    @Schema(description = "密码", example = "Admin@123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    /**
     * 记住我标志
     * <p>
     * 可选项，如果为true，则延长token过期时间
     * </p>
     */
    @Schema(description = "记住我", example = "false")
    private Boolean rememberMe;
}