# ProManage 日志规范文档

## 概述

本文档定义了ProManage系统的日志记录标准，确保日志的质量、安全性和可维护性。

**最后更新**: 2025-11-01  
**版本**: 1.0

---

## 1. 日志级别使用指南

### 1.1 DEBUG（调试级别）

**用途**: 详细的调试信息，通常只在开发和调试时启用。

**使用场景**:
- 详细的执行流程追踪
- 方法参数和返回值记录
- 数据库查询详情
- 性能关键操作的中间步骤

**示例**:
```java
// ✅ 推荐
log.debug("查询用户详情, userId={}", userId);
log.debug("批量查询用户, ids={}", ids);
log.debug("收到会话 {} 的消息: {}", sessionId, payload);

// ❌ 避免
log.debug("查询用户详情成功"); // 信息不够详细
```

**生产环境**: 默认关闭，仅在需要调试时临时启用。

### 1.2 INFO（信息级别）

**用途**: 记录关键业务流程和状态变化，生产环境默认开启。

**使用场景**:
- 用户操作的关键步骤（登录、注册、创建资源等）
- 业务对象的状态变更（创建、更新、删除）
- 系统配置初始化完成
- 重要业务指标

**示例**:
```java
// ✅ 推荐
log.info("用户登录成功, username={}, userId={}", username, userId);
log.info("任务创建成功, taskId={}, title={}", taskId, request.getTitle());
log.info("变更请求审批完成, id={}, decision={}", changeRequestId, decision);

// ❌ 避免
log.info("用户ID: {}, 用户名: {}, 邮箱: {}", userId, username, email); 
// 包含过多详细信息，应该使用DEBUG
```

**生产环境**: 默认开启，用于监控业务流程。

### 1.3 WARN（警告级别）

**用途**: 记录潜在问题和非关键错误，不影响应用程序正常运行。

**使用场景**:
- 业务规则违反（但已处理）
- 降级处理
- 预期外的业务状态
- 配置问题

**示例**:
```java
// ✅ 推荐
log.warn("用户不存在, username={}", username);
log.warn("密码错误, username={}", username);
log.warn("无法从会话中获取用户ID, 关闭连接");
log.warn("发送心跳消息失败, 会话ID: {}", sessionId);

// ❌ 避免
log.warn("处理失败"); // 信息不够具体
```

**生产环境**: 默认开启，需要监控但不需要立即处理。

### 1.4 ERROR（错误级别）

**用途**: 记录严重错误，需要人工干预或告警。

**使用场景**:
- 异常情况导致业务操作失败
- 系统错误（数据库连接失败、外部服务调用失败等）
- 安全相关错误
- 数据完整性错误

**示例**:
```java
// ✅ 推荐
log.error("用户创建失败, username={}", username, e);
log.error("处理WebSocket消息失败, 会话ID: {}", sessionId, e);
log.error("批量更新任务失败, taskId={}, field={}", taskId, field, e);

// ❌ 避免
log.error("操作失败"); // 缺少上下文和异常信息
log.error("操作失败", e); // 缺少具体错误描述
```

**生产环境**: 默认开启，需要立即关注和告警。

---

## 2. 敏感信息处理规范

### 2.1 禁止在日志中记录的信息

**绝对禁止**:
- 明文密码
- 完整Token（JWT、OAuth等）
- API密钥
- 私钥
- 信用卡号
- 社会安全号（SSN）
- 银行账户信息

### 2.2 敏感信息脱敏规则

**密码**:
```java
// ❌ 绝对禁止
log.info("用户登录, password={}", password);

// ✅ 推荐
log.info("用户登录, username={}", username);
// 或完全不记录
```

**Token**:
```java
// ❌ 绝对禁止
log.debug("Token: {}", token);

// ✅ 推荐
log.debug("Token: {}...{}", token.substring(0, 10), token.substring(token.length() - 5));
// 或只记录Token是否存在
log.debug("Token验证, isValid={}", isTokenValid(token));
```

**用户ID和基本信息**:
```java
// ✅ 允许（用户ID是安全的）
log.info("用户操作, userId={}", userId);

// ❌ 避免（邮箱可能涉及隐私）
log.info("用户详情, email={}", email);
```

### 2.3 敏感信息检查清单

在代码审查时，检查以下关键词：
- `password`, `Password`, `PASSWORD`
- `token`, `Token`, `TOKEN`
- `secret`, `Secret`, `SECRET`
- `apiKey`, `apikey`, `API_KEY`
- `creditCard`, `credit_card`
- `ssn`, `SSN`
- `privateKey`, `private_key`

---

## 3. 日志格式标准

### 3.1 日志消息格式

**基本格式**:
```
[级别] 操作描述, 关键参数1={}, 关键参数2={}
```

**示例**:
```java
// ✅ 推荐
log.info("用户登录成功, username={}, userId={}", username, userId);
log.error("处理WebSocket消息失败, 会话ID: {}, 错误: {}", sessionId, e.getMessage(), e);

// ❌ 避免
log.info("用户登录成功"); // 缺少关键信息
log.info("用户 " + username + " 登录成功"); // 字符串拼接，性能差
```

### 3.2 异常日志格式

**必须包含**:
1. 操作描述
2. 关键参数（如ID、标识符）
3. 异常对象（用于堆栈跟踪）

**示例**:
```java
// ✅ 推荐
log.error("创建用户失败, username={}", username, e);
log.error("批量更新任务失败, taskId={}, field={}", taskId, field, e);

// ❌ 避免
log.error("创建用户失败", e); // 缺少关键参数
log.error("创建用户失败, username={}", username); // 缺少异常对象
```

