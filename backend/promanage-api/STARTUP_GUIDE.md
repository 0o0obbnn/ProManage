# ProManage Backend - 启动指南

## 📋 启动前检查清单

### 1. 环境要求
- ✅ **Java 21** - 已安装 (检测到: Java 21.0.1)
- ✅ **Maven 3.9+** - 已安装 (检测到: Maven 3.9.9)
- ⚠️ **PostgreSQL 15+** - 需要运行在 `192.168.18.7:5432`
- ⚠️ **Redis** - 需要运行在 `192.168.18.7:6379`

### 2. 数据库配置
确保 PostgreSQL 数据库已创建并可访问：
```sql
-- 数据库名称: promanage
-- 用户名: postgres
-- 密码: postgres
-- 主机: 192.168.18.7
-- 端口: 5432
```

### 3. Redis 配置
确保 Redis 服务正在运行：
```
主机: 192.168.18.7
端口: 6379
密码: (无)
```

---

## 🚀 启动方式

### 方式一：使用批处理脚本（推荐）

**Windows 批处理文件：**
```bash
# 双击运行或在命令行中执行
.\start-app.bat
```

**PowerShell 脚本：**
```powershell
# 在 PowerShell 中执行
.\start-app.ps1
```

### 方式二：使用 Maven 命令

**在 `backend/promanage-api` 目录下执行：**

```bash
# 使用开发环境配置启动
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 或者使用默认配置（也是 dev）
mvn spring-boot:run
```

### 方式三：先编译后运行

```bash
# 1. 编译整个项目
cd G:\nifa\ProManage\backend
mvn clean install -DskipTests

# 2. 运行应用
cd promanage-api
mvn spring-boot:run
```

---

## 📊 启动状态监控

### 预期启动日志

应用启动时，您应该看到以下关键日志信息：

```
================================================
   _____           __  __                            
  |  __ \         |  \/  |                           
  | |__) | __ ___ | \  / | __ _ _ __   __ _  __ _  ___ 
  |  ___/ '__/ _ \| |\/| |/ _` | '_ \ / _` |/ _` |/ _ \
  | |   | | | (_) | |  | | (_| | | | | (_| | (_| |  __/
  |_|   |_|  \___/|_|  |_|\__,_|_| |_|\__,_|\__, |\___|
                                             __/ |     
                                            |___/      
   Project & Document Management System
   Version: 1.0.0-SNAPSHOT
   Spring Boot: 3.2.5
================================================

✅ JWT configuration validated successfully
✅ MyBatis Plus interceptor initialized successfully
✅ DruidDataSource inited
✅ Spring Security filter chain configured successfully
✅ Tomcat started on port(s): 8080 (http)
✅ Started ProManageApplication in X.XXX seconds
```

### 启动成功标志

当您看到以下信息时，表示应用已成功启动：

```
Started ProManageApplication in X.XXX seconds (JVM running for X.XXX)
```

---

## 🔍 验证应用状态

### 1. 健康检查端点

**访问健康检查 API：**
```bash
# 使用浏览器访问
http://localhost:8080/actuator/health

# 或使用 curl
curl http://localhost:8080/actuator/health
```

**预期响应：**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "diskSpace": {
      "status": "UP"
    },
    "ping": {
      "status": "UP"
    },
    "redis": {
      "status": "UP"
    }
  }
}
```

### 2. Swagger UI 文档

**访问 API 文档：**
```
http://localhost:8080/swagger-ui.html
```

您应该能看到完整的 API 文档界面，包括：
- Authentication APIs (认证接口)
- User Management APIs (用户管理接口)
- Project Management APIs (项目管理接口)
- Document Management APIs (文档管理接口)
- System Management APIs (系统管理接口)

### 3. Druid 监控面板

**访问数据库连接池监控：**
```
http://localhost:8080/druid/index.html

用户名: admin
密码: admin
```

---

## ⚠️ 常见问题排查

### 问题 1: 端口 8080 被占用

**错误信息：**
```
Web server failed to start. Port 8080 was already in use.
```

**解决方案：**

**方法 A - 停止占用端口的进程：**
```powershell
# 1. 查找占用端口的进程
netstat -ano | findstr :8080

# 2. 记下 PID（最后一列的数字）

# 3. 终止该进程
taskkill /PID <PID> /F
```

