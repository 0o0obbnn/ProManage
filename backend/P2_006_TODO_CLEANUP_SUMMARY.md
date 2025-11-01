# ProManage Backend - P2-006 (TODO Cleanup) Summary

**修复日期**: 2025-10-16
**修复范围**: P2-006 Medium Priority - Review and Clean Up TODO Comments
**状态**: ✅ **已完成**

---

## 📊 执行概览

| 优先级 | 问题ID | 问题描述 | 状态 | 结论 |
|--------|--------|---------|------|------|
| 🟡 P2 | Medium-006 | TODO注释清理 | ✅ 已审查 | 保留全部6个TODO |

---

## 🎯 P2-006: TODO注释审查与清理

### 任务目标

**原始需求**:
- 检查代码中的TODO注释是否对应已实现的功能
- 如果功能已实现，清理TODO注释（代码清理）
- 如果功能未实现，保留TODO注释（作为实现指南）

**审查原则**:
1. ✅ **已实现功能** → 移除TODO注释（减少代码噪音）
2. ⏳ **未实现功能** → 保留TODO注释（保持路线图可见性）
3. 📋 **有效占位符** → 保留并确保注释清晰
4. 🗑️ **过期注释** → 移除或更新

---

## 🔍 审查方法

### 1. 全局TODO搜索

**搜索范围**: `backend/promanage-service/src` (排除文档文件)

**搜索命令**:
```bash
grep -r -n "TODO" backend/promanage-service/src --include="*.java"
```

**搜索结果统计**:
- 总计TODO注释: **6个**
- 涉及文件: **2个**
- 文件分布:
  - DocumentFileServiceImpl.java: 3个TODO
  - DocumentServiceImpl.java: 3个TODO

---

## 📋 TODO注释详细分析

### A. DocumentFileServiceImpl.java (3个TODO)

**文件路径**: `backend/promanage-service/src/main/java/com/promanage/service/impl/DocumentFileServiceImpl.java`

#### TODO #1: 文件存储逻辑 (Line 149)

```java
// TODO: 实际的文件存储逻辑（MinIO/S3）
// 当前简化实现：仅保存文件名作为URL
// 🛡️ 使用消毒后的文件名生成安全的URL路径
String fileUrl = "/files/" + System.currentTimeMillis() + "_" + sanitizedFilename;
document.setFileUrl(fileUrl);
```

**实现状态**: ❌ **未实现**

**当前实现**:
- 仅生成本地文件路径字符串 (`/files/{timestamp}_{filename}`)
- 没有实际的文件存储操作
- 没有MinIO或S3客户端集成

**需要的工作**:
1. 添加MinIO/S3 SDK依赖到 `pom.xml`
2. 配置MinIO/S3连接信息 (endpoint, access key, secret key)
3. 实现文件上传到分布式存储的逻辑
4. 返回实际的存储URL而非本地路径

**依赖**:
- 外部服务: MinIO 或 AWS S3
- 配置文件更新: `application.yml`
- 新增配置类: `MinioConfig.java` 或 `S3Config.java`

**决策**: ✅ **保留** - 这是计划中的分布式存储实现的有效占位符

---

#### TODO #2: 下载令牌生成 (Line 201)

```java
// TODO: 生成临时下载令牌（有效期15分钟）- 可通过扩展DTO添加此字段
```

**实现状态**: ❌ **未实现**

**当前实现**:
- `getDownloadInfo()` 方法返回文档基本信息
- 没有生成任何下载令牌
- 没有令牌过期机制

**需要的工作**:
1. 实现JWT令牌生成逻辑（类似auth令牌但作用域不同）
2. 设置15分钟过期时间
3. 扩展 `DocumentDownloadInfo` DTO添加 `downloadToken` 字段
4. 在下载接口验证令牌有效性

**安全增强价值**:
- 防止未授权的文件直接访问
- 提供细粒度的下载权限控制
- 支持下载审计（谁在什么时候下载了什么）

**依赖**:
- JWT工具类（可能需要新增 `DownloadTokenUtil.java`）
- 令牌验证拦截器
- DTO扩展

**决策**: ✅ **保留** - 这是计划中的安全增强功能的有效占位符

---

#### TODO #3: 文件下载逻辑 (Line 235)

