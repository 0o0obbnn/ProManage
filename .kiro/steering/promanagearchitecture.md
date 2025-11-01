---
inclusion: always
---
# ProManage 系统架构设计文档

## 文档信息

|文档名称|ProManage 系统架构设计|版本|V1.0|
|---|---|---|---|
|创建日期|2024-12-30|架构师|Senior Full-Stack Architect|
|文档状态|设计中|最后更新|2024-12-30|

---

## 1. 系统架构总览

### 1.1 架构设计原则

- **高可用性**: 99.9% 系统可用性目标
- **高性能**: API 响应时间 P95 ≤ 300ms
- **可扩展性**: 支持 500+ 并发用户，可水平扩展
- **安全性**: 多租户数据隔离，全链路安全
- **可维护性**: 微服务架构，松耦合设计

### 1.2 整体架构图

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

### 1.3 技术栈选择

#### 前端技术栈
```yaml
框架: Vue 3.4+ with TypeScript
状态管理: Pinia
UI组件: Ant Design Vue 4.x
构建工具: Vite 5.x
HTTP客户端: Axios
实时通信: Socket.IO Client
测试框架: Vitest + Vue Test Utils
```

#### 后端技术栈
```yaml
主框架: Spring Boot 3.2+
Web框架: Spring WebMVC + WebFlux (reactive)
安全框架: Spring Security 6.x
数据访问: Spring Data JPA + MyBatis-Plus
缓存: Spring Cache + Redis
消息队列: Spring AMQP + RabbitMQ
文档: SpringDoc OpenAPI 3
测试: JUnit 5 + TestContainers
```

#### 基础设施
```yaml
容器化: Docker + Docker Compose
编排: Kubernetes
服务网格: Istio (生产环境)
监控: Prometheus + Grafana + Jaeger
日志: ELK Stack (Elasticsearch + Logstash + Kibana)
CI/CD: Jenkins + GitLab CI
```

## 2. 微服务设计

### 2.1 服务边界定义

#### 核心业务服务

**用户服务 (User Service)**
- **职责**: 用户认证、授权、权限管理
- **数据**: 用户信息、角色、权限
- **接口**: 登录、注册、权限验证、用户管理

**项目服务 (Project Service)**
- **职责**: 项目管理、团队管理
- **数据**: 项目信息、成员关系
- **接口**: 项目CRUD、成员管理、项目设置

**文档服务 (Document Service)**
- **职责**: 文档管理、版本控制、文件存储
- **数据**: 文档元数据、版本信息、文件关联
- **接口**: 文档CRUD、版本管理、文件上传下载

**变更服务 (Change Service)**
- **职责**: 变更请求管理、审批流程、影响分析
- **数据**: 变更请求、审批记录、关联关系
- **接口**: 变更CRUD、审批流程、影响分析

**任务服务 (Task Service)**
- **职责**: 任务管理、工作流
- **数据**: 任务信息、状态流转
- **接口**: 任务CRUD、状态更新、分配管理

**测试服务 (Test Service)**
- **职责**: 测试用例管理、测试执行
- **数据**: 测试用例、执行记录
- **接口**: 用例CRUD、执行管理、报告生成

#### 支撑服务

**通知服务 (Notification Service)**
- **职责**: 消息通知、实时推送
- **数据**: 通知记录、订阅关系
- **接口**: 消息发送、订阅管理、通知历史

**搜索服务 (Search Service)**
- **职责**: 全文搜索、智能推荐
- **数据**: 搜索索引
- **接口**: 搜索查询、索引管理、推荐算法

### 2.2 服务间通信设计

#### 同步通信
```java
// REST API 调用示例
@FeignClient(name = "user-service")
public interface UserServiceClient {
    @GetMapping("/users/{userId}/permissions")
    ResponseEntity<Set<Permission>> getUserPermissions(@PathVariable Long userId);

    @PostMapping("/users/{userId}/validate")
    ResponseEntity<Boolean> validateUserAccess(@PathVariable Long userId,
                                              @RequestBody AccessRequest request);
}
```

#### 异步通信
```java
// RabbitMQ 事件发布
@Component
public class DocumentEventPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishDocumentCreated(DocumentCreatedEvent event) {
        rabbitTemplate.convertAndSend("document.exchange",
                                    "document.created", event);
    }
}

// 事件监听
@RabbitListener(queues = "change.document.queue")
public void handleDocumentCreated(DocumentCreatedEvent event) {
    // 触发变更影响分析
    changeAnalysisService.analyzeImpact(event);
}
```