**方法 B - 更改应用端口：**
在 `application-dev.yml` 中修改：
```yaml
server:
  port: 8081  # 改为其他端口
```

### 问题 2: 数据库连接失败

**错误信息：**
```
Failed to obtain JDBC Connection
Connection refused: connect
```

**检查清单：**
1. ✅ PostgreSQL 服务是否正在运行？
2. ✅ 数据库 `promanage` 是否已创建？
3. ✅ 主机地址 `192.168.18.7` 是否可访问？
4. ✅ 用户名和密码是否正确？

**测试数据库连接：**
```bash
# 使用 psql 测试连接
psql -h 192.168.18.7 -p 5432 -U postgres -d promanage
```

**解决方案：**
- 启动 PostgreSQL 服务
- 创建数据库：`CREATE DATABASE promanage;`
- 检查防火墙设置
- 验证 `application-dev.yml` 中的数据库配置

### 问题 3: Redis 连接失败

**错误信息：**
```
Unable to connect to Redis
Connection refused
```

**检查清单：**
1. ✅ Redis 服务是否正在运行？
2. ✅ 主机地址 `192.168.18.7` 是否可访问？
3. ✅ 端口 6379 是否开放？

**测试 Redis 连接：**
```bash
# 使用 redis-cli 测试
redis-cli -h 192.168.18.7 -p 6379 ping
# 应该返回: PONG
```

**解决方案：**
- 启动 Redis 服务
- 检查防火墙设置
- 验证 `application-dev.yml` 中的 Redis 配置

### 问题 4: Flyway 迁移失败

**错误信息：**
```
FlywayException: Unable to find migration scripts
```

**原因：**
- 缺少数据库迁移脚本文件

**解决方案：**
1. 检查 `src/main/resources/db/migration` 目录是否存在
2. 如果不存在迁移文件，可以临时禁用 Flyway：

在 `application-dev.yml` 中设置：
```yaml
spring:
  flyway:
    enabled: false  # 临时禁用
```

### 问题 5: Maven 依赖下载失败

**错误信息：**
```
Could not resolve dependencies
Failed to read artifact descriptor
```

**解决方案：**
```bash
# 清理并重新下载依赖
mvn clean install -U

# 或者删除本地仓库缓存
rm -rf ~/.m2/repository/com/promanage
mvn clean install
```

---

## 📝 启动日志位置

应用运行时会生成以下日志文件：

```
backend/promanage-api/logs/
├── promanage.log              # 主日志文件
├── promanage-error.log        # 错误日志
├── promanage-sql.log          # SQL 日志
└── promanage-YYYY-MM-DD.*.log # 按日期归档的日志
```

---

## 🎯 启动成功后的下一步

### 1. 测试认证接口

```bash
# 注册新用户
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test@123456",
    "fullName": "Test User"
  }'

# 登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test@123456"
  }'
```

### 2. 访问 Swagger UI

打开浏览器访问：
```
http://localhost:8080/swagger-ui.html
```

在 Swagger UI 中可以：
- 查看所有 API 接口
- 在线测试 API
- 查看请求/响应示例

### 3. 监控应用状态

访问 Actuator 端点：
```
http://localhost:8080/actuator/health    # 健康检查
http://localhost:8080/actuator/info      # 应用信息
http://localhost:8080/actuator/metrics   # 性能指标
```

---

## 🛑 停止应用

### 方式一：在运行窗口中
按 `Ctrl + C` 停止应用

### 方式二：使用命令
```bash
# 查找 Java 进程
jps -l | findstr ProManageApplication

# 终止进程
taskkill /PID <PID> /F
```

---

## 📞 获取帮助

如果遇到问题：

1. **查看日志文件**：`logs/promanage-error.log`
2. **检查配置文件**：`application-dev.yml`
3. **参考文档**：
   - `README.md` - 项目概述
   - `PROJECT_IMPLEMENTATION_GUIDE.md` - 实现指南
   - `PROJECT_STATUS.md` - 项目状态

---

**最后更新**: 2025-10-03
**版本**: 1.0.0-SNAPSHOT

