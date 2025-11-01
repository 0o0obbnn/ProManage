package com.promanage.api.controller;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.promanage.api.dto.request.DocumentUploadRequest;
import com.promanage.api.dto.response.DocumentDetailResponse;
import com.promanage.common.domain.Result;
import com.promanage.common.domain.ResultCode;
import com.promanage.common.exception.BusinessException;
import com.promanage.infrastructure.security.SecurityUtils;
import com.promanage.service.dto.request.CreateDocumentRequest;
import com.promanage.service.dto.request.UpdateDocumentRequest;
import com.promanage.service.service.IDocumentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 文档管理控制器 (Rebuilt Version)
 *
 * <p>提供了文档管理的核心API。 关键功能 'uploadDocument' 已完整实现。 其他方法已作为存根(stub)实现，以允许编译和恢复服务。
 *
 * @author ProManage Team (Remediation)
 * @version 1.3
 * @since 2025-10-21
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@Tag(name = "文档管理", description = "文档管理相关接口")
@RequiredArgsConstructor
public class DocumentController {

  private final IDocumentService documentService;

  // --- Fully Implemented Feature ---

  @PostMapping(
      value = "/projects/{projectId}/documents/upload",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "上传文档", description = "上传一个新文档或文档的新版本")
  @PreAuthorize("@permissionCheck.hasPermission('document:upload')")
  public Result<DocumentDetailResponse> uploadDocument(
      @Parameter(description = "项目ID") @PathVariable Long projectId,
      @Parameter(description = "文件本身", required = true) @RequestPart("file") MultipartFile file,
      @Parameter(description = "文件元数据") @RequestPart("metadata") @Valid
          DocumentUploadRequest metadata) {
    log.info("文件上传请求, projectId={}, fileName={}", projectId, file.getOriginalFilename());

    if (file.isEmpty()) {
      throw new BusinessException(ResultCode.PARAM_ERROR, "上传的文件不能为空");
    }
    if (!Objects.equals(projectId, metadata.getProjectId())) {
      throw new BusinessException(ResultCode.PARAM_ERROR, "URL中的项目ID与元数据中的项目ID不匹配");
    }

    Long currentUserId = currentUserId();

    // 1. Map API DTO to Service DTO because they are incompatible types
    com.promanage.service.dto.request.DocumentUploadRequest serviceMetadata =
        new com.promanage.service.dto.request.DocumentUploadRequest();
    serviceMetadata.setProjectId(metadata.getProjectId());
    serviceMetadata.setFolderId(metadata.getFolderId());
    serviceMetadata.setDescription(metadata.getDescription());
    serviceMetadata.setTitle(file.getOriginalFilename()); // Use filename as title
    serviceMetadata.setFile(file);

    // 2. Call the service with the correct arguments
    com.promanage.service.dto.response.DocumentDTO documentDto =
        documentService.uploadDocument(
            projectId,
            currentUserId,
            serviceMetadata, // Pass the correctly typed and populated service DTO
            file);

    // 3. Map Service DTO to API Response DTO
    DocumentDetailResponse response = new DocumentDetailResponse();
    if (documentDto != null) {
      // 从组合对象中获取基本信息
      if (documentDto.getBasicInfo() != null) {
        response.setId(documentDto.getBasicInfo().getId());
        response.setTitle(documentDto.getBasicInfo().getTitle());
        response.setSummary(documentDto.getBasicInfo().getSummary());
        response.setType(documentDto.getBasicInfo().getType());
        response.setStatus(documentDto.getBasicInfo().getStatus());
      }
      
      // 从组合对象中获取项目信息
      if (documentDto.getProjectInfo() != null) {
        response.setProjectId(documentDto.getProjectInfo().getProjectId());
        response.setProjectName(documentDto.getProjectInfo().getProjectName());
      }
      
      // 从组合对象中获取文件信息
      if (documentDto.getFileInfo() != null) {
        response.setFileUrl(documentDto.getFileInfo().getFilePath());
        response.setFileSize(documentDto.getFileInfo().getFileSize());
        response.setCurrentVersion(documentDto.getFileInfo().getVersion() != null
            ? String.valueOf(documentDto.getFileInfo().getVersion())
            : null);
        response.setContent(documentDto.getFileInfo().getContent());
        response.setContentType(documentDto.getFileInfo().getContentType());
      }
      
      // 从组合对象中获取统计信息
      if (documentDto.getStatsInfo() != null) {
        response.setViewCount(documentDto.getStatsInfo().getViewCount());
        response.setTags(documentDto.getStatsInfo().getTags());
      }
      
      // 从组合对象中获取用户信息
      if (documentDto.getUserInfo() != null) {
        response.setCreatorId(documentDto.getUserInfo().getCreatorId());
        response.setCreatorName(documentDto.getUserInfo().getCreatorName());
        response.setUpdaterId(documentDto.getUserInfo().getUpdaterId());
        response.setUpdaterName(documentDto.getUserInfo().getUpdaterName());
      }
      
      // 时间信息
      response.setCreateTime(documentDto.getCreateTime());
      response.setUpdateTime(documentDto.getUpdateTime());
    }

    return Result.success(response);
  }

  // --- Stub Implementations for Compilation ---

  @PostMapping("/projects/{projectId}/documents")
  @Operation(summary = "创建文档 (STUB)", description = "在指定项目中创建新文档")
  @PreAuthorize("@permissionCheck.hasPermission('document:create')")
  public ResponseEntity<Result<DocumentDetailResponse>> createDocument(
      @PathVariable Long projectId, @Valid @RequestBody CreateDocumentRequest request) {
    log.warn("STUB IMPLEMENTATION: createDocument is not fully implemented.");
    // In a real implementation, you would call documentService.createDocument(...)
    // and return a detailed response.
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(Result.success(new DocumentDetailResponse()));
  }

  @GetMapping("/documents/{documentId}")
  @Operation(summary = "获取文档详情 (STUB)", description = "根据文档ID获取文档详细信息")
  @PreAuthorize("@permissionCheck.hasPermission('document:view')")
  public Result<DocumentDetailResponse> getDocument(@PathVariable Long documentId) {
    log.warn("STUB IMPLEMENTATION: getDocument is not fully implemented.");
    // In a real implementation, you would call documentService.getById(...)
    return Result.success(new DocumentDetailResponse());
  }

  @PutMapping("/documents/{documentId}")
  @Operation(summary = "更新文档 (STUB)", description = "更新指定文档的信息")
  @PreAuthorize("@permissionCheck.hasPermission('document:update')")
  public ResponseEntity<Result<DocumentDetailResponse>> updateDocument(
      @PathVariable Long documentId, @Valid @RequestBody UpdateDocumentRequest request) {
    log.warn("STUB IMPLEMENTATION: updateDocument is not fully implemented.");
    // In a real implementation, you would call documentService.updateDocument(...)
    return ResponseEntity.ok(Result.success(new DocumentDetailResponse()));
  }

  @DeleteMapping("/documents/{documentId}")
  @Operation(summary = "删除文档 (STUB)", description = "删除指定文档")
  @PreAuthorize("@permissionCheck.hasPermission('document:delete')")
  public ResponseEntity<Void> deleteDocument(@PathVariable Long documentId) {
    log.warn("STUB IMPLEMENTATION: deleteDocument is not fully implemented.");
    // In a real implementation, you would call documentService.deleteDocument(...)
    return ResponseEntity.noContent().build();
  }

  // --- Helper Methods ---

  private Long currentUserId() {
    return SecurityUtils.getCurrentUserId()
        .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录"));
  }
}