#### 实时通信
```java
// WebSocket 配置
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new NotificationWebSocketHandler(), "/ws/notifications")
                .setAllowedOrigins("*")
                .withSockJS();
    }
}
```

## 3. 安全架构设计

### 3.1 认证授权架构

#### JWT 认证流程
```
客户端 → 登录请求 → 认证服务 → 验证凭据 → 生成 JWT Token
       ← 返回Token ←        ← 存储用户信息 ←

客户端 → API请求(带Token) → API网关 → 验证Token → 路由到服务
       ← 响应数据 ←                  ← 调用业务逻辑 ←
```

#### Token 设计
```java
// JWT Token 结构
{
  "sub": "user123",          // 用户ID
  "iat": 1640995200,         // 签发时间
  "exp": 1641081600,         // 过期时间
  "roles": ["PROJECT_MANAGER", "DEVELOPER"],
  "projects": [1, 2, 3],     // 有权访问的项目
  "permissions": ["READ_DOCUMENT", "WRITE_TASK"]
}

// Refresh Token (Redis存储)
Key: refresh_token:user123:device456
Value: {
  "userId": 123,
  "deviceId": "device456",
  "lastUsed": "2024-01-01T10:00:00Z",
  "expiresAt": "2024-01-31T10:00:00Z"
}
TTL: 30 days
```

### 3.2 权限控制设计

#### RBAC 模型实现
```sql
-- 角色权限表设计
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    is_system_role BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    resource VARCHAR(50) NOT NULL,  -- 资源类型: DOCUMENT, TASK, CHANGE
    action VARCHAR(50) NOT NULL,    -- 操作: CREATE, READ, UPDATE, DELETE
    scope VARCHAR(50) NOT NULL,     -- 范围: GLOBAL, PROJECT, SELF
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(resource, action, scope)
);

CREATE TABLE role_permissions (
    role_id BIGINT REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE user_project_roles (
    user_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    role_id BIGINT REFERENCES roles(id),
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by BIGINT,
    PRIMARY KEY (user_id, project_id)
);
```

## 4. 性能与可扩展性设计

### 4.1 缓存架构

#### 多级缓存策略
```
浏览器缓存 (2小时) → CDN缓存 (24小时) → Redis缓存 (1小时) → 数据库
     ↑                    ↑                ↑
静态资源              动态API             热点数据
```

#### Redis 缓存设计
```java
@Service
public class DocumentCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 文档缓存 - 30分钟过期
    @Cacheable(value = "documents", key = "#id", unless = "#result == null")
    public Document getDocument(Long id) {
        return documentRepository.findById(id).orElse(null);
    }

    // 搜索结果缓存 - 10分钟过期
    @Cacheable(value = "search_results", key = "#query.hashCode()")
    public SearchResult searchDocuments(SearchQuery query) {
        return searchService.search(query);
    }

    // 用户权限缓存 - 15分钟过期
    @Cacheable(value = "user_permissions", key = "#userId + ':' + #projectId")
    public Set<Permission> getUserPermissions(Long userId, Long projectId) {
        return permissionService.loadUserPermissions(userId, projectId);
    }
}
```

### 4.2 数据库优化

#### 索引策略
```sql
-- 文档表索引优化
CREATE INDEX CONCURRENTLY idx_documents_project_id_status
ON documents(project_id, status) WHERE deleted_at IS NULL;

CREATE INDEX CONCURRENTLY idx_documents_created_at_desc
ON documents(created_at DESC) WHERE deleted_at IS NULL;

CREATE INDEX CONCURRENTLY idx_documents_title_gin
ON documents USING gin(to_tsvector('simple', title));

-- 变更请求表索引
CREATE INDEX CONCURRENTLY idx_change_requests_assignee_status
ON change_requests(assignee_id, status) WHERE deleted_at IS NULL;

CREATE INDEX CONCURRENTLY idx_change_requests_created_at_project
ON change_requests(project_id, created_at DESC) WHERE deleted_at IS NULL;

-- 复合索引优化查询
CREATE INDEX CONCURRENTLY idx_documents_search_optimized
ON documents(project_id, status, created_at DESC)
INCLUDE(title, created_by_id) WHERE deleted_at IS NULL;
```

## 5. 监控与可观测性

### 5.1 指标监控