```java
// TODO: 实际的文件下载逻辑（从MinIO/S3读取文件流）
// 当前简化实现：返回提示信息
response.getWriter().write("文件下载功能待完善，文件路径: " + document.getFileUrl());
response.getWriter().flush();
```

**实现状态**: ❌ **未实现**

**当前实现**:
- 仅返回提示文本，没有实际文件传输
- 没有从MinIO/S3读取文件流的逻辑

**需要的工作**:
1. 使用MinIO/S3客户端获取文件输入流
2. 设置正确的响应头（Content-Type, Content-Disposition, Content-Length）
3. 实现流式传输（避免大文件内存溢出）
4. 添加断点续传支持（HTTP Range请求）
5. 添加下载速率限制（防止带宽耗尽）

**依赖**:
- TODO #1必须先完成（文件必须先存储在MinIO/S3）
- MinIO/S3客户端集成
- 流式传输工具类

**决策**: ✅ **保留** - 这是与TODO #1配套的下载实现占位符

---

### B. DocumentServiceImpl.java (3个TODO)

**文件路径**: `backend/promanage-service/src/main/java/com/promanage/service/impl/DocumentServiceImpl.java`

**共同特点**: 这3个TODO都与**文档收藏功能**相关，需要创建完整的收藏子系统。

---

#### TODO #4: 收藏功能实现 (Line 1120)

```java
@Override
public void toggleFavorite(Long documentId, Long userId, boolean favorite) {
    log.info("切换文档收藏状态, documentId={}, userId={}, favorite={}", documentId, userId, favorite);

    if (userId == null) {
        throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
    }

    // 权限检查
    validateDocumentAccess(documentId, userId);

    // TODO: 实现收藏功能 - 需要创建DocumentFavorite实体和Mapper
    // 当前简化实现：记录日志
    log.warn("收藏功能待实现，需要创建tb_document_favorite表和相关实体");
    log.info("用户{}{}收藏文档{}", userId, favorite ? "" : "取消", documentId);
}
```

**实现状态**: ❌ **未实现** (方法框架存在，但核心功能缺失)

**当前实现**:
- 仅进行权限检查和日志记录
- 没有任何数据库操作
- 方法调用不会改变任何状态

**需要的工作**:
1. **数据库表**: 创建 `tb_document_favorite` 表
   ```sql
   CREATE TABLE tb_document_favorite (
       id BIGSERIAL PRIMARY KEY,
       document_id BIGINT NOT NULL,
       user_id BIGINT NOT NULL,
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       UNIQUE (document_id, user_id)
   );
   CREATE INDEX idx_document_favorite_user ON tb_document_favorite(user_id);
   CREATE INDEX idx_document_favorite_document ON tb_document_favorite(document_id);
   ```

2. **实体类**: 创建 `DocumentFavorite.java`
   ```java
   @Data
   @TableName("tb_document_favorite")
   public class DocumentFavorite {
       @TableId(type = IdType.AUTO)
       private Long id;
       private Long documentId;
       private Long userId;
       private LocalDateTime createdAt;
   }
   ```

3. **Mapper接口**: 创建 `DocumentFavoriteMapper.java`
   ```java
   @Mapper
   public interface DocumentFavoriteMapper extends BaseMapper<DocumentFavorite> {
       // MyBatis Plus自动提供CRUD方法
   }
   ```

4. **实现逻辑**:
   - `favorite = true`: 插入记录到 `tb_document_favorite`（使用 `INSERT IGNORE` 避免重复）
   - `favorite = false`: 删除对应记录

**依赖**:
- 数据库迁移脚本（Flyway或手动SQL）
- 新增实体类和Mapper
- 可能需要更新缓存策略（收藏数量缓存）

**决策**: ✅ **保留** - 这是完整收藏功能的基础，需要数据库schema变更

---

#### TODO #5: 收藏状态检查 (Line 1134)

```java
@Override
public boolean isFavorited(Long documentId, Long userId) {
    log.info("检查文档收藏状态, documentId={}, userId={}", documentId, userId);

    if (userId == null || documentId == null) {
        return false;
    }

    // TODO: 实现收藏状态检查 - 需要查询DocumentFavorite表
    // 当前简化实现：返回false
    return false;
}
```

**实现状态**: ❌ **未实现** (始终返回 `false`)

