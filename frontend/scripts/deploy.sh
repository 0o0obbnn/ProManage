#!/bin/bash

# ProManage 前端部署脚本

set -e

echo "🚀 开始部署 ProManage 前端..."

# 检查环境
if [ -z "$NODE_ENV" ]; then
  export NODE_ENV=production
fi

echo "📦 环境: $NODE_ENV"

# 安装依赖
echo "📥 安装依赖..."
npm ci

# 运行测试
echo "🧪 运行测试..."
npm run test:run

# 类型检查
echo "🔍 类型检查..."
npm run type-check

# 构建
echo "🔨 构建生产版本..."
npm run build

# 检查构建产物
if [ ! -d "dist" ]; then
  echo "❌ 构建失败：dist目录不存在"
  exit 1
fi

echo "✅ 构建完成"

# 可选：上传到服务器
# echo "📤 上传到服务器..."
# rsync -avz --delete dist/ user@server:/var/www/promanage/

echo "🎉 部署完成！"
