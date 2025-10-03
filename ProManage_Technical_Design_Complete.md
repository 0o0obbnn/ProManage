# ProManage 项目管理系统 - 完整技术设计文档

## 文档信息

|文档名称|ProManage 完整技术设计|版本|V1.0|
|---|---|---|---|
|创建日期|2024-12-30|架构师|Senior Full-Stack Architect|
|文档状态|设计完成|最后更新|2024-12-30|

---

## 📋 目录

1. [项目概述](#1-项目概述)
2. [系统架构设计](#2-系统架构设计)
3. [数据库设计](#3-数据库设计)
4. [搜索引擎设计](#4-搜索引擎设计)
5. [API 接口设计](#5-api-接口设计)
6. [技术实施策略](#6-技术实施策略)
7. [部署指南](#7-部署指南)
8. [开发指南](#8-开发指南)
9. [运维监控](#9-运维监控)
10. [安全方案](#10-安全方案)

---

## 1. 项目概述

### 1.1 产品愿景

ProManage 是一个智能、集成的项目管理系统，通过**中央化文档管理**、**自动化变更通知**和**可复用测试用例库**，提升团队协作效率50%，减少返工30%，降低项目延期风险。

### 1.2 核心功能特性

- ✅ **统一知识库**: 消除文档孤岛，提供单一信息源
- ✅ **智能变更管理**: 自动化变更影响分析和通知
- ✅ **测试用例复用**: 目标复用率70%+，提升测试效率
- ✅ **角色化工作台**: 为7类用户提供个性化界面
- ✅ **实时协作**: WebSocket 实时通知和协作
- ✅ **全文搜索**: Elasticsearch 支持的智能搜索

### 1.3 技术目标

| 性能指标 | 目标值 | 验证方式 |
|---------|--------|----------|
| API 响应时间 P95 | ≤ 300ms | 性能测试 |
| 页面首屏加载时间 | ≤ 3秒 | Lighthouse 测试 |
| 搜索响应时间 P95 | ≤ 2秒 | 搜索压力测试 |
| 并发用户支持 | 500+ | 负载测试 |
| 系统可用性 | ≥ 99.9% | 监控统计 |

### 1.4 用户角色定义

| 角色 | 核心需求 | 成功标准 |
|------|----------|----------|
| **超级管理员** | 系统管理，全局配置 | 管理效率提升50% |
| **项目经理** | 资源协调，进度跟踪 | 项目延期减少25% |
| **开发人员** | 快速访问文档，清晰任务 | 开发效率提升20% |
| **测试人员** | 用例管理，变更通知 | 测试效率提升40% |
| **UI设计师** | 设计稿管理，反馈收集 | 设计评审周期缩短50% |
| **运维人员** | 部署文档，环境信息 | 部署成功率提升15% |
| **第三方人员** | 受限访问，信息安全 | 零数据泄露 |

---

## 2. 系统架构设计

### 2.1 整体架构概览

```
┌─────────────────────────────────────────────────────────────────┐
│                           CDN + 负载均衡                          │
└─────────────────────────────────┬───────────────────────────────┘
                                  │
┌─────────────────────────────────┴───────────────────────────────┐
│                           Web 网关层                            │
│                     (API Gateway + 认证)                       │
└─────────────────────────────────┬───────────────────────────────┘
                                  │
┌─────────────────────────────────┴───────────────────────────────┐
│                          微服务层                               │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌────────────┐ │
│  │ 用户服务   │  │ 文档服务   │  │ 变更服务   │  │ 通知服务   │ │
│  │ User       │  │ Document   │  │ Change     │  │ Notification│ │
│  │ Service    │  │ Service    │  │ Service    │  │ Service    │ │
│  └────────────┘  └────────────┘  └────────────┘  └────────────┘ │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌────────────┐ │
│  │ 项目服务   │  │ 任务服务   │  │ 测试服务   │  │ 搜索服务   │ │
│  │ Project    │  │ Task       │  │ Test       │  │ Search     │ │
│  │ Service    │  │ Service    │  │ Service    │  │ Service    │ │
│  └────────────┘  └────────────┘  └────────────┘  └────────────┘ │
└─────────────────────────────────┬───────────────────────────────┘
                                  │
┌─────────────────────────────────┴───────────────────────────────┐
│                          数据层                                 │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌────────────┐ │
│  │ PostgreSQL │  │ Elasticsearch│ │   Redis    │  │ RabbitMQ   │ │
│  │ 主数据库   │  │ 搜索引擎   │  │   缓存     │  │ 消息队列   │ │
│  └────────────┘  └────────────┘  └────────────┘  └────────────┘ │
│  ┌────────────┐  ┌────────────┐                                 │
│  │   MinIO    │  │   监控     │                                 │
│  │ 文件存储   │  │   告警     │                                 │
│  └────────────┘  └────────────┘                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 技术栈选择

#### 前端技术栈
```yaml
核心框架: Vue 3.4+ with Composition API
状态管理: Pinia
UI组件库: Ant Design Vue 4.x
构建工具: Vite 5.x
类型系统: TypeScript 5.x
HTTP客户端: Axios
实时通信: Socket.IO Client
测试框架: Vitest + Vue Test Utils
代码规范: ESLint + Prettier
```

#### 后端技术栈
```yaml
主框架: Spring Boot 3.2+
安全框架: Spring Security 6.x
数据访问: Spring Data JPA + MyBatis-Plus
缓存: Spring Cache + Redis 7.x
消息队列: Spring AMQP + RabbitMQ 3.x
API文档: SpringDoc OpenAPI 3
测试框架: JUnit 5 + TestContainers
```

#### 基础设施
```yaml
容器化: Docker + Docker Compose
编排: Kubernetes 1.28+
服务网格: Istio (生产环境)
监控: Prometheus + Grafana + Jaeger
日志: ELK Stack
CI/CD: GitLab CI + Jenkins
```

### 2.3 微服务设计

#### 服务职责划分

| 服务名称 | 职责范围 | 主要功能 |
|----------|----------|----------|
| **用户服务** | 用户认证、授权、权限管理 | 登录注册、RBAC、JWT管理 |
| **项目服务** | 项目管理、团队管理 | 项目CRUD、成员管理、设置 |
| **文档服务** | 文档管理、版本控制、文件存储 | 文档CRUD、版本管理、附件上传 |
| **变更服务** | 变更请求管理、审批流程、影响分析 | 变更流程、智能分析、审批 |
| **任务服务** | 任务管理、工作流 | 任务CRUD、状态流转、依赖关系 |
| **测试服务** | 测试用例管理、测试执行 | 用例管理、执行记录、复用分析 |
| **通知服务** | 消息通知、实时推送 | 多渠道通知、订阅管理、实时推送 |
| **搜索服务** | 全文搜索、智能推荐 | 索引管理、搜索查询、推荐算法 |

#### 服务间通信模式

**同步通信 (REST API)**
```java
@FeignClient(name = "user-service")
public interface UserServiceClient {
    @GetMapping("/users/{userId}/permissions")
    ResponseEntity<Set<Permission>> getUserPermissions(@PathVariable Long userId);
}
```

**异步通信 (消息队列)**
```java
@RabbitListener(queues = "change.document.queue")
public void handleDocumentCreated(DocumentCreatedEvent event) {
    changeAnalysisService.analyzeImpact(event);
}
```

**实时通信 (WebSocket)**
```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new NotificationWebSocketHandler(), "/ws/notifications");
    }
}
```

### 2.4 安全架构

#### 认证授权流程
```
客户端 → 登录请求 → 认证服务 → 验证凭据 → 生成 JWT Token
       ← 返回Token ←        ← 存储用户信息 ←

客户端 → API请求(带Token) → API网关 → 验证Token → 路由到服务
       ← 响应数据 ←                  ← 调用业务逻辑 ←
```

#### JWT Token 设计
```json
{
  "sub": "user123",
  "iat": 1640995200,
  "exp": 1641081600,
  "roles": ["PROJECT_MANAGER", "DEVELOPER"],
  "projects": [1, 2, 3],
  "permissions": ["READ_DOCUMENT", "WRITE_TASK"]
}
```

#### 数据安全策略
- **数据传输**: 全站 HTTPS + HSTS
- **数据存储**: 静态数据 AES-256 加密
- **敏感数据**: 动态脱敏显示
- **访问控制**: 行级安全策略 (RLS)
- **审计日志**: 180天操作记录保留

---

## 3. 数据库设计

### 3.1 数据库架构

#### 核心表结构
```sql
-- 组织表（多租户支持）
CREATE TABLE organizations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 用户表
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id),
    username VARCHAR(50) NOT NULL,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    status user_status DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT users_email_org_unique UNIQUE (organization_id, email)
);

-- 项目表
CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id),
    name VARCHAR(200) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    description TEXT,
    status project_status DEFAULT 'PLANNING',
    owner_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT projects_slug_org_unique UNIQUE (organization_id, slug)
);

-- 文档表
CREATE TABLE documents (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    title VARCHAR(500) NOT NULL,
    content TEXT,
    status document_status DEFAULT 'DRAFT',
    version VARCHAR(20) DEFAULT '1.0.0',
    author_id BIGINT NOT NULL REFERENCES users(id),
    search_vector tsvector,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
```

### 3.2 性能优化

#### 索引策略
```sql
-- 复合索引优化常用查询
CREATE INDEX CONCURRENTLY idx_documents_project_status_created
ON documents(project_id, status, created_at DESC) WHERE deleted_at IS NULL;

-- 全文搜索索引
CREATE INDEX CONCURRENTLY idx_documents_search_vector
ON documents USING gin(search_vector);

-- 用户权限查询优化
CREATE INDEX CONCURRENTLY idx_project_members_user_project
ON project_members(user_id, project_id) WHERE is_active = TRUE;
```

#### 分区策略
```sql
-- 活动日志表按时间分区
CREATE TABLE activity_logs (
    id BIGSERIAL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    -- 其他字段...
) PARTITION BY RANGE (created_at);

-- 创建月度分区
CREATE TABLE activity_logs_202412 PARTITION OF activity_logs
    FOR VALUES FROM ('2024-12-01') TO ('2025-01-01');
```

### 3.3 数据一致性

#### 软删除策略
```sql
-- 软删除触发器
CREATE OR REPLACE FUNCTION soft_delete_cascade()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE documents SET deleted_at = CURRENT_TIMESTAMP
    WHERE project_id = OLD.id AND deleted_at IS NULL;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER soft_delete_project_documents
    AFTER UPDATE OF deleted_at ON projects
    FOR EACH ROW EXECUTE FUNCTION soft_delete_cascade();
```

#### 事务管理
```java
@Transactional(rollbackFor = Exception.class)
public class DocumentService {

    @Transactional(readOnly = true)
    public Document getDocument(Long id) {
        return documentRepository.findById(id);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateSearchIndex(Long documentId) {
        // 独立事务更新搜索索引
    }
}
```

---

## 4. 搜索引擎设计

### 4.1 Elasticsearch 索引设计

#### 文档索引映射
```json
{
  "mappings": {
    "properties": {
      "title": {
        "type": "text",
        "analyzer": "chinese_analyzer",
        "fields": {
          "keyword": {"type": "keyword"},
          "pinyin": {"type": "text", "analyzer": "pinyin_analyzer"},
          "autocomplete": {"type": "text", "analyzer": "autocomplete_analyzer"}
        }
      },
      "content": {
        "type": "text",
        "analyzer": "chinese_analyzer"
      },
      "tags": {"type": "keyword"},
      "project_id": {"type": "long"},
      "author": {
        "type": "object",
        "properties": {
          "id": {"type": "long"},
          "name": {"type": "text", "analyzer": "chinese_analyzer"}
        }
      },
      "created_at": {"type": "date"},
      "suggest": {
        "type": "completion",
        "contexts": [
          {"name": "project", "type": "category"}
        ]
      }
    }
  }
}
```

#### 搜索模板
```json
{
  "script": {
    "lang": "mustache",
    "source": {
      "query": {
        "bool": {
          "must": [
            {
              "multi_match": {
                "query": "{{query}}",
                "fields": ["title^3", "content^1", "tags^2"],
                "type": "best_fields",
                "fuzziness": "AUTO"
              }
            }
          ],
          "filter": [
            {"terms": {"project_id": "{{project_ids}}"}}
          ]
        }
      },
      "highlight": {
        "fields": {
          "title": {"number_of_fragments": 0},
          "content": {"fragment_size": 150}
        }
      },
      "sort": [
        {"_score": {"order": "desc"}},
        {"updated_at": {"order": "desc"}}
      ]
    }
  }
}
```

### 4.2 智能推荐算法

#### 相似文档推荐
```java
@Service
public class RecommendationService {

    public List<Document> findSimilarDocuments(Long documentId, int limit) {
        SearchSourceBuilder searchSource = new SearchSourceBuilder()
            .query(QueryBuilders.moreLikeThisQuery(
                new String[]{"title", "content", "tags"},
                new Item[]{new Item("documents", documentId.toString())})
                .minTermFreq(2)
                .maxQueryTerms(12))
            .size(limit);

        return elasticsearchClient.search(searchSource);
    }
}
```

#### 搜索建议
```java
@RestController
public class SearchController {

    @GetMapping("/search/suggestions")
    public List<String> getSearchSuggestions(
            @RequestParam String prefix,
            @RequestParam(required = false) Long projectId) {

        SuggestBuilder suggestBuilder = new SuggestBuilder()
            .addSuggestion("document_suggest",
                SuggestBuilders.completionSuggestion("suggest")
                    .prefix(prefix)
                    .size(10)
                    .contexts("project", projectId.toString()));

        return searchService.getSuggestions(suggestBuilder);
    }
}
```

### 4.3 数据同步策略

#### 实时同步
```java
@EventListener
@Async
public void handleDocumentUpdated(DocumentUpdatedEvent event) {
    // 异步更新 Elasticsearch 索引
    SearchDocument searchDoc = SearchDocument.builder()
        .id(event.getDocumentId())
        .title(event.getTitle())
        .content(event.getContent())
        .projectId(event.getProjectId())
        .build();

    elasticsearchTemplate.save(searchDoc);
}
```

#### 批量同步
```java
@Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点
public void syncDocumentsToElasticsearch() {
    List<Document> documents = documentRepository
        .findModifiedSince(LocalDateTime.now().minusDays(1));

    List<SearchDocument> searchDocs = documents.stream()
        .map(this::convertToSearchDocument)
        .collect(Collectors.toList());

    elasticsearchTemplate.save(searchDocs);
}
```

---

## 5. API 接口设计

### 5.1 RESTful API 规范

#### 统一响应格式
```java
@Data
@Builder
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String errorCode;
    private Long timestamp;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .success(true)
            .data(data)
            .timestamp(System.currentTimeMillis())
            .build();
    }

    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .errorCode(errorCode)
            .timestamp(System.currentTimeMillis())
            .build();
    }
}
```

#### 分页响应
```java
@Data
public class PagedResponse<T> {
    private List<T> data;
    private PaginationInfo pagination;

    @Data
    public static class PaginationInfo {
        private int page;
        private int size;
        private long total;
        private int totalPages;
        private boolean hasNext;
        private boolean hasPrev;
    }
}
```

### 5.2 核心 API 端点

#### 文档管理 API
```java
@RestController
@RequestMapping("/api/v1/documents")
@PreAuthorize("hasRole('USER')")
public class DocumentController {

    @GetMapping
    @PreAuthorize("hasPermission(#projectId, 'PROJECT', 'READ')")
    public PagedResponse<DocumentDTO> getDocuments(
            @RequestParam Long projectId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        return documentService.getDocuments(projectId, page, size, search);
    }

    @PostMapping
    @PreAuthorize("hasPermission(#request.projectId, 'PROJECT', 'CREATE_DOCUMENT')")
    public ApiResponse<DocumentDTO> createDocument(
            @RequestBody @Valid CreateDocumentRequest request) {
        DocumentDTO document = documentService.createDocument(request);
        return ApiResponse.success(document);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(#id, 'DOCUMENT', 'READ')")
    public ApiResponse<DocumentDetailDTO> getDocument(@PathVariable Long id) {
        DocumentDetailDTO document = documentService.getDocumentDetail(id);
        return ApiResponse.success(document);
    }
}
```

#### 变更请求 API
```java
@RestController
@RequestMapping("/api/v1/change-requests")
public class ChangeRequestController {

    @PostMapping("/{id}/impact-analysis")
    @PreAuthorize("hasPermission(#id, 'CHANGE_REQUEST', 'ANALYZE')")
    public ApiResponse<List<ImpactAnalysisResult>> analyzeImpact(@PathVariable Long id) {
        List<ImpactAnalysisResult> results = changeRequestService.analyzeImpact(id);
        return ApiResponse.success(results);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasPermission(#id, 'CHANGE_REQUEST', 'APPROVE')")
    public ApiResponse<ChangeRequestDTO> approveChangeRequest(
            @PathVariable Long id,
            @RequestBody @Valid ApprovalRequest request) {
        ChangeRequestDTO changeRequest = changeRequestService.approve(id, request);
        return ApiResponse.success(changeRequest);
    }
}
```

### 5.3 API 安全和限流

#### 请求限流
```java
@Component
public class RateLimitingFilter implements Filter {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                        FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String clientId = getClientId(httpRequest);
        String key = "rate_limit:" + clientId;

        String count = redisTemplate.opsForValue().get(key);
        if (count == null) {
            redisTemplate.opsForValue().set(key, "1", Duration.ofMinutes(1));
        } else if (Integer.parseInt(count) >= 100) { // 100 requests per minute
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            return;
        } else {
            redisTemplate.opsForValue().increment(key);
        }

        chain.doFilter(request, response);
    }
}
```

#### API 版本控制
```java
@RestController
@RequestMapping("/api/v1/documents")
public class DocumentControllerV1 {
    // V1 实现
}

@RestController
@RequestMapping("/api/v2/documents")
public class DocumentControllerV2 {
    // V2 实现，向后兼容
}
```

---

## 6. 技术实施策略

### 6.1 三阶段开发计划

#### Phase 1: MVP 核心功能 (3个月)
```yaml
目标: 解决核心痛点，建立基础功能
范围:
  - 用户认证和权限管理
  - 项目和文档管理
  - 基础变更管理
  - 简化任务管理
  - 基础通知系统

里程碑:
  M1 (4周): 基础架构搭建
  M2 (6周): 核心业务功能
  M3 (2周): 集成测试和优化
```

#### Phase 2: 体验完善 (2个月)
```yaml
目标: 完善用户体验，引入智能化
范围:
  - 智能变更影响分析
  - 高级搜索和推荐
  - 测试用例管理
  - 移动端适配
  - 性能优化

里程碑:
  M4 (4周): 智能化功能开发
  M5 (4周): 移动端和体验优化
```

#### Phase 3: 生态集成 (2个月)
```yaml
目标: 生态集成，商业化准备
范围:
  - 第三方系统集成
  - 高级分析报告
  - 企业级安全
  - 多租户优化
  - 商业化功能

里程碑:
  M6 (4周): 生态集成开发
  M7 (4周): 商业化准备
```

### 6.2 开发流程规范

#### Git 工作流
```bash
# 功能开发流程
git checkout develop
git pull origin develop
git checkout -b feature/PRO-123-document-versioning
# 开发完成后
git add .
git commit -m "feat(documents): add version comparison feature

- Implement side-by-side version comparison
- Add visual diff highlighting
- Support multiple diff modes

Closes #123"
git push origin feature/PRO-123-document-versioning
# 创建 Pull Request
```

#### 代码审查标准
```markdown
## 代码审查检查清单

### 功能性
- [ ] 功能实现符合需求规格
- [ ] 边界条件处理完善
- [ ] 错误处理机制健全

### 安全性
- [ ] 输入验证和过滤
- [ ] SQL注入防护
- [ ] XSS攻击防护
- [ ] 权限控制正确

### 性能
- [ ] 数据库查询优化
- [ ] 缓存策略合理
- [ ] 无性能瓶颈

### 可维护性
- [ ] 代码结构清晰
- [ ] 命名规范统一
- [ ] 测试覆盖充分
```

### 6.3 质量保证体系

#### 测试策略
```yaml
单元测试 (70%):
  - 业务逻辑单元测试
  - 前端组件测试
  - 覆盖率目标: >80%

集成测试 (20%):
  - API集成测试
  - 数据库集成测试
  - 第三方服务集成测试

端到端测试 (10%):
  - 关键业务流程测试
  - 用户界面自动化测试
  - 跨浏览器兼容性测试
```

#### CI/CD 流水线
```yaml
stages:
  - validate    # 代码格式和语法检查
  - test        # 单元测试和集成测试
  - build       # 构建应用和Docker镜像
  - security    # 安全扫描和依赖检查
  - deploy      # 部署到相应环境

自动化检查:
  - ESLint + SonarQube 代码质量
  - JUnit + TestNG 测试结果
  - OWASP 依赖漏洞扫描
  - Trivy 容器镜像安全扫描
```

---

## 7. 部署指南

### 7.1 开发环境部署

#### Docker Compose 配置
```yaml
# docker-compose.dev.yml
version: '3.8'

services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: promanage_dev
      POSTGRES_USER: promanage
      POSTGRES_PASSWORD: promanage123
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes

  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.9.0
    environment:
      - discovery.type=single-node
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
      - xpack.security.enabled=false
    ports:
      - "9200:9200"

  rabbitmq:
    image: rabbitmq:3-management
    environment:
      RABBITMQ_DEFAULT_USER: promanage
      RABBITMQ_DEFAULT_PASS: promanage123
    ports:
      - "5672:5672"
      - "15672:15672"

  minio:
    image: minio/minio:latest
    environment:
      MINIO_ACCESS_KEY: minioadmin
      MINIO_SECRET_KEY: minioadmin123
    ports:
      - "9000:9000"
      - "9001:9001"
    command: server /data --console-address ":9001"

volumes:
  postgres_data:
```

#### 快速启动脚本
```bash
#!/bin/bash
# start-dev.sh

echo "Starting ProManage development environment..."

# 启动基础服务
docker-compose -f docker-compose.dev.yml up -d

# 等待数据库启动
echo "Waiting for database to be ready..."
sleep 30

# 初始化数据库
echo "Initializing database..."
docker-compose exec postgres psql -U promanage -d promanage_dev -f /init.sql

# 启动后端服务
echo "Starting backend service..."
cd backend
mvn spring-boot:run -Dspring.profiles.active=dev &

# 启动前端服务
echo "Starting frontend service..."
cd ../frontend
npm run dev &

echo "ProManage development environment is ready!"
echo "Frontend: http://localhost:3000"
echo "Backend API: http://localhost:8080"
echo "Database: localhost:5432"
```

### 7.2 生产环境部署

#### Kubernetes 部署配置
```yaml
# kubernetes/promanage-backend-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: promanage-backend
  labels:
    app: promanage-backend
spec:
  replicas: 3
  selector:
    matchLabels:
      app: promanage-backend
  template:
    metadata:
      labels:
        app: promanage-backend
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "8080"
        prometheus.io/path: "/actuator/prometheus"
    spec:
      containers:
      - name: promanage-backend
        image: registry.example.com/promanage-backend:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: DATABASE_HOST
          value: "postgres-service"
        - name: REDIS_HOST
          value: "redis-service"
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10

---
apiVersion: v1
kind: Service
metadata:
  name: promanage-backend-service
spec:
  selector:
    app: promanage-backend
  ports:
  - name: http
    port: 8080
    targetPort: 8080
  type: ClusterIP
```

#### Helm Chart 配置
```yaml
# helm/promanage/values.yaml
global:
  environment: production
  imageRegistry: registry.example.com
  imageTag: latest

backend:
  replicaCount: 3
  image:
    repository: promanage-backend
    tag: latest
  resources:
    requests:
      memory: 1Gi
      cpu: 500m
    limits:
      memory: 2Gi
      cpu: 1000m

frontend:
  replicaCount: 2
  image:
    repository: promanage-frontend
    tag: latest

postgresql:
  enabled: true
  auth:
    database: promanage
    username: promanage
  primary:
    persistence:
      enabled: true
      size: 100Gi
      storageClass: fast-ssd

redis:
  enabled: true
  auth:
    enabled: true
  master:
    persistence:
      enabled: true
      size: 20Gi

elasticsearch:
  enabled: true
  clusterName: promanage-search
  nodeGroup: master
  replicas: 3
  persistence:
    enabled: true
    size: 100Gi

ingress:
  enabled: true
  className: nginx
  annotations:
    cert-manager.io/cluster-issuer: letsencrypt-prod
    nginx.ingress.kubernetes.io/rate-limit: "100"
  hosts:
  - host: api.promanage.com
    paths:
    - path: /
      pathType: Prefix
  tls:
  - secretName: promanage-tls
    hosts:
    - api.promanage.com
```

### 7.3 监控和日志

#### Prometheus 监控配置
```yaml
# monitoring/prometheus-config.yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  - "promanage_rules.yml"

scrape_configs:
  - job_name: 'promanage-services'
    kubernetes_sd_configs:
    - role: pod
      namespaces:
        names:
        - promanage
    relabel_configs:
    - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_scrape]
      action: keep
      regex: true
```

#### 告警规则
```yaml
# monitoring/promanage_rules.yml
groups:
- name: promanage.rules
  rules:
  - alert: HighAPILatency
    expr: histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m])) > 0.3
    for: 2m
    labels:
      severity: warning
    annotations:
      summary: "API响应时间过高"
      description: "95%响应时间超过300ms"

  - alert: HighErrorRate
    expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.1
    for: 1m
    labels:
      severity: critical
    annotations:
      summary: "应用错误率过高"
      description: "5xx错误率超过10%"
```

---

## 8. 开发指南

### 8.1 环境搭建

#### 前端开发环境
```bash
# 克隆项目
git clone https://github.com/company/promanage-frontend.git
cd promanage-frontend

# 安装依赖
npm install

# 配置环境变量
cp .env.example .env.local
# 编辑 .env.local 配置开发环境参数

# 启动开发服务器
npm run dev

# 代码格式化
npm run lint
npm run format

# 运行测试
npm run test
npm run test:coverage
```

#### 后端开发环境
```bash
# 克隆项目
git clone https://github.com/company/promanage-backend.git
cd promanage-backend

# 配置数据库
docker-compose -f docker-compose.dev.yml up -d postgres redis

# 配置应用参数
cp src/main/resources/application-dev.yml.example src/main/resources/application-dev.yml
# 编辑数据库连接等配置

# 运行应用
mvn spring-boot:run -Dspring.profiles.active=dev

# 运行测试
mvn test
mvn verify  # 包含集成测试
```

### 8.2 开发规范

#### 前端开发规范
```typescript
// 组件开发示例
<template>
  <div class="document-list">
    <div class="search-bar">
      <a-input
        v-model:value="searchKeyword"
        placeholder="搜索文档..."
        @input="handleSearch"
      />
    </div>

    <div class="document-grid">
      <DocumentCard
        v-for="doc in documents"
        :key="doc.id"
        :document="doc"
        @click="handleDocumentClick"
      />
    </div>

    <Pagination
      v-model:current="currentPage"
      :total="totalCount"
      :page-size="pageSize"
      @change="handlePageChange"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useDocumentStore } from '@/stores/document'
