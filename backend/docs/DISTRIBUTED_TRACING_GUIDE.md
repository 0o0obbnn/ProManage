# ProManage 分布式追踪使用指南

## 概述

ProManage项目集成了完整的分布式追踪解决方案，使用Micrometer Tracing与Zipkin集成，实现请求链路追踪、性能监控和故障排查功能。

## 技术栈

- **Micrometer Tracing**: 分布式追踪核心框架
- **OpenTelemetry**: 追踪数据导出
- **Zipkin**: 追踪数据收集和可视化
- **Brave**: Zipkin Java客户端
- **Spring Boot Actuator**: 指标暴露
- **Prometheus**: 指标收集
- **Grafana**: 指标可视化

## 快速开始

### 1. 启动追踪服务

使用提供的脚本启动Zipkin、Prometheus和Grafana：

```bash
# Linux/Mac
./monitoring.sh start

# Windows
powershell -File monitoring.ps1 start
```

### 2. 访问追踪控制台

服务启动后，可以通过以下地址访问：

- **Zipkin追踪控制台**: http://localhost:9411
- **Prometheus指标控制台**: http://localhost:9090
- **Grafana可视化控制台**: http://localhost:3000 (admin/admin)

### 3. 配置应用

在`application.yml`中配置追踪参数：

```yaml
promanage:
  tracing:
    enabled: true
    zipkin:
      endpoint: http://localhost:9411/api/v2/spans
    sample-rate: 0.1  # 10%的采样率
    service-name: promanage-api
    log-spans: false
```

## 使用方法

### 1. 使用@Traceable注解

在业务方法上添加`@Traceable`注解，自动创建Span：

```java
@Service
public class ProjectService {
    
    @Traceable(value = "createProject", tags = {"request", "creatorId"})
    public Project createProject(CreateProjectRequestDTO request, Long creatorId) {
        // 业务逻辑
        return project;
    }
}
```

### 2. 手动创建和管理Span

使用`TracingUtil`手动创建Span：

```java
@Service
public class ComplexService {
    
    private final TracingUtil tracingUtil;
    
    public void complexOperation(Long projectId) {
        Map<String, String> tags = Map.of(
            "projectId", String.valueOf(projectId),
            "operation", "complex-analysis"
        );
        
        tracingUtil.runInSpan("complexOperation", tags, () -> {
            // 步骤1：数据验证
            validateData();
            
            // 步骤2：数据处理
            processData();
            
            // 步骤3：结果验证
            validateResults();
            
            return null;
        });
    }
}
```

### 3. 记录事件和标签

在Span中记录事件和标签：

```java
// 记录事件
tracingUtil.recordEvent("Data processing started");

// 添加标签
tracingUtil.addTag("user.id", userId);
tracingUtil.addTag("tenant.id", tenantId);
tracingUtil.addTag("data.size", String.valueOf(dataSize));

// 记录异常
try {
    // 业务逻辑
} catch (Exception e) {
    tracingUtil.recordException(e);
    throw e;
}
```

### 4. 获取追踪信息

获取当前追踪上下文信息：

```java
// 获取Trace ID
String traceId = tracingUtil.getCurrentTraceId();

// 获取Span ID
String spanId = tracingUtil.getCurrentSpanId();

// 检查是否有活跃Span
boolean hasSpan = tracingUtil.hasActiveSpan();
```

## 追踪数据

### 1. HTTP请求追踪

系统自动追踪所有HTTP请求，包括：

- 请求方法、URL、状态码
- 请求头信息（用户ID、租户ID等）
- 响应时间和大小
- 异常信息

### 2. 数据库操作追踪

数据库操作自动被追踪，包括：

- SQL查询执行时间
- 数据库连接信息
- 查询参数和结果数量

### 3. 业务方法追踪

使用`@Traceable`注解的方法会被追踪，包括：

- 方法执行时间
- 方法参数和返回值
- 异常信息
- 自定义标签和事件

## 性能优化

### 1. 采样率配置

在高并发场景下，可以通过调整采样率来减少追踪开销：

```yaml
promanage:
  tracing:
    sample-rate: 0.01  # 1%采样率
```

### 2. 选择性追踪

只对关键业务方法进行追踪：

```java
// 只对关键方法进行追踪
@Traceable(value = "criticalOperation", tags = {"critical", "high-priority"})
public void criticalOperation() {
    // 关键业务逻辑
}
```

### 3. 异步追踪

对于异步操作，确保在正确的线程上下文中进行追踪：

```java
@Async
@Traceable(value = "asyncOperation")
public CompletableFuture<Void> asyncOperation() {
    // 异步操作会自动继承父Span的上下文
    return CompletableFuture.completedFuture(null);
}
```

## 故障排查

### 1. 追踪数据未显示

- 检查Zipkin服务是否正常运行
- 确认应用配置中的Zipkin端点地址正确
- 检查网络连接是否畅通
- 查看应用日志中的追踪相关错误

### 2. 性能问题

- 降低采样率以减少追踪开销
- 减少追踪的标签和事件数量
- 只在关键路径上启用追踪

### 3. 数据不一致

- 确保所有服务使用相同的追踪配置
- 检查时间同步是否准确
- 验证跨服务调用的追踪上下文传递

## 最佳实践

### 1. 命名规范

- Span名称应该简洁明了，描述操作内容
- 标签键应该使用小写字母和下划线
- 事件名称应该使用动词短语

### 2. 数据安全

- 避免在追踪中记录敏感信息（密码、密钥等）
- 对用户ID等个人信息进行脱敏处理
- 遵守数据保护法规要求

### 3. 性能考虑

- 在高并发场景下使用适当的采样率
- 避免在循环中创建大量Span
- 及时结束Span以避免内存泄漏

### 4. 监控和告警

- 设置追踪数据的健康检查
- 配置异常追踪的告警规则
- 定期分析追踪数据以发现性能瓶颈

## 集成测试

### 1. 启动测试环境

```bash
# 启动所有服务
docker-compose up -d

# 启动监控服务
./monitoring.sh start
```

### 2. 执行测试请求

```bash
# 创建项目（会被自动追踪）
curl -X POST http://localhost:8080/api/v1/projects \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Project","code":"TEST001","description":"Test Description"}'
```

### 3. 查看追踪结果

1. 访问Zipkin控制台：http://localhost:9411
2. 搜索最近的追踪记录
3. 查看请求链路和时间分布
4. 分析性能瓶颈和异常

## 常见问题

### Q1: 追踪数据太多怎么办？
A: 可以通过以下方式减少数据量：
- 降低采样率
- 减少追踪的标签数量
- 只在关键路径上启用追踪

### Q2: 追踪影响性能怎么办？
A: 可以通过以下方式优化性能：
- 使用异步追踪
- 调整采样策略
- 优化追踪配置

### Q3: 跨服务追踪不工作怎么办？
A: 检查以下配置：
- 确保所有服务使用相同的追踪框架
- 检查HTTP头信息传递是否正确
- 验证网络连接是否畅通

## 相关文档

- [Micrometer Tracing官方文档](https://micrometer.io/docs/tracing)
- [Zipkin官方文档](https://zipkin.io/pages/documentation.html)
- [Spring Boot Actuator文档](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [OpenTelemetry文档](https://opentelemetry.io/docs/)