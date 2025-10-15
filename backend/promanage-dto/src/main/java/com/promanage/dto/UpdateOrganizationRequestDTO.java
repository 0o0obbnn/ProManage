package com.promanage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

/**
 * 更新组织请求DTO
 * <p>
 * 用于更新组织的请求参数
 * </p>
 *
 * @author ProManage Team
 * @since 2025-10-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新组织请求")
public class UpdateOrganizationRequestDTO {

    /**
     * 组织名称
     */
    @Size(min = 2, max = 50, message = "组织名称长度必须在2到50之间")
    @Schema(description = "组织名称", example = "示例科技有限公司")
    private String name;

    /**
     * 组织描述
     */
    @Size(max = 500, message = "组织描述不能超过500个字符")
    @Schema(description = "组织描述", example = "一个示例组织的描述信息")
    private String description;

    /**
     * 组织Logo URL
     */
    @URL(message = "Logo URL格式不正确")
    @Schema(description = "组织Logo URL", example = "https://example.com/logo.png")
    private String logoUrl;

    /**
     * 组织网站URL
     */
    @URL(message = "网站URL格式不正确")
    @Schema(description = "组织网站URL", example = "https://example.com")
    private String websiteUrl;

    /**
     * 联系邮箱
     */
    @Email(message = "联系邮箱格式不正确")
    @Schema(description = "联系邮箱", example = "contact@example.com")
    private String contactEmail;
}