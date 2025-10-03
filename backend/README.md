# ProManage Backend

<div align="center">

![ProManage Logo](https://via.placeholder.com/150)

**ProManage** - 智能项目管理系统后端服务

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

</div>

---

## 📖 项目简介

ProManage 是一个智能、集成的项目管理系统,提供统一知识库、智能变更管理、测试用例管理等核心功能。本项目是 ProManage 的后端服务,采用微服务架构和现代化技术栈。

### ✨ 核心特性

- **统一知识库**: 集中管理项目文档,支持多格式、版本控制和全文搜索
- **智能变更管理**: 自动化变更流程,智能影响分析和通知
- **权限管理**: 完善的 RBAC 权限控制,支持项目级和数据级权限
- **我的工作台**: 个性化工作台,聚合待办事项和项目动态
- **RESTful API**: 标准化 API 设计,完整的 Swagger 文档

---

## 🏗️ 技术架构

### 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| **Spring Boot** | 3.2.5 | 核心框架 |
| **Java** | 17 | 开发语言 |
| **PostgreSQL** | 15+ | 主数据库 |
| **MyBatis Plus** | 3.5.6 | ORM 框架 |
| **Redis** | 7.0+ | 缓存 |
| **Elasticsearch** | 8.0+ | 搜索引擎 |
| **RabbitMQ** | 3.12+ | 消息队列 |
| **MinIO** | Latest | 文件存储 |
| **JWT** | 0.12.5 | 认证授权 |
| **SpringDoc OpenAPI** | 2.5.0 | API 文档 |

### 项目结构

```
backend/
├── promanage-common/          # 公共模块
│   ├── domain/                # 公共领域对象 (Result, PageResult, BaseEntity)
│   ├── exception/             # 异常定义和全局处理
│   ├── constant/              # 系统常量
│   ├── enums/                 # 公共枚举
│   └── util/                  # 工具类
├── promanage-infrastructure/  # 基础设施模块
│   ├── config/                # 配置类 (MyBatis, Redis, Security)
│   ├── security/              # 安全配置 (JWT, RBAC)
│   ├── cache/                 # 缓存管理
│   ├── mq/                    # 消息队列
│   └── storage/               # 文件存储
├── promanage-service/         # 业务服务模块
│   ├── entity/                # 数据库实体
│   ├── mapper/                # MyBatis Mapper
│   ├── service/               # 业务服务接口
│   ├── impl/                  # 业务服务实现
│   └── converter/             # 对象转换器
└── promanage-api/             # API 模块
    ├── controller/            # REST 控制器
    ├── dto/                   # 数据传输对象
    └── ProManageApplication   # 启动类
```

### 系统架构图

```
┌─────────────────────────────────────────────────────────┐
│                      前端层                               │
│            (Vue 3 + TypeScript + Vite)                   │
└─────────────────┬───────────────────────────────────────┘
                  │ HTTPS/REST API
┌─────────────────▼───────────────────────────────────────┐
│                    API 网关层                             │
│              (Spring Cloud Gateway)                      │
└─────────────────┬───────────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────────┐
│                   业务服务层                              │
│    ┌─────────┬──────────┬──────────┬─────────────┐     │
│    │ 用户服务 │ 文档服务  │ 项目服务  │ 变更服务    │     │
│    └─────────┴──────────┴──────────┴─────────────┘     │
└─────────────────┬───────────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────────┐
│                   数据层 & 中间件                         │
│  ┌──────────┬────────┬──────────┬────────┬─────────┐  │
│  │PostgreSQL│ Redis  │Elasticsearch│RabbitMQ│  MinIO │  │
│  └──────────┴────────┴──────────┴────────┴─────────┘  │
└─────────────────────────────────────────────────────────┘
```

---

## 🚀 快速开始

### 环境要求

- **JDK**: 17 或更高版本
- **Maven**: 3.9 或更高版本
- **PostgreSQL**: 15 或更高版本
- **Redis**: 7.0 或更高版本
- **Node.js**: 18+ (用于前端开发)

### 安装步骤

#### 1. 克隆项目

```bash
git clone https://github.com/yourusername/promanage.git
cd promanage/backend
```

#### 2. 配置数据库

```bash
# 创建数据库
createdb promanage

# 执行初始化脚本
psql -d promanage -f ../ProManage_Database_Schema.sql
```

#### 3. 配置 Redis

```bash
# 启动 Redis
redis-server
```

#### 4. 修改配置文件

编辑 `promanage-api/src/main/resources/application-dev.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/promanage
    username: your_username
    password: your_password
  redis:
    host: localhost
    port: 6379
```

#### 5. 构建项目

```bash
# 完整构建
mvn clean install

# 跳过测试
mvn clean install -DskipTests
```

#### 6. 运行应用

```bash
# 方式1: 使用 Maven
cd promanage-api
mvn spring-boot:run

# 方式2: 使用 JAR
java -jar promanage-api/target/promanage-api-1.0.0-SNAPSHOT.jar

# 方式3: 指定环境
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### 7. 验证启动

访问以下地址确认服务启动成功:

- **应用首页**: http://localhost:8080
- **Swagger API 文档**: http://localhost:8080/swagger-ui.html
- **健康检查**: http://localhost:8080/actuator/health

---

## 📝 API 文档

### Swagger UI

启动应用后访问: **http://localhost:8080/swagger-ui.html**

### 核心 API 端点

#### 认证相关

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | 用户登录 |
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/logout` | 用户登出 |
| POST | `/api/auth/refresh` | 刷新 Token |
| GET | `/api/auth/current` | 获取当前用户信息 |

#### 用户管理

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users` | 获取用户列表 |
| GET | `/api/users/{id}` | 获取用户详情 |
| POST | `/api/users` | 创建用户 |
| PUT | `/api/users/{id}` | 更新用户 |
| DELETE | `/api/users/{id}` | 删除用户 |

#### 文档管理

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/documents` | 获取文档列表 |
| GET | `/api/documents/{id}` | 获取文档详情 |
| POST | `/api/documents` | 创建文档 |
| PUT | `/api/documents/{id}` | 更新文档 |
| DELETE | `/api/documents/{id}` | 删除文档 |
| POST | `/api/documents/{id}/publish` | 发布文档 |
| GET | `/api/documents/{id}/versions` | 获取文档版本 |

#### 项目管理

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/projects` | 获取项目列表 |
| GET | `/api/projects/{id}` | 获取项目详情 |
| POST | `/api/projects` | 创建项目 |
| PUT | `/api/projects/{id}` | 更新项目 |
| DELETE | `/api/projects/{id}` | 删除项目 |
| GET | `/api/projects/{id}/members` | 获取项目成员 |

### API 响应格式

#### 成功响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "name": "示例数据"
  },
  "timestamp": 1727654400000
}
```

#### 错误响应

```json
{
  "code": 400,
  "message": "参数错误",
  "timestamp": 1727654400000
}
```

#### 分页响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "list": [...],
    "total": 100,
    "page": 1,
    "pageSize": 20,
    "totalPages": 5,
    "hasNext": true,
    "hasPrevious": false
  },
  "timestamp": 1727654400000
}
```

---

## 🧪 测试

### 运行单元测试

```bash
mvn test
```

### 运行集成测试

```bash
mvn verify
```

### 测试覆盖率报告

```bash
mvn clean test jacoco:report
```

查看覆盖率报告: `target/site/jacoco/index.html`

### 测试要求

- **单元测试覆盖率**: ≥ 80%
- **核心业务逻辑覆盖率**: 100%
- **所有 Controller**: 集成测试覆盖

---

## 📊 代码质量

### SonarQube 扫描

```bash
mvn clean verify sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=your_token
```

### Checkstyle 检查

```bash
mvn checkstyle:check
```

### 代码质量标准

| 指标 | 要求 |
|------|------|
| 代码覆盖率 | ≥ 80% |
| 重复代码率 | ≤ 3% |
| 代码复杂度 | ≤ 10 |
| 严重 Bug | 0 个 |

---

## 🔐 安全

### JWT 认证

所有需要认证的 API 都需要在 Header 中携带 JWT Token:

```
Authorization: Bearer your_jwt_token
```

### Token 获取

通过登录接口获取 Token:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

### RBAC 权限控制

系统支持以下角色:

- **SuperAdmin**: 系统超级管理员,拥有所有权限
- **ProjectManager**: 项目经理,管理项目和团队
- **Developer**: 开发人员,读写代码和文档
- **Tester**: 测试人员,管理测试用例
- **UIDesigner**: UI 设计师,管理设计文档
- **DevOps**: 运维人员,管理部署文档
- **Guest**: 访客,只读权限

---

## 🚢 部署

### Docker 部署

#### 构建镜像

```bash
docker build -t promanage-backend:latest .
```

#### 运行容器

```bash
docker run -d \
  --name promanage-backend \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL=jdbc:postgresql://postgres:5432/promanage \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=password \
  -e REDIS_HOST=redis \
  -e REDIS_PORT=6379 \
  promanage-backend:latest
