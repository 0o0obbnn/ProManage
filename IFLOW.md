# ProManage 项目系统上下文

## 项目概述

ProManage 是一个智能、集成的项目管理系统，旨在通过中央化文档管理、自动化变更通知和可复用测试用例库，提升团队协作效率。系统采用前后端分离的现代化技术架构，提供统一知识库、智能变更管理、权限管理、个性化工作台等核心功能。

### 核心价值主张

- **统一可信源**：消除文档孤岛，提供单一信息源
- **智能协作**：自动化变更影响分析和通知
- **效率提升**：测试用例复用率目标70%+
- **角色适配**：为7类用户提供个性化工作界面

### 技术架构

#### 前端技术栈
- 主框架：Vue 3 + TypeScript
- 状态管理：Pinia
- UI组件库：Ant Design Vue 4.2+
- 构建工具：Vite
- 测试框架：Vitest

#### 后端技术栈
- 主框架：Spring Boot 3.2.10 (Java 21)
- 数据库：PostgreSQL + Elasticsearch
- 缓存：Redis
- 消息队列：RabbitMQ
- 文件存储：MinIO + AWS S3
- 安全认证：JWT + Spring Security

## 项目结构

```
ProManage/
├── backend/                 # 后端服务
│   ├── promanage-common/    # 公共模块
│   ├── promanage-dto/       # 数据传输对象
│   ├── promanage-domain/    # 领域模型
│   ├── promanage-infrastructure/ # 基础设施
│   ├── promanage-service/   # 业务服务
│   └── promanage-api/       # API接口
├── frontend/                # 前端应用
│   ├── src/
│   │   ├── api/            # API接口
│   │   ├── components/     # 组件库
│   │   ├── views/          # 页面视图
│   │   ├── stores/         # 状态管理
│   │   └── router/         # 路由配置
└── docs/                    # 文档资料
```

## 开发环境搭建

### 后端环境要求
- JDK 21
- Maven 3.9+
- PostgreSQL 15+
- Redis 7.0+
- Elasticsearch 8.11.0

### 前端环境要求
- Node.js 18+
- npm 9+

## 构建和运行

### 后端构建运行

1. 安装依赖并构建：
```bash
cd backend
mvn clean install
```

2. 运行应用：
```bash
cd promanage-api
mvn spring-boot:run
# 或者使用 JAR 文件运行
java -jar promanage-api/target/promanage-api-1.0.0-SNAPSHOT.jar
```

3. 访问服务：
- 应用首页: http://localhost:8080
- API文档: http://localhost:8080/swagger-ui.html
- 健康检查: http://localhost:8080/actuator/health

### 前端构建运行

1. 安装依赖：
```bash
cd frontend
npm install
```

2. 开发模式运行：
```bash
npm run dev
```
访问 http://localhost:3000

3. 构建生产版本：
```bash
npm run build
```

## 测试

### 后端测试
```bash
# 运行单元测试
mvn test

# 运行集成测试
mvn verify

# 生成测试覆盖率报告
mvn clean test jacoco:report
```

### 前端测试
```bash
# 运行测试
npm run test

# 测试覆盖率
npm run test:coverage

# 类型检查
npm run type-check
```

## API 文档

系统使用 Swagger 提供 API 文档，启动后访问：
http://localhost:8080/swagger-ui.html

核心 API 包括：
- 认证相关：登录、注册、Token刷新
- 用户管理：用户增删改查
- 文档管理：文档上传、版本控制、检索
- 项目管理：项目创建、成员管理
- 变更管理：变更请求、影响分析

## 部署

### Docker 部署
```bash
# 构建镜像
docker build -t promanage-backend:latest .

# 运行容器
docker run -d --name promanage-backend -p 8080:8080 promanage-backend:latest
```

### Docker Compose 部署
```bash
docker-compose up -d
```

## 开发规范

### 代码质量
- 后端使用 Checkstyle、SpotBugs、PMD 进行代码质量检查
- 前端使用 ESLint、Prettier 进行代码规范
- 单元测试覆盖率要求 ≥ 70%
- 遵循 Conventional Commits 提交规范

### 安全实践
- JWT Token 认证
- RBAC 权限控制
- 数据加密存储
- XSS/CSRF 防护
- 请求去重机制

## 监控和运维

系统集成 Spring Boot Actuator 进行健康监控：
- 健康检查: http://localhost:8080/actuator/health
- 指标监控: http://localhost:8080/actuator/metrics
- Prometheus 导出: http://localhost:8080/actuator/prometheus

## 相关文档

- 产品需求文档: ProManage_prd.md
- 系统架构文档: ProManage_System_Architecture.md
- 数据库设计文档: database_design_documentation.md
- 工程规范文档: ProManage_engineering_spec.md