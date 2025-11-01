# ProManage 未完成任务原子化实施计划

**文档版本**: V1.0  
**创建日期**: 2025-10-30  
**负责人**: 技术架构组  
**预计完成时间**: 4周（20个工作日）

---

## 📋 执行摘要

本文档将ProManage后端审计报告中未完成的任务进行原子化分解，提供详细、可执行的实施计划。所有任务按照优先级排序，每个任务都包含明确的输入、输出、验收标准和测试策略。

---

## 🎯 总体目标

### 主要目标
1. **消除N+1查询问题**：重构4个Controller中的DTO转换逻辑，将SQL查询次数从N+1降低到2-3次
2. **优化架构分层**：将DTO装配逻辑从Controller层下沉到Service层，遵循单一职责原则
3. **提升系统性能**：API响应时间P95降低40%以上，数据库负载降低60%以上
4. **改进异常处理**：细化WebSocket处理器中的异常捕获，提高系统健壮性

### 成功指标
- ✅ 所有列表接口的SQL查询次数 ≤ 3次（排除主查询）
- ✅ API响应时间P95提升 ≥ 40%
- ✅ 代码覆盖率保持 ≥ 80%
- ✅ 所有单元测试和集成测试通过
- ✅ PMD静态分析无新增警告

---

## 📊 任务分解总览

| 任务组 | 优先级 | 预计工作量 | 依赖关系 |
|--------|--------|-----------|---------|
| **任务组1: N+1查询重构** | 🔴 高 | 15天 | 无 |
| **任务组2: WebSocket异常处理** | 🟡 中 | 2天 | 无 |
| **任务组3: 日志审查** | 🟢 低 | 2天 | 无 |

---

# 🔴 任务组1: N+1查询问题重构（高优先级）

## 任务1.1: ChangeRequestController N+1重构

### 📝 任务描述
重构`ChangeRequestController`中的`convertToChangeRequestResponse`等方法，消除N+1查询问题。

### 🎯 验收标准
- [ ] 单个`listChangeRequests`接口SQL查询次数从N+3降低到2次（主查询+1次批量用户查询）
- [ ] 所有单元测试通过
- [ ] 集成测试通过，响应时间P95提升≥40%
- [ ] 代码审查通过

### 📥 输入
- `ChangeRequestController.java`
- `IUserService.getByIds()` 方法（已存在）
- 相关DTO类：`ChangeRequestResponse`, `ImpactResponse`, `ApprovalResponse`

### 📤 输出
- 重构后的`ChangeRequestController.java`
- 重构后的`ChangeRequestControllerTest.java`
- 性能测试报告

### 🔧 实施步骤

#### Step 1.1.1: 分析现有代码结构（30分钟）
**目标**: 识别所有需要重构的方法和依赖关系

**操作**:
```bash
# 查找所有convert方法
grep -n "convertTo.*Response" backend/promanage-api/src/main/java/com/promanage/api/controller/ChangeRequestController.java

# 统计每个方法中的userService.getById调用次数
```

**产出**:
- 方法列表：`convertToChangeRequestResponse`, `convertToImpactResponse`, `convertToApprovalResponse`
- 用户ID字段列表：`requesterId`, `assigneeId`, `reviewerId`

#### Step 1.1.2: 创建用户ID收集工具方法（1小时）
**目标**: 在Controller中创建辅助方法，用于从实体列表中收集所有用户ID

**代码示例**:
```java
/**
 * 从变更请求列表中收集所有关联的用户ID
 * 
 * @param changeRequests 变更请求列表
 * @return 去重后的用户ID集合
 */
private Set<Long> collectUserIds(List<ChangeRequest> changeRequests) {
    return changeRequests.stream()
        .flatMap(cr -> Stream.of(
            cr.getRequesterId(),
            cr.getAssigneeId(),
            cr.getReviewerId()
        ))
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
}
```

**验收**:
- [ ] 方法能正确收集所有用户ID
- [ ] 包含单元测试
- [ ] 处理null值情况

