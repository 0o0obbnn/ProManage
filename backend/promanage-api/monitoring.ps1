# ProManage 分布式追踪和监控服务启动脚本 (PowerShell版本)
# Version: 1.0
# Author: ProManage Team

param(
    [string]$Command = "help",
    [string]$Service = "",
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

# 配置
$COMPOSE_FILE = "docker-compose-monitoring.yml"
$ZIPKIN_URL = "http://localhost:9411"
$PROMETHEUS_URL = "http://localhost:9090"
$GRAFANA_URL = "http://localhost:3000"

# 显示帮助信息
function Show-Help {
    Write-Host "Usage: .\monitoring.ps1 [COMMAND] [OPTIONS]"
    Write-Host "ProManage分布式追踪和监控服务管理脚本"
    Write-Host ""
    Write-Host "Commands:"
    Write-Host "  start     启动所有监控服务"
    Write-Host "  stop      停止所有监控服务"
    Write-Host "  restart   重启所有监控服务"
    Write-Host "  status    查看服务状态"
    Write-Host "  health    健康检查"
    Write-Host "  logs      查看服务日志"
    Write-Host "  clean     清理数据和日志"
    Write-Host "  help      显示帮助信息"
    Write-Host ""
    Write-Host "Options:"
    Write-Host "  -Service SERVICE  指定服务名称（用于logs命令）"
    Write-Host "  -Help              显示帮助信息"
    Write-Host ""
    Write-Host "Examples:"
    Write-Host "  .\monitoring.ps1 start     启动所有监控服务"
    Write-Host "  .\monitoring.ps1 status    查看服务状态"
    Write-Host "  .\monitoring.ps1 logs      查看服务日志"
}

# 检查必要工具
function Test-Prerequisites {
    Write-Info "检查必要工具..."
    
    try {
        $dockerVersion = docker --version
        Write-Info "Docker版本: $dockerVersion"
    } catch {
        Write-Error "Docker未安装，请先安装Docker"
        exit 1
    }
    
    try {
        $composeVersion = docker-compose --version
        Write-Info "Docker Compose版本: $composeVersion"
    } catch {
        Write-Error "Docker Compose未安装，请先安装Docker Compose"
        exit 1
    }
    
    Write-Success "所有必要工具检查通过"
}

# 启动监控服务
function Start-Services {
    Write-Info "启动ProManage监控服务..."
    
    # 检查配置文件是否存在
    if (-not (Test-Path $COMPOSE_FILE)) {
        Write-Error "Docker Compose文件不存在: $COMPOSE_FILE"
        exit 1
    }
    
    # 启动服务
    try {
        docker-compose -f $COMPOSE_FILE up -d
        Write-Success "监控服务启动成功"
        
        # 等待服务启动
        Write-Info "等待服务启动..."
        Start-Sleep -Seconds 10
        
        # 执行健康检查
        Test-Health
        
        # 显示访问信息
        Show-AccessInfo
        
    } catch {
        Write-Error "监控服务启动失败: $($_.Exception.Message)"
        exit 1
    }
}

# 停止监控服务
function Stop-Services {
    Write-Info "停止ProManage监控服务..."
    
    try {
        docker-compose -f $COMPOSE_FILE down
        Write-Success "监控服务停止成功"
    } catch {
        Write-Error "监控服务停止失败: $($_.Exception.Message)"
        exit 1
    }
}

# 重启监控服务
function Restart-Services {
    Write-Info "重启ProManage监控服务..."
    
    Stop-Services
    Start-Services
}

# 查看服务状态
function Get-ServiceStatus {
    Write-Info "查看ProManage监控服务状态..."
    
    Write-Host "服务状态:"
    docker-compose -f $COMPOSE_FILE ps
    
    Write-Host ""
    Write-Host "容器状态:"
    docker ps --filter "label=com.docker.compose.project=promanage" --format "table {{.Names}}	{{.Status}}	{{.Ports}}"
}

# 健康检查
function Test-Health {
    Write-Info "执行健康检查..."
    
    $services = @("zipkin", "prometheus", "grafana")
    $urls = @($ZIPKIN_URL + "/health", $PROMETHEUS_URL + "/-/healthy", $GRAFANA_URL + "/api/health")
    
    for ($i = 0; $i -lt $services.Count; $i++) {
        $service = $services[$i]
        $url = $urls[$i]
        
        Write-Info "检查$service服务..."
        
        for ($attempt = 1; $attempt -le 30; $attempt++) {
            try {
                $response = Invoke-WebRequest -Uri $url -Method Get -UseBasicParsing
                if ($response.StatusCode -eq 200) {
                    Write-Success "$service服务健康检查通过"
                    break
                }
            } catch {
                if ($attempt -eq 30) {
                    Write-Warn "$service服务健康检查失败，请稍后手动检查"
                } else {
                    Write-Info "$service服务检查中... (尝试 $attempt/30)"
                    Start-Sleep -Seconds 10
                }
            }
        }
    }
}

# 查看服务日志
function Get-ServiceLogs {
    param([string]$ServiceName = "")
    
    Write-Info "查看ProManage监控服务日志..."
    
    if ($ServiceName) {
        # 查看指定服务的日志
        docker-compose -f $COMPOSE_FILE logs -f $ServiceName
    } else {
        # 查看所有服务的日志
        docker-compose -f $COMPOSE_FILE logs -f
    }
}

# 清理数据和日志
function Clear-Services {
    Write-Warn "清理ProManage监控服务数据和日志..."
    
    # 停止服务
    Stop-Services
    
    # 删除数据卷
    try {
        docker-compose -f $COMPOSE_FILE down -v
        Write-Success "数据卷清理成功"
    } catch {
        Write-Error "数据卷清理失败: $($_.Exception.Message)"
    }
    
    # 清理Docker镜像（可选）
    $response = Read-Host "是否清理Docker镜像？(y/N)"
    if ($response -eq "y" -or $response -eq "Y") {
        Write-Info "清理Docker镜像..."
        docker image prune -f
        Write-Success "Docker镜像清理完成"
    }
}

# 显示访问信息
function Show-AccessInfo {
    Write-Info "监控服务访问信息："
    Write-Host "=================================="
    Write-Host "Zipkin追踪控制台: $ZIPKIN_URL"
    Write-Host "Prometheus指标控制台: $PROMETHEUS_URL"
    Write-Host "Grafana可视化控制台: $GRAFANA_URL"
    Write-Host "=================================="
    Write-Host ""
    Write-Host "默认凭据："
    Write-Host "Grafana: admin/admin"
    Write-Host ""
    Write-Host "ProManage API追踪配置："
    Write-Host "ZIPKIN_ENDPOINT=$ZIPKIN_URL/api/v2/spans"
    Write-Host "TRACING_SAMPLE_RATE=0.1"
    Write-Host "=================================="
}

# 主函数
function Main {
    Write-Info "开始执行$SCRIPT_NAME v$SCRIPT_VERSION"
    
    if ($Help) {
        Show-Help
        return
    }
    
    # 检查前提条件
    Test-Prerequisites
    
    # 解析命令
    switch ($Command.ToLower()) {
        "start" { Start-Services }
        "stop" { Stop-Services }
        "restart" { Restart-Services }
        "status" { Get-ServiceStatus }
        "health" { Test-Health }
        "logs" { Get-ServiceLogs -ServiceName $Service }
        "clean" { Clear-Services }
        "help" { Show-Help }
        default {
            Write-Error "未知命令: $Command"
            Show-Help
            exit 1
        }
    }
    
    Write-Success "$SCRIPT_NAME执行完成"
}

# 执行主函数
Main