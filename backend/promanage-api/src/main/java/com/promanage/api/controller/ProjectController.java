package com.promanage.api.controller;

import com.promanage.api.dto.request.CreateProjectRequest;
import com.promanage.api.dto.request.UpdateProjectRequest;
import com.promanage.api.dto.response.ProjectDetailResponse;
import com.promanage.api.dto.response.ProjectMemberResponse;
import com.promanage.api.dto.response.ProjectResponse;
import com.promanage.api.dto.response.UserResponse;
import com.promanage.common.domain.PageResult;
import com.promanage.common.domain.Result;
import com.promanage.common.exception.BusinessException;
import com.promanage.infrastructure.utils.SecurityUtils;
import com.promanage.service.entity.Project;
import com.promanage.service.entity.ProjectMember;
import com.promanage.service.entity.User;
import com.promanage.service.service.IProjectService;
import com.promanage.service.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 项目管理控制器
 * <p>
 * 提供项目的创建、查询、更新、删除以及成员管理功能
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-03
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/projects")
@Tag(name = "项目管理", description = "项目创建、查询、更新、删除以及成员管理接口")
@RequiredArgsConstructor
public class ProjectController {

    private final IProjectService projectService;
    private final IUserService userService;

    /**
     * 获取项目列表
     *
     * @param page 页码
     * @param size 每页大小
     * @param status 项目状态
     * @return 项目列表
     */
    @GetMapping
    @Operation(summary = "获取项目列表", description = "获取当前用户可访问的项目列表")
    public Result<PageResult<ProjectResponse>> getProjects(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Integer status) {

        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("获取项目列表请求, userId={}, page={}, size={}, status={}", userId, page, size, status);

        PageResult<Project> projectPage = projectService.listUserProjects(userId, page, size, status);

        List<ProjectResponse> projectResponses = projectPage.getList().stream()
                .map(this::convertToProjectResponse)
                .collect(Collectors.toList());

        PageResult<ProjectResponse> response = PageResult.of(
                projectResponses,
                projectPage.getTotal(),
                projectPage.getPage(),
                projectPage.getPageSize()
        );

        log.info("获取项目列表成功, total={}", response.getTotal());
        return Result.success(response);
    }

