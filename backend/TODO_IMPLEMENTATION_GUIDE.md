# DocumentServiceImpl TODO 实现指南

## 快速参考

### ✅ 已完成功能

| 功能模块 | 方法名 | 状态 | 说明 |
|---------|--------|------|------|
| 权限检查 | `isProjectMember()` | ✅ 完成 | 检查用户是否是项目成员 |
| 权限检查 | `hasProjectAdminPermission()` | ✅ 完成 | 检查项目管理员权限 |
| 权限检查 | `hasSystemAdminPermission()` | ✅ 完成 | 检查系统管理员权限 |
| 文件夹 | `getFolderTree()` | ✅ 完成 | 获取文件夹树形结构 |
| 文件夹 | `convertToDocumentFolderDTO()` | ✅ 完成 | 树节点转DTO |
| 上传 | `upload()` | ⚠️ 简化实现 | 文档上传（待集成MinIO） |
| 下载 | `getDownloadInfo()` | ⚠️ 简化实现 | 获取下载信息（待完善令牌） |
| 下载 | `downloadDocument()` | ⚠️ 简化实现 | 文档下载（待集成MinIO） |
| 收藏 | `toggleFavorite()` | ⚠️ 框架实现 | 切换收藏（待建表） |
| 收藏 | `isFavorited()` | ⚠️ 框架实现 | 检查收藏（待建表） |
| 收藏 | `getFavoriteCount()` | ⚠️ 框架实现 | 收藏数量（待建表） |

---

## 实现详情

### 1. 权限检查 - isProjectMember()

**实现逻辑：**
```java
1. 参数验证：projectId 和 userId 不能为空
2. 查询 ProjectMember 表
3. 检查成员状态是否为正常（status = 0）
4. 异常处理：返回 false 而不抛出异常
```

**数据库查询：**
```sql
SELECT * FROM tb_project_member 
WHERE project_id = ? AND user_id = ? AND status = 0
```

**使用场景：**
- 文档访问权限验证
- 项目资源访问控制
- 所有需要项目成员身份的操作

---

### 2. 权限检查 - hasProjectAdminPermission()

**实现逻辑：**
```java
1. 参数验证：projectId 和 userId 不能为空
2. 查询 ProjectMember 表获取成员信息
3. 检查成员状态是否正常（status = 0）
4. 检查角色ID是否为1（项目经理）
5. 异常处理：返回 false
```

**角色定义（PRD 4.2.4）：**
- roleId = 1: 项目经理 → 项目内所有权限（除删除项目）
- roleId = 2: 开发人员 → 任务、文档读写权限
- roleId = 3: 测试人员 → 测试用例、任务读写权限

**使用场景：**
- 文档删除权限验证
- 项目设置修改
- 成员管理操作

---

### 3. 权限检查 - hasSystemAdminPermission()

**实现逻辑：**
```java
1. 参数验证：userId 不能为空
2. 查询 UserRole 表
3. 检查是否拥有系统管理员角色（roleId = 1）
4. 异常处理：返回 false
```

**数据库查询：**
```sql
SELECT COUNT(1) > 0 FROM tb_user_role 
WHERE user_id = ? AND role_id = 1
```

**使用场景：**
- 查看其他用户的文档
- 系统级配置修改
- 跨项目操作

---

### 4. 文件夹树 - getFolderTree()

**实现逻辑：**
```java
1. 用户登录验证
2. 项目访问权限检查
3. 调用 documentFolderService.getFolderTree()
4. 递归转换为 DocumentFolderDTO
5. 异常处理：返回空列表
```

**DTO结构：**
```java
DocumentFolderDTO {
    Long id;
    String name;
    String description;
    Long projectId;
    Long parentId;
    List<DocumentFolderDTO> children;  // 递归子节点
}
```

**使用场景：**
- 文档管理界面显示文件夹树
- 文档移动时选择目标文件夹
- 文件夹权限管理

---

### 5. 文档上传 - upload()

**实现逻辑：**
```java
1. 用户登录验证
2. 项目创建权限检查
3. 文件验证：
   - 文件不能为空
   - 大小不超过500MB
4. 创建文档实体
5. 保存到数据库
6. 返回文档信息
```

