package com.promanage.service.service;

import java.util.List;
import java.util.Set;

import com.promanage.service.dto.request.AssignPermissionsRequest;
import com.promanage.service.dto.response.PermissionResponse;
import com.promanage.domain.entity.Role;

/**
 * 角色权限服务接口 (已重构以支持多租户)
 *
 * <p>提供角色权限分配和管理相关功能
 *
 * @author ProManage Team (Remediation)
 * @version 1.1
 * @since 2025-10-20
 */
public interface IRolePermissionService {
  /**
   * 为指定组织下的角色分配权限
   *
   * @param organizationId 组织ID
   * @param roleId 角色ID
   * @param request 权限分配请求
   */
  void assignPermissionsToRole(Long organizationId, Long roleId, AssignPermissionsRequest request);

  /**
   * 获取指定组织下角色的权限列表
   *
   * @param organizationId 组织ID
   * @param roleId 角色ID
   * @return 权限响应列表
   */
  List<PermissionResponse> getRolePermissions(Long organizationId, Long roleId);

  /**
   * 获取指定组织下角色拥有的权限ID列表
   *
   * @param organizationId 组织ID
   * @param roleId 角色ID
   * @return 权限ID列表
   */
  Set<Long> getRolePermissionIds(Long organizationId, Long roleId);

  /**
   * 获取指定组织下角色拥有的权限编码列表
   *
   * @param organizationId 组织ID
   * @param roleId 角色ID
   * @return 权限编码列表
   */
  List<String> getRolePermissionCodes(Long organizationId, Long roleId);

  /**
   * 获取指定组织下的角色列表及其权限信息
   *
   * @param organizationId 组织ID
   * @return 角色列表
   */
  List<Role> listRolesWithPermissions(Long organizationId);
}
