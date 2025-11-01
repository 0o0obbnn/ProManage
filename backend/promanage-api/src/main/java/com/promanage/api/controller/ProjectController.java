package com.promanage.api.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.promanage.api.dto.request.AddProjectMemberRequest;
import com.promanage.api.dto.request.CreateProjectRequest;
import com.promanage.api.dto.request.UpdateProjectRequest;
import com.promanage.common.domain.Result;
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
import com.promanage.service.IProjectService;
import com.promanage.service.entity.Project;
import com.promanage.service.mapper.ProjectDtoMapper;
import com.promanage.service.service.IUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/projects")
@Tag(name = "项目管理", description = "项目管理相关接口")
@RequiredArgsConstructor
public class ProjectController {

  private final IProjectService projectService;
  private final IUserService userService;
  private final ProjectDtoMapper projectDtoMapper;

  @GetMapping
  @Operation(summary = "分页查询项目", description = "根据权限返回可访问的项目列表")
  public Result<PageResult<ProjectDTO>> listProjects(
      @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
      @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize,
      @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
      @Parameter(description = "项目状态") @RequestParam(required = false) Integer status,
      @Parameter(description = "组织ID，仅超级管理员可指定") @RequestParam(required = false)
          Long organizationId) {

    Long userId =
        SecurityUtils.getCurrentUserId()
            .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "请先登录"));
    PageResult<ProjectDTO> result =
        projectService.listProjects(userId, page, pageSize, keyword, status, organizationId);
    return Result.success(result);
  }

  @GetMapping("/{projectId}")
  @Operation(summary = "获取项目详情", description = "返回指定项目的详细信息")
  public Result<ProjectDTO> getProject(@PathVariable Long projectId) {
    Long userId =
        SecurityUtils.getCurrentUserId()
            .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "请先登录"));
    ProjectDTO dto = projectService.getProjectDtoById(projectId, userId);
    return Result.success(dto);
  }

  @PostMapping
  @Operation(summary = "创建项目", description = "创建新的项目")
  public Result<ProjectDTO> createProject(@Valid @RequestBody CreateProjectRequest request) {
    Long userId =
        SecurityUtils.getCurrentUserId()
            .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "请先登录"));

    CreateProjectRequestDTO dto =
        CreateProjectRequestDTO.builder()
            .name(request.getName())
            .code(request.getCode())
            .description(request.getDescription())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .priority(request.getPriority())
            .type(request.getType())
            .color(request.getColor())
            .icon(request.getIcon())
            .ownerId(userId)
            .status(1)
            .build();

    Project project = projectService.createProject(dto, userId);
    ProjectDTO response = projectDtoMapper.toDto(project);
    // 批量获取owner信息
    if (project.getOwnerId() != null) {
      Map<Long, User> userMap = userService.getByIds(List.of(project.getOwnerId()));
      User owner = userMap.get(project.getOwnerId());
      if (owner != null) {
        response.setOwnerName(owner.getRealName());
      }
    }
    return Result.success(response);
  }

  @PutMapping("/{projectId}")
  @Operation(summary = "更新项目", description = "更新项目基础信息")
  public Result<ProjectDTO> updateProject(
      @PathVariable Long projectId, @Valid @RequestBody UpdateProjectRequest request) {
    Long userId =
        SecurityUtils.getCurrentUserId()
            .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "请先登录"));

    UpdateProjectRequestDTO dto =
        UpdateProjectRequestDTO.builder()
            .name(request.getName())
            .description(request.getDescription())
            .status(request.getStatus())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .icon(request.getIcon())
            .color(request.getColor())
            .type(request.getType())
            .priority(request.getPriority())
            .progress(request.getProgress())
            .build();

    Project project = projectService.updateProject(projectId, dto, userId);
    ProjectDTO response = projectDtoMapper.toDto(project);
    // 批量获取owner信息
    if (project.getOwnerId() != null) {
      Map<Long, User> userMap = userService.getByIds(List.of(project.getOwnerId()));
      User owner = userMap.get(project.getOwnerId());
      Optional.ofNullable(owner).ifPresent(value -> response.setOwnerName(value.getRealName()));
    }
    return Result.success(response);
  }

  @DeleteMapping("/{projectId}")
  @Operation(summary = "删除项目", description = "逻辑删除项目")
  public Result<Void> deleteProject(@PathVariable Long projectId) {
    Long userId =
        SecurityUtils.getCurrentUserId()
            .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "请先登录"));
    projectService.deleteProject(projectId, userId);
    return Result.success();
  }

  @GetMapping("/{projectId}/members")
  @Operation(summary = "分页获取项目成员", description = "根据角色筛选项目成员")
  public Result<PageResult<ProjectMemberDTO>> listProjectMembers(
      @PathVariable Long projectId,
      @RequestParam(defaultValue = "1") Integer page,
      @RequestParam(defaultValue = "20") Integer pageSize,
      @RequestParam(required = false) Long roleId) {
    Long userId =
        SecurityUtils.getCurrentUserId()
            .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "请先登录"));
    PageResult<ProjectMemberDTO> members =
        projectService.listProjectMembers(projectId, userId, page, pageSize, roleId);
    return Result.success(members);
  }

  @PostMapping("/{projectId}/members")
  @Operation(summary = "添加项目成员", description = "邀请用户加入项目")
  public Result<ProjectMemberDTO> addProjectMember(
      @PathVariable Long projectId, @Valid @RequestBody AddProjectMemberRequest request) {
    Long userId =
        SecurityUtils.getCurrentUserId()
            .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "请先登录"));
    ProjectMemberDTO member =
        projectService.addProjectMember(
            projectId, request.getUserId(), request.getRoleId(), userId);
    return Result.success(member);
  }

  @DeleteMapping("/{projectId}/members/{memberUserId}")
  @Operation(summary = "移除项目成员", description = "将指定用户从项目中移除")
  public Result<Void> removeProjectMember(
      @PathVariable Long projectId, @PathVariable Long memberUserId) {
    Long userId =
        SecurityUtils.getCurrentUserId()
            .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "请先登录"));
    projectService.removeProjectMember(projectId, memberUserId, userId);
    return Result.success();
  }

  @GetMapping("/{projectId}/stats")
  @Operation(summary = "获取项目统计数据", description = "返回任务、文档、成员等统计信息")
  public Result<ProjectStatsDTO> getProjectStats(@PathVariable Long projectId) {
    Long userId =
        SecurityUtils.getCurrentUserId()
            .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "请先登录"));
    return Result.success(projectService.getProjectStats(projectId, userId));
  }
}