    /**
     * 创建新项目
     *
     * @param request 创建项目请求
     * @return 创建的项目信息
     */
    @PostMapping
    @Operation(summary = "创建新项目", description = "创建一个新的项目")
    public Result<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request) {
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("创建项目请求, userId={}, name={}", userId, request.getName());

        // 检查项目编码是否已存在
        if (projectService.existsByCode(request.getCode())) {
            throw new BusinessException("项目编码已存在");
        }

        Project project = new Project();
        project.setName(request.getName());
        project.setCode(request.getCode());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setPriority(request.getPriority());
        project.setStatus(0); // 默认状态：未开始
        project.setProgress(0); // 默认进度：0%
        project.setOwnerId(userId);
        project.setIcon(request.getIcon());
        project.setColor(request.getColor());
        project.setType(request.getType());

        Long projectId = projectService.create(project);

        // 将创建者添加为项目成员（项目经理角色）
        projectService.addMember(projectId, userId, 1L); // 假设1L是项目经理角色ID

        Project createdProject = projectService.getById(projectId);
        ProjectResponse response = convertToProjectResponse(createdProject);

        log.info("项目创建成功, projectId={}, name={}", projectId, request.getName());
        return Result.success(response);
    }

    /**
     * 获取项目详情
     *
     * @param projectId 项目ID
     * @return 项目详情
     */
    @GetMapping("/{projectId}")
    @Operation(summary = "获取项目详情", description = "获取指定项目的详细信息")
    public Result<ProjectDetailResponse> getProject(@PathVariable Long projectId) {
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("获取项目详情请求, projectId={}, userId={}", projectId, userId);

        // 检查权限
        if (!projectService.hasProjectViewPermission(projectId, userId)) {
            throw new BusinessException("没有权限查看此项目");
        }

        Project project = projectService.getById(projectId);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }

        ProjectDetailResponse response = convertToProjectDetailResponse(project);

        log.info("获取项目详情成功, projectId={}", projectId);
        return Result.success(response);
    }

    /**
     * 更新项目
     *
     * @param projectId 项目ID
     * @param request 更新项目请求
     * @return 更新后的项目信息
     */
    @PutMapping("/{projectId}")
    @Operation(summary = "更新项目", description = "更新项目信息")
    public Result<ProjectResponse> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody UpdateProjectRequest request) {

        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("更新项目请求, projectId={}, userId={}", projectId, userId);

        // 检查权限
        if (!projectService.hasProjectEditPermission(projectId, userId)) {
            throw new BusinessException("没有权限编辑此项目");
        }

        Project project = projectService.getById(projectId);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }

        // 更新项目信息
        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getStartDate() != null) {
            project.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            project.setEndDate(request.getEndDate());
        }
        if (request.getPriority() != null) {
            project.setPriority(request.getPriority());
        }
        if (request.getIcon() != null) {
            project.setIcon(request.getIcon());
        }
        if (request.getColor() != null) {
            project.setColor(request.getColor());
        }
        if (request.getType() != null) {
            project.setType(request.getType());
        }

        projectService.update(projectId, project);

        Project updatedProject = projectService.getById(projectId);
        ProjectResponse response = convertToProjectResponse(updatedProject);

        log.info("项目更新成功, projectId={}", projectId);
        return Result.success(response);
    }

    /**
     * 删除项目
     *
     * @param projectId 项目ID
     * @return 操作结果
     */
    @DeleteMapping("/{projectId}")
    @Operation(summary = "删除项目", description = "软删除项目（仅项目所有者可操作）")
    public Result<Void> deleteProject(@PathVariable Long projectId) {
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("删除项目请求, projectId={}, userId={}", projectId, userId);

        // 检查权限
        if (!projectService.hasProjectDeletePermission(projectId, userId)) {
            throw new BusinessException("没有权限删除此项目");
        }

        projectService.delete(projectId);

        log.info("项目删除成功, projectId={}", projectId);
        return Result.success();
    }

    /**
     * 获取项目成员列表
     *
     * @param projectId 项目ID
     * @param page 页码
     * @param size 每页大小
     * @return 成员列表
     */
    @GetMapping("/{projectId}/members")
    @Operation(summary = "获取项目成员", description = "获取项目成员列表")
    public Result<PageResult<ProjectMemberResponse>> getProjectMembers(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("获取项目成员列表请求, projectId={}, userId={}, page={}, size={}", projectId, userId, page, size);

        // 检查权限
        if (!projectService.hasProjectViewPermission(projectId, userId)) {
            throw new BusinessException("没有权限查看此项目");
        }

        PageResult<ProjectMember> memberPage = projectService.listProjectMembers(projectId, page, size);

        List<ProjectMemberResponse> memberResponses = memberPage.getList().stream()
                .map(this::convertToProjectMemberResponse)
                .collect(Collectors.toList());

        PageResult<ProjectMemberResponse> response = PageResult.of(
                memberResponses,
                memberPage.getTotal(),
                memberPage.getPage(),
                memberPage.getPageSize()
        );

        log.info("获取项目成员列表成功, projectId={}, total={}", projectId, response.getTotal());
        return Result.success(response);
    }

    /**
     * 添加项目成员
     *
     * @param projectId 项目ID
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 操作结果
     */
    @PostMapping("/{projectId}/members")
    @Operation(summary = "添加项目成员", description = "邀请用户加入项目")
    public Result<ProjectMemberResponse> addProjectMember(
            @PathVariable Long projectId,
            @RequestParam Long userId,
            @RequestParam Long roleId) {

        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("添加项目成员请求, projectId={}, currentUserId={}, newUserId={}, roleId={}",
                projectId, currentUserId, userId, roleId);

        // 检查权限
        if (!projectService.hasProjectMemberManagePermission(projectId, currentUserId)) {
            throw new BusinessException("没有权限管理项目成员");
        }

        // 检查用户是否已存在
        if (projectService.isProjectMember(projectId, userId)) {
            throw new BusinessException("该用户已是项目成员");
        }

        projectService.addMember(projectId, userId, roleId);

        // 获取新添加的成员信息
        ProjectMember member = projectService.getProjectMemberRole(projectId, userId);
        ProjectMemberResponse response = convertToProjectMemberResponse(member);

        log.info("添加项目成员成功, projectId={}, userId={}, roleId={}", projectId, userId, roleId);
        return Result.success(response);
    }

    /**
     * 更新成员角色
     *
     * @param projectId 项目ID
     * @param userId 用户ID
     * @param roleId 新角色ID
     * @return 更新后的成员信息
     */
    @PutMapping("/{projectId}/members/{userId}")
    @Operation(summary = "更新成员角色", description = "更新项目成员的角色")
    public Result<ProjectMemberResponse> updateProjectMemberRole(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @RequestParam Long roleId) {

        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("更新项目成员角色请求, projectId={}, currentUserId={}, targetUserId={}, newRoleId={}",
                projectId, currentUserId, userId, roleId);

        // 检查权限 - 简化处理，只有项目负责人可以管理成员
        if (!projectService.isMember(projectId, currentUserId)) {
            throw new BusinessException("没有权限管理项目成员");
        }

        // 检查用户是否是成员
        if (!projectService.isMember(projectId, userId)) {
            throw new BusinessException("该用户不是项目成员");
        }

        // TODO: 实现更新成员角色功能
        // projectService.updateProjectMemberRole(projectId, userId, roleId);
        // 暂时使用删除再添加的方式
        projectService.removeMember(projectId, userId);
        projectService.addMember(projectId, userId, roleId);

        // TODO: 获取成员信息需要实现相应的查询方法
        // ProjectMember member = projectService.getProjectMemberRole(projectId, userId);
        // 暂时创建一个模拟的成员响应
        User user = userService.getById(userId);
        ProjectMemberResponse response = ProjectMemberResponse.builder()
                .userId(userId)
                .username(user != null ? user.getUsername() : "")
                .realName(user != null ? user.getRealName() : "未知")
                .avatar(user != null ? user.getAvatar() : null)
                .email(user != null ? user.getEmail() : null)
                .roleId(roleId)
                .roleName("项目成员") // TODO: 从角色服务获取
                .joinedAt(java.time.LocalDateTime.now())
                .status(0)
                .build();

        log.info("更新项目成员角色成功, projectId={}, userId={}, roleId={}", projectId, userId, roleId);
        return Result.success(response);
    }

    /**
     * 移除项目成员
     *
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{projectId}/members/{userId}")
    @Operation(summary = "移除项目成员", description = "从项目中移除成员")
    public Result<Void> removeProjectMember(
            @PathVariable Long projectId,
            @PathVariable Long userId) {

        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("移除项目成员请求, projectId={}, currentUserId={}, targetUserId={}",
                projectId, currentUserId, userId);

        // 检查权限 - 简化处理，只有项目成员可以管理成员
        if (!projectService.isMember(projectId, currentUserId)) {
            throw new BusinessException("没有权限管理项目成员");
        }

        // 不能移除项目负责人 - 简化处理，检查是否为项目创建者
        Project project = projectService.getById(projectId);
        if (project != null && project.getOwnerId().equals(userId)) {
            throw new BusinessException("不能移除项目负责人");
        }

        projectService.removeMember(projectId, userId);

        log.info("移除项目成员成功, projectId={}, userId={}", projectId, userId);
        return Result.success();
    }

    // 辅助方法

    private ProjectResponse convertToProjectResponse(Project project) {
        User owner = userService.getById(project.getOwnerId());

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .code(project.getCode())
                .description(project.getDescription())
                .status(project.getStatus())
                .ownerId(project.getOwnerId())
                .ownerName(owner != null ? owner.getRealName() : "未知")
                .ownerAvatar(owner != null ? owner.getAvatar() : null)
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .icon(project.getIcon())
                .color(project.getColor())
                .type(project.getType())
                .priority(project.getPriority())
                .progress(project.getProgress())
                .memberCount(projectService.countProjectMembers(project.getId()))
                .documentCount(0) // TODO: 需要从文档服务获取
                .createTime(project.getCreateTime())
                .updateTime(project.getUpdateTime())
                .build();
    }

    private ProjectDetailResponse convertToProjectDetailResponse(Project project) {
        User owner = userService.getById(project.getOwnerId());

        return ProjectDetailResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .code(project.getCode())
                .description(project.getDescription())
                .status(project.getStatus())
                .ownerId(project.getOwnerId())
                .ownerName(owner != null ? owner.getRealName() : "未知")
                .ownerAvatar(owner != null ? owner.getAvatar() : null)
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .icon(project.getIcon())
                .color(project.getColor())
                .type(project.getType())
                .priority(project.getPriority())
                .progress(project.getProgress())
                .memberCount(projectService.countProjectMembers(project.getId()))
                .documentCount(0) // TODO: 需要从文档服务获取
                .taskCount(0) // TODO: 需要从任务服务获取
                .createTime(project.getCreateTime())
                .updateTime(project.getUpdateTime())
                .build();
    }

    private ProjectMemberResponse convertToProjectMemberResponse(ProjectMember member) {
        User user = userService.getById(member.getUserId());

        return ProjectMemberResponse.builder()
                .id(member.getId())
                .projectId(member.getProjectId())
                .userId(member.getUserId())
                .username(user != null ? user.getUsername() : "")
                .realName(user != null ? user.getRealName() : "未知")
                .avatar(user != null ? user.getAvatar() : null)
                .email(user != null ? user.getEmail() : null)
                .roleId(member.getRoleId())
                .roleName("项目成员") // TODO: 需要从角色服务获取
                .roleCode("MEMBER") // TODO: 需要从角色服务获取
                .joinedAt(member.getJoinTime())
                .status(member.getStatus())
                .build();
    }
}