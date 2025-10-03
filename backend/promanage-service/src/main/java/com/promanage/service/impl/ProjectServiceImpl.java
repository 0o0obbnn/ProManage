package com.promanage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.promanage.common.domain.PageResult;
import com.promanage.common.domain.ResultCode;
import com.promanage.common.exception.BusinessException;
import com.promanage.infrastructure.security.SecurityUtils;
import com.promanage.service.entity.Project;
import com.promanage.service.entity.ProjectMember;
import com.promanage.service.mapper.ProjectMapper;
import com.promanage.service.mapper.ProjectMemberMapper;
import com.promanage.service.service.IProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 项目服务实现类
 * <p>
 * 实现项目管理的所有业务逻辑,包括项目CRUD操作、成员管理和状态管理。
 * 使用Redis缓存提高查询性能,使用事务保证数据一致性。
 * 支持项目生命周期管理和团队协作。
 * </p>
 *
 * @author ProManage Team
 * @date 2025-09-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements IProjectService {

    private final ProjectMapper projectMapper;
    private final ProjectMemberMapper projectMemberMapper;

    /**
     * 项目状态 - 规划中
     */
    private static final int PROJECT_STATUS_PLANNING = 0;

    /**
     * 项目状态 - 已归档
     */
    private static final int PROJECT_STATUS_ARCHIVED = 3;

    @Override
    @Cacheable(value = "projects", key = "#id", unless = "#result == null")
    public Project getById(Long id) {
        if (id == null) {
            return null;
        }

        log.debug("查询项目详情, id={}", id);
        Project project = projectMapper.selectById(id);
        if (project != null) {
            log.debug("项目查询成功, id={}, name={}", id, project.getName());
        } else {
            log.warn("项目不存在, id={}", id);
        }
        return project;
    }

    @Override
    @Cacheable(value = "projects", key = "'code:' + #code", unless = "#result == null")
    public Project getByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }

        log.debug("根据编码查询项目, code={}", code);
        Project project = projectMapper.findByCode(code);
        if (project != null) {
            log.debug("项目查询成功, code={}, name={}", code, project.getName());
        } else {
            log.warn("项目不存在, code={}", code);
        }
        return project;
    }

    @Override
    public PageResult<Project> listProjects(Integer page, Integer pageSize, String keyword, Integer status) {
        log.debug("查询项目列表, page={}, pageSize={}, keyword={}, status={}", page, pageSize, keyword, status);

        // 参数验证和默认值设置
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 20;
        }

        // 构建查询条件
        LambdaQueryWrapper<Project> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Project::getDeleted, false);

        // 关键词搜索
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(Project::getName, keyword)
                    .or()
                    .like(Project::getCode, keyword)
                    .or()
                    .like(Project::getDescription, keyword)
            );
        }

        // 状态筛选
        if (status != null) {
            queryWrapper.eq(Project::getStatus, status);
        }

        // 排序
        queryWrapper.orderByDesc(Project::getCreateTime);

        // 分页查询
        Page<Project> pageParam = new Page<>(page, pageSize);
        IPage<Project> pageResult = projectMapper.selectPage(pageParam, queryWrapper);

        // 构建返回结果
        PageResult<Project> result = PageResult.of(
                pageResult.getRecords(),
                pageResult.getTotal(),
                page,
                pageSize
        );

        log.debug("项目列表查询完成, 总数={}, 当前页={}", result.getTotal(), result.getPage());
        return result;
    }

    @Override
    public List<Project> listByOwnerId(Long ownerId) {
        if (ownerId == null) {
            return List.of();
        }

        log.debug("查询用户负责的项目列表, ownerId={}", ownerId);
        List<Project> projects = projectMapper.findByOwnerId(ownerId);
        log.debug("用户负责的项目查询完成, ownerId={}, 数量={}", ownerId, projects.size());
        return projects;
    }

    @Override
    public List<Project> listByMemberId(Long userId) {
        if (userId == null) {
            return List.of();
        }

        log.debug("查询用户参与的项目列表, userId={}", userId);
        List<Project> projects = projectMapper.findByMemberId(userId);
        log.debug("用户参与的项目查询完成, userId={}, 数量={}", userId, projects.size());
        return projects;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "projects", allEntries = true)
    public Long create(Project project) {
        log.info("创建项目, name={}, code={}", project.getName(), project.getCode());

        // 参数验证
        validateProject(project, true);

        // 检查项目编码是否已存在
        if (StringUtils.isNotBlank(project.getCode()) && projectMapper.existsByCode(project.getCode())) {
            log.warn("项目编码已存在, code={}", project.getCode());
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS, "项目编码已存在");
        }

        // 设置默认值
        if (project.getStatus() == null) {
            project.setStatus(PROJECT_STATUS_PLANNING);
        }
        if (project.getProgress() == null) {
            project.setProgress(0);
        }

        // 设置创建信息
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "未登录"));
        project.setCreatorId(currentUserId);
        project.setUpdaterId(currentUserId);

        // 保存项目
        int result = projectMapper.insert(project);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "创建项目失败");
        }

        log.info("项目创建成功, id={}, name={}, code={}", project.getId(), project.getName(), project.getCode());
        return project.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "projects", key = "#id")
    public void update(Long id, Project project) {
        log.info("更新项目, id={}, name={}", id, project.getName());

        // 参数验证
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "项目ID不能为空");
        }

        // 检查项目是否存在
        Project existingProject = getById(id);
        if (existingProject == null) {
            throw new BusinessException(ResultCode.PROJECT_NOT_FOUND);
        }

        // 检查项目是否已归档
        if (existingProject.getStatus() != null && existingProject.getStatus().equals(PROJECT_STATUS_ARCHIVED)) {
            log.warn("项目已归档,不允许修改, id={}", id);
            throw new BusinessException(ResultCode.PROJECT_ARCHIVED);
        }

        // 验证项目数据
        validateProject(project, false);

        // 检查项目编码是否重复（排除自己）
        if (StringUtils.isNotBlank(project.getCode()) && 
            !project.getCode().equals(existingProject.getCode()) && 
            projectMapper.existsByCode(project.getCode())) {
                log.warn("项目编码已存在, code={}", project.getCode());
                throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS, "项目编码已存在");
        }

        // 设置更新信息
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "未登录"));
        project.setId(id);
        project.setUpdaterId(currentUserId);

        // 更新项目
        int result = projectMapper.updateById(project);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "更新项目失败");
        }

        log.info("项目更新成功, id={}, name={}", id, project.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "projects", key = "#id")
    public void delete(Long id) {
        log.info("删除项目, id={}", id);

        // 参数验证
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "项目ID不能为空");
        }

        // 检查项目是否存在
        Project project = getById(id);
        if (project == null) {
            throw new BusinessException(ResultCode.PROJECT_NOT_FOUND);
        }

        // 逻辑删除项目
        project.setDeleted(true);
        project.setUpdaterId(SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "未登录")));

        int result = projectMapper.updateById(project);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "删除项目失败");
        }

        log.info("项目删除成功, id={}, name={}", id, project.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "projects", allEntries = true)
    public int batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        log.info("批量删除项目, ids={}", ids);

        int deletedCount = 0;
        for (Long id : ids) {
            try {
                delete(id);
                deletedCount++;
            } catch (Exception e) {
                log.error("删除项目失败, id={}, error={}", id, e.getMessage());
            }
        }

        log.info("批量删除项目完成, 总数={}, 成功={}", ids.size(), deletedCount);
        return deletedCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "projects", key = "#id")
    public void updateStatus(Long id, Integer status) {
        log.info("更新项目状态, id={}, status={}", id, status);

        // 参数验证
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "项目ID不能为空");
        }
        if (status == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "项目状态不能为空");
        }

        // 检查项目是否存在
        Project project = getById(id);
        if (project == null) {
            throw new BusinessException(ResultCode.PROJECT_NOT_FOUND);
        }

        // 检查状态是否有效
        if (status < 0 || status > 3) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "项目状态无效");
        }

        // 更新状态
        project.setStatus(status);
        project.setUpdaterId(SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "未登录")));

        int result = projectMapper.updateById(project);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "更新项目状态失败");
        }

        log.info("项目状态更新成功, id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "projectMembers", allEntries = true)
    public void addMember(Long projectId, Long userId, Long roleId) {
        log.info("添加项目成员, projectId={}, userId={}, roleId={}", projectId, userId, roleId);

        // 参数验证
        if (projectId == null || userId == null || roleId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "项目ID、用户ID和角色ID不能为空");
        }

        // 检查项目是否存在
        Project project = getById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.PROJECT_NOT_FOUND);
        }

        // 检查是否已经是项目成员
        if (isMember(projectId, userId)) {
            log.warn("用户已经是项目成员, projectId={}, userId={}", projectId, userId);
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS, "用户已经是项目成员");
        }

        // 创建项目成员记录
        ProjectMember projectMember = new ProjectMember();
        projectMember.setProjectId(projectId);
        projectMember.setUserId(userId);
        projectMember.setRoleId(roleId);
        projectMember.setJoinTime(java.time.LocalDateTime.now());
        projectMember.setStatus(0); // 0-正常
        projectMember.setCreatorId(SecurityUtils.getCurrentUserId().orElse(null));

        projectMemberMapper.insert(projectMember);
        log.info("项目成员添加成功, projectId={}, userId={}, roleId={}", projectId, userId, roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "projectMembers", allEntries = true)
    public void addMembers(Long projectId, List<Object> members) {
        log.info("批量添加项目成员, projectId={}, count={}", projectId, members != null ? members.size() : 0);

        if (projectId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "项目ID不能为空");
        }

        if (members == null || members.isEmpty()) {
            log.warn("成员列表为空, projectId={}", projectId);
            return;
        }

        // 检查项目是否存在
        Project project = getById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.PROJECT_NOT_FOUND);
        }

        // 批量添加成员
        List<ProjectMember> projectMembers = new ArrayList<>();
        for (Object memberObj : members) {
            if (memberObj instanceof ProjectMember) {
                ProjectMember member = (ProjectMember) memberObj;
                member.setProjectId(projectId);
                member.setJoinTime(java.time.LocalDateTime.now());
                member.setStatus(0); // 0-正常
                member.setCreatorId(SecurityUtils.getCurrentUserId().orElse(null));
                projectMembers.add(member);
            }
        }

        if (!projectMembers.isEmpty()) {
            projectMemberMapper.batchInsert(projectMembers);
            log.info("批量添加项目成员成功, projectId={}, count={}", projectId, projectMembers.size());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "projectMembers", allEntries = true)
    public void removeMember(Long projectId, Long userId) {
        log.info("移除项目成员, projectId={}, userId={}", projectId, userId);

        // 参数验证
        if (projectId == null || userId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "项目ID和用户ID不能为空");
        }

        // 检查项目是否存在
        Project project = getById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.PROJECT_NOT_FOUND);
        }

        // 检查是否是项目成员
        if (!isMember(projectId, userId)) {
            log.warn("用户不是项目成员, projectId={}, userId={}", projectId, userId);
            throw new BusinessException(ResultCode.NOT_PROJECT_MEMBER);
        }

        // 检查是否是项目负责人
        if (project.getOwnerId().equals(userId)) {
            log.warn("不能移除项目负责人, projectId={}, userId={}", projectId, userId);
            throw new BusinessException(ResultCode.OPERATION_FAILED, "不能移除项目负责人");
        }

        // 删除项目成员记录
        projectMemberMapper.delete(new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getUserId, userId));

        log.info("项目成员移除成功, projectId={}, userId={}", projectId, userId);
    }

    @Override
    @Cacheable(value = "projectMembers", key = "#projectId")
    public List<Object> listMembers(Long projectId) {
        log.info("查询项目成员列表, projectId={}", projectId);

        if (projectId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "项目ID不能为空");
        }

        // 检查项目是否存在
        Project project = getById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.PROJECT_NOT_FOUND);
        }

        List<ProjectMember> members = projectMemberMapper.findByProjectId(projectId);
        log.info("查询项目成员列表成功, projectId={}, count={}", projectId, members.size());
        
        // 转换为Object列表以保持接口兼容性
        return new ArrayList<>(members);
    }

    @Override
    public boolean isMember(Long projectId, Long userId) {
        if (projectId == null || userId == null) {
            return false;
        }

        return projectMemberMapper.existsByProjectIdAndUserId(projectId, userId);
    }

    @Override
    public boolean existsByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return false;
        }

        log.debug("检查项目编码是否存在, code={}", code);
        boolean exists = projectMapper.existsByCode(code);
        log.debug("项目编码检查完成, code={}, exists={}", code, exists);
        return exists;
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

    @Override
    public int countProjectMembers(Long projectId) {
        log.debug("统计项目成员数量, projectId={}", projectId);

        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getStatus, 0); // 正常状态

        return Math.toIntExact(projectMemberMapper.selectCount(wrapper));
    }

    @Override
    public PageResult<Project> listUserProjects(Long userId, Integer page, Integer pageSize, Integer status) {
        log.debug("查询用户项目列表, userId={}, page={}, pageSize={}, status={}", userId, page, pageSize, status);

        // 参数验证和默认值设置
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 20;
        }

        // 查询用户作为成员的项目ID列表
        List<Project> userProjects = projectMapper.findByMemberId(userId);
        List<Long> projectIds = userProjects.stream()
                .map(Project::getId)
                .collect(Collectors.toList());

        if (projectIds.isEmpty()) {
            return PageResult.of(List.of(), 0L, page, pageSize);
        }

        // 构建查询条件
        LambdaQueryWrapper<Project> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Project::getId, projectIds)
                .eq(Project::getDeleted, false);

        // 状态筛选
        if (status != null) {
            queryWrapper.eq(Project::getStatus, status);
        }

        // 排序
        queryWrapper.orderByDesc(Project::getCreateTime);

        // 分页查询
        Page<Project> pageParam = new Page<>(page, pageSize);
        IPage<Project> pageResult = projectMapper.selectPage(pageParam, queryWrapper);

        // 构建返回结果
        PageResult<Project> result = PageResult.of(
                pageResult.getRecords(),
                pageResult.getTotal(),
                page,
                pageSize
        );

        log.debug("用户项目列表查询完成, 总数={}, 当前页={}", result.getTotal(), result.getPage());
        return result;
    }

    @Override
    public boolean hasProjectViewPermission(Long projectId, Long userId) {
        if (projectId == null || userId == null) {
            return false;
        }
        // 项目负责人或项目成员都有查看权限
        Project project = getById(projectId);
        if (project != null && project.getOwnerId().equals(userId)) {
            return true;
        }
        return isMember(projectId, userId);
    }

    @Override
    public boolean hasProjectEditPermission(Long projectId, Long userId) {
        if (projectId == null || userId == null) {
            return false;
        }
        // 项目负责人有编辑权限
        Project project = getById(projectId);
        return project != null && project.getOwnerId().equals(userId);
    }

    @Override
    public boolean hasProjectDeletePermission(Long projectId, Long userId) {
        if (projectId == null || userId == null) {
            return false;
        }
        // 项目负责人有删除权限
        Project project = getById(projectId);
        return project != null && project.getOwnerId().equals(userId);
    }

    @Override
    public boolean hasProjectMemberManagePermission(Long projectId, Long userId) {
        if (projectId == null || userId == null) {
            return false;
        }
        // 项目负责人有成员管理权限
        Project project = getById(projectId);
        return project != null && project.getOwnerId().equals(userId);
    }

    @Override
    public boolean isProjectMember(Long projectId, Long userId) {
        return isMember(projectId, userId);
    }

    @Override
    public PageResult<ProjectMember> listProjectMembers(Long projectId, Integer page, Integer pageSize) {
        if (projectId == null) {
            return PageResult.of(List.of(), 0L, page != null ? page : 1, pageSize != null ? pageSize : 20);
        }

        // 参数验证和默认值设置
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 20;
        }

        // 查询项目成员
        List<ProjectMember> members = projectMemberMapper.findByProjectId(projectId);

        // 手动分页
        int total = members.size();
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, total);

        List<ProjectMember> pageList = start < total ? members.subList(start, end) : List.of();

        return PageResult.of(pageList, (long) total, page, pageSize);
    }

    @Override
    public ProjectMember getProjectMemberRole(Long projectId, Long userId) {
        if (projectId == null || userId == null) {
            return null;
        }

        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getUserId, userId)
                .eq(ProjectMember::getStatus, 0); // 正常状态

        return projectMemberMapper.selectOne(wrapper);
    }
}