import { Document } from '@/types/document'

interface Props {
  projectId: number
}

const props = defineProps<Props>()
const documentStore = useDocumentStore()

const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = 20

const documents = computed(() => documentStore.documents)
const totalCount = computed(() => documentStore.totalCount)

const handleSearch = async () => {
  await documentStore.searchDocuments({
    projectId: props.projectId,
    keyword: searchKeyword.value,
    page: 1,
    size: pageSize
  })
}

const handlePageChange = async (page: number) => {
  currentPage.value = page
  await documentStore.loadDocuments({
    projectId: props.projectId,
    page,
    size: pageSize
  })
}

onMounted(() => {
  handlePageChange(1)
})
</script>
```

#### 后端开发规范
```java
// Controller 层示例
@RestController
@RequestMapping("/api/v1/documents")
@Validated
@Slf4j
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    @PreAuthorize("hasPermission(#projectId, 'PROJECT', 'READ')")
    public ResponseEntity<PagedResponse<DocumentDTO>> getDocuments(
            @RequestParam @NotNull Long projectId,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size,
            @RequestParam(required = false) String search) {

        log.info("Getting documents for project {} with search: {}", projectId, search);

        PagedResponse<DocumentDTO> response = documentService.getDocuments(
            GetDocumentsRequest.builder()
                .projectId(projectId)
                .page(page)
                .size(size)
                .search(search)
                .build()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasPermission(#request.projectId, 'PROJECT', 'CREATE_DOCUMENT')")
    public ResponseEntity<ApiResponse<DocumentDTO>> createDocument(
            @RequestBody @Valid CreateDocumentRequest request) {

        log.info("Creating document: {}", request.getTitle());

        try {
            DocumentDTO document = documentService.createDocument(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(document));
        } catch (BusinessException e) {
            log.error("Failed to create document", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), e.getErrorCode()));
        }
    }
}

