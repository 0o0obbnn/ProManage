#!/bin/bash

# ProManage 自动化部署脚本
# Version: 1.1.0
# Author: ProManage Team

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 脚本信息
SCRIPT_NAME="ProManage Deploy Script"
SCRIPT_VERSION="1.1.0"

# 默认配置
ENVIRONMENT="dev"
BUILD_PROFILE="dev"
DOCKER_REGISTRY="localhost:5000"
APP_NAME="promanage-api"
APP_VERSION="1.1.0"

# 显示帮助信息
show_help() {
    echo "Usage: $0 [OPTIONS]"
    echo "ProManage自动化部署脚本"
    echo ""
    echo "Options:"
    echo "  -e, --env ENV          部署环境 (dev|test|prod) [默认: dev]"
    echo "  -v, --version VERSION  应用版本 [默认: 1.1.0]"
    echo "  -r, --registry REG     Docker镜像仓库 [默认: localhost:5000]"
    echo "  -b, --build            构建项目"
    echo "  -d, --deploy           部署应用"
    echo "  -m, --migrate          执行数据库迁移"
    echo "  -c, --check            健康检查"
    echo "  -R, --rollback         回滚到上一版本"
    echo "  -h, --help             显示帮助信息"
    echo ""
    echo "Examples:"
    echo "  $0 -e dev -b -d        构建并部署到开发环境"
    echo "  $0 -e prod -m          在生产环境执行数据库迁移"
    echo "  $0 -e prod -c          检查生产环境应用健康状态"
    echo "  $0 -e prod -R          回滚生产环境到上一版本"
}

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

# 检查命令是否存在
check_command() {
    if ! command -v $1 &> /dev/null; then
        log_error "$1 命令未找到，请先安装 $1"
        exit 1
    fi
}

# 检查必要工具
check_prerequisites() {
    log_info "检查必要工具..."
    check_command "mvn"
    check_command "docker"
    check_command "docker-compose"
    check_command "curl"
    log_success "所有必要工具检查通过"
}

# 构建项目
build_project() {
    log_info "开始构建项目..."
    
    # 清理旧构建
    mvn clean
    
    # 执行构建
    if mvn package -DskipTests; then
        log_success "项目构建成功"
        
        # 复制JAR文件到Docker构建目录
        cp target/promanage.jar promanage.jar
        log_info "JAR文件已复制到Docker构建目录"
    else
        log_error "项目构建失败"
        exit 1
    fi
}

# 构建Docker镜像
build_docker_image() {
    log_info "构建Docker镜像..."
    
    local image_name="${DOCKER_REGISTRY}/${APP_NAME}:${APP_VERSION}"
    
    if docker build -t ${image_name} .; then
        log_success "Docker镜像构建成功: ${image_name}"
    else
        log_error "Docker镜像构建失败"
        exit 1
    fi
}

# 推送Docker镜像到仓库
push_docker_image() {
    log_info "推送Docker镜像到仓库..."
    
    local image_name="${DOCKER_REGISTRY}/${APP_NAME}:${APP_VERSION}"
    
    if docker push ${image_name}; then
        log_success "Docker镜像推送成功: ${image_name}"
    else
        log_error "Docker镜像推送失败"
        exit 1
    fi
}

# 部署到开发/测试环境
deploy_dev_test() {
    log_info "部署到${ENVIRONMENT}环境..."
    
    # 使用docker-compose部署
    if docker-compose up -d; then
        log_success "${ENVIRONMENT}环境部署成功"
    else
        log_error "${ENVIRONMENT}环境部署失败"
        exit 1
    fi
}

# 部署到生产环境
deploy_prod() {
    log_info "部署到生产环境..."
    
    # 这里应该使用Kubernetes或类似工具进行部署
    # 为简化起见，我们使用docker-compose作为示例
    
    # 设置生产环境变量
    export SPRING_PROFILES_ACTIVE=prod
    
    if docker-compose up -d; then
        log_success "生产环境部署成功"
    else
        log_error "生产环境部署失败"
        exit 1
    fi
}

# 执行数据库迁移
run_migrations() {
    log_info "执行数据库迁移..."
    
    # 使用Flyway执行迁移
    if mvn flyway:migrate; then
        log_success "数据库迁移执行成功"
    else
        log_error "数据库迁移执行失败"
        exit 1
    fi
}

# 健康检查
health_check() {
    log_info "执行健康检查..."
    
    local max_attempts=30
    local attempt=1
    local health_url="http://localhost:8080/actuator/health"
    
    while [ $attempt -le $max_attempts ]; do
        log_info "尝试连接应用 (尝试 $attempt/$max_attempts)..."
        
        if curl -f -s ${health_url} > /dev/null; then
            log_success "应用健康检查通过"
            return 0
        else
            log_warn "应用未响应，等待5秒后重试..."
            sleep 5
            ((attempt++))
        fi
    done
    
    log_error "健康检查失败，应用在${max_attempts}次尝试后仍未响应"
    exit 1
}

# 回滚部署
rollback() {
    log_info "执行回滚操作..."
    
    # 这里应该实现具体的回滚逻辑
    # 例如：回滚到上一个Docker镜像版本
    
    log_warn "回滚功能需要根据具体部署环境实现"
    log_info "请参考部署文档执行手动回滚"
}

# 主函数
main() {
    log_info "开始执行${SCRIPT_NAME} v${SCRIPT_VERSION}"
    
    # 检查前提条件
    check_prerequisites
    
    # 解析命令行参数
    BUILD=false
    DEPLOY=false
    MIGRATE=false
    CHECK=false
    ROLLBACK=false
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            -e|--env)
                ENVIRONMENT="$2"
                shift 2
                ;;
            -v|--version)
                APP_VERSION="$2"
                shift 2
                ;;
            -r|--registry)
                DOCKER_REGISTRY="$2"
                shift 2
                ;;
            -b|--build)
                BUILD=true
                shift
                ;;
            -d|--deploy)
                DEPLOY=true
                shift
                ;;
            -m|--migrate)
                MIGRATE=true
                shift
                ;;
            -c|--check)
                CHECK=true
                shift
                ;;
            -R|--rollback)
                ROLLBACK=true
                shift
                ;;
            -h|--help)
                show_help
                exit 0
                ;;
            *)
                log_error "未知参数: $1"
                show_help
                exit 1
                ;;
        esac
    done
    
    # 根据参数执行相应操作
    if [ "$BUILD" = true ]; then
        build_project
        build_docker_image
        push_docker_image
    fi
    
    if [ "$MIGRATE" = true ]; then
        run_migrations
    fi
    
    if [ "$DEPLOY" = true ]; then
        case $ENVIRONMENT in
            dev|test)
                deploy_dev_test
                ;;
            prod)
                deploy_prod
                ;;
            *)
                log_error "未知环境: $ENVIRONMENT"
                exit 1
                ;;
        esac
    fi
    
    if [ "$CHECK" = true ]; then
        health_check
    fi
    
    if [ "$ROLLBACK" = true ]; then
        rollback
    fi
    
    log_success "${SCRIPT_NAME}执行完成"
}

# 执行主函数
main "$@"