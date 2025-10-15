package com.promanage.api.controller;

import com.promanage.api.dto.request.CreateDocumentFolderRequest;
import com.promanage.api.dto.request.UpdateDocumentFolderRequest;
import com.promanage.api.dto.response.DocumentAttachmentResponse;
import com.promanage.api.dto.response.DocumentDetailResponse;
import com.promanage.api.dto.response.DocumentFolderResponse;
import com.promanage.api.dto.response.DocumentRelationResponse;
import com.promanage.api.dto.response.DocumentResponse;
import com.promanage.api.dto.response.DocumentStatistics;
import com.promanage.api.dto.response.DocumentUserSummary;
import com.promanage.api.dto.response.DocumentVersionResponse;
import com.promanage.api.dto.response.PaginatedDataResponse;
import com.promanage.common.domain.Result;
import com.promanage.common.domain.ResultCode;
import com.promanage.common.entity.User;
import com.promanage.common.exception.BusinessException;
import com.promanage.common.result.PageResult;
import com.promanage.infrastructure.security.RequirePermission;
import com.promanage.infrastructure.security.SecurityUtils;
import com.promanage.service.dto.request.CreateDocumentRequest;
import com.promanage.service.dto.request.DocumentSearchRequest;
import com.promanage.service.dto.request.UpdateDocumentRequest;
import com.promanage.service.entity.Document;
import com.promanage.service.entity.DocumentFolder;
import com.promanage.service.entity.DocumentVersion;
import com.promanage.service.mapper.CommentMapper;
import com.promanage.service.service.IDocumentFolderService;
import com.promanage.service.service.IDocumentService;
import com.promanage.service.service.ITagService;
import com.promanage.service.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文档管理控制器。
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@Tag(name = "文档管理", description = "提供文档的创建、查询、更新、删除等接口")
@RequiredArgsConstructor
public class DocumentController {

    private static final String DOCUMENT_ENTITY_TYPE = "DOCUMENT";

    private final IDocumentService documentService;
    private final IDocumentFolderService documentFolderService;
    private final IUserService userService;
    private final CommentMapper commentMapper;
    private final ITagService tagService;

