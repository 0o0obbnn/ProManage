package com.promanage.api.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.promanage.common.domain.Result;
import com.promanage.service.dto.request.AssignPermissionsRequest;
import com.promanage.service.dto.request.CreatePermissionRequest;
import com.promanage.service.dto.request.UpdatePermissionRequest;
import com.promanage.service.dto.response.PermissionResponse;
import com.promanage.service.dto.response.PermissionTreeResponse;
import com.promanage.service.service.IPermissionManagementService;
import com.promanage.service.service.IRolePermissionService;
import com.promanage.service.service.IUserPermissionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 权限管理控制器 (已重构以支持多租户)
 *
 * <p>提供权限的创建、查询、更新、删除以及权限分配管理功能
 *
 * @author ProManage Team (Remediation)
 * @version 1.1
 * @since 2025-10-20
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@Tag(name = "权限管理", description = "组织级和系统级权限管理接口")
@RequiredArgsConstructor
public class PermissionController {

  private final IPermissionManagementService permissionManagementService;
  private final IRolePermissionService rolePermissionService;
  private final IUserPermissionService userPermissionService;

  // --- Organization-level Permissions ---

  @GetMapping("/organizations/{organizationId}/permissions")
  @Operation(summary = "获取组织权限列表", description = "获取指定组织的权限列表")
  @PreAuthorize("@permissionCheck.hasPermission('permission:view')")
  public Result<List<PermissionResponse>> getOrganizationPermissions(
      @PathVariable Long organizationId) {
    log.info("获取组织权限列表请求, organizationId={}", organizationId);
    List<PermissionResponse> permissions =
        permissionManagementService.listPermissions(organizationId);
    return Result.success(permissions);
  }

  @GetMapping("/organizations/{organizationId}/permissions/tree")
  @Operation(summary = "获取组织权限树", description = "获取指定组织的权限树形结构")
  @PreAuthorize("@permissionCheck.hasPermission('permission:view')")
  public Result<List<PermissionTreeResponse>> getOrganizationPermissionTree(
      @PathVariable Long organizationId) {
    log.info("获取组织权限树请求, organizationId={}", organizationId);
    List<PermissionTreeResponse> permissionTree =
        permissionManagementService.getPermissionTree(organizationId);
    return Result.success(permissionTree);
  }

  @GetMapping("/organizations/{organizationId}/permissions/{permissionId}")
  @Operation(summary = "获取组织权限详情", description = "获取指定组织的单个权限详细信息")
  @PreAuthorize("@permissionCheck.hasPermission('permission:view')")
  public Result<PermissionResponse> getOrganizationPermission(
      @PathVariable Long organizationId, @PathVariable Long permissionId) {
    log.info("获取组织权限详情请求, organizationId={}, permissionId={}", organizationId, permissionId);
    PermissionResponse permission =
        permissionManagementService.getPermission(organizationId, permissionId);
    return Result.success(permission);
  }

  @PostMapping("/organizations/{organizationId}/permissions")
  @Operation(summary = "创建组织权限", description = "在指定组织下创建新权限")
  @PreAuthorize("@permissionCheck.hasPermission('permission:create')")
  public Result<PermissionResponse> createOrganizationPermission(
      @PathVariable Long organizationId, @Valid @RequestBody CreatePermissionRequest request) {
    log.info(
        "创建组织权限请求, organizationId={}, permissionName={}",
        organizationId,
        request.getPermissionName());
    Long permissionId = permissionManagementService.createPermission(organizationId, request);
    PermissionResponse createdPermission =
        permissionManagementService.getPermission(organizationId, permissionId);
    return Result.success(createdPermission);
  }

  @PutMapping("/organizations/{organizationId}/permissions/{permissionId}")
  @Operation(summary = "更新组织权限", description = "更新指定组织下的权限信息")
  @PreAuthorize("@permissionCheck.hasPermission('permission:edit')")
  public Result<PermissionResponse> updateOrganizationPermission(
      @PathVariable Long organizationId,
      @PathVariable Long permissionId,
      @Valid @RequestBody UpdatePermissionRequest request) {
    log.info("更新组织权限请求, organizationId={}, permissionId={}", organizationId, permissionId);
    permissionManagementService.updatePermission(organizationId, permissionId, request);
    PermissionResponse updatedPermission =
        permissionManagementService.getPermission(organizationId, permissionId);
    return Result.success(updatedPermission);
  }

  @DeleteMapping("/organizations/{organizationId}/permissions/{permissionId}")
  @Operation(summary = "删除组织权限", description = "删除指定组织下的权限")
  @PreAuthorize("@permissionCheck.hasPermission('permission:delete')")
  public Result<Void> deleteOrganizationPermission(
      @PathVariable Long organizationId, @PathVariable Long permissionId) {
    log.info("删除组织权限请求, organizationId={}, permissionId={}", organizationId, permissionId);
    permissionManagementService.deletePermission(organizationId, permissionId);
    return Result.success();
  }

  // --- Role and User Permission Mappings (Still requires tenant context in service layer) ---

  @GetMapping("/organizations/{organizationId}/roles/{roleId}/permissions")
  @Operation(summary = "获取角色权限", description = "获取指定组织下某个角色的权限列表")
  @PreAuthorize("@permissionCheck.hasPermission('permission:view')")
  public Result<List<PermissionResponse>> getRolePermissions(
      @PathVariable Long organizationId, @PathVariable Long roleId) {
    log.info("获取角色权限请求, organizationId={}, roleId={}", organizationId, roleId);
    // This service method also needs to be refactored to be tenant-aware
    List<PermissionResponse> permissionResponses =
        rolePermissionService.getRolePermissions(organizationId, roleId);
    return Result.success(permissionResponses);
  }

  @PostMapping("/organizations/{organizationId}/roles/{roleId}/permissions")
  @Operation(summary = "分配权限给角色", description = "为指定组织下的角色分配权限")
  @PreAuthorize("@permissionCheck.hasPermission('permission:assign')")
  public Result<Void> assignPermissionsToRole(
      @PathVariable Long organizationId,
      @PathVariable Long roleId,
      @Valid @RequestBody AssignPermissionsRequest request) {
    log.info(
        "分配权限给角色请求, organizationId={}, roleId={}, permissionCount={}",
        organizationId,
        roleId,
        request.getPermissionIds().size());
    // This service method also needs to be refactored to be tenant-aware
    rolePermissionService.assignPermissionsToRole(organizationId, roleId, request);
    return Result.success();
  }

  @GetMapping("/users/{userId}/permissions")
  @Operation(summary = "获取用户权限", description = "获取指定用户的最终有效权限列表")
  @PreAuthorize(
      "@permissionCheck.hasPermission('permission:view') or #userId == authentication.principal.id")
  public Result<List<PermissionResponse>> getUserPermissions(@PathVariable Long userId) {
    log.info("获取用户权限请求, userId={}", userId);
    List<PermissionResponse> permissionResponses = userPermissionService.getUserPermissions(userId);
    return Result.success(permissionResponses);
  }
}
