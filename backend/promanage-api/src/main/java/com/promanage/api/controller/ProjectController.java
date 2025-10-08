package com.promanage.api.controller;

import com.promanage.api.dto.request.CreateProjectRequest;
import com.promanage.api.dto.request.UpdateProjectRequest;
import com.promanage.api.dto.response.ProjectDetailResponse;
import com.promanage.api.dto.response.ProjectMemberResponse;
import com.promanage.api.dto.response.ProjectResponse;
import com.promanage.common.domain.PageResult;
import com.promanage.common.domain.Result;
import com.promanage.common.exception.BusinessException;
import com.promanage.infrastructure.utils.SecurityUtils;
import com.promanage.service.IProjectActivityService;
import com.promanage.service.IProjectService;
import com.promanage.service.service.IUserService;
import com.promanage.service.service.IDocumentService;
import com.promanage.service.service.ITaskService;
import com.promanage.service.service.IRoleService;
import com.promanage.dto.ProjectMemberDTO;
import com.promanage.dto.ProjectStatsDTO;
import com.promanage.service.entity.Project;
import com.promanage.service.entity.ProjectActivity;
import com.promanage.common.entity.User;
import com.promanage.service.entity.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@Tag(name = "项目管理", description = "项目管理相关接口")
@RequiredArgsConstructor
public class ProjectController {