    @PostMapping("/projects/{projectId}/documents")
    @Operation(summary = "创建文档", description = "在指定项目中创建新文档")
    @RequirePermission("document:create")
    public ResponseEntity<Result<DocumentDetailResponse>> createDocument(@PathVariable Long projectId,
                                                                         @Valid @RequestBody CreateDocumentRequest request) {
        log.info("创建文档请求, projectId={}, title={}", projectId, request.getTitle());

        Long currentUserId = currentUserId();
        ensureProjectConsistency(projectId, request.getProjectId());
        request.setProjectId(projectId);

        Document document = documentService.createDocument(projectId, request, currentUserId);
        DocumentDetailResponse response = DocumentDetailResponse.fromEntity(document);
        populateDetailResponse(response, document, currentUserId);

        log.info("文档创建成功, documentId={}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(response));
    }

    @GetMapping("/documents/{documentId}")
    @Operation(summary = "获取文档详情", description = "根据文档ID获取文档详细信息")
    @RequirePermission("document:view")
    public Result<DocumentDetailResponse> getDocument(@PathVariable Long documentId) {
        log.info("获取文档详情请求, documentId={}", documentId);

        Long currentUserId = currentUserId();
        Document document = documentService.getById(documentId, currentUserId, true);
        DocumentDetailResponse response = DocumentDetailResponse.fromEntity(document);
        populateDetailResponse(response, document, currentUserId);

        log.info("获取文档详情成功, documentId={}", documentId);
        return Result.success(response);
    }

    @PutMapping("/documents/{documentId}")
    @Operation(summary = "更新文档", description = "更新指定文档的信息")
    @RequirePermission("document:update")
    public ResponseEntity<Result<DocumentDetailResponse>> updateDocument(@PathVariable Long documentId,
                                                                         @Valid @RequestBody UpdateDocumentRequest request) {
        log.info("更新文档请求, documentId={}", documentId);

        Long currentUserId = currentUserId();
        Document document = documentService.updateDocument(documentId, request, currentUserId);
        DocumentDetailResponse response = DocumentDetailResponse.fromEntity(document);
        populateDetailResponse(response, document, currentUserId);

        log.info("文档更新成功, documentId={}", documentId);
        return ResponseEntity.ok(Result.success(response));
    }

    @DeleteMapping("/documents/{documentId}")
    @Operation(summary = "删除文档", description = "删除指定文档")
    @RequirePermission("document:delete")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long documentId) {
        log.info("删除文档请求, documentId={}", documentId);

        Long currentUserId = currentUserId();
        documentService.deleteDocument(documentId, currentUserId);

        log.info("文档删除成功, documentId={}", documentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/documents/folders")
    @Operation(summary = "获取文档文件夹列表", description = "获取文档的文件夹列表")
    @RequirePermission("document:folder:view")
    public Result<PageResult<DocumentFolderResponse>> getDocumentFolders(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long parentId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        log.info("获取文档文件夹列表请求, projectId={}, parentId={}, page={}, size={}",
                projectId, parentId, page, size);

        PageResult<DocumentFolder> folders = documentFolderService.listFolders(page, size, projectId, parentId);
        List<DocumentFolderResponse> responseList = folders.getList().stream()
                .map(DocumentFolderResponse::fromEntity)
                .collect(Collectors.toList());

        PageResult<DocumentFolderResponse> response = PageResult.of(
                responseList,
                folders.getTotal(),
                folders.getPage(),
                folders.getPageSize()
        );

        log.info("获取文档文件夹列表成功, count={}", response.getList().size());
        return Result.success(response);
    }

    @PostMapping("/documents/folders")
    @Operation(summary = "创建文档文件夹", description = "创建新的文档文件夹")
    @RequirePermission("document:folder:create")
    public Result<DocumentFolderResponse> createDocumentFolder(
            @Valid @RequestBody CreateDocumentFolderRequest request) {

        log.info("创建文档文件夹请求, name={}, projectId={}", request.getName(), request.getProjectId());

        Long currentUserId = currentUserId();
        DocumentFolder folder = new DocumentFolder();
        folder.setName(request.getName());
        folder.setDescription(request.getDescription());
        folder.setProjectId(request.getProjectId());
        folder.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        folder.setSortOrder(request.getSortOrder());
        folder.setCreatorId(currentUserId);

        Long folderId = documentFolderService.createFolder(folder);
        DocumentFolder createdFolder = documentFolderService.getFolderById(folderId);
        DocumentFolderResponse response = DocumentFolderResponse.fromEntity(createdFolder);

        log.info("文档文件夹创建成功, folderId={}", folderId);
        return Result.success(response);
    }

    @PutMapping("/documents/folders/{folderId}")
    @Operation(summary = "更新文档文件夹", description = "更新指定文档文件夹的信息")
    @RequirePermission("document:folder:update")
    public Result<DocumentFolderResponse> updateDocumentFolder(@PathVariable Long folderId,
                                                               @Valid @RequestBody UpdateDocumentFolderRequest request) {
        log.info("更新文档文件夹请求, folderId={}", folderId);

        Long currentUserId = currentUserId();
        DocumentFolder folder = new DocumentFolder();
        folder.setName(request.getName());
        folder.setDescription(request.getDescription());
        folder.setProjectId(request.getProjectId());
        folder.setParentId(request.getParentId());
        folder.setSortOrder(request.getSortOrder());
        folder.setUpdaterId(currentUserId);

        documentFolderService.updateFolder(folderId, folder);
        DocumentFolder updatedFolder = documentFolderService.getFolderById(folderId);
        DocumentFolderResponse response = DocumentFolderResponse.fromEntity(updatedFolder);

        log.info("文档文件夹更新成功, folderId={}", folderId);
        return Result.success(response);
    }

    @DeleteMapping("/documents/folders/{folderId}")
    @Operation(summary = "删除文档文件夹", description = "删除指定文档文件夹")
    @RequirePermission("document:folder:delete")
    public Result<Void> deleteDocumentFolder(@PathVariable Long folderId) {
        log.info("删除文档文件夹请求, folderId={}", folderId);

        documentFolderService.deleteFolder(folderId);

        log.info("文档文件夹删除成功, folderId={}", folderId);
        return Result.success();
    }

    @GetMapping("/documents/folders/tree")
    @Operation(summary = "获取文档文件夹树", description = "获取文档的文件夹组织结构树")
    @RequirePermission("document:folder:view")
    public Result<List<IDocumentFolderService.DocumentFolderTreeNode>> getDocumentFolderTree(
            @RequestParam(required = false) Long projectId) {

        log.info("获取文档文件夹树请求, projectId={}", projectId);

        List<IDocumentFolderService.DocumentFolderTreeNode> folders = documentFolderService.getFolderTree(projectId);

        log.info("获取文档文件夹树成功, count={}", folders.size());
        return Result.success(folders);
    }

    @GetMapping("/documents")
    @Operation(summary = "获取所有文档列表", description = "获取当前用户可访问的所有文档列表，支持过滤和搜索")
    @RequirePermission("document:list")
    public ResponseEntity<Result<PaginatedDataResponse<DocumentResponse>>> listAllDocuments(@RequestParam(defaultValue = "1") Integer page,
                                                                                           @RequestParam(defaultValue = "20") Integer size,
                                                                                           @RequestParam(required = false) Long projectId,
                                                                                           @RequestParam(required = false) String status,
                                                                                           @RequestParam(required = false) String keyword) {

        log.info("获取所有文档列表请求, page={}, size={}, projectId={}, status={}, keyword={}",
                page, size, projectId, status, keyword);

        Long currentUserId = currentUserId();
        PageResult<Document> documents = documentService.listAllDocuments(page, size, projectId, status, keyword);
        List<DocumentResponse> responseList = documents.getList().stream()
                .map(DocumentResponse::fromEntity)
                .collect(Collectors.toList());

        populateDocumentResponses(responseList, currentUserId);

        PaginatedDataResponse<DocumentResponse> response = PaginatedDataResponse.<DocumentResponse>builder()
                .data(responseList)
                .total(documents.getTotal())
                .page(documents.getPage())
                .pageSize(documents.getPageSize())
                .totalPages(documents.getTotalPages())
                .hasNext(documents.getHasNext())
                .hasPrevious(documents.getHasPrevious())
                .build();

        log.info("获取所有文档列表成功, total={}", response.getTotal());
        return ResponseEntity.ok(Result.success(response));
    }

    @GetMapping("/projects/{projectId}/documents")
    @Operation(summary = "获取项目文档列表", description = "获取指定项目下的文档列表")
    @RequirePermission("document:list")
    public ResponseEntity<Result<PaginatedDataResponse<DocumentResponse>>> listDocumentsByProject(@PathVariable Long projectId,
                                                                                                 @RequestParam(defaultValue = "1") Integer page,
                                                                                                 @RequestParam(defaultValue = "20") Integer size,
                                                                                                 @RequestParam(name = "status", required = false) String status,
                                                                                                 @RequestParam(name = "search", required = false) String search,
                                                                                                 @RequestParam(name = "keyword", required = false) String keyword,
                                                                                                 @RequestParam(name = "author_id", required = false) Long authorId,
                                                                                                 @RequestParam(name = "creatorId", required = false) Long legacyCreatorId,
                                                                                                 @RequestParam(name = "category_id", required = false) Long categoryId,
                                                                                                 @RequestParam(name = "tags", required = false) String tags) {
        Long creatorId = legacyCreatorId != null ? legacyCreatorId : authorId;
        String resolvedKeyword = search != null ? search : keyword;
        log.info("获取项目文档列表请求, projectId={}, page={}, size={}, status={}, keyword={}, creatorId={}, categoryId={}, tags={}",
                projectId, page, size, status, resolvedKeyword, creatorId, categoryId, tags);

        Long currentUserId = currentUserId();
        DocumentSearchRequest request = new DocumentSearchRequest();
        request.setPage(page);
        request.setPageSize(size);
        request.setProjectId(projectId);
        request.setStatus(status);
        request.setKeyword(resolvedKeyword);
        request.setCreatorId(creatorId);
        request.setCategoryId(categoryId);
        request.setTags(tags);

        PageResult<Document> documents = documentService.searchDocuments(request, currentUserId);
        List<DocumentResponse> responseList = documents.getList().stream()
                .map(DocumentResponse::fromEntity)
                .collect(Collectors.toList());

        populateDocumentResponses(responseList, currentUserId);

        PaginatedDataResponse<DocumentResponse> response = PaginatedDataResponse.<DocumentResponse>builder()
                .data(responseList)
                .total(documents.getTotal())
                .page(documents.getPage())
                .pageSize(documents.getPageSize())
                .totalPages(documents.getTotalPages())
                .hasNext(documents.getHasNext())
                .hasPrevious(documents.getHasPrevious())
                .build();

        log.info("获取项目文档列表成功, projectId={}, total={}", projectId, response.getTotal());
        return ResponseEntity.ok(Result.success(response));
    }

    @GetMapping("/documents/search")
    @Operation(summary = "高级搜索文档", description = "支持多种过滤条件的文档搜索")
    @RequirePermission("document:search")
    public ResponseEntity<Result<PaginatedDataResponse<DocumentResponse>>> searchDocuments(@RequestParam(defaultValue = "1") Integer page,
                                                                                          @RequestParam(defaultValue = "20") Integer size,
                                                                                          @RequestParam(required = false) Long projectId,
                                                                                          @RequestParam(name = "status", required = false) String status,
                                                                                          @RequestParam(name = "search", required = false) String search,
                                                                                          @RequestParam(name = "keyword", required = false) String keyword,
                                                                                          @RequestParam(required = false) Long folderId,
                                                                                          @RequestParam(name = "creatorId", required = false) Long legacyCreatorId,
                                                                                          @RequestParam(name = "author_id", required = false) Long authorId,
                                                                                          @RequestParam(required = false) String type,
                                                                                          @RequestParam(name = "category_id", required = false) Long categoryId,
                                                                                          @RequestParam(name = "tags", required = false) String tags,
                                                                                          @RequestParam(required = false) LocalDateTime startTime,
                                                                                          @RequestParam(required = false) LocalDateTime endTime) {

        Long creatorId = legacyCreatorId != null ? legacyCreatorId : authorId;
        String resolvedKeyword = search != null ? search : keyword;
        log.info("高级搜索文档请求, page={}, size={}, projectId={}, status={}, keyword={}, folderId={}, creatorId={}, type={}, categoryId={}, tags={}, startTime={}, endTime={}",
                page, size, projectId, status, resolvedKeyword, folderId, creatorId, type, categoryId, tags, startTime, endTime);

        Long currentUserId = currentUserId();
        DocumentSearchRequest request = new DocumentSearchRequest();
        request.setPage(page);
        request.setPageSize(size);
        request.setProjectId(projectId);
        request.setStatus(status);
        request.setKeyword(resolvedKeyword);
        request.setFolderId(folderId);
        request.setCreatorId(creatorId);
        request.setType(type);
        request.setCategoryId(categoryId);
        request.setTags(tags);
        request.setStartTime(startTime);
        request.setEndTime(endTime);

        PageResult<Document> documents = documentService.searchDocuments(request, currentUserId);
        List<DocumentResponse> responseList = documents.getList().stream()
                .map(DocumentResponse::fromEntity)
                .collect(Collectors.toList());

        populateDocumentResponses(responseList, currentUserId);

        PaginatedDataResponse<DocumentResponse> response = PaginatedDataResponse.<DocumentResponse>builder()
                .data(responseList)
                .total(documents.getTotal())
                .page(documents.getPage())
                .pageSize(documents.getPageSize())
                .totalPages(documents.getTotalPages())
                .hasNext(documents.getHasNext())
                .hasPrevious(documents.getHasPrevious())
                .build();

        log.info("高级搜索文档成功, total={}", response.getTotal());
        return ResponseEntity.ok(Result.success(response));
    }

    @GetMapping("/documents/{documentId}/versions")
    @Operation(summary = "获取文档版本历史", description = "获取文档的所有版本信息")
    @RequirePermission("document:view")
    public ResponseEntity<Result<PaginatedDataResponse<DocumentVersionResponse>>> getDocumentVersions(
            @PathVariable Long documentId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        Long currentUserId = currentUserId();
        List<DocumentVersion> versions = documentService.listVersions(documentId, currentUserId);
        int total = versions != null ? versions.size() : 0;
        if (total == 0) {
            PaginatedDataResponse<DocumentVersionResponse> empty = PaginatedDataResponse.<DocumentVersionResponse>builder()
                    .data(Collections.emptyList())
                    .total(0L)
                    .page(1)
                    .pageSize(size != null ? size : 20)
                    .totalPages(0)
                    .hasNext(false)
                    .hasPrevious(false)
                    .build();
            return ResponseEntity.ok(Result.success(empty));
        }

        int pageNumber = (page == null || page < 1) ? 1 : page;
        int pageSize = (size == null || size < 1) ? 20 : size;
        int fromIndex = Math.min((pageNumber - 1) * pageSize, total);
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<DocumentVersion> pageItems = versions.subList(fromIndex, toIndex);

        Set<Long> creatorIds = pageItems.stream()
                .map(DocumentVersion::getCreatorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, User> userMap = creatorIds.isEmpty()
                ? Collections.emptyMap()
                : userService.getByIds(new ArrayList<>(creatorIds));

        List<DocumentVersionResponse> responseList = pageItems.stream()
                .map(version -> {
                    User creator = userMap.get(version.getCreatorId());
                    DocumentUserSummary author = null;
                    if (creator != null) {
                        String displayName = creator.getRealName();
                        if (displayName == null || displayName.isBlank()) {
                            displayName = creator.getUsername();
                        }
                        author = DocumentUserSummary.builder()
                                .id(creator.getId())
                                .username(creator.getUsername())
                                .displayName(displayName)
                                .avatar(creator.getAvatar())
                                .build();
                    }
                    return DocumentVersionResponse.builder()
                            .id(version.getId())
                            .documentId(version.getDocumentId())
                            .version(version.getVersionNumber())
                            .title(version.getTitle())
                            .changeLog(version.getChangeLog())
                            .fileUrl(version.getFileUrl())
                            .fileSize(version.getFileSize())
                            .contentHash(version.getContentHash())
                            .creatorId(version.getCreatorId())
                            .creatorName(creator != null ? Objects.requireNonNullElse(creator.getRealName(), creator.getUsername()) : null)
                            .creatorAvatar(creator != null ? creator.getAvatar() : null)
                            .author(author)
                            .createTime(version.getCreateTime())
                            .isCurrent(version.getIsCurrent())
                            .build();
                })
                .collect(Collectors.toList());

        int totalPages = pageSize == 0 ? 0 : (int) Math.ceil((double) total / pageSize);

        PaginatedDataResponse<DocumentVersionResponse> pageResult = PaginatedDataResponse.<DocumentVersionResponse>builder()
                .data(responseList)
                .total((long) total)
                .page(pageNumber)
                .pageSize(pageSize)
                .totalPages(totalPages)
                .hasNext(pageNumber < totalPages)
                .hasPrevious(pageNumber > 1)
                .build();

        return ResponseEntity.ok(Result.success(pageResult));
    }

    @PostMapping("/documents/{documentId}/versions/{version}/restore")
    @Operation(summary = "恢复文档版本", description = "将文档恢复到指定版本")
    @RequirePermission("document:update")
    public ResponseEntity<Result<DocumentDetailResponse>> restoreDocumentVersion(@PathVariable Long documentId,
                                                                                @PathVariable String version) {
        Long currentUserId = currentUserId();
        Document restored = documentService.rollbackToVersion(documentId, version, currentUserId);
        DocumentDetailResponse response = DocumentDetailResponse.fromEntity(restored);
        populateDetailResponse(response, restored, currentUserId);
        return ResponseEntity.ok(Result.success(response));
    }

    private Long currentUserId() {
        return SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("用户未登录"));
    }

    private void ensureProjectConsistency(Long pathProjectId, Long bodyProjectId) {
        if (bodyProjectId != null && !Objects.equals(pathProjectId, bodyProjectId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "路径项目ID与请求体不一致");
        }
    }

    private void populateDocumentResponses(List<DocumentResponse> responses, Long currentUserId) {
        if (responses == null || responses.isEmpty()) {
            return;
        }

        Set<Long> userIds = new HashSet<>();
        Set<Long> projectIds = new HashSet<>();
        Set<Long> folderIds = new HashSet<>();

        for (DocumentResponse response : responses) {
            if (response.getCreatorId() != null) {
                userIds.add(response.getCreatorId());
            }
            if (response.getReviewerId() != null) {
                userIds.add(response.getReviewerId());
            }
            if (response.getUpdaterId() != null) {
                userIds.add(response.getUpdaterId());
            }
            if (response.getProjectId() != null) {
                projectIds.add(response.getProjectId());
            }
            if (response.getFolderId() != null && response.getFolderId() > 0) {
                folderIds.add(response.getFolderId());
            }
        }

        Map<Long, User> userMap = userIds.isEmpty()
                ? Collections.emptyMap()
                : userService.getByIds(new ArrayList<>(userIds));

        Map<Long, String> projectNameMap = projectIds.isEmpty()
                ? Collections.emptyMap()
                : documentService.getProjectNames(new ArrayList<>(projectIds), currentUserId);

        Map<Long, String> folderNameMap = folderIds.isEmpty()
                ? Collections.emptyMap()
                : documentFolderService.getFolderNamesByIds(new ArrayList<>(folderIds));

        for (DocumentResponse response : responses) {
            if (response.getCreatorId() != null) {
                User creator = userMap.get(response.getCreatorId());
                if (creator != null) {
                    String creatorDisplayName = creator.getRealName();
                    if (creatorDisplayName == null || creatorDisplayName.isBlank()) {
                        creatorDisplayName = creator.getUsername();
                    }
                    response.setCreatorName(creatorDisplayName);
                    response.setCreatorAvatar(creator.getAvatar());
                    response.setAuthor(DocumentUserSummary.builder()
                            .id(creator.getId())
                            .username(creator.getUsername())
                            .displayName(creatorDisplayName)
                            .avatar(creator.getAvatar())
                            .build());
                }
            }

            if (response.getReviewerId() != null) {
                User reviewer = userMap.get(response.getReviewerId());
                if (reviewer != null) {
                    String reviewerDisplayName = reviewer.getRealName();
                    if (reviewerDisplayName == null || reviewerDisplayName.isBlank()) {
                        reviewerDisplayName = reviewer.getUsername();
                    }
                    response.setReviewer(DocumentUserSummary.builder()
                            .id(reviewer.getId())
                            .username(reviewer.getUsername())
                            .displayName(reviewerDisplayName)
                            .avatar(reviewer.getAvatar())
                            .build());
                }
            }

            if (response.getUpdaterId() != null) {
                User updater = userMap.get(response.getUpdaterId());
                if (updater != null) {
                    String updaterDisplayName = updater.getRealName();
                    if (updaterDisplayName == null || updaterDisplayName.isBlank()) {
                        updaterDisplayName = updater.getUsername();
                    }
                    response.setUpdaterName(updaterDisplayName);
                }
            }

            if (response.getProjectId() != null) {
                response.setProjectName(projectNameMap.get(response.getProjectId()));
            }

            if (response.getFolderId() != null && response.getFolderId() > 0) {
                response.setFolderName(folderNameMap.get(response.getFolderId()));
            }
        }
    }

    private void populateDetailResponse(DocumentDetailResponse response, Document document, Long currentUserId) {
        if (response == null || document == null) {
            return;
        }

        populateDocumentResponses(Collections.singletonList(response), currentUserId);

        try {
            List<DocumentVersion> versions = documentService.listVersions(document.getId(), currentUserId);
            if (versions != null && !versions.isEmpty()) {
                Set<Long> versionCreatorIds = versions.stream()
                        .map(DocumentVersion::getCreatorId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                Map<Long, User> versionCreators = versionCreatorIds.isEmpty()
                        ? Collections.emptyMap()
                        : userService.getByIds(new ArrayList<>(versionCreatorIds));

                List<DocumentVersionResponse> versionResponses = versions.stream()
                        .map(version -> {
                            User creator = versionCreators.get(version.getCreatorId());
                            DocumentUserSummary author = null;
                            if (creator != null) {
                                String displayName = creator.getRealName();
                                if (displayName == null || displayName.isBlank()) {
                                    displayName = creator.getUsername();
                                }
                                author = DocumentUserSummary.builder()
                                        .id(creator.getId())
                                        .username(creator.getUsername())
                                        .displayName(displayName)
                                        .avatar(creator.getAvatar())
                                        .build();
                            }
                            return DocumentVersionResponse.builder()
                                    .id(version.getId())
                                    .documentId(version.getDocumentId())
                                    .version(version.getVersionNumber())
                                    .title(version.getTitle())
                                    .changeLog(version.getChangeLog())
                                    .fileUrl(version.getFileUrl())
                                    .fileSize(version.getFileSize())
                                    .contentHash(version.getContentHash())
                                    .creatorId(version.getCreatorId())
                                    .creatorName(creator != null ? Objects.requireNonNullElse(creator.getRealName(), creator.getUsername()) : null)
                                    .creatorAvatar(creator != null ? creator.getAvatar() : null)
                                    .author(author)
                                    .createTime(version.getCreateTime())
                                    .isCurrent(version.getIsCurrent())
                                    .build();
                        })
                        .collect(Collectors.toList());

                response.setVersions(versionResponses);
                response.setVersionsCount(versionResponses.size());
            } else {
                response.setVersions(Collections.emptyList());
                response.setVersionsCount(0);
            }
        } catch (Exception ex) {
            log.warn("获取文档版本历史失败, documentId={}", document.getId(), ex);
            response.setVersions(Collections.emptyList());
            response.setVersionsCount(0);
        }

        try {
            int weeklyViews = documentService.getWeeklyViewCount(document.getId(), currentUserId);
            int favoriteCount = documentService.getFavoriteCount(document.getId(), currentUserId);
            int commentCount = commentMapper.countByEntityTypeAndEntityId(DOCUMENT_ENTITY_TYPE, document.getId());
            int totalViews = document.getViewCount() != null ? document.getViewCount() : 0;

            DocumentStatistics statistics = DocumentStatistics.builder()
                    .totalVersions(response.getVersionsCount())
                    .totalViews(totalViews)
                    .weekViews(weeklyViews)
                    .favoriteCount(favoriteCount)
                    .commentCount(commentCount)
                    .build();

            response.setStatistics(statistics);
            response.setLikeCount(favoriteCount);
            response.setCommentsCount(commentCount);
        } catch (Exception ex) {
            log.warn("构建文档统计信息失败, documentId={}", document.getId(), ex);
        }

        try {
            List<String> tagNames = tagService.getTagsByDocumentId(document.getId()).stream()
                    .map(com.promanage.service.entity.Tag::getName)
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(name -> !name.isEmpty())
                    .collect(Collectors.toList());
            response.setTags(tagNames);
        } catch (Exception ex) {
            log.warn("加载文档标签失败, documentId={}", document.getId(), ex);
        }

        if (response.getAttachments() == null) {
            response.setAttachments(Collections.emptyList());
        }

        if (response.getRelations() == null) {
            response.setRelations(Collections.emptyList());
        }
    }
}