### 3.3 结构化日志（推荐）

对于需要日志分析的场景，考虑使用结构化日志格式（JSON）：

```java
// 使用SLF4J的Marker和结构化日志
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

Marker marker = MarkerFactory.getMarker("USER_LOGIN");
log.info(marker, "用户登录, username={}, userId={}, ip={}", username, userId, ip);
```

---

## 4. 日志最佳实践

### 4.1 使用占位符

**推荐**:
```java
log.info("用户登录成功, username={}, userId={}", username, userId);
```

**避免**:
```java
log.info("用户登录成功, username=" + username + ", userId=" + userId);
// 问题: 即使日志级别关闭也会执行字符串拼接
```

### 4.2 避免过多的日志

**原则**: 
- 避免在循环中记录日志（除非是关键错误）
- 避免在频繁调用的方法中记录DEBUG日志

**示例**:
```java
// ❌ 避免
for (User user : users) {
    log.debug("处理用户, userId={}", user.getId()); // 如果users很大，会产生大量日志
}

// ✅ 推荐
log.debug("批量处理用户, count={}", users.size());
if (log.isDebugEnabled()) {
    for (User user : users) {
        // 只在需要时记录详细信息
    }
}
```

### 4.3 包含足够的上下文

**日志消息应该回答**:
- 发生了什么？（操作描述）
- 涉及哪些关键对象？（ID、标识符）
- 结果如何？（成功/失败）
- 失败原因？（异常信息）

**示例**:
```java
// ✅ 推荐
log.info("变更请求创建成功, changeRequestId={}, title={}, projectId={}", 
    changeRequestId, request.getTitle(), projectId);

// ❌ 避免
log.info("变更请求创建成功"); // 缺少关键信息
```

---

## 5. 日志配置文件

### 5.1 Logback配置示例

```xml
<!-- logback-spring.xml -->
<configuration>
    <!-- 控制台输出 -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- 文件输出 -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/promanage.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/promanage.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- ERROR日志单独文件 -->
    <appender name="ERROR_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/error.log</file>
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>ERROR</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/error.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- 生产环境: INFO级别 -->
    <springProfile name="prod">
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
            <appender-ref ref="FILE"/>
            <appender-ref ref="ERROR_FILE"/>
        </root>
    </springProfile>

    <!-- 开发环境: DEBUG级别 -->
    <springProfile name="dev">
        <root level="DEBUG">
            <appender-ref ref="CONSOLE"/>
            <appender-ref ref="FILE"/>
        </root>
    </springProfile>
</configuration>
```

### 5.2 日志级别配置建议

| 环境 | 默认级别 | DEBUG | INFO | WARN | ERROR |
|------|---------|-------|------|------|-------|
| **开发** | DEBUG | ✅ | ✅ | ✅ | ✅ |
| **测试** | INFO | ❌ | ✅ | ✅ | ✅ |
| **生产** | INFO | ❌ | ✅ | ✅ | ✅ |

---

## 6. 日志审查清单

### 6.1 代码审查检查项

- [ ] 日志级别是否合适？
- [ ] 日志消息是否清晰、具体？
- [ ] 是否包含足够的上下文信息？
- [ ] 异常日志是否包含异常对象？
- [ ] 是否包含敏感信息？
- [ ] 是否使用了占位符而不是字符串拼接？
- [ ] 循环中是否有过多日志？

### 6.2 定期审查

**建议**: 每个迭代周期结束时，审查日志输出：
1. 统计各日志级别的使用情况
2. 检查是否有敏感信息泄露
3. 优化冗余或低价值的日志
4. 确保关键业务操作都有日志记录

---

## 7. 常见问题和解决方案

### 7.1 问题：日志过多导致性能问题

**解决方案**:
- 减少DEBUG日志在生产环境的输出
- 避免在频繁调用的方法中记录详细日志
- 使用条件日志：`if (log.isDebugEnabled())`

### 7.2 问题：日志信息不足，难以排查问题

**解决方案**:
- 确保日志包含操作描述和关键参数
- 异常日志必须包含完整的堆栈信息
- 使用关联ID（如requestId）追踪请求流程

### 7.3 问题：敏感信息泄露

**解决方案**:
- 建立代码审查流程，检查敏感信息关键词
- 使用自动化工具扫描日志代码
- 对敏感字段进行脱敏处理

---

## 8. 附录

### A. 日志关键字搜索命令

```bash
# 搜索密码相关日志
grep -r "password" backend/ --include="*.java" -i | grep "log\."

# 搜索Token相关日志
grep -r "token" backend/ --include="*.java" -i | grep "log\."

# 统计日志级别使用情况
grep -r "log\.\(debug\|info\|warn\|error\)" backend/ --include="*.java" | \
  sed 's/.*log\.\(debug\|info\|warn\|error\).*/\1/' | sort | uniq -c
```

### B. 日志级别决策树

```
是否需要记录？
├─ 是
│  ├─ 是否包含敏感信息？
│  │  ├─ 是 → 脱敏处理或移除
│  │  └─ 否 → 继续
│  ├─ 是否需要立即关注？
│  │  ├─ 是 → ERROR
│  │  └─ 否 → 继续
│  ├─ 是否是预期外但已处理？
│  │  ├─ 是 → WARN
│  │  └─ 否 → 继续
│  ├─ 是否是关键业务流程？
│  │  ├─ 是 → INFO
│  │  └─ 否 → DEBUG
│  └─ 是否为详细调试信息？
│     ├─ 是 → DEBUG
│     └─ 否 → INFO
└─ 否 → 不记录
```

---

**文档维护**: 本规范应根据项目实际情况和团队反馈持续更新。

