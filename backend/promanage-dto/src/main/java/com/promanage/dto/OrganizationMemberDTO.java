package com.promanage.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 组织成员信息 DTO
 *
 * <p>对外暴露的成员信息，避免敏感字段泄露。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "组织成员信息")
public class OrganizationMemberDTO {

  @Schema(description = "用户ID", example = "1")
  private Long id;

  @Schema(description = "用户名", example = "li.yun")
  private String username;

  @Schema(description = "真实姓名", example = "李芸")
  private String realName;

  @Schema(description = "邮箱", example = "li.yun@example.com")
  private String email;

  @Schema(description = "职位", example = "后端工程师")
  private String position;

  @Schema(description = "账号状态: 0-禁用 1-启用 2-锁定", example = "1")
  private Integer status;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  @Schema(description = "最后登录时间")
  private LocalDateTime lastLoginTime;
}