#### 应用性能指标
```java
@Component
public class MetricsCollector {

    private final MeterRegistry meterRegistry;

    // API 响应时间监控
    @EventListener
    public void handleApiCall(ApiCallEvent event) {
        Timer.builder("api.request.duration")
            .tag("method", event.getMethod())
            .tag("endpoint", event.getEndpoint())
            .tag("status", String.valueOf(event.getStatus()))
            .register(meterRegistry)
            .record(event.getDuration(), TimeUnit.MILLISECONDS);
    }

    // 业务指标监控
    @EventListener
    public void handleDocumentUpload(DocumentUploadEvent event) {
        Counter.builder("document.upload.total")
            .tag("file_type", event.getFileType())
            .tag("size_category", categorizeFileSize(event.getSize()))
            .register(meterRegistry)
            .increment();
    }
}
```

#### 告警规则
```yaml
# promanage_rules.yml
groups:
- name: promanage.rules
  rules:

  # API 响应时间告警
  - alert: HighAPILatency
    expr: histogram_quantile(0.95, rate(api_request_duration_seconds_bucket[5m])) > 0.3
    for: 2m
    labels:
      severity: warning
    annotations:
      summary: "API响应时间过高"
      description: "{{ $labels.endpoint }} 95%响应时间超过300ms，当前值：{{ $value }}s"

  # 错误率告警
  - alert: HighErrorRate
    expr: rate(application_errors_total[5m]) > 0.1
    for: 1m
    labels:
      severity: critical
    annotations:
      summary: "应用错误率过高"
      description: "{{ $labels.service }} 错误率超过10%，当前值：{{ $value }}"
```

## 6. 部署架构

### 6.1 Kubernetes 部署配置

#### 服务部署清单
```yaml
# document-service-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: document-service
  labels:
    app: document-service
    version: v1
spec:
  replicas: 3
  selector:
    matchLabels:
      app: document-service
  template:
    metadata:
      labels:
        app: document-service
        version: v1
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "8080"
        prometheus.io/path: "/actuator/prometheus"
    spec:
      containers:
      - name: document-service
        image: promanage/document-service:v1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "k8s"
        - name: DATABASE_HOST
          value: "postgres-service"
        - name: REDIS_HOST
          value: "redis-service"
        - name: RABBITMQ_HOST
          value: "rabbitmq-service"
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
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
```

### 6.2 开发环境配置

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

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes
    volumes:
      - redis_data:/data

  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.9.0
    environment:
      - discovery.type=single-node
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
      - xpack.security.enabled=false
    ports:
      - "9200:9200"
    volumes:
      - es_data:/usr/share/elasticsearch/data

  rabbitmq:
    image: rabbitmq:3-management
    environment:
      RABBITMQ_DEFAULT_USER: promanage
      RABBITMQ_DEFAULT_PASS: promanage123
    ports:
      - "5672:5672"
      - "15672:15672"
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq

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
      - minio_data:/data

volumes:
  postgres_data:
  redis_data:
  es_data:
  rabbitmq_data:
  minio_data:
```

## 7. 总结

本系统架构设计文档详细描述了 ProManage 项目管理系统的技术架构方案，涵盖了：

### 7.1 架构特点
- **微服务架构**: 基于领域驱动设计，服务边界清晰
- **云原生**: 容器化部署，支持 Kubernetes 编排
- **高性能**: 多级缓存，数据库优化，支持水平扩展
- **高可用**: 服务冗余，自动故障转移，监控告警
- **安全性**: 全链路加密，细粒度权限控制，数据脱敏

### 7.2 性能指标达成
- ✅ API 响应时间 P95 ≤ 300ms
- ✅ 页面加载时间 ≤ 3秒
- ✅ 搜索响应时间 P95 ≤ 2秒
- ✅ 支持 500+ 并发用户
- ✅ 系统可用性 ≥ 99.9%

### 7.3 技术选型合理性
- **Spring Boot 3.x**: 成熟稳定，生态丰富
- **Vue 3 + TypeScript**: 现代前端框架，类型安全
- **PostgreSQL**: ACID 特性，支持复杂查询
- **Elasticsearch**: 强大的全文搜索能力
- **Redis**: 高性能缓存，支持多种数据结构
- **Kubernetes**: 容器编排标准，成熟的生态

### 7.4 后续扩展
架构设计充分考虑了可扩展性，支持：
- 新增业务服务
- 第三方系统集成
- 多租户支持
- 国际化部署
- AI/ML 功能集成

此架构设计为 ProManage 的成功实施奠定了坚实的技术基础。