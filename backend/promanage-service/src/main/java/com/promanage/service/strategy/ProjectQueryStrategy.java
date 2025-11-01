package com.promanage.service.strategy;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.promanage.common.result.PageResult;
import com.promanage.dto.ProjectDTO;
import com.promanage.service.entity.Project;
import com.promanage.service.mapper.ProjectDtoMapper;
import com.promanage.service.mapper.ProjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 项目查询策略
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
@Slf4j
@RequiredArgsConstructor
public class ProjectQueryStrategy {

    private final ProjectMapper projectMapper;
    private final ProjectDtoMapper projectDtoMapper;

    /**
     * 根据ID获取项目
     */
    public Project getProjectById(Long projectId) {
        return projectMapper.selectById(projectId);
    }

    /**
     * 根据ID获取项目DTO
     */
    public ProjectDTO getProjectDtoById(Long projectId) {
        return projectDtoMapper.selectById(projectId);
    }

    /**
     * 分页查询项目列表
     */
    public PageResult<ProjectDTO> listProjects(int page, int pageSize, String keyword, 
                                             String status, Long organizationId, Long userId) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                .like(Project::getName, keyword)
                .or()
                .like(Project::getDescription, keyword)
            );
        }
        
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(Project::getStatus, status);
        }
        
        if (organizationId != null) {
            wrapper.eq(Project::getOrganizationId, organizationId);
        }
        
        wrapper.orderByDesc(Project::getCreateTime);
        
        IPage<Project> pageResult = projectMapper.selectPage(new Page<>(page, pageSize), wrapper);
        
        // 转换为DTO
        List<ProjectDTO> dtoList = pageResult.getRecords().stream()
            .map(projectDtoMapper::toDto)
            .collect(java.util.stream.Collectors.toList());
        
        return PageResult.<ProjectDTO>builder()
            .list(dtoList)
            .total(pageResult.getTotal())
            .page(page)
            .pageSize(pageSize)
            .build();
    }

    /**
     * 获取用户项目列表
     */
    public PageResult<Project> listUserProjects(Long userId, int page, int pageSize, String keyword, String status) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        
        // 这里应该通过ProjectMember表关联查询用户有权限的项目
        // 简化实现，直接查询所有项目
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                .like(Project::getName, keyword)
                .or()
                .like(Project::getDescription, keyword)
            );
        }
        
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(Project::getStatus, status);
        }
        
        wrapper.orderByDesc(Project::getCreateTime);
        
        IPage<Project> pageResult = projectMapper.selectPage(new Page<>(page, pageSize), wrapper);
        
        return PageResult.<Project>builder()
            .list(pageResult.getRecords())
            .total(pageResult.getTotal())
            .page(page)
            .pageSize(pageSize)
            .build();
    }

    /**
     * 根据ID列表获取项目
     */
    public List<Project> listByIds(List<Long> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Project::getId, projectIds);
        return projectMapper.selectList(wrapper);
    }

    /**
     * 获取用户可访问的项目ID列表
     */
    public List<Long> getUserAccessibleProjectIds(Long userId) {
        // 这里应该通过ProjectMember表查询用户有权限的项目ID
        // 简化实现，返回空列表
        return java.util.Collections.emptyList();
    }

    /**
     * 检查项目是否存在
     */
    public boolean existsById(Long projectId) {
        return projectMapper.selectById(projectId) != null;
    }

    /**
     * 获取项目数量
     */
    public long getProjectCount(Long organizationId) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        if (organizationId != null) {
            wrapper.eq(Project::getOrganizationId, organizationId);
        }
        return projectMapper.selectCount(wrapper);
    }
}
