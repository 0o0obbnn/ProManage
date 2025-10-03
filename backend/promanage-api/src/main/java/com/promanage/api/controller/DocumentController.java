package com.promanage.api.controller;

import com.promanage.service.dto.request.CreateDocumentRequest;
import com.promanage.service.dto.request.UpdateDocumentRequest;
import com.promanage.api.dto.response.DocumentDetailResponse;
import com.promanage.api.dto.response.DocumentResponse;
import com.promanage.common.domain.PageResult;
import com.promanage.common.domain.Result;
import com.promanage.service.entity.Document;
import com.promanage.service.service.IDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 文档管理控制器
 * <p>
 * 提供文档的创建、查询、更新、删除等管理功能
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/documents")
@Tag(name = "文档管理", description = "文档创建、查询、更新、删除等接口")
@RequiredArgsConstructor
public class DocumentController {

    private final IDocumentService documentService;

    /**
     * 创建文档
     *
     * @param projectId 项目ID
     * @param request   创建文档请求
     * @return 文档详情
     */
    @PostMapping("/projects/{projectId}")
    @Operation(summary = "创建文档", description = "在指定项目中创建新文档")
    public Result<DocumentDetailResponse> createDocument(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateDocumentRequest request) {
        
        log.info("创建文档请求, projectId={}, title={}", projectId, request.getTitle());
        
        Document document = documentService.createDocument(projectId, request);
        DocumentDetailResponse response = DocumentDetailResponse.fromEntity(document);
        
        log.info("文档创建成功, documentId={}", document.getId());
        return Result.success(response);
    }

    /**
     * 获取文档详情
     *
     * @param documentId 文档ID
     * @return 文档详情
     */
    @GetMapping("/{documentId}")
    @Operation(summary = "获取文档详情", description = "根据文档ID获取文档详细信息")
    public Result<DocumentDetailResponse> getDocument(@PathVariable Long documentId) {
        
        log.info("获取文档详情请求, documentId={}", documentId);
        
        Document document = documentService.getDocumentById(documentId);
        DocumentDetailResponse response = DocumentDetailResponse.fromEntity(document);
        
        log.info("获取文档详情成功, documentId={}", documentId);
        return Result.success(response);
    }

    /**
     * 更新文档
     *
     * @param documentId 文档ID
     * @param request    更新文档请求
     * @return 文档详情
     */
    @PutMapping("/{documentId}")
    @Operation(summary = "更新文档", description = "更新指定文档的信息")
    public Result<DocumentDetailResponse> updateDocument(
            @PathVariable Long documentId,
            @Valid @RequestBody UpdateDocumentRequest request) {
        
        log.info("更新文档请求, documentId={}", documentId);
        
        Document document = documentService.updateDocument(documentId, request);
        DocumentDetailResponse response = DocumentDetailResponse.fromEntity(document);
        
        log.info("文档更新成功, documentId={}", documentId);
        return Result.success(response);
    }

    /**
     * 删除文档
     *
     * @param documentId 文档ID
     * @return 操作结果
     */
    @DeleteMapping("/{documentId}")
    @Operation(summary = "删除文档", description = "删除指定文档")
    public Result<Void> deleteDocument(@PathVariable Long documentId) {
        
        log.info("删除文档请求, documentId={}", documentId);
        
        documentService.deleteDocument(documentId);
        
        log.info("文档删除成功, documentId={}", documentId);
        return Result.success();
    }

    /**
     * 获取项目文档列表
     *
     * @param projectId 项目ID
     * @param page      页码
     * @param size      每页大小
     * @return 文档列表
     */
    @GetMapping("/projects/{projectId}")
    @Operation(summary = "获取项目文档列表", description = "获取指定项目下的文档列表")
    public Result<PageResult<DocumentResponse>> listDocuments(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        log.info("获取项目文档列表请求, projectId={}, page={}, size={}", projectId, page, size);
        
        PageResult<Document> documentPage = documentService.listDocuments(projectId, page, size);
        PageResult<DocumentResponse> response = PageResult.of(
                documentPage.getList().stream().map(DocumentResponse::fromEntity).toList(),
                documentPage.getTotal(),
                documentPage.getPage(),
                documentPage.getPageSize()
        );
        
        log.info("获取项目文档列表成功, projectId={}, total={}", projectId, response.getTotal());
        return Result.success(response);
    }
}