#### Step 1.1.3: 重构convertToChangeRequestResponse方法（2小时）
**目标**: 修改方法签名，接受用户Map作为参数，移除userService依赖

**Before**:
```java
private ChangeRequestResponse convertToChangeRequestResponse(ChangeRequest changeRequest) {
    User requester = changeRequest.getRequesterId() != null 
        ? userService.getById(changeRequest.getRequesterId()) : null;
    // ... 多次getById调用
}
```

**After**:
```java
private ChangeRequestResponse convertToChangeRequestResponse(
        ChangeRequest changeRequest, 
        Map<Long, User> userMap) {
    User requester = Optional.ofNullable(changeRequest.getRequesterId())
        .map(userMap::get)
        .orElse(null);
    // ... 从Map中获取
}
```

**验收**:
- [ ] 方法不再调用userService
- [ ] 从Map中安全获取用户信息（处理null情况）
- [ ] 原有功能保持不变

#### Step 1.1.4: 重构调用方代码（2小时）
**目标**: 在Controller的列表方法中，批量获取用户并传递给转换方法

**代码示例**:
```java
@GetMapping
public Result<PageResult<ChangeRequestResponse>> listChangeRequests(...) {
    // 1. 获取变更请求列表
    PageResult<ChangeRequest> pageResult = changeRequestService.listChangeRequests(...);
    
    // 2. 收集所有用户ID
    Set<Long> userIds = collectUserIds(pageResult.getList());
    
    // 3. 批量获取用户
    Map<Long, User> userMap = userIds.isEmpty() 
        ? Collections.emptyMap() 
        : userService.getByIds(new ArrayList<>(userIds));
    
    // 4. 转换DTO（传入userMap）
    List<ChangeRequestResponse> responses = pageResult.getList().stream()
        .map(cr -> convertToChangeRequestResponse(cr, userMap))
        .collect(Collectors.toList());
    
    // 5. 构建响应
    return Result.success(PageResult.of(responses, pageResult.getTotal(), ...));
}
```

**验收**:
- [ ] SQL查询日志显示只有2次查询（主查询+批量用户查询）
- [ ] 功能测试通过
- [ ] 处理空列表情况

#### Step 1.1.5: 重构其他转换方法（3小时）
**目标**: 同样重构`convertToImpactResponse`和`convertToApprovalResponse`

**步骤**:
1. 创建对应的ID收集方法
2. 修改方法签名，接受Map参数
3. 更新所有调用方

**验收**:
- [ ] 所有转换方法都使用批量查询
- [ ] 无遗漏的getById调用

#### Step 1.1.6: 编写单元测试（2小时）
**目标**: 确保重构后功能正确性

**测试用例**:
```java
@Test
void testConvertToChangeRequestResponse_withUserMap() {
    // Given
    ChangeRequest cr = new ChangeRequest();
    cr.setRequesterId(1L);
    cr.setAssigneeId(2L);
    
    User user1 = new User();
    user1.setId(1L);
    user1.setRealName("User 1");
    
    User user2 = new User();
    user2.setId(2L);
    user2.setRealName("User 2");
    
    Map<Long, User> userMap = Map.of(1L, user1, 2L, user2);
    
    // When
    ChangeRequestResponse response = controller.convertToChangeRequestResponse(cr, userMap);
    
    // Then
    assertThat(response.getRequesterName()).isEqualTo("User 1");
    assertThat(response.getAssigneeName()).isEqualTo("User 2");
}

@Test
void testListChangeRequests_shouldUseBatchQuery() {
    // Given: Mock service返回3个ChangeRequest
    // When: 调用listChangeRequests
    // Then: 验证userService.getByIds只被调用一次
    verify(userService, times(1)).getByIds(anyList());
    verify(userService, never()).getById(anyLong());
}
```

**验收**:
- [ ] 测试覆盖率≥90%
- [ ] 所有测试通过

#### Step 1.1.7: 性能测试（1小时）
**目标**: 验证性能提升

