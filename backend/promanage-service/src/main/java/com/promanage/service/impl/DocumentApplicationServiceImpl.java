package com.promanage.service.impl;

import com.promanage.service.dto.response.DocumentDetailDTO;
import com.promanage.service.dto.response.DocumentStatisticsDTO;
import com.promanage.service.dto.response.DocumentVersionDTO;
import com.promanage.common.entity.User;
import com.promanage.common.exception.BusinessException;
import com.promanage.common.domain.ResultCode;
import com.promanage.infrastructure.security.SecurityUtils;
import com.promanage.service.entity.Document;
import com.promanage.service.entity.DocumentVersion;
import com.promanage.service.mapper.CommentMapper;
import com.promanage.service.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentApplicationServiceImpl implements IDocumentApplicationService {

    private final IDocumentService documentService;
    private final IDocumentVersionService documentVersionService; // Assuming this service exists or will be created
    private final IDocumentViewCountService documentViewCountService;
    private final IDocumentFavoriteService documentFavoriteService;
    private final IUserService userService;
    private final CommentMapper commentMapper; // Temporarily keep mapper, should be replaced by ICommentService

    @Override
    @Transactional(readOnly = true)
    public DocumentDetailDTO getDocumentDetails(Long documentId) {
        log.info("Fetching full details for documentId={}", documentId);

        // 1. Get the core document entity
        // The getById method already performs the necessary permission checks
        Document document = documentService.getById(documentId, SecurityUtils.getCurrentUserId().orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录")), true); // incrementView = true

        // 2. Start building the response DTO
        DocumentDetailDTO response = DocumentDetailDTO.fromEntity(document);

        // 3. Batch fetch related data
        List<DocumentVersion> versions = documentVersionService.listVersions(documentId, SecurityUtils.getCurrentUserId().orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录")));
        int favoriteCount = documentFavoriteService.getFavoriteCount(documentId);
        int commentCount = commentMapper.countByEntityTypeAndEntityId("DOCUMENT", documentId);
        int weeklyViewCount = documentViewCountService.getWeeklyViewCount(documentId);

        // 4. Hydrate the DTO
        enrichWithVersions(response, versions);
        enrichWithStatistics(response, document, versions, favoriteCount, commentCount, weeklyViewCount);
        enrichWithUserInfo(response);

        log.info("Successfully assembled full details for documentId={}", documentId);
        return response;
    }

    @Override
    @Transactional
    public DocumentDetailDTO createDocument(Long projectId, com.promanage.service.dto.request.CreateDocumentRequest request) {
        log.info("Creating document via application service, projectId={}, title={}", projectId, request.getTitle());

        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录"));

        // Create the core document
        Document createdDocument = documentService.createDocument(projectId, request, currentUserId);

        // Get full details for the response (re-use getDocumentDetails logic)
        return getDocumentDetails(createdDocument.getId());
    }

    private void enrichWithVersions(DocumentDetailDTO response, List<DocumentVersion> versions) {
        if (versions == null || versions.isEmpty()) {
            response.setVersions(Collections.emptyList());
            return;
        }

        Set<Long> creatorIds = versions.stream()
                .map(DocumentVersion::getCreatorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, User> userMap = userService.getByIds(new ArrayList<>(creatorIds));

        List<DocumentVersionDTO> versionResponses = versions.stream().map(version -> {
            User creator = userMap.get(version.getCreatorId());
            return DocumentVersionDTO.builder()
                    .id(version.getId())
                    .documentId(version.getDocumentId())
                    .version(version.getVersionNumber())
                    .changeLog(version.getChangeLog())
                    .creatorId(version.getCreatorId())
                    .creatorName(creator != null ? (creator.getRealName() != null ? creator.getRealName() : creator.getUsername()) : null)
                    .creatorAvatar(creator != null ? creator.getAvatar() : null)
                    .createTime(version.getCreateTime())
                    .isCurrent(version.getIsCurrent())
                    .build();
        }).collect(Collectors.toList());

        response.setVersions(versionResponses);
    }

    private void enrichWithStatistics(DocumentDetailDTO response, Document document, List<DocumentVersion> versions, int favoriteCount, int commentCount, int weeklyViewCount) {
        DocumentStatisticsDTO statistics = DocumentStatisticsDTO.builder()
                .totalVersions(versions != null ? versions.size() : 0)
                .totalViews(document.getViewCount() != null ? document.getViewCount() : 0)
                .weekViews(weeklyViewCount)
                .favoriteCount(favoriteCount)
                .commentCount(commentCount)
                .build();
        response.setStatistics(statistics);
    }

    private void enrichWithUserInfo(DocumentDetailDTO response) {
        if (response == null || response.getCreatorId() == null) {
            return;
        }
        User creator = userService.getById(response.getCreatorId());
        if (creator != null) {
            response.setCreatorName(creator.getRealName() != null ? creator.getRealName() : creator.getUsername());
            response.setCreatorAvatar(creator.getAvatar());
        }
    }
}
