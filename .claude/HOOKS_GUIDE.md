# Claude Code Hooks 使用指南

## 什么是 Hooks？

Hooks 允许你在 Claude Code 执行特定操作时自动运行命令，实现自动化工作流。

## 已配置的 Hooks

### 1. `afterEdit` - 编辑后自动编译（✅ 已启用）

**触发时机：** 每次使用 `Edit` 工具修改 Java 文件后

**执行命令：** `cd backend && mvn compile -q`

**作用：** 自动编译 Java 代码，快速发现编译错误

**配置：**
```json
"afterEdit": {
  "command": "cd backend && mvn compile -q",
  "description": "Compile Java code after editing",
  "enabled": true,
  "continueOnError": true
}
```

### 2. `onError` - 错误处理（✅ 已启用）

**触发时机：** 任何操作出错时

**执行命令：** `echo '❌ Error occurred. Check logs for details.'`

**作用：** 友好的错误提示

### 3. `beforeEdit` - 编辑前备份（❌ 未启用）

**触发时机：** 每次编辑文件前

**执行命令：** `echo 'Backing up file before edit...'`

**如何启用：** 将 `enabled` 改为 `true`

**建议命令：**
```json
"beforeEdit": {
  "command": "cp {{filePath}} {{filePath}}.backup",
  "description": "Backup file before editing",
  "enabled": true
}
```

### 4. `afterWrite` - 写入后操作（❌ 未启用）

**触发时机：** 创建新文件后

**建议用途：**
- 自动格式化代码
- 添加文件到 Git
- 运行代码检查

**示例配置：**
```json
"afterWrite": {
  "command": "git add {{filePath}} && echo 'File added to git'",
  "description": "Add new file to git",
  "enabled": true
}
```

### 5. `userPromptSubmit` - 用户提交提示时（❌ 未启用）

**触发时机：** 每次用户发送消息时

**当前命令：** `git status --short`

**建议启用场景：**
- 自动显示项目状态
- 检查未提交的更改
- 显示当前分支

## 可用的 Hook 类型

| Hook 名称 | 触发时机 | 常见用途 |
|-----------|----------|----------|
| `beforeRead` | 读取文件前 | 权限检查 |
| `afterRead` | 读取文件后 | 日志记录 |
| `beforeEdit` | 编辑文件前 | 备份文件 |
| `afterEdit` | 编辑文件后 | 自动编译/格式化 |
| `beforeWrite` | 写入文件前 | 检查文件是否存在 |
| `afterWrite` | 写入文件后 | 添加到 Git/运行测试 |
| `beforeBash` | 执行命令前 | 权限检查/日志 |
| `afterBash` | 执行命令后 | 清理/通知 |
| `onError` | 发生错误时 | 错误处理/回滚 |
| `userPromptSubmit` | 用户发送消息时 | 显示状态 |

## Hook 配置参数

```json
{
  "command": "要执行的命令",
  "description": "Hook 的描述",
  "enabled": true,  // 是否启用
  "continueOnError": true,  // 出错时是否继续（不阻断主流程）
  "timeout": 5000  // 超时时间（毫秒）
}
```

## 实用 Hook 配置示例

### 示例 1：自动运行测试

```json
"afterEdit": {
  "command": "cd backend && mvn test -Dtest={{testClassName}} -q",
  "description": "Run related tests after editing",
  "enabled": true,
  "continueOnError": true
}
```

### 示例 2：自动格式化代码

```json
"afterWrite": {
  "command": "cd backend && mvn spotless:apply -q",
  "description": "Format code after writing",
  "enabled": true,
  "continueOnError": true
}
```

### 示例 3：代码质量检查

```json
"afterEdit": {
  "command": "cd backend && mvn checkstyle:check -q",
  "description": "Check code style after editing",
  "enabled": true,
  "continueOnError": true
}
```

### 示例 4：自动生成文档

```json
"afterWrite": {
  "command": "cd backend && mvn javadoc:javadoc -q",
  "description": "Generate Javadoc after writing",
  "enabled": false,
  "continueOnError": true
}
```

### 示例 5：Git 自动添加

