package com.promanage.service.impl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.promanage.common.domain.ResultCode;
import com.promanage.common.exception.BusinessException;
import com.promanage.infrastructure.security.SecurityUtils;
import com.promanage.service.dto.request.AssignPermissionsRequest;
import com.promanage.service.dto.response.PermissionResponse;
import com.promanage.domain.entity.Role;
import com.promanage.domain.entity.RolePermission;
import com.promanage.domain.mapper.RoleMapper;
import com.promanage.domain.mapper.RolePermissionMapper;
import com.promanage.domain.event.RolePermissionChangedEvent;
import com.promanage.service.service.IPermissionManagementService;
import com.promanage.service.service.IPermissionService;
import com.promanage.service.service.IRolePermissionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

/**
 * 角色权限服务实现类 (已重构以支持多租户)
 *
 * <p>提供角色权限分配和管理相关功能
 *
 * @author ProManage Team (Remediation)
 * @version 1.1
 * @since 2025-10-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RolePermissionServiceImpl implements IRolePermissionService {

  private final RoleMapper roleMapper;
  private final RolePermissionMapper rolePermissionMapper;
  private final IPermissionManagementService permissionManagementService;
  private final IPermissionService permissionService; // Authorization checks
  private final ApplicationEventPublisher eventPublisher;

  // Helper method for authorization check
  private void checkAdminPermission(Long organizationId) {
    Long userId =
        SecurityUtils.getCurrentUserId()
            .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录"));
    if (!permissionService.isOrganizationAdmin(userId, organizationId)) {
      throw new BusinessException(ResultCode.FORBIDDEN, "您不是该组织的管理员，无权操作");
    }
  }

  private Role validateRoleExists(Long organizationId, Long roleId) {
    Role role = roleMapper.selectById(roleId);

    if (role == null || role.getDeleted()) {
      throw new BusinessException(ResultCode.DATA_NOT_FOUND, "指定组织下的角色不存在");
    }

    return role;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  @CacheEvict(
      value = {"roles", "permissions"},
      allEntries = true)
  public void assignPermissionsToRole(
      Long organizationId, Long roleId, AssignPermissionsRequest request) {
    log.info(
        "为角色分配权限, organizationId={}, roleId={}, permissionIds={}",
        organizationId,
        roleId,
        request.getPermissionIds());
    checkAdminPermission(organizationId);
    validateRoleExists(organizationId, roleId);

    if (CollectionUtils.isEmpty(request.getPermissionIds())) {
      // Allowing to remove all permissions
      rolePermissionMapper.deleteByRoleId(roleId);
      log.info("已移除角色的所有权限, roleId={}", roleId);
      return;
    }

    for (Long permissionId : request.getPermissionIds()) {
      permissionManagementService.validatePermissionExists(organizationId, permissionId);
    }

    rolePermissionMapper.deleteByRoleId(roleId);

    List<RolePermission> rolePermissions =
        request.getPermissionIds().stream()
            .map(
                permissionId -> {
                  RolePermission rolePermission = new RolePermission();
                  rolePermission.setRoleId(roleId);
                  rolePermission.setPermissionId(permissionId);
                  rolePermission.setCreateTime(LocalDateTime.now());
                  return rolePermission;
                })
            .collect(Collectors.toList());

    rolePermissionMapper.insertBatch(rolePermissions);

    // 发布角色权限变更事件
    eventPublisher.publishEvent(new RolePermissionChangedEvent(roleId));

    log.info("角色权限分配成功, roleId={}", roleId);
  }

  @Override
  @Cacheable(value = "permissions", key = "#organizationId + ':role:' + #roleId")
  public List<PermissionResponse> getRolePermissions(Long organizationId, Long roleId) {
    log.debug("获取角色权限列表, organizationId={}, roleId={}", organizationId, roleId);
    checkAdminPermission(organizationId);
    validateRoleExists(organizationId, roleId);

    Set<Long> permissionIds = getRolePermissionIds(organizationId, roleId);
    if (CollectionUtils.isEmpty(permissionIds)) {
      return Collections.emptyList();
    }

    return permissionIds.stream()
        .map(
            permissionId -> permissionManagementService.getPermission(organizationId, permissionId))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Override
  public Set<Long> getRolePermissionIds(Long organizationId, Long roleId) {
    // This is an internal method, authorization is handled by calling methods.
    if (roleId == null) {
      return Set.of();
    }
    List<Long> permissionIds = rolePermissionMapper.selectPermissionIdsByRoleId(roleId);
    return new HashSet<>(permissionIds);
  }

  @Override
  @Cacheable(value = "roles", key = "#organizationId + ':role_permission_codes:' + #roleId")
  public List<String> getRolePermissionCodes(Long organizationId, Long roleId) {
    log.debug("获取角色拥有的权限编码列表, organizationId={}, roleId={}", organizationId, roleId);
    // No explicit auth check here as this is often called internally by user permission checks.
    // The roleId is the primary key.
    return rolePermissionMapper.selectPermissionCodesByRoleId(roleId);
  }

  @Override
  @Cacheable(value = "roles", key = "#organizationId + ':list_with_permissions'")
  public List<Role> listRolesWithPermissions(Long organizationId) {
    log.debug("获取组织的角色列表及其权限信息, organizationId={}", organizationId);
    checkAdminPermission(organizationId);

    List<Role> roles = roleMapper.selectListByDeleted(false);

    // Note: The original code did not set permissions, so we return roles without permission details.
    // This can be enhanced later with a custom join query to avoid N+1 queries if needed.

    return roles;
  }
}
