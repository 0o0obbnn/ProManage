# ProManage 自动化部署脚本 (PowerShell版本)
# Version: 1.1.0
# Author: ProManage Team

param(
    [string]$Environment = "dev",
    [string]$Version = "1.1.0",
    [string]$Registry = "localhost:5000",
    [switch]$Build,
    [switch]$Deploy,
    [switch]$Migrate,
    [switch]$Check,
    [switch]$Rollback,
    [switch]$Help
)

# 颜色定义
function Write-Info {
    param([string]$Message)
    Write-Host "[INFO] $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') $Message" -ForegroundColor Blue
}

function Write-Success {
    param([string]$Message)
    Write-Host "[SUCCESS] $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') $Message" -ForegroundColor Green
}

function Write-Warn {
    param([string]$Message)
    Write-Host "[WARN] $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') $Message" -ForegroundColor Yellow
}

function Write-Error {
    param([string]$Message)
    Write-Host "[ERROR] $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') $Message" -ForegroundColor Red
}

# 显示帮助信息
function Show-Help {
    Write-Host "Usage: .\deploy.ps1 [OPTIONS]"
    Write-Host "ProManage自动化部署脚本"
    Write-Host ""
    Write-Host "Options:"
    Write-Host "  -Environment ENV          部署环境 (dev|test|prod) [默认: dev]"
    Write-Host "  -Version VERSION          应用版本 [默认: 1.1.0]"
    Write-Host "  -Registry REG             Docker镜像仓库 [默认: localhost:5000]"
    Write-Host "  -Build                    构建项目"
    Write-Host "  -Deploy                   部署应用"
    Write-Host "  -Migrate                  执行数据库迁移"
    Write-Host "  -Check                    健康检查"
    Write-Host "  -Rollback                 回滚到上一版本"
    Write-Host "  -Help                     显示帮助信息"
    Write-Host ""
    Write-Host "Examples:"
    Write-Host "  .\deploy.ps1 -Environment dev -Build -Deploy        构建并部署到开发环境"
    Write-Host "  .\deploy.ps1 -Environment prod -Migrate             在生产环境执行数据库迁移"
    Write-Host "  .\deploy.ps1 -Environment prod -Check               检查生产环境应用健康状态"
    Write-Host "  .\deploy.ps1 -Environment prod -Rollback            回滚生产环境到上一版本"
}

# 检查命令是否存在
function Test-Command {
    param([string]$Command)
    if (!(Get-Command $Command -ErrorAction SilentlyContinue)) {
        Write-Error "$Command 命令未找到，请先安装 $Command"
        exit 1
    }
}

# 检查必要工具
function Check-Prerequisites {
    Write-Info "检查必要工具..."
    Test-Command "mvn"
    Test-Command "docker"
    Test-Command "docker-compose"
    Test-Command "curl.exe"
    Write-Success "所有必要工具检查通过"
}

# 构建项目
function Build-Project {
    Write-Info "开始构建项目..."
    
    # 清理旧构建
    mvn clean
    
    # 执行构建
    if (mvn package -DskipTests) {
        Write-Success "项目构建成功"
        
        # 复制JAR文件到Docker构建目录
        Copy-Item "target\promanage.jar" "promanage.jar" -Force
        Write-Info "JAR文件已复制到Docker构建目录"
    } else {
        Write-Error "项目构建失败"
        exit 1
    }
}

# 构建Docker镜像
function Build-DockerImage {
    Write-Info "构建Docker镜像..."
    
    $imageName = "${Registry}/promanage-api:${Version}"
    
    if (docker build -t $imageName .) {
        Write-Success "Docker镜像构建成功: $imageName"
    } else {
        Write-Error "Docker镜像构建失败"
        exit 1
    }
}

# 推送Docker镜像到仓库
function Push-DockerImage {
    Write-Info "推送Docker镜像到仓库..."
    
    $imageName = "${Registry}/promanage-api:${Version}"
    
    if (docker push $imageName) {
        Write-Success "Docker镜像推送成功: $imageName"
    } else {
        Write-Error "Docker镜像推送失败"
        exit 1
    }
}

# 部署到开发/测试环境
function Deploy-DevTest {
    Write-Info "部署到$Environment环境..."
    
    # 使用docker-compose部署
    if (docker-compose up -d) {
        Write-Success "$Environment环境部署成功"
    } else {
        Write-Error "$Environment环境部署失败"
        exit 1
    }
}

# 部署到生产环境
function Deploy-Prod {
    Write-Info "部署到生产环境..."
    
    # 这里应该使用Kubernetes或类似工具进行部署
    # 为简化起见，我们使用docker-compose作为示例
    
    # 设置生产环境变量
    $env:SPRING_PROFILES_ACTIVE = "prod"
    
    if (docker-compose up -d) {
        Write-Success "生产环境部署成功"
    } else {
        Write-Error "生产环境部署失败"
        exit 1
    }
}

# 执行数据库迁移
function Run-Migrations {
    Write-Info "执行数据库迁移..."
    
    # 使用Flyway执行迁移
    if (mvn flyway:migrate) {
        Write-Success "数据库迁移执行成功"
    } else {
        Write-Error "数据库迁移执行失败"
        exit 1
    }
}

# 健康检查
function Health-Check {
    Write-Info "执行健康检查..."
    
    $maxAttempts = 30
    $attempt = 1
    $healthUrl = "http://localhost:8080/actuator/health"
    
    while ($attempt -le $maxAttempts) {
        Write-Info "尝试连接应用 (尝试 $attempt/$maxAttempts)..."
        
        try {
            $response = Invoke-WebRequest -Uri $healthUrl -Method Get -UseBasicParsing
            if ($response.StatusCode -eq 200) {
                Write-Success "应用健康检查通过"
                return
            }
        } catch {
            Write-Warn "应用未响应，等待5秒后重试..."
            Start-Sleep -Seconds 5
            $attempt++
        }
    }
    
    Write-Error "健康检查失败，应用在$maxAttempts次尝试后仍未响应"
    exit 1
}

# 回滚部署
function Rollback-Deployment {
    Write-Info "执行回滚操作..."
    
    # 这里应该实现具体的回滚逻辑
    # 例如：回滚到上一个Docker镜像版本
    
    Write-Warn "回滚功能需要根据具体部署环境实现"
    Write-Info "请参考部署文档执行手动回滚"
}

# 主函数
function Main {
    Write-Info "开始执行ProManage部署脚本 v1.1.0"
    
    # 显示帮助
    if ($Help) {
        Show-Help
        return
    }
    
    # 检查前提条件
    Check-Prerequisites
    
    # 根据参数执行相应操作
    if ($Build) {
        Build-Project
        Build-DockerImage
        Push-DockerImage
    }
    
    if ($Migrate) {
        Run-Migrations
    }
    
    if ($Deploy) {
        switch ($Environment) {
            "dev" { Deploy-DevTest }
            "test" { Deploy-DevTest }
            "prod" { Deploy-Prod }
            default { 
                Write-Error "未知环境: $Environment"
                exit 1
            }
        }
    }
    
    if ($Check) {
        Health-Check
    }
    
    if ($Rollback) {
        Rollback-Deployment
    }
    
    Write-Success "ProManage部署脚本执行完成"
}

# 执行主函数
Main