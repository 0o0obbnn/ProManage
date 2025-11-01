package com.promanage.service.service;

import com.promanage.common.entity.Organization;

/**
 * 组织权限服务接口
 *
 * <p>提供组织相关的权限检查和管理功能
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-16
 */
public interface IOrganizationPermissionService {

  /**
   * 检查用户是否为组织管理员
   *
   * @param userId 用户ID
   * @param organizationId 组织ID
   * @return 是否为组织管理员
   */
  boolean isOrganizationAdmin(Long userId, Long organizationId);

  /**
   * 检查用户是否为组织成员
   *
   * @param userId 用户ID
   * @param organizationId 组织ID
   * @return 是否为组织成员
   */
  boolean isOrganizationMember(Long userId, Long organizationId);

  /**
   * 确保用户可以访问组织
   *
   * @param organizationId 组织ID
   * @param requesterId 请求者ID
   */
  void ensureOrganizationReadable(Long organizationId, Long requesterId);

  /**
   * 确保用户可以修改组织
   *
   * @param organizationId 组织ID
   * @param operatorId 操作者ID
   */
  void ensureOrganizationWritable(Long organizationId, Long operatorId);

  /**
   * 获取组织信息（带权限检查）
   *
   * @param organizationId 组织ID
   * @param requesterId 请求者ID
   * @return 组织信息
   */
  Organization getOrganizationWithPermissionCheck(Long organizationId, Long requesterId);
}
