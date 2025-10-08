# ProManage 前端快速启动指南

## 🚀 快速开始

### 1. 安装依赖
```bash
cd frontend
npm install
```

### 2. 启动开发服务器
```bash
npm run dev
```

访问: http://localhost:5173

### 3. 运行测试
```bash
npm run test
```

---

## 📋 最新功能 (2025-10-05)

### ✅ 项目详情页 - 任务Tab
**路径**: `/projects/:id` → 任务列表Tab

**功能**:
- 📋 列表视图: 查看所有任务
- 📊 甘特图视图: 时间线展示
- 🔍 搜索任务
- 🏷️ 按状态/优先级筛选
- ➕ 创建新任务
- ✏️ 编辑任务
- 🗑️ 删除任务

**使用步骤**:
1. 登录系统
2. 进入项目列表
3. 点击任意项目进入详情
4. 点击"任务列表"Tab
5. 点击"新建任务"创建任务
6. 切换"列表"或"甘特图"视图

---

### ✅ 项目详情页 - 文档Tab
**路径**: `/projects/:id` → 文档Tab

**功能**:
- 📋 列表视图: 表格展示文档
- 🎴 网格视图: 卡片展示文档
- 🔍 搜索文档
- 🏷️ 按文件类型筛选
- ➕ 创建新文档
- 📤 上传文件
- 💾 下载文档
- 🗑️ 删除文档

**使用步骤**:
1. 登录系统
2. 进入项目列表
3. 点击任意项目进入详情
4. 点击"文档"Tab
5. 点击"新建文档"或"上传"
6. 切换"列表"或"网格"视图

---

## 🎯 核心页面导航

### 认证页面
- `/login` - 登录页面
- `/register` - 注册页面
- `/forgot-password` - 忘记密码

### 主要功能页面
- `/dashboard` - 仪表盘
- `/projects` - 项目列表
- `/projects/:id` - 项目详情
  - 概览 Tab
  - 看板 Tab
  - 任务列表 Tab ⭐ 新增
  - 文档 Tab ⭐ 新增
- `/tasks` - 任务管理
- `/documents` - 文档管理
- `/analytics` - 数据分析

---

## 🛠️ 开发命令

### 开发
```bash
npm run dev          # 启动开发服务器
npm run build        # 构建生产版本
npm run preview      # 预览生产构建
```

### 测试
```bash
npm run test         # 运行测试 (watch模式)
npm run test:run     # 运行测试 (单次)
npm run test:ui      # 测试UI界面
npm run coverage     # 生成覆盖率报告
```

### 代码质量
```bash
npm run lint         # ESLint检查
npm run lint:fix     # 自动修复
npm run type-check   # TypeScript类型检查
```

---

## 📁 项目结构

```
frontend/
├── src/
│   ├── api/                    # API接口
│   │   └── modules/
│   │       ├── project.ts      # 项目API
│   │       ├── task.ts         # 任务API
│   │       └── document.ts     # 文档API
│   ├── components/             # 公共组件
│   │   ├── business/           # 业务组件
│   │   ├── charts/             # 图表组件
│   │   └── common/             # 通用组件
│   ├── views/                  # 页面组件
│   │   ├── project/            # 项目相关页面
│   │   │   ├── index.vue       # 项目列表
│   │   │   ├── Detail.vue      # 项目详情
│   │   │   └── components/     # 项目子组件
│   │   │       ├── ProjectTaskList.vue      ⭐ 新增
│   │   │       └── ProjectDocumentList.vue  ⭐ 新增
│   │   ├── task/               # 任务相关页面
│   │   └── document/           # 文档相关页面
│   ├── stores/                 # Pinia状态管理
│   │   └── modules/
│   │       ├── project.ts
│   │       ├── task.ts
│   │       └── document.ts
│   ├── types/                  # TypeScript类型定义
│   ├── router/                 # 路由配置
│   └── utils/                  # 工具函数
├── tests/                      # 测试文件
└── public/                     # 静态资源
```

---

## 🔑 环境变量

创建 `.env.local` 文件:

```env
# API地址
VITE_API_BASE_URL=http://localhost:8080/api/v1

# WebSocket地址
VITE_WS_URL=ws://localhost:8080/ws

# 应用标题
VITE_APP_TITLE=ProManage

# 是否启用Mock数据
VITE_USE_MOCK=false
```

---

## 🎨 技术栈

### 核心框架
- **Vue 3** - 渐进式JavaScript框架
- **TypeScript** - 类型安全
- **Vite** - 快速构建工具

### UI组件库
- **Ant Design Vue** - 企业级UI组件库
- **@ant-design/icons-vue** - 图标库

### 状态管理
- **Pinia** - Vue官方状态管理库

### 路由
- **Vue Router** - 官方路由管理器

### HTTP客户端
- **Axios** - Promise based HTTP client

### 工具库
- **dayjs** - 日期处理
- **lodash-es** - 工具函数库

### 测试
- **Vitest** - 单元测试框架
- **@vue/test-utils** - Vue组件测试工具

---

## 📝 开发规范

### 命名规范
- **组件**: PascalCase (UserList.vue)
- **函数**: camelCase (getUserList)
- **常量**: UPPER_SNAKE_CASE (API_BASE_URL)
- **CSS类**: kebab-case (user-list)

### Git提交规范
```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 重构
test: 测试相关
chore: 构建/工具相关
```

### 代码风格
- 使用 Composition API
- 使用 `<script setup>` 语法
- 使用 TypeScript 类型注解
- 遵循 ESLint 规则

---

## 🐛 常见问题

### 1. 端口被占用
```bash
# 修改端口
vite --port 3000
```

### 2. 依赖安装失败
```bash
# 清除缓存重新安装
rm -rf node_modules package-lock.json
npm install
```

### 3. TypeScript类型错误
```bash
# 重新生成类型
npm run type-check
```

### 4. 测试失败
```bash
# 清除测试缓存
npm run test -- --clearCache
```

---

## 📚 学习资源

### 官方文档
- [Vue 3](https://cn.vuejs.org/)
- [Vite](https://cn.vitejs.dev/)
- [Ant Design Vue](https://antdv.com/)
- [Pinia](https://pinia.vuejs.org/zh/)
- [Vue Router](https://router.vuejs.org/zh/)

### 项目文档
- [前端开发计划](FRONTEND_DEVELOPMENT_PLAN.md)
- [任务3.4和3.5实现文档](TASK_3.4_3.5_IMPLEMENTATION.md)
- [UI/UX设计文档](../ProManage_UI_UX_Design_Document.md)

---

## 🤝 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'feat: Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

---

## 📞 联系方式

如有问题,请联系开发团队或提交 Issue。

---

**最后更新**: 2025-10-05  
**版本**: v1.0.0