**当前实现**:
- 无论用户是否收藏该文档，都返回 `false`
- 前端UI无法正确显示收藏状态

**需要的工作**:
1. 查询 `tb_document_favorite` 表
   ```java
   LambdaQueryWrapper<DocumentFavorite> wrapper = new LambdaQueryWrapper<>();
   wrapper.eq(DocumentFavorite::getDocumentId, documentId)
          .eq(DocumentFavorite::getUserId, userId);
   return documentFavoriteMapper.selectCount(wrapper) > 0;
   ```

2. 添加缓存优化（减少数据库查询）
   ```java
   @Cacheable(value = "document:favorite", key = "#userId + ':' + #documentId")
   public boolean isFavorited(Long documentId, Long userId) { ... }
   ```

**依赖**:
- TODO #4必须先完成（需要 `DocumentFavorite` 实体和Mapper）
- 可选：Redis缓存配置

**决策**: ✅ **保留** - 依赖TODO #4，是收藏功能的必要组成部分

---

#### TODO #6: 收藏数量统计 (Line 1157)

```java
@Override
public int getFavoriteCount(Long documentId, Long userId) {
    log.info("获取文档收藏数量, documentId={}, userId={}", documentId, userId);

    if (documentId == null) {
        return 0;
    }

    // 权限检查
    if (userId != null) {
        try {
            validateDocumentAccess(documentId, userId);
        } catch (BusinessException e) {
            log.warn("用户无权查看文档收藏数, documentId={}, userId={}", documentId, userId);
            return 0;
        }
    }

    // TODO: 实现收藏数量统计 - 需要查询DocumentFavorite表
    // 当前简化实现：返回0
    return 0;
}
```

**实现状态**: ❌ **未实现** (始终返回 `0`)

**当前实现**:
- 无论实际收藏人数，都返回 `0`
- 前端无法显示文档的受欢迎程度

**需要的工作**:
1. 统计 `tb_document_favorite` 表中的记录数
   ```java
   LambdaQueryWrapper<DocumentFavorite> wrapper = new LambdaQueryWrapper<>();
   wrapper.eq(DocumentFavorite::getDocumentId, documentId);
   return Math.toIntExact(documentFavoriteMapper.selectCount(wrapper));
   ```

2. 添加高性能缓存（收藏数是高频读取的数据）
   ```java
   @Cacheable(value = "document:favoriteCount", key = "#documentId", unless = "#result == 0")
   public int getFavoriteCount(Long documentId, Long userId) { ... }
   ```

3. 配合缓存失效策略（当用户收藏/取消收藏时）
   ```java
   @CacheEvict(value = "document:favoriteCount", key = "#documentId")
   public void toggleFavorite(...) { ... }
   ```

**依赖**:
- TODO #4必须先完成（需要 `DocumentFavorite` 表和Mapper）
- 推荐配置Redis缓存（减少数据库负载）

**决策**: ✅ **保留** - 依赖TODO #4，是收藏功能的统计展示部分

---

## 📊 TODO分类汇总

### 按功能分类

| 功能模块 | TODO数量 | 实现状态 | 优先级 |
|---------|---------|---------|--------|
| 分布式文件存储 (MinIO/S3) | 3个 | 未实现 | 🔴 高 |
| 文档收藏功能 | 3个 | 未实现 | 🟡 中 |

### 按实现复杂度分类

| 复杂度 | TODO数量 | 说明 |
|--------|---------|------|
| 🔴 高 | 3个 | MinIO/S3集成（需要外部服务、配置、流式传输） |
| 🟡 中 | 3个 | 收藏功能（需要数据库schema变更、新增实体和Mapper） |

### 按依赖关系

```
独立模块A: 分布式文件存储
├── TODO #1: 文件上传到MinIO/S3 (基础)
├── TODO #2: 下载令牌生成 (依赖#1, 安全增强)
└── TODO #3: 文件下载从MinIO/S3 (依赖#1, 完整闭环)

独立模块B: 文档收藏功能
├── TODO #4: 收藏功能实现 (基础 - 创建表和实体)
├── TODO #5: 收藏状态检查 (依赖#4, UI状态显示)
└── TODO #6: 收藏数量统计 (依赖#4, 数据统计展示)
```

---

## ✅ 审查结论