**测试步骤**:
```bash
# 使用JMeter或Postman进行性能测试
# 场景1: 10条记录
# 场景2: 100条记录
# 场景3: 1000条记录

# 记录指标：
# - SQL查询次数
# - 响应时间P50/P95/P99
# - 数据库CPU使用率
```

**验收**:
- [ ] 响应时间P95提升≥40%
- [ ] SQL查询次数降低≥90%

#### Step 1.1.8: 代码审查（1小时）
**目标**: 确保代码质量

**检查清单**:
- [ ] 代码符合规范
- [ ] 异常处理完整
- [ ] 日志记录适当
- [ ] 注释清晰

### ⚠️ 风险评估
- **风险1**: 遗漏某些转换方法 → **缓解**: 使用静态分析工具扫描所有getById调用
- **风险2**: 破坏现有功能 → **缓解**: 完整的功能测试覆盖
- **风险3**: 性能提升不明显 → **缓解**: 基准测试验证

### 📊 工作量估算
- 分析设计: 1小时
- 编码实现: 8小时
- 测试编写: 2小时
- 测试执行: 1小时
- 代码审查: 1小时
- **总计**: 13小时（约1.6天）

---

## 任务1.2: TaskController N+1重构

### 📝 任务描述
重构`TaskController`中的`convertToTaskResponse`、`convertToTaskDetailResponse`、`convertToTaskCommentResponse`等方法。

### 🎯 验收标准
- [ ] `listTasks`接口SQL查询次数 ≤ 3次
- [ ] 所有单元测试通过
- [ ] 性能测试通过

### 📥 输入
- `TaskController.java`
- `TaskResponse.java`, `TaskDetailResponse.java`, `TaskCommentResponse.java`

### 📤 输出
- 重构后的Controller和测试

### 🔧 实施步骤

#### Step 1.2.1: 分析任务关联的用户ID（30分钟）
**识别字段**:
- `assigneeId` - 执行人
- `reporterId` - 报告人
- `creatorId` - 创建人（评论和活动）

#### Step 1.2.2: 创建用户ID收集方法（1小时）
```java
private Set<Long> collectUserIdsFromTasks(List<Task> tasks) {
    return tasks.stream()
        .flatMap(task -> Stream.of(
            task.getAssigneeId(),
            task.getReporterId(),
            task.getCreatorId()
        ))
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
}
```

#### Step 1.2.3: 重构convertToTaskResponse（2小时）
**注意**: 该方法中还调用了`taskService.listTaskComments()`和`taskService.listSubtasks()`，需要评估是否也需要优化。

#### Step 1.2.4: 重构convertToTaskDetailResponse（3小时）
**特殊处理**: 
- 子任务列表转换（递归调用）
- 依赖任务列表转换
- 评论列表转换
- 活动列表转换

**策略**: 
- 先收集所有子任务、依赖任务的用户ID
- 收集所有评论的用户ID
- 收集所有活动的用户ID
- 一次性批量查询所有用户

#### Step 1.2.5: 重构convertToTaskCommentResponse（1小时）
**相对简单**: 只涉及评论创建者的用户ID

#### Step 1.2.6: 编写测试（2小时）
**重点测试**:
- 嵌套任务转换（子任务包含子任务）
- 空用户Map处理
- 部分用户不存在的情况

#### Step 1.2.7: 性能测试（1小时）
**重点关注**: 详情接口（包含多层嵌套转换）

### ⚠️ 特殊注意事项
- **嵌套转换**: `convertToTaskDetailResponse`中会递归调用`convertToTaskResponse`，需要传递用户Map
- **评论数量查询**: `commentCount`可能触发额外查询，考虑缓存或JOIN查询优化

### 📊 工作量估算
- **总计**: 11小时（约1.4天）

---

## 任务1.3: TestCaseController N+1重构

### 📝 任务描述
重构`TestCaseController`中的转换方法。

### 🎯 验收标准
同任务1.1

### 🔧 实施步骤

#### Step 1.3.1: 识别用户ID字段（30分钟）
**字段列表**:
- `creatorId`
- `assigneeId`
- `reviewerId`
- `lastExecutedById`