// Service 层示例
@Service
@Transactional(readOnly = true)
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;
    private final SearchService searchService;

    @Transactional
    public DocumentDTO createDocument(CreateDocumentRequest request) {
        // 验证项目存在
        validateProjectExists(request.getProjectId());

        // 创建文档实体
        Document document = Document.builder()
            .title(request.getTitle())
            .content(request.getContent())
            .projectId(request.getProjectId())
            .authorId(SecurityUtils.getCurrentUserId())
            .status(DocumentStatus.DRAFT)
            .version("1.0.0")
            .build();

        // 保存文档
        document = documentRepository.save(document);

        // 异步更新搜索索引
        searchService.indexDocumentAsync(document);

        // 发布文档创建事件
        publishDocumentCreatedEvent(document);

        return documentMapper.toDTO(document);
    }

    private void validateProjectExists(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new BusinessException("项目不存在", "PROJECT_NOT_FOUND");
        }
    }

    @EventListener
    @Async
    public void handleDocumentCreated(DocumentCreatedEvent event) {
        log.info("Document created: {}", event.getDocumentId());
        // 处理文档创建后的业务逻辑
    }
}
```

### 8.3 测试指南

#### 单元测试示例
```java
@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private SearchService searchService;

    @InjectMocks
    private DocumentService documentService;

    @Test
    @DisplayName("创建文档 - 成功场景")
    void createDocument_Success() {
        // Arrange
        CreateDocumentRequest request = CreateDocumentRequest.builder()
            .title("测试文档")
            .content("文档内容")
            .projectId(1L)
            .build();

        Document savedDocument = Document.builder()
            .id(1L)
            .title("测试文档")
            .content("文档内容")
            .build();

        when(documentRepository.save(any(Document.class))).thenReturn(savedDocument);

        // Act
        DocumentDTO result = documentService.createDocument(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("测试文档");
        verify(searchService).indexDocumentAsync(savedDocument);
    }
}
```

#### 集成测试示例
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "spring.profiles.active=test")
@Testcontainers
class DocumentControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DocumentRepository documentRepository;

    @Test
    @WithMockUser(roles = "USER")
    void createDocument_ShouldReturnCreatedDocument() {
        // Arrange
        CreateDocumentRequest request = new CreateDocumentRequest();
        request.setTitle("Integration Test Document");
        request.setContent("Test content");
        request.setProjectId(1L);

        // Act
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
            "/api/v1/documents", request, ApiResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(documentRepository.count()).isEqualTo(1);
    }
}
```

