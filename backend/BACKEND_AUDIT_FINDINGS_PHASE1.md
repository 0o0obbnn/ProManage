# 后端审计发现 - 阶段一：自动化分析

## 1. 静态代码分析 (Task 1.3)

### [CRITICAL] 自动化静态代码分析完全无效

- **问题描述:** 多次尝试通过修改`pom.xml`为`maven-pmd-plugin`配置正确的Java 21语言级别，但均以失败告终。PMD持续报告Java 14/15/16的语法解析错误，无法正确分析任何使用现代Java语法的代码文件。
- **影响:** 项目完全缺乏有效的自动化静态代码质量监控。潜在的bug、性能问题、安全漏洞和不规范代码无法被自动发现，严重依赖于人工审查，这会增加引入错误的风险并降低开发效率。
- **建议:**
  1. **立即修复构建配置:** 必须指派专人解决此问题。正确的PMD配置对于维护代码质量至关重要。
  2. **统一插件管理:** 建议在父`pom.xml`的`<pluginManagement>`中统一管理所有构建和报告插件（包括`maven-pmd-plugin`, `dependency-check-maven`等），并在子模块中通过`<plugins>`引用。这需要确保聚合执行时配置能正确应用。
  3. **引入更全面的规则集:** 一旦插件正常工作，应配置一个比默认更严格、更贴合项目实践的PMD规则集。

---

## 2. 依赖分析 (Task 1.2)

### [HIGH] 项目无法执行自动化的依赖安全扫描

- **问题描述:** 在尝试运行OWASP Dependency-Check时，由于缺少NVD (National Vulnerability Database) API密钥，扫描任务失败。NVD对匿名访问进行了速率限制，没有API密钥无法下载最新的漏洞数据。
- **影响:** 无法自动、持续地监控项目依赖中的已知安全漏洞 (CVEs)，使项目面临使用过时或易受攻击组件的风险。
- **建议:**
  1. 申请一个NVD API密钥。
  2. 将API密钥配置为环境变量 (`NVD_API_KEY`) 或在CI/CD流程中安全地提供给Maven命令，例如：
     ```bash
     mvn org.owasp:dependency-check-maven:9.2.0:check -DnvdApiKey=YOUR_API_KEY_HERE
     ```
  3. 将`dependency-check-maven`插件集成到`pom.xml`中，并纳入CI/CD流水线，以实现持续监控。
