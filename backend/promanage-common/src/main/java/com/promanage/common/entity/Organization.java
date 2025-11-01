package com.promanage.common.entity;

import java.time.LocalDateTime;

import org.hibernate.validator.constraints.URL;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 组织实体类
 *
 * <p>组织/租户信息实体，包含组织的基本信息和订阅状态
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("organizations")
@Schema(description = "组织信息")
public class Organization extends BaseEntity {

  /** 组织名称 */
  @NotBlank(message = "组织名称不能为空")
  @Size(min = 2, max = 50, message = "组织名称长度必须在2到50之间")
  @Schema(description = "组织名称", example = "示例科技有限公司")
  private String name;

  /** 组织标识符（唯一） */
  @NotBlank(message = "组织标识符不能为空")
  @Pattern(regexp = "^[a-z0-9-]+$", message = "组织标识符只能包含小写字母、数字和连字符")
  @Size(min = 3, max = 30, message = "组织标识符长度必须在3到30之间")
  @Schema(description = "组织标识符", example = "demo-org")
  private String slug;

  /** 组织描述 */
  @Size(max = 500, message = "组织描述不能超过500个字符")
  @Schema(description = "组织描述", example = "一个示例组织的描述信息")
  private String description;

  /** 组织Logo URL */
  @URL(message = "Logo URL格式不正确")
  @Schema(description = "组织Logo URL", example = "https://example.com/logo.png")
  private String logoUrl;

  /** 组织网站URL */
  @URL(message = "网站URL格式不正确")
  @Schema(description = "组织网站URL", example = "https://example.com")
  private String websiteUrl;

  /** 联系邮箱 */
  @Email(message = "联系邮箱格式不正确")
  @Schema(description = "联系邮箱", example = "contact@example.com")
  private String contactEmail;

  /** 组织设置（JSON格式） */
  @Schema(description = "组织设置")
  private String settings;

  /** 是否激活 */
  @Schema(description = "是否激活", example = "true")
  private Boolean isActive;

  /** 组织状态 */
  @Schema(description = "组织状态", example = "ACTIVE")
  private String status;

  /** 订阅计划 */
  @Schema(description = "订阅计划", example = "FREE")
  private String subscriptionPlan;

  /** 订阅过期时间 */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  @Schema(description = "订阅过期时间")
  private LocalDateTime subscriptionExpiresAt;

  /**
   * 获取组织所有者ID
   *
   * @return 组织所有者ID
   */
  public Long getOwnerId() {
    return this.getCreatorId();
  }
}