**当前实现：**
- ✅ 参数验证
- ✅ 权限检查
- ✅ 文件大小限制
- ⚠️ 文件URL使用简化方案（时间戳+文件名）

**待完善：**
```java
// TODO: 集成MinIO客户端
MinioClient minioClient = ...;
String objectName = generateObjectName(file);
minioClient.putObject(
    PutObjectArgs.builder()
        .bucket("promanage-documents")
        .object(objectName)
        .stream(file.getInputStream(), file.getSize(), -1)
        .contentType(file.getContentType())
        .build()
);
String fileUrl = minioClient.getPresignedObjectUrl(...);
```

---

### 6. 文档下载 - getDownloadInfo()

**实现逻辑：**
```java
1. 用户登录验证
2. 文档访问权限检查
3. 获取文档信息
4. 构建下载信息对象
5. 生成临时下载令牌
```

**返回信息：**
```java
DocumentDownloadInfo {
    Long documentId;
    String fileName;
    String fileUrl;
    Long fileSize;
    String contentType;
    String downloadToken;  // 临时令牌
}
```

**待完善：**
```java
// TODO: 实现JWT令牌生成
String downloadToken = JWT.create()
    .withClaim("documentId", documentId)
    .withClaim("userId", userId)
    .withExpiresAt(new Date(System.currentTimeMillis() + 15 * 60 * 1000))
    .sign(Algorithm.HMAC256(secret));

// 存储到Redis，15分钟过期
redisTemplate.opsForValue().set(
    "download_token:" + downloadToken,
    documentId,
    15,
    TimeUnit.MINUTES
);
```

---

### 7. 文档下载 - downloadDocument()

**实现逻辑：**
```java
1. 用户登录验证
2. 文档访问权限检查
3. 获取文档信息
4. 设置HTTP响应头
5. 写入文件流到响应
```

**HTTP响应头：**
```java
Content-Type: application/octet-stream
Content-Disposition: attachment; filename="文档标题"
Content-Length: 文件大小
```

**待完善：**
```java
// TODO: 从MinIO读取文件流
try (InputStream stream = minioClient.getObject(
    GetObjectArgs.builder()
        .bucket("promanage-documents")
        .object(document.getFileUrl())
        .build())) {
    
    // 支持断点续传
    long start = 0;
    long end = document.getFileSize() - 1;
    
    if (rangeHeader != null) {
        // 解析 Range: bytes=start-end
        // 设置响应状态码 206 Partial Content
    }
    
    // 写入响应流
    IOUtils.copy(stream, response.getOutputStream());
}
```

---

### 8. 收藏功能 - toggleFavorite()

**实现逻辑：**
```java
1. 用户登录验证
2. 文档访问权限检查
3. 记录日志（当前实现）
```

**待完善：**

**1. 创建数据库表：**
```sql
CREATE TABLE tb_document_favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    document_id BIGINT NOT NULL COMMENT '文档ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    UNIQUE KEY uk_doc_user (document_id, user_id),
    KEY idx_user_id (user_id),
    KEY idx_document_id (document_id)
) COMMENT='文档收藏表';
```

**2. 创建实体类：**
```java
@Data
@TableName("tb_document_favorite")
public class DocumentFavorite implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long documentId;
    private Long userId;
    private LocalDateTime createTime;
}
```

**3. 创建Mapper：**
```java
@Mapper
public interface DocumentFavoriteMapper extends BaseMapper<DocumentFavorite> {
    DocumentFavorite findByDocumentIdAndUserId(
        @Param("documentId") Long documentId, 
        @Param("userId") Long userId
    );
    
    int countByDocumentId(@Param("documentId") Long documentId);
}
```

**4. 实现逻辑：**
```java
if (favorite) {
    // 添加收藏
    DocumentFavorite fav = new DocumentFavorite();
    fav.setDocumentId(documentId);
    fav.setUserId(userId);
    documentFavoriteMapper.insert(fav);
} else {
    // 取消收藏
    documentFavoriteMapper.delete(
        new LambdaQueryWrapper<DocumentFavorite>()
            .eq(DocumentFavorite::getDocumentId, documentId)
            .eq(DocumentFavorite::getUserId, userId)
    );
}

// 清除缓存
redisTemplate.delete("favorite_count:" + documentId);
```