### 决策: 保留全部6个TODO注释

**原因**:

1. **全部未实现** ✅
   - 所有6个TODO都对应未实现的功能
   - 没有发现"已实现但忘记删除TODO"的情况

2. **注释质量高** ✅
   - 所有TODO都有清晰的说明（需要做什么）
   - 明确指出依赖和需要的工作（如"需要创建DocumentFavorite实体"）
   - 包含当前简化实现的说明（如"当前简化实现：记录日志"）

3. **作为实现指南** ✅
   - TODO注释为未来实现提供了清晰的路线图
   - 新开发者可以快速理解哪些功能是占位符
   - 防止将简化实现误认为最终实现

4. **无代码噪音** ✅
   - TODO数量合理（仅6个）
   - 集中在2个文件中，便于追踪
   - 不影响代码可读性

5. **符合最佳实践** ✅
   - 保留未实现功能的TODO是行业标准做法
   - 便于项目管理和优先级规划
   - 支持敏捷开发中的增量交付

---

## 🎯 实现优先级建议

### P1 高优先级: 分布式文件存储 (1-2月内)

**原因**: 当前本地文件路径方案不适合生产环境

**实施顺序**:
1. **Phase 1**: MinIO/S3基础集成
   - 完成TODO #1: 文件上传逻辑
   - 完成TODO #3: 文件下载逻辑
   - 估计工时: 3-5天

2. **Phase 2**: 安全增强
   - 完成TODO #2: 临时下载令牌
   - 估计工时: 2-3天

**预期收益**:
- ✅ 支持分布式部署（多节点共享文件）
- ✅ 提升文件存储可靠性（MinIO副本机制）
- ✅ 增强下载安全性（临时令牌机制）

---

### P2 中优先级: 文档收藏功能 (2-3月内)

**原因**: 用户体验增强功能，非核心业务流程

**实施顺序**:
1. **Phase 1**: 数据库Schema
   - 创建 `tb_document_favorite` 表
   - Flyway迁移脚本
   - 估计工时: 0.5天

2. **Phase 2**: 基础功能
   - 创建 `DocumentFavorite` 实体和Mapper
   - 完成TODO #4: toggleFavorite()
   - 完成TODO #5: isFavorited()
   - 完成TODO #6: getFavoriteCount()
   - 估计工时: 2-3天

3. **Phase 3**: 缓存优化
   - Redis缓存配置
   - 缓存失效策略
   - 估计工时: 1-2天

**预期收益**:
- ✅ 提升用户体验（收藏常用文档）
- ✅ 数据洞察（了解文档受欢迎程度）
- ✅ 推荐系统基础（基于收藏的协同过滤）

---

## 🔧 实现指南

### 模块A: 分布式文件存储实现指南

#### 1. 添加MinIO依赖 (pom.xml)

```xml
<!-- MinIO Client -->
<dependency>
    <groupId>io.minio</groupId>
    <artifactId>minio</artifactId>
    <version>8.5.7</version>
</dependency>
```

#### 2. 配置MinIO连接 (application.yml)

```yaml
minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket-name: promanage-documents
  connect-timeout: 10000
  write-timeout: 60000
  read-timeout: 10000
```

#### 3. 创建MinIO配置类

```java
@Configuration
public class MinioConfig {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
            .endpoint(endpoint)
            .credentials(accessKey, secretKey)
            .build();
    }
}
```

#### 4. 创建文件存储服务

```java
@Service
public class MinioFileStorageService {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    /**
     * 上传文件到MinIO
     */
    public String uploadFile(MultipartFile file, String fileName) throws IOException {
        try {
            // 确保bucket存在
            boolean bucketExists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build()
            );

            if (!bucketExists) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(bucketName).build()
                );
            }

            // 上传文件
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );

            // 返回文件访问URL
            return String.format("%s/%s/%s", endpoint, bucketName, fileName);

        } catch (Exception e) {
            throw new IOException("文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从MinIO下载文件
     */
    public InputStream downloadFile(String fileName) throws IOException {
        try {
            return minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .build()
            );
        } catch (Exception e) {
            throw new IOException("文件下载失败: " + e.getMessage(), e);
        }
    }
}
```

#### 5. 更新DocumentFileServiceImpl

**TODO #1: 替换文件上传逻辑**

