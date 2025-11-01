package com.promanage.api.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.web.bind.annotation.*;

import com.promanage.api.dto.request.ApproveChangeRequestRequest;
import com.promanage.api.dto.request.CreateChangeRequestRequest;
import com.promanage.api.dto.request.UpdateChangeRequestRequest;
import com.promanage.api.dto.response.ChangeRequestApprovalResponse;
import com.promanage.api.dto.response.ChangeRequestImpactResponse;
import com.promanage.api.dto.response.ChangeRequestResponse;
import com.promanage.common.domain.Result;
import com.promanage.common.entity.User;
import com.promanage.common.exception.BusinessException;
import com.promanage.common.result.PageResult;
import com.promanage.infrastructure.utils.SecurityUtils;
import com.promanage.service.entity.ChangeRequest;
import com.promanage.service.entity.ChangeRequestApproval;
import com.promanage.service.entity.ChangeRequestImpact;
import com.promanage.service.service.IChangeRequestService;
import com.promanage.service.service.IUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 变更请求管理控制器
 *
 * <p>提供变更请求的创建、查询、更新、删除以及审批管理功能
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-03
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@Tag(name = "变更请求管理", description = "变更请求创建、查询、更新、删除以及审批管理接口")
@RequiredArgsConstructor
public class ChangeRequestController {

  private final IChangeRequestService changeRequestService;
  private final IUserService userService;

  /**
   * 获取项目变更请求列表
   *
   * @param projectId 项目ID
   * @param page 页码
   * @param size 每页大小
   * @param status 变更请求状态
   * @param priority 变更请求优先级
   * @param impactLevel 影响程度
   * @param assigneeId 指派人ID
   * @param requesterId 请求人ID
   * @param reviewerId 审核人ID
   * @param keyword 关键词搜索（标题、描述）
   * @param tags 标签
   * @return 变更请求列表
   */
  @GetMapping("/projects/{projectId}/change-requests")
  @Operation(summary = "获取变更请求列表", description = "获取项目的变更请求列表")
  public Result<PageResult<ChangeRequestResponse>> getChangeRequests(
      @PathVariable Long projectId,
      @RequestParam(defaultValue = "1") Integer page,
      @RequestParam(defaultValue = "20") Integer size,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Integer priority,
      @RequestParam(required = false) String impactLevel,
      @RequestParam(required = false) Long assigneeId,
      @RequestParam(required = false) Long requesterId,
      @RequestParam(required = false) Long reviewerId,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String tags) {
    Long userId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new BusinessException("请先登录"));

    log.info(
        "获取变更请求列表请求, projectId={}, userId={}, page={}, size={}, status={}, priority={}, impactLevel={}, assigneeId={}, requesterId={}, reviewerId={}, keyword={}, tags={}",
        projectId,
        userId,
        page,
        size,
        status,
        priority,
        impactLevel,
        assigneeId,
        requesterId,
        reviewerId,
        keyword,
        tags);

    // 检查权限
    if (!changeRequestService.hasChangeRequestViewPermission(projectId, userId)) {
      throw new BusinessException("没有权限查看此项目的变更请求");
    }

    PageResult<ChangeRequest> changeRequestPage =
        changeRequestService.listChangeRequests(
            projectId,
            page,
            size,
            status,
            priority,
            impactLevel,
            assigneeId,
            requesterId,
            reviewerId,
            keyword,
            tags);

    // 收集所有用户ID
    Set<Long> userIds = collectUserIds(changeRequestPage.getList());

    // 批量获取用户
    Map<Long, User> userMap =
        userIds.isEmpty()
            ? Collections.emptyMap()
            : userService.getByIds(new ArrayList<>(userIds));

    // 转换DTO（传入userMap）
    List<ChangeRequestResponse> changeRequestResponses =
        changeRequestPage.getList().stream()
            .map(cr -> convertToChangeRequestResponse(cr, userMap))
            .collect(Collectors.toList());

