# ProManage 测试覆盖率改进计划

## 📊 当前测试覆盖率分析

### Infrastructure 模块
- **总体覆盖率**: 约 5%
- **主要问题**: 大部分配置类和工具类未测试
- **已测试**: JwtAuthenticationFilter (部分)

### Service 模块  
- **总体覆盖率**: 约 25%
- **主要问题**: 业务逻辑测试不完整，集成测试配置错误
- **已测试**: SearchServiceImpl, TaskServiceImpl (部分), OrganizationServiceImpl (部分)

## 🎯 改进目标

### 短期目标 (1-2周)
- **目标覆盖率**: 70%
- **重点模块**: Service层核心业务逻辑
- **修复**: 现有测试失败问题

### 中期目标 (1个月)
- **目标覆盖率**: 80%
- **完善**: Infrastructure层工具类测试
- **新增**: 集成测试和端到端测试

## 🔧 改进策略

### 1. 修复现有测试问题

#### 集成测试配置问题
```java
// 问题: 缺少 @SpringBootConfiguration
// 解决方案: 添加测试配置类或使用 @SpringBootTest(classes = ...)

@SpringBootTest(classes = {
    ProManageApplication.class,
    TestSecurityConfig.class
})
@TestPropertySource(locations = "classpath:application-test.yml")
class TaskServiceIntegrationTest {
    // 测试代码
}
```

#### Mock配置问题
```java
// 问题: UnnecessaryStubbingException
// 解决方案: 使用 @MockitoSettings(strictness = Lenient.LENIENT)

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PermissionServiceImplTest {
    // 测试代码
}
```

### 2. 优先级测试列表

#### 高优先级 (核心业务逻辑)
1. **DocumentServiceImpl** - 文档管理核心
2. **UserServiceImpl** - 用户管理
3. **ProjectServiceImpl** - 项目管理
4. **PermissionServiceImpl** - 权限控制
5. **NotificationServiceImpl** - 通知服务

#### 中优先级 (支撑功能)
1. **OrganizationServiceImpl** - 组织管理
2. **TaskServiceImpl** - 任务管理
3. **DocumentFolderServiceImpl** - 文档文件夹
4. **RoleServiceImpl** - 角色管理

#### 低优先级 (工具类和配置)
1. **SecurityUtils** - 安全工具
2. **TracingUtil** - 链路追踪
3. **CacheService** - 缓存服务
4. **各种Config类** - 配置类

### 3. 测试模板和最佳实践

#### 单元测试模板
```java
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ServiceImplTest {

    @Mock
    private SomeMapper someMapper;
    
    @Mock
    private SomeService someService;
    
    @InjectMocks
    private ServiceImpl serviceImpl;
    
    @BeforeEach
    void setUp() {
        // 通用设置
    }
    
    @Test
    @DisplayName("应该成功执行正常流程")
    void shouldExecuteSuccessfully_whenValidInput() {
        // Given
        // When
        // Then
    }
    
    @Test
    @DisplayName("应该抛出异常_当输入无效时")
    void shouldThrowException_whenInvalidInput() {
        // Given
        // When & Then
        assertThrows(BusinessException.class, () -> {
            serviceImpl.someMethod(invalidInput);
        });
    }
}
```

#### 集成测试模板
```java
@SpringBootTest(classes = {
    ProManageApplication.class,
    TestSecurityConfig.class
})
@TestPropertySource(locations = "classpath:application-test.yml")
@Transactional
@Rollback
class ServiceIntegrationTest {

    @Autowired
    private SomeService someService;
    
    @Autowired
    private TestDataFactory testDataFactory;
    
    @Test
    @DisplayName("集成测试_完整业务流程")
    void shouldCompleteBusinessFlow_successfully() {
        // Given
        var testData = testDataFactory.createTestData();
        
        // When
        var result = someService.processBusinessFlow(testData);
        
        // Then
        assertThat(result).isNotNull();
    }
}
```

