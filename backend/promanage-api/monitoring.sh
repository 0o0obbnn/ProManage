#!/bin/bash

# ProManage 分布式追踪和监控服务启动脚本
# Version: 1.0
# Author: ProManage Team

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 脚本信息
SCRIPT_NAME="ProManage Monitoring Services"
SCRIPT_VERSION="1.0"

# 默认配置
COMPOSE_FILE="docker-compose-monitoring.yml"
ZIPKIN_URL="http://localhost:9411"
PROMETHEUS_URL="http://localhost:9090"
GRAFANA_URL="http://localhost:3000"

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]$(date '+%Y-%m-%d %H:%M:%S')${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]$(date '+%Y-%m-%d %H:%M:%S')${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]$(date '+%Y-%m-%d %H:%M:%S')${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]$(date '+%Y-%m-%d %H:%M:%S')${NC} $1"
}

# 显示帮助信息
show_help() {
    echo "Usage: $0 [COMMAND]"
    echo "ProManage分布式追踪和监控服务管理脚本"
    echo ""
    echo "Commands:"
    echo "  start     启动所有监控服务"
    echo "  stop      停止所有监控服务"
    echo "  restart   重启所有监控服务"
    echo "  status    查看服务状态"
    echo "  health    健康检查"
    echo "  logs      查看服务日志"
    echo "  clean     清理数据和日志"
    echo "  help      显示帮助信息"
    echo ""
    echo "Examples:"
    echo "  $0 start     启动所有监控服务"
    echo "  $0 status    查看服务状态"
    echo "  $0 logs      查看服务日志"
}

# 检查必要工具
check_prerequisites() {
    log_info "检查必要工具..."
    
    if ! command -v docker &> /dev/null; then
        log_error "Docker未安装，请先安装Docker"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose未安装，请先安装Docker Compose"
        exit 1
    fi
    
    log_success "所有必要工具检查通过"
}

# 启动监控服务
start_services() {
    log_info "启动ProManage监控服务..."
    
    # 检查配置文件是否存在
    if [ ! -f "$COMPOSE_FILE" ]; then
        log_error "Docker Compose文件不存在: $COMPOSE_FILE"
        exit 1
    fi
    
    # 启动服务
    if docker-compose -f "$COMPOSE_FILE" up -d; then
        log_success "监控服务启动成功"
        
        # 等待服务启动
        log_info "等待服务启动..."
        sleep 10
        
        # 执行健康检查
        health_check
        
        # 显示访问信息
        show_access_info
        
    else
        log_error "监控服务启动失败"
        exit 1
    fi
}

# 停止监控服务
stop_services() {
    log_info "停止ProManage监控服务..."
    
    if docker-compose -f "$COMPOSE_FILE" down; then
        log_success "监控服务停止成功"
    else
        log_error "监控服务停止失败"
        exit 1
    fi
}

# 重启监控服务
restart_services() {
    log_info "重启ProManage监控服务..."
    
    stop_services
    start_services
}

# 查看服务状态
status_services() {
    log_info "查看ProManage监控服务状态..."
    
    echo "服务状态:"
    docker-compose -f "$COMPOSE_FILE" ps
    
    echo ""
    echo "容器状态:"
    docker ps --filter "label=com.docker.compose.project=promanage" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
}

# 健康检查
health_check() {
    log_info "执行健康检查..."
    
    local services=("zipkin" "prometheus" "grafana")
    local urls=("$ZIPKIN_URL/health" "$PROMETHEUS_URL/-/healthy" "$GRAFANA_URL/api/health")
    
    for i in "${!services[@]}"; do
        local service="${services[$i]}"
        local url="${urls[$i]}"
        
        log_info "检查$service服务..."
        
        for attempt in {1..30}; do
            if curl -f -s "$url" > /dev/null 2>&1; then
                log_success "$service服务健康检查通过"
                break
            else
                if [ $attempt -eq 30 ]; then
                    log_warn "$service服务健康检查失败，请稍后手动检查"
                else
                    log_info "$service服务检查中... (尝试 $attempt/30)"
                    sleep 10
                fi
            fi
        done
    done
}

# 查看服务日志
logs_services() {
    log_info "查看ProManage监控服务日志..."
    
    if [ -n "$1" ]; then
        # 查看指定服务的日志
        docker-compose -f "$COMPOSE_FILE" logs -f "$1"
    else
        # 查看所有服务的日志
        docker-compose -f "$COMPOSE_FILE" logs -f
    fi
}

# 清理数据和日志
clean_services() {
    log_warn "清理ProManage监控服务数据和日志..."
    
    # 停止服务
    stop_services
    
    # 删除数据卷
    if docker-compose -f "$COMPOSE_FILE" down -v; then
        log_success "数据卷清理成功"
    else
        log_error "数据卷清理失败"
    fi
    
    # 清理Docker镜像（可选）
    read -p "是否清理Docker镜像？(y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        log_info "清理Docker镜像..."
        docker image prune -f
        log_success "Docker镜像清理完成"
    fi
}

# 显示访问信息
show_access_info() {
    log_info "监控服务访问信息："
    echo "=================================="
    echo "Zipkin追踪控制台: $ZIPKIN_URL"
    echo "Prometheus指标控制台: $PROMETHEUS_URL"
    echo "Grafana可视化控制台: $GRAFANA_URL"
    echo "=================================="
    echo ""
    echo "默认凭据："
    echo "Grafana: admin/admin"
    echo ""
    echo "ProManage API追踪配置："
    echo "ZIPKIN_ENDPOINT=$ZIPKIN_URL/api/v2/spans"
    echo "TRACING_SAMPLE_RATE=0.1"
    echo "=================================="
}

# 主函数
main() {
    log_info "开始执行${SCRIPT_NAME} v${SCRIPT_VERSION}"
    
    # 检查前提条件
    check_prerequisites
    
    # 解析命令
    case "${1:-help}" in
        start)
            start_services
            ;;
        stop)
            stop_services
            ;;
        restart)
            restart_services
            ;;
        status)
            status_services
            ;;
        health)
            health_check
            ;;
        logs)
            logs_services "$2"
            ;;
        clean)
            clean_services
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            log_error "未知命令: $1"
            show_help
            exit 1
            ;;
    esac
    
    log_success "${SCRIPT_NAME}执行完成"
}

# 执行主函数
main "$@"