---

## 9. 运维监控

### 9.1 监控体系

#### 应用性能监控
```java
// 自定义监控指标
@Component
public class BusinessMetrics {

    private final MeterRegistry meterRegistry;
    private final Counter documentCreateCounter;
    private final Timer searchTimer;

    public BusinessMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.documentCreateCounter = Counter.builder("documents.created.total")
            .description("Total number of documents created")
            .register(meterRegistry);
        this.searchTimer = Timer.builder("search.duration")
            .description("Search request duration")
            .register(meterRegistry);
    }

    public void incrementDocumentCreated(String projectName) {
        documentCreateCounter.increment(Tags.of("project", projectName));
    }

    public Timer.Sample startSearchTimer() {
        return Timer.start(meterRegistry);
    }
}
```

#### 健康检查
```java
@Component
public class CustomHealthIndicator implements HealthIndicator {

    private final DatabaseHealthService databaseHealthService;
    private final RedisHealthService redisHealthService;

    @Override
    public Health health() {
        Health.Builder status = Health.up();

        // 检查数据库连接
        if (!databaseHealthService.isHealthy()) {
            status.down().withDetail("database", "Connection failed");
        }

        // 检查 Redis 连接
        if (!redisHealthService.isHealthy()) {
            status.down().withDetail("redis", "Connection failed");
        }

        // 检查磁盘空间
        long freeSpace = new File("/").getFreeSpace();
        long totalSpace = new File("/").getTotalSpace();
        double freeSpacePercent = (double) freeSpace / totalSpace * 100;

        if (freeSpacePercent < 10) {
            status.down().withDetail("disk", "Low disk space: " + freeSpacePercent + "%");
        }

        return status.build();
    }
}
```

