package com.promanage.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应DTO
 * <p>
 * 包含访问令牌、刷新令牌和用户基本信息
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-09-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginResponse {

    /**
     * JWT访问令牌
     * <p>
     * 用于API请求认证，应添加到请求头：Authorization: Bearer {token}
     * </p>
     */
    @Schema(description = "JWT访问令牌", example = "eyJhbGciOiJIUzUxMiJ9...")
    private String token;

    /**
     * 刷新令牌
     * <p>
     * 用于在访问令牌过期后获取新的访问令牌
     * </p>
     */
    @Schema(description = "刷新令牌", example = "eyJhbGciOiJIUzUxMiJ9...")
    private String refreshToken;

    /**
     * 令牌类型
     * <p>
     * 固定为"Bearer"
     * </p>
     */
    @Schema(description = "令牌类型", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * 访问令牌过期时间（秒）
     * <p>
     * 从当前时间开始计算的秒数
     * </p>
     */
    @Schema(description = "访问令牌过期时间（秒）", example = "86400")
    private Long expiresIn;

    /**
     * 用户基本信息
     */
    @Schema(description = "用户基本信息")
    private UserResponse userInfo;
}