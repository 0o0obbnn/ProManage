# TASK-BE-004.3: 任务评论功能增强 - 阶段性开发总结

**任务编号**: TASK-BE-004.3
**任务名称**: 优化任务评论功能（@提及、附件、点赞）
**开发日期**: 2025-10-05
**开发者**: Claude Code
**当前状态**: 🔄 基础架构已完成，Service/Controller层待实现

---

## 📊 当前进度概览

### 已完成工作 (第一阶段)

| 子任务 | 内容 | 状态 | 完成度 |
|-------|------|------|--------|
| 数据模型设计 | 3个实体类 | ✅ 完成 | 100% |
| 数据访问层 | 3个Mapper接口 + XML | ✅ 完成 | 100% |
| DTO设计 | Request/Response更新 | ✅ 完成 | 100% |
| Service层实现 | 业务逻辑 | ⏸️ 待开始 | 0% |
| Controller层实现 | API端点 | ⏸️ 待开始 | 0% |

**第一阶段完成度**: 60% (基础架构完成)

---

## 🎯 已完成详细内容

### 1. 实体类设计 ✅

#### 1.1 CommentMention（评论@提及）
**文件**: `backend/promanage-service/src/main/java/com/promanage/service/entity/CommentMention.java`

**字段设计**:
```java
public class CommentMention extends BaseEntity {
    private Long id;                  // 提及记录ID
    private Long commentId;           // 评论ID
    private Long mentionedUserId;     // 被提及的用户ID
    private String commentType;       // 评论类型 (TASK_COMMENT/DOCUMENT_COMMENT)
    private Boolean isRead;           // 是否已读
}
```

**功能说明**:
- 记录评论中@提及的所有用户
- 支持未读提醒功能
- 支持多种评论类型（任务评论、文档评论）

#### 1.2 CommentAttachment（评论附件）
**文件**: `backend/promanage-service/src/main/java/com/promanage/service/entity/CommentAttachment.java`

**字段设计**:
```java
public class CommentAttachment extends BaseEntity {
    private Long id;                  // 附件ID
    private Long commentId;           // 评论ID
    private String fileName;          // 附件名称
    private String originalFileName;  // 附件原始名称
    private String filePath;          // 附件存储路径
    private String fileUrl;           // 附件访问URL
    private Long fileSize;            // 文件大小（字节）
    private String fileType;          // 文件类型/MIME类型
    private Long uploaderId;          // 上传者ID
}
```

**功能说明**:
- 支持多种文件类型上传
- 记录文件元数据（大小、类型等）
- 支持文件访问权限控制

#### 1.3 CommentLike（评论点赞）
**文件**: `backend/promanage-service/src/main/java/com/promanage/service/entity/CommentLike.java`

**字段设计**:
```java
public class CommentLike extends BaseEntity {
    private Long id;                  // 点赞记录ID
    private Long commentId;           // 评论ID
    private Long userId;              // 点赞用户ID
    private String commentType;       // 评论类型
}
```

**功能说明**:
- 记录用户点赞行为
- 支持点赞/取消点赞
- 防止重复点赞

---

### 2. 数据访问层（Mapper）✅

#### 2.1 CommentMentionMapper
**文件**:
- Interface: `backend/promanage-service/src/main/java/com/promanage/service/mapper/CommentMentionMapper.java`
- XML: `backend/promanage-service/src/main/resources/mapper/CommentMentionMapper.xml`

**核心方法**:
```java
// 根据评论ID查询所有提及记录
List<CommentMention> findByCommentId(Long commentId);

// 根据评论ID查询被提及的用户ID列表
List<Long> findMentionedUserIdsByCommentId(Long commentId);

// 根据用户ID查询该用户被提及的评论列表
List<CommentMention> findByMentionedUserId(Long userId, String commentType, Boolean isRead);

// 批量插入提及记录
int batchInsert(List<CommentMention> mentions);

// 标记提及为已读
int markAsRead(Long id);

// 删除评论的所有提及记录
int deleteByCommentId(Long commentId);
```

#### 2.2 CommentAttachmentMapper
**文件**:
- Interface: `backend/promanage-service/src/main/java/com/promanage/service/mapper/CommentAttachmentMapper.java`
- XML: `backend/promanage-service/src/main/resources/mapper/CommentAttachmentMapper.xml`