### 9.2 日志管理

#### 日志配置
```xml
<!-- logback-spring.xml -->
<configuration>
    <springProfile name="!prod">
        <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder>
                <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>
        <root level="INFO">
            <appender-ref ref="CONSOLE" />
        </root>
    </springProfile>

    <springProfile name="prod">
        <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>logs/promanage.log</file>
            <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                <fileNamePattern>logs/promanage.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
                <maxFileSize>100MB</maxFileSize>
                <maxHistory>30</maxHistory>
                <totalSizeCap>3GB</totalSizeCap>
            </rollingPolicy>
            <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
                <providers>
                    <timestamp/>
                    <logLevel/>
                    <loggerName/>
                    <message/>
                    <mdc/>
                    <stackTrace/>
                </providers>
            </encoder>
        </appender>
        <root level="INFO">
            <appender-ref ref="FILE" />
        </root>
    </springProfile>
</configuration>
```

#### 结构化日志
```java
@Slf4j
@Component
public class AuditLogger {

    public void logUserAction(String action, Long userId, Object details) {
        MDC.put("action", action);
        MDC.put("userId", userId.toString());
        MDC.put("details", JsonUtils.toJson(details));

        log.info("User action performed: {}", action);

        MDC.clear();
    }

    public void logSecurityEvent(String event, String ipAddress, String userAgent) {
        MDC.put("eventType", "SECURITY");
        MDC.put("event", event);
        MDC.put("ipAddress", ipAddress);
        MDC.put("userAgent", userAgent);

        log.warn("Security event: {}", event);

        MDC.clear();
    }
}
```

