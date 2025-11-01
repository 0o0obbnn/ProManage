package com.promanage.service.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

import com.promanage.service.dto.request.CreatePermissionRequest;
import com.promanage.service.dto.request.UpdatePermissionRequest;
import com.promanage.service.dto.response.PermissionResponse;
import com.promanage.service.dto.response.PermissionTreeResponse;
import com.promanage.domain.entity.Permission;

/**
 * 权限管理服务接口 (已重构以支持多租户)
 *
 * <p>提供权限管理的核心业务逻辑，包括权限的CRUD操作和权限树形结构构建
 *
 * @author ProManage Team (Remediation)
 * @version 1.1
 * @since 2025-10-20
 */
public interface IPermissionManagementService extends IService<Permission> {
  /**
   * 在指定组织下创建权限
   *
   * @param organizationId 组织ID
   * @param request 权限创建请求
   * @return 权限ID
   */
  Long createPermission(Long organizationId, CreatePermissionRequest request);

  /**
   * 更新指定组织下的权限
   *
   * @param organizationId 组织ID
   * @param permissionId 权限ID
   * @param request 权限更新请求
   * @return 是否更新成功
   */
  Boolean updatePermission(Long organizationId, Long permissionId, UpdatePermissionRequest request);

  /**
   * 删除指定组织下的权限
   *
   * @param organizationId 组织ID
   * @param permissionId 权限ID
   * @return 是否删除成功
   */
  Boolean deletePermission(Long organizationId, Long permissionId);

  /**
   * 获取指定组织下的权限详情
   *
   * @param organizationId 组织ID
   * @param permissionId 权限ID
   * @return 权限信息
   */
  PermissionResponse getPermission(Long organizationId, Long permissionId);

  /**
   * 获取指定组织下的权限列表
   *
   * @param organizationId 组织ID
   * @return 权限列表
   */
  List<PermissionResponse> listPermissions(Long organizationId);

  /**
   * 获取指定组织下的权限树形结构
   *
   * @param organizationId 组织ID
   * @return 权限树形结构
   */
  List<PermissionTreeResponse> getPermissionTree(Long organizationId);

  /**
   * 验证权限编码在指定组织下的唯一性
   *
   * @param permissionCode 权限编码
   * @param organizationId 组织ID
   * @param permissionId 权限ID（更新时使用，可为null）
   */
  void validatePermissionCodeUnique(String permissionCode, Long organizationId, Long permissionId);

  /**
   * 验证指定组织下的权限是否存在
   *
   * @param organizationId 组织ID
   * @param permissionId 权限ID
   * @return 权限实体
   */
  Permission validatePermissionExists(Long organizationId, Long permissionId);
}
