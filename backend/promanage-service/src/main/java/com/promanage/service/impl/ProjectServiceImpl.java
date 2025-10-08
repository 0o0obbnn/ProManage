package com.promanage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.promanage.common.domain.PageResult;
import com.promanage.common.domain.ResultCode;
import com.promanage.common.exception.BusinessException;
import com.promanage.infrastructure.security.SecurityUtils;
import com.promanage.service.IProjectActivityService;
import com.promanage.service.IProjectService;
import com.promanage.service.service.IUserService;
import com.promanage.dto.ProjectMemberDTO;
import com.promanage.dto.ProjectRequest;
import com.promanage.dto.ProjectStatsDTO;
import com.promanage.service.entity.Project;
import com.promanage.service.entity.ProjectMember;
import com.promanage.common.entity.User;
import com.promanage.service.mapper.ProjectMapper;
import com.promanage.service.mapper.ProjectMemberMapper;
import com.promanage.service.mapper.TaskMapper;
import com.promanage.service.mapper.DocumentMapper;
import com.promanage.service.mapper.ChangeRequestMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author nifa
 * @since 2024-10-04
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements IProjectService {

    private final ProjectMemberMapper projectMemberMapper;
    private final IUserService userService;
    private final TaskMapper taskMapper;
    private final DocumentMapper documentMapper;
    private final ChangeRequestMapper changeRequestMapper;
    private final IProjectActivityService projectActivityService;


    /**
     * 创建项目
     */
    @Transactional
    public Long create(Project project) {
        log.info("创建项目: {}", project.getName());
        validateProject(project, true);
        project.setStatus(0); // 0 表示正常状态
        project.setCreateTime(java.time.LocalDateTime.now());
        project.setUpdateTime(java.time.LocalDateTime.now());
        save(project);
        return project.getId();
    }

    /**
     * 更新项目
     */
    @Transactional
    public void update(Long id, Project project) {
        log.info("更新项目: {}", id);
        Project existing = getById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "项目不存在");
        }
        validateProject(project, false);
        project.setId(id);
        project.setUpdateTime(java.time.LocalDateTime.now());
        updateById(project);
    }

    /**
     * 删除项目
     */
    @Transactional
    public void delete(Long id) {
        log.info("删除项目: {}", id);
        Project existing = getById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "项目不存在");
        }
        removeById(id);
    }

    /**
     * 查询项目列表
     */
    public PageResult<Project> listProjects(Integer page, Integer pageSize, String keyword, Integer status) {
        log.info("查询项目列表: page={}, pageSize={}, keyword={}, status={}", page, pageSize, keyword, status);
        
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Project::getName, keyword)
                    .or().like(Project::getCode, keyword)
                    .or().like(Project::getDescription, keyword));
        }
        if (status != null) {
            wrapper.eq(Project::getStatus, status);
        }
        wrapper.orderByDesc(Project::getUpdateTime);
        
        IPage<Project> pageResult = page(new Page<>(page, pageSize), wrapper);
        return PageResult.of(pageResult.getRecords(), pageResult.getTotal(), page, pageSize);
    }

    /**
     * 根据负责人ID查询项目
     */
    public List<Project> listByOwnerId(Long ownerId) {
        log.info("根据负责人ID查询项目: {}", ownerId);
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Project::getOwnerId, ownerId);
        wrapper.orderByDesc(Project::getUpdateTime);
        return list(wrapper);
    }

    /**
     * 根据成员ID查询项目
     */
    public List<Project> listByMemberId(Long userId) {
        log.info("根据成员ID查询项目: {}", userId);
        // 查询用户参与的项目ID
        LambdaQueryWrapper<ProjectMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(ProjectMember::getUserId, userId);
        List<ProjectMember> members = projectMemberMapper.selectList(memberWrapper);
        
        if (members.isEmpty()) {
            return List.of();
        }
        
        List<Long> projectIds = members.stream()
                .map(ProjectMember::getProjectId)
                .collect(Collectors.toList());
        
        LambdaQueryWrapper<Project> projectWrapper = new LambdaQueryWrapper<>();
        projectWrapper.in(Project::getId, projectIds);
        return list(projectWrapper);
    }

    /**
     * 根据ID查询项目
     */
    public Project getById(Long id) {
        log.debug("根据ID查询项目: {}", id);
        return super.getById(id);
    }

    /**
     * 根据编码查询项目
     */
    public Project getByCode(String code) {
        log.debug("根据编码查询项目: {}", code);
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Project::getCode, code);
        return getOne(wrapper);
    }

    /**
     * 统计项目成员数量
     */
    public int countProjectMembers(Long projectId) {
        log.debug("统计项目成员数量: {}", projectId);
        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId);
        return Math.toIntExact(projectMemberMapper.selectCount(wrapper));
    }

    @Override
    public PageResult<ProjectMemberDTO> listMembers(Long projectId, Integer page, Integer pageSize, Long roleId) {
        log.info("查询项目成员列表: projectId={}, page={}, pageSize={}, roleId={}", projectId, page, pageSize, roleId);
        
        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId);
        if (roleId != null) {
            wrapper.eq(ProjectMember::getRoleId, roleId);
        }
        
        IPage<ProjectMember> pageResult = projectMemberMapper.selectPage(new Page<>(page, pageSize), wrapper);
        
        List<ProjectMemberDTO> dtoList = pageResult.getRecords().stream().map(member -> {
            ProjectMemberDTO dto = new ProjectMemberDTO();
            BeanUtils.copyProperties(member, dto);
            
            // 查询用户信息
            User user = userService.getById(member.getUserId());
            if (user != null) {
                dto.setUsername(user.getUsername());
                dto.setRealName(user.getRealName());
                dto.setEmail(user.getEmail());
                dto.setAvatar(user.getAvatar());
            }
            
            return dto;
        }).collect(Collectors.toList());
        
        return PageResult.of(dtoList, pageResult.getTotal(), page, pageSize);
    }

    @Override
    public ProjectStatsDTO getProjectStats(Long projectId) {
        log.info("获取项目统计数据: {}", projectId);
        
        // 验证项目是否存在
        getById(projectId);

        long taskCount = taskMapper.selectCount(new LambdaQueryWrapper<com.promanage.service.entity.Task>().eq(com.promanage.service.entity.Task::getProjectId, projectId));
        long documentCount = documentMapper.selectCount(new LambdaQueryWrapper<com.promanage.service.entity.Document>().eq(com.promanage.service.entity.Document::getProjectId, projectId));
        long memberCount = projectMemberMapper.selectCount(new LambdaQueryWrapper<ProjectMember>().eq(ProjectMember::getProjectId, projectId));
        long changeRequestCount = changeRequestMapper.selectCount(new LambdaQueryWrapper<com.promanage.service.entity.ChangeRequest>().eq(com.promanage.service.entity.ChangeRequest::getProjectId, projectId));
        
        // 查询任务状态统计
        long completedTasks = taskMapper.selectCount(new LambdaQueryWrapper<com.promanage.service.entity.Task>()
                .eq(com.promanage.service.entity.Task::getProjectId, projectId)
                .eq(com.promanage.service.entity.Task::getStatus, 3)); // 3 表示已完成
        long inProgressTasks = taskMapper.selectCount(new LambdaQueryWrapper<com.promanage.service.entity.Task>()
                .eq(com.promanage.service.entity.Task::getProjectId, projectId)
                .eq(com.promanage.service.entity.Task::getStatus, 2)); // 2 表示进行中
        long pendingTasks = taskMapper.selectCount(new LambdaQueryWrapper<com.promanage.service.entity.Task>()
                .eq(com.promanage.service.entity.Task::getProjectId, projectId)
                .eq(com.promanage.service.entity.Task::getStatus, 1)); // 1 表示待办

        ProjectStatsDTO stats = new ProjectStatsDTO();
        stats.setProjectId(projectId);
        stats.setTotalTasks((int) taskCount);
        stats.setCompletedTasks((int) completedTasks);
        stats.setInProgressTasks((int) inProgressTasks);
        stats.setPendingTasks((int) pendingTasks);
        stats.setMemberCount((int) memberCount);
        stats.setTotalDocuments((int) documentCount);
        stats.setChangeRequests((int) changeRequestCount);
        
        // 计算进度百分比
        if (taskCount > 0) {
            stats.setProgressPercentage((double) completedTasks / taskCount * 100);
        } else {
            stats.setProgressPercentage(0.0);
        }
        
        return stats;
    }

    @Override
    @Transactional
    public void archive(Long projectId) {
        log.info("归档项目: {}", projectId);
        Project project = getById(projectId);
        project.setStatus(3); // 3 表示已归档
        updateById(project);
        
        Long currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
        projectActivityService.recordActivity(projectId, currentUserId, "PROJECT_ARCHIVED", "项目已归档");
    }

    @Override
    @Transactional
    public void unarchive(Long projectId) {
        log.info("取消归档项目: {}", projectId);
        Project project = getById(projectId);
        if (project.getStatus() != 3) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "项目未归档");
        }
        project.setStatus(0); // 0 表示正常
        updateById(project);
        
        Long currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
        projectActivityService.recordActivity(projectId, currentUserId, "PROJECT_UNARCHIVED", "项目已取消归档");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMember(Long projectId, Long userId, Long roleId) {
        log.info("添加项目成员: projectId={}, userId={}, roleId={}", projectId, userId, roleId);
        
        // 验证用户是否存在
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        
        // 检查是否已经是成员
        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId);
        wrapper.eq(ProjectMember::getUserId, userId);
        ProjectMember existing = projectMemberMapper.selectOne(wrapper);
        if (existing != null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户已经是项目成员");
        }
        
        // 添加成员
        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(userId);
        member.setRoleId(roleId);
        member.setJoinTime(java.time.LocalDateTime.now());
        member.setStatus(1); // 1 表示启用
        projectMemberMapper.insert(member);
        
        // 记录活动
        Long currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
        projectActivityService.recordActivity(projectId, currentUserId, "MEMBER_ADDED", 
            String.format("添加成员: %s", user.getRealName()));
    }

    /**
     * 批量添加项目成员
     */
    @Transactional(rollbackFor = Exception.class)
    public void addMembers(Long projectId, List<ProjectMemberDTO> members) {
        log.info("批量添加项目成员: projectId={}, count={}", projectId, members.size());
        
        for (ProjectMemberDTO memberDto : members) {
            addMember(projectId, memberDto.getUserId(), memberDto.getRoleId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeMember(Long projectId, Long userId) {
        log.info("移除项目成员: projectId={}, userId={}", projectId, userId);
        
        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId);
        wrapper.eq(ProjectMember::getUserId, userId);
        ProjectMember member = projectMemberMapper.selectOne(wrapper);
        if (member == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "成员不存在");
        }
        
        projectMemberMapper.deleteById(member.getId());
        
        // 记录活动
        User user = userService.getById(userId);
        Long currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
        projectActivityService.recordActivity(projectId, currentUserId, "MEMBER_REMOVED", 
            String.format("移除成员: %s", user != null ? user.getRealName() : "未知用户"));
    }

    public boolean isMember(Long projectId, Long userId) {
        log.debug("检查用户是否为项目成员: projectId={}, userId={}", projectId, userId);
        
        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId);
        wrapper.eq(ProjectMember::getUserId, userId);
        wrapper.eq(ProjectMember::getStatus, 1); // 1 表示启用
        return projectMemberMapper.selectCount(wrapper) > 0;
    }

    /**
     * 检查项目编码是否存在
     */
    public boolean existsByCode(String code) {
        log.debug("检查项目编码是否存在: {}", code);
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Project::getCode, code);
        return count(wrapper) > 0;
    }
    
    @Override
    public List<ProjectMemberDTO> listMembersByRole(Long projectId, Long userId, Long roleId) {
        log.info("查询项目成员列表（按角色过滤）: projectId={}, userId={}, roleId={}", projectId, userId, roleId);
        
        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId);
        if (roleId != null) {
            wrapper.eq(ProjectMember::getRoleId, roleId);
        }
        
        List<ProjectMember> members = projectMemberMapper.selectList(wrapper);
        
        return members.stream().map(member -> {
            ProjectMemberDTO dto = new ProjectMemberDTO();
            BeanUtils.copyProperties(member, dto);
            
            // 查询用户信息
            User user = userService.getById(member.getUserId());
            if (user != null) {
                dto.setUsername(user.getUsername());
                dto.setRealName(user.getRealName());
                dto.setEmail(user.getEmail());
                dto.setAvatar(user.getAvatar());
            }
            
            return dto;
        }).collect(Collectors.toList());
    }
    
    @Override
    public PageResult<Project> listUserProjects(Long userId, Integer page, Integer pageSize, Integer status, String keyword) {
        log.info("获取项目列表（按用户）: userId={}, page={}, pageSize={}, status={}, keyword={}", userId, page, pageSize, status, keyword);

        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();

        // Base query for projects accessible by the user
        wrapper.and(w -> w.eq(Project::getOwnerId, userId)
                .or(in -> {
                    LambdaQueryWrapper<ProjectMember> memberWrapper = new LambdaQueryWrapper<>();
                    memberWrapper.select(ProjectMember::getProjectId).eq(ProjectMember::getUserId, userId);
                    List<Object> projectIds = projectMemberMapper.selectObjs(memberWrapper);
                    if (projectIds == null || projectIds.isEmpty()) {
                        in.apply("1 = 0"); // No projects if not a member of any
                    } else {
                        in.in(Project::getId, projectIds);
                    }
                }));

        // Filter by status
        if (status != null) {
            wrapper.eq(Project::getStatus, status);
        }

        // Filter by keyword
        if (StringUtils.isNotBlank(keyword)) {
            // Find users whose name matches the keyword to search by owner
            List<User> users = userService.listAll().stream()
                    .filter(u -> (u.getRealName() != null && u.getRealName().contains(keyword)) ||
                                 (u.getUsername() != null && u.getUsername().contains(keyword)))
                    .collect(Collectors.toList());
            List<Long> ownerIds = users.stream().map(User::getId).collect(Collectors.toList());

            wrapper.and(w -> {
                w.like(Project::getName, keyword)
                        .or().like(Project::getCode, keyword);
                if (!ownerIds.isEmpty()) {
                    w.or().in(Project::getOwnerId, ownerIds);
                }
            });
        }

        wrapper.orderByDesc(Project::getUpdateTime);

        IPage<Project> pageResult = page(new Page<>(page, pageSize), wrapper);
        return PageResult.of(pageResult.getRecords(), pageResult.getTotal(), page, pageSize);
    }

    @Override
    public PageResult<Project> listUserProjects(Long userId, Integer page, Integer pageSize, Integer status) {
        return listUserProjects(userId, page, pageSize, status, null);
    }
    
    @Override
    public boolean isAdmin(Long projectId, Long userId) {
        log.debug("检查用户是否为项目管理员: projectId={}, userId={}", projectId, userId);
        
        // 检查是否为项目负责人
        Project project = getById(projectId);
        if (project != null && project.getOwnerId().equals(userId)) {
            return true;
        }
        
        // 检查是否有管理员角色
        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId);
        wrapper.eq(ProjectMember::getUserId, userId);
        wrapper.eq(ProjectMember::getRoleId, 1L); // 假设 1 是管理员角色ID
        wrapper.eq(ProjectMember::getStatus, 1);
        return projectMemberMapper.selectCount(wrapper) > 0;
    }
    
    @Override
    public boolean isMemberOrAdmin(Long projectId, Long userId) {
        return isMember(projectId, userId) || isAdmin(projectId, userId);
    }

    /**
     * 验证项目数据
     *
     * @param project 项目实体
     * @param isCreate 是否为创建操作
     */
    private void validateProject(Project project, boolean isCreate) {
        if (project == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "项目信息不能为空");
        }

        if (isCreate && StringUtils.isBlank(project.getName())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "项目名称不能为空");
        }

        if (isCreate && StringUtils.isBlank(project.getCode())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "项目编码不能为空");
        }

        if (StringUtils.isNotBlank(project.getName()) && project.getName().length() > 100) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "项目名称长度不能超过100个字符");
        }

        if (StringUtils.isNotBlank(project.getCode()) && project.getCode().length() > 50) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "项目编码长度不能超过50个字符");
        }

        if (StringUtils.isNotBlank(project.getDescription()) && project.getDescription().length() > 500) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "项目描述长度不能超过500个字符");
        }
    }
}