### 9.3 告警和应急响应

#### 告警配置
```yaml
# alertmanager.yml
global:
  smtp_smarthost: 'smtp.company.com:587'
  smtp_from: 'alerts@company.com'

route:
  group_by: ['alertname']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 1h
  receiver: 'web.hook'

receivers:
- name: 'web.hook'
  email_configs:
  - to: 'ops@company.com'
    subject: 'ProManage Alert: {{ .GroupLabels.alertname }}'
    body: |
      {{ range .Alerts }}
      Alert: {{ .Annotations.summary }}
      Description: {{ .Annotations.description }}
      {{ end }}

  slack_configs:
  - api_url: 'https://hooks.slack.com/services/...'
    channel: '#ops-alerts'
    title: 'ProManage Alert'
    text: '{{ range .Alerts }}{{ .Annotations.summary }}{{ end }}'
```

#### 应急响应手册
```markdown
# ProManage 应急响应手册

## P0 级别故障 (系统完全不可用)

### 响应时间: 15分钟内
### 解决时间: 1小时内

#### 检查步骤:
1. 检查服务状态: `kubectl get pods -n promanage`
2. 检查资源使用: `kubectl top pods -n promanage`
3. 检查日志: `kubectl logs -f deployment/promanage-backend`
4. 检查数据库连接: `psql -h postgres-service -U promanage`

#### 常见问题处理:
- **数据库连接失败**: 重启数据库 Pod，检查连接池配置
- **服务Pod崩溃**: 检查资源限制，查看Pod日志
- **网络问题**: 检查Ingress配置，验证DNS解析

#### 回滚步骤:
```bash
# 回滚到上一个版本
kubectl rollout undo deployment/promanage-backend
kubectl rollout undo deployment/promanage-frontend

