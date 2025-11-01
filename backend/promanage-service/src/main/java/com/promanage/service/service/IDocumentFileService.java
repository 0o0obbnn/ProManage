package com.promanage.service.service;

import java.io.IOException;

import com.promanage.service.dto.request.DocumentUploadRequest;
import com.promanage.service.dto.response.DocumentDownloadInfo;
import com.promanage.service.entity.Document;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 文档文件管理服务接口
 *
 * <p>负责文档文件相关的所有操作，包括： - 文件上传 - 文件下载 - 下载信息获取
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-16
 */
public interface IDocumentFileService {

  /**
   * 上传文档文件
   *
   * <p>上传流程： 1. 验证用户权限（项目创建权限） 2. 验证文件大小（最大500MB） 3. 保存文件到MinIO/S3 4. 创建文档记录 5. 创建初始版本
   *
   * @param request 上传请求（包含文件、项目ID、文件夹ID等）
   * @param uploaderId 上传者ID
   * @return 创建的文档对象
   * @throws IOException 如果文件读写失败
   * @throws com.promanage.common.exception.BusinessException 如果参数无效或用户无权限
   */
  Document upload(DocumentUploadRequest request, Long uploaderId) throws IOException;

  /**
   * 获取文档下载信息
   *
   * <p>返回信息包括： - 文件名 - 文件URL - 文件大小 - 内容类型 - 临时下载令牌（有效期15分钟）
   *
   * @param documentId 文档ID
   * @param userId 用户ID（用于权限验证）
   * @return 下载信息对象
   * @throws com.promanage.common.exception.BusinessException 如果文档不存在或用户无权限
   */
  DocumentDownloadInfo getDownloadInfo(Long documentId, Long userId);

  /**
   * 下载文档文件
   *
   * <p>下载流程： 1. 验证用户权限 2. 从MinIO/S3读取文件 3. 设置响应头 4. 写入文件流到响应
   *
   * @param documentId 文档ID
   * @param userId 用户ID（用于权限验证）
   * @param response HTTP响应对象
   * @throws IOException 如果文件读写失败
   * @throws com.promanage.common.exception.BusinessException 如果文档不存在或用户无权限
   */
  void downloadDocument(Long documentId, Long userId, HttpServletResponse response)
      throws IOException;
}
