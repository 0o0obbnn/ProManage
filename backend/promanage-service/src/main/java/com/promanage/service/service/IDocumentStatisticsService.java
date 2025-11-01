package com.promanage.service.service;

import java.util.List;
import java.util.Map;

/**
 * 文档统计分析服务接口
 *
 * <p>负责文档统计相关的所有操作，包括： - 文档数量统计 - 浏览量统计 - 项目信息批量获取
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-16
 */
public interface IDocumentStatisticsService {

  /**
   * 统计项目的文档数量
   *
   * @param projectId 项目ID
   * @param userId 用户ID（用于权限验证）
   * @return 文档数量
   * @throws com.promanage.common.exception.BusinessException 如果用户无权限访问项目
   */
  int countByProject(Long projectId, Long userId);

  /**
   * 统计用户创建的文档数量
   *
   * @param creatorId 创建者ID
   * @param userId 当前用户ID（用于权限验证）
   * @return 文档数量
   * @throws com.promanage.common.exception.BusinessException 如果用户无权限查看创建者的文档
   */
  int countByCreator(Long creatorId, Long userId);

  /**
   * 获取文档的周浏览量
   *
   * <p>从Redis中获取最近7天的浏览量统计
   *
   * @param documentId 文档ID
   * @param userId 用户ID（用于权限验证）
   * @return 周浏览量
   * @throws com.promanage.common.exception.BusinessException 如果用户无权限查看文档
   */
  int getWeeklyViewCount(Long documentId, Long userId);

  /**
   * 批量获取项目名称
   *
   * <p>用于解决N+1查询问题，一次性获取多个项目的名称
   *
   * @param projectIds 项目ID列表
   * @param userId 用户ID（用于权限验证）
   * @return 项目ID到项目名称的映射
   */
  Map<Long, String> getProjectNames(List<Long> projectIds, Long userId);
}