#### Step 1.3.2-1.3.7: 同任务1.1的步骤（8小时）
**工作量**: 8小时（约1天）

---

## 任务1.4: DocumentVersionResponse DTO重构

### 📝 任务描述
移除`DocumentVersionResponse.fromEntityWithUser`方法中的数据访问逻辑，将其移至Service层。

### 🎯 验收标准
- [ ] DTO中不再包含业务逻辑和数据访问
- [ ] 所有调用方已更新
- [ ] 测试通过

### 🔧 实施步骤

#### Step 1.4.1: 查找所有调用方（30分钟）
```bash
grep -r "fromEntityWithUser" backend/
```

#### Step 1.4.2: 在Service层创建批量转换方法（2小时）
**位置**: `DocumentServiceImpl`或`DocumentVersionServiceImpl`

**代码示例**:
```java
/**
 * 批量转换DocumentVersion为Response，包含用户信息
 * 
 * @param versions 版本列表
 * @return Response列表
 */
public List<DocumentVersionResponse> convertToResponseListWithUser(List<DocumentVersion> versions) {
    if (versions == null || versions.isEmpty()) {
        return Collections.emptyList();
    }
    
    // 1. 收集所有用户ID
    Set<Long> userIds = versions.stream()
        .map(DocumentVersion::getCreatorId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    
    // 2. 批量获取用户
    Map<Long, User> userMap = userIds.isEmpty()
        ? Collections.emptyMap()
        : userService.getByIds(new ArrayList<>(userIds));
    
    // 3. 转换
    return versions.stream()
        .map(version -> {
            User creator = Optional.ofNullable(version.getCreatorId())
                .map(userMap::get)
                .orElse(null);
            
            return DocumentVersionResponse.builder()
                .id(version.getId())
                .creatorId(version.getCreatorId())
                .creatorName(creator != null ? creator.getRealName() : null)
                .creatorAvatar(creator != null ? creator.getAvatar() : null)
                // ... 其他字段
                .build();
        })
        .collect(Collectors.toList());
}
```

#### Step 1.4.3: 更新所有调用方（1小时）
**替换策略**:
- 将所有`DocumentVersionResponse.fromEntityWithUser`调用替换为Service方法调用

#### Step 1.4.4: 移除DTO中的fromEntityWithUser方法（30分钟）
**注意**: 保留`fromEntity`方法作为向后兼容

#### Step 1.4.5: 编写测试（1小时）
**测试Service层的新方法**

### 📊 工作量估算
- **总计**: 5小时（约0.6天）

---

## 任务1.5: 性能基准测试和监控

### 📝 任务描述
建立性能基准，验证重构效果，并设置监控告警。

### 🔧 实施步骤

#### Step 1.5.1: 建立重构前性能基准（2小时）
**工具**: JMeter / Gatling / Apache Bench

**测试场景**:
| 场景 | 记录数 | 并发用户 | 测量指标 |
|------|--------|---------|---------|
| 小数据量 | 10 | 10 | SQL次数、响应时间 |
| 中数据量 | 100 | 20 | SQL次数、响应时间、DB CPU |
| 大数据量 | 1000 | 50 | SQL次数、响应时间、DB CPU、内存 |

**记录指标**:
- SQL查询次数（通过MyBatis日志或P6Spy）
- 响应时间：P50, P95, P99
- 数据库CPU使用率
- 应用内存使用

#### Step 1.5.2: 建立重构后性能基准（2小时）
**相同场景复测**

#### Step 1.5.3: 对比分析（1小时）
**产出报告**:
```
重构效果对比报告
================
ChangeRequestController.listChangeRequests:
- SQL查询次数: 103 → 2 (-98%)
- P95响应时间: 450ms → 180ms (-60%)
- DB CPU: 45% → 15% (-67%)

TaskController.listTasks:
- SQL查询次数: 205 → 3 (-98.5%)
- P95响应时间: 680ms → 250ms (-63%)
...
```

#### Step 1.5.4: 设置监控告警（1小时）
**监控项**:
- SQL查询次数异常告警（>5次）
- API响应时间P95告警（>300ms）
- 慢查询告警