---

### 9. 收藏功能 - isFavorited()

**待完善实现：**
```java
@Override
public boolean isFavorited(Long documentId, Long userId) {
    if (userId == null || documentId == null) {
        return false;
    }
    
    // 查询收藏记录
    DocumentFavorite favorite = documentFavoriteMapper
        .findByDocumentIdAndUserId(documentId, userId);
    
    return favorite != null;
}
```

---

### 10. 收藏功能 - getFavoriteCount()

**待完善实现：**
```java
@Override
@Cacheable(value = "favorite_count", key = "#documentId")
public int getFavoriteCount(Long documentId, Long userId) {
    if (documentId == null) {
        return 0;
    }
    
    // 权限检查（可选）
    if (userId != null) {
        validateDocumentAccess(documentId, userId);
    }
    
    // 统计收藏数量
    return documentFavoriteMapper.countByDocumentId(documentId);
}
```

**缓存策略：**
```java
// Redis缓存配置
@Bean
public RedisCacheConfiguration favoriteCountCacheConfig() {
    return RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofHours(1))  // 1小时过期
        .serializeValuesWith(
            RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer())
        );
}
```

---

## 下一步工作

### Phase 1: 文件存储集成（1-2天）
1. 添加MinIO依赖
2. 配置MinIO连接
3. 实现文件上传到MinIO
4. 实现文件下载从MinIO
5. 测试大文件上传下载

### Phase 2: 收藏功能完善（1天）
1. 创建数据库表
2. 创建实体和Mapper
3. 实现收藏逻辑
4. 添加Redis缓存
5. 单元测试

### Phase 3: 下载令牌（0.5天）
1. 集成JWT库
2. 实现令牌生成
3. 实现令牌验证
4. Redis存储令牌

### Phase 4: 性能优化（1天）
1. 权限检查结果缓存
2. 文件夹树缓存
3. 批量查询优化
4. 性能测试

---

## 测试清单

### 单元测试
- [ ] testIsProjectMember_Success
- [ ] testIsProjectMember_NotMember
- [ ] testHasProjectAdminPermission_Admin
- [ ] testHasProjectAdminPermission_NotAdmin
- [ ] testHasSystemAdminPermission_Admin
- [ ] testHasSystemAdminPermission_NotAdmin
- [ ] testGetFolderTree_Success
- [ ] testUpload_Success
- [ ] testUpload_FileTooLarge
- [ ] testGetDownloadInfo_Success
- [ ] testDownloadDocument_Success

### 集成测试
- [ ] 完整的文档上传下载流程
- [ ] 权限控制各种场景
- [ ] 文件夹树形结构
- [ ] 收藏功能完整流程

### 性能测试
- [ ] 100+并发用户上传
- [ ] 500MB大文件上传
- [ ] 权限检查性能（1000次/秒）
- [ ] 文件夹树查询性能

---

## 常见问题

### Q1: 为什么权限检查失败返回false而不抛异常？
A: 权限检查方法是内部辅助方法，由调用方决定如何处理。返回false可以让调用方灵活处理，比如记录日志、返回友好提示等。

### Q2: 文件上传为什么是简化实现？
A: 当前阶段重点是完成核心业务逻辑，文件存储需要额外的基础设施（MinIO/S3）。简化实现可以让系统先运行起来，后续再集成真实的文件存储。

### Q3: 收藏功能为什么只是框架实现？
A: 收藏功能需要新建数据库表和实体类，这涉及到数据库迁移和多个模块的协调。当前先完成接口定义和基本逻辑，确保不影响其他功能。

### Q4: 如何测试这些功能？
A: 建议使用TestContainers进行集成测试，可以启动真实的PostgreSQL和Redis容器，模拟真实环境。

---

**文档版本**: V1.0  
**最后更新**: 2024-12-30  
**维护者**: Amazon Q Developer
