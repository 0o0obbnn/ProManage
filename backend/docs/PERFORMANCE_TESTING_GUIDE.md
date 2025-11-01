# ProManage 性能测试指南

## 概述

本文档描述了如何对ProManage系统进行性能测试，特别是针对N+1查询重构后的性能验证。

**最后更新**: 2025-11-01  
**版本**: 1.0

---

## 1. 测试目标

### 1.1 性能指标要求

| 指标 | 目标值 |
|------|--------|
| API响应时间 P50 | ≤ 100ms |
| API响应时间 P95 | ≤ 300ms |
| API响应时间 P99 | ≤ 500ms |
| 并发用户数 | ≥ 500 |
| TPS (每秒事务数) | ≥ 1000 |
| 错误率 | ≤ 0.1% |
| SQL查询次数（列表接口） | ≤ 5次 |

### 1.2 重构效果验证

验证N+1查询重构的效果：
- **ChangeRequestController.listChangeRequests**: SQL查询次数应减少≥90%
- **TaskController.listTasks**: SQL查询次数应减少≥90%
- **TestCaseController.listTestCases**: SQL查询次数应减少≥90%

---

## 2. 测试环境准备

### 2.1 测试数据准备

#### 小数据量场景（10条记录）
```sql
-- 准备测试数据
INSERT INTO tb_change_request (project_id, title, requester_id, assignee_id, reviewer_id, ...)
SELECT 1, '测试变更请求' || generate_series, 
       1 + (random() * 10)::int,  -- requester_id
       1 + (random() * 10)::int,  -- assignee_id
       1 + (random() * 10)::int,  -- reviewer_id
       ...
FROM generate_series(1, 10);
```

#### 中数据量场景（100条记录）
```sql
-- 生成100条测试数据
INSERT INTO tb_change_request (project_id, title, requester_id, assignee_id, reviewer_id, ...)
SELECT 1, '测试变更请求' || generate_series, 
       1 + (random() * 20)::int,
       1 + (random() * 20)::int,
       1 + (random() * 20)::int,
       ...
FROM generate_series(1, 100);
```

#### 大数据量场景（1000条记录）
```sql
-- 生成1000条测试数据
INSERT INTO tb_change_request (project_id, title, requester_id, assignee_id, reviewer_id, ...)
SELECT 1, '测试变更请求' || generate_series, 
       1 + (random() * 50)::int,
       1 + (random() * 50)::int,
       1 + (random() * 50)::int,
       ...
FROM generate_series(1, 1000);
```

### 2.2 启用SQL日志

#### 配置MyBatis日志
在 `application.yml` 中添加：
```yaml
logging:
  level:
    com.promanage.service.mapper: DEBUG
    org.apache.ibatis: DEBUG
    java.sql.Connection: DEBUG
    java.sql.Statement: DEBUG
    java.sql.PreparedStatement: DEBUG
```

#### 使用P6Spy监控SQL（推荐）

在 `pom.xml` 中添加依赖：
```xml
<dependency>
    <groupId>p6spy</groupId>
    <artifactId>p6spy</artifactId>
    <version>3.9.1</version>
</dependency>
```

在 `application.yml` 中配置：
```yaml
spring:
  datasource:
    driver-class-name: com.p6spy.engine.spy.P6SpyDriver
    url: jdbc:p6spy:postgresql://localhost:5432/promanage
```

创建 `spy.properties`：
```properties
module.log=com.p6spy.engine.logging.P6LogFactory
appender=com.p6spy.engine.spy.appender.Slf4JLogger
logMessageFormat=com.p6spy.engine.spy.appender.CustomLineFormat
customLogMessageFormat=%(currentTime) | took %(executionTime)ms | %(category) | connection%(connectionId) | %(sqlSingleLine)
```

---

## 3. 性能测试工具

### 3.1 JMeter测试脚本

#### 3.1.1 创建测试计划