### 📊 工作量估算
- **总计**: 6小时（约0.75天）

---

# 🟡 任务组2: WebSocket异常处理细化（中优先级）

## 任务2.1: WebSocket异常处理优化

### 📝 任务描述
细化`NotificationWebSocketHandler`中的泛型异常捕获，提高错误处理精确性。

### 🎯 验收标准
- [ ] 所有catch块捕获具体异常类型
- [ ] PMD静态分析无AvoidCatchingGenericException警告
- [ ] 异常日志包含完整堆栈信息
- [ ] 功能测试通过

### 🔧 实施步骤

#### Step 2.1.1: 分析现有异常处理（1小时）
**目标**: 识别所有泛型Exception捕获点

**检查文件**:
- `NotificationWebSocketHandler.java`
- `DefaultWebSocketSessionHandler.java`
- `WebSocketSessionManager.java`

**当前问题**:
```java
// ❌ 当前代码
} catch (Exception e) {
    log.error("处理WebSocket消息失败", e);
    throw new RuntimeException("Failed to process WebSocket message", e);
}
```

#### Step 2.1.2: 细化handleMessage异常处理（1小时）
**分析可能抛出的异常**:
- `IOException` - WebSocket消息传输错误
- `JsonProcessingException` - JSON解析错误
- `IllegalArgumentException` - 参数错误
- `SecurityException` - 认证失败

**重构后**:
```java
} catch (JsonProcessingException e) {
    log.error("JSON解析失败, 会话ID: {}, 消息: {}", sessionId, payload, e);
    handler.sendMessage(WebSocketMessage.error("消息格式错误"));
} catch (IOException e) {
    log.error("WebSocket传输错误, 会话ID: {}", sessionId, e);
    sessionManager.removeUserSession(sessionId);
    throw new RuntimeException("WebSocket传输失败", e);
} catch (SecurityException e) {
    log.warn("认证失败, 会话ID: {}", sessionId, e);
    session.close(CloseStatus.NOT_ACCEPTABLE.withReason("认证失败"));
} catch (IllegalArgumentException e) {
    log.warn("参数错误, 会话ID: {}, 错误: {}", sessionId, e.getMessage());
    handler.sendMessage(WebSocketMessage.error("参数错误: " + e.getMessage()));
} catch (RuntimeException e) {
    log.error("处理WebSocket消息失败, 会话ID: {}", sessionId, e);
    throw e;
}
```

#### Step 2.1.3: 细化getUserIdFromSession异常处理（1小时）
**分析异常**:
- `NullPointerException` - URI或查询参数为null
- `IllegalArgumentException` - Token格式错误
- `RuntimeException` - JWT解析失败

#### Step 2.1.4: 重构parseUserIdFromToken（1小时）
**创建专门的Token解析服务**:
```java
@Component
public class WebSocketAuthService {
    private final JwtTokenProvider jwtTokenProvider;
    
    /**
     * 从Token解析用户ID
     * 
     * @param token JWT Token
     * @return 用户ID
     * @throws WebSocketAuthException 如果Token无效或解析失败
     */
    public Long parseUserIdFromToken(String token) throws WebSocketAuthException {
        try {
            return jwtTokenProvider.getUserIdFromToken(token);
        } catch (ExpiredJwtException e) {
            throw new WebSocketAuthException("Token已过期", e);
        } catch (MalformedJwtException e) {
            throw new WebSocketAuthException("Token格式错误", e);
        } catch (SignatureException e) {
            throw new WebSocketAuthException("Token签名无效", e);
        } catch (Exception e) {
            throw new WebSocketAuthException("Token解析失败", e);
        }
    }
}
```

#### Step 2.1.5: 编写测试（1小时）
**测试用例**:
- 无效Token处理
- 过期Token处理
- 网络IO异常处理
- JSON解析错误处理

#### Step 2.1.6: 代码审查（30分钟）

### 📊 工作量估算
- **总计**: 5.5小时（约0.7天）

---

# 🟢 任务组3: 日志审查（低优先级）

