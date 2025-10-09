# Elasticsearch 安装和配置指南

本指南介绍如何在Windows环境下安装、配置和使用Elasticsearch。

## 系统要求

- Windows 10/11 或 Windows Server 2016+
- Java 17+ (已安装，用于运行ProManage)
- 至少 2GB 可用内存
- 至少 1GB 可用磁盘空间

## 安装步骤

### 1. 下载Elasticsearch

访问Elasticsearch官网下载页面：
```
https://www.elastic.co/downloads/elasticsearch
```

或使用直接下载链接（Windows ZIP版本）：
```
https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-8.11.0-windows-x86_64.zip
```

**推荐版本**: Elasticsearch 8.11.0 或更高版本

### 2. 解压安装包

1. 将下载的ZIP文件解压到合适的目录，例如：
   ```
   C:\elasticsearch-8.11.0
   ```

2. 解压后的目录结构：
   ```
   C:\elasticsearch-8.11.0\
   ├── bin\           # 可执行文件
   ├── config\        # 配置文件
   ├── data\          # 数据目录
   ├── logs\          # 日志目录
   └── plugins\       # 插件目录
   ```

### 3. 配置Elasticsearch

编辑配置文件：`C:\elasticsearch-8.11.0\config\elasticsearch.yml`

```yaml
# 集群名称
cluster.name: promanage-cluster

# 节点名称
node.name: node-1

# 数据和日志路径
path.data: C:\elasticsearch-8.11.0\data
path.logs: C:\elasticsearch-8.11.0\logs

# 网络配置
network.host: 0.0.0.0
http.port: 9200

# 开发环境配置（禁用安全认证，仅用于开发）
xpack.security.enabled: false
xpack.security.enrollment.enabled: false
xpack.security.http.ssl.enabled: false
xpack.security.transport.ssl.enabled: false

# 单节点模式
discovery.type: single-node

# JVM堆内存设置（可选，编辑 config\jvm.options）
# -Xms1g
# -Xmx1g
```

### 4. 配置JVM内存（可选）

编辑 `C:\elasticsearch-8.11.0\config\jvm.options`：

对于开发环境，建议设置：
```
-Xms512m
-Xmx512m
```

对于生产环境，建议设置：
```
-Xms2g
-Xmx2g
```

## 启动Elasticsearch

### 方式一：使用命令行启动

1. 打开命令提示符（CMD）或PowerShell
2. 切换到Elasticsearch的bin目录：
   ```cmd
   cd C:\elasticsearch-8.11.0\bin
   ```
3. 启动Elasticsearch：
   ```cmd
   elasticsearch.bat
   ```

### 方式二：使用Windows服务（推荐）

1. 以管理员身份打开命令提示符
2. 切换到bin目录：
   ```cmd
   cd C:\elasticsearch-8.11.0\bin
   ```
3. 安装Windows服务：
   ```cmd
   elasticsearch-service.bat install
   ```
4. 启动服务：
   ```cmd
   elasticsearch-service.bat start
   ```
5. 其他服务管理命令：
   ```cmd
   # 停止服务
   elasticsearch-service.bat stop

   # 移除服务
   elasticsearch-service.bat remove

   # 查看服务状态
   elasticsearch-service.bat manager
   ```

## 验证安装

### 1. 检查Elasticsearch是否运行

打开浏览器或使用curl访问：
```
http://localhost:9200
```

成功响应示例：
```json
{
  "name" : "node-1",
  "cluster_name" : "promanage-cluster",
  "cluster_uuid" : "xxx",
  "version" : {
    "number" : "8.11.0",
    ...
  },
  "tagline" : "You Know, for Search"
}
```

### 2. 使用curl测试（如果已安装）

```cmd
curl -X GET "localhost:9200/_cluster/health?pretty"
```

成功响应：
```json
{
  "cluster_name" : "promanage-cluster",
  "status" : "green",
  "number_of_nodes" : 1,
  "number_of_data_nodes" : 1
}
```

## 在ProManage中启用Elasticsearch

### 1. 修改配置文件

编辑：`backend\promanage-api\src\main\resources\application-dev.yml`

将Elasticsearch启用：
```yaml
spring:
  elasticsearch:
    enabled: true          # 改为 true
    uris: localhost:9200   # 确保地址正确
```

### 2. 重启ProManage应用

```cmd
cd backend\promanage-api
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 3. 验证集成

检查应用日志，应该看到类似输出：
```
Successfully connected to Elasticsearch at localhost:9200
```

## 常见问题

### 问题1：端口9200已被占用

**解决方案**：修改elasticsearch.yml中的http.port为其他端口：
```yaml
http.port: 9201
```
同时更新ProManage配置中的uris：
```yaml
spring.elasticsearch.uris: localhost:9201
```

### 问题2：内存不足错误

**解决方案**：减少JVM堆内存设置（编辑jvm.options）：
```
-Xms256m
-Xmx256m
```

### 问题3：启动失败，提示Java版本不兼容

**解决方案**：确保JAVA_HOME环境变量指向Java 17+：
```cmd
java -version
```

### 问题4：Elasticsearch无法访问

**检查步骤**：
1. 确认Elasticsearch进程正在运行：
   ```cmd
   netstat -ano | findstr :9200
   ```
2. 检查防火墙设置
3. 查看Elasticsearch日志：
   ```
   C:\elasticsearch-8.11.0\logs\promanage-cluster.log
   ```

## 生产环境配置（重要）

⚠️ **开发环境配置已禁用安全功能，生产环境必须启用安全认证！**

生产环境配置要点：
1. 启用xpack安全：
   ```yaml
   xpack.security.enabled: true
   xpack.security.http.ssl.enabled: true
   ```
2. 设置强密码
3. 配置SSL/TLS证书
4. 限制网络访问
5. 启用审计日志

详细配置请参考：
https://www.elastic.co/guide/en/elasticsearch/reference/current/security-settings.html

## 相关资源

- Elasticsearch官方文档：https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html
- Windows安装指南：https://www.elastic.co/guide/en/elasticsearch/reference/current/zip-windows.html
- 中文社区：https://elasticsearch.cn/

## ProManage搜索功能

启用Elasticsearch后，ProManage将支持以下搜索功能：
- 全文搜索文档内容
- 快速搜索任务、项目
- 智能搜索提示
- 搜索结果高亮显示

搜索索引会在应用启动时自动创建。
