package com.promanage.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

import com.promanage.common.result.PageResult;
import com.promanage.dto.CreateProjectRequestDTO;
import com.promanage.dto.ProjectDTO;
import com.promanage.dto.ProjectMemberDTO;
import com.promanage.dto.ProjectStatsDTO;
import com.promanage.dto.UpdateProjectRequestDTO;
import com.promanage.service.entity.Project;

/** 项目服务接口 提供项目管理、成员协作、统计分析以及权限控制等能力。 */
public interface IProjectService extends IService<Project> {

  PageResult<ProjectDTO> listProjects(
      Long requesterId,
      Integer page,
      Integer pageSize,
      String keyword,
      Integer status,
      Long organizationId);

  Project getProjectById(Long projectId, Long requesterId);

  /**
   * 获取项目详情DTO，包含项目负责人姓名
   *
   * @param projectId 项目ID
   * @param requesterId 请求者ID
   * @return 项目详情DTO
   */
  ProjectDTO getProjectDtoById(Long projectId, Long requesterId);

  Project createProject(CreateProjectRequestDTO request, Long creatorId);

  Project updateProject(Long projectId, UpdateProjectRequestDTO request, Long updaterId);

  void deleteProject(Long projectId, Long deleterId);

  ProjectStatsDTO getProjectStats(Long projectId, Long requesterId);

  PageResult<ProjectMemberDTO> listProjectMembers(
      Long projectId, Long requesterId, Integer page, Integer pageSize, Long roleId);

  ProjectMemberDTO addProjectMember(Long projectId, Long userId, Long roleId, Long operatorId);

  void removeProjectMember(Long projectId, Long userId, Long operatorId);

  void archive(Long projectId, Long operatorId);

  void unarchive(Long projectId, Long operatorId);

  boolean isProjectAdmin(Long projectId, Long userId);

  boolean isProjectMember(Long projectId, Long userId);

  /**
   * Check if user is a member of the project Alias for isProjectMember for convenience
   *
   * @param projectId Project ID
   * @param userId User ID
   * @return true if user is project member
   */
  default boolean isMember(Long projectId, Long userId) {
    return isProjectMember(projectId, userId);
  }

  boolean isMemberOrAdmin(Long projectId, Long userId);

  @Deprecated
  default boolean isAdmin(Long projectId, Long userId) {
    return isProjectAdmin(projectId, userId);
  }

  @Deprecated
  List<ProjectMemberDTO> listMembersByRole(Long projectId, Long userId, Long roleId);

  @Deprecated
  PageResult<Project> listUserProjects(
      Long userId, Integer page, Integer pageSize, Integer status, String keyword);

  @Deprecated
  PageResult<Project> listUserProjects(Long userId, Integer page, Integer pageSize, Integer status);

  /**
   * 根据项目ID列表批量查询项目
   *
   * @param projectIds 项目ID列表
   * @return 项目列表
   */
  List<Project> listByIds(List<Long> projectIds);

  /**
   * 获取用户有权访问的项目ID列表
   *
   * <p>用户可访问的项目包括: 1. 用户作为成员的项目 (project_members表中status=1且未删除的记录) 2. 超级管理员可访问所有项目 3.
   * 组织管理员可访问其组织下的所有项目
   *
   * @param userId 用户ID
   * @return 用户可访问的项目ID列表 (如果没有权限则返回空列表)
   */
  List<Long> getUserAccessibleProjectIds(Long userId);
}