```java
// 修复前 (Line 149)
// TODO: 实际的文件存储逻辑（MinIO/S3）
String fileUrl = "/files/" + System.currentTimeMillis() + "_" + sanitizedFilename;
document.setFileUrl(fileUrl);

// 修复后
@Autowired
private MinioFileStorageService minioFileStorageService;

String fileName = System.currentTimeMillis() + "_" + sanitizedFilename;
String fileUrl = minioFileStorageService.uploadFile(file, fileName);
document.setFileUrl(fileUrl);
log.info("文件上传成功到MinIO, fileUrl={}", fileUrl);
```

**TODO #3: 替换文件下载逻辑**

```java
// 修复前 (Line 235)
// TODO: 实际的文件下载逻辑（从MinIO/S3读取文件流）
response.getWriter().write("文件下载功能待完善，文件路径: " + document.getFileUrl());

// 修复后
String fileName = extractFileNameFromUrl(document.getFileUrl());
try (InputStream inputStream = minioFileStorageService.downloadFile(fileName);
     OutputStream outputStream = response.getOutputStream()) {

    byte[] buffer = new byte[8192];
    int bytesRead;
    while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
    }
    outputStream.flush();
}
log.info("文件下载完成, documentId={}", documentId);
```

---

### 模块B: 文档收藏功能实现指南

#### 1. 数据库迁移脚本

创建 `V1.3.0__create_document_favorite_table.sql`:

```sql
-- =====================================================
-- 文档收藏表
-- =====================================================

CREATE TABLE IF NOT EXISTS tb_document_favorite (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_document_user UNIQUE (document_id, user_id)
);

COMMENT ON TABLE tb_document_favorite IS '文档收藏表';
COMMENT ON COLUMN tb_document_favorite.id IS '主键ID';
COMMENT ON COLUMN tb_document_favorite.document_id IS '文档ID';
COMMENT ON COLUMN tb_document_favorite.user_id IS '用户ID';
COMMENT ON COLUMN tb_document_favorite.created_at IS '收藏时间';

-- 索引：按用户查询收藏的文档
CREATE INDEX idx_document_favorite_user ON tb_document_favorite(user_id);

-- 索引：按文档统计收藏数
CREATE INDEX idx_document_favorite_document ON tb_document_favorite(document_id);

-- 外键：关联文档表
ALTER TABLE tb_document_favorite
ADD CONSTRAINT fk_favorite_document
FOREIGN KEY (document_id) REFERENCES tb_document(id) ON DELETE CASCADE;

-- 外键：关联用户表
ALTER TABLE tb_document_favorite
ADD CONSTRAINT fk_favorite_user
FOREIGN KEY (user_id) REFERENCES tb_user(id) ON DELETE CASCADE;
```

#### 2. 创建实体类

创建 `DocumentFavorite.java`:

```java
package com.promanage.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档收藏实体
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-16
 */
@Data
@TableName("tb_document_favorite")
public class DocumentFavorite {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 文档ID
     */
    private Long documentId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 收藏时间
     */
    private LocalDateTime createdAt;
}
```

#### 3. 创建Mapper接口

创建 `DocumentFavoriteMapper.java`:

```java
package com.promanage.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.promanage.service.entity.DocumentFavorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文档收藏Mapper
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-16
 */
@Mapper
public interface DocumentFavoriteMapper extends BaseMapper<DocumentFavorite> {
    // MyBatis Plus自动提供CRUD方法:
    // - insert(DocumentFavorite entity)
    // - deleteById(Serializable id)
    // - selectById(Serializable id)
    // - selectCount(Wrapper<DocumentFavorite> wrapper)
    // - selectList(Wrapper<DocumentFavorite> wrapper)
}
```

#### 4. 更新DocumentServiceImpl

**TODO #4: 实现toggleFavorite()**

