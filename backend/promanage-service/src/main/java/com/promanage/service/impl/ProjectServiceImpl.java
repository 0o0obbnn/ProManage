package com.promanage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.promanage.common.domain.ResultCode;
import com.promanage.common.entity.User;
import com.promanage.common.exception.BusinessException;
import com.promanage.common.result.PageResult;
import com.promanage.dto.CreateProjectRequestDTO;
import com.promanage.dto.ProjectDTO;
import com.promanage.dto.ProjectMemberDTO;
import com.promanage.dto.ProjectStatsDTO;
import com.promanage.dto.UpdateProjectRequestDTO;
import com.promanage.infrastructure.security.SecurityUtils;
import com.promanage.service.IProjectActivityService;
import com.promanage.service.IProjectService;
import com.promanage.service.entity.Project;
import com.promanage.service.entity.ProjectMember;
import com.promanage.service.entity.Role;
import com.promanage.service.mapper.ChangeRequestMapper;
import com.promanage.service.mapper.DocumentMapper;
import com.promanage.service.mapper.ProjectDtoMapper;
import com.promanage.service.mapper.ProjectMapper;
import com.promanage.service.mapper.ProjectMemberMapper;
import com.promanage.service.mapper.TaskMapper;
import com.promanage.service.service.IPermissionService;
import com.promanage.service.service.IRoleService;
import com.promanage.service.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements IProjectService {

    private static final Set<Long> PROJECT_ADMIN_ROLE_IDS = Set.of(1L);

    private final ProjectMapper projectMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final IUserService userService;
    private final IRoleService roleService;
    private final IPermissionService permissionService;
    private final ProjectDtoMapper projectDtoMapper;
    private final TaskMapper taskMapper;
    private final DocumentMapper documentMapper;
    private final ChangeRequestMapper changeRequestMapper;
    private final IProjectActivityService projectActivityService;

    private Project loadActiveProject(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null || project.getDeleted()) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "项目不存在");
        }
        return project;
    }

    private User loadActiveUser(Long userId) {
        User user = userService.getById(userId);
        if (user == null || user.getDeleted()) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private void ensureProjectReadable(Long projectId, Long requesterId) {
        if (permissionService.isSuperAdmin(requesterId)) {
            return;
        }
        Project project = loadActiveProject(projectId);
        boolean allowed = permissionService.isProjectMember(requesterId, projectId)
                || permissionService.isProjectAdmin(requesterId, projectId)
                || (project.getOrganizationId() != null && permissionService.isOrganizationAdmin(requesterId, project.getOrganizationId()));
        if (!allowed) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权访问该项目");
        }
    }

    private void ensureProjectWritable(Long projectId, Long operatorId) {
        if (permissionService.isSuperAdmin(operatorId)) {
            return;
        }
        Project project = loadActiveProject(projectId);
        if (permissionService.isProjectAdmin(operatorId, projectId)) {
            return;
        }
        if (project.getOrganizationId() != null && permissionService.isOrganizationAdmin(operatorId, project.getOrganizationId())) {
            return;
        }
        throw new BusinessException(ResultCode.FORBIDDEN, "无权操作该项目");
    }

    private Long resolveOrganizationId(Long requestedOrganizationId, Long userId) {
        if (permissionService.isSuperAdmin(userId)) {
            if (requestedOrganizationId == null) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "超级管理员创建项目时必须指定组织");
            }
            return requestedOrganizationId;
        }
        User user = loadActiveUser(userId);
        if (user.getOrganizationId() == null) {
            throw new BusinessException(ResultCode.FORBIDDEN, "当前用户未关联组织，无法创建项目");
        }
        if (requestedOrganizationId != null && !Objects.equals(requestedOrganizationId, user.getOrganizationId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权为其他组织创建项目");
        }
        return user.getOrganizationId();
    }

    private void ensureProjectCodeUnique(String code, Long organizationId, Long ignoreProjectId) {
        if (StringUtils.isBlank(code)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "项目编码不能为空");
        }
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Project::getCode, code)
               .isNull(Project::getDeletedAt);
        if (organizationId != null) {
            wrapper.eq(Project::getOrganizationId, organizationId);
        }
        if (ignoreProjectId != null) {
            wrapper.ne(Project::getId, ignoreProjectId);
        }
        if (projectMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "项目编码已存在");
        }
    }

    private void validateDates(LocalDate start, LocalDate end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "结束日期不能早于开始日期");
        }
    }

    private ProjectMemberDTO toMemberDto(ProjectMember member) {
        ProjectMemberDTO dto = new ProjectMemberDTO();
        dto.setId(member.getId());
        dto.setProjectId(member.getProjectId());
        dto.setUserId(member.getUserId());
        dto.setRoleId(member.getRoleId());
        dto.setJoinTime(member.getJoinTime());
        dto.setStatus(member.getStatus());
        User user = userService.getById(member.getUserId());
        if (user != null) {
            dto.setUsername(user.getUsername());
            dto.setRealName(user.getRealName());
            dto.setEmail(user.getEmail());
            dto.setAvatar(user.getAvatar());
        }
        Role role = roleService.getById(member.getRoleId());
        if (role != null) {
            dto.setRoleName(role.getRoleName());
            dto.setRoleCode(role.getRoleCode());
        }
        return dto;
    }

    private void ensureOwnerMembership(Project project, Long operatorId) {
        if (project.getOwnerId() == null) {
            return;
        }
        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, project.getId())
               .eq(ProjectMember::getUserId, project.getOwnerId())
               .isNull(ProjectMember::getDeletedAt);
        if (projectMemberMapper.selectCount(wrapper) == 0) {
            ProjectMember ownerMember = new ProjectMember();
            ownerMember.setProjectId(project.getId());
            ownerMember.setUserId(project.getOwnerId());
            ownerMember.setRoleId(PROJECT_ADMIN_ROLE_IDS.iterator().next());
            ownerMember.setJoinTime(LocalDateTime.now());
            ownerMember.setStatus(1);
            ownerMember.setCreatorId(operatorId);
            ownerMember.setUpdaterId(operatorId);
            projectMemberMapper.insert(ownerMember);
        }
    }

    private ProjectDTO enrichDto(Project project) {
        ProjectDTO dto = projectDtoMapper.toDto(project);
        if (project.getOwnerId() != null) {
            User owner = userService.getById(project.getOwnerId());
            if (owner != null) {
                dto.setOwnerName(owner.getRealName());
            }
        }
        return dto;
    }

    @Override
    public PageResult<ProjectDTO> listProjects(Long requesterId, Integer page, Integer pageSize, String keyword, Integer status, Long organizationId) {
        int currentPage = page == null || page < 1 ? 1 : page;
        int size = pageSize == null || pageSize < 1 ? 20 : pageSize;

        Long scopedOrganizationId = resolveOrganizationId(organizationId, requesterId);

        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Project::getDeletedAt)
               .eq(Project::getOrganizationId, scopedOrganizationId);

        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Project::getName, keyword)
                    .or().like(Project::getCode, keyword));
        }
        if (status != null) {
            wrapper.eq(Project::getStatus, status);
        }
        wrapper.orderByDesc(Project::getUpdateTime);

        IPage<Project> pageResult = projectMapper.selectPage(new Page<>(currentPage, size), wrapper);
        List<ProjectDTO> dtoList = pageResult.getRecords().stream()
                .map(this::enrichDto)
                .collect(Collectors.toList());
        return PageResult.of(dtoList, pageResult.getTotal(), currentPage, size);
    }

    @Override
    public Project getProjectById(Long projectId, Long requesterId) {
        ensureProjectReadable(projectId, requesterId);
        return loadActiveProject(projectId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Project createProject(CreateProjectRequestDTO request, Long creatorId) {
        Long operatorId = Optional.ofNullable(creatorId)
                .orElseGet(() -> SecurityUtils.getCurrentUserId().orElse(null));
        if (operatorId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }

        Long organizationId = resolveOrganizationId(request.getOrganizationId(), operatorId);
        ensureProjectCodeUnique(request.getCode(), organizationId, null);
        validateDates(request.getStartDate(), request.getEndDate());

        Project project = projectDtoMapper.toEntity(request);
        project.setOrganizationId(organizationId);
        project.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        project.setCreatorId(operatorId);
        project.setUpdaterId(operatorId);
        project.setCreateTime(LocalDateTime.now());
        project.setUpdateTime(LocalDateTime.now());
        project.setDeletedAt(null);
        projectMapper.insert(project);

        if (project.getOwnerId() != null) {
            ensureOwnerMembership(project, operatorId);
        }

        projectActivityService.recordActivity(project.getId(), operatorId, "PROJECT_CREATED", "创建项目");
        return project;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Project updateProject(Long projectId, UpdateProjectRequestDTO request, Long updaterId) {
        Long operatorId = Optional.ofNullable(updaterId)
                .orElseGet(() -> SecurityUtils.getCurrentUserId().orElse(null));
        if (operatorId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }

        ensureProjectWritable(projectId, operatorId);
        Project existing = loadActiveProject(projectId);

        if (StringUtils.isNotBlank(request.getCode()) && !Objects.equals(request.getCode(), existing.getCode())) {
            ensureProjectCodeUnique(request.getCode(), existing.getOrganizationId(), projectId);
        }
        validateDates(request.getStartDate(), request.getEndDate());

        projectDtoMapper.updateEntity(request, existing);
        if (request.getOrganizationId() != null && !Objects.equals(request.getOrganizationId(), existing.getOrganizationId())) {
            Long scopedOrganizationId = resolveOrganizationId(request.getOrganizationId(), operatorId);
            existing.setOrganizationId(scopedOrganizationId);
        }
        existing.setUpdaterId(operatorId);
        existing.setUpdateTime(LocalDateTime.now());
        projectMapper.updateById(existing);

        ensureOwnerMembership(existing, operatorId);
        projectActivityService.recordActivity(projectId, operatorId, "PROJECT_UPDATED", "更新项目信息");
        return existing;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProject(Long projectId, Long deleterId) {
        Long operatorId = Optional.ofNullable(deleterId)
                .orElseGet(() -> SecurityUtils.getCurrentUserId().orElse(null));
        if (operatorId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }
        ensureProjectWritable(projectId, operatorId);
        Project project = loadActiveProject(projectId);
        project.setDeletedAt(LocalDateTime.now());
        project.setDeletedBy(operatorId);
        project.setUpdaterId(operatorId);
        projectMapper.updateById(project);
        projectActivityService.recordActivity(projectId, operatorId, "PROJECT_DELETED", "删除项目");
    }

    @Override
    public ProjectStatsDTO getProjectStats(Long projectId, Long requesterId) {
        ensureProjectReadable(projectId, requesterId);
        loadActiveProject(projectId);

        long taskCount = taskMapper.selectCount(new LambdaQueryWrapper<com.promanage.service.entity.Task>()
                .eq(com.promanage.service.entity.Task::getProjectId, projectId)
                .isNull(com.promanage.service.entity.Task::getDeletedAt));
        long documentCount = documentMapper.selectCount(new LambdaQueryWrapper<com.promanage.service.entity.Document>()
                .eq(com.promanage.service.entity.Document::getProjectId, projectId)
                .isNull(com.promanage.service.entity.Document::getDeletedAt));
        long memberCount = projectMemberMapper.selectCount(new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectId)
                .isNull(ProjectMember::getDeletedAt));
        long changeRequestCount = changeRequestMapper.selectCount(new LambdaQueryWrapper<com.promanage.service.entity.ChangeRequest>()
                .eq(com.promanage.service.entity.ChangeRequest::getProjectId, projectId)
                .isNull(com.promanage.service.entity.ChangeRequest::getDeletedAt));

        long completedTasks = taskMapper.selectCount(new LambdaQueryWrapper<com.promanage.service.entity.Task>()
                .eq(com.promanage.service.entity.Task::getProjectId, projectId)
                .eq(com.promanage.service.entity.Task::getStatus, 3)
                .isNull(com.promanage.service.entity.Task::getDeletedAt));
        long inProgressTasks = taskMapper.selectCount(new LambdaQueryWrapper<com.promanage.service.entity.Task>()
                .eq(com.promanage.service.entity.Task::getProjectId, projectId)
                .eq(com.promanage.service.entity.Task::getStatus, 2)
                .isNull(com.promanage.service.entity.Task::getDeletedAt));
        long pendingTasks = taskMapper.selectCount(new LambdaQueryWrapper<com.promanage.service.entity.Task>()
                .eq(com.promanage.service.entity.Task::getProjectId, projectId)
                .eq(com.promanage.service.entity.Task::getStatus, 1)
                .isNull(com.promanage.service.entity.Task::getDeletedAt));

        ProjectStatsDTO stats = new ProjectStatsDTO();
        stats.setProjectId(projectId);
        stats.setTotalTasks((int) taskCount);
        stats.setCompletedTasks((int) completedTasks);
        stats.setInProgressTasks((int) inProgressTasks);
        stats.setPendingTasks((int) pendingTasks);
        stats.setMemberCount((int) memberCount);
        stats.setTotalDocuments((int) documentCount);
        stats.setChangeRequests((int) changeRequestCount);
        stats.setProgressPercentage(taskCount > 0 ? (double) completedTasks / taskCount * 100 : 0.0);
        return stats;
    }

    @Override
    public PageResult<ProjectMemberDTO> listProjectMembers(Long projectId, Long requesterId, Integer page, Integer pageSize, Long roleId) {
        ensureProjectReadable(projectId, requesterId);
        int currentPage = page == null || page < 1 ? 1 : page;
        int size = pageSize == null || pageSize < 1 ? 20 : pageSize;

        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId)
               .isNull(ProjectMember::getDeletedAt);
        if (roleId != null) {
            wrapper.eq(ProjectMember::getRoleId, roleId);
        }
        wrapper.orderByAsc(ProjectMember::getJoinTime);

        IPage<ProjectMember> memberPage = projectMemberMapper.selectPage(new Page<>(currentPage, size), wrapper);
        List<ProjectMemberDTO> dtoList = memberPage.getRecords().stream()
                .map(this::toMemberDto)
                .collect(Collectors.toList());
        return PageResult.of(dtoList, memberPage.getTotal(), currentPage, size);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectMemberDTO addProjectMember(Long projectId, Long userId, Long roleId, Long operatorId) {
        Long actorId = Optional.ofNullable(operatorId)
                .orElseGet(() -> SecurityUtils.getCurrentUserId().orElse(null));
        if (actorId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }
        if (roleId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "角色ID不能为空");
        }
        ensureProjectWritable(projectId, actorId);

        Project project = loadActiveProject(projectId);
        User targetUser = loadActiveUser(userId);
        if (project.getOrganizationId() != null && !Objects.equals(project.getOrganizationId(), targetUser.getOrganizationId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "用户不属于该项目所在组织");
        }

        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId)
               .eq(ProjectMember::getUserId, userId)
               .isNull(ProjectMember::getDeletedAt);
        if (projectMemberMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户已经是项目成员");
        }

        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(userId);
        member.setRoleId(roleId);
        member.setJoinTime(LocalDateTime.now());
        member.setStatus(1);
        member.setCreatorId(actorId);
        member.setUpdaterId(actorId);
        projectMemberMapper.insert(member);

        projectActivityService.recordActivity(projectId, actorId, "MEMBER_ADDED", "添加项目成员");
        return toMemberDto(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeProjectMember(Long projectId, Long userId, Long operatorId) {
        Long actorId = Optional.ofNullable(operatorId)
                .orElseGet(() -> SecurityUtils.getCurrentUserId().orElse(null));
        if (actorId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }
        ensureProjectWritable(projectId, actorId);

        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId)
               .eq(ProjectMember::getUserId, userId)
               .isNull(ProjectMember::getDeletedAt);
        ProjectMember member = projectMemberMapper.selectOne(wrapper);
        if (member == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "项目成员不存在");
        }
        if (Objects.equals(member.getUserId(), loadActiveProject(projectId).getOwnerId())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不能移除项目负责人");
        }
        member.setDeletedAt(LocalDateTime.now());
        member.setUpdaterId(actorId);
        member.setStatus(0);
        projectMemberMapper.updateById(member);
        projectActivityService.recordActivity(projectId, actorId, "MEMBER_REMOVED", "移除项目成员");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void archive(Long projectId, Long operatorId) {
        Long actorId = Optional.ofNullable(operatorId)
                .orElseGet(() -> SecurityUtils.getCurrentUserId().orElse(null));
        if (actorId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }
        ensureProjectWritable(projectId, actorId);
        Project project = loadActiveProject(projectId);
        project.setStatus(3);
        project.setUpdaterId(actorId);
        project.setUpdateTime(LocalDateTime.now());
        projectMapper.updateById(project);
        projectActivityService.recordActivity(projectId, actorId, "PROJECT_ARCHIVED", "归档项目");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unarchive(Long projectId, Long operatorId) {
        Long actorId = Optional.ofNullable(operatorId)
                .orElseGet(() -> SecurityUtils.getCurrentUserId().orElse(null));
        if (actorId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }
        ensureProjectWritable(projectId, actorId);
        Project project = loadActiveProject(projectId);
        if (!Objects.equals(project.getStatus(), 3)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "项目未处于归档状态");
        }
        project.setStatus(1);
        project.setUpdaterId(actorId);
        project.setUpdateTime(LocalDateTime.now());
        projectMapper.updateById(project);
        projectActivityService.recordActivity(projectId, actorId, "PROJECT_UNARCHIVED", "取消归档项目");
    }

    @Override
    public boolean isProjectAdmin(Long projectId, Long userId) {
        return permissionService.isProjectAdmin(userId, projectId);
    }

    @Override
    public boolean isProjectMember(Long projectId, Long userId) {
        return permissionService.isProjectMember(userId, projectId);
    }

    @Override
    public boolean isMemberOrAdmin(Long projectId, Long userId) {
        return isProjectMember(projectId, userId) || isProjectAdmin(projectId, userId);
    }

    @Override
    @Deprecated
    public List<ProjectMemberDTO> listMembersByRole(Long projectId, Long userId, Long roleId) {
        PageResult<ProjectMemberDTO> result = listProjectMembers(projectId, userId, 1, Integer.MAX_VALUE, roleId);
        return result.getList() == null ? Collections.emptyList() : result.getList();
    }

    @Override
    @Deprecated
    public PageResult<Project> listUserProjects(Long userId, Integer page, Integer pageSize, Integer status, String keyword) {
        PageResult<ProjectDTO> dtoResult = listProjects(userId, page, pageSize, keyword, status, null);
        List<Project> projects = dtoResult.getList() == null ? Collections.emptyList() : dtoResult.getList().stream()
                .map(dto -> {
                    Project project = new Project();
                    project.setId(dto.getId());
                    project.setName(dto.getName());
                    project.setCode(dto.getCode());
                    project.setDescription(dto.getDescription());
                    project.setStatus(dto.getStatus());
                    project.setOwnerId(dto.getOwnerId());
                    project.setOrganizationId(dto.getOrganizationId());
                    project.setStartDate(dto.getStartDate());
                    project.setEndDate(dto.getEndDate());
                    project.setPriority(dto.getPriority());
                    project.setProgress(dto.getProgress());
                    project.setTags(dto.getTags());
                    project.setIcon(dto.getIcon());
                    project.setColor(dto.getColor());
                    project.setType(dto.getType());
                    project.setCreateTime(dto.getCreatedAt());
                    project.setUpdateTime(dto.getUpdatedAt());
                    return project;
                })
                .collect(Collectors.toList());
        return PageResult.of(projects, dtoResult.getTotal(), dtoResult.getPage(), dtoResult.getPageSize());
    }

    @Override
    @Deprecated
    public PageResult<Project> listUserProjects(Long userId, Integer page, Integer pageSize, Integer status) {
        return listUserProjects(userId, page, pageSize, status, null);
    }

    @Override
    public List<Project> listByIds(List<Long> projectIds) {
        log.info("批量查询项目, projectIds={}", projectIds);
        
        if (projectIds == null || projectIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        try {
            return baseMapper.selectByIds(projectIds);
        } catch (Exception e) {
            log.error("批量查询项目失败, projectIds={}", projectIds, e);
            return Collections.emptyList();
        }
    }
}