测试计划结构：
```
ProManage Performance Test
├── Thread Group (变更请求列表)
│   ├── HTTP Request (GET /api/change-requests)
│   ├── Response Assertion
│   └── Aggregate Report
├── Thread Group (任务列表)
│   ├── HTTP Request (GET /api/tasks)
│   ├── Response Assertion
│   └── Aggregate Report
└── Thread Group (测试用例列表)
    ├── HTTP Request (GET /api/test-cases)
    ├── Response Assertion
    └── Aggregate Report
```

#### 3.1.2 测试场景配置

**场景1: 小数据量（10条记录）**
- 并发用户: 10
- Ramp-up时间: 10秒
- 循环次数: 10次
- 总请求数: 100

**场景2: 中数据量（100条记录）**
- 并发用户: 20
- Ramp-up时间: 20秒
- 循环次数: 10次
- 总请求数: 200

**场景3: 大数据量（1000条记录）**
- 并发用户: 50
- Ramp-up时间: 60秒
- 循环次数: 5次
- 总请求数: 250

### 3.2 Apache Bench (ab) 简单测试

```bash
# 变更请求列表接口
ab -n 1000 -c 50 -H "Authorization: Bearer YOUR_TOKEN" \
   "http://localhost:8080/api/change-requests?projectId=1&page=1&size=20"

# 任务列表接口
ab -n 1000 -c 50 -H "Authorization: Bearer YOUR_TOKEN" \
   "http://localhost:8080/api/tasks?projectId=1&page=1&size=20"
```

### 3.3 Gatling测试脚本（Scala）

```scala
import io.gatling.core.Predef._
import io.gatling.http.Predef._

class ProManagePerformanceTest extends Simulation {
  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .authorizationHeader("Bearer YOUR_TOKEN")

  val changeRequestScenario = scenario("Change Request List")
    .exec(http("listChangeRequests")
      .get("/api/change-requests")
      .queryParam("projectId", "1")
      .queryParam("page", "1")
      .queryParam("size", "20")
      .check(status.is(200)))

  setUp(
    changeRequestScenario.inject(
      rampUsers(50) during (60 seconds)
    )
  ).protocols(httpProtocol)
}
```

---

## 4. SQL查询次数统计

### 4.1 使用P6Spy统计

P6Spy会自动记录每次SQL执行，通过日志可以统计SQL查询次数。

统计脚本示例（Python）：
```python
import re
from collections import defaultdict

def count_sql_queries(log_file):
    query_count = defaultdict(int)
    with open(log_file, 'r') as f:
        for line in f:
            if '| took' in line and '|' in line:
                parts = line.split('|')
                if len(parts) >= 5:
                    sql = parts[-1].strip()
                    # 提取SQL类型
                    sql_type = sql.split()[0] if sql.split() else 'UNKNOWN'
                    query_count[sql_type] += 1
    
    return query_count

# 使用
counts = count_sql_queries('application.log')
print(f"SELECT查询次数: {counts.get('SELECT', 0)}")
print(f"INSERT查询次数: {counts.get('INSERT', 0)}")
```

### 4.2 使用MyBatis Interceptor统计

创建自定义Interceptor：
```java
@Component
public class SqlCountInterceptor implements Interceptor {
    private static final ThreadLocal<Integer> SQL_COUNT = new ThreadLocal<>();
    
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        SQL_COUNT.set(0);
        Object result = invocation.proceed();
        // 获取SQL计数
        return result;
    }
    
    public static int getSqlCount() {
        return SQL_COUNT.get() != null ? SQL_COUNT.get() : 0;
    }
    
    public static void clear() {
        SQL_COUNT.remove();
    }
}
```

---

## 5. 性能指标收集

### 5.1 响应时间指标

#### 使用Spring Boot Actuator
```yaml
management:
  endpoints:
    web:
      exposure:
        include: metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

#### 自定义指标收集
```java
@Component
public class PerformanceMetrics {
    private final MeterRegistry meterRegistry;
    
