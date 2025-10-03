# ProManage 前端项目

智能项目管理系统 - 前端应用

## 项目简介

ProManage 是一个基于 Vue 3 + TypeScript + Ant Design Vue 的现代化项目管理系统前端应用，提供文档管理、任务管理、变更管理、测试管理等核心功能。

## 技术栈

- **核心框架**: Vue 3.4+ (Composition API)
- **开发语言**: TypeScript 5.0+
- **构建工具**: Vite 5.0+
- **状态管理**: Pinia 2.0+
- **UI组件库**: Ant Design Vue 4.0+
- **路由管理**: Vue Router 4.0+
- **HTTP客户端**: Axios 1.6+
- **CSS方案**: SCSS + CSS Modules
- **代码规范**: ESLint + Prettier

## 系统要求

- Node.js: 18.0+
- npm: 9.0+

## 快速开始

### 1. 安装依赖

```bash
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

访问地址: http://localhost:5173

### 3. 构建生产版本

```bash
npm run build
```

### 4. 预览生产构建

```bash
npm run preview
```

## 项目结构

```
frontend/
├── public/                    # 静态资源
├── src/
│   ├── api/                  # API接口定义
│   │   ├── modules/          # 按模块划分
│   │   └── request.ts        # Axios封装
│   ├── assets/               # 资源文件
│   │   ├── images/
│   │   ├── styles/           # 全局样式
│   │   └── icons/
│   ├── components/           # 公共组件
│   │   ├── common/           # 基础组件
│   │   └── business/         # 业务组件
│   ├── composables/          # 组合式函数
│   ├── config/               # 配置文件
│   ├── directives/           # 自定义指令
│   ├── layouts/              # 布局组件
│   │   └── DefaultLayout.vue # 默认布局
│   ├── router/               # 路由配置
│   │   ├── index.ts          # 路由实例
│   │   └── guards.ts         # 路由守卫
│   ├── stores/               # Pinia状态管理
│   │   ├── modules/          # 状态模块
│   │   └── index.ts
│   ├── types/                # TypeScript类型定义
│   ├── utils/                # 工具函数
│   ├── views/                # 页面组件
│   │   ├── auth/             # 认证页面
│   │   ├── dashboard/        # 工作台
│   │   ├── document/         # 文档管理
│   │   ├── task/             # 任务管理
│   │   ├── change/           # 变更管理
│   │   └── error/            # 错误页面
│   ├── App.vue               # 根组件
│   └── main.ts               # 应用入口
├── .env.development          # 开发环境变量
├── .env.production           # 生产环境变量
├── .eslintrc.cjs             # ESLint配置
├── .prettierrc.json          # Prettier配置
├── tsconfig.json             # TypeScript配置
├── vite.config.ts            # Vite配置
└── package.json
```

## 核心功能

### 已实现功能

- ✅ 项目基础架构搭建
- ✅ Vue 3 + TypeScript + Vite 配置
- ✅ Ant Design Vue 集成和主题配置
- ✅ Vue Router 路由系统
- ✅ Pinia 状态管理
- ✅ Axios HTTP 客户端封装
- ✅ 设计系统（色彩、字体、间距）
- ✅ 布局组件（Header、Sidebar、Content）
- ✅ 登录认证模块
- ✅ 路由守卫和权限控制
- ✅ 响应式设计支持

### 待开发功能

- ⏳ 文档管理模块
- ⏳ 任务管理模块
- ⏳ 变更管理模块
- ⏳ 测试管理模块
- ⏳ 通知系统
- ⏳ 数据分析
- ⏳ 搜索功能
- ⏳ 用户设置

## 设计规范

项目遵循 ProManage UI/UX 设计规范：

- **色彩系统**: 主色调 #1890ff，支持7种用户角色主题色
- **字体系统**: 基准字号 14px，模块化比例 1.25
- **间距系统**: 基于 8px 网格系统
- **响应式断点**: xs/sm/md/lg/xl/xxl
- **性能目标**: 页面加载 < 3s，API响应 < 300ms

详细设计规范请参考项目根目录的 UI/UX 设计文档。

## 开发规范

### 代码风格

- 使用 ESLint + Prettier 自动格式化
- 组件命名采用 PascalCase
- 文件命名采用 kebab-case
- 使用 Composition API + `<script setup>`
- 严格的 TypeScript 类型检查

### Git 提交规范

```
feat: 新功能
fix: Bug修复
docs: 文档更新
style: 代码格式调整
refactor: 代码重构
perf: 性能优化
test: 测试相关
chore: 构建/工具链相关
```

## 环境变量

### 开发环境 (.env.development)

```env
VITE_APP_TITLE=ProManage
VITE_APP_ENV=development
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_WS_URL=ws://localhost:8080/ws
```

### 生产环境 (.env.production)

```env
VITE_APP_TITLE=ProManage
VITE_APP_ENV=production
VITE_API_BASE_URL=https://api.promanage.com/v1
VITE_WS_URL=wss://api.promanage.com/ws
```

## 常用命令

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览生产构建
npm run preview

# 代码检查
npm run lint

# 代码格式化
npm run format

# 类型检查
npm run type-check
```

## 浏览器支持

- Chrome: 最新版
- Firefox: 最新版
- Safari: 最新版
- Edge: 最新版

## 性能指标

- 首屏加载时间: < 3秒
- API 响应时间: < 300ms (P95)
- 页面切换: < 200ms
- 支持并发用户: 500+

## 可访问性

- 遵循 WCAG 2.1 AA 标准
- 支持键盘导航
- 屏幕阅读器友好
- 色彩对比度 ≥ 4.5:1

## 相关文档

- [产品需求文档](../ProManage_prd.md)
- [系统架构设计](../ProManage_System_Architecture.md)
- [工程规范](../ProManage_engineering_spec.md)
- [UI/UX 设计规范](../ProManage_UIUX_Design_Part1_DesignSystem.md)
- [API 接口规范](../ProManage_API_Specification.yaml)

## 许可证

MIT License

## 联系方式

- 项目主页: https://github.com/promanage/promanage
- 问题反馈: https://github.com/promanage/promanage/issues
- 邮箱: dev@promanage.com

---

**ProManage Team** © 2025