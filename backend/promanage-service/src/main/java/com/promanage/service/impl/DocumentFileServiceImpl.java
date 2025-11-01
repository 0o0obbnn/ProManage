package com.promanage.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.promanage.common.domain.ResultCode;
import com.promanage.common.exception.BusinessException;
import com.promanage.service.IProjectService;
import com.promanage.service.dto.request.DocumentUploadRequest;
import com.promanage.service.dto.response.DocumentDownloadInfo;
import com.promanage.service.entity.Document;
import com.promanage.service.entity.DocumentVersion;
import com.promanage.service.entity.Project;
import com.promanage.service.mapper.DocumentMapper;
import com.promanage.service.mapper.DocumentVersionMapper;
import com.promanage.service.service.IDocumentFileService;
import com.promanage.service.service.IPermissionService;
import com.promanage.service.constant.CommonConstants;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 文档文件管理服务实现类
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentFileServiceImpl implements IDocumentFileService {

  private final DocumentMapper documentMapper;
  private final DocumentVersionMapper documentVersionMapper;
  private final IPermissionService permissionService;
  private final IProjectService projectService;

  // ==================== 文件上传安全配置 ====================

  /** 🛡️ 允许上传的文件扩展名白名单 仅允许常见的文档、图片、压缩包格式 */
  private static final Set<String> ALLOWED_FILE_EXTENSIONS =
      Set.of(
          // 文档类
          "txt",
          "md",
          "pdf",
          "doc",
          "docx",
          "xls",
          "xlsx",
          "ppt",
          "pptx",
          "odt",
          "rtf",
          // 图片类
          "jpg",
          "jpeg",
          "png",
          "gif",
          "bmp",
          "svg",
          "webp",
          // 压缩包类
          "zip",
          "rar",
          "7z",
          "tar",
          "gz",
          // 代码类
          "java",
          "py",
          "js",
          "ts",
          "json",
          "xml",
          "yaml",
          "yml",
          "sql",
          "sh",
          // 其他
          "csv",
          "log");

  /** 🛡️ 文件名合法性正则 (防止路径遍历攻击) 允许: 字母、数字、中文、下划线、连字符、点号、括号、空格 禁止: 路径分隔符(/ \)、特殊字符(.. ./ ../)等 */
  private static final Pattern SAFE_FILENAME_PATTERN =
      Pattern.compile("^[\\w\\u4e00-\\u9fa5.()\\-\\s]+$");

  /** 🛡️ Magic Byte验证映射表 用于验证文件实际内容与声明的扩展名是否匹配 */
  private static final java.util.Map<String, byte[]> MAGIC_BYTES_MAP =
      java.util.Map.ofEntries(
          // 图片格式
          java.util.Map.entry("jpg", new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}),
          java.util.Map.entry("jpeg", new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}),
          java.util.Map.entry("png", new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47}),
          java.util.Map.entry("gif", new byte[] {0x47, 0x49, 0x46}),
          // 文档格式
          java.util.Map.entry("pdf", new byte[] {0x25, 0x50, 0x44, 0x46}),
          // 压缩包格式
          java.util.Map.entry("zip", new byte[] {0x50, 0x4B, 0x03, 0x04}),
          java.util.Map.entry("rar", new byte[] {0x52, 0x61, 0x72, 0x21}));

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Document upload(DocumentUploadRequest request, Long uploaderId) throws IOException {
    MultipartFile file = request.getFile();
    String originalFilename = file.getOriginalFilename();
    log.info(
        "上传文档, uploaderId={}, fileName={}, size={}", uploaderId, originalFilename, file.getSize());

    if (uploaderId == null) {
      throw new BusinessException(ResultCode.UNAUTHORIZED, CommonConstants.ERROR_USER_NOT_LOGIN);
    }

    // 权限检查
    validateProjectCreateAccess(request.getProjectId(), uploaderId);

    // 参数验证
    if (file == null || file.isEmpty()) {
      throw new BusinessException(ResultCode.PARAM_ERROR, "上传文件不能为空");
    }

    // 🛡️ 安全验证 #1: 文件大小限制 (500MB)
    long maxSize = 500 * 1024 * 1024L;
    if (file.getSize() > maxSize) {
      throw new BusinessException(ResultCode.PARAM_ERROR, "文件大小不能超过500MB");
    }

    // 🛡️ 安全验证 #2: 文件名合法性检查 (防止路径遍历攻击)
    if (StringUtils.isBlank(originalFilename)) {
      throw new BusinessException(ResultCode.PARAM_ERROR, "文件名不能为空");
    }
    String sanitizedFilename = sanitizeFilename(originalFilename);
    log.info("文件名消毒: {} → {}", originalFilename, sanitizedFilename);

    // 🛡️ 安全验证 #3: 文件扩展名白名单检查
    String fileExtension = getFileExtension(sanitizedFilename);
    if (!ALLOWED_FILE_EXTENSIONS.contains(fileExtension)) {
      log.warn("拒绝不安全的文件类型: {}, 原始文件名: {}", fileExtension, originalFilename);
      throw new BusinessException(
          ResultCode.PARAM_ERROR,
          "不支持的文件类型: " + fileExtension + "。允许的类型: " + String.join(", ", ALLOWED_FILE_EXTENSIONS));
    }

    // 🛡️ 安全验证 #4: Magic Byte验证 (防止文件伪装)
    if (MAGIC_BYTES_MAP.containsKey(fileExtension)) {
      validateFileMagicBytes(file, fileExtension);
    }

    // 创建文档实体
    Document document = new Document();
    document.setTitle(
        StringUtils.isNotBlank(request.getTitle())
            ? sanitizeFilename(request.getTitle())
            : sanitizedFilename);
    document.setProjectId(request.getProjectId());
    document.setFolderId(request.getFolderId() != null ? request.getFolderId() : 0L);
    document.setType("file"); // 默认文件类型
    document.setContentType(file.getContentType());
    document.setFileSize(file.getSize());
    document.setCreatorId(uploaderId);
    document.setSummary(request.getDescription());
    document.setStatus(0); // 草稿状态
    document.setCurrentVersion("1.0.0");
    document.setViewCount(0);
    document.setIsTemplate(false);

    // TODO: 实际的文件存储逻辑（MinIO/S3）
    // 当前简化实现：仅保存文件名作为URL
    // 🛡️ 使用消毒后的文件名生成安全的URL路径
    String fileUrl = "/files/" + System.currentTimeMillis() + "_" + sanitizedFilename;
    document.setFileUrl(fileUrl);

    // 保存文档
    documentMapper.insert(document);

    // 创建初始版本
    DocumentVersion version = new DocumentVersion();
    version.setDocumentId(document.getId());
    version.setVersionNumber("1.0.0");
    version.setTitle(document.getTitle());
    version.setContent(document.getContent());
    version.setContentType(document.getContentType());
    version.setChangeLog("初始版本");
    version.setFileUrl(document.getFileUrl());
    version.setFileSize(document.getFileSize());
    version.setCreatorId(uploaderId);
    version.setIsCurrent(true);
    documentVersionMapper.insert(version);

    log.info("文档上传成功, documentId={}, fileUrl={}, securityChecks=PASSED", document.getId(), fileUrl);
    return document;
  }

  @Override
  public DocumentDownloadInfo getDownloadInfo(Long documentId, Long userId) {
    log.info("获取文档下载信息, id={}, userId={}", documentId, userId);

    if (userId == null) {
      throw new BusinessException(ResultCode.UNAUTHORIZED, CommonConstants.ERROR_USER_NOT_LOGIN);
    }

    // 权限检查
    validateDocumentAccess(documentId, userId);

    // 获取文档信息
    Document document = getDocumentById(documentId);

    if (StringUtils.isBlank(document.getFileUrl())) {
      throw new BusinessException(ResultCode.PARAM_ERROR, "文档没有关联文件");
    }

    // 构建下载信息
    DocumentDownloadInfo downloadInfo = new DocumentDownloadInfo();
    downloadInfo.setFileName(document.getTitle());
    downloadInfo.setFileUrl(document.getFileUrl());
    downloadInfo.setFileSize(document.getFileSize());
    downloadInfo.setContentType(document.getContentType());

    // TODO: 生成临时下载令牌（有效期15分钟）- 可通过扩展DTO添加此字段

    log.info("获取文档下载信息成功, documentId={}", documentId);
    return downloadInfo;
  }

  @Override
  public void downloadDocument(Long documentId, Long userId, HttpServletResponse response)
      throws IOException {
    log.info("下载文档, id={}, userId={}", documentId, userId);

    if (userId == null) {
      throw new BusinessException(ResultCode.UNAUTHORIZED, CommonConstants.ERROR_USER_NOT_LOGIN);
    }

    // 权限检查
    validateDocumentAccess(documentId, userId);

    // 获取文档信息
    Document document = getDocumentById(documentId);

    if (StringUtils.isBlank(document.getFileUrl())) {
      throw new BusinessException(ResultCode.PARAM_ERROR, "文档没有关联文件");
    }

    // 设置响应头
    response.setContentType(
        document.getContentType() != null ? document.getContentType() : "application/octet-stream");
    response.setHeader(
        "Content-Disposition", "attachment; filename=\"" + document.getTitle() + "\"");

    if (document.getFileSize() != null) {
      response.setContentLengthLong(document.getFileSize());
    }

    // TODO: 实际的文件下载逻辑（从MinIO/S3读取文件流）
    // 当前简化实现：返回提示信息
    response.getWriter().write("文件下载功能待完善，文件路径: " + document.getFileUrl());
    response.getWriter().flush();

    log.info("文档下载完成, documentId={}", documentId);
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
    if (!permissionService.isProjectMember(userId, document.getProjectId())) {
      throw new BusinessException(ResultCode.FORBIDDEN, "无权访问此文档");
    }
  }

  /** 验证用户是否有权在项目中创建文档 */
  private void validateProjectCreateAccess(Long projectId, Long userId) {
    if (userId == null) {
      throw new BusinessException(ResultCode.UNAUTHORIZED, CommonConstants.ERROR_USER_NOT_LOGIN);
    }

    Project project = projectService.getById(projectId);
    if (project == null) {
      throw new BusinessException(ResultCode.NOT_FOUND, "项目不存在");
    }

    // 检查用户是否是项目成员
    if (!permissionService.isProjectMember(userId, projectId)) {
      throw new BusinessException(ResultCode.FORBIDDEN, "无权在此项目中创建文档");
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

  // ==================== 文件安全验证辅助方法 ====================

  /**
   * 🛡️ 文件名消毒（防止路径遍历攻击）
   *
   * <p>安全措施: 1. 移除所有路径分隔符 (/, \) 2. 移除 . 和 .. 路径遍历字符 3. 仅保留安全字符（字母、数字、中文、下划线、连字符、点号、括号、空格） 4.
   * 限制文件名长度为255个字符
   *
   * @param filename 原始文件名
   * @return 消毒后的安全文件名
   */
  private String sanitizeFilename(String filename) {
    if (StringUtils.isBlank(filename)) {
      return "unnamed_file";
    }

    // 1. 移除路径分隔符和路径遍历字符
    String sanitized =
        filename
            .replace("/", "")
            .replace("\\", "")
            .replace("..", "")
            .replace("./", "")
            .replace(".\\", "");

    // 2. 使用正则验证文件名合法性
    if (!SAFE_FILENAME_PATTERN.matcher(sanitized).matches()) {
      log.warn("文件名包含非法字符,使用默认名称: {}", filename);
      // 如果文件名不合法,提取扩展名并生成安全的默认文件名
      String extension = getFileExtension(filename);
      return "file_"
          + System.currentTimeMillis()
          + (StringUtils.isNotBlank(extension) ? "." + extension : "");
    }

    // 3. 限制文件名长度
    if (sanitized.length() > 255) {
      String extension = getFileExtension(sanitized);
      String nameWithoutExt = sanitized.substring(0, sanitized.lastIndexOf('.'));
      sanitized = nameWithoutExt.substring(0, 250 - extension.length()) + "." + extension;
    }

    return sanitized;
  }

  /**
   * 🛡️ 获取文件扩展名（小写）
   *
   * @param filename 文件名
   * @return 小写扩展名（不含点号），如果没有扩展名返回空字符串
   */
  private String getFileExtension(String filename) {
    if (StringUtils.isBlank(filename)) {
      return "";
    }

    int lastDotIndex = filename.lastIndexOf('.');
    if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
      return "";
    }

    return filename.substring(lastDotIndex + 1).toLowerCase();
  }

  /**
   * 🛡️ Magic Byte验证（防止文件伪装攻击）
   *
   * <p>验证文件的实际内容（Magic Bytes）是否与声明的扩展名匹配。 例如: - 将 .exe 文件伪装成 .jpg 会被检测到 - 将恶意脚本伪装成 .pdf 会被检测到
   *
   * @param file 上传的文件
   * @param declaredExtension 声明的文件扩展名
   * @throws IOException 读取文件失败
   * @throws BusinessException 文件内容与扩展名不匹配
   */
  private void validateFileMagicBytes(MultipartFile file, String declaredExtension)
      throws IOException {
    byte[] expectedMagicBytes = MAGIC_BYTES_MAP.get(declaredExtension);
    if (expectedMagicBytes == null) {
      // 无需验证此类型的文件
      return;
    }

    try (InputStream inputStream = file.getInputStream()) {
      byte[] actualMagicBytes = new byte[expectedMagicBytes.length];
      int bytesRead = inputStream.read(actualMagicBytes);

      if (bytesRead < expectedMagicBytes.length) {
        log.warn(
            "文件过小,无法读取Magic Bytes: {}, 期望{}字节,实际{}字节",
            file.getOriginalFilename(),
            expectedMagicBytes.length,
            bytesRead);
        throw new BusinessException(ResultCode.PARAM_ERROR, "文件格式无效或文件已损坏");
      }

      // 比较实际Magic Bytes与期望值
      for (int i = 0; i < expectedMagicBytes.length; i++) {
        if (actualMagicBytes[i] != expectedMagicBytes[i]) {
          log.warn(
              "Magic Byte验证失败: 文件={}, 声明类型={}, 实际Magic Bytes与期望不符",
              file.getOriginalFilename(),
              declaredExtension);
          throw new BusinessException(
              ResultCode.PARAM_ERROR, "文件内容与扩展名不匹配，可能是伪装文件。声明类型: " + declaredExtension);
        }
      }

      log.debug("Magic Byte验证通过: {}, 类型={}", file.getOriginalFilename(), declaredExtension);
    }
  }
}
