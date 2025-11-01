package com.promanage.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.promanage.common.exception.BusinessException;
import com.promanage.common.result.PageResult;
import com.promanage.common.domain.ResultCode;
import com.promanage.dto.CreateProjectRequestDTO;
import com.promanage.dto.ProjectDTO;
import com.promanage.dto.ProjectMemberDTO;
import com.promanage.dto.ProjectStatsDTO;
import com.promanage.dto.UpdateProjectRequestDTO;
import com.promanage.service.IProjectActivityService;
import com.promanage.service.IProjectService;
import com.promanage.service.constant.ProjectConstants;
import com.promanage.service.entity.Project;
import com.promanage.service.mapper.ProjectDtoMapper;
import com.promanage.service.mapper.ProjectMapper;
import com.promanage.service.strategy.ProjectMemberStrategy;
import com.promanage.service.strategy.ProjectQueryStrategy;
import com.promanage.service.strategy.ProjectStatsStrategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 项目服务实现类 - 重构版本
 * 使用策略模式减少方法数量
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements IProjectService {

    private final ProjectMapper projectMapper;
    private final ProjectDtoMapper projectDtoMapper;
    private final IProjectActivityService projectActivityService;
    
    // 策略类
    private final ProjectQueryStrategy queryStrategy;
    private final ProjectMemberStrategy memberStrategy;
    private final ProjectStatsStrategy statsStrategy;

    // ==================== MyBatis Plus Required Methods ====================

    @Override
    public Class<Project> getEntityClass() {
        return Project.class;
    }

    @Override
    public ProjectMapper getBaseMapper() {
        return projectMapper;
    }

    // ==================== 核心CRUD方法 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Project createProject(CreateProjectRequestDTO request, Long creatorId) {
        validateCreateProjectRequest(request);
        
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStatus(1); // 1-进行中
        project.setOrganizationId(request.getOrganizationId());
        project.setCreatorId(creatorId);
        project.setCreateTime(LocalDateTime.now());
        project.setUpdateTime(LocalDateTime.now());
        
        projectMapper.insert(project);
        
        // 记录活动
        projectActivityService.recordActivity(project.getId(), creatorId, "PROJECT_CREATED",
                                            "项目创建");

        return project;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Project updateProject(Long projectId, UpdateProjectRequestDTO request, Long updaterId) {
        validateUpdateProjectRequest(request);

        Project project = queryStrategy.getProjectById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, ProjectConstants.ERROR_PROJECT_NOT_FOUND);
        }
        
        // 检查权限
        if (!memberStrategy.isProjectAdmin(project.getId(), updaterId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您无权修改此项目");
        }
        
        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
        
        project.setUpdaterId(updaterId);
        project.setUpdateTime(LocalDateTime.now());
        
        projectMapper.updateById(project);

        // 记录活动
        projectActivityService.recordActivity(project.getId(), updaterId, "PROJECT_UPDATED",
                                            "项目更新");

        return project;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProject(Long projectId, Long deleterId) {
        Project project = queryStrategy.getProjectById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, ProjectConstants.ERROR_PROJECT_NOT_FOUND);
        }
        
        // 检查权限
        if (!memberStrategy.isProjectAdmin(projectId, deleterId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您无权删除此项目");
        }
        
        projectMapper.deleteById(projectId);

        // 记录活动
        projectActivityService.recordActivity(projectId, deleterId, "PROJECT_DELETED",
                                            "项目删除");
    }

    @Override
    public Project getProjectById(Long projectId, Long requesterId) {
        Project project = queryStrategy.getProjectById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, ProjectConstants.ERROR_PROJECT_NOT_FOUND);
        }
        
        // 检查访问权限
        if (!memberStrategy.isMemberOrAdmin(projectId, requesterId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您无权访问此项目");
        }
        
        return project;
    }

    @Override
    public ProjectDTO getProjectDtoById(Long projectId, Long requesterId) {
        Project project = getProjectById(projectId, requesterId);
        return projectDtoMapper.toDto(project);
    }

    // ==================== 查询方法 ====================

    @Override
    public PageResult<ProjectDTO> listProjects(
        Long requesterId,
        Integer page,
        Integer pageSize,
        String keyword,
        Integer status,
        Long organizationId) {
        // Convert Integer status to String if needed by strategy
        String statusStr = status != null ? String.valueOf(status) : null;
        return queryStrategy.listProjects(
            page != null ? page : 1,
            pageSize != null ? pageSize : 20,
            keyword,
            statusStr,
            organizationId,
            requesterId);
    }

    @Override
    @Deprecated
    public PageResult<Project> listUserProjects(
        Long userId, Integer page, Integer pageSize, Integer status, String keyword) {
      // 将Integer状态转换为String (如果需要)
      String statusStr = status != null ? String.valueOf(status) : null;
      // 调用策略方法
      return queryStrategy.listUserProjects(
          userId,
          page != null ? page : 1,
          pageSize != null ? pageSize : 20,
          keyword,
          statusStr);
    }

    @Override
    @Deprecated
    public PageResult<Project> listUserProjects(
        Long userId, Integer page, Integer pageSize, Integer status) {
      // 调用5参数版本，keyword传null
      return listUserProjects(userId, page, pageSize, status, null);
    }

    @Override
    public List<Project> listByIds(List<Long> projectIds) {
        return queryStrategy.listByIds(projectIds);
    }

    @Override
    public List<Long> getUserAccessibleProjectIds(Long userId) {
        return queryStrategy.getUserAccessibleProjectIds(userId);
    }

    // ==================== 成员管理方法 ====================

    @Override
    public PageResult<ProjectMemberDTO> listProjectMembers(
        Long projectId,
        Long requesterId,
        Integer page,
        Integer pageSize,
        Long roleId) {
        // Convert Long roleId to String if needed by strategy
        String roleStr = roleId != null ? String.valueOf(roleId) : null;
        return memberStrategy.listProjectMembers(
            projectId,
            page != null ? page : 1,
            pageSize != null ? pageSize : 20,
            null, // keyword
            roleStr);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectMemberDTO addProjectMember(Long projectId, Long userId, Long roleId, Long operatorId) {
        // Convert Long roleId to String if needed by strategy
        String roleStr = roleId != null ? String.valueOf(roleId) : null;
        return memberStrategy.addProjectMember(projectId, userId, roleStr, operatorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeProjectMember(Long projectId, Long userId, Long operatorId) {
        memberStrategy.removeProjectMember(projectId, userId, operatorId);
    }

    @Override
    @Deprecated
    public List<ProjectMemberDTO> listMembersByRole(Long projectId, Long userId, Long roleId) {
      // 验证用户权限
      if (!memberStrategy.isMemberOrAdmin(projectId, userId)) {
        throw new BusinessException(ResultCode.FORBIDDEN, "您无权访问此项目");
      }

      // roleId为null时返回所有成员
      if (roleId == null) {
        return memberStrategy.listMembersByRole(projectId, null);
      }

      // 根据roleId查询角色名称（简化实现：假设roleId直接对应角色名）
      // 如果需要更复杂的逻辑，可以通过roleService查询
      return memberStrategy.listMembersByRole(projectId, String.valueOf(roleId));
    }

    // ==================== 权限检查方法 ====================

    @Override
    public boolean isProjectAdmin(Long projectId, Long userId) {
        return memberStrategy.isProjectAdmin(projectId, userId);
    }

    @Override
    public boolean isProjectMember(Long projectId, Long userId) {
        return memberStrategy.isProjectMember(projectId, userId);
    }

    @Override
    public boolean isMemberOrAdmin(Long projectId, Long userId) {
        return memberStrategy.isMemberOrAdmin(projectId, userId);
    }

    // ==================== 统计方法 ====================

    @Override
    public ProjectStatsDTO getProjectStats(Long projectId, Long requesterId) {
        // 检查访问权限
        if (!memberStrategy.isMemberOrAdmin(projectId, requesterId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您无权查看此项目统计");
        }
        
        return statsStrategy.getProjectStats(projectId);
    }

    // ==================== 归档方法 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void archive(Long projectId, Long operatorId) {
        updateProjectStatus(projectId, 3, operatorId); // 3-已归档
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unarchive(Long projectId, Long operatorId) {
        updateProjectStatus(projectId, 1, operatorId); // 1-进行中
    }

    // ==================== 私有辅助方法 ====================

    private void validateCreateProjectRequest(CreateProjectRequestDTO request) {
        if (request == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "项目信息不能为空");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "项目名称不能为空");
        }
    }

    private void validateUpdateProjectRequest(UpdateProjectRequestDTO request) {
        if (request == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "项目信息不能为空");
        }
        if (request.getId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, ProjectConstants.ERROR_PROJECT_ID_NULL);
        }
    }

    private void updateProjectStatus(Long projectId, Integer status, Long operatorId) {
        Project project = queryStrategy.getProjectById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, ProjectConstants.ERROR_PROJECT_NOT_FOUND);
        }

        // 检查权限
        if (!memberStrategy.isProjectAdmin(projectId, operatorId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您无权修改此项目状态");
        }

        project.setStatus(status);
        project.setUpdaterId(operatorId);
        project.setUpdateTime(LocalDateTime.now());

        try {
            projectMapper.updateById(project);
        } catch (DataAccessException e) {
            log.error("更新项目状态失败, projectId={}, status={}", projectId, status, e);
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "更新项目状态失败");
        }

        // 记录活动
        projectActivityService.recordActivity(projectId, operatorId, "PROJECT_STATUS_CHANGED",
                                            "项目状态变更为: " + status);
    }
}
