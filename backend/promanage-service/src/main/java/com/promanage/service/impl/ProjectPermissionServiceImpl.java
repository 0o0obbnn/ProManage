package com.promanage.service.impl;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.promanage.common.domain.ResultCode;
import com.promanage.common.exception.BusinessException;
import com.promanage.service.entity.Project;
import com.promanage.service.entity.ProjectMember;
import com.promanage.service.mapper.ProjectMapper;
import com.promanage.service.mapper.ProjectMemberMapper;
import com.promanage.service.service.IPermissionService;
import com.promanage.service.service.IProjectPermissionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 项目权限服务实现类
 *
 * <p>提供项目相关的权限检查和管理功能
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectPermissionServiceImpl implements IProjectPermissionService {

  private static final Set<Long> PROJECT_ADMIN_ROLE_IDS = Set.of(1L);

  private final ProjectMapper projectMapper;
  private final ProjectMemberMapper projectMemberMapper;
  private final IPermissionService permissionService;

  @Override
  public boolean isProjectAdmin(Long userId, Long projectId) {
    log.debug("检查用户是否为项目管理员, userId={}, projectId={}", userId, projectId);

    // 超级管理员具有所有项目权限
    if (permissionService.isSuperAdmin(userId)) {
      return true;
    }

    // 检查是否为项目管理员角色
    ProjectMember projectMember = projectMemberMapper.findByProjectIdAndUserId(projectId, userId);
    List<ProjectMember> members = projectMember != null ? List.of(projectMember) : List.of();
    for (ProjectMember member : members) {
      if (PROJECT_ADMIN_ROLE_IDS.contains(member.getRoleId()) && member.getStatus() == 1) {
        return true;
      }
    }

    // 检查是否为项目所有者
    Project project = projectMapper.selectById(projectId);
    if (project != null && userId.equals(project.getOwnerId())) {
      return true;
    }

    return false;
  }

  @Override
  public boolean isProjectMember(Long userId, Long projectId) {
    log.debug("检查用户是否为项目成员, userId={}, projectId={}", userId, projectId);

    // 超级管理员自动是项目成员
    if (permissionService.isSuperAdmin(userId)) {
      return true;
    }

    // 检查是否为项目成员
    ProjectMember member = projectMemberMapper.selectByProjectIdAndUserId(projectId, userId);
    return member != null && member.getStatus() == 1;
  }

  @Override
  public boolean isProjectMemberOrAdmin(Long userId, Long projectId) {
    return isProjectMember(userId, projectId) || isProjectAdmin(userId, projectId);
  }

  @Override
  public void ensureProjectReadable(Long projectId, Long requesterId) {
    log.debug("确保用户可以访问项目, projectId={}, requesterId={}", projectId, requesterId);

    if (permissionService.isSuperAdmin(requesterId)) {
      return;
    }

    Project project = loadActiveProject(projectId);
    boolean allowed =
        isProjectMemberOrAdmin(requesterId, projectId)
            || (project.getOrganizationId() != null
                && permissionService.isOrganizationAdmin(requesterId, project.getOrganizationId()));

    if (!allowed) {
      throw new BusinessException(ResultCode.FORBIDDEN, "无权访问该项目");
    }
  }

  @Override
  public void ensureProjectWritable(Long projectId, Long operatorId) {
    log.debug("确保用户可以修改项目, projectId={}, operatorId={}", projectId, operatorId);

    if (permissionService.isSuperAdmin(operatorId)) {
      return;
    }

    Project project = loadActiveProject(projectId);
    if (isProjectAdmin(operatorId, projectId)) {
      return;
    }

    boolean allowed =
        permissionService.isOrganizationAdmin(operatorId, project.getOrganizationId());
    if (!allowed) {
      throw new BusinessException(ResultCode.FORBIDDEN, "无权修改该项目");
    }
  }

  @Override
  public List<ProjectMember> listProjectMembers(Long projectId) {
    log.debug("获取项目成员列表, projectId={}", projectId);

    return projectMemberMapper.selectByProjectId(projectId);
  }

  @Override
  public Project getProjectWithPermissionCheck(Long projectId, Long requesterId) {
    ensureProjectReadable(projectId, requesterId);
    return loadActiveProject(projectId);
  }

  /**
   * 加载激活的项目
   *
   * @param projectId 项目ID
   * @return 项目实体
   */
  private Project loadActiveProject(Long projectId) {
    Project project = projectMapper.selectById(projectId);
    if (project == null || project.getDeleted()) {
      throw new BusinessException(ResultCode.DATA_NOT_FOUND, "项目不存在");
    }
    return project;
  }
}
