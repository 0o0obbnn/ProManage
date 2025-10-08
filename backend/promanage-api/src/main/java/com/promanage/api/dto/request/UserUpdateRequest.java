package com.promanage.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新用户请求DTO
 * <p>
 * 用于更新用户信息的请求参数，不包括密码
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-08
 */
@Data
@Schema(description = "更新用户请求")
public class UserUpdateRequest {

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱地址", example = "john@example.com")
    private String email;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @Schema(description = "组织ID", example = "1")
    private Long organizationId;

    @Schema(description = "部门ID", example = "1")
    private Long departmentId;

    @Schema(description = "职位", example = "软件工程师")
    private String position;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;
}