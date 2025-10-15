# ProManage 前端

基于 Vue 3 + TypeScript + Vite + Ant Design Vue 的现代化项目管理系统前端。

## 📋 目录

- [技术栈](#技术栈)
- [快速开始](#快速开始)
- [项目结构](#项目结构)
- [开发指南](#开发指南)
- [构建部署](#构建部署)
- [安全说明](#安全说明)

## 🛠 技术栈

- **框架**: Vue 3.5+ (Composition API)
- **语言**: TypeScript 5.8+
- **构建**: Vite 7.1+
- **UI库**: Ant Design Vue 4.2+
- **状态管理**: Pinia 3.0+
- **路由**: Vue Router 4.5+
- **HTTP**: Axios 1.12+
- **测试**: Vitest 2.1+

## 🚀 快速开始

### 环境要求

- Node.js >= 18.0.0
- npm >= 9.0.0

### 安装依赖

```bash
npm install
```

### 开发模式

```bash
npm run dev
```

访问 http://localhost:3000

### 构建生产版本

```bash
npm run build
```

### 预览生产版本

```bash
npm run preview
```

## 📁 项目结构

```
frontend/
├── public/              # 静态资源
├── src/
│   ├── api/            # API接口
│   ├── assets/         # 资源文件
│   ├── components/     # 组件
│   │   ├── base/      # 基础组件
│   │   ├── business/  # 业务组件
│   │   └── charts/    # 图表组件
│   ├── composables/    # 组合式函数
│   ├── config/         # 配置文件
│   ├── directives/     # 自定义指令
│   ├── layouts/        # 布局组件
│   ├── router/         # 路由配置
│   ├── stores/         # 状态管理
│   ├── types/          # 类型定义
│   ├── utils/          # 工具函数
│   ├── views/          # 页面组件
│   ├── App.vue         # 根组件
│   └── main.ts         # 入口文件
├── .env.development    # 开发环境变量
├── .env.production     # 生产环境变量
├── vite.config.ts      # Vite配置
└── package.json        # 项目配置
```

## 💻 开发指南

### 代码规范

```bash
# 代码检查
npm run lint

# 代码格式化
npm run format

# 类型检查
npm run type-check
```

### 测试

```bash
# 运行测试
npm run test

# 测试覆盖率
npm run test:coverage

# 测试UI
npm run test:ui
```

### 性能分析

```bash
# Lighthouse分析
npm run lighthouse

# 打包分析
npm run analyze
```

## 🔒 安全说明

### 环境变量配置

生产环境部署前必须修改以下配置：

1. **存储加密密钥**
```env
VITE_STORAGE_SECRET=your-secure-random-key-min-32-chars
```

2. **API地址**
```env
VITE_API_BASE_URL=https://your-api-domain.com/api/v1
VITE_WS_URL=wss://your-api-domain.com/ws
```

### 安全特性

- ✅ Token刷新竞态条件保护
- ✅ CSRF防护
- ✅ XSS防护
- ✅ 数据加密存储
- ✅ CSP内容安全策略
- ✅ 请求去重
- ✅ WebSocket自动重连

详见 [SECURITY_FIX_REPORT.md](./SECURITY_FIX_REPORT.md)

## 📦 构建部署

### 使用脚本部署

**Linux/Mac:**
```bash
chmod +x scripts/deploy.sh
./scripts/deploy.sh
```

**Windows:**
```bash
scripts\deploy.bat
```

### 手动部署

1. 安装依赖
```bash
npm ci
```

2. 运行测试
```bash
npm run test:run
```

3. 构建
```bash
npm run build
```

4. 部署dist目录到服务器

### Docker部署

```dockerfile
FROM node:18-alpine as builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

## 📊 性能指标

- **首屏加载**: < 3秒
- **Lighthouse Performance**: > 90
- **打包体积**: < 2MB
- **测试覆盖率**: > 40%

## 🔧 常见问题

### 1. 依赖安装失败

```bash
# 清除缓存
npm cache clean --force
rm -rf node_modules package-lock.json
npm install
```

### 2. 构建失败

```bash
# 检查Node版本
node -v  # 应该 >= 18.0.0

# 检查TypeScript错误
npm run type-check
```

### 3. 开发服务器启动失败

```bash
# 检查端口占用
netstat -ano | findstr :3000  # Windows
lsof -i :3000                 # Linux/Mac

# 修改端口
# 在vite.config.ts中修改server.port
```

## 📚 相关文档

- [前端开发计划](./FRONTEND_DEVELOPMENT_PLAN.md)
- [前端审查报告](./FRONTEND_AUDIT_REPORT.md)
- [安全修复报告](./SECURITY_FIX_REPORT.md)
- [性能优化报告](./PERFORMANCE_FIX_REPORT.md)
- [最终修复总结](./FINAL_FIX_SUMMARY.md)

## 📝 更新日志

### v1.1.0 (2025-01-11)

**安全增强**
- 实现Token刷新管理器
- 添加CSRF防护
- 实现数据加密存储
- 添加CSP配置

**稳定性提升**
- 修复路由守卫循环
- 添加全局错误处理
- 完善WebSocket重连

**性能优化**
- 实现请求去重
- 优化代码分割
- 添加虚拟列表

详见 [FINAL_FIX_SUMMARY.md](./FINAL_FIX_SUMMARY.md)

## 📞 技术支持

如有问题，请联系开发团队或查看相关文档。

## 📄 许可证

Copyright © 2025 ProManage Team
