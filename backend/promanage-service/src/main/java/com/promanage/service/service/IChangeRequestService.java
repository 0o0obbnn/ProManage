package com.promanage.service.service;

import java.util.List;

import com.promanage.common.result.PageResult;
import com.promanage.service.entity.ChangeRequest;
import com.promanage.service.entity.ChangeRequestApproval;
import com.promanage.service.entity.ChangeRequestImpact;

/**
 * 变更请求服务接口
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-03
 */
public interface IChangeRequestService {

  /**
   * 创建变更请求
   *
   * @param changeRequest 变更请求实体
   * @return 变更请求ID
   */
  Long createChangeRequest(ChangeRequest changeRequest);

  /**
   * 根据ID获取变更请求
   *
   * @param changeRequestId 变更请求ID
   * @return 变更请求实体
   */
  ChangeRequest getChangeRequestById(Long changeRequestId);

  /**
   * 更新变更请求
   *
   * @param changeRequest 变更请求实体
   */
  void updateChangeRequest(ChangeRequest changeRequest);

  /**
   * 删除变更请求（软删除）
   *
   * @param changeRequestId 变更请求ID
   * @param userId 操作人ID
   */
  void deleteChangeRequest(Long changeRequestId, Long userId);

  /**
   * 获取项目变更请求列表
   *
   * @param projectId 项目ID
   * @param page 页码
   * @param size 每页大小
   * @param status 状态（可选）
   * @param priority 优先级（可选）
   * @param impactLevel 影响程度（可选）
   * @param assigneeId 指派人ID（可选）
   * @param requesterId 请求人ID（可选）
   * @param reviewerId 审核人ID（可选）
   * @param keyword 关键词搜索（可选，搜索标题和描述）
   * @param tags 标签（可选）
   * @return 分页结果
   */
  PageResult<ChangeRequest> listChangeRequests(
      Long projectId,
      Integer page,
      Integer size,
      String status,
      Integer priority,
      String impactLevel,
      Long assigneeId,
      Long requesterId,
      Long reviewerId,
      String keyword,
      String tags);

  /**
   * 提交变更请求
   *
   * @param changeRequestId 变更请求ID
   * @param userId 操作人ID
   */
  void submitChangeRequest(Long changeRequestId, Long userId);

  /**
   * 审批变更请求
   *
   * @param changeRequestId 变更请求ID
   * @param decision 审批决定（APPROVED/REJECTED）
   * @param comments 审批意见
   * @param userId 审批人ID
   */
  void approveChangeRequest(Long changeRequestId, String decision, String comments, Long userId);

  /**
   * 执行影响分析
   *
   * @param changeRequestId 变更请求ID
   * @param forceRefresh 是否强制重新分析
   * @return 影响分析结果列表
   */
  List<ChangeRequestImpact> analyzeChangeRequestImpact(Long changeRequestId, boolean forceRefresh);

  /**
   * 获取变更请求的影响分析结果
   *
   * @param changeRequestId 变更请求ID
   * @return 影响分析结果列表
   */
  List<ChangeRequestImpact> getChangeRequestImpacts(Long changeRequestId);

  /**
   * 验证影响分析结果
   *
   * @param impactId 影响ID
   * @param isValid 是否有效
   * @param userId 验证人ID
   */
  void validateImpactAnalysis(Long impactId, boolean isValid, Long userId);

  /**
   * 获取用户的变更请求列表
   *
   * @param userId 用户ID
   * @param page 页码
   * @param size 每页大小
   * @param status 状态（可选）
   * @return 分页结果
   */
  PageResult<ChangeRequest> listChangeRequestsByUser(
      Long userId, Integer page, Integer size, String status);

  /**
   * 获取待审批的变更请求列表
   *
   * @param reviewerId 审核人ID
   * @param page 页码
   * @param size 每页大小
   * @return 分页结果
   */
  PageResult<ChangeRequest> listPendingApprovalChangeRequests(
      Long reviewerId, Integer page, Integer size);

  /**
   * 统计项目变更请求数量
   *
   * @param projectId 项目ID
   * @return 变更请求数量
   */
  int countChangeRequestsByProject(Long projectId);

  /**
   * 统计用户创建的变更请求数量
   *
   * @param userId 用户ID
   * @param status 状态（可选）
   * @return 变更请求数量
   */
  int countChangeRequestsByUser(Long userId, String status);

