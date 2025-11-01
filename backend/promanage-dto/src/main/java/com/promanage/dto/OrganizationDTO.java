package com.promanage.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 组织DTO
 *
 * <p>用于API响应的组织信息传输对象
 *
 * @author ProManage Team
 * @since 2025-10-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "组织信息")
public class OrganizationDTO {

  /** 组织ID */
  @Schema(description = "组织ID", example = "1")
  private Long id;

  /** 组织名称 */
  @Schema(description = "组织名称", example = "示例科技有限公司")
  private String name;

  /** 组织标识符（唯一） */
  @Schema(description = "组织标识符", example = "demo-org")
  private String slug;

  /** 组织描述 */
  @Schema(description = "组织描述", example = "一个示例组织的描述信息")
  private String description;

  /** 组织Logo URL */
  @Schema(description = "组织Logo URL", example = "https://example.com/logo.png")
  private String logoUrl;

  /** 组织网站URL */
  @Schema(description = "组织网站URL", example = "https://example.com")
  private String websiteUrl;

  /** 联系邮箱 */
  @Schema(description = "联系邮箱", example = "contact@example.com")
  private String contactEmail;

  /** 是否激活 */
  @Schema(description = "是否激活", example = "true")
  private Boolean isActive;

  /** 订阅计划 */
  @Schema(description = "订阅计划", example = "FREE")
  private String subscriptionPlan;

  /** 订阅过期时间 */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  @Schema(description = "订阅过期时间")
  private LocalDateTime subscriptionExpiresAt;

  /** 创建时间 */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  @Schema(description = "创建时间")
  private LocalDateTime createdAt;

  /** 更新时间 */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  @Schema(description = "更新时间")
  private LocalDateTime updatedAt;
}