## 任务3.1: 日志级别和内容审查

### 📝 任务描述
审查所有日志输出，确保级别正确、内容适当、无敏感信息泄露。

### 🎯 验收标准
- [ ] 所有DEBUG日志仅用于开发调试
- [ ] INFO日志记录关键业务流程
- [ ] ERROR日志包含完整堆栈信息
- [ ] 无敏感信息（密码、Token、个人隐私）泄露

### 🔧 实施步骤

#### Step 3.1.1: 扫描所有日志语句（2小时）
**工具**: 
```bash
# 查找所有日志调用
grep -r "log\." backend/promanage-*/src/main/java --include="*.java" > logs.txt

# 分析日志级别分布
grep -o "log\.\(debug\|info\|warn\|error\)" backend/ | sort | uniq -c
```

#### Step 3.1.2: 检查敏感信息泄露（2小时）
**关键词搜索**:
- `password`
- `token`
- `secret`
- `apiKey`
- `creditCard`
- `ssn`（社会安全号）

**检查清单**:
- [ ] 日志中无明文密码
- [ ] 日志中无完整Token（只记录前几位）
- [ ] 日志中无API密钥
- [ ] 个人隐私信息已脱敏

#### Step 3.1.3: 审查日志级别合理性（2小时）
**标准**:
- `DEBUG`: 详细的调试信息，生产环境关闭
- `INFO`: 关键业务流程、状态变化
- `WARN`: 潜在问题、非关键错误
- `ERROR`: 严重错误、需要人工干预

**示例检查**:
```java
// ❌ 不推荐
log.info("用户ID: {}, 用户名: {}", userId, username); // 应该用DEBUG

// ✅ 推荐
log.info("用户登录成功, userId={}", userId);
log.debug("用户详细信息, userId={}, username={}", userId, username);
```

#### Step 3.1.4: 优化日志内容（2小时）
**改进点**:
- 日志消息清晰、具体
- 包含足够的上下文信息
- 使用结构化日志（JSON格式，如果使用Logstash）

#### Step 3.1.5: 编写日志规范文档（1小时）
**内容**:
- 日志级别使用指南
- 敏感信息处理规范
- 日志格式标准

### 📊 工作量估算
- **总计**: 9小时（约1.1天）

---

## 📅 实施时间表

| 阶段 | 任务 | 开始日期 | 结束日期 | 负责人 |
|------|------|---------|---------|--------|
| **Week 1** | 1.1 ChangeRequestController重构 | Day 1 | Day 2 | Developer A |
| | 1.2 TaskController重构 | Day 2 | Day 4 | Developer B |
| **Week 2** | 1.3 TestCaseController重构 | Day 5 | Day 6 | Developer A |
| | 1.4 DocumentVersionResponse重构 | Day 6 | Day 7 | Developer B |
| | 1.5 性能基准测试 | Day 7 | Day 8 | QA Team |
| **Week 3** | 2.1 WebSocket异常处理 | Day 9 | Day 10 | Developer A |
| | 回归测试 | Day 10 | Day 12 | QA Team |
| **Week 4** | 3.1 日志审查 | Day 13 | Day 14 | Developer B |
| | 最终测试和发布 | Day 15 | Day 16 | All |

---

## ✅ 质量保证

### 测试策略

#### 单元测试
- **覆盖率要求**: ≥90%
- **关键路径**: 所有转换方法和批量查询方法
- **边界条件**: 空列表、null值、部分用户不存在

#### 集成测试
- **API测试**: 所有受影响的接口
- **性能测试**: 验证SQL查询次数和响应时间
- **压力测试**: 验证并发场景下的性能

#### 回归测试
- **功能回归**: 确保重构未破坏现有功能
- **性能回归**: 确保性能提升达标

### 代码审查清单
- [ ] 代码符合编码规范
- [ ] 无硬编码值
- [ ] 异常处理完整
- [ ] 日志记录适当
- [ ] 注释清晰
- [ ] 无重复代码
- [ ] 性能考虑充分

---

## 📊 风险管理和缓解策略