```java
@Autowired
private DocumentFavoriteMapper documentFavoriteMapper;

@Override
@Transactional(rollbackFor = Exception.class)
@CacheEvict(value = "document:favoriteCount", key = "#documentId")
public void toggleFavorite(Long documentId, Long userId, boolean favorite) {
    log.info("切换文档收藏状态, documentId={}, userId={}, favorite={}", documentId, userId, favorite);

    if (userId == null) {
        throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
    }

    // 权限检查
    validateDocumentAccess(documentId, userId);

    if (favorite) {
        // 添加收藏
        DocumentFavorite documentFavorite = new DocumentFavorite();
        documentFavorite.setDocumentId(documentId);
        documentFavorite.setUserId(userId);
        documentFavorite.setCreatedAt(LocalDateTime.now());

        try {
            documentFavoriteMapper.insert(documentFavorite);
            log.info("用户{}收藏文档{}成功", userId, documentId);
        } catch (DuplicateKeyException e) {
            log.warn("用户{}已收藏文档{}, 忽略重复收藏", userId, documentId);
        }
    } else {
        // 取消收藏
        LambdaQueryWrapper<DocumentFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentFavorite::getDocumentId, documentId)
               .eq(DocumentFavorite::getUserId, userId);

        int deletedCount = documentFavoriteMapper.delete(wrapper);
        log.info("用户{}取消收藏文档{}, 删除记录数={}", userId, documentId, deletedCount);
    }
}
```

**TODO #5: 实现isFavorited()**

```java
@Override
@Cacheable(value = "document:favorite", key = "#userId + ':' + #documentId")
public boolean isFavorited(Long documentId, Long userId) {
    log.info("检查文档收藏状态, documentId={}, userId={}", documentId, userId);

    if (userId == null || documentId == null) {
        return false;
    }

    LambdaQueryWrapper<DocumentFavorite> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(DocumentFavorite::getDocumentId, documentId)
           .eq(DocumentFavorite::getUserId, userId);

    Long count = documentFavoriteMapper.selectCount(wrapper);
    boolean favorited = count != null && count > 0;

    log.debug("文档收藏状态检查结果: documentId={}, userId={}, favorited={}",
              documentId, userId, favorited);

    return favorited;
}
```

**TODO #6: 实现getFavoriteCount()**

```java
@Override
@Cacheable(value = "document:favoriteCount", key = "#documentId", unless = "#result == 0")
public int getFavoriteCount(Long documentId, Long userId) {
    log.info("获取文档收藏数量, documentId={}, userId={}", documentId, userId);

    if (documentId == null) {
        return 0;
    }

    // 权限检查
    if (userId != null) {
        try {
            validateDocumentAccess(documentId, userId);
        } catch (BusinessException e) {
            log.warn("用户无权查看文档收藏数, documentId={}, userId={}", documentId, userId);
            return 0;
        }
    }

    LambdaQueryWrapper<DocumentFavorite> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(DocumentFavorite::getDocumentId, documentId);

    Long count = documentFavoriteMapper.selectCount(wrapper);
    int favoriteCount = count != null ? Math.toIntExact(count) : 0;

    log.debug("文档收藏数量: documentId={}, count={}", documentId, favoriteCount);

    return favoriteCount;
}
```

---

## 🧪 测试建议

### 模块A: 分布式文件存储测试

```java
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DocumentFileServiceImplTest {

    @Autowired
    private IDocumentFileService documentFileService;

    @Test
    void shouldUploadFileToMinIO_whenValidFileProvided() throws IOException {
        // given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "Hello MinIO".getBytes()
        );
        DocumentUploadRequest request = new DocumentUploadRequest();
        request.setFile(file);
        request.setProjectId(1L);
        request.setTitle("Test Document");

        // when
        Document document = documentFileService.upload(request, 1L);

        // then
        assertNotNull(document);
        assertTrue(document.getFileUrl().contains("http://"));
        assertTrue(document.getFileUrl().contains("promanage-documents"));
        log.info("上传成功, fileUrl={}", document.getFileUrl());
    }

    @Test
    void shouldDownloadFileFromMinIO_whenDocumentExists() throws IOException {
        // given
        Long documentId = 1L; // 假设已有文档
        Long userId = 1L;
        HttpServletResponse response = mock(HttpServletResponse.class);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public void write(int b) {
                outputStream.write(b);
            }
            // ... 其他必需方法 ...
        });

        // when
        documentFileService.downloadDocument(documentId, userId, response);

        // then
        byte[] downloadedContent = outputStream.toByteArray();
        assertTrue(downloadedContent.length > 0);
        log.info("下载成功, contentSize={}", downloadedContent.length);
    }
}
```

### 模块B: 文档收藏功能测试