**核心方法**:
```java
// 根据评论ID查询所有附件
List<CommentAttachment> findByCommentId(Long commentId);

// 统计评论的附件数量
int countByCommentId(Long commentId);

// 删除评论的所有附件记录
int deleteByCommentId(Long commentId);

// 批量插入附件
int batchInsert(List<CommentAttachment> attachments);
```

#### 2.3 CommentLikeMapper
**文件**:
- Interface: `backend/promanage-service/src/main/java/com/promanage/service/mapper/CommentLikeMapper.java`
- XML: `backend/promanage-service/src/main/resources/mapper/CommentLikeMapper.xml`

**核心方法**:
```java
// 根据评论ID查询所有点赞记录
List<CommentLike> findByCommentId(Long commentId);

// 统计评论的点赞数量
int countByCommentId(Long commentId);

// 查询用户是否已点赞该评论
CommentLike findByCommentIdAndUserId(Long commentId, Long userId);

// 检查用户是否已点赞该评论
boolean existsByCommentIdAndUserId(Long commentId, Long userId);

// 删除点赞记录
int deleteByCommentIdAndUserId(Long commentId, Long userId);

// 删除评论的所有点赞记录
int deleteByCommentId(Long commentId);
```

---

### 3. DTO层更新 ✅

#### 3.1 TaskCommentResponse更新
**文件**: `backend/promanage-api/src/main/java/com/promanage/api/dto/response/TaskCommentResponse.java`

**新增字段**:
```java
// @提及功能
private List<MentionedUser> mentionedUsers;  // 被提及的用户列表

// 附件功能
private List<CommentAttachmentInfo> attachments;  // 评论附件列表
private Integer attachmentCount;                   // 附件数量

// 点赞功能
private Integer likeCount;   // 点赞数量
private Boolean isLiked;     // 当前用户是否已点赞
```

**新增嵌套类**:
```java
// 被提及用户信息
public static class MentionedUser {
    private Long userId;
    private String userName;
    private String userAvatar;
}

// 评论附件信息
public static class CommentAttachmentInfo {
    private Long id;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String fileType;
}
```

#### 3.2 CreateTaskCommentRequest更新
**文件**: `backend/promanage-api/src/main/java/com/promanage/api/dto/request/CreateTaskCommentRequest.java`

**新增字段**:
```java
// @提及功能
private List<Long> mentionedUserIds;  // 被提及的用户ID列表
```

---

## 📋 待完成工作 (第二阶段)

### 1. Service层实现 ⏸️

需要在 `TaskServiceImpl` 中实现以下功能：

#### 1.1 评论创建时处理@提及
```java
@Override
@Transactional
public Long addTaskComment(TaskComment comment, List<Long> mentionedUserIds) {
    // 1. 创建评论
    taskCommentMapper.insert(comment);

    // 2. 处理@提及
    if (mentionedUserIds != null && !mentionedUserIds.isEmpty()) {
        List<CommentMention> mentions = mentionedUserIds.stream()
            .map(userId -> {
                CommentMention mention = new CommentMention();
                mention.setCommentId(comment.getId());
                mention.setMentionedUserId(userId);
                mention.setCommentType("TASK_COMMENT");
                mention.setIsRead(false);
                return mention;
            })
            .collect(Collectors.toList());
        commentMentionMapper.batchInsert(mentions);

        // 3. 发送通知给被提及的用户（TODO: 集成通知系统）
    }

    return comment.getId();
}
```

