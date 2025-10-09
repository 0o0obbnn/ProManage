# 修复 @MockBean 过时警告的指南

## 问题描述

在 Spring Boot 3.4+ 中，`@MockBean` 注解已被标记为过时（deprecated），将在未来版本中移除。编译时会出现以下警告：

```
[WARNING] org.springframework.boot.test.mock.mockito 中的 org.springframework.boot.test.mock.mockito.MockBean 已过时, 且标记为待删除
```

## 解决方案

### 方案 1: 使用新的 @MockitoBean 注解（推荐）

Spring Boot 3.4+ 引入了新的 `@MockitoBean` 注解作为替代。

**修改步骤**:

1. **更新 import 语句**:
```java
// 旧的 (过时)
import org.springframework.boot.test.mock.mockito.MockBean;

// 新的 (推荐)
import org.springframework.test.context.bean.override.mockito.MockitoBean;
```

2. **更新注解**:
```java
// 旧的
@MockBean
private IAuthService authService;

// 新的
@MockitoBean
private IAuthService authService;
```

### 方案 2: 继续使用 @MockBean (临时方案)

如果您暂时不想修改代码，可以通过以下方式抑制警告：

**在类级别添加注解**:
```java
@SuppressWarnings("deprecation")
@WebMvcTest(AuthController.class)
class AuthControllerTest {
    // ...
}
```

**或在具体字段上添加**:
```java
@SuppressWarnings("deprecation")
@MockBean
private IAuthService authService;
```

## 需要修复的文件列表

根据编译警告，以下文件需要修复：

1. `AuthControllerTest.java` (第 41 行)
   - `@MockBean private IAuthService authService;`
   - `@MockBean private IUserService userService;` (第 44 行)

2. `DocumentControllerTest.java`
   - 第 30 行
   - 第 40 行
   - 第 44 行

3. `SearchControllerTest.java` (第 27 行)

## 批量修复脚本

### Windows PowerShell 脚本

```powershell
# 批量替换 import 语句
Get-ChildItem -Path "backend/promanage-api/src/test/java" -Filter "*Test.java" -Recurse | ForEach-Object {
    $content = Get-Content $_.FullName -Raw
    $content = $content -replace 'import org\.springframework\.boot\.test\.mock\.mockito\.MockBean;', 'import org.springframework.test.context.bean.override.mockito.MockitoBean;'
    $content = $content -replace '@MockBean', '@MockitoBean'
    Set-Content -Path $_.FullName -Value $content
}
```

### Linux/Mac Bash 脚本

```bash
#!/bin/bash
# 批量替换 import 语句和注解
find backend/promanage-api/src/test/java -name "*Test.java" -type f -exec sed -i \
  's/import org\.springframework\.boot\.test\.mock\.mockito\.MockBean;/import org.springframework.test.context.bean.override.mockito.MockitoBean;/g; s/@MockBean/@MockitoBean/g' {} \;
```

### 使用 sed 命令 (推荐)

```bash
# 1. 替换 import 语句
sed -i 's/import org\.springframework\.boot\.test\.mock\.mockito\.MockBean;/import org.springframework.test.context.bean.override.mockito.MockitoBean;/g' backend/promanage-api/src/test/java/com/promanage/api/controller/*Test.java

# 2. 替换注解
sed -i 's/@MockBean/@MockitoBean/g' backend/promanage-api/src/test/java/com/promanage/api/controller/*Test.java
```

## 手动修复示例

### AuthControllerTest.java

**修改前**:
```java
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(AuthController.class)
class AuthControllerTest {
    @MockBean
    private IAuthService authService;

    @MockBean
    private IUserService userService;
}
```

**修改后**:
```java
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(AuthController.class)
class AuthControllerTest {
    @MockitoBean
    private IAuthService authService;

    @MockitoBean
    private IUserService userService;
}
```

## 验证修复

修复后，重新编译项目确认警告消失：

```bash
cd backend
mvn clean compile
```

如果没有警告信息，说明修复成功。

## 相关资源

- [Spring Boot 3.4 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.4-Release-Notes)
- [Spring Framework 6.2 - Bean Override Testing](https://docs.spring.io/spring-framework/reference/testing/testcontext-framework/bean-overriding.html)
- [Migration Guide: @MockBean to @MockitoBean](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.4-Release-Notes#deprecation-of-mockbean-and-spybean)

## 注意事项

1. **功能等价**: `@MockitoBean` 与 `@MockBean` 在功能上完全等价，只是包路径不同
2. **向后兼容**: 旧的 `@MockBean` 在当前版本仍可使用，但建议尽快迁移
3. **测试行为**: 迁移后测试行为完全一致，不需要修改测试逻辑
4. **IDE支持**: 主流 IDE (IntelliJ IDEA, Eclipse) 都已支持新注解的自动补全和重构

## 优先级

**建议优先级**: P1 (中等优先级)

- 不影响系统功能运行
- 不影响测试执行
- 但会在编译时产生大量警告信息
- 未来版本可能导致编译失败

**建议时间**: 0.5天

## 完成标准

- ✅ 所有测试文件中的 `@MockBean` 替换为 `@MockitoBean`
- ✅ 所有 import 语句更新为新包路径
- ✅ 编译时无 deprecation 警告
- ✅ 所有测试用例通过
