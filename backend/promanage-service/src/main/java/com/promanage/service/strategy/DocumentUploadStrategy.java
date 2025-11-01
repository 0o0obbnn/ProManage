package com.promanage.service.strategy;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.promanage.common.exception.BusinessException;
import com.promanage.common.domain.ResultCode;
import com.promanage.service.dto.response.DocumentDownloadInfo;
import com.promanage.service.dto.request.DocumentUploadRequest;
import com.promanage.service.constant.DocumentConstants;
import com.promanage.service.entity.Document;
import com.promanage.service.mapper.DocumentMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 文档上传下载策略
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentUploadStrategy {

    private final DocumentMapper documentMapper;

    /**
     * 上传文档
     */
    public Document upload(DocumentUploadRequest request, Long uploaderId) throws IOException {
        validateUploadRequest(request);
        
        // 处理文件上传
        String filePath = processFileUpload(request.getFile());
        
        // 创建文档记录
        Document document = new Document();
        document.setTitle(request.getTitle());
        document.setDescription(request.getDescription());
        document.setProjectId(request.getProjectId());
        document.setFileUrl(filePath);
        document.setFileName(request.getFile().getOriginalFilename());
        document.setFileSize(request.getFile().getSize());
        document.setFileExtension(getFileExtension(request.getFile().getOriginalFilename()));
        document.setContentType(request.getFile().getContentType());
        document.setStatus(0); // 0-草稿, String → Integer conversion
        document.setCreatorId(uploaderId);
        document.setCreateTime(LocalDateTime.now());
        document.setUpdateTime(LocalDateTime.now());
        
        documentMapper.insert(document);
        
        return document;
    }

    /**
     * 获取下载信息
     */
    public DocumentDownloadInfo getDownloadInfo(Long id, Long userId) {
        Document document = documentMapper.selectById(id);
        if (document == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, DocumentConstants.ERROR_DOCUMENT_NOT_FOUND);
        }
        
        DocumentDownloadInfo downloadInfo = new DocumentDownloadInfo();
        downloadInfo.setDocumentId(document.getId());
        downloadInfo.setFileName(document.getFileName());
        downloadInfo.setFileSize(document.getFileSize());
        downloadInfo.setContentType(document.getContentType());
        downloadInfo.setDownloadUrl(generateDownloadUrl(document.getId()));
        
        return downloadInfo;
    }

    /**
     * 处理文件上传
     */
    private String processFileUpload(MultipartFile file) throws IOException {
        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        
        // 这里应该将文件保存到文件存储系统（如MinIO、AWS S3等）
        // 简化实现，返回虚拟路径
        String filePath = "/uploads/" + uniqueFileName;
        
        // 实际实现中，这里应该调用文件存储服务
        // fileStorageService.upload(file, uniqueFileName);
        
        return filePath;
    }

    /**
     * 生成下载URL
     */
    private String generateDownloadUrl(Long documentId) {
        // 这里应该生成实际的下载URL
        return "/api/documents/" + documentId + "/download";
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * 验证上传请求
     */
    private void validateUploadRequest(DocumentUploadRequest request) {
        if (request == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "上传请求不能为空");
        }
        if (request.getFile() == null || request.getFile().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文件不能为空");
        }
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文档标题不能为空");
        }
        if (request.getProjectId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "项目ID不能为空");
        }
        
        // 验证文件大小
        long maxFileSize = 10 * 1024 * 1024; // 10MB
        if (request.getFile().getSize() > maxFileSize) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文件大小不能超过10MB");
        }
        
        // 验证文件类型
        String contentType = request.getFile().getContentType();
        if (contentType == null || !isAllowedContentType(contentType)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不支持的文件类型");
        }
    }

    /**
     * 检查是否允许的文件类型
     */
    private boolean isAllowedContentType(String contentType) {
        String[] allowedTypes = {
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain",
            "text/markdown"
        };
        
        for (String allowedType : allowedTypes) {
            if (contentType.equals(allowedType)) {
                return true;
            }
        }
        return false;
    }
}
