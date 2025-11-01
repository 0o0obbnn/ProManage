package com.promanage.service.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promanage.common.domain.ResultCode;
import com.promanage.common.exception.BusinessException;
import com.promanage.service.entity.Document;
import com.promanage.service.entity.DocumentVersion;
import com.promanage.service.mapper.DocumentMapper;
import com.promanage.service.mapper.DocumentVersionMapper;
import com.promanage.service.service.IDocumentVersionService;
import com.promanage.service.constant.DocumentConstants;
import com.promanage.service.service.IPermissionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 文档版本管理服务实现类
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentVersionServiceImpl implements IDocumentVersionService {

  private final DocumentVersionMapper documentVersionMapper;
  private final DocumentMapper documentMapper;
  private final IPermissionService permissionService;

  @Override
  @Cacheable(value = DocumentConstants.CACHE_DOCUMENT_VERSIONS, key = "#documentId")
  public List<DocumentVersion> listVersions(Long documentId, Long userId) {
    log.info("查询文档版本列表, documentId={}, userId={}", documentId, userId);

    if (documentId == null) {
      throw new BusinessException(ResultCode.PARAM_ERROR, "文档ID不能为空");
    }

    // 权限检查 - 验证用户有权查看此文档版本
    validateDocumentAccess(documentId, userId);

    return documentVersionMapper.findByDocumentId(documentId);
  }

  @Override
  @Cacheable(value = DocumentConstants.CACHE_DOCUMENT_VERSIONS, key = "#documentId + ':' + #version")
  public DocumentVersion getVersion(Long documentId, String version, Long userId) {
    log.info("查询文档版本, documentId={}, version={}, userId={}", documentId, version, userId);

    if (documentId == null || StringUtils.isBlank(version)) {
      throw new BusinessException(ResultCode.PARAM_ERROR, "文档ID和版本号不能为空");
    }

    // 权限检查 - 验证用户有权查看此文档版本
    validateDocumentAccess(documentId, userId);

    DocumentVersion documentVersion =
        documentVersionMapper.findByDocumentIdAndVersion(documentId, version);
    if (documentVersion == null) {
      throw new BusinessException(ResultCode.PARAM_ERROR, "文档版本不存在");
    }

    return documentVersion;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  @CacheEvict(value = DocumentConstants.CACHE_DOCUMENT_VERSIONS, allEntries = true)
  public Long createVersion(DocumentVersion documentVersion, Long creatorId) {
    log.info(
        "创建文档版本, documentId={}, version={}, creatorId={}",
        documentVersion.getDocumentId(),
        documentVersion.getVersionNumber(),
        creatorId);

    // 参数验证
    if (documentVersion.getDocumentId() == null
        || StringUtils.isBlank(documentVersion.getVersionNumber())) {
      throw new BusinessException(ResultCode.PARAM_ERROR, "文档ID和版本号不能为空");
    }

    // 权限检查 - 验证用户有权为此文档创建版本
    validateDocumentUpdateAccess(documentVersion.getDocumentId(), creatorId);

    // 设置创建者ID
    documentVersion.setCreatorId(creatorId);

    // 保存版本
    documentVersionMapper.insert(documentVersion);

    log.info("创建文档版本成功, id={}", documentVersion.getId());
    return documentVersion.getId();
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  @CacheEvict(
      value = {"documents", DocumentConstants.CACHE_DOCUMENT_VERSIONS},
      allEntries = true)
  public Document rollbackToVersion(Long documentId, String version, Long updaterId) {
    log.info("回滚文档到指定版本, documentId={}, version={}, updaterId={}", documentId, version, updaterId);

    // 权限检查 - 验证用户有权回滚此文档
    validateDocumentUpdateAccess(documentId, updaterId);

    // 检查文档是否存在
    Document document = getDocumentById(documentId);

    // 获取指定版本
    DocumentVersion targetVersion = getVersion(documentId, version, updaterId);

    // 恢复内容
    document.setContent(targetVersion.getContent());
    document.setFileUrl(targetVersion.getFileUrl());

    // 生成新版本号
    String newVersion = generateNextVersion(document.getCurrentVersion());
    document.setCurrentVersion(newVersion);

    // 保存文档
    documentMapper.updateById(document);

    // 创建新版本记录
    DocumentVersion newVersionRecord = new DocumentVersion();
    newVersionRecord.setDocumentId(documentId);
    newVersionRecord.setVersionNumber(newVersion);
    newVersionRecord.setContent(targetVersion.getContent());
    newVersionRecord.setChangeLog("回滚到版本 " + version);
    newVersionRecord.setFileUrl(targetVersion.getFileUrl());
    newVersionRecord.setCreatorId(updaterId);
    documentVersionMapper.insert(newVersionRecord);

    log.info(
        "回滚文档成功, documentId={}, targetVersion={}, newVersion={}", documentId, version, newVersion);
    return getDocumentById(documentId);
  }

  @Override
  public String generateNextVersion(String currentVersion) {
    if (StringUtils.isBlank(currentVersion)) {
      return "1.0.0";
    }

    try {
      String[] parts = currentVersion.split("\\.");
      if (parts.length == 3) {
        int major = Integer.parseInt(parts[0]);
        int minor = Integer.parseInt(parts[1]);
        int patch = Integer.parseInt(parts[2]);
        return major + "." + minor + "." + (patch + 1);
      }
    } catch (NumberFormatException e) {
      log.warn("解析版本号失败, currentVersion={}", currentVersion, e);
    }

    return "1.0.0";
  }

  // ==================== 私有辅助方法 ====================

  /** 验证用户是否有权访问文档 */
  private void validateDocumentAccess(Long documentId, Long userId) {
    if (userId == null) {
      throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
    }

    Document document = getDocumentById(documentId);
    if (document == null) {
      throw new BusinessException(ResultCode.NOT_FOUND, "文档不存在");
    }

    // 检查用户是否有权访问文档所属的项目
    if (!permissionService.isProjectMember(userId, document.getProjectId())) {
      throw new BusinessException(ResultCode.FORBIDDEN, "无权访问此文档");
    }
  }

  /** 验证用户是否有权更新文档 */
  private void validateDocumentUpdateAccess(Long documentId, Long userId) {
    if (userId == null) {
      throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
    }

    Document document = getDocumentById(documentId);
    if (document == null) {
      throw new BusinessException(ResultCode.NOT_FOUND, "文档不存在");
    }

    // 检查用户是否是项目成员
    if (!permissionService.isProjectMember(userId, document.getProjectId())) {
      throw new BusinessException(ResultCode.FORBIDDEN, "无权更新此文档");
    }

    // 文档创建者可以更新自己的文档
    if (!userId.equals(document.getCreatorId())) {
      // 非创建者需要项目管理员权限
      if (!permissionService.isProjectAdmin(userId, document.getProjectId())) {
        throw new BusinessException(ResultCode.FORBIDDEN, "无权更新此文档");
      }
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
