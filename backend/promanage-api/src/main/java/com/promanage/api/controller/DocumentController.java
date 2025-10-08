package com.promanage.api.controller;

import com.promanage.api.dto.request.CreateDocumentFolderRequest;
import com.promanage.api.dto.request.UpdateDocumentFolderRequest;
import com.promanage.api.dto.response.*;
import com.promanage.common.domain.PageResult;
import com.promanage.common.domain.Result;
import com.promanage.service.dto.request.CreateDocumentRequest;
import com.promanage.service.dto.request.UpdateDocumentRequest;
import com.promanage.service.entity.Document;
import com.promanage.service.entity.DocumentVersion;
import com.promanage.common.entity.User;
import com.promanage.service.mapper.CommentMapper;
import com.promanage.service.service.IDocumentFolderService;
import com.promanage.service.service.IDocumentService;
import com.promanage.service.service.IUserService;
import com.promanage.infrastructure.security.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 文档管理控制器* <p>
 *提供文档的创建、查询、更新、删除等管理功能
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
    private final IDocumentFolderService documentFolderService;
    private final IUserService userService;
    private final CommentMapper commentMapper;

    /**
     * 创建文档
     *
     *@param projectId 项目ID* @param request   创建文档请求
     * @return 文档详情
     */
    @PostMapping("/projects/{projectId}")
    @Operation(summary = "创建文档", description = "在指定项目中创建新文档")
    @RequirePermission("document:create")
   public Result<DocumentDetailResponse> createDocument(
            @PathVariable Long projectId,
@Valid @RequestBody CreateDocumentRequest request) {

        log.info("创建文档请求, projectId={}, title={}", projectId, request.getTitle());

        Document document = documentService.createDocument(projectId, request);
        DocumentDetailResponse response = DocumentDetailResponse.fromEntity(document);

        // 填充额外信息
enrichDetailResponse(response, document);

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
    @RequirePermission("document:view")
    public Result<DocumentDetailResponse> getDocument(@PathVariable Long documentId) {

        log.info("获取文档详情请求, documentId={}", documentId);

        Document document = documentService.getDocumentById(documentId);
DocumentDetailResponse response = DocumentDetailResponse.fromEntity(document);

        // 填充额外信息
        enrichDetailResponse(response, document);

        log.info("获取文档详情成功, documentId={}", documentId);
        return Result.success(response);
    }

    /**
     * 更新文档
     *
*@param documentId 文档ID
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
        DocumentDetailResponse response= DocumentDetailResponse.fromEntity(document);

        // 填充额外信息enrichDetailResponse(response, document);

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
    @RequirePermission("document:delete")
    public Result<Void> deleteDocument(@PathVariable Long documentId) {
        
        log.info("删除文档请求, documentId={}", documentId);
        
        documentService.deleteDocument(documentId);
        
        log.info("文档删除成功, documentId={}", documentId);
        return Result.success();
    }

    /**
     * 获取文档文件夹列表*
     * @param projectId 项目ID（可选）
     * @param parentId 父文件夹ID（可选）
     * @param page 页码
     *@paramsize 每页大小
     * @return 文件夹列表
     */
    @GetMapping("/folders")
    @Operation(summary = "获取文档文件夹列表", description = "获取文档的文件夹列表")
    @RequirePermission("document:folder:view")
    public Result<PageResult<DocumentFolderResponse>> getDocumentFolders(
            @RequestParam(required= false) Long projectId,
            @RequestParam(required = false) Long parentId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        log.info("获取文档文件夹列表请求, projectId={}, parentId={}, page={}, size={}", 
                projectId, parentId, page, size);

        PageResult<com.promanage.service.entity.DocumentFolder> folders = 
               documentFolderService.listFolders(page, size, projectId, parentId);

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

    /**
     *创建文档文件夹
     *
     * @param request 创建文件夹请求
     * @return 文件夹详情*/
    @PostMapping("/folders")
    @Operation(summary = "创建文档文件夹", description = "创建新的文档文件夹")
    public Result<DocumentFolderResponse> createDocumentFolder(
            @Valid @RequestBody CreateDocumentFolderRequest request) {

        log.info("创建文档文件夹请求, name={}, projectId={}", request.getName(), request.getProjectId());

        com.promanage.service.entity.DocumentFolder folder = new com.promanage.service.entity.DocumentFolder();
        folder.setName(request.getName());
        folder.setDescription(request.getDescription());
       folder.setProjectId(request.getProjectId());
        folder.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        folder.setSortOrder(request.getSortOrder());
        
        // 从安全上下文获取当前用户ID
        Long currentUserId = com.promanage.infrastructure.security.SecurityUtils.getCurrentUserId()
.orElseThrow(() -> new com.promanage.common.exception.BusinessException(
                        com.promanage.common.domain.ResultCode.UNAUTHORIZED, "用户未登录"));
        folder.setCreatorId(currentUserId);

        Long folderId = documentFolderService.createFolder(folder);
        com.promanage.service.entity.DocumentFolder createdFolder = documentFolderService.getFolderById(folderId);
        DocumentFolderResponse response = DocumentFolderResponse.fromEntity(createdFolder);

        log.info("文档文件夹创建成功, folderId={}", folderId);
        return Result.success(response);
    }

    /**
     * 更新文档文件夹
     *
     * @param folderId文件夹ID
     * @param request 更新文件夹请求
     * @return 文件夹详情
     */
    @PutMapping("/folders/{folderId}")
    @Operation(summary = "更新文档文件夹", description = "更新指定文档文件夹的信息")
    public Result<DocumentFolderResponse> updateDocumentFolder(
            @PathVariable Long folderId,
            @Valid @RequestBody UpdateDocumentFolderRequest request){

        log.info("更新文档文件夹请求, folderId={}", folderId);

        com.promanage.service.entity.DocumentFolder folder = new com.promanage.service.entity.DocumentFolder();
        folder.setName(request.getName());
       folder.setDescription(request.getDescription());
       folder.setProjectId(request.getProjectId());
        folder.setParentId(request.getParentId());
folder.setSortOrder(request.getSortOrder());

        documentFolderService.updateFolder(folderId, folder);
        com.promanage.service.entity.DocumentFolder updatedFolder = documentFolderService.getFolderById(folderId);
        DocumentFolderResponse response = DocumentFolderResponse.fromEntity(updatedFolder);

        log.info("文档文件夹更新成功,folderId={}", folderId);
        return Result.success(response);
    }

    /**
     * 删除文档文件夹
     *
     * @param folderId 文件夹ID
     * @return 操作结果
*/
    @DeleteMapping("/folders/{folderId}")
    @Operation(summary = "删除文档文件夹", description = "删除指定文档文件夹")
    @RequirePermission("document:folder:delete")
    public Result<Void> deleteDocumentFolder(@PathVariable Long folderId) {
        
        log.info("删除文档文件夹请求, folderId={}", folderId);
documentFolderService.deleteFolder(folderId);
        
        log.info("文档文件夹删除成功,folderId={}", folderId);
        return Result.success();
    }

    /**
     * 获取文档文件夹树形结构
     *
     * @param projectId 项目ID（可选）
     * @return 文件夹树形结构
     */
    @GetMapping("/folders/tree")
    @Operation(summary = "获取文档文件夹树", description = "获取文档的文件夹组织结构树")
    @RequirePermission("document:folder:view")
    public Result<List<IDocumentFolderService.DocumentFolderTreeNode>> getDocumentFolderTree(
            @RequestParam(required = false) Long projectId){

        log.info("获取文档文件夹树请求, projectId={}", projectId);

        List<IDocumentFolderService.DocumentFolderTreeNode> folders = documentFolderService.getFolderTree(projectId);

        log.info("获取文档文件夹树成功, count={}", folders.size());
        return Result.success(folders);
    }

   /**
* 获取所有文档列表（跨项目）
     *
     * @param page      页码* @param size      每页大小
     * @param projectId 可选的项目ID过滤
     * @param status    可选的状态过滤
     * @param keyword  可选的关键词搜索
     * @return文档列表
     */
    @GetMapping
@Operation(summary = "获取所有文档列表", description = "获取当前用户可访问的所有文档列表，支持过滤和搜索")
    @RequirePermission("document:list")
    public Result<PageResult<DocumentResponse>> listAllDocuments(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
@RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {

        log.info("获取所有文档列表请求, page={},size={}, projectId={}, status={},keyword={}",
                page, size,projectId, status, keyword);

        //调用服务层查询文档
        PageResult<Document> documents = documentService.listAllDocuments(
                page, size, projectId, status, keyword);

        // 转换为响应DTO
List<DocumentResponse> responseList = documents.getList().stream()
                .map(DocumentResponse::fromEntity)
               .collect(Collectors.toList());

        // 批量填充用户信息
        enrichWithUserInfo(responseList);

        PageResult<DocumentResponse> response = PageResult.of(
                responseList,
                documents.getTotal(),
               documents.getPage(),
                documents.getPageSize()
       );

        log.info("获取所有文档列表成功, total={}",response.getTotal());
        return Result.success(response);
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
    @RequirePermission("document:list")
    public Result<PageResult<DocumentResponse>> listDocumentsByProject(
           @PathVariable Long projectId,
            @RequestParam(defaultValue = "1") Integer page,
@RequestParam(defaultValue ="20") Integer size) {

        log.info("获取项目文档列表请求, projectId={}, page={}, size={}", projectId, page, size);

        PageResult<Document> documentPage = documentService.listDocuments(projectId, page, size);
        List<DocumentResponse> documentResponses = documentPage.getList().stream()
                .map(DocumentResponse::fromEntity)
                .collect(Collectors.toList());

// 批量填充用户信息
        enrichWithUserInfo(documentResponses);

        PageResult<DocumentResponse> response = PageResult.of(
documentResponses,
                documentPage.getTotal(),
                documentPage.getPage(),
documentPage.getPageSize()
);

        log.info("获取项目文档列表成功, projectId={}, total={}", projectId, response.getTotal());
        return Result.success(response);
    }

    /**
*高级搜索文档
     *
     * @param page      页码
     * @param size      每页大小* @paramprojectId 项目ID（可选）
     * @param status    文档状态（可选）
     * @param keyword   搜索关键词（可选）
     * @param folderId  文件夹ID（可选）
     * @param creatorId 创建人ID（可选）
     * @paramtype      文档类型（可选）
     * @paramstartTime 创建时间开始（可选）
     * @param endTime   创建时间结束（可选）
     * @return 文档列表
     */
    @GetMapping("/search")
    @Operation(summary = "高级搜索文档",description = "支持多种过滤条件的文档搜索")
    @RequirePermission("document:search")
    public Result<PageResult<DocumentResponse>> searchDocuments(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long folderId,
            @RequestParam(required = false) Long creatorId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) java.time.LocalDateTime startTime,
            @RequestParam(required = false)java.time.LocalDateTime endTime) {

        log.info("高级搜索文档请求, page={}, size={}, projectId={}, status={}, keyword={}, folderId={}, creatorId={}, type={}, startTime={}, endTime={}",
                page, size, projectId, status, keyword, folderId, creatorId, type, startTime, endTime);

        //调用服务层搜索文档
        PageResult<Document> documents = documentService.searchDocuments(
                page, size, projectId, status, keyword, folderId, creatorId, type, startTime, endTime);

        // 转换为响应DTO
        List<DocumentResponse> responseList = documents.getList().stream()
.map(DocumentResponse::fromEntity)
                .collect(Collectors.toList());

        // 批量填充用户信息
        enrichWithUserInfo(responseList);

        PageResult<DocumentResponse> response= PageResult.of(
                responseList,
                documents.getTotal(),
               documents.getPage(),
                documents.getPageSize()
        );

        log.info("高级搜索文档成功, total={}", response.getTotal());
        return Result.success(response);
    }

   //==================== 辅助方法 ====================

    /**
     *填充文档响应的用户信息
     * <p>
     * 批量查询用户信息，避免N+1查询问题
     * </p>
     *
     * @param response 文档响应对象
     */
    private void enrichWithUserInfo(DocumentResponse response) {
        if (response== null) {
            return;
        }

        // 收集需要查询的用户ID
        Set<Long> userIds = new HashSet<>();
        if (response.getCreatorId() != null) {
            userIds.add(response.getCreatorId());
        }

        //批量查询用户信息
        if (!userIds.isEmpty()) {
            Map<Long, User> userMap = userService.getByIds(new ArrayList<>(userIds));

            // 填充创建者信息
            if (response.getCreatorId() != null) {
                User creator = userMap.get(response.getCreatorId());
                if (creator != null) {
response.setCreatorName(creator.getRealName() != null ? creator.getRealName() : creator.getUsername());
                    response.setCreatorAvatar(creator.getAvatar());
                }
            }
      }
    }

    /**
     * 批量填充文档响应中的用户信息
     *
     * @param documents 文档响应列表
     */
    private void enrichWithUserInfo(List<DocumentResponse> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }

        // 收集所有需要查询的用户ID
        Set<Long> userIds = new HashSet<>();
        Set<Long> projectIds = new HashSet<>();
        Set<Long> folderIds = new HashSet<>();

        for (DocumentResponse doc : documents) {
            if (doc.getCreatorId() != null) {
                userIds.add(doc.getCreatorId());
            }
            if (doc.getProjectId() != null) {
                projectIds.add(doc.getProjectId());
            }
            if (doc.getFolderId() != null && doc.getFolderId() > 0) {
                folderIds.add(doc.getFolderId());
            }
        }

        // 批量查询用户信息
        Map<Long, User> userMap = userIds.isEmpty() ? 
                Collections.emptyMap() : 
                userService.getByIds(new ArrayList<>(userIds))
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // 批量查询项目信息
        Map<Long, String> projectMap = projectIds.isEmpty() ? 
                Collections.emptyMap() : 
                documentService.getProjectNamesByIds(new ArrayList<>(projectIds));

        // 批量查询文件夹信息
        Map<Long, String> folderMap = folderIds.isEmpty() ? 
                Collections.emptyMap() : 
                documentFolderService.getFolderNamesByIds(new ArrayList<>(folderIds));

        // 填充信息
        for (DocumentResponse doc : documents) {
            // 填充创建者信息
            User creator = userMap.get(doc.getCreatorId());
            if (creator != null) {
                doc.setCreatorName(creator.getRealName() != null ? creator.getRealName() : creator.getUsername());
                doc.setCreatorAvatar(creator.getAvatar());
            }

            // 填充项目名称
            if (doc.getProjectId() != null) {
                doc.setProjectName(projectMap.get(doc.getProjectId()));
            }

            // 填充文件夹名称
            if (doc.getFolderId() != null && doc.getFolderId() > 0) {
                doc.setFolderName(folderMap.get(doc.getFolderId()));
            }
        }
    }



    /**
     * 填充文档详情响应的额外信息
     * <p>
     * 包括：用户信息、版本历史、统计信息
     * </p>
     *
     * @paramresponse 文档详情响应对象
     * @param document 文档实体
     */
    private void enrichDetailResponse(DocumentDetailResponse response, Document document) {
        if (response == null || document == null) {
            return;
        }

        // 1. 填充用户信息
        enrichWithUserInfo(response);

        //2. 填充版本历史
        try {
            List<DocumentVersion> versions = documentService.listVersions(document.getId());
            if (versions != null && !versions.isEmpty()) {
                // 收集版本创建者ID
                Set<Long> versionCreatorIds = versions.stream()
                     .map(DocumentVersion::getCreatorId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                // 批量查询版本创建者信息
                Map<Long, User> versionCreatorMap = versionCreatorIds.isEmpty() ?
                        Collections.emptyMap() : userService.getByIds(new ArrayList<>(versionCreatorIds));

               // 转换为DTO
                List<DocumentVersionResponse> versionResponses = versions.stream()
                        .map(version -> {
                            User creator = versionCreatorMap.get(version.getCreatorId());
                            return DocumentVersionResponse.builder()
                                    .id(version.getId())
                                    .documentId(version.getDocumentId())
                                 .version(version.getVersionNumber())
                                    .changeLog(version.getChangeLog())
                                    .fileUrl(version.getFileUrl())
                                    .fileSize(version.getFileSize())
                                    .contentHash(version.getContentHash())
                                    .creatorId(version.getCreatorId())
                                    .creatorName(creator != null ? (creator.getRealName() != null? creator.getRealName() : creator.getUsername()) : null)
                                    .creatorAvatar(creator != null ? creator.getAvatar() : null)
                                    .createTime(version.getCreateTime())
                                    .isCurrent(version.getIsCurrent())
                                    .build();
                        })
                        .collect(Collectors.toList());

                response.setVersions(versionResponses);
         }
        } catch(Exception e) {
            log.error("获取文档版本历史失败, documentId={}", document.getId(), e);
            response.setVersions(Collections.emptyList());
        }

        // 3. 填充统计信息
        try {
            DocumentStatistics statistics = DocumentStatistics.builder()
                    .totalVersions(response.getVersions()!= null ? response.getVersions().size() : 0)
                    .totalViews(document.getViewCount() != null ? document.getViewCount() : 0)
                    .weekViews(documentService.getWeekViewCount(document.getId()))
                    .favoriteCount(documentService.getFavoriteCount(document.getId()))
                    .commentCount(commentMapper.countByEntityTypeAndEntityId("DOCUMENT", document.getId()))
                    .build();

            response.setStatistics(statistics);
        } catch (Exception e) {
            log.error("获取文档统计信息失败, documentId={}", document.getId(), e);
        }

        // 4. 关联文档（暂不实现，需要创建DocumentRelation实体）
        response.setRelatedDocuments(Collections.emptyList());
    }

    }