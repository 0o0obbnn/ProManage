package com.promanage.service.strategy;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.promanage.common.exception.BusinessException;
import com.promanage.common.result.PageResult;
import com.promanage.common.domain.ResultCode;
import com.promanage.dto.ProjectMemberDTO;
import com.promanage.service.constant.ProjectConstants;
import com.promanage.service.entity.Project;
import com.promanage.service.entity.ProjectMember;
import com.promanage.domain.entity.Role;
import com.promanage.service.mapper.ProjectMemberMapper;
import com.promanage.service.mapper.ProjectMapper;
import com.promanage.service.service.IRoleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 项目成员管理策略
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
@Slf4j
@RequiredArgsConstructor
public class ProjectMemberStrategy {

    private final ProjectMemberMapper projectMemberMapper;
    private final ProjectMapper projectMapper;
    private final IRoleService roleService;

    /**
     * 分页查询项目成员
     */
    public PageResult<ProjectMemberDTO> listProjectMembers(Long projectId, int page, int pageSize, 
                                                         String keyword, String role) {
        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId);
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                .like(ProjectMember::getUsername, keyword)
                .or()
                .like(ProjectMember::getRealName, keyword)
            );
        }
        
        if (role != null && !role.trim().isEmpty()) {
            wrapper.eq(ProjectMember::getRole, role);
        }
        
        wrapper.orderByDesc(ProjectMember::getJoinTime);
        
        IPage<ProjectMember> pageResult = projectMemberMapper.selectPage(new Page<>(page, pageSize), wrapper);
        
        // 转换为DTO
        List<ProjectMemberDTO> dtoList = pageResult.getRecords().stream()
            .map(this::toProjectMemberDTO)
            .collect(java.util.stream.Collectors.toList());
        
        return PageResult.<ProjectMemberDTO>builder()
            .list(dtoList)
            .total(pageResult.getTotal())
            .page(page)
            .pageSize(pageSize)
            .build();
    }

    /**
     * 添加项目成员
     */
    public ProjectMemberDTO addProjectMember(Long projectId, Long userId, String role, Long operatorId) {
        // 验证项目存在
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, ProjectConstants.ERROR_PROJECT_NOT_FOUND);
        }
        
        // 验证角色存在
        Role roleEntity = roleService.getRoleByName(role);
        if (roleEntity == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "角色不存在");
        }
        
        // 检查成员是否已存在
        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId);
        wrapper.eq(ProjectMember::getUserId, userId);
        ProjectMember existingMember = projectMemberMapper.selectOne(wrapper);
        if (existingMember != null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "用户已是项目成员");
        }
        
        // 创建项目成员
        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(userId);
        member.setRole(role);
        member.setJoinTime(LocalDateTime.now());
        member.setCreatorId(operatorId);
        member.setCreateTime(LocalDateTime.now());
        
        projectMemberMapper.insert(member);
        
        return toProjectMemberDTO(member);
    }

    /**
     * 移除项目成员
     */
    public void removeProjectMember(Long projectId, Long userId, Long operatorId) {
        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId);
        wrapper.eq(ProjectMember::getUserId, userId);
        
        ProjectMember member = projectMemberMapper.selectOne(wrapper);
        if (member == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "项目成员不存在");
        }
        
        projectMemberMapper.delete(wrapper);
    }

    /**
     * 根据角色获取成员列表
     */
    public List<ProjectMemberDTO> listMembersByRole(Long projectId, String role) {
        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId);
        wrapper.eq(ProjectMember::getRole, role);
        wrapper.orderByDesc(ProjectMember::getJoinTime);
        
        List<ProjectMember> members = projectMemberMapper.selectList(wrapper);
        
        return members.stream()
            .map(this::toProjectMemberDTO)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 检查用户是否是项目管理员
     */
    public boolean isProjectAdmin(Long projectId, Long userId) {
        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId);
        wrapper.eq(ProjectMember::getUserId, userId);
        wrapper.eq(ProjectMember::getRole, "ADMIN");
        
        return projectMemberMapper.selectCount(wrapper) > 0;
    }

    /**
     * 检查用户是否是项目成员
     */
    public boolean isProjectMember(Long projectId, Long userId) {
        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId);
        wrapper.eq(ProjectMember::getUserId, userId);
        
        return projectMemberMapper.selectCount(wrapper) > 0;
    }

    /**
     * 检查用户是否是项目成员或管理员
     */
    public boolean isMemberOrAdmin(Long projectId, Long userId) {
        return isProjectMember(projectId, userId) || isProjectAdmin(projectId, userId);
    }

    /**
     * 获取项目成员数量
     */
    public long getMemberCount(Long projectId) {
        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId);
        return projectMemberMapper.selectCount(wrapper);
    }

    /**
     * 转换为ProjectMemberDTO
     */
    private ProjectMemberDTO toProjectMemberDTO(ProjectMember member) {
        ProjectMemberDTO dto = new ProjectMemberDTO();
        dto.setId(member.getId());
        dto.setProjectId(member.getProjectId());
        dto.setUserId(member.getUserId());
        dto.setUsername(member.getUsername());
        dto.setRealName(member.getRealName());
        dto.setRole(member.getRole());
        dto.setJoinTime(member.getJoinTime());
        dto.setCreateTime(member.getCreateTime());
        return dto;
    }
}
