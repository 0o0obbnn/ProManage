# Backend API Notes - Tags, Comments, and Document Details

## 1) Tag System

- Storage
  - Tags: `tags` (id, name, color, description, project_id, creator_id, usage_count, is_active, created_at, updated_at, deleted_at, deleted_by)
  - Relations: `document_tags` (id, document_id, tag_id, created_at, creator_id), unique (document_id, tag_id)

- Services
  - `ITagService`
    - `ensureTagsExist(List<String> tagNames) -> List<Tag>`: creates missing tags by name and returns all tags.
  - `IDocumentTagService`
    - `setDocumentTags(Long documentId, List<Long> tagIds)`: replaces document-tag relations atomically.

- Document integration (already wired)
  - `DocumentServiceImpl.createDocument(...)`: after document insert, ensures tag existence and persists relations if `request.getTags()` provided.
  - `DocumentServiceImpl.updateDocument(...)`: on update, ensures tag existence and resets relations if `request.getTags()` provided (null = no change, empty list = clear all).

- Querying by Tag (baseline)
  - Prefer adding repository-level joins for production filtering. As of now, keyword search remains unchanged; add a dedicated API for tag filters if needed (recommended):
    - GET `/api/documents?tag=Design` or `/api/documents?tagId=123` (to be implemented in controller/mapper via join on `document_tags`).

## 2) Comments

- Storage
  - `tb_document_comment` (id, document_id, user_id, content, create_time, update_time, deleted)

- Services
  - `IDocumentCommentService.countByDocumentId(Long documentId) -> int`

- Usage in details
  - `DocumentDetailResponse.fromEntityWithDetails(..., IDocumentCommentService, IDocumentRelationService)` sets `statistics.commentCount` via the comment service.

## 3) Related Documents

- Strategy
  - `IDocumentRelationService.findRelatedByTags(documentId, limit)` returns documents sharing any tag, ordered by update time desc.
  - Current heuristic-based; can be replaced by explicit relations in future.

## 4) Document Analytics

- Daily views tracked in Redis under `document:viewcount:daily:{documentId}:{yyyy-MM-dd}` with 8-day TTL.
- `DocumentServiceImpl.getWeekViewCount(documentId)` aggregates last 7 days.

## 5) Document Details DTO Usage

- Preferred:
  - `DocumentDetailResponse.fromEntityWithDetails(document, documentService, userService, documentVersionMapper, documentCommentService, documentRelationService)`
    - Populates: versions (with creator info), statistics (views/favorites/versions/comments), relatedDocuments.

- Minimal:
  - `DocumentDetailResponse.fromEntity(document)` returns core fields only; versions, statistics, relatedDocuments are null.

## 6) Test Case Export

- CSV export writes to local `exports/` directory and returns `/exports/{file}` URL. Excel/PDF intentionally deferred.

## 7) Migrations

- Added `database_migrations/V1.2.0__add_tags_and_comments.sql` to create `tags`, `document_tags`, and `tb_document_comment` with indexes and constraints.

## 8) Follow-ups (recommended)

- Add controller endpoints for tag CRUD and tag-filtered document queries.
- Add explicit related-document relations if business requires (beyond tag heuristic).
- Secure comment creation/listing endpoints with permissions and moderation.
