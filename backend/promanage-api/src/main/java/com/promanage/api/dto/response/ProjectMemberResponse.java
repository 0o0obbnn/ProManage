package com.promanage.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 项目成员响应DTO
 * <p>
 * 返回项目成员的信息和角色
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
@Schema(description = "项目成员响应")
public class ProjectMemberResponse {

    @Schema(description = "成员ID", example = "1")
    private Long id;

    @Schema(description = "项目ID", example = "1")
    private Long projectId;

    @Schema(description = "用户ID", example = "5")
    private Long userId;

    @Schema(description = "用户名", example = "developer01")
    private String username;

    @Schema(description = "真实姓名", example = "李开发")
    private String realName;

    @Schema(description = "用户头像", example = "https://example.com/avatar/user5.jpg")
    private String avatar;

    @Schema(description = "电子邮箱", example = "developer@example.com")
    private String email;

    @Schema(description = "角色ID", example = "3")
    private Long roleId;

    @Schema(description = "角色名称", example = "开发人员")
    private String roleName;

    @Schema(description = "角色编码", example = "DEVELOPER")
    private String roleCode;

    @Schema(description = "加入时间", example = "2025-01-10T09:00:00")
    private LocalDateTime joinedAt;

    @Schema(description = "成员状态：0-禁用，1-正常", example = "1")
    private Integer status;

    @Schema(description = "备注", example = "负责后端开发")
    private String remark;
}