package com.promanage.api.controller;

import com.promanage.api.dto.request.UserCreateRequest;
import com.promanage.api.dto.request.UserUpdateRequest;
import com.promanage.api.dto.request.UserAssignRoleRequest;
import com.promanage.api.dto.request.UserUpdatePasswordRequest;
import com.promanage.api.dto.request.UserUpdateStatusRequest;
import com.promanage.api.dto.response.UserResponse;
import com.promanage.api.dto.response.RoleResponse;
import com.promanage.common.domain.PageResult;
import com.promanage.common.domain.Result;
import com.promanage.common.domain.ResultCode;
import com.promanage.common.exception.BusinessException;
import com.promanage.infrastructure.utils.SecurityUtils;
import com.promanage.service.entity.Role;
import com.promanage.service.entity.Permission;
import com.promanage.common.entity.User;
import com.promanage.service.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理控制器
 * <p>
 * 提供用户管理的API接口，包括用户的CRUD操作、角色管理等功能
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-08
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "用户管理", description = "用户信息的增删改查和角色管理")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    /**
     * 分页查询用户列表
     *
     * @param page 页码，从1开始
     * @param pageSize 每页大小
     * @param keyword 搜索关键词（可选，搜索用户名、邮箱、真实姓名）
     * @return 分页用户列表
     */
    @GetMapping
    @Operation(summary = "分页查询用户列表", description = "支持关键词搜索用户名、邮箱、真实姓名")
    @PreAuthorize("hasAuthority('user:list')")
    public Result<PageResult<UserResponse>> listUsers(
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(defaultValue = "1") Integer page,
            
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") Integer pageSize,
            
            @Parameter(description = "搜索关键词", example = "admin")
            @RequestParam(required = false) String keyword) {
        
        log.info("查询用户列表, page={}, pageSize={}, keyword={}", page, pageSize, keyword);
        
        PageResult<User> pageResult = userService.listUsers(page, pageSize, keyword);
        
        // 转换为响应DTO
        List<UserResponse> userResponses = pageResult.getList().stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
        
        PageResult<UserResponse> response = PageResult.<UserResponse>builder()
                .list(userResponses)
                .total(pageResult.getTotal())
                .page(page)
                .pageSize(pageSize)
                .build();
        
        return Result.success(response);
    }

    /**
     * 根据ID获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详细信息")
    @PreAuthorize("hasAuthority('user:view')")
    public Result<UserResponse> getUserById(
            @Parameter(description = "用户ID", example = "1")
            @PathVariable Long id) {
        
        log.info("获取用户详情, id={}", id);
        
        User user = userService.getById(id);
        UserResponse response = convertToUserResponse(user);
        
        return Result.success(response);
    }

    /**
     * 创建用户
     *
     * @param request 创建用户请求
     * @return 创建成功的用户信息
     */
    @PostMapping
    @Operation(summary = "创建用户", description = "创建新用户账号")
    @PreAuthorize("hasAuthority('user:create')")
    public Result<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        log.info("创建用户, username={}, email={}", request.getUsername(), request.getEmail());
        
        // 检查用户名是否已存在
        if (userService.existsByUsername(request.getUsername())) {
            throw new BusinessException(ResultCode.USERNAME_ALREADY_EXISTS);
        }
        
        // 检查邮箱是否已存在
        if (userService.existsByEmail(request.getEmail())) {
            throw new BusinessException(ResultCode.EMAIL_ALREADY_EXISTS);
        }
        
        // 构建用户实体
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword()); // 将在Service层加密
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRealName(request.getRealName());
        user.setOrganizationId(request.getOrganizationId());
        user.setDepartmentId(request.getDepartmentId());
        user.setPosition(request.getPosition());
        user.setStatus(1); // 默认启用
        
        // 创建用户
        Long userId = userService.create(user);
        
        // 如果指定了角色，分配角色
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            userService.assignRoles(userId, request.getRoleIds());
        }
        
        // 获取创建后的用户信息
        User createdUser = userService.getById(userId);
        UserResponse response = convertToUserResponse(createdUser);
        
        log.info("用户创建成功, userId={}", userId);
        return Result.success(response);
    }

    /**
     * 更新用户信息
     *
     * @param id 用户ID
     * @param request 更新用户请求
     * @return 更新后的用户信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新用户信息", description = "更新用户的基本信息（不包括密码）")
    @PreAuthorize("hasAuthority('user:update')")
    public Result<UserResponse> updateUser(
            @Parameter(description = "用户ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        
        log.info("更新用户信息, id={}", id);
        
        // 检查用户是否存在
        User existingUser = userService.getById(id);
        
        // 如果更新邮箱，检查邮箱是否已被其他用户使用
        if (request.getEmail() != null && !request.getEmail().equals(existingUser.getEmail())) {
            if (userService.existsByEmail(request.getEmail())) {
                throw new BusinessException(ResultCode.EMAIL_ALREADY_EXISTS);
            }
        }
        
        // 构建更新实体
        User updateUser = new User();
        updateUser.setEmail(request.getEmail());
        updateUser.setPhone(request.getPhone());
        updateUser.setRealName(request.getRealName());
        updateUser.setOrganizationId(request.getOrganizationId());
        updateUser.setDepartmentId(request.getDepartmentId());
        updateUser.setPosition(request.getPosition());
        updateUser.setAvatar(request.getAvatar());
        
        // 更新用户
        userService.update(id, updateUser);
        
        // 获取更新后的用户信息
        User updatedUser = userService.getById(id);
        UserResponse response = convertToUserResponse(updatedUser);
        
        log.info("用户信息更新成功, id={}", id);
        return Result.success(response);
    }

    /**
     * 删除用户（逻辑删除）
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", description = "逻辑删除用户（不是物理删除）")
    @PreAuthorize("hasAuthority('user:delete')")
    public Result<Void> deleteUser(
            @Parameter(description = "用户ID", example = "1")
            @PathVariable Long id) {
        
        log.info("删除用户, id={}", id);
        
        // 检查是否尝试删除自己
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED));
        
        if (currentUserId.equals(id)) {
            throw new BusinessException(ResultCode.CANNOT_DELETE_SELF);
        }
        
        // 删除用户
        userService.delete(id);
        
        log.info("用户删除成功, id={}", id);
        return Result.success();
    }

    /**
     * 批量删除用户（逻辑删除）
     *
     * @param ids 用户ID列表
     * @return 删除的记录数
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除用户", description = "批量逻辑删除用户")
    @PreAuthorize("hasAuthority('user:delete')")
    public Result<Integer> batchDeleteUsers(@RequestBody List<Long> ids) {
        log.info("批量删除用户, ids={}", ids);
        
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户ID列表不能为空");
        }
        
        // 检查是否尝试删除自己
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED));
        
        if (ids.contains(currentUserId)) {
            throw new BusinessException(ResultCode.CANNOT_DELETE_SELF);
        }
        
        // 批量删除用户
        int deletedCount = userService.batchDelete(ids);
        
        log.info("批量删除用户成功, count={}", deletedCount);
        return Result.success(deletedCount);
    }

    /**
     * 更新用户状态
     *
     * @param id 用户ID
     * @param request 更新状态请求
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "更新用户状态", description = "启用或禁用用户")
    @PreAuthorize("hasAuthority('user:update')")
    public Result<Void> updateUserStatus(
            @Parameter(description = "用户ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateStatusRequest request) {
        
        log.info("更新用户状态, id={}, status={}", id, request.getStatus());
        
        // 检查是否尝试禁用自己
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED));
        
        if (currentUserId.equals(id) && request.getStatus() != 1) {
            throw new BusinessException(ResultCode.CANNOT_DISABLE_SELF);
        }
        
        // 更新用户状态
        userService.updateStatus(id, request.getStatus());
        
        log.info("用户状态更新成功, id={}", id);
        return Result.success();
    }

    /**
     * 修改用户密码
     *
     * @param id 用户ID
     * @param request 修改密码请求
     * @return 操作结果
     */
    @PutMapping("/{id}/password")
    @Operation(summary = "修改用户密码", description = "管理员修改用户密码，无需验证旧密码")
    @PreAuthorize("hasAuthority('user:reset-password')")
    public Result<Void> updateUserPassword(
            @Parameter(description = "用户ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UserUpdatePasswordRequest request) {
        
        log.info("修改用户密码, id={}", id);
        
        // 重置密码
        userService.resetPassword(id, request.getNewPassword());
        
        log.info("用户密码修改成功, id={}", id);
        return Result.success();
    }

    /**
     * 为用户分配角色
     *
     * @param id 用户ID
     * @param request 分配角色请求
     * @return 操作结果
     */
    @PutMapping("/{id}/roles")
    @Operation(summary = "为用户分配角色", description = "替换用户的所有角色")
    @PreAuthorize("hasAuthority('user:assign-role')")
    public Result<Void> assignRoles(
            @Parameter(description = "用户ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UserAssignRoleRequest request) {
        
        log.info("为用户分配角色, id={}, roleIds={}", id, request.getRoleIds());
        
        // 分配角色
        userService.assignRoles(id, request.getRoleIds());
        
        log.info("用户角色分配成功, id={}", id);
        return Result.success();
    }

    /**
     * 为用户添加角色
     *
     * @param id 用户ID
     * @param roleId 角色ID
     * @return 操作结果
     */
    @PostMapping("/{id}/roles/{roleId}")
    @Operation(summary = "为用户添加角色", description = "为用户添加单个角色（不清空现有角色）")
    @PreAuthorize("hasAuthority('user:assign-role')")
    public Result<Void> addRole(
            @Parameter(description = "用户ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "角色ID", example = "1")
            @PathVariable Long roleId) {
        
        log.info("为用户添加角色, id={}, roleId={}", id, roleId);
        
        // 添加角色
        userService.addRole(id, roleId);
        
        log.info("用户角色添加成功, id={}", id);
        return Result.success();
    }

    /**
     * 移除用户角色
     *
     * @param id 用户ID
     * @param roleId 角色ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}/roles/{roleId}")
    @Operation(summary = "移除用户角色", description = "移除用户的单个角色")
    @PreAuthorize("hasAuthority('user:assign-role')")
    public Result<Void> removeRole(
            @Parameter(description = "用户ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "角色ID", example = "1")
            @PathVariable Long roleId) {
        
        log.info("移除用户角色, id={}, roleId={}", id, roleId);
        
        // 移除角色
        userService.removeRole(id, roleId);
        
        log.info("用户角色移除成功, id={}", id);
        return Result.success();
    }

    /**
     * 获取用户的所有角色
     *
     * @param id 用户ID
     * @return 用户角色列表
     */
    @GetMapping("/{id}/roles")
    @Operation(summary = "获取用户角色", description = "获取用户的所有角色")
    @PreAuthorize("hasAuthority('user:view')")
    public Result<List<RoleResponse>> getUserRoles(
            @Parameter(description = "用户ID", example = "1")
            @PathVariable Long id) {
        
        log.info("获取用户角色, id={}", id);
        
        // 获取用户角色
        List<Role> roles = userService.getUserRoles(id);
        
        // 转换为响应DTO
        List<RoleResponse> roleResponses = roles.stream()
                .map(this::convertToRoleResponse)
                .collect(Collectors.toList());
        
        return Result.success(roleResponses);
    }

    /**
     * 获取用户的所有权限
     *
     * @param id 用户ID
     * @return 用户权限列表
     */
    @GetMapping("/{id}/permissions")
    @Operation(summary = "获取用户权限", description = "获取用户的所有权限（通过角色汇总）")
    @PreAuthorize("hasAuthority('user:view')")
    public Result<List<String>> getUserPermissions(
            @Parameter(description = "用户ID", example = "1")
            @PathVariable Long id) {
        
        log.info("获取用户权限, id={}", id);
        
        // 获取用户权限
        List<Permission> permissions = userService.getUserPermissions(id);
        
        // 提取权限编码
        List<String> permissionCodes = permissions.stream()
                .map(Permission::getPermissionCode)
                .collect(Collectors.toList());
        
        return Result.success(permissionCodes);
    }

    /**
     * 转换User实体为UserResponse DTO
     *
     * @param user 用户实体
     * @return 用户响应DTO
     */
    private UserResponse convertToUserResponse(User user) {
        // 获取用户角色
        List<Role> roles = userService.getUserRoles(user.getId());
        List<RoleResponse> roleResponses = roles.stream()
                .map(this::convertToRoleResponse)
                .collect(Collectors.toList());

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .status(user.getStatus())
                .realName(user.getRealName())
                .organizationId(user.getOrganizationId())
                .departmentId(user.getDepartmentId())
                .position(user.getPosition())
                .lastLoginTime(user.getLastLoginTime())
                .lastLoginIp(user.getLastLoginIp())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .roles(roleResponses)
                .build();
    }

    /**
     * 转换Role实体为RoleResponse DTO
     *
     * @param role 角色实体
     * @return 角色响应DTO
     */
    private RoleResponse convertToRoleResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .roleCode(role.getRoleCode())
                .description(role.getDescription())
                .sort(role.getSort())
                .status(role.getStatus())
                .createTime(role.getCreateTime())
                .updateTime(role.getUpdateTime())
                .build();
    }
}