| 风险 | 概率 | 影响 | 缓解策略 |
|------|------|------|---------|
| 重构引入Bug | 中 | 高 | 完整测试覆盖、代码审查、灰度发布 |
| 性能提升不明显 | 低 | 中 | 基准测试验证、监控告警 |
| 遗漏某些转换方法 | 中 | 中 | 静态分析工具扫描、代码审查 |
| 影响其他模块 | 低 | 高 | 接口向后兼容、完整回归测试 |

---

## 📈 成功指标和度量

### 性能指标
- **SQL查询次数**: 降低 ≥90%
- **API响应时间P95**: 提升 ≥40%
- **数据库CPU使用率**: 降低 ≥50%

### 代码质量指标
- **代码覆盖率**: 保持 ≥80%
- **PMD警告**: 无新增警告
- **代码重复率**: ≤3%

### 业务指标
- **用户体验**: 页面加载时间提升 ≥30%
- **系统稳定性**: 无新增Bug

---

## 📝 附录

### A. 代码模板

#### 批量用户查询模板
```java
// 1. 收集用户ID
Set<Long> userIds = entities.stream()
    .flatMap(entity -> Stream.of(
        entity.getField1Id(),
        entity.getField2Id()
    ))
    .filter(Objects::nonNull)
    .collect(Collectors.toSet());

// 2. 批量查询
Map<Long, User> userMap = userIds.isEmpty()
    ? Collections.emptyMap()
    : userService.getByIds(new ArrayList<>(userIds));

// 3. 转换时使用Map
entities.stream()
    .map(entity -> convertToResponse(entity, userMap))
    .collect(Collectors.toList());
```

#### 转换方法模板
```java
private ResponseDTO convertToResponse(Entity entity, Map<Long, User> userMap) {
    User user1 = Optional.ofNullable(entity.getField1Id())
        .map(userMap::get)
        .orElse(null);
    
    return ResponseDTO.builder()
        .id(entity.getId())
        .field1Id(entity.getField1Id())
        .field1Name(user1 != null ? user1.getRealName() : null)
        // ... 其他字段
        .build();
}
```

### B. 测试用例模板

```java
@Test
void testConvertWithBatchUsers() {
    // Given
    Entity entity = createTestEntity();
    User user = createTestUser();
    Map<Long, User> userMap = Map.of(user.getId(), user);
    
    // When
    ResponseDTO dto = controller.convertToResponse(entity, userMap);
    
    // Then
    assertThat(dto.getField1Name()).isEqualTo(user.getRealName());
}

@Test
void testListEntities_shouldCallGetByIdsOnce() {
    // Given
    when(service.listEntities(any())).thenReturn(createEntityList(10));
    
    // When
    controller.listEntities(...);
    
    // Then
    verify(userService, times(1)).getByIds(anyList());
    verify(userService, never()).getById(anyLong());
}
```

### C. 性能测试脚本模板

```bash
#!/bin/bash
# 性能测试脚本

echo "开始性能测试..."

# 测试场景1: 10条记录
echo "场景1: 10条记录"
ab -n 1000 -c 10 http://localhost:8080/api/v1/change-requests?pageSize=10

# 测试场景2: 100条记录
echo "场景2: 100条记录"
ab -n 500 -c 20 http://localhost:8080/api/v1/change-requests?pageSize=100

# 测试场景3: 1000条记录
echo "场景3: 1000条记录"
ab -n 100 -c 10 http://localhost:8080/api/v1/change-requests?pageSize=1000

echo "性能测试完成"
```

---

## 🎯 总结

本文档提供了ProManage后端未完成任务的详细原子化实施计划。所有任务都经过仔细分解，包含明确的步骤、验收标准和测试策略。按照本计划执行，预计在4周内完成所有重构和优化工作，显著提升系统性能和代码质量。

**下一步行动**:
1. 技术负责人审查并批准本计划
2. 分配任务给开发人员
3. 建立项目看板跟踪进度
4. 开始执行任务1.1

---

**文档维护**: 本文档应在任务执行过程中持续更新，记录实际进度和遇到的问题。