    private final IProjectService projectService;
    private final IProjectActivityService projectActivityService;
    private final IUserService userService;
    private final IDocumentService documentService;
    private final ITaskService taskService;
    private final IRoleService roleService;

    

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
        if (projectService.lambdaQuery().eq(Project::getCode, request.getCode()).exists()) {
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

        projectService.save(project);
        Long projectId = project.getId();

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
        if (!projectService.isMemberOrAdmin(projectId, userId)) {
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
        if (!projectService.isAdmin(projectId, userId)) {
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

        projectService.updateById(project);

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
        if (!projectService.isAdmin(projectId, userId)) {
            throw new BusinessException("没有权限删除此项目");
        }

        projectService.removeById(projectId);

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
        if (!projectService.isMemberOrAdmin(projectId, userId)) {
            throw new BusinessException("没有权限查看此项目");
        }

        PageResult<ProjectMemberDTO> memberPage = projectService.listMembers(projectId, page, size, null);

        List<ProjectMemberResponse> memberResponses = memberPage.getList().stream()
                .map(this::convertToProjectMemberDTOToResponse)
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
        if (!projectService.isAdmin(projectId, currentUserId)) {
            throw new BusinessException("没有权限管理项目成员");
        }

        // 检查用户是否已存在
        if (projectService.isMemberOrAdmin(projectId, userId)) {
            throw new BusinessException("该用户已是项目成员");
        }

        projectService.addMember(projectId, userId, roleId);

        // 获取新添加的成员信息
        PageResult<ProjectMemberDTO> memberPage = projectService.listMembers(projectId, 1, 1, null);
        ProjectMemberDTO memberDTO = memberPage.getList().stream()
                .filter(m -> m.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
        ProjectMemberResponse response = convertToProjectMemberDTOToResponse(memberDTO);

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
        if (!projectService.isAdmin(projectId, currentUserId)) {
            throw new BusinessException("没有权限管理项目成员");
        }

        // 检查用户是否是成员
        if (!projectService.isMemberOrAdmin(projectId, userId)) {
            throw new BusinessException("该用户不是项目成员");
        }

        // 更新成员角色（使用删除再添加的方式，因为 ProjectMember 表中 roleId 是关键字段）
        projectService.removeMember(projectId, userId);
        projectService.addMember(projectId, userId, roleId);

        // 获取更新后的成员信息
        PageResult<ProjectMemberDTO> memberPage = projectService.listMembers(projectId, 1, 1, null);
        ProjectMemberDTO memberDTO = memberPage.getList().stream()
                .filter(m -> m.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
        ProjectMemberResponse response = convertToProjectMemberDTOToResponse(memberDTO);

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
        if (!projectService.isAdmin(projectId, currentUserId)) {
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

    /**
     * 获取项目成员列表
     *
     * @param projectId 项目ID
     * @param page 页码
     * @param size 每页大小
     * @return 成员列表
     */
    @GetMapping("/{id}/members")
    @Operation(summary = "获取项目成员列表", description = "获取项目成员列表，支持分页和按角色过滤")
    public Result<PageResult<ProjectMemberDTO>> getMembers(
            @Parameter(description = "项目ID") @PathVariable Long id,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "角色ID") @RequestParam(required = false) Long roleId) {
        return Result.success(projectService.listMembers(id, page, pageSize, roleId));
    }

    /**
     * 获取项目统计数据
     *
     * @param projectId 项目ID
     * @return 项目统计数据
     */
    @GetMapping("/{id}/stats")
    @Operation(summary = "获取项目统计数据", description = "获取项目的统计数据")
    public Result<ProjectStatsDTO> getProjectStats(@Parameter(description = "项目ID") @PathVariable Long id) {
        return Result.success(projectService.getProjectStats(id));
    }

    /**
     * 归档项目
     *
     * @param projectId 项目ID
     * @return 操作结果
     */
    @PostMapping("/{id}/archive")
    @Operation(summary = "归档项目", description = "归档项目")
    public Result<Void> archiveProject(@Parameter(description = "项目ID") @PathVariable Long id) {
        projectService.archive(id);
        return Result.success();
    }

    /**
     * 取消归档项目
     *
     * @param projectId 项目ID
     * @return 操作结果
     */
    @PostMapping("/{id}/unarchive")
    @Operation(summary = "取消归档项目", description = "取消归档项目")
    public Result<Void> unarchiveProject(@Parameter(description = "项目ID") @PathVariable Long id) {
        projectService.unarchive(id);
        return Result.success();
    }

    /**
     * 获取项目活动时间线
     *
     * @param projectId 项目ID
     * @param page 页码
     * @param pageSize 每页大小
     * @return 活动时间线
     */
    @GetMapping("/{id}/activities")
    @Operation(summary = "获取项目活动时间线", description = "获取项目的活动时间线")
    public Result<PageResult<ProjectActivity>> getProjectActivities(
            @Parameter(description = "项目ID") @PathVariable Long id,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(projectActivityService.getProjectActivities(id, page, pageSize));
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
                .memberCount(projectService.listMembers(project.getId(), 1, 1, null).getTotal().intValue())
                .documentCount(documentService.countByProjectId(project.getId()))
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
                .memberCount(projectService.listMembers(project.getId(), 1, 1, null).getTotal().intValue())
                .documentCount(documentService.countByProjectId(project.getId()))
                .taskCount(taskService.countTasksByProject(project.getId()))
                .createTime(project.getCreateTime())
                .updateTime(project.getUpdateTime())
                .build();
    }

    private ProjectMemberResponse convertToProjectMemberDTOToResponse(ProjectMemberDTO memberDTO) {
        if (memberDTO == null) {
            return null;
        }
        
        User user = userService.getById(memberDTO.getUserId());

        return ProjectMemberResponse.builder()
                .id(memberDTO.getId())
                .projectId(memberDTO.getProjectId())
                .userId(memberDTO.getUserId())
                .username(user != null ? user.getUsername() : "")
                .realName(user != null ? user.getRealName() : "未知")
                .avatar(user != null ? user.getAvatar() : null)
                .email(user != null ? user.getEmail() : null)
                .roleId(memberDTO.getRoleId())
                .roleName(getRoleName(memberDTO.getRoleId()))
                .roleCode(getRoleCode(memberDTO.getRoleId()))
                .joinedAt(memberDTO.getJoinTime())
                .status(memberDTO.getStatus())
                .build();
    }

    /**
     * 获取角色名称
     *
     * @param roleId 角色ID
     * @return 角色名称
     */
    private String getRoleName(Long roleId) {
        if (roleId == null) {
            return "未知角色";
        }
        try {
            Role role = roleService.getById(roleId);
            return role != null ? role.getRoleName() : "未知角色";
        } catch (Exception e) {
            log.warn("获取角色名称失败, roleId={}", roleId, e);
            return "未知角色";
        }
    }

    /**
     * 获取角色编码
     *
     * @param roleId 角色ID
     * @return 角色编码
     */
    private String getRoleCode(Long roleId) {
        if (roleId == null) {
            return "UNKNOWN";
        }
        try {
            Role role = roleService.getById(roleId);
            return role != null ? role.getRoleCode() : "UNKNOWN";
        } catch (Exception e) {
            log.warn("获取角色编码失败, roleId={}", roleId, e);
            return "UNKNOWN";
        }
    }
}