  /**
   * 统计待审批的变更请求数量
   *
   * @param reviewerId 审核人ID
   * @return 待审批数量
   */
  int countPendingApprovalChangeRequests(Long reviewerId);

  /**
   * 检查用户是否有权限操作变更请求
   *
   * @param changeRequestId 变更请求ID
   * @param userId 用户ID
   * @return 是否有权限
   */
  boolean hasChangeRequestPermission(Long changeRequestId, Long userId);

  /**
   * 检查用户是否有权限查看变更请求
   *
   * @param changeRequestId 变更请求ID
   * @param userId 用户ID
   * @return 是否有权限
   */
  boolean hasChangeRequestViewPermission(Long changeRequestId, Long userId);

  /**
   * 检查用户是否可以审批变更请求
   *
   * @param changeRequestId 变更请求ID
   * @param userId 用户ID
   * @return 是否可以审批
   */
  boolean canApproveChangeRequest(Long changeRequestId, Long userId);

  /**
   * 实施变更请求
   *
   * @param changeRequestId 变更请求ID
   * @param userId 实施人ID
   */
  void implementChangeRequest(Long changeRequestId, Long userId);

  /**
   * 关闭变更请求
   *
   * @param changeRequestId 变更请求ID
   * @param userId 操作人ID
   */
  void closeChangeRequest(Long changeRequestId, Long userId);

  /**
   * 重新打开变更请求
   *
   * @param changeRequestId 变更请求ID
   * @param userId 操作人ID
   */
  void reopenChangeRequest(Long changeRequestId, Long userId);

  /**
   * 获取变更请求审批历史
   *
   * @param changeRequestId 变更请求ID
   * @return 审批历史列表
   */
  List<ChangeRequestApproval> getChangeRequestApprovalHistory(Long changeRequestId);

  /**
   * 获取变更请求评论数量
   *
   * @param changeRequestId 变更请求ID
   * @return 评论数量
   */
  int getChangeRequestCommentCount(Long changeRequestId);

  /**
   * 获取变更请求影响分析数量
   *
   * @param changeRequestId 变更请求ID
   * @return 影响分析数量
   */
  int getChangeRequestImpactCount(Long changeRequestId);

  /**
   * 批量更新变更请求状态
   *
   * @param changeRequestIds 变更请求ID列表
   * @param status 新状态
   * @param userId 操作人ID
   * @return 批量操作结果
   */
  com.promanage.common.domain.BatchOperationResult<Long> batchUpdateChangeRequestStatus(
      List<Long> changeRequestIds, String status, Long userId);

  /**
   * 获取项目变更请求统计信息
   *
   * @param projectId 项目ID
   * @return 统计信息
   */
  ChangeRequestStatistics getChangeRequestStatistics(Long projectId);

  /** 变更请求统计信息 */
  class ChangeRequestStatistics {
    private int totalCount;
    private int draftCount;
    private int submittedCount;
    private int underReviewCount;
    private int approvedCount;
    private int rejectedCount;
    private int implementedCount;
    private int closedCount;

    // Getters and setters
    public int getTotalCount() {
      return totalCount;
    }

    public void setTotalCount(int totalCount) {
      this.totalCount = totalCount;
    }

    public int getDraftCount() {
      return draftCount;
    }

    public void setDraftCount(int draftCount) {
      this.draftCount = draftCount;
    }

    public int getSubmittedCount() {
      return submittedCount;
    }

    public void setSubmittedCount(int submittedCount) {
      this.submittedCount = submittedCount;
    }

    public int getUnderReviewCount() {
      return underReviewCount;
    }

    public void setUnderReviewCount(int underReviewCount) {
      this.underReviewCount = underReviewCount;
    }

    public int getApprovedCount() {
      return approvedCount;
    }

    public void setApprovedCount(int approvedCount) {
      this.approvedCount = approvedCount;
    }

    public int getRejectedCount() {
      return rejectedCount;
    }

    public void setRejectedCount(int rejectedCount) {
      this.rejectedCount = rejectedCount;
    }

    public int getImplementedCount() {
      return implementedCount;
    }

    public void setImplementedCount(int implementedCount) {
      this.implementedCount = implementedCount;
    }

    public int getClosedCount() {
      return closedCount;
    }

    public void setClosedCount(int closedCount) {
      this.closedCount = closedCount;
    }
  }
}