```java
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DocumentServiceImplFavoriteTest {

    @Autowired
    private IDocumentService documentService;

    @Autowired
    private DocumentFavoriteMapper documentFavoriteMapper;

    @Test
    @Transactional
    void shouldToggleFavorite_whenValidDocumentAndUser() {
        // given
        Long documentId = 1L;
        Long userId = 1L;

        // when: 收藏文档
        documentService.toggleFavorite(documentId, userId, true);

        // then: 验证收藏状态
        assertTrue(documentService.isFavorited(documentId, userId));
        assertEquals(1, documentService.getFavoriteCount(documentId, userId));

        // when: 取消收藏
        documentService.toggleFavorite(documentId, userId, false);

        // then: 验证取消收藏
        assertFalse(documentService.isFavorited(documentId, userId));
        assertEquals(0, documentService.getFavoriteCount(documentId, userId));
    }

    @Test
    @Transactional
    void shouldCountMultipleFavorites_whenMultipleUsersLikeDocument() {
        // given
        Long documentId = 1L;
        List<Long> userIds = List.of(1L, 2L, 3L, 4L, 5L);

        // when: 5个用户收藏同一文档
        userIds.forEach(userId ->
            documentService.toggleFavorite(documentId, userId, true)
        );

        // then: 收藏数应为5
        assertEquals(5, documentService.getFavoriteCount(documentId, 1L));
    }

    @Test
    void shouldPreventDuplicateFavorite_whenUserFavoritesTwice() {
        // given
        Long documentId = 1L;
        Long userId = 1L;

        // when: 用户重复收藏
        documentService.toggleFavorite(documentId, userId, true);
        documentService.toggleFavorite(documentId, userId, true); // 第二次收藏

        // then: 收藏数应仍为1（不应重复插入）
        assertEquals(1, documentService.getFavoriteCount(documentId, userId));
    }
}
```

---

## 📦 部署清单

### 前置条件

- [x] P2-006审查已完成 ✅
- [x] 所有6个TODO已确认为未实现功能 ✅
- [x] 决策: 保留全部TODO注释 ✅
- [ ] 实现计划已制定 ✅ (本文档提供)
- [ ] 待开始实际实现工作 ⏳

### 风险评估

- **破坏性变更**: 无 ✅ (仅审查，未修改代码)
- **向后兼容性**: 完全兼容 ✅
- **数据迁移**: 不需要 ✅ (TODO实现时才需要)
- **配置变更**: 不需要 ✅
- **性能影响**: 无影响 ✅

### 回滚计划

无需回滚，本次任务仅为审查，未修改任何代码。

---

## 📚 相关文档

- **审计报告**: `backend/COMPREHENSIVE_BACKEND_AUDIT_REPORT.md` (Lines 685-852)
- **P1修复报告**: `backend/HIGH_PRIORITY_FIXES_SUMMARY.md`
- **P2-001/003修复报告**: `backend/P2_MEDIUM_PRIORITY_FIXES_SUMMARY.md`
- **P2-004修复报告**: `backend/P2_004_CORRELATION_ID_FIX_SUMMARY.md`
- **P0修复报告**: `backend/FIX_REPORT_P0_DEPENDENCY_INJECTION.md`
- **实现指南**: `backend/TODO_IMPLEMENTATION_GUIDE.md`

---

## ✅ 审查确认清单

- [x] P2-006 TODO审查已完成
- [x] 所有TODO注释已逐一分析
- [x] 实现状态已明确（全部未实现）
- [x] 决策已制定（保留全部6个TODO）
- [x] 实现优先级已建议（P1: 文件存储, P2: 收藏功能）
- [x] 实现指南已提供（包含代码示例）
- [x] 测试策略已规划（单元测试和集成测试）
- [x] 文档已完成 ✅

---

**报告状态**: COMPLETE ✅

**下一步行动**:
1. ✅ P2-006已完成（TODO审查）
2. ⏳ 开始P2-005（缓存键优化）
3. ⏳ 继续P2-007（API版本管理）
4. ⏳ 根据优先级实施TODO功能（分布式文件存储 → 收藏功能）

**审查人员**: Claude Code
**审查日期**: 2025-10-16
**批准人员**: 待指定
**批准日期**: 待定

---

**END OF P2-006 SUMMARY REPORT**