## 📋 具体改进任务

### Phase 1: 修复现有测试 (本周)

#### 任务1: 修复集成测试配置
- [ ] 创建统一的测试配置类
- [ ] 修复 TestCaseProjectIntegrationTest
- [ ] 修复 UserPermissionIntegrationTest  
- [ ] 修复 TaskServiceIntegrationTest

#### 任务2: 修复单元测试问题
- [ ] 修复 DocumentServiceImplTest 的 Mock 问题
- [ ] 修复 NotificationServiceImplTest 的用户上下文问题
- [ ] 修复 PermissionServiceImplTest 的 Stubbing 问题
- [ ] 修复 ProjectActivityServiceImplTest 的 Mapper 问题

### Phase 2: 新增核心测试 (下周)

#### 任务3: DocumentServiceImpl 完整测试
- [ ] 文档创建测试 (正常/异常流程)
- [ ] 文档查询测试 (分页/搜索/过滤)
- [ ] 文档更新测试 (权限/版本控制)
- [ ] 文档删除测试 (软删除/权限检查)

#### 任务4: UserServiceImpl 完整测试
- [ ] 用户注册测试
- [ ] 用户认证测试
- [ ] 用户信息更新测试
- [ ] 用户权限管理测试

#### 任务5: ProjectServiceImpl 完整测试
- [ ] 项目创建测试
- [ ] 项目成员管理测试
- [ ] 项目权限测试
- [ ] 项目统计测试

### Phase 3: 工具类和配置测试 (第3周)

#### 任务6: Infrastructure 层测试
- [ ] SecurityUtils 测试
- [ ] JwtTokenProvider 完整测试
- [ ] TracingUtil 测试
- [ ] CacheService 测试

#### 任务7: 配置类测试
- [ ] SecurityConfig 测试
- [ ] CacheConfig 测试
- [ ] AsyncConfig 测试

## 🛠️ 测试工具和环境

### 测试依赖
```xml
<!-- 已包含的测试依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
```

### 测试配置文件
- `application-test.yml` - 测试环境配置
- `TestSecurityConfig.java` - 测试安全配置
- `TestDataFactory.java` - 测试数据工厂

### 测试命令
```bash
# 运行所有测试
mvn test -Pdev

# 生成覆盖率报告
mvn jacoco:report -Pdev

# 查看覆盖率报告
# Infrastructure: backend/promanage-infrastructure/target/site/jacoco/index.html
# Service: backend/promanage-service/target/site/jacoco/index.html
```

## 📈 成功指标

### 覆盖率目标
- **行覆盖率**: 70% → 80%
- **分支覆盖率**: 60% → 70%
- **方法覆盖率**: 70% → 85%

### 质量指标
- **测试通过率**: 100%
- **测试执行时间**: < 30秒
- **测试稳定性**: 无随机失败

### 业务指标
- **核心业务逻辑覆盖**: 90%+
- **异常场景覆盖**: 80%+
- **边界条件测试**: 完整覆盖

## 🔄 持续改进

### 每日检查
- [ ] 新增代码的测试覆盖率
- [ ] 测试执行结果
- [ ] 失败测试的修复

### 每周回顾
- [ ] 覆盖率趋势分析
- [ ] 测试质量评估
- [ ] 改进计划调整

### 每月评估
- [ ] 整体测试策略回顾
- [ ] 工具和框架升级
- [ ] 最佳实践更新

---

## 📝 备注

1. **优先修复现有失败测试**，确保构建稳定性
2. **重点关注核心业务逻辑**，确保关键功能的可靠性
3. **逐步提升覆盖率**，避免一次性大量修改
4. **保持测试质量**，宁可少而精，不要多而乱
5. **定期维护测试**，及时更新过时的测试用例

---

**创建时间**: 2025-10-22  
**负责人**: ProManage Team  
**预计完成时间**: 3周  
**当前状态**: 进行中