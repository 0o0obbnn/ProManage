# ProManage 监控告警配置

## 概述

本文档描述了ProManage系统的监控和告警配置，用于及时发现性能问题和系统异常。

**最后更新**: 2025-11-01  
**版本**: 1.0

---

## 1. 监控架构

```
应用层 (Spring Boot Actuator)
    ↓
指标收集 (Micrometer)
    ↓
Prometheus (指标存储)
    ↓
Grafana (可视化) + Alertmanager (告警)
```

---

## 2. Prometheus配置

### 2.1 prometheus.yml

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  # Spring Boot应用
  - job_name: 'promanage-api'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
        labels:
          application: 'promanage-api'
          environment: 'production'

  # PostgreSQL
  - job_name: 'postgresql'
    static_configs:
      - targets: ['localhost:9187']

  # Redis
  - job_name: 'redis'
    static_configs:
      - targets: ['localhost:9121']
```

### 2.2 告警规则 (prometheus_rules.yml)

```yaml
groups:
  - name: promanage_performance
    interval: 30s
    rules:
      # SQL查询次数告警
      - alert: HighSQLQueryCount
        expr: |
          sum(increase(promanage_sql_query_count_total[5m])) by (endpoint) > 5
        for: 5m
        labels:
          severity: warning
          team: backend
        annotations:
          summary: "API接口SQL查询次数过多"
          description: |
            {{ $labels.endpoint }} 在过去5分钟内平均SQL查询次数: {{ $value }}
            建议检查是否存在N+1查询问题。

      # API响应时间P95告警
      - alert: HighAPILatency
        expr: |
          histogram_quantile(0.95, 
            sum(rate(http_server_requests_seconds_bucket{uri=~"/api/.*"}[5m])) by (le, uri)
          ) > 0.3
        for: 5m
        labels:
          severity: warning
          team: backend
        annotations:
          summary: "API响应时间P95超过阈值"
          description: |
            {{ $labels.uri }} P95响应时间: {{ $value }}s (目标: <0.3s)

      # API响应时间P99告警
      - alert: CriticalAPILatency
        expr: |
          histogram_quantile(0.99, 
            sum(rate(http_server_requests_seconds_bucket{uri=~"/api/.*"}[5m])) by (le, uri)
          ) > 0.5
        for: 3m
        labels:
          severity: critical
          team: backend
        annotations:
          summary: "API响应时间P99超过阈值"
          description: |
            {{ $labels.uri }} P99响应时间: {{ $value }}s (目标: <0.5s)
            需要立即调查。

  - name: promanage_errors
    interval: 30s
    rules:
      # 错误率告警
      - alert: HighErrorRate
        expr: |
          sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) by (uri)
          /
          sum(rate(http_server_requests_seconds_count[5m])) by (uri)
          > 0.01
        for: 5m
        labels:
          severity: critical
          team: backend
        annotations:
          summary: "API错误率过高"
          description: |
            {{ $labels.uri }} 错误率: {{ $value | humanizePercentage }}
            目标: <0.1%

      # 4xx错误率告警
      - alert: HighClientErrorRate
        expr: |
          sum(rate(http_server_requests_seconds_count{status=~"4.."}[5m])) by (uri)
          /
          sum(rate(http_server_requests_seconds_count[5m])) by (uri)
          > 0.05
        for: 10m
        labels:
          severity: warning
          team: backend
        annotations:
          summary: "API 4xx错误率过高"
          description: |
            {{ $labels.uri }} 4xx错误率: {{ $value | humanizePercentage }}
            可能是客户端请求参数问题。

  - name: promanage_resources
    interval: 30s
    rules:
      # CPU使用率告警
      - alert: HighCPUUsage
        expr: |
          (1 - avg(rate(process_cpu_seconds_total[5m]))) * 100 > 80
        for: 10m
        labels:
          severity: warning
          team: infrastructure
        annotations:
          summary: "应用CPU使用率过高"
          description: |
            CPU使用率: {{ $value }}% (目标: <70%)

      # 内存使用率告警
      - alert: HighMemoryUsage
        expr: |
          (jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) * 100 > 80
        for: 10m
        labels:
          severity: warning
          team: infrastructure
        annotations:
          summary: "应用内存使用率过高"
          description: |
            堆内存使用率: {{ $value }}% (目标: <80%)

      # 数据库连接数告警
      - alert: HighDatabaseConnections
        expr: |
          pg_stat_database_numbackends > 80
        for: 5m
        labels:
          severity: warning
          team: database
        annotations:
          summary: "数据库连接数过高"
          description: |
            当前连接数: {{ $value }} (目标: <80)

      # 慢查询告警
      - alert: SlowQueryDetected
        expr: |
          pg_stat_statements_mean_exec_time{mean_exec_time > 100} > 100
        for: 5m
        labels:
          severity: warning
          team: database
        annotations:
          summary: "检测到慢查询"
          description: |
            查询 {{ $labels.query }} 平均执行时间: {{ $value }}ms
            需要优化。

  - name: promanage_business
    interval: 1m
    rules:
      # 业务指标告警
      - alert: LowTPS
        expr: |
          sum(rate(http_server_requests_seconds_count[5m])) < 100
        for: 10m
        labels:
          severity: info
          team: backend
        annotations:
          summary: "TPS低于预期"
          description: |
            当前TPS: {{ $value }} (目标: >1000)
            可能是正常业务低峰期。
```

---

## 3. Alertmanager配置

### 3.1 alertmanager.yml

```yaml
global:
  resolve_timeout: 5m
  # 邮件发送配置
  smtp_smarthost: 'smtp.example.com:587'
  smtp_from: 'promanage-alert@example.com'
  smtp_auth_username: 'alert@example.com'
  smtp_auth_password: 'your_password'