#### 1.2 评论查询时加载关联数据
```java
private TaskCommentResponse convertToTaskCommentResponse(TaskComment comment, Long currentUserId) {
    // 1. 基础信息转换
    TaskCommentResponse response = // ... 现有转换逻辑

    // 2. 加载被提及用户列表
    List<Long> mentionedUserIds = commentMentionMapper.findMentionedUserIdsByCommentId(comment.getId());
    List<MentionedUser> mentionedUsers = mentionedUserIds.stream()
        .map(userId -> {
            User user = userService.getById(userId);
            return MentionedUser.builder()
                .userId(user.getId())
                .userName(user.getRealName())
                .userAvatar(user.getAvatar())
                .build();
        })
        .collect(Collectors.toList());
    response.setMentionedUsers(mentionedUsers);

    // 3. 加载附件列表
    List<CommentAttachment> attachments = commentAttachmentMapper.findByCommentId(comment.getId());
    response.setAttachments(attachments.stream()
        .map(att -> CommentAttachmentInfo.builder()
            .id(att.getId())
            .fileName(att.getFileName())
            .fileUrl(att.getFileUrl())
            .fileSize(att.getFileSize())
            .fileType(att.getFileType())
            .build())
        .collect(Collectors.toList()));
    response.setAttachmentCount(attachments.size());

    // 4. 加载点赞信息
    int likeCount = commentLikeMapper.countByCommentId(comment.getId());
    boolean isLiked = commentLikeMapper.existsByCommentIdAndUserId(comment.getId(), currentUserId);
    response.setLikeCount(likeCount);
    response.setIsLiked(isLiked);

    return response;
}
```

#### 1.3 点赞/取消点赞
```java
@Override
@Transactional
public void likeComment(Long commentId, Long userId) {
    // 检查是否已点赞
    if (commentLikeMapper.existsByCommentIdAndUserId(commentId, userId)) {
        throw new BusinessException("已经点赞过该评论");
    }

    // 创建点赞记录
    CommentLike like = new CommentLike();
    like.setCommentId(commentId);
    like.setUserId(userId);
    like.setCommentType("TASK_COMMENT");
    commentLikeMapper.insert(like);
}

@Override
@Transactional
public void unlikeComment(Long commentId, Long userId) {
    commentLikeMapper.deleteByCommentIdAndUserId(commentId, userId);
}
```

### 2. Controller层实现 ⏸️

需要在 `TaskController` 中添加以下API端点：

#### 2.1 点赞/取消点赞评论
```java
@PostMapping("/comments/{commentId}/like")
@Operation(summary = "点赞评论", description = "为评论点赞")
public Result<Void> likeComment(@PathVariable Long commentId) {
    Long userId = SecurityUtils.getCurrentUserId()
        .orElseThrow(() -> new BusinessException("请先登录"));
    taskService.likeComment(commentId, userId);
    return Result.success();
}

@DeleteMapping("/comments/{commentId}/like")
@Operation(summary = "取消点赞评论", description = "取消对评论的点赞")
public Result<Void> unlikeComment(@PathVariable Long commentId) {
    Long userId = SecurityUtils.getCurrentUserId()
        .orElseThrow(() -> new BusinessException("请先登录"));
    taskService.unlikeComment(commentId, userId);
    return Result.success();
}
```

#### 2.2 上传评论附件（需集成文件上传服务）
```java
@PostMapping("/comments/{commentId}/attachments")
@Operation(summary = "上传评论附件", description = "为评论上传附件")
public Result<CommentAttachmentInfo> uploadCommentAttachment(
        @PathVariable Long commentId,
        @RequestParam("file") MultipartFile file) {
    // 1. 验证文件
    // 2. 上传到文件存储服务（MinIO）
    // 3. 创建附件记录
    // 4. 返回附件信息
    return Result.success(attachmentInfo);
}
```

#### 2.3 获取我的@提及列表
```java
@GetMapping("/my-mentions")
@Operation(summary = "获取我的@提及列表", description = "获取所有提及我的评论")
public Result<PageResult<MentionNotification>> getMyMentions(
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "20") Integer size,
        @RequestParam(required = false) Boolean isRead) {
    Long userId = SecurityUtils.getCurrentUserId()
        .orElseThrow(() -> new BusinessException("请先登录"));
    // 查询提及记录并返回
    return Result.success(mentions);
}
```

---

## 🔧 技术实现要点

### 1. @提及解析策略

**前端处理方案（推荐）**:
```javascript
// 前端在富文本编辑器中解析@提及
// 示例：@张三 @李四 这个功能需要重新设计
// 提取被提及的用户ID: [5, 8]
// 发送到后端：
{
  "content": "@张三 @李四 这个功能需要重新设计",
  "mentionedUserIds": [5, 8]
}
```

**后端处理策略**:
```java
// 后端接收mentionedUserIds列表
// 验证用户存在性
// 创建CommentMention记录
// 触发通知（待集成通知系统）
```

### 2. 附件上传流程

