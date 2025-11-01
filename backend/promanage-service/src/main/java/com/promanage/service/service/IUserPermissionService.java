package com.promanage.service.service;

import java.util.List;
import java.util.Set;

import com.promanage.common.entity.User;
import com.promanage.service.dto.response.PermissionResponse;
import com.promanage.domain.entity.UserRole;

/**
 * 用户权限服务接口
 *
 * <p>提供用户权限查询和验证相关功能
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-16
 */
public interface IUserPermissionService {
  /**
   * 为用户分配角色
   *
   * @param userId 用户ID
   * @param roleIds 角色ID列表
   */
  void assignRolesToUser(Long userId, List<Long> roleIds);

  /**
   * 获取用户拥有的角色ID列表
   *
   * @param userId 用户ID
   * @return 角色ID列表
   */
  Set<Long> getUserRoleIds(Long userId);

  /**
   * 获取用户拥有的角色编码列表
   *
   * @param userId 用户ID
   * @return 角色编码列表
   */
  List<String> getUserRoleCodes(Long userId);

  /**
   * 获取用户最终有效权限列表
   *
   * @param userId 用户ID
   * @return 权限响应列表
   */
  List<PermissionResponse> getUserPermissions(Long userId);

  /**
   * 获取用户信息及其角色
   *
   * @param userId 用户ID
   * @return 用户信息
   */
  User getUserWithRoles(Long userId);

  /**
   * 获取用户角色列表
   *
   * @param userId 用户ID
   * @return 用户角色列表
   */
  List<UserRole> getUserRoles(Long userId);
}