# 路由配置
route:
  group_by: ['alertname', 'severity']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 12h
  receiver: 'default'
  routes:
    # 严重告警立即通知
    - match:
        severity: critical
      receiver: 'critical-alerts'
      continue: true
    
    # 警告级别告警
    - match:
        severity: warning
      receiver: 'warning-alerts'
    
    # 后端团队告警
    - match:
        team: backend
      receiver: 'backend-team'
    
    # 数据库团队告警
    - match:
        team: database
      receiver: 'database-team'

# 接收器配置
receivers:
  - name: 'default'
    email_configs:
      - to: 'devops@example.com'
        subject: 'ProManage告警: {{ .GroupLabels.alertname }}'
        html: |
          <h2>告警详情</h2>
          <p><strong>告警名称:</strong> {{ .GroupLabels.alertname }}</p>
          <p><strong>严重程度:</strong> {{ .GroupLabels.severity }}</p>
          <p><strong>团队:</strong> {{ .GroupLabels.team }}</p>
          <h3>告警实例</h3>
          {{ range .Alerts }}
          <p>
            <strong>描述:</strong> {{ .Annotations.description }}<br/>
            <strong>开始时间:</strong> {{ .StartsAt }}<br/>
            <strong>标签:</strong> {{ range .Labels.SortedPairs }}{{ .Name }}={{ .Value }} {{ end }}
          </p>
          {{ end }}

  - name: 'critical-alerts'
    email_configs:
      - to: 'on-call@example.com'
        send_resolved: true
    webhook_configs:
      - url: 'http://localhost:5001/alert'
        send_resolved: true

  - name: 'warning-alerts'
    email_configs:
      - to: 'devops@example.com'
        send_resolved: false

  - name: 'backend-team'
    email_configs:
      - to: 'backend-team@example.com'
    slack_configs:
      - api_url: 'https://hooks.slack.com/services/YOUR/SLACK/WEBHOOK'
        channel: '#backend-alerts'
        title: 'ProManage后端告警'
        text: '{{ range .Alerts }}{{ .Annotations.description }}{{ end }}'

  - name: 'database-team'
    email_configs:
      - to: 'database-team@example.com'
```

---

## 4. Grafana Dashboard配置

### 4.1 关键指标面板

#### 4.1.1 API性能面板
- **SQL查询次数**: `sum(promanage_sql_query_count_total) by (endpoint)`
- **响应时间P50/P95/P99**: `histogram_quantile(0.xx, rate(http_server_requests_seconds_bucket[5m]))`
- **请求速率**: `rate(http_server_requests_seconds_count[5m])`
- **错误率**: `rate(http_server_requests_seconds_count{status=~"5.."}[5m]) / rate(http_server_requests_seconds_count[5m])`

#### 4.1.2 系统资源面板
- **CPU使用率**: `rate(process_cpu_seconds_total[5m]) * 100`
- **内存使用**: `jvm_memory_used_bytes / jvm_memory_max_bytes * 100`
- **线程数**: `jvm_threads_live_threads`
- **GC次数**: `jvm_gc_pause_seconds_count`

#### 4.1.3 数据库性能面板
- **连接数**: `pg_stat_database_numbackends`
- **慢查询**: `pg_stat_statements_mean_exec_time`
- **查询速率**: `rate(pg_stat_statements_calls[5m])`

---

## 5. 应用层监控配置

### 5.1 Spring Boot Actuator配置

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}
      environment: ${spring.profiles.active}
  endpoint:
    health:
      show-details: always
```

### 5.2 自定义指标

```java
@Component
public class PerformanceMetrics {
    private final MeterRegistry meterRegistry;
    
    // SQL查询次数指标
    private final Counter sqlQueryCounter;
    
    public PerformanceMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.sqlQueryCounter = Counter.builder("promanage.sql.query.count")
            .description("SQL查询次数")
            .tag("type", "total")
            .register(meterRegistry);
    }
    
    public void incrementSqlQuery(String endpoint) {
        sqlQueryCounter.increment(Tags.of("endpoint", endpoint));
    }
}
```

---

## 6. 告警响应流程

### 6.1 告警分级

| 级别 | 响应时间 | 通知方式 | 负责人 |
|------|---------|---------|--------|
| Critical | 15分钟 | 电话+邮件+Slack | 值班工程师 |
| Warning | 1小时 | 邮件+Slack | 团队负责人 |
| Info | 1天 | 邮件 | 相关人员 |

### 6.2 处理流程

1. **告警触发** → Alertmanager发送通知
2. **确认告警** → 值班工程师确认问题
3. **调查问题** → 查看日志和指标
4. **解决问题** → 修复或缓解
5. **记录问题** → 更新事件日志
6. **告警恢复** → 验证问题已解决

---

## 7. 监控最佳实践

1. **避免告警疲劳**
   - 设置合理的阈值
   - 合并相似告警
   - 使用告警抑制规则

2. **定期审查**
   - 每周审查告警规则
   - 分析告警趋势
   - 优化阈值设置

3. **文档化**
   - 记录告警处理流程
   - 建立知识库
   - 分享经验教训

---

## 附录

### A. 告警规则文件

- `prometheus_rules.yml`: Prometheus告警规则
- `alertmanager.yml`: Alertmanager配置

### B. Grafana Dashboard JSON

导出Dashboard JSON配置，便于团队共享。

### C. 监控脚本

- `scripts/check_sql_query_count.sh`: SQL查询次数检查脚本
- `scripts/generate_metrics_report.sh`: 指标报告生成脚本


