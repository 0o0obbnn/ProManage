package com.promanage.api.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 用户响应DTO
 *
 * <p>返回用户基本信息，不包含敏感数据
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-09-30
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户响应")
public class UserResponse {

  @Schema(description = "用户ID", example = "1")
  private Long id;

  @Schema(description = "用户名", example = "admin")
  private String username;

  @Schema(description = "电子邮箱", example = "admin@example.com")
  private String email;

  @Schema(description = "手机号码", example = "13800138000")
  private String phone;

  @Schema(description = "真实姓名", example = "张三")
  private String realName;

  @Schema(description = "用户头像URL", example = "https://example.com/avatar/user1.jpg")
  private String avatar;

  @Schema(description = "用户状态：0-禁用，1-正常，2-锁定", example = "1")
  private Integer status;

  @Schema(description = "组织ID", example = "1")
  private Long organizationId;

  @Schema(description = "部门ID", example = "1")
  private Long departmentId;

  @Schema(description = "个人简介", example = "资深Java开发工程师")
  private String bio;

  @Schema(description = "所属部门", example = "研发部")
  private String department;

  @Schema(description = "职位", example = "高级开发工程师")
  private String position;

  @Schema(description = "最后登录时间", example = "2025-09-30T10:30:00")
  private LocalDateTime lastLoginTime;

  @Schema(description = "最后登录IP", example = "192.168.1.100")
  private String lastLoginIp;

  @Schema(description = "创建时间", example = "2025-01-01T00:00:00")
  private LocalDateTime createTime;

  @Schema(description = "更新时间", example = "2025-09-30T10:30:00")
  private LocalDateTime updateTime;

  @Schema(description = "用户角色列表")
  private List<RoleResponse> roles;
}
