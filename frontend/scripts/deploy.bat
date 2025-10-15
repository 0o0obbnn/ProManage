@echo off
REM ProManage 前端部署脚本 (Windows)

echo 🚀 开始部署 ProManage 前端...

REM 设置环境
if not defined NODE_ENV set NODE_ENV=production
echo 📦 环境: %NODE_ENV%

REM 安装依赖
echo 📥 安装依赖...
call npm ci
if errorlevel 1 goto error

REM 运行测试
echo 🧪 运行测试...
call npm run test:run
if errorlevel 1 goto error

REM 类型检查
echo 🔍 类型检查...
call npm run type-check
if errorlevel 1 goto error

REM 构建
echo 🔨 构建生产版本...
call npm run build
if errorlevel 1 goto error

REM 检查构建产物
if not exist "dist" (
  echo ❌ 构建失败：dist目录不存在
  goto error
)

echo ✅ 构建完成
echo 🎉 部署完成！
goto end

:error
echo ❌ 部署失败
exit /b 1

:end
