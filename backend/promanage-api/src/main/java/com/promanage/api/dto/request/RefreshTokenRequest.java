package com.promanage.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 刷新令牌请求 DTO
 *
 * <p>用于封装客户端在请求新的访问令牌时发送的刷新令牌。
 *
 * @author ProManage Team
 * @since 2025-10-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "刷新令牌请求")
public class RefreshTokenRequest {

  @NotBlank(message = "刷新令牌不能为空")
  @Schema(
      description = "用于获取新访问令牌的刷新令牌。",
      required = true,
      example =
          "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTY3MjUyOTAwMCwiZXhwIjoxNjczMTMzODAwfQ.abc...")
  private String refreshToken;
}