```

### Docker Compose 部署

```bash
docker-compose up -d
```

### Kubernetes 部署

```bash
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
```

---

## 📈 监控

### Spring Boot Actuator

访问 Actuator 端点:

- **健康检查**: http://localhost:8080/actuator/health
- **指标监控**: http://localhost:8080/actuator/metrics
- **应用信息**: http://localhost:8080/actuator/info

### Prometheus & Grafana

系统支持 Prometheus 指标导出,可以通过 Grafana 进行可视化监控。

Prometheus 端点: http://localhost:8080/actuator/prometheus

---

## 📚 开发文档

### 项目文档

- [产品需求文档 (PRD)](../ProManage_prd.md)
- [工程规范文档](../ProManage_engineering_spec.md)
- [系统架构文档](../ProManage_System_Architecture.md)
- [数据库设计文档](../database_design_documentation.md)
- [实施指南](PROJECT_IMPLEMENTATION_GUIDE.md)

### 代码规范

详见 [工程规范文档](../ProManage_engineering_spec.md) 第2章 - 后端工程规范

### 提交规范

遵循 Conventional Commits 规范:

```
<type>(<scope>): <subject>

<body>

<footer>
```

示例:

```
feat(document): 实现文档上传功能

- 支持拖拽上传
- 支持多文件上传
- 添加进度显示

Closes #123
```

---

## 🤝 贡献指南

欢迎贡献代码! 请遵循以下步骤:

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'feat: add amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 提交 Pull Request

### 代码审查清单

- [ ] 代码符合项目规范
- [ ] 添加了必要的单元测试
- [ ] 所有测试通过
- [ ] 添加了 API 文档注解
- [ ] 更新了相关文档

---

## 🐛 问题反馈

如果发现 Bug 或有功能建议,请:

1. 查看 [Issues](https://github.com/yourusername/promanage/issues) 是否已存在
2. 如果没有,创建新的 Issue
3. 提供详细的问题描述和复现步骤

---

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

---

## 👥 团队

**ProManage Team**

- 技术负责人: [待填写]
- 后端开发: [待填写]
- 前端开发: [待填写]
- 测试工程师: [待填写]

---

## 📞 联系我们

- **邮箱**: support@promanage.com
- **文档**: https://docs.promanage.com
- **官网**: https://www.promanage.com

---

<div align="center">

**Made with ❤️ by ProManage Team**

[⬆ 回到顶部](#promanage-backend)

</div>