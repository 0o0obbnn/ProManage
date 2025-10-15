package com.promanage.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 简化的用户信息,用于文档相关响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文档关联的用户信息")
public class DocumentUserSummary {

    @Schema(description = "用户ID", example = "8")
    private Long id;

    @Schema(description = "用户名", example = "dev_zhang")
    private String username;

    @Schema(description = "显示名称", example = "张开发")
    private String displayName;

    @Schema(description = "头像地址", example = "https://example.com/avatar/dev_zhang.png")
    private String avatar;
}
