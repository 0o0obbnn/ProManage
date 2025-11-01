package com.promanage.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.promanage.common.domain.ResultCode;
import com.promanage.common.exception.BusinessException;
import com.promanage.service.IProjectService;
import com.promanage.service.entity.Document;
import com.promanage.service.entity.Project;
import com.promanage.service.mapper.DocumentMapper;
import com.promanage.service.service.IDocumentStatisticsService;
import com.promanage.service.constant.CommonConstants;
import com.promanage.service.service.IDocumentViewCountService;
import com.promanage.service.service.IPermissionService;

import org.springframework.dao.DataAccessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 文档统计分析服务实现类
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentStatisticsServiceImpl implements IDocumentStatisticsService {

  private final DocumentMapper documentMapper;
  private final IPermissionService permissionService;
  private final IProjectService projectService;
  private final IDocumentViewCountService documentViewCountService;

  @Override
  public int countByProject(Long projectId, Long userId) {
    log.info("统计项目文档数量, projectId={}, userId={}", projectId, userId);

    // 权限检查 - 验证用户有权访问此项目
    validateProjectAccess(projectId, userId);

    if (projectId == null) {
      return 0;
    }

    return documentMapper.countByProjectId(projectId);
  }

  @Override
  public int countByCreator(Long creatorId, Long userId) {
    log.info("统计用户创建的文档数量, creatorId={}, userId={}", creatorId, userId);

    // 权限检查 - 验证用户有权查看此创建者的文档
    validateCreatorAccess(creatorId, userId);

    if (creatorId == null) {
      return 0;
    }

    return documentMapper.countByCreatorId(creatorId);
  }

  @Override
  public int getWeeklyViewCount(Long documentId, Long userId) {
    log.info("获取文档周浏览量, documentId={}, userId={}", documentId, userId);

    // 权限检查 - 验证用户有权查看此文档统计
    validateDocumentAccess(documentId, userId);

    return documentViewCountService.getWeeklyViewCount(documentId);
  }

  @Override
  public Map<Long, String> getProjectNames(List<Long> projectIds, Long userId) {
    log.info("批量获取项目名称, projectIds={}, userId={}", projectIds, userId);

    if (projectIds == null || projectIds.isEmpty()) {
      return Collections.emptyMap();
    }

    if (userId == null) {
      log.error("用户ID为空,拒绝执行");
      throw new BusinessException(ResultCode.UNAUTHORIZED, CommonConstants.ERROR_USER_NOT_LOGIN);
    }

    try {
      // ✅ 批量权限检查：获取用户可访问的所有项目ID (1次查询)
      List<Long> accessibleProjectIds = projectService.getUserAccessibleProjectIds(userId);

      // ✅ 过滤：只保留用户有权访问的项目ID
      List<Long> filteredProjectIds =
          projectIds.stream().filter(accessibleProjectIds::contains).collect(Collectors.toList());

      if (filteredProjectIds.isEmpty()) {
        log.info("用户{}无权访问请求的任何项目, requestedIds={}", userId, projectIds);
        return Collections.emptyMap();
      }

      // ✅ 批量查询项目信息 (1次查询)
      List<Project> projects = projectService.listByIds(filteredProjectIds);

      log.info("用户{}可访问{}个项目(共请求{}个)", userId, projects.size(), projectIds.size());

      // 转换为Map
      return projects.stream()
          .collect(
              Collectors.toMap(
                  Project::getId,
                  Project::getName,
                  (existing, replacement) -> existing // 重复key时保留第一个
                  ));
    } catch (DataAccessException e) {
      log.error("批量获取项目名称失败, projectIds={}, userId={}", projectIds, userId, e);
      return Collections.emptyMap();
    }
  }

  // ==================== 私有辅助方法 ====================

  /** 验证用户是否有权访问文档 */
  private void validateDocumentAccess(Long documentId, Long userId) {
    if (userId == null) {
      throw new BusinessException(ResultCode.UNAUTHORIZED, CommonConstants.ERROR_USER_NOT_LOGIN);
    }

    Document document = getDocumentById(documentId);
    if (document == null) {
      throw new BusinessException(ResultCode.NOT_FOUND, "文档不存在");
    }

    // 检查用户是否有权访问文档所属的项目
    validateProjectAccess(document.getProjectId(), userId);
  }

  /** 验证用户是否有权访问项目 */
  private void validateProjectAccess(Long projectId, Long userId) {
    if (userId == null) {
      throw new BusinessException(ResultCode.UNAUTHORIZED, CommonConstants.ERROR_USER_NOT_LOGIN);
    }

    Project project = projectService.getById(projectId);
    if (project == null) {
      throw new BusinessException(ResultCode.NOT_FOUND, "项目不存在");
    }

    // 检查用户是否是项目成员
    if (!permissionService.isProjectMember(userId, projectId)) {
      throw new BusinessException(ResultCode.FORBIDDEN, "无权访问此项目");
    }
  }

  /** 验证用户是否有权查看创建者的文档 */
  private void validateCreatorAccess(Long creatorId, Long userId) {
    if (userId == null) {
      throw new BusinessException(ResultCode.UNAUTHORIZED, CommonConstants.ERROR_USER_NOT_LOGIN);
    }

    // 用户可以查看自己的文档
    if (userId.equals(creatorId)) {
      return;
    }

    // 查看他人文档需要管理员权限
    if (!permissionService.isSuperAdmin(userId)) {
      throw new BusinessException(ResultCode.FORBIDDEN, "无权查看此用户的文档");
    }
  }

  /** 根据ID获取文档 */
  private Document getDocumentById(Long documentId) {
    if (documentId == null) {
      throw new BusinessException(ResultCode.PARAM_ERROR, "文档ID不能为空");
    }

    Document document = documentMapper.selectById(documentId);
    if (document == null) {
      throw new BusinessException(ResultCode.NOT_FOUND, "文档不存在");
    }

    return document;
  }
}