# 验证回滚结果
kubectl rollout status deployment/promanage-backend
```

## P1 级别故障 (核心功能受影响)

### 响应时间: 30分钟内
### 解决时间: 4小时内

#### 处理流程:
1. 确认故障范围和影响
2. 查看监控数据和日志
3. 分析根本原因
4. 实施修复方案
5. 验证修复结果
6. 更新文档记录
```

---

## 10. 安全方案

### 10.1 应用安全

#### 输入验证和过滤
```java
@Component
public class SecurityValidator {

    private static final Pattern SQL_INJECTION_PATTERN =
        Pattern.compile("('.+(\\-\\-|;|\\||\\*|%|=))|('\\s*(or|and)\\s+')",
                       Pattern.CASE_INSENSITIVE);

    private static final Pattern XSS_PATTERN =
        Pattern.compile("<script[^>]*>.*?</script>|javascript:|on\\w+\\s*=",
                       Pattern.CASE_INSENSITIVE);

    public void validateInput(String input) {
        if (input == null) return;

        // SQL 注入检查
        if (SQL_INJECTION_PATTERN.matcher(input).find()) {
            throw new SecurityException("Potential SQL injection detected");
        }

        // XSS 攻击检查
        if (XSS_PATTERN.matcher(input).find()) {
            throw new SecurityException("Potential XSS attack detected");
        }
    }

    public String sanitizeHtml(String input) {
        if (input == null) return null;

        return Jsoup.clean(input, Whitelist.basicWithImages()
            .addTags("h1", "h2", "h3", "h4", "h5", "h6")
            .addAttributes("img", "alt", "src", "title"));
    }
}
```

#### 权限控制
```java
@PreAuthorize("hasPermission(#documentId, 'DOCUMENT', 'READ')")
public DocumentDTO getDocument(Long documentId) {
    return documentService.getDocument(documentId);
}

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Override
    public boolean hasPermission(Authentication authentication,
                               Object targetDomainObject,
                               Object permission) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String targetType = targetDomainObject.getClass().getSimpleName().toUpperCase();
        String permissionStr = permission.toString();

        UserDetails user = (UserDetails) authentication.getPrincipal();
        return permissionService.hasPermission(user.getId(), targetType, permissionStr);
    }

    @Override
    public boolean hasPermission(Authentication authentication,
                               Serializable targetId,
                               String targetType,
                               Object permission) {
        // 实现特定资源的权限检查
        return permissionService.hasPermission(
            getCurrentUserId(authentication),
            targetId,
            targetType,
            permission.toString()
        );
    }
}
```

