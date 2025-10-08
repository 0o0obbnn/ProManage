package com.promanage.api.controller;

import com.promanage.service.dto.request.CreatePermissionRequest;
import com.promanage.service.dto.request.UpdatePermissionRequest;
import com.promanage.service.dto.request.AssignPermissionsRequest;
import com.promanage.service.dto.response.PermissionResponse;
import com.promanage.service.dto.response.PermissionTreeResponse;
import com.promanage.common.domain.Result;
import com.promanage.common.exception.BusinessException;
import com.promanage.infrastructure.utils.SecurityUtils;
import com.promanage.service.service.IPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限管理控制器
 * <p>
 * 提供权限的创建、查询、更新、删除以及权限分配管理功能
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-08
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/permissions")
@Tag(name = "权限管理", description = "权限创建、查询、更新、删除以及权限分配管理接口")
@RequiredArgsConstructor
public class PermissionController {

    private final IPermissionService permissionService;

    /**
     * 获取权限列表
     *
     * @param type 权限类型（可选）
     * @param status 权限状态（可选）
     * @param parentId 父级权限ID（可选）
     * @return 权限列表
     */
    @PreAuthorize("hasAuthority('permission:view')")
    @GetMapping
    @Operation(summary = "获取权限列表", description = "获取系统权限列表")
    public Result<List<PermissionResponse>> getPermissions(
            @Parameter(description = "权限类型") @RequestParam(required = false) String type,
            @Parameter(description = "权限状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "父级权限ID") @RequestParam(required = false) Long parentId) {

        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("获取权限列表请求, userId={}, type={}, status={}, parentId={}", userId, type, status, parentId);

        // 检查权限
        if (!permissionService.hasPermissionViewPermission(userId)) {
            throw new BusinessException("没有权限查看权限列表");
        }

        // 暂时获取所有权限，后续需要根据参数过滤
        List<PermissionResponse> permissions = permissionService.listAllPermissions();

        List<PermissionResponse> permissionResponses = permissions;

        log.info("获取权限列表成功, count={}", permissionResponses.size());
        return Result.success(permissionResponses);
    }

    /**
     * 获取权限树形结构
     *
     * @param type 权限类型（可选）
     * @return 权限树形结构
     */
    @PreAuthorize("hasAuthority('permission:view')")
    @GetMapping("/tree")
    @Operation(summary = "获取权限树", description = "获取权限的树形结构")
    public Result<List<PermissionTreeResponse>> getPermissionTree(
            @Parameter(description = "权限类型") @RequestParam(required = false) String type) {

        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("获取权限树请求, userId={}, type={}", userId, type);

        // 检查权限
        if (!permissionService.hasPermissionViewPermission(userId)) {
            throw new BusinessException("没有权限查看权限树");
        }

        List<PermissionTreeResponse> permissionTree = permissionService.getPermissionTree();

        log.info("获取权限树成功, count={}", permissionTree.size());
        return Result.success(permissionTree);
    }

    /**
     * 获取权限详情
     *
     * @param permissionId 权限ID
     * @return 权限详情
     */
    @PreAuthorize("hasAuthority('permission:view')")
    @GetMapping("/{permissionId}")
    @Operation(summary = "获取权限详情", description = "获取权限的详细信息")
    public Result<PermissionResponse> getPermission(@PathVariable Long permissionId) {
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("获取权限详情请求, permissionId={}, userId={}", permissionId, userId);

        // 检查权限
        if (!permissionService.hasPermissionViewPermission(userId)) {
            throw new BusinessException("没有权限查看权限详情");
        }

        PermissionResponse permission = permissionService.getPermissionById(permissionId);
        if (permission == null) {
            throw new BusinessException("权限不存在");
        }

        PermissionResponse response = permission;

        log.info("获取权限详情成功, permissionId={}", permissionId);
        return Result.success(response);
    }

    /**
     * 创建权限
     *
     * @param request 创建权限请求
     * @return 创建的权限信息
     */
    @PreAuthorize("hasAuthority('permission:create')")
    @PostMapping
    @Operation(summary = "创建权限", description = "创建新的权限")
    public Result<PermissionResponse> createPermission(@Valid @RequestBody CreatePermissionRequest request) {
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("创建权限请求, userId={}, permissionName={}, permissionCode={}", 
                userId, request.getPermissionName(), request.getPermissionCode());

        // 检查权限
        if (!permissionService.hasPermissionCreatePermission(userId)) {
            throw new BusinessException("没有权限创建权限");
        }

        Long permissionId = permissionService.createPermission(request);

        PermissionResponse createdPermission = permissionService.getPermissionById(permissionId);
        PermissionResponse response = createdPermission;

        log.info("权限创建成功, permissionId={}, permissionCode={}", permissionId, request.getPermissionCode());
        return Result.success(response);
    }

    /**
     * 更新权限
     *
     * @param permissionId 权限ID
     * @param request 更新权限请求
     * @return 更新后的权限信息
     */
    @PreAuthorize("hasAuthority('permission:edit')")
    @PutMapping("/{permissionId}")
    @Operation(summary = "更新权限", description = "更新权限信息")
    public Result<PermissionResponse> updatePermission(
            @PathVariable Long permissionId,
            @Valid @RequestBody UpdatePermissionRequest request) {

        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("更新权限请求, permissionId={}, userId={}", permissionId, userId);

        // 检查权限
        if (!permissionService.hasPermissionEditPermission(userId)) {
            throw new BusinessException("没有权限编辑权限");
        }

        PermissionResponse permission = permissionService.getPermissionById(permissionId);
        if (permission == null) {
            throw new BusinessException("权限不存在");
        }

        // 更新权限信息
        if (request.getPermissionName() != null) {
            permission.setPermissionName(request.getPermissionName());
        }
        if (request.getPermissionCode() != null) {
            permission.setPermissionCode(request.getPermissionCode());
        }
        if (request.getType() != null) {
            permission.setType(request.getType());
        }
        if (request.getUrl() != null) {
            permission.setUrl(request.getUrl());
        }
        if (request.getPath() != null) {
            permission.setPath(request.getPath());
        }
        if (request.getComponent() != null) {
            permission.setComponent(request.getComponent());
        }
        if (request.getMethod() != null) {
            permission.setMethod(request.getMethod());
        }
        if (request.getParentId() != null) {
            permission.setParentId(request.getParentId());
        }
        if (request.getSort() != null) {
            permission.setSort(request.getSort());
        }
        if (request.getIcon() != null) {
            permission.setIcon(request.getIcon());
        }
        if (request.getStatus() != null) {
            permission.setStatus(request.getStatus());
        }

        permissionService.updatePermission(permissionId, request);

        PermissionResponse updatedPermission = permissionService.getPermissionById(permissionId);
        PermissionResponse response = updatedPermission;

        log.info("权限更新成功, permissionId={}", permissionId);
        return Result.success(response);
    }

    /**
     * 删除权限
     *
     * @param permissionId 权限ID
     * @return 操作结果
     */
    @PreAuthorize("hasAuthority('permission:delete')")
    @DeleteMapping("/{permissionId}")
    @Operation(summary = "删除权限", description = "删除权限")
    public Result<Void> deletePermission(@PathVariable Long permissionId) {
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("删除权限请求, permissionId={}, userId={}", permissionId, userId);

        // 检查权限
        if (!permissionService.hasPermissionDeletePermission(userId)) {
            throw new BusinessException("没有权限删除权限");
        }

        permissionService.deletePermission(permissionId);

        log.info("权限删除成功, permissionId={}", permissionId);
        return Result.success();
    }

    /**
     * 获取角色权限
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    @PreAuthorize("hasAuthority('permission:view')")
    @GetMapping("/roles/{roleId}")
    @Operation(summary = "获取角色权限", description = "获取指定角色的权限列表")
    public Result<List<PermissionResponse>> getRolePermissions(@PathVariable Long roleId) {
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("获取角色权限请求, roleId={}, userId={}", roleId, userId);

        // 检查权限
        if (!permissionService.hasPermissionViewPermission(userId)) {
            throw new BusinessException("没有权限查看角色权限");
        }

        List<PermissionResponse> permissionResponses = permissionService.getRolePermissions(roleId);

        log.info("获取角色权限成功, roleId={}, count={}", roleId, permissionResponses.size());
        return Result.success(permissionResponses);
    }

    /**
     * 分配权限给角色
     *
     * @param request 分配权限请求
     * @return 操作结果
     */
    @PreAuthorize("hasAuthority('permission:assign')")
    @PostMapping("/roles/assign")
    @Operation(summary = "分配权限", description = "为角色分配权限")
    public Result<Void> assignPermissionsToRole(@Valid @RequestBody AssignPermissionsRequest request) {
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("分配权限请求, userId={}, roleId={}, permissionCount={}", 
                userId, request.getRoleId(), request.getPermissionIds().size());

        // 检查权限
        if (!permissionService.hasPermissionAssignPermission(userId)) {
            throw new BusinessException("没有权限分配权限");
        }

        permissionService.assignPermissionsToRole(request);

        log.info("权限分配成功, roleId={}, permissionCount={}", 
                request.getRoleId(), request.getPermissionIds().size());
        return Result.success();
    }

    /**
     * 获取用户权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @PreAuthorize("hasAuthority('permission:view')")
    @GetMapping("/users/{userId}")
    @Operation(summary = "获取用户权限", description = "获取指定用户的权限列表")
    public Result<List<PermissionResponse>> getUserPermissions(@PathVariable Long userId) {
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("获取用户权限请求, userId={}, currentUserId={}", userId, currentUserId);

        // 检查权限
        if (!permissionService.hasPermissionViewPermission(currentUserId)) {
            throw new BusinessException("没有权限查看用户权限");
        }

        List<PermissionResponse> permissionResponses = permissionService.getUserPermissions(userId);

        log.info("获取用户权限成功, userId={}, count={}", userId, permissionResponses.size());
        return Result.success(permissionResponses);
    }

    /**
     * 检查用户是否有指定权限
     *
     * @param userId 用户ID
     * @param permissionCode 权限编码
     * @return 检查结果
     */
    @PreAuthorize("hasAuthority('permission:view')")
    @GetMapping("/users/{userId}/check")
    @Operation(summary = "检查用户权限", description = "检查用户是否有指定权限")
    public Result<Boolean> checkUserPermission(
            @PathVariable Long userId,
            @Parameter(description = "权限编码") @RequestParam String permissionCode) {

        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("检查用户权限请求, userId={}, permissionCode={}, currentUserId={}", 
                userId, permissionCode, currentUserId);

        // 检查权限
        if (!currentUserId.equals(userId) && !permissionService.hasPermissionViewPermission(currentUserId)) {
            throw new BusinessException("没有权限检查其他用户权限");
        }

        boolean hasPermission = permissionService.checkUserPermission(userId, permissionCode);

        log.info("检查用户权限完成, userId={}, permissionCode={}, hasPermission={}", 
                userId, permissionCode, hasPermission);
        return Result.success(hasPermission);
    }

}