```
1. 前端上传文件 → 2. 后端验证（类型、大小）→ 3. 上传MinIO
→ 4. 生成访问URL → 5. 创建附件记录 → 6. 返回附件信息
```

### 3. 点赞防重机制

使用数据库唯一索引防止重复点赞：
```sql
CREATE UNIQUE INDEX idx_comment_like_unique
ON tb_comment_like(comment_id, user_id, deleted);
```

### 4. 性能优化

**批量查询优化**:
```java
// 避免N+1查询
// 查询评论列表后，批量加载关联数据
List<Long> commentIds = comments.stream()
    .map(Comment::getId)
    .collect(Collectors.toList());

// 批量查询提及、附件、点赞数据
Map<Long, List<Mention>> mentionsMap = ...;
Map<Long, List<Attachment>> attachmentsMap = ...;
Map<Long, Integer> likeCountsMap = ...;
```

---

## 📊 数据库Schema（待创建）

### 1. tb_comment_mention
```sql
CREATE TABLE tb_comment_mention (
    id BIGSERIAL PRIMARY KEY,
    comment_id BIGINT NOT NULL,
    mentioned_user_id BIGINT NOT NULL,
    comment_type VARCHAR(50) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_comment FOREIGN KEY (comment_id) REFERENCES tb_task_comment(id),
    CONSTRAINT fk_user FOREIGN KEY (mentioned_user_id) REFERENCES tb_user(id)
);

CREATE INDEX idx_comment_mention_comment_id ON tb_comment_mention(comment_id);
CREATE INDEX idx_comment_mention_user_id ON tb_comment_mention(mentioned_user_id);
```

### 2. tb_comment_attachment
```sql
CREATE TABLE tb_comment_attachment (
    id BIGSERIAL PRIMARY KEY,
    comment_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(255),
    file_path VARCHAR(500) NOT NULL,
    file_url VARCHAR(500),
    file_size BIGINT,
    file_type VARCHAR(100),
    uploader_id BIGINT,
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_comment_att FOREIGN KEY (comment_id) REFERENCES tb_task_comment(id)
);

CREATE INDEX idx_comment_attachment_comment_id ON tb_comment_attachment(comment_id);
```

### 3. tb_comment_like
```sql
CREATE TABLE tb_comment_like (
    id BIGSERIAL PRIMARY KEY,
    comment_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    comment_type VARCHAR(50) NOT NULL,
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_comment_like FOREIGN KEY (comment_id) REFERENCES tb_task_comment(id),
    CONSTRAINT fk_user_like FOREIGN KEY (user_id) REFERENCES tb_user(id)
);

CREATE UNIQUE INDEX idx_comment_like_unique ON tb_comment_like(comment_id, user_id) WHERE deleted = FALSE;
CREATE INDEX idx_comment_like_comment_id ON tb_comment_like(comment_id);
```

---

## 🎯 下一步行动计划

### 立即执行

1. **完成Service层实现** (预计2小时)
   - 更新 `ITaskService` 接口
   - 实现 `TaskServiceImpl` 中的@提及、附件、点赞逻辑
   - 更新评论转换方法

2. **完成Controller层实现** (预计1小时)
   - 添加点赞/取消点赞端点
   - 添加获取@提及列表端点
   - 更新创建评论端点以支持@提及

3. **创建数据库Schema** (预计30分钟)
   - 在PostgreSQL中创建3张新表
   - 创建必要的索引

4. **集成测试** (预计1小时)
   - 测试@提及功能
   - 测试点赞/取消点赞功能
   - 测试评论查询带关联数据

### 后续优化

1. **集成文件上传服务** - 实现评论附件上传
2. **集成通知系统** - @提及时发送通知
3. **前端集成** - 配合前端实现富文本编辑器的@提及UI

---

## 📝 总结

**第一阶段完成内容**:
- ✅ 3个实体类设计与实现
- ✅ 3个Mapper接口与XML实现
- ✅ DTO层更新（Request/Response）
- ✅ 完整的数据访问层架构

**待完成核心工作**:
- ⏸️ Service层业务逻辑实现
- ⏸️ Controller层API端点实现
- ⏸️ 数据库Schema创建
- ⏸️ 集成测试与验证

**预计剩余工作量**: 4-5小时

---

**报告生成时间**: 2025-10-05
**报告生成者**: Claude Code
**版本**: V1.0-阶段性总结