### 10.2 数据安全

#### 敏感数据加密
```java
@Component
public class DataEncryptionService {

    private final AESUtil aesUtil;

    @EventListener
    public void handleDocumentSave(DocumentSaveEvent event) {
        Document document = event.getDocument();

        // 加密敏感内容
        if (document.isSensitive()) {
            document.setContent(aesUtil.encrypt(document.getContent()));
        }
    }

    @EventListener
    public void handleDocumentLoad(DocumentLoadEvent event) {
        Document document = event.getDocument();

        // 解密敏感内容
        if (document.isSensitive()) {
            document.setContent(aesUtil.decrypt(document.getContent()));
        }
    }
}

@Component
public class AESUtil {

    @Value("${app.security.encryption.key}")
    private String encryptionKey;

    public String encrypt(String plainText) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(
                encryptionKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            byte[] encrypted = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new SecurityException("Encryption failed", e);
        }
    }

    public String decrypt(String encryptedText) {
        // 解密实现
    }
}
```

#### 数据脱敏
```java
@Component
public class DataMaskingService {

    public UserDTO maskUserData(User user, UserContext requestor) {
        UserDTO dto = UserDTO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .build();

        // 根据权限决定是否脱敏
        if (hasPermission(requestor, "VIEW_USER_EMAIL")) {
            dto.setEmail(user.getEmail());
        } else {
            dto.setEmail(maskEmail(user.getEmail()));
        }

        if (hasPermission(requestor, "VIEW_USER_PHONE")) {
            dto.setPhone(user.getPhone());
        } else {
            dto.setPhone(maskPhone(user.getPhone()));
        }

        return dto;
    }

    private String maskEmail(String email) {
        if (email == null || email.length() < 3) return "***";

        int atIndex = email.indexOf('@');
        if (atIndex > 2) {
            return email.substring(0, 2) + "***" + email.substring(atIndex);
        }
        return "***" + email.substring(atIndex);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return "***";

        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
```

### 10.3 网络安全

#### HTTPS 配置
```yaml
# nginx.conf
server {
    listen 443 ssl http2;
    server_name api.promanage.com;

    ssl_certificate /etc/ssl/certs/promanage.crt;
    ssl_certificate_key /etc/ssl/private/promanage.key;

    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers 'ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256';
    ssl_prefer_server_ciphers off;

    add_header Strict-Transport-Security "max-age=63072000" always;
    add_header X-Frame-Options DENY always;
    add_header X-Content-Type-Options nosniff always;
    add_header X-XSS-Protection "1; mode=block" always;

    location / {
        proxy_pass http://promanage-backend-service:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

#### API 安全配置
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/api/v1/**").authenticated()
                .anyRequest().denyAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            )
            .headers(headers -> headers
                .frameOptions().deny()
                .contentTypeOptions()
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubdomains(true)
                )
            );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter =
            new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix("ROLE_");
        authoritiesConverter.setAuthoritiesClaimName("roles");

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return jwtConverter;
    }
}
```

---

## 📋 总结

本技术设计文档提供了 ProManage 项目管理系统的完整技术实施方案，涵盖了从系统架构到具体实现的所有关键方面：

### ✅ 已完成的设计组件

1. **系统架构设计** - 微服务架构，支持高并发和可扩展性
2. **数据库设计** - PostgreSQL 主数据库，支持复杂业务逻辑
3. **搜索引擎设计** - Elasticsearch 全文搜索和智能推荐
4. **API 接口设计** - RESTful API 规范，支持版本控制
5. **技术实施策略** - 三阶段开发计划，质量保证体系
6. **部署运维方案** - 容器化部署，监控告警体系
7. **安全防护方案** - 全链路安全设计，数据保护

### 🎯 关键技术决策

- **架构模式**: 微服务架构 + 事件驱动
- **技术栈**: Spring Boot 3.x + Vue 3 + TypeScript
- **数据存储**: PostgreSQL + Elasticsearch + Redis
- **部署策略**: Kubernetes + Docker + Helm
- **监控体系**: Prometheus + Grafana + Jaeger
- **安全策略**: JWT + RBAC + 数据加密

### 📈 预期达成目标

| 指标类型 | 目标值 | 验证方式 |
|---------|--------|----------|
| **性能指标** | API 响应 P95 ≤ 300ms | 压力测试 |
| **可用性** | 系统可用性 ≥ 99.9% | 监控统计 |
| **并发能力** | 支持 500+ 并发用户 | 负载测试 |
| **搜索性能** | 搜索响应 P95 ≤ 2秒 | 搜索压力测试 |
| **业务效率** | 团队协作效率提升 50% | 业务指标统计 |

### 🚀 实施建议

1. **优先级排序**: 按照 MVP → 体验完善 → 生态集成的顺序实施
2. **质量保证**: 建立完善的测试体系和代码审查流程
3. **监控先行**: 在功能开发的同时建立监控和告警体系
4. **安全内建**: 将安全考虑融入到开发流程的每个环节
5. **持续优化**: 基于用户反馈和监控数据持续优化系统

此技术设计文档为 ProManage 项目的成功实施提供了详细的技术指导，确保项目能够按时、高质量地交付，并为后续的持续发展奠定坚实的技术基础。

---

**文档版本**: V1.0
**最后更新**: 2024-12-30
**技术架构师**: Senior Full-Stack Architect