    PageResult<ChangeRequestResponse> response =
        PageResult.of(
            changeRequestResponses,
            changeRequestPage.getTotal(),
            changeRequestPage.getPage(),
            changeRequestPage.getPageSize());

    log.info("获取变更请求列表成功, projectId={}, total={}", projectId, response.getTotal());
    return Result.success(response);
  }

  /**
   * 创建变更请求
   *
   * @param projectId 项目ID
   * @param request 创建变更请求请求
   * @return 创建的变更请求信息
   */
  @PostMapping("/projects/{projectId}/change-requests")
  @Operation(summary = "创建变更请求", description = "创建新的变更请求")
  public Result<ChangeRequestResponse> createChangeRequest(
      @PathVariable Long projectId, @Valid @RequestBody CreateChangeRequestRequest request) {
    Long userId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new BusinessException("请先登录"));

    log.info("创建变更请求请求, projectId={}, userId={}, title={}", projectId, userId, request.getTitle());

    // 检查权限
    if (!changeRequestService.hasChangeRequestPermission(projectId, userId)) {
      throw new BusinessException("没有权限在此项目中创建变更请求");
    }

    ChangeRequest changeRequest = new ChangeRequest();
    changeRequest.setTitle(request.getTitle());
    changeRequest.setDescription(request.getDescription());
    changeRequest.setReason(request.getReason());
    changeRequest.setStatus("DRAFT"); // 默认状态：草稿
    changeRequest.setPriority(request.getPriority());
    changeRequest.setImpactLevel(request.getImpactLevel());
    changeRequest.setRequesterId(userId);
    changeRequest.setAssigneeId(request.getAssigneeId());
    changeRequest.setReviewerId(request.getReviewerId());
    changeRequest.setEstimatedEffort(request.getEstimatedEffort());
    changeRequest.setImplementationDate(request.getImplementationDate());
    changeRequest.setTags(request.getTags());
    changeRequest.setProjectId(projectId);

    Long changeRequestId = changeRequestService.createChangeRequest(changeRequest);

    ChangeRequest createdChangeRequest = changeRequestService.getChangeRequestById(changeRequestId);
    
    // 收集用户ID
    Set<Long> userIds = collectUserIds(List.of(createdChangeRequest));

    // 批量获取用户
    Map<Long, User> userMap =
        userIds.isEmpty()
            ? Collections.emptyMap()
            : userService.getByIds(new ArrayList<>(userIds));

    // 转换DTO（传入userMap）
    ChangeRequestResponse response = convertToChangeRequestResponse(createdChangeRequest, userMap);

    log.info("变更请求创建成功, changeRequestId={}, title={}", changeRequestId, request.getTitle());
    return Result.success(response);
  }

  /**
   * 获取变更请求详情
   *
   * @param changeRequestId 变更请求ID
   * @return 变更请求详情
   */
  @GetMapping("/change-requests/{changeRequestId}")
  @Operation(summary = "获取变更请求详情", description = "获取变更请求的详细信息")
  public Result<ChangeRequestResponse> getChangeRequest(@PathVariable Long changeRequestId) {
    Long userId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new BusinessException("请先登录"));

    log.info("获取变更请求详情请求, changeRequestId={}, userId={}", changeRequestId, userId);

    // 检查权限
    if (!changeRequestService.hasChangeRequestViewPermission(changeRequestId, userId)) {
      throw new BusinessException("没有权限查看此变更请求");
    }

    ChangeRequest changeRequest = changeRequestService.getChangeRequestById(changeRequestId);
    if (changeRequest == null) {
      throw new BusinessException("变更请求不存在");
    }

    // 收集用户ID
    Set<Long> userIds = collectUserIds(List.of(changeRequest));

    // 批量获取用户
    Map<Long, User> userMap =
        userIds.isEmpty()
            ? Collections.emptyMap()
            : userService.getByIds(new ArrayList<>(userIds));

    // 转换DTO（传入userMap）
    ChangeRequestResponse response = convertToChangeRequestResponse(changeRequest, userMap);

    log.info("获取变更请求详情成功, changeRequestId={}", changeRequestId);
    return Result.success(response);
  }

  /**
   * 更新变更请求
   *
   * @param changeRequestId 变更请求ID
   * @param request 更新变更请求请求
   * @return 更新后的变更请求信息
   */
  @PutMapping("/change-requests/{changeRequestId}")
  @Operation(summary = "更新变更请求", description = "更新变更请求信息")
  public Result<ChangeRequestResponse> updateChangeRequest(
      @PathVariable Long changeRequestId, @Valid @RequestBody UpdateChangeRequestRequest request) {
    Long userId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new BusinessException("请先登录"));

    log.info("更新变更请求请求, changeRequestId={}, userId={}", changeRequestId, userId);

    // 检查权限
    if (!changeRequestService.hasChangeRequestPermission(changeRequestId, userId)) {
      throw new BusinessException("没有权限编辑此变更请求");
    }

    ChangeRequest changeRequest = changeRequestService.getChangeRequestById(changeRequestId);
    if (changeRequest == null) {
      throw new BusinessException("变更请求不存在");
    }

    // 只能编辑草稿状态的变更请求
    if (!"DRAFT".equals(changeRequest.getStatus())) {
      throw new BusinessException("只能编辑草稿状态的变更请求");
    }

    // 更新变更请求信息
    if (request.getTitle() != null) {
      changeRequest.setTitle(request.getTitle());
    }
    if (request.getDescription() != null) {
      changeRequest.setDescription(request.getDescription());
    }
    if (request.getReason() != null) {
      changeRequest.setReason(request.getReason());
    }
    if (request.getPriority() != null) {
      changeRequest.setPriority(request.getPriority());
    }
    if (request.getImpactLevel() != null) {
      changeRequest.setImpactLevel(request.getImpactLevel());
    }
    if (request.getAssigneeId() != null) {
      changeRequest.setAssigneeId(request.getAssigneeId());
    }
    if (request.getReviewerId() != null) {
      changeRequest.setReviewerId(request.getReviewerId());
    }
    if (request.getEstimatedEffort() != null) {
      changeRequest.setEstimatedEffort(request.getEstimatedEffort());
    }
    if (request.getActualEffort() != null) {
      changeRequest.setActualEffort(request.getActualEffort());
    }
    if (request.getImplementationDate() != null) {
      changeRequest.setImplementationDate(request.getImplementationDate());
    }
    if (request.getTags() != null) {
      changeRequest.setTags(request.getTags());
    }

    changeRequestService.updateChangeRequest(changeRequest);

    ChangeRequest updatedChangeRequest = changeRequestService.getChangeRequestById(changeRequestId);
    
    // 收集用户ID
    Set<Long> userIds = collectUserIds(List.of(updatedChangeRequest));

    // 批量获取用户
    Map<Long, User> userMap =
        userIds.isEmpty()
            ? Collections.emptyMap()
            : userService.getByIds(new ArrayList<>(userIds));

    // 转换DTO（传入userMap）
    ChangeRequestResponse response = convertToChangeRequestResponse(updatedChangeRequest, userMap);

    log.info("更新变更请求成功, changeRequestId={}", changeRequestId);
    return Result.success(response);
  }

  /**
   * 提交变更请求
   *
   * @param changeRequestId 变更请求ID
   * @return 操作结果
   */
  @PostMapping("/change-requests/{changeRequestId}/submit")
  @Operation(summary = "提交变更请求", description = "将草稿状态的变更请求提交审批")
  public Result<Void> submitChangeRequest(@PathVariable Long changeRequestId) {
    Long userId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new BusinessException("请先登录"));

    log.info("提交变更请求请求, changeRequestId={}, userId={}", changeRequestId, userId);

    // 检查权限
    if (!changeRequestService.hasChangeRequestPermission(changeRequestId, userId)) {
      throw new BusinessException("没有权限提交此变更请求");
    }

    changeRequestService.submitChangeRequest(changeRequestId, userId);

    log.info("提交变更请求成功, changeRequestId={}", changeRequestId);
    return Result.success();
  }

  /**
   * 审批变更请求
   *
   * @param changeRequestId 变更请求ID
   * @param request 审批请求
   * @return 操作结果
   */
  @PostMapping("/change-requests/{changeRequestId}/approve")
  @Operation(summary = "审批变更请求", description = "审批或拒绝变更请求")
  public Result<Void> approveChangeRequest(
      @PathVariable Long changeRequestId, @Valid @RequestBody ApproveChangeRequestRequest request) {
    Long userId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new BusinessException("请先登录"));

    log.info(
        "审批变更请求请求, changeRequestId={}, userId={}, decision={}",
        changeRequestId,
        userId,
        request.getDecision());

    // 检查权限
    if (!changeRequestService.canApproveChangeRequest(changeRequestId, userId)) {
      throw new BusinessException("没有权限审批此变更请求");
    }

    changeRequestService.approveChangeRequest(
        changeRequestId, request.getDecision(), request.getComments(), userId);

    log.info("审批变更请求成功, changeRequestId={}, decision={}", changeRequestId, request.getDecision());
    return Result.success();
  }

  /**
   * 获取变更请求影响分析结果
   *
   * @param changeRequestId 变更请求ID
   * @return 影响分析结果列表
   */
  @GetMapping("/change-requests/{changeRequestId}/impact-analysis")
  @Operation(summary = "获取影响分析", description = "获取变更请求的影响分析结果")
  public Result<List<ChangeRequestImpactResponse>> getChangeRequestImpactAnalysis(
      @PathVariable Long changeRequestId) {
    Long userId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new BusinessException("请先登录"));

    log.info("获取变更请求影响分析请求, changeRequestId={}, userId={}", changeRequestId, userId);

    // 检查权限
    if (!changeRequestService.hasChangeRequestViewPermission(changeRequestId, userId)) {
      throw new BusinessException("没有权限查看此变更请求的影响分析");
    }

    List<ChangeRequestImpact> impacts =
        changeRequestService.getChangeRequestImpacts(changeRequestId);

    // 收集所有用户ID
    Set<Long> userIds = collectUserIdsFromImpacts(impacts);

    // 批量获取用户
    Map<Long, User> userMap =
        userIds.isEmpty()
            ? Collections.emptyMap()
            : userService.getByIds(new ArrayList<>(userIds));

    // 转换DTO（传入userMap）
    List<ChangeRequestImpactResponse> response =
        impacts.stream()
            .map(impact -> convertToImpactResponse(impact, userMap))
            .collect(Collectors.toList());

    log.info("获取变更请求影响分析成功, changeRequestId={}, impactCount={}", changeRequestId, response.size());
    return Result.success(response);
  }

  /**
   * 执行影响分析
   *
   * @param changeRequestId 变更请求ID
   * @param forceRefresh 是否强制重新分析
   * @return 影响分析结果列表
   */
  @PostMapping("/change-requests/{changeRequestId}/impact-analysis")
  @Operation(summary = "执行影响分析", description = "对变更请求执行智能影响分析")
  public Result<List<ChangeRequestImpactResponse>> analyzeChangeRequestImpact(
      @PathVariable Long changeRequestId,
      @RequestParam(defaultValue = "false") boolean forceRefresh) {
    Long userId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new BusinessException("请先登录"));

    log.info(
        "执行变更请求影响分析请求, changeRequestId={}, userId={}, forceRefresh={}",
        changeRequestId,
        userId,
        forceRefresh);

    // 检查权限
    if (!changeRequestService.hasChangeRequestPermission(changeRequestId, userId)) {
      throw new BusinessException("没有权限执行此变更请求的影响分析");
    }

    List<ChangeRequestImpact> impacts =
        changeRequestService.analyzeChangeRequestImpact(changeRequestId, forceRefresh);

    // 收集所有用户ID
    Set<Long> userIds = collectUserIdsFromImpacts(impacts);

    // 批量获取用户
    Map<Long, User> userMap =
        userIds.isEmpty()
            ? Collections.emptyMap()
            : userService.getByIds(new ArrayList<>(userIds));

    // 转换DTO（传入userMap）
    List<ChangeRequestImpactResponse> response =
        impacts.stream()
            .map(impact -> convertToImpactResponse(impact, userMap))
            .collect(Collectors.toList());

    log.info("执行变更请求影响分析成功, changeRequestId={}, impactCount={}", changeRequestId, response.size());
    return Result.success(response);
  }

  /**
   * 获取变更请求审批历史
   *
   * @param changeRequestId 变更请求ID
   * @return 审批历史列表
   */
  @GetMapping("/change-requests/{changeRequestId}/approvals")
  @Operation(summary = "获取审批历史", description = "获取变更请求的审批历史记录")
  public Result<List<ChangeRequestApprovalResponse>> getChangeRequestApprovalHistory(
      @PathVariable Long changeRequestId) {
    Long userId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new BusinessException("请先登录"));

    log.info("获取变更请求审批历史请求, changeRequestId={}, userId={}", changeRequestId, userId);

    // 检查权限
    if (!changeRequestService.hasChangeRequestViewPermission(changeRequestId, userId)) {
      throw new BusinessException("没有权限查看此变更请求的审批历史");
    }

    List<ChangeRequestApproval> approvals =
        changeRequestService.getChangeRequestApprovalHistory(changeRequestId);

    // 收集所有用户ID
    Set<Long> userIds = collectUserIdsFromApprovals(approvals);

    // 批量获取用户
    Map<Long, User> userMap =
        userIds.isEmpty()
            ? Collections.emptyMap()
            : userService.getByIds(new ArrayList<>(userIds));

    // 转换DTO（传入userMap）
    List<ChangeRequestApprovalResponse> response =
        approvals.stream()
            .map(approval -> convertToApprovalResponse(approval, userMap))
            .collect(Collectors.toList());

    log.info(
        "获取变更请求审批历史成功, changeRequestId={}, approvalCount={}", changeRequestId, response.size());
    return Result.success(response);
  }

  // 辅助方法

  /**
   * 从变更请求列表中收集所有关联的用户ID
   *
   * @param changeRequests 变更请求列表
   * @return 去重后的用户ID集合
   */
  Set<Long> collectUserIds(List<ChangeRequest> changeRequests) {
    if (changeRequests == null || changeRequests.isEmpty()) {
      return Collections.emptySet();
    }
    return changeRequests.stream()
        .flatMap(
            cr ->
                Stream.of(cr.getRequesterId(), cr.getAssigneeId(), cr.getReviewerId())
                    .filter(Objects::nonNull))
        .collect(Collectors.toSet());
  }

  /**
   * 从影响分析列表中收集所有关联的用户ID
   *
   * @param impacts 影响分析列表
   * @return 去重后的用户ID集合
   */
  Set<Long> collectUserIdsFromImpacts(List<ChangeRequestImpact> impacts) {
    if (impacts == null || impacts.isEmpty()) {
      return Collections.emptySet();
    }
    return impacts.stream()
        .map(ChangeRequestImpact::getVerifiedBy)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }

  /**
   * 从审批历史列表中收集所有关联的用户ID
   *
   * @param approvals 审批历史列表
   * @return 去重后的用户ID集合
   */
  Set<Long> collectUserIdsFromApprovals(List<ChangeRequestApproval> approvals) {
    if (approvals == null || approvals.isEmpty()) {
      return Collections.emptySet();
    }
    return approvals.stream()
        .map(ChangeRequestApproval::getApproverId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }

  ChangeRequestResponse convertToChangeRequestResponse(
      ChangeRequest changeRequest, Map<Long, User> userMap) {
    User requester =
        Optional.ofNullable(changeRequest.getRequesterId())
            .map(userMap::get)
            .orElse(null);
    User assignee =
        Optional.ofNullable(changeRequest.getAssigneeId())
            .map(userMap::get)
            .orElse(null);
    User reviewer =
        Optional.ofNullable(changeRequest.getReviewerId())
            .map(userMap::get)
            .orElse(null);

    return ChangeRequestResponse.builder()
        .id(changeRequest.getId())
        .title(changeRequest.getTitle())
        .description(changeRequest.getDescription())
        .reason(changeRequest.getReason())
        .status(changeRequest.getStatus())
        .priority(changeRequest.getPriority())
        .impactLevel(changeRequest.getImpactLevel())
        .requesterId(changeRequest.getRequesterId())
        .requesterName(requester != null ? requester.getRealName() : null)
        .requesterAvatar(requester != null ? requester.getAvatar() : null)
        .assigneeId(changeRequest.getAssigneeId())
        .assigneeName(assignee != null ? assignee.getRealName() : null)
        .assigneeAvatar(assignee != null ? assignee.getAvatar() : null)
        .reviewerId(changeRequest.getReviewerId())
        .reviewerName(reviewer != null ? reviewer.getRealName() : null)
        .reviewerAvatar(reviewer != null ? reviewer.getAvatar() : null)
        .projectId(changeRequest.getProjectId())
        .estimatedEffort(changeRequest.getEstimatedEffort())
        .actualEffort(changeRequest.getActualEffort())
        .implementationDate(changeRequest.getImplementationDate())
        .tags(changeRequest.getTags())
        .submittedAt(changeRequest.getSubmittedAt())
        .approvedAt(changeRequest.getApprovedAt())
        .implementedAt(changeRequest.getImplementedAt())
        .createTime(changeRequest.getCreateTime())
        .updateTime(changeRequest.getUpdateTime())
        .commentCount(changeRequestService.getChangeRequestCommentCount(changeRequest.getId()))
        .impactCount(changeRequestService.getChangeRequestImpactCount(changeRequest.getId()))
        .build();
  }

  ChangeRequestImpactResponse convertToImpactResponse(
      ChangeRequestImpact impact, Map<Long, User> userMap) {
    String verifiedByName =
        Optional.ofNullable(impact.getVerifiedBy())
            .map(userMap::get)
            .map(User::getRealName)
            .orElse(null);

    return ChangeRequestImpactResponse.builder()
        .id(impact.getId())
        .entityType(impact.getEntityType())
        .entityId(impact.getEntityId())
        .entityTitle(impact.getEntityTitle())
        .impactLevel(impact.getImpactLevel())
        .impactDescription(impact.getImpactDescription())
        .confidenceScore(impact.getConfidenceScore())
        .isVerified(impact.getIsVerified())
        .verifiedBy(verifiedByName)
        .verifiedAt(impact.getVerifiedAt())
        .build();
  }

  ChangeRequestApprovalResponse convertToApprovalResponse(
      ChangeRequestApproval approval, Map<Long, User> userMap) {
    User approver =
        Optional.ofNullable(approval.getApproverId())
            .map(userMap::get)
            .orElse(null);

    return ChangeRequestApprovalResponse.builder()
        .id(approval.getId())
        .changeRequestId(approval.getChangeRequestId())
        .approverId(approval.getApproverId())
        .approverName(approver != null ? approver.getRealName() : approval.getApproverName())
        .approverAvatar(approver != null ? approver.getAvatar() : null)
        .approvalStep(approval.getApprovalStep())
        .approvalLevel(approval.getApprovalLevel())
        .status(approval.getStatus())
        .comments(approval.getComments())
        .approvedAt(approval.getApprovedAt())
        .createTime(approval.getCreateTime())
        .updateTime(approval.getUpdateTime())
        .build();
  }
}
