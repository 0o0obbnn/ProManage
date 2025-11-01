package com.promanage.service.impl;

import org.springframework.stereotype.Service;

import com.promanage.common.domain.ResultCode;
import com.promanage.common.entity.Organization;
import com.promanage.common.exception.BusinessException;
import com.promanage.service.mapper.OrganizationMapper;
import com.promanage.service.service.IOrganizationPermissionService;
import com.promanage.service.service.IPermissionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 组织权限服务实现类
 *
 * <p>提供组织相关的权限检查和管理功能
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationPermissionServiceImpl implements IOrganizationPermissionService {

  private final OrganizationMapper organizationMapper;
  private final IPermissionService permissionService;

  @Override
  public boolean isOrganizationAdmin(Long userId, Long organizationId) {
    log.debug("检查用户是否为组织管理员, userId={}, organizationId={}", userId, organizationId);

    // 超级管理员具有所有组织权限
    if (permissionService.isSuperAdmin(userId)) {
      return true;
    }

    // 检查用户是否为组织所有者
    Organization organization = organizationMapper.selectById(organizationId);
    if (organization != null && userId.equals(organization.getOwnerId())) {
      return true;
    }

    // 检查用户是否为组织管理员
    return permissionService.isOrganizationAdmin(userId, organizationId);
  }

  @Override
  public boolean isOrganizationMember(Long userId, Long organizationId) {
    log.debug("检查用户是否为组织成员, userId={}, organizationId={}", userId, organizationId);

    // 超级管理员自动是组织成员
    if (permissionService.isSuperAdmin(userId)) {
      return true;
    }

    // 检查用户是否为组织成员
    return permissionService.isOrganizationMember(userId, organizationId);
  }

  @Override
  public void ensureOrganizationReadable(Long organizationId, Long requesterId) {
    log.debug("确保用户可以访问组织, organizationId={}, requesterId={}", organizationId, requesterId);

    if (permissionService.isSuperAdmin(requesterId)) {
      return;
    }

    // 验证组织存在
    loadActiveOrganization(organizationId);
    
    boolean allowed =
        isOrganizationMember(requesterId, organizationId)
            || isOrganizationAdmin(requesterId, organizationId);

    if (!allowed) {
      throw new BusinessException(ResultCode.FORBIDDEN, "无权访问该组织");
    }
  }

  @Override
  public void ensureOrganizationWritable(Long organizationId, Long operatorId) {
    log.debug("确保用户可以修改组织, organizationId={}, operatorId={}", organizationId, operatorId);

    if (permissionService.isSuperAdmin(operatorId)) {
      return;
    }

    if (isOrganizationAdmin(operatorId, organizationId)) {
      return;
    }

    throw new BusinessException(ResultCode.FORBIDDEN, "无权修改该组织");
  }

  @Override
  public Organization getOrganizationWithPermissionCheck(Long organizationId, Long requesterId) {
    ensureOrganizationReadable(organizationId, requesterId);
    return loadActiveOrganization(organizationId);
  }

  /**
   * 加载激活的组织
   *
   * @param organizationId 组织ID
   * @return 组织实体
   */
  private Organization loadActiveOrganization(Long organizationId) {
    Organization organization = organizationMapper.selectById(organizationId);
    if (organization == null || organization.getDeleted()) {
      throw new BusinessException(ResultCode.DATA_NOT_FOUND, "组织不存在");
    }
    return organization;
  }
}
