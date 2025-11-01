package com.promanage.service.service;

import java.util.List;

import com.promanage.service.entity.Document;
import com.promanage.service.entity.DocumentVersion;

/**
 * 文档版本管理服务接口
 *
 * <p>负责文档版本控制相关的所有操作，包括： - 版本查询和获取 - 版本创建 - 版本回滚 - 版本号生成
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-16
 */
public interface IDocumentVersionService {

  /**
   * 查询文档的所有版本列表
   *
   * @param documentId 文档ID
   * @param userId 用户ID（用于权限验证）
   * @return 版本列表，按创建时间倒序排列
   * @throws com.promanage.common.exception.BusinessException 如果文档不存在或用户无权限
   */
  List<DocumentVersion> listVersions(Long documentId, Long userId);

  /**
   * 获取文档的指定版本
   *
   * @param documentId 文档ID
   * @param version 版本号（如 "1.0.0"）
   * @param userId 用户ID（用于权限验证）
   * @return 指定版本的详细信息
   * @throws com.promanage.common.exception.BusinessException 如果版本不存在或用户无权限
   */
  DocumentVersion getVersion(Long documentId, String version, Long userId);

  /**
   * 为文档创建新版本
   *
   * <p>创建新版本时会： 1. 验证用户权限 2. 设置创建者ID 3. 保存版本记录
   *
   * @param documentVersion 版本信息（需包含documentId和versionNumber）
   * @param creatorId 创建者ID
   * @return 新创建的版本ID
   * @throws com.promanage.common.exception.BusinessException 如果参数无效或用户无权限
   */
  Long createVersion(DocumentVersion documentVersion, Long creatorId);

  /**
   * 将文档回滚到指定版本
   *
   * <p>回滚操作会： 1. 恢复文档内容到目标版本 2. 生成新的版本号 3. 创建新版本记录（changeLog标记为回滚）
   *
   * @param documentId 文档ID
   * @param version 目标版本号
   * @param updaterId 操作者ID
   * @return 回滚后的文档对象
   * @throws com.promanage.common.exception.BusinessException 如果目标版本不存在或用户无权限
   */
  Document rollbackToVersion(Long documentId, String version, Long updaterId);

  /**
   * 生成下一个版本号
   *
   * <p>版本号格式: major.minor.patch 规则: 每次更新patch+1 (如 1.0.0 -> 1.0.1)
   *
   * @param currentVersion 当前版本号
   * @return 下一个版本号
   */
  String generateNextVersion(String currentVersion);
}
