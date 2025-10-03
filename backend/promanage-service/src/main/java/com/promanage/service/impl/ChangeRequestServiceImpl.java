package com.promanage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.promanage.common.domain.PageResult;
import com.promanage.common.domain.ResultCode;
import com.promanage.common.exception.BusinessException;
import com.promanage.infrastructure.security.SecurityUtils;
import com.promanage.service.entity.ChangeRequest;
import com.promanage.service.entity.ChangeRequestImpact;
import com.promanage.service.mapper.ChangeRequestMapper;
import com.promanage.service.mapper.ChangeRequestImpactMapper;
import com.promanage.service.service.IChangeRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 变更请求服务实现类
 * <p>
 * 实现变更请求管理的所有业务逻辑，包括变更请求CRUD操作、审批流程和影响分析。
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChangeRequestServiceImpl implements IChangeRequestService {

    private final ChangeRequestMapper changeRequestMapper;
    private final ChangeRequestImpactMapper changeRequestImpactMapper;
    private final ProjectServiceImpl projectService;

    /**
     * 变更请求状态
     */
    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_SUBMITTED = "SUBMITTED";
    private static final String STATUS_UNDER_REVIEW = "UNDER_REVIEW";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String STATUS_IMPLEMENTED = "IMPLEMENTED";
    private static final String STATUS_CLOSED = "CLOSED";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createChangeRequest(ChangeRequest changeRequest) {
        log.info("创建变更请求, title={}, projectId={}", changeRequest.getTitle(), changeRequest.getProjectId());

        // 参数验证
        if (changeRequest == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "变更请求信息不能为空");
        }
        if (changeRequest.getTitle() == null || changeRequest.getTitle().trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "变更请求标题不能为空");
        }
        if (changeRequest.getProjectId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "项目ID不能为空");
        }

        // 设置默认值
        if (changeRequest.getStatus() == null) {
            changeRequest.setStatus(STATUS_DRAFT);
        }
        if (changeRequest.getPriority() == null) {
            changeRequest.setPriority(2); // 默认中等优先级
        }

        // 设置创建信息
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "未登录"));
        changeRequest.setRequesterId(currentUserId);
        changeRequest.setCreatorId(currentUserId);
        changeRequest.setUpdaterId(currentUserId);

        // 保存变更请求
        int result = changeRequestMapper.insert(changeRequest);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "创建变更请求失败");
        }

        log.info("变更请求创建成功, id={}, title={}", changeRequest.getId(), changeRequest.getTitle());
        return changeRequest.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateChangeRequest(ChangeRequest changeRequest) {
        log.info("更新变更请求, id={}, title={}", changeRequest.getId(), changeRequest.getTitle());

        if (changeRequest.getId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "变更请求ID不能为空");
        }

        // 检查变更请求是否存在
        ChangeRequest existing = getChangeRequestById(changeRequest.getId());
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "变更请求不存在");
        }

        // 检查状态是否允许更新（只有草稿状态可以更新）
        if (!STATUS_DRAFT.equals(existing.getStatus())) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "只有草稿状态的变更请求可以更新");
        }

        // 设置更新信息
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "未登录"));
        changeRequest.setUpdaterId(currentUserId);

        // 更新变更请求
        int result = changeRequestMapper.updateById(changeRequest);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "更新变更请求失败");
        }

        log.info("变更请求更新成功, id={}", changeRequest.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteChangeRequest(Long changeRequestId, Long userId) {
        log.info("删除变更请求, id={}, userId={}", changeRequestId, userId);

        if (changeRequestId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "变更请求ID不能为空");
        }

        // 检查变更请求是否存在
        ChangeRequest changeRequest = getChangeRequestById(changeRequestId);
        if (changeRequest == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "变更请求不存在");
        }

        // 检查权限（只有请求人或项目负责人可以删除）
        if (!hasChangeRequestPermission(changeRequestId, userId)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "没有权限删除此变更请求");
        }

        // 检查状态（只有草稿状态可以删除）
        if (!STATUS_DRAFT.equals(changeRequest.getStatus())) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "只有草稿状态的变更请求可以删除");
        }

        // 软删除
        changeRequest.setDeleted(true);
        changeRequest.setUpdaterId(userId);

        int result = changeRequestMapper.updateById(changeRequest);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "删除变更请求失败");
        }

        log.info("变更请求删除成功, id={}", changeRequestId);
    }

    @Override
    public ChangeRequest getChangeRequestById(Long changeRequestId) {
        if (changeRequestId == null) {
            return null;
        }

        log.debug("查询变更请求详情, id={}", changeRequestId);
        ChangeRequest changeRequest = changeRequestMapper.selectById(changeRequestId);
        if (changeRequest != null && !changeRequest.getDeleted()) {
            log.debug("变更请求查询成功, id={}, title={}", changeRequestId, changeRequest.getTitle());
            return changeRequest;
        }
        return null;
    }

    @Override
    public PageResult<ChangeRequest> listChangeRequests(Long projectId, Integer page, Integer pageSize,
                                                      String status, Integer priority, Long assigneeId, Long requesterId) {
        log.debug("查询变更请求列表, projectId={}, page={}, pageSize={}, status={}, priority={}, assigneeId={}, requesterId={}",
                projectId, page, pageSize, status, priority, assigneeId, requesterId);

        // 参数验证和默认值设置
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 20;
        }

        // 构建查询条件
        LambdaQueryWrapper<ChangeRequest> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChangeRequest::getDeleted, false);

        if (projectId != null) {
            queryWrapper.eq(ChangeRequest::getProjectId, projectId);
        }
        if (status != null && !status.trim().isEmpty()) {
            queryWrapper.eq(ChangeRequest::getStatus, status);
        }
        if (priority != null) {
            queryWrapper.eq(ChangeRequest::getPriority, priority);
        }
        if (assigneeId != null) {
            queryWrapper.eq(ChangeRequest::getAssigneeId, assigneeId);
        }
        if (requesterId != null) {
            queryWrapper.eq(ChangeRequest::getRequesterId, requesterId);
        }

        // 排序
        queryWrapper.orderByDesc(ChangeRequest::getCreateTime);

        // 分页查询
        Page<ChangeRequest> pageParam = new Page<>(page, pageSize);
        IPage<ChangeRequest> pageResult = changeRequestMapper.selectPage(pageParam, queryWrapper);

        // 构建返回结果
        PageResult<ChangeRequest> result = PageResult.of(
                pageResult.getRecords(),
                pageResult.getTotal(),
                page,
                pageSize
        );

        log.debug("变更请求列表查询完成, 总数={}, 当前页={}", result.getTotal(), result.getPage());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveChangeRequest(Long changeRequestId, String decision, String comments, Long userId) {
        log.info("审批变更请求, id={}, decision={}, userId={}", changeRequestId, decision, userId);

        if (changeRequestId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "变更请求ID不能为空");
        }
        if (decision == null || (!"APPROVED".equals(decision) && !"REJECTED".equals(decision))) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "审批决定无效");
        }

        // 检查变更请求是否存在
        ChangeRequest changeRequest = getChangeRequestById(changeRequestId);
        if (changeRequest == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "变更请求不存在");
        }

        // 检查是否有审批权限
        if (!canApproveChangeRequest(changeRequestId, userId)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "没有权限审批此变更请求");
        }

        // 检查状态（只有待审批状态可以审批）
        if (!STATUS_UNDER_REVIEW.equals(changeRequest.getStatus())) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "变更请求当前状态不允许审批");
        }

        // 更新状态
        String newStatus = "APPROVED".equals(decision) ? STATUS_APPROVED : STATUS_REJECTED;
        changeRequest.setStatus(newStatus);
        changeRequest.setReviewerId(userId);
        changeRequest.setUpdaterId(userId);

        int result = changeRequestMapper.updateById(changeRequest);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "审批变更请求失败");
        }

        log.info("变更请求审批完成, id={}, status={}", changeRequestId, newStatus);
    }

    @Override
    public List<ChangeRequestImpact> getChangeRequestImpacts(Long changeRequestId) {
        if (changeRequestId == null) {
            return List.of();
        }

        log.debug("查询变更请求影响分析, changeRequestId={}", changeRequestId);

        LambdaQueryWrapper<ChangeRequestImpact> wrapper = new LambdaQueryWrapper<ChangeRequestImpact>()
                .eq(ChangeRequestImpact::getChangeRequestId, changeRequestId)
                .eq(ChangeRequestImpact::getDeleted, false)
                .orderByDesc(ChangeRequestImpact::getCreateTime);

        return changeRequestImpactMapper.selectList(wrapper).stream()
                .filter(impact -> !impact.getDeleted())
                .collect(Collectors.toList());
    }

    @Override
    public List<ChangeRequestImpact> analyzeChangeRequestImpact(Long changeRequestId, boolean forceRefresh) {
        log.info("执行变更请求影响分析, changeRequestId={}, forceRefresh={}", changeRequestId, forceRefresh);

        if (changeRequestId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "变更请求ID不能为空");
        }

        // 检查变更请求是否存在
        ChangeRequest changeRequest = getChangeRequestById(changeRequestId);
        if (changeRequest == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "变更请求不存在");
        }

        // 如果不需要强制刷新且已有分析结果，直接返回
        List<ChangeRequestImpact> existingImpacts = getChangeRequestImpacts(changeRequestId);
        if (!forceRefresh && !existingImpacts.isEmpty()) {
            return existingImpacts;
        }

        // 执行智能影响分析（简化版）
        // TODO: 这里应该调用更复杂的智能分析算法
        List<ChangeRequestImpact> impacts = performBasicImpactAnalysis(changeRequest);

        // 保存分析结果
        if (forceRefresh) {
            // 删除旧的分析结果
            LambdaQueryWrapper<ChangeRequestImpact> deleteWrapper = new LambdaQueryWrapper<ChangeRequestImpact>()
                    .eq(ChangeRequestImpact::getChangeRequestId, changeRequestId);
            changeRequestImpactMapper.delete(deleteWrapper);
        }

        // 保存新的分析结果
        if (!impacts.isEmpty()) {
            for (ChangeRequestImpact impact : impacts) {
                impact.setChangeRequestId(changeRequestId);
                impact.setCreatorId(changeRequest.getRequesterId());
                impact.setUpdaterId(changeRequest.getRequesterId());
                impact.setDeleted(false);
                changeRequestImpactMapper.insert(impact);
            }
        }

        log.info("变更请求影响分析完成, changeRequestId={}, impactCount={}", changeRequestId, impacts.size());
        return impacts;
    }

    @Override
    public void validateImpactAnalysis(Long impactId, boolean isValid, Long userId) {
        log.info("验证影响分析, impactId={}, isValid={}, userId={}", impactId, isValid, userId);

        if (impactId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "影响分析ID不能为空");
        }

        ChangeRequestImpact impact = changeRequestImpactMapper.selectById(impactId);
        if (impact == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "影响分析不存在");
        }

        // 更新验证状态
        impact.setIsValid(isValid);
        impact.setVerifiedBy(userId);
        impact.setVerifiedAt(LocalDateTime.now());
        impact.setUpdaterId(userId);

        int result = changeRequestImpactMapper.updateById(impact);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "验证影响分析失败");
        }

        log.info("影响分析验证完成, impactId={}", impactId);
    }

    @Override
    public PageResult<ChangeRequest> listChangeRequestsByUser(Long userId, Integer page, Integer size, String status) {
        return listChangeRequests(null, page, size, status, null, null, userId);
    }

    @Override
    public PageResult<ChangeRequest> listPendingApprovalChangeRequests(Long reviewerId, Integer page, Integer size) {
        return listChangeRequests(null, page, size, STATUS_UNDER_REVIEW, null, reviewerId, null);
    }

    @Override
    public int countChangeRequestsByProject(Long projectId) {
        if (projectId == null) {
            return 0;
        }
        LambdaQueryWrapper<ChangeRequest> wrapper = new LambdaQueryWrapper<ChangeRequest>()
                .eq(ChangeRequest::getProjectId, projectId)
                .eq(ChangeRequest::getDeleted, false);
        return Math.toIntExact(changeRequestMapper.selectCount(wrapper));
    }

    @Override
    public int countChangeRequestsByUser(Long userId, String status) {
        if (userId == null) {
            return 0;
        }
        LambdaQueryWrapper<ChangeRequest> wrapper = new LambdaQueryWrapper<ChangeRequest>()
                .eq(ChangeRequest::getRequesterId, userId)
                .eq(ChangeRequest::getDeleted, false);
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(ChangeRequest::getStatus, status);
        }
        return Math.toIntExact(changeRequestMapper.selectCount(wrapper));
    }

    @Override
    public int countPendingApprovalChangeRequests(Long reviewerId) {
        if (reviewerId == null) {
            return 0;
        }
        LambdaQueryWrapper<ChangeRequest> wrapper = new LambdaQueryWrapper<ChangeRequest>()
                .eq(ChangeRequest::getReviewerId, reviewerId)
                .eq(ChangeRequest::getStatus, STATUS_UNDER_REVIEW)
                .eq(ChangeRequest::getDeleted, false);
        return Math.toIntExact(changeRequestMapper.selectCount(wrapper));
    }

    @Override
    public boolean hasChangeRequestPermission(Long changeRequestId, Long userId) {
        if (changeRequestId == null || userId == null) {
            return false;
        }

        ChangeRequest changeRequest = getChangeRequestById(changeRequestId);
        if (changeRequest == null) {
            return false;
        }

        // 请求人有权限
        if (userId.equals(changeRequest.getRequesterId())) {
            return true;
        }

        // 项目成员有权限
        return projectService.isMember(changeRequest.getProjectId(), userId);
    }

    @Override
    public boolean hasChangeRequestViewPermission(Long changeRequestId, Long userId) {
        return hasChangeRequestPermission(changeRequestId, userId);
    }

    @Override
    public boolean canApproveChangeRequest(Long changeRequestId, Long userId) {
        if (changeRequestId == null || userId == null) {
            return false;
        }

        ChangeRequest changeRequest = getChangeRequestById(changeRequestId);
        if (changeRequest == null) {
            return false;
        }

        // 只有项目负责人可以审批
        return projectService.hasProjectEditPermission(changeRequest.getProjectId(), userId);
    }

    @Override
    public void submitChangeRequest(Long changeRequestId, Long userId) {
        log.info("提交变更请求, id={}, userId={}", changeRequestId, userId);

        if (changeRequestId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "变更请求ID不能为空");
        }

        // 检查变更请求是否存在
        ChangeRequest changeRequest = getChangeRequestById(changeRequestId);
        if (changeRequest == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "变更请求不存在");
        }

        // 检查权限（只有请求人可以提交）
        if (!userId.equals(changeRequest.getRequesterId())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "没有权限提交此变更请求");
        }

        // 检查状态（只有草稿状态可以提交）
        if (!STATUS_DRAFT.equals(changeRequest.getStatus())) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "只有草稿状态的变更请求可以提交");
        }

        // 更新状态为待审批
        changeRequest.setStatus(STATUS_UNDER_REVIEW);
        changeRequest.setUpdaterId(userId);

        int result = changeRequestMapper.updateById(changeRequest);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "提交变更请求失败");
        }

        log.info("变更请求提交成功, id={}", changeRequestId);
    }

    @Override
    public void implementChangeRequest(Long changeRequestId, Long userId) {
        log.info("实施变更请求, id={}, userId={}", changeRequestId, userId);

        if (changeRequestId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "变更请求ID不能为空");
        }

        // 检查变更请求是否存在
        ChangeRequest changeRequest = getChangeRequestById(changeRequestId);
        if (changeRequest == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "变更请求不存在");
        }

        // 检查状态（只有已批准状态可以实施）
        if (!STATUS_APPROVED.equals(changeRequest.getStatus())) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "变更请求当前状态不允许实施");
        }

        // 更新状态为已实施
        changeRequest.setStatus(STATUS_IMPLEMENTED);
        changeRequest.setUpdaterId(userId);

        int result = changeRequestMapper.updateById(changeRequest);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "实施变更请求失败");
        }

        log.info("变更请求实施成功, id={}", changeRequestId);
    }

    @Override
    public void closeChangeRequest(Long changeRequestId, Long userId) {
        log.info("关闭变更请求, id={}, userId={}", changeRequestId, userId);

        if (changeRequestId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "变更请求ID不能为空");
        }

        // 检查变更请求是否存在
        ChangeRequest changeRequest = getChangeRequestById(changeRequestId);
        if (changeRequest == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "变更请求不存在");
        }

        // 检查权限
        if (!hasChangeRequestPermission(changeRequestId, userId)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "没有权限关闭此变更请求");
        }

        // 检查状态（已实施状态可以关闭）
        if (!STATUS_IMPLEMENTED.equals(changeRequest.getStatus())) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "只有已实施状态的变更请求可以关闭");
        }

        // 更新状态为已关闭
        changeRequest.setStatus(STATUS_CLOSED);
        changeRequest.setUpdaterId(userId);

        int result = changeRequestMapper.updateById(changeRequest);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "关闭变更请求失败");
        }

        log.info("变更请求关闭成功, id={}", changeRequestId);
    }

    @Override
    public void reopenChangeRequest(Long changeRequestId, Long userId) {
        log.info("重新打开变更请求, id={}, userId={}", changeRequestId, userId);

        if (changeRequestId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "变更请求ID不能为空");
        }

        // 检查变更请求是否存在
        ChangeRequest changeRequest = getChangeRequestById(changeRequestId);
        if (changeRequest == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "变更请求不存在");
        }

        // 检查权限
        if (!hasChangeRequestPermission(changeRequestId, userId)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "没有权限重新打开此变更请求");
        }

        // 检查状态（已关闭状态可以重新打开）
        if (!STATUS_CLOSED.equals(changeRequest.getStatus())) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "只有已关闭状态的变更请求可以重新打开");
        }

        // 更新状态为草稿
        changeRequest.setStatus(STATUS_DRAFT);
        changeRequest.setUpdaterId(userId);

        int result = changeRequestMapper.updateById(changeRequest);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "重新打开变更请求失败");
        }

        log.info("变更请求重新打开成功, id={}", changeRequestId);
    }

    @Override
    public List<ChangeRequestApproval> getChangeRequestApprovalHistory(Long changeRequestId) {
        // TODO: 实现审批历史查询
        return List.of();
    }

    @Override
    public int getChangeRequestCommentCount(Long changeRequestId) {
        // TODO: 实现评论数量统计
        return 0;
    }

    @Override
    public int getChangeRequestImpactCount(Long changeRequestId) {
        if (changeRequestId == null) {
            return 0;
        }
        LambdaQueryWrapper<ChangeRequestImpact> wrapper = new LambdaQueryWrapper<ChangeRequestImpact>()
                .eq(ChangeRequestImpact::getChangeRequestId, changeRequestId)
                .eq(ChangeRequestImpact::getDeleted, false);
        return Math.toIntExact(changeRequestImpactMapper.selectCount(wrapper));
    }

    @Override
    public void batchUpdateChangeRequestStatus(List<Long> changeRequestIds, String status, Long userId) {
        if (changeRequestIds == null || changeRequestIds.isEmpty()) {
            return;
        }

        log.info("批量更新变更请求状态, count={}, status={}, userId={}", changeRequestIds.size(), status, userId);

        for (Long changeRequestId : changeRequestIds) {
            try {
                ChangeRequest changeRequest = getChangeRequestById(changeRequestId);
                if (changeRequest != null) {
                    changeRequest.setStatus(status);
                    changeRequest.setUpdaterId(userId);
                    changeRequestMapper.updateById(changeRequest);
                }
            } catch (Exception e) {
                log.error("批量更新变更请求状态失败, id={}, error={}", changeRequestId, e.getMessage());
            }
        }
    }

    @Override
    public ChangeRequestStatistics getChangeRequestStatistics(Long projectId) {
        if (projectId == null) {
            return new ChangeRequestStatistics();
        }

        ChangeRequestStatistics statistics = new ChangeRequestStatistics();

        // 统计总数
        LambdaQueryWrapper<ChangeRequest> totalWrapper = new LambdaQueryWrapper<ChangeRequest>()
                .eq(ChangeRequest::getProjectId, projectId)
                .eq(ChangeRequest::getDeleted, false);
        statistics.setTotalCount(Math.toIntExact(changeRequestMapper.selectCount(totalWrapper)));

        // 按状态统计
        statistics.setDraftCount(countByStatus(projectId, STATUS_DRAFT));
        statistics.setSubmittedCount(countByStatus(projectId, STATUS_SUBMITTED));
        statistics.setUnderReviewCount(countByStatus(projectId, STATUS_UNDER_REVIEW));
        statistics.setApprovedCount(countByStatus(projectId, STATUS_APPROVED));
        statistics.setRejectedCount(countByStatus(projectId, STATUS_REJECTED));
        statistics.setImplementedCount(countByStatus(projectId, STATUS_IMPLEMENTED));
        statistics.setClosedCount(countByStatus(projectId, STATUS_CLOSED));

        return statistics;
    }

    /**
     * 按状态统计数量
     */
    private int countByStatus(Long projectId, String status) {
        LambdaQueryWrapper<ChangeRequest> wrapper = new LambdaQueryWrapper<ChangeRequest>()
                .eq(ChangeRequest::getProjectId, projectId)
                .eq(ChangeRequest::getStatus, status)
                .eq(ChangeRequest::getDeleted, false);
        return Math.toIntExact(changeRequestMapper.selectCount(wrapper));
    }

    /**
     * 执行基础影响分析（简化版）
     */
    private List<ChangeRequestImpact> performBasicImpactAnalysis(ChangeRequest changeRequest) {
        // 这里应该调用更复杂的智能分析算法
        // 目前返回一些基础的影响分析结果

        ChangeRequestImpact impact1 = new ChangeRequestImpact();
        impact1.setEntityType("DOCUMENT");
        impact1.setEntityTitle("项目文档");
        impact1.setImpactDescription("可能需要更新相关文档");
        impact1.setImpactLevel("MEDIUM");
        impact1.setConfidenceScore(0.85);
        impact1.setIsVerified(false);
        impact1.setAnalysisVersion("v1.0");
        impact1.setAnalysisDetails("{\"type\": \"document_update\", \"effort\": 4}");
        impact1.setIsValid(true);

        ChangeRequestImpact impact2 = new ChangeRequestImpact();
        impact2.setEntityType("TASK");
        impact2.setEntityTitle("项目任务");
        impact2.setImpactDescription("可能影响相关任务的执行");
        impact2.setImpactLevel("HIGH");
        impact2.setConfidenceScore(0.75);
        impact2.setIsVerified(false);
        impact2.setAnalysisVersion("v1.0");
        impact2.setAnalysisDetails("{\"type\": \"task_dependency\", \"effort\": 8}");
        impact2.setIsValid(true);

        return List.of(impact1, impact2);
    }
}