    @EventListener
    public void handleApiCall(ApiCallEvent event) {
        Timer.builder("api.request.duration")
            .tag("endpoint", event.getEndpoint())
            .tag("method", event.getMethod())
            .register(meterRegistry)
            .record(event.getDuration(), TimeUnit.MILLISECONDS);
    }
}
```

### 5.2 数据库性能指标

#### PostgreSQL查询统计
```sql
-- 查看慢查询
SELECT pid, now() - pg_stat_activity.query_start AS duration, query
FROM pg_stat_activity
WHERE (now() - pg_stat_activity.query_start) > interval '100 milliseconds'
  AND state = 'active';

-- 查看连接数
SELECT count(*) FROM pg_stat_activity;

-- 查看数据库大小
SELECT pg_size_pretty(pg_database_size('promanage'));
```

---

## 6. 测试执行步骤

### 6.1 执行前准备

1. **备份数据库**
   ```bash
   pg_dump -U promanage -d promanage > backup_$(date +%Y%m%d).sql
   ```

2. **清理缓存**
   ```bash
   # Redis
   redis-cli FLUSHDB
   ```

3. **重启应用**
   ```bash
   mvn spring-boot:run
   ```

### 6.2 执行测试

#### 步骤1: 预热
```bash
# 执行少量请求预热JVM
ab -n 10 -c 1 "http://localhost:8080/api/change-requests?projectId=1"
```

#### 步骤2: 执行性能测试
```bash
# 使用JMeter运行测试计划
jmeter -n -t promanage_performance_test.jmx -l results.jtl

# 或使用Gatling
gatling.sh -s ProManagePerformanceTest
```

#### 步骤3: 收集指标
- 应用日志（SQL查询次数）
- JMeter结果文件（响应时间）
- Prometheus指标（系统资源）

---

## 7. 性能测试报告模板

### 7.1 测试结果示例

```
ProManage N+1重构性能测试报告
==============================

测试日期: 2025-11-01
测试环境: 本地开发环境
数据库: PostgreSQL 15
应用版本: 1.0.0-SNAPSHOT

一、ChangeRequestController.listChangeRequests
-----------------------------------------------
数据量: 100条
并发用户: 20

重构前:
- SQL查询次数: 103次
- P50响应时间: 320ms
- P95响应时间: 450ms
- P99响应时间: 680ms
- 数据库CPU: 45%

重构后:
- SQL查询次数: 2次 (-98%)
- P50响应时间: 85ms (-73%)
- P95响应时间: 180ms (-60%)
- P99响应时间: 320ms (-53%)
- 数据库CPU: 15% (-67%)

二、TaskController.listTasks
---------------------------
数据量: 100条
并发用户: 20

重构前:
- SQL查询次数: 205次
- P95响应时间: 680ms
- 数据库CPU: 52%

重构后:
- SQL查询次数: 3次 (-98.5%)
- P95响应时间: 250ms (-63%)
- 数据库CPU: 18% (-65%)

三、总结
-------
✅ SQL查询次数减少: 平均98%
✅ 响应时间改善: 平均60%
✅ 数据库负载降低: 平均65%
✅ 所有指标达到目标值
```

---

## 8. 持续监控

### 8.1 Prometheus告警规则

```yaml
groups:
  - name: promanage_performance
    rules:
      # SQL查询次数告警
      - alert: HighSQLQueryCount
        expr: prometheus_sql_query_count_total > 5
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "API接口SQL查询次数过多"
          description: "{{ $labels.endpoint }} SQL查询次数: {{ $value }}"

      # 响应时间告警
      - alert: HighAPIResponseTime
        expr: histogram_quantile(0.95, rate(api_request_duration_seconds_bucket[5m])) > 0.3
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "API响应时间过高"
          description: "{{ $labels.endpoint }} P95响应时间: {{ $value }}s"
```

---

## 9. 参考资源

- [JMeter官方文档](https://jmeter.apache.org/usermanual/)
- [Gatling官方文档](https://gatling.io/docs/)
- [P6Spy文档](https://p6spy.readthedocs.io/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

---

## 附录

### A. 测试数据生成脚本

参见 `scripts/generate_test_data.sql`

### B. JMeter测试计划文件

参见 `scripts/promanage_performance_test.jmx`

### C. Gatling测试脚本

参见 `scripts/ProManagePerformanceTest.scala`


