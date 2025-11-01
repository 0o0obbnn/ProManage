package com.promanage.service.service;

import java.util.List;

import com.promanage.service.entity.Project;
import com.promanage.service.entity.ProjectMember;

/**
 * 项目权限服务接口
 *
 * <p>提供项目相关的权限检查和管理功能
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-16
 */
public interface IProjectPermissionService {

  /**
   * 检查用户是否为项目管理员
   *
   * @param userId 用户ID
   * @param projectId 项目ID
   * @return 是否为项目管理员
   */
  boolean isProjectAdmin(Long userId, Long projectId);

  /**
   * 检查用户是否为项目成员
   *
   * @param userId 用户ID
   * @param projectId 项目ID
   * @return 是否为项目成员
   */
  boolean isProjectMember(Long userId, Long projectId);

  /**
   * 检查用户是否为项目成员或管理员
   *
   * @param userId 用户ID
   * @param projectId 项目ID
   * @return 是否为项目成员或管理员
   */
  boolean isProjectMemberOrAdmin(Long userId, Long projectId);

  /**
   * 确保用户可以访问项目
   *
   * @param projectId 项目ID
   * @param requesterId 请求者ID
   */
  void ensureProjectReadable(Long projectId, Long requesterId);

  /**
   * 确保用户可以修改项目
   *
   * @param projectId 项目ID
   * @param operatorId 操作者ID
   */
  void ensureProjectWritable(Long projectId, Long operatorId);

  /**
   * 获取项目成员列表
   *
   * @param projectId 项目ID
   * @return 项目成员列表
   */
  List<ProjectMember> listProjectMembers(Long projectId);

  /**
   * 获取项目信息（带权限检查）
   *
   * @param projectId 项目ID
   * @param requesterId 请求者ID
   * @return 项目信息
   */
  Project getProjectWithPermissionCheck(Long projectId, Long requesterId);
}