```json
"afterWrite": {
  "command": "git add {{filePath}}",
  "description": "Auto add file to git staging",
  "enabled": false
}
```

### 示例 6：构建项目

```json
"afterEdit": {
  "command": "cd backend && mvn clean install -DskipTests -q",
  "description": "Build project after major changes",
  "enabled": false
}
```

### 示例 7：运行 Spring Boot 应用（开发模式）

```json
"afterEdit": {
  "command": "cd backend/promanage-api && mvn spring-boot:run &",
  "description": "Restart Spring Boot app after edit",
  "enabled": false
}
```

## 变量占位符

Hook 命令中可以使用以下变量：

- `{{filePath}}` - 操作的文件路径
- `{{fileName}}` - 文件名
- `{{fileDir}}` - 文件所在目录
- `{{command}}` - 执行的命令（仅 beforeBash/afterBash）
- `{{error}}` - 错误信息（仅 onError）

## 最佳实践

### 1. 性能考虑

❌ **不推荐：** 每次编辑后都运行完整构建
```json
"afterEdit": {
  "command": "mvn clean install",  // 太慢！
  "enabled": true
}
```

✅ **推荐：** 只编译或运行相关测试
```json
"afterEdit": {
  "command": "mvn compile -q",  // 快速反馈
  "enabled": true,
  "continueOnError": true
}
```

### 2. 错误处理

始终设置 `continueOnError: true` 避免阻断工作流：

```json
"afterEdit": {
  "command": "mvn compile",
  "enabled": true,
  "continueOnError": true  // ✅ 编译失败也不会中断 Claude
}
```

### 3. 日志输出

使用 `-q` (quiet) 减少不必要的输出：

```json
"afterEdit": {
  "command": "mvn compile -q",  // ✅ 只显示错误
  "enabled": true
}
```

### 4. 条件执行

使用 shell 条件判断：

```json
"afterEdit": {
  "command": "if [[ {{filePath}} == *.java ]]; then mvn compile -q; fi",
  "description": "Only compile Java files",
  "enabled": true
}
```

## 调试 Hooks

### 查看 Hook 执行日志

Hooks 的输出会显示在 Claude Code 的响应中。

### 临时禁用所有 Hooks

设置环境变量：
```bash
export CLAUDE_DISABLE_HOOKS=true
```

### 调试单个 Hook

将 `enabled` 设为 `false` 来禁用特定 Hook。

## ProManage 项目推荐配置

### 开发阶段（快速反馈）

```json
"hooks": {
  "afterEdit": {
    "command": "cd backend && mvn compile -q",
    "description": "Quick compile check",
    "enabled": true,
    "continueOnError": true
  }
}
```

### 测试阶段（自动测试）

```json
"hooks": {
  "afterEdit": {
    "command": "cd backend && mvn test -q",
    "description": "Run all tests",
    "enabled": true,
    "continueOnError": true,
    "timeout": 60000
  }
}
```

### 代码审查阶段（质量检查）

```json
"hooks": {
  "afterEdit": {
    "command": "cd backend && mvn checkstyle:check pmd:check spotbugs:check -q",
    "description": "Quality checks",
    "enabled": true,
    "continueOnError": true
  }
}
```

## 常见问题

### Q: Hook 执行失败怎么办？

A: 检查 `continueOnError` 是否设置为 `true`，并查看命令是否正确。

### Q: Hook 太慢影响效率？

A: 使用 `-q` 减少输出，或者只在必要时启用 Hook。

### Q: 如何在 Hook 中使用项目特定配置？

A: 可以创建 shell 脚本，然后在 Hook 中调用：
```json
"afterEdit": {
  "command": "bash .claude/hooks/after-edit.sh {{filePath}}",
  "enabled": true
}
```

### Q: Hook 可以访问环境变量吗？

A: 可以，Hook 命令在 shell 环境中执行，可以访问所有环境变量。

## 更多示例

查看 `.claude/config.json` 中的完整配置，根据需要启用或修改。

---

**提示：** 修改 Hook 配置后，Claude Code 会立即生效，无需重启。
