# ProManage 代码质量检查配置指南

## 🎯 概述

本项目集成了多种代码质量检查工具，以确保代码质量和安全性。为了平衡开发效率和代码质量，我们提供了不同的构建配置。

## 🚀 快速开始

### 开发环境（推荐）
```bash
# 跳过大部分质量检查，提高开发效率
mvn clean compile -Pdev

# 或者使用属性方式
mvn clean compile -DskipQualityChecks=true
```

### CI/CD环境
```bash
# 启用所有质量检查
mvn clean verify -Pci
```

### 快速构建
```bash
# 跳过所有检查和测试，仅编译
mvn clean compile -Pfast
```

## 🛠️ 工具配置

### 1. Checkstyle
- **用途**: 代码风格检查
- **配置文件**: `checkstyle.xml`
- **跳过方式**: `-Dcheckstyle.skip=true`

### 2. SpotBugs + FindSecBugs
- **用途**: Bug检测和安全漏洞扫描
- **配置**: 包含安全插件
- **跳过方式**: `-Dspotbugs.skip=true`

### 3. PMD
- **用途**: 代码质量分析
- **配置文件**: `pmd-ruleset.xml`
- **跳过方式**: `-Dpmd.skip=true`

### 4. Spotless
- **用途**: 代码格式化
- **配置**: Google Java Style
- **跳过方式**: `-Dspotless.check.skip=true`
- **手动格式化**: `mvn spotless:apply`

### 5. JaCoCo
- **用途**: 测试覆盖率
- **目标覆盖率**: 
  - 行覆盖率: 70% (目标80%)
  - 分支覆盖率: 60%
- **跳过方式**: `-Djacoco.skip=true`

## 📊 覆盖率报告

测试覆盖率报告生成位置：
- HTML报告: `target/site/jacoco/index.html`
- XML报告: `target/site/jacoco/jacoco.xml`

## 🔧 IDE集成

### IntelliJ IDEA
1. 安装Checkstyle插件
2. 导入 `checkstyle.xml` 配置
3. 安装SpotBugs插件
4. 配置Google Java Format插件

### VS Code
1. 安装Java Extension Pack
2. 配置Checkstyle扩展
3. 安装SonarLint扩展

## 🚨 常见问题

### Q: 编译时出现格式化冲突？
A: 使用开发环境配置：`mvn clean compile -Pdev`

### Q: 如何只运行特定的质量检查？
A: 
```bash
# 只运行Checkstyle
mvn checkstyle:check

# 只运行SpotBugs
mvn spotbugs:check

# 只运行测试覆盖率
mvn jacoco:check
```

### Q: 如何在CI/CD中集成？
A: 使用CI配置文件：`mvn clean verify -Pci`

## 📈 质量指标目标

| 指标 | 当前目标 | 最终目标 |
|------|----------|----------|
| 行覆盖率 | 70% | 80% |
| 分支覆盖率 | 60% | 70% |
| Checkstyle违规 | < 50 | 0 |
| SpotBugs问题 | 0 | 0 |
| PMD违规 | 警告模式 | < 10 |

## 🔄 持续改进

1. **每周**: 检查覆盖率趋势
2. **每月**: 更新质量检查规则
3. **每季度**: 评估工具配置效果
4. **每半年**: 升级工具版本

## 📝 最佳实践

1. **开发时**: 使用 `-Pdev` 配置
2. **提交前**: 运行 `mvn spotless:apply` 格式化代码
3. **PR前**: 运行 `mvn clean verify -Pci` 完整检查
4. **定期**: 查看覆盖率报告，补充测试用例