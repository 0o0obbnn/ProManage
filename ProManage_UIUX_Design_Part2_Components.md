# ProManage UI/UX 设计文档 - Part 2: 组件库规范和交互模式

## 1. 组件库概述

### 1.1 组件分类
```
基础组件 (Foundation)
├── Button (按钮)
├── Input (输入框)
├── Select (选择器)
├── Checkbox (复选框)
├── Radio (单选框)
├── Switch (开关)
└── DatePicker (日期选择器)

导航组件 (Navigation)
├── Menu (菜单)
├── Tabs (标签页)
├── Breadcrumb (面包屑)
├── Pagination (分页)
└── Steps (步骤条)

数据展示 (Data Display)
├── Table (表格)
├── List (列表)
├── Card (卡片)
├── Tag (标签)
├── Badge (徽章)
├── Avatar (头像)
├── Timeline (时间轴)
└── Tree (树形控件)

反馈组件 (Feedback)
├── Alert (警告提示)
├── Message (全局提示)
├── Notification (通知提醒)
├── Modal (对话框)
├── Drawer (抽屉)
├── Progress (进度条)
├── Spin (加载中)
└── Skeleton (骨架屏)

业务组件 (Business)
├── ProjectCard (项目卡片)
├── DocumentViewer (文档查看器)
├── TaskBoard (任务看板)
├── ChangeTimeline (变更时间轴)
├── TestCaseEditor (测试用例编辑器)
├── UserSelector (用户选择器)
└── RichTextEditor (富文本编辑器)
```

---

## 2. 基础组件规范

### 2.1 Button (按钮)

#### 按钮类型
```
Primary (主要按钮)
- 背景色: Primary-600 (#1890ff)
- 文字色: #ffffff
- 悬停: Primary-500 (#40a9ff)
- 按下: Primary-700 (#096dd9)
- 禁用: 背景 #f5f5f5, 文字 #bfbfbf
- 用途: 主要操作，每个区域最多1个

Default (次要按钮)
- 背景色: #ffffff
- 边框: #d9d9d9
- 文字色: #262626
- 悬停: Primary-50 (#f0f9ff), 边框 Primary-600
- 用途: 次要操作

Dashed (虚线按钮)
- 背景色: #ffffff
- 边框: #d9d9d9 (虚线)
- 文字色: #262626
- 用途: 添加操作，如"+ 添加项目"

Text (文本按钮)
- 背景色: transparent
- 文字色: #262626
- 悬停: 背景 #f5f5f5
- 用途: 弱化操作，如"取消"

Link (链接按钮)
- 背景色: transparent
- 文字色: Primary-600
- 悬停: Primary-500, 下划线
- 用途: 跳转类操作

Danger (危险按钮)
- 背景色: Error-600 (#ff4d4f)
- 文字色: #ffffff
- 悬停: #ff7875
- 用途: 删除、清空等危险操作
```

#### 按钮尺寸
```
Large (大号)
- 高度: 40px
- 内边距: 6px 16px
- 字号: 16px
- 用途: 表单提交、页面主操作

Default (默认)
- 高度: 32px
- 内边距: 4px 16px
- 字号: 14px
- 用途: 常规操作（推荐）

Small (小号)
- 高度: 24px
- 内边距: 0px 8px
- 字号: 14px
- 用途: 表格内操作、紧凑布局
```

#### 按钮状态
```typescript
// Vue 组件示例
<template>
  <!-- 默认状态 -->
  <a-button type="primary">保存</a-button>

  <!-- 加载状态 -->
  <a-button type="primary" :loading="true">保存中...</a-button>

  <!-- 禁用状态 -->
  <a-button type="primary" :disabled="true">保存</a-button>

  <!-- 图标按钮 -->
  <a-button type="primary">
    <template #icon><PlusOutlined /></template>
    新建项目
  </a-button>

  <!-- 仅图标按钮 -->
  <a-button type="text" shape="circle">
    <template #icon><SearchOutlined /></template>
  </a-button>
</template>
```

#### 按钮组合规范
```
操作按钮排列顺序（从左到右）:
1. 主要操作（Primary）
2. 次要操作（Default）
3. 取消操作（Text/Link）

示例:
[保存] [保存并继续] 取消
[确定] 取消
[删除] [导出] 取消

按钮间距: 8px (Space-2)
```

---

### 2.2 Input (输入框)

#### 输入框类型
```
Text (文本输入)
- 单行文本输入
- 高度: 32px
- 内边距: 4px 12px
- 边框: 1px solid #d9d9d9
- 圆角: 4px

TextArea (多行文本)
- 多行文本输入
- 最小高度: 96px (3行)
- 可调整大小 (resize)
- 显示字数统计

Password (密码输入)
- 密码输入，带显示/隐藏切换
- 图标: EyeOutlined / EyeInvisibleOutlined

Search (搜索框)
- 带搜索图标
- 支持清除按钮
- Enter 键触发搜索
```

#### 输入框尺寸
```
Large: 40px (表单标题、重要输入)
Default: 32px (常规输入)
Small: 24px (表格内编辑、紧凑布局)
```

#### 输入框状态
```css
/* 默认状态 */
.ant-input {
  border-color: #d9d9d9;
  background: #ffffff;
}

/* 悬停状态 */
.ant-input:hover {
  border-color: #40a9ff;
}

/* 聚焦状态 */
.ant-input:focus {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
  outline: none;
}

/* 错误状态 */
.ant-input-status-error {
  border-color: #ff4d4f;
}
.ant-input-status-error:focus {
  box-shadow: 0 0 0 2px rgba(255, 77, 79, 0.2);
}

/* 禁用状态 */
.ant-input:disabled {
  background: #f5f5f5;
  color: #bfbfbf;
  cursor: not-allowed;
}
```

#### 输入框增强功能
```typescript
// 前缀/后缀图标
<a-input
  v-model:value="searchText"
  placeholder="搜索项目"
>
  <template #prefix>
    <SearchOutlined />
  </template>
  <template #suffix>
    <CloseCircleFilled @click="clearSearch" />
  </template>
</a-input>

// 字数统计
<a-textarea
  v-model:value="description"
  :maxlength="500"
  :showCount="true"
  placeholder="请输入项目描述"
/>

// 自动完成
<a-auto-complete
  v-model:value="projectName"
  :options="projectSuggestions"
  placeholder="项目名称"
/>
```

---

### 2.3 Select (选择器)

#### 选择器类型
```
Single Select (单选)
- 下拉选择单个选项
- 清除按钮（可选）

Multiple Select (多选)
- 下拉选择多个选项
- 显示已选标签
- 支持全选/清空

Searchable Select (可搜索)
- 支持搜索过滤选项
- 远程搜索（动态加载）

Cascader (级联选择)
- 多级关联选择
- 用途: 组织架构、地区选择

TreeSelect (树形选择)
- 树形结构选择
- 用途: 项目分类、权限选择
```

#### 选择器状态
```typescript
// 基础单选
<a-select
  v-model:value="projectType"
  placeholder="选择项目类型"
  style="width: 200px"
>
  <a-select-option value="web">Web应用</a-select-option>
  <a-select-option value="mobile">移动应用</a-select-option>
  <a-select-option value="backend">后端服务</a-select-option>
</a-select>

// 多选带搜索
<a-select
  v-model:value="selectedMembers"
  mode="multiple"
  placeholder="选择团队成员"
  :filter-option="filterOption"
  :max-tag-count="3"
  style="width: 100%"
>
  <a-select-option
    v-for="user in users"
    :key="user.id"
    :value="user.id"
  >
    <a-avatar :size="20" :src="user.avatar" />
    {{ user.name }}
  </a-select-option>
</a-select>

// 分组选择
<a-select placeholder="选择负责人">
  <a-select-opt-group label="开发团队">
    <a-select-option value="dev1">张三</a-select-option>
    <a-select-option value="dev2">李四</a-select-option>
  </a-select-opt-group>
  <a-select-opt-group label="测试团队">
    <a-select-option value="qa1">王五</a-select-option>
    <a-select-option value="qa2">赵六</a-select-option>
  </a-select-opt-group>
</a-select>
```

---

### 2.4 Form (表单)

#### 表单布局
```
Horizontal (水平布局)
- 标签与控件水平排列
- 标签宽度: 80-120px (根据内容)
- 适用: 桌面端标准表单

Vertical (垂直布局)
- 标签在控件上方
- 标签与控件左对齐
- 适用: 移动端、狭窄空间

Inline (行内布局)
- 表单项水平排列
- 适用: 搜索筛选、工具栏
```

#### 表单项规范
```typescript
// 标准表单示例
<a-form
  :model="formData"
  :rules="rules"
  :label-col="{ span: 4 }"
  :wrapper-col="{ span: 20 }"
>
  <!-- 必填项 -->
  <a-form-item
    label="项目名称"
    name="projectName"
    :rules="[{ required: true, message: '请输入项目名称' }]"
  >
    <a-input
      v-model:value="formData.projectName"
      placeholder="请输入项目名称"
      :maxlength="50"
    />
  </a-form-item>

  <!-- 可选项 -->
  <a-form-item label="项目描述" name="description">
    <a-textarea
      v-model:value="formData.description"
      placeholder="请输入项目描述（可选）"
      :rows="4"
      :maxlength="500"
      show-count
    />
  </a-form-item>

  <!-- 选择器 -->
  <a-form-item
    label="项目类型"
    name="projectType"
    :rules="[{ required: true, message: '请选择项目类型' }]"
  >
    <a-select
      v-model:value="formData.projectType"
      placeholder="请选择"
    >
      <a-select-option value="web">Web应用</a-select-option>
      <a-select-option value="mobile">移动应用</a-select-option>
    </a-select>
  </a-form-item>

  <!-- 日期选择 -->
  <a-form-item label="开始日期" name="startDate">
    <a-date-picker
      v-model:value="formData.startDate"
      style="width: 100%"
      placeholder="选择开始日期"
    />
  </a-form-item>

  <!-- 表单操作 -->
  <a-form-item :wrapper-col="{ offset: 4, span: 20 }">
    <a-space>
      <a-button type="primary" html-type="submit">
        提交
      </a-button>
      <a-button @click="handleReset">重置</a-button>
      <a-button type="link" @click="handleCancel">取消</a-button>
    </a-space>
  </a-form-item>
</a-form>
```

#### 表单验证规范
```typescript
// 验证规则配置
const rules = {
  // 必填验证
  projectName: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],

  // 邮箱验证
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],

  // 手机号验证
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],

  // 自定义验证
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    {
      validator: (rule, value) => {
        if (value.length < 8) {
          return Promise.reject('密码长度不能少于8位');
        }
        if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(value)) {
          return Promise.reject('密码必须包含大小写字母和数字');
        }
        return Promise.resolve();
      },
      trigger: 'blur'
    }
  ]
};

// 错误提示样式
.ant-form-item-has-error {
  .ant-input {
    border-color: #ff4d4f;
  }
  .ant-form-item-explain-error {
    color: #ff4d4f;
    font-size: 12px;
    margin-top: 4px;
  }
}
```

---

## 3. 导航组件规范

### 3.1 Menu (菜单)

#### 侧边栏导航
```typescript
// 主导航菜单配置
const menuItems = [
  {
    key: 'dashboard',
    icon: DashboardOutlined,
    label: '工作台',
    path: '/dashboard'
  },
  {
    key: 'projects',
    icon: ProjectOutlined,
    label: '项目管理',
    children: [
      { key: 'project-list', label: '项目列表', path: '/projects' },
      { key: 'project-create', label: '创建项目', path: '/projects/create' }
    ]
  },
  {
    key: 'documents',
    icon: FileTextOutlined,
    label: '文档中心',
    path: '/documents',
    badge: 5  // 未读数量
  },
  {
    key: 'tasks',
    icon: CheckSquareOutlined,
    label: '任务管理',
    path: '/tasks'
  },
  {
    key: 'changes',
    icon: SwapOutlined,
    label: '变更管理',
    path: '/changes'
  },
  {
    key: 'testing',
    icon: ExperimentOutlined,
    label: '测试管理',
    path: '/testing'
  },
  {
    key: 'analytics',
    icon: BarChartOutlined,
    label: '数据分析',
    path: '/analytics'
  }
];

// 侧边栏组件
<a-layout-sider
  v-model:collapsed="collapsed"
  :width="200"
  :collapsed-width="80"
  :trigger="null"
  collapsible
>
  <div class="logo">
    <img src="/logo.svg" alt="ProManage" />
    <span v-show="!collapsed">ProManage</span>
  </div>

  <a-menu
    v-model:selectedKeys="selectedKeys"
    v-model:openKeys="openKeys"
    mode="inline"
    theme="dark"
    :items="menuItems"
  />
</a-layout-sider>
```

#### 顶部导航
```typescript
// 顶部操作区
<a-layout-header class="header">
  <div class="header-left">
    <menu-unfold-outlined
      v-if="collapsed"
      @click="toggleCollapsed"
    />
    <menu-fold-outlined
      v-else
      @click="toggleCollapsed"
    />

    <a-breadcrumb :routes="breadcrumbRoutes">
      <template #itemRender="{ route }">
        <router-link :to="route.path">
          {{ route.breadcrumbName }}
        </router-link>
      </template>
    </a-breadcrumb>
  </div>

  <div class="header-right">
    <!-- 全局搜索 -->
    <a-input-search
      v-model:value="searchText"
      placeholder="搜索项目、文档、任务..."
      style="width: 300px"
      @search="handleSearch"
    />

    <!-- 通知中心 -->
    <a-badge :count="unreadCount" :overflow-count="99">
      <a-button type="text" shape="circle">
        <template #icon><BellOutlined /></template>
      </a-button>
    </a-badge>

    <!-- 用户菜单 -->
    <a-dropdown>
      <a-space class="user-info">
        <a-avatar :src="currentUser.avatar" />
        <span>{{ currentUser.name }}</span>
      </a-space>
      <template #overlay>
        <a-menu>
          <a-menu-item key="profile">
            <UserOutlined /> 个人中心
          </a-menu-item>
          <a-menu-item key="settings">
            <SettingOutlined /> 系统设置
          </a-menu-item>
          <a-menu-divider />
          <a-menu-item key="logout">
            <LogoutOutlined /> 退出登录
          </a-menu-item>
        </a-menu>
      </template>
    </a-dropdown>
  </div>
</a-layout-header>
```

#### 样式规范
```scss
// 侧边栏样式
.ant-layout-sider {
  background: #001529;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);

  .logo {
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #ffffff;
    font-size: 18px;
    font-weight: 600;

    img {
      width: 32px;
      height: 32px;
      margin-right: 8px;
    }
  }

  .ant-menu-dark {
    background: transparent;

    .ant-menu-item {
      height: 48px;
      line-height: 48px;
      margin: 4px 0;

      &:hover {
        background: rgba(255, 255, 255, 0.08);
      }

      &.ant-menu-item-selected {
        background: #1890ff;

        &::after {
          border-right: 3px solid #ffffff;
        }
      }
    }
  }
}

// 顶部导航样式
.header {
  background: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 16px;

    .user-info {
      cursor: pointer;
      padding: 4px 12px;
      border-radius: 4px;
      transition: background 0.2s;

      &:hover {
        background: #f5f5f5;
      }
    }
  }
}
```

---

### 3.2 Tabs (标签页)

#### 标签页类型
```typescript
// 基础标签页
<a-tabs v-model:activeKey="activeKey">
  <a-tab-pane key="overview" tab="项目概览">
    <ProjectOverview />
  </a-tab-pane>
  <a-tab-pane key="documents" tab="文档">
    <DocumentList />
  </a-tab-pane>
  <a-tab-pane key="tasks" tab="任务">
    <TaskBoard />
  </a-tab-pane>
  <a-tab-pane key="members" tab="成员">
    <MemberList />
  </a-tab-pane>
</a-tabs>

// 卡片式标签页
<a-tabs v-model:activeKey="activeKey" type="card">
  <a-tab-pane key="all" tab="全部文档" />
  <a-tab-pane key="my" tab="我的文档" />
  <a-tab-pane key="shared" tab="共享文档" />
</a-tabs>

// 可编辑标签页 (用于多文档编辑)
<a-tabs
  v-model:activeKey="activeKey"
  type="editable-card"
  @edit="handleEdit"
>
  <a-tab-pane
    v-for="doc in openedDocs"
    :key="doc.id"
    :tab="doc.name"
    :closable="true"
  >
    <DocumentEditor :document="doc" />
  </a-tab-pane>
</a-tabs>

// 带图标和徽章的标签页
<a-tabs v-model:activeKey="activeKey">
  <a-tab-pane key="pending">
    <template #tab>
      <a-badge :count="pendingCount" :offset="[10, 0]">
        <ClockCircleOutlined />
        <span>待处理</span>
      </a-badge>
    </template>
  </a-tab-pane>
  <a-tab-pane key="approved">
    <template #tab>
      <CheckCircleOutlined />
      <span>已通过</span>
    </template>
  </a-tab-pane>
</a-tabs>
```

---

### 3.3 Breadcrumb (面包屑)

```typescript
// 基础面包屑
<a-breadcrumb>
  <a-breadcrumb-item>
    <router-link to="/"><HomeOutlined /></router-link>
  </a-breadcrumb-item>
  <a-breadcrumb-item>
    <router-link to="/projects">项目管理</router-link>
  </a-breadcrumb-item>
  <a-breadcrumb-item>
    <router-link to="/projects/123">ProManage项目</router-link>
  </a-breadcrumb-item>
  <a-breadcrumb-item>文档列表</a-breadcrumb-item>
</a-breadcrumb>

// 带下拉菜单的面包屑
<a-breadcrumb>
  <a-breadcrumb-item>
    <HomeOutlined />
  </a-breadcrumb-item>
  <a-breadcrumb-item overlay>
    <template #overlay>
      <a-menu>
        <a-menu-item>项目列表</a-menu-item>
        <a-menu-item>创建项目</a-menu-item>
      </a-menu>
    </template>
    项目管理
  </a-breadcrumb-item>
  <a-breadcrumb-item>项目详情</a-breadcrumb-item>
</a-breadcrumb>
```

---

## 4. 数据展示组件

### 4.1 Table (表格)

#### 基础表格
```typescript
// 表格配置
const columns = [
  {
    title: '项目名称',
    dataIndex: 'name',
    key: 'name',
    width: 200,
    fixed: 'left',
    sorter: true,
    // 自定义渲染
    customRender: ({ record }) => (
      <router-link to={`/projects/${record.id}`}>
        {record.name}
      </router-link>
    )
  },
  {
    title: '负责人',
    dataIndex: 'owner',
    key: 'owner',
    width: 120,
    customRender: ({ record }) => (
      <a-space>
        <a-avatar size="small" src={record.owner.avatar} />
        <span>{record.owner.name}</span>
      </a-space>
    )
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100,
    filters: [
      { text: '进行中', value: 'active' },
      { text: '已完成', value: 'completed' },
      { text: '已归档', value: 'archived' }
    ],
    customRender: ({ text }) => {
      const statusConfig = {
        active: { color: 'blue', text: '进行中' },
        completed: { color: 'green', text: '已完成' },
        archived: { color: 'default', text: '已归档' }
      };
      const config = statusConfig[text];
      return <a-tag color={config.color}>{config.text}</a-tag>;
    }
  },
  {
    title: '创建时间',
    dataIndex: 'createdAt',
    key: 'createdAt',
    width: 180,
    sorter: true,
    customRender: ({ text }) => dayjs(text).format('YYYY-MM-DD HH:mm')
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right',
    customRender: ({ record }) => (
      <a-space>
        <a-button type="link" size="small" onClick={() => handleEdit(record)}>
          编辑
        </a-button>
        <a-button type="link" size="small" onClick={() => handleView(record)}>
          查看
        </a-button>
        <a-dropdown>
          <a-button type="link" size="small">
            更多 <DownOutlined />
          </a-button>
          <template #overlay>
            <a-menu>
              <a-menu-item key="archive">归档</a-menu-item>
              <a-menu-item key="delete" danger>删除</a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </a-space>
    )
  }
];

// 表格组件
<a-table
  :columns="columns"
  :data-source="dataSource"
  :loading="loading"
  :pagination="pagination"
  :row-selection="rowSelection"
  :scroll="{ x: 1200, y: 600 }"
  @change="handleTableChange"
>
  <!-- 表格顶部工具栏 -->
  <template #title>
    <div class="table-toolbar">
      <a-space>
        <a-button type="primary">
          <template #icon><PlusOutlined /></template>
          新建项目
        </a-button>
        <a-button>
          <template #icon><ExportOutlined /></template>
          导出
        </a-button>
      </a-space>

      <a-space>
        <a-input-search
          v-model:value="searchText"
          placeholder="搜索项目"
          style="width: 200px"
        />
        <a-button>
          <template #icon><FilterOutlined /></template>
          筛选
        </a-button>
      </a-space>
    </div>
  </template>
</a-table>
```

#### 表格样式规范
```scss
.ant-table {
  // 表头样式
  .ant-table-thead > tr > th {
    background: #fafafa;
    color: #262626;
    font-weight: 600;
    font-size: 14px;
    padding: 12px 16px;
    border-bottom: 1px solid #f0f0f0;
  }

  // 表格行样式
  .ant-table-tbody > tr {
    &:hover {
      background: #f5f5f5;
    }

    > td {
      padding: 12px 16px;
      border-bottom: 1px solid #f0f0f0;
    }
  }

  // 选中行样式
  .ant-table-tbody > tr.ant-table-row-selected > td {
    background: #e6f7ff;
  }
}

// 表格工具栏
.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
}
```

---

### 4.2 Card (卡片)

#### 卡片类型
```typescript
// 基础卡片
<a-card title="项目概览">
  <p>项目描述内容...</p>
</a-card>

// 带操作的卡片
<a-card title="项目名称">
  <template #extra>
    <a-space>
      <a-button type="link" size="small">编辑</a-button>
      <a-dropdown>
        <a-button type="text" size="small">
          <MoreOutlined />
        </a-button>
        <template #overlay>
          <a-menu>
            <a-menu-item>归档</a-menu-item>
            <a-menu-item danger>删除</a-menu-item>
          </a-menu>
        </template>
      </a-dropdown>
    </a-space>
  </template>

  <a-descriptions :column="2">
    <a-descriptions-item label="负责人">张三</a-descriptions-item>
    <a-descriptions-item label="状态">进行中</a-descriptions-item>
    <a-descriptions-item label="开始时间">2025-01-01</a-descriptions-item>
    <a-descriptions-item label="截止时间">2025-12-31</a-descriptions-item>
  </a-descriptions>
</a-card>

// 网格卡片 (项目列表)
<a-row :gutter="[16, 16]">
  <a-col
    v-for="project in projects"
    :key="project.id"
    :xs="24"
    :sm="12"
    :md="8"
    :lg="6"
  >
    <a-card
      :hoverable="true"
      class="project-card"
      @click="handleCardClick(project)"
    >
      <template #cover>
        <img :src="project.cover" :alt="project.name" />
      </template>

      <a-card-meta
        :title="project.name"
        :description="project.description"
      >
        <template #avatar>
          <a-avatar :src="project.owner.avatar" />
        </template>
      </a-card-meta>

      <template #actions>
        <span><StarOutlined /> {{ project.stars }}</span>
        <span><EyeOutlined /> {{ project.views }}</span>
        <span><TeamOutlined /> {{ project.members }}</span>
      </template>
    </a-card>
  </a-col>
</a-row>
```

---

### 4.3 List (列表)

```typescript
// 文档列表
<a-list
  :data-source="documents"
  :pagination="pagination"
  item-layout="horizontal"
>
  <template #renderItem="{ item }">
    <a-list-item>
      <template #actions>
        <a-button type="link" size="small">查看</a-button>
        <a-button type="link" size="small">编辑</a-button>
        <a-button type="link" size="small">下载</a-button>
      </template>

      <a-list-item-meta>
        <template #avatar>
          <FileTextOutlined style="font-size: 24px; color: #1890ff;" />
        </template>

        <template #title>
          <a @click="handleView(item)">{{ item.name }}</a>
        </template>

        <template #description>
          <a-space split="|">
            <span>{{ item.author }}</span>
            <span>{{ formatDate(item.updatedAt) }}</span>
            <span>{{ item.size }}</span>
          </a-space>
        </template>
      </a-list-item-meta>
    </a-list-item>
  </template>
</a-list>

// 活动时间轴列表
<a-list
  :data-source="activities"
  class="activity-list"
>
  <template #renderItem="{ item }">
    <a-list-item>
      <a-list-item-meta>
        <template #avatar>
          <a-avatar :src="item.user.avatar" />
        </template>

        <template #title>
          <span>{{ item.user.name }}</span>
          <span class="activity-action">{{ item.action }}</span>
          <span class="activity-target">{{ item.target }}</span>
        </template>

        <template #description>
          {{ formatTime(item.createdAt) }}
        </template>
      </a-list-item-meta>
    </a-list-item>
  </template>
</a-list>
```

---

## 5. 反馈组件规范

### 5.1 Message (全局提示)

```typescript
// 成功提示
message.success('保存成功');

// 错误提示
message.error('保存失败，请重试');

// 警告提示
message.warning('您有未保存的更改');

// 信息提示
message.info('数据正在同步中...');

// 加载提示
const hide = message.loading('正在处理中...', 0);
// 2秒后关闭
setTimeout(hide, 2000);

// 自定义时长和位置
message.success({
  content: '操作成功',
  duration: 3,
  top: '100px'
});
```

### 5.2 Notification (通知提醒)

```typescript
// 基础通知
notification.info({
  message: '新消息',
  description: '您有一条新的任务分配'
});

// 带图标的通知
notification.success({
  message: '构建成功',
  description: '项目已成功构建并部署到测试环境',
  icon: <CheckCircleOutlined style="color: #52c41a" />
});

// 带操作按钮的通知
notification.open({
  message: '代码审查请求',
  description: '张三请求您审查 PR #123',
  btn: (
    <a-space>
      <a-button type="primary" size="small" onClick={handleApprove}>
        批准
      </a-button>
      <a-button size="small" onClick={handleReject}>
        拒绝
      </a-button>
    </a-space>
  ),
  duration: 0,  // 不自动关闭
  placement: 'topRight'
});

// 系统通知
notification.warning({
  message: '系统维护通知',
  description: '系统将于今晚22:00-24:00进行维护，请提前保存您的工作',
  duration: 0,
  placement: 'top'
});
```

### 5.3 Modal (对话框)

```typescript
// 确认对话框
Modal.confirm({
  title: '确认删除',
  content: '删除后数据将无法恢复，确定要删除该项目吗？',
  okText: '确定',
  cancelText: '取消',
  okType: 'danger',
  onOk() {
    return handleDelete();
  }
});

// 信息对话框
Modal.info({
  title: '操作提示',
  content: '该操作需要项目管理员权限',
});

// 自定义对话框
<a-modal
  v-model:visible="visible"
  title="创建项目"
  :width="800"
  @ok="handleSubmit"
  @cancel="handleCancel"
>
  <a-form
    ref="formRef"
    :model="formData"
    :label-col="{ span: 6 }"
    :wrapper-col="{ span: 18 }"
  >
    <a-form-item label="项目名称" name="name">
      <a-input v-model:value="formData.name" />
    </a-form-item>
    <a-form-item label="项目描述" name="description">
      <a-textarea v-model:value="formData.description" :rows="4" />
    </a-form-item>
  </a-form>
</a-modal>

// 抽屉式对话框
<a-drawer
  v-model:visible="visible"
  title="项目详情"
  :width="720"
  placement="right"
>
  <ProjectDetail :project-id="currentProjectId" />
</a-drawer>
```

---

## 6. 交互模式规范

### 6.1 加载状态

```typescript
// 页面级加载
<a-spin :spinning="loading" size="large" tip="加载中...">
  <div class="content">
    <!-- 页面内容 -->
  </div>
</a-spin>

// 骨架屏加载
<a-skeleton v-if="loading" :paragraph="{ rows: 4 }" active />
<div v-else class="content">
  <!-- 实际内容 -->
</div>

// 按钮加载
<a-button type="primary" :loading="submitting" @click="handleSubmit">
  {{ submitting ? '提交中...' : '提交' }}
</a-button>

// 表格加载
<a-table
  :loading="loading"
  :data-source="dataSource"
  :columns="columns"
/>
```

### 6.2 空状态

```typescript
// 通用空状态
<a-empty
  v-if="!dataSource.length"
  description="暂无数据"
>
  <a-button type="primary">
    <template #icon><PlusOutlined /></template>
    创建第一个项目
  </a-button>
</a-empty>

// 自定义空状态
<a-empty
  v-if="!searchResults.length"
  :image="searchEmptyImage"
  description="未找到相关结果"
>
  <p>尝试使用其他关键词搜索</p>
</a-empty>

// 错误状态
<a-result
  status="error"
  title="加载失败"
  sub-title="抱歉，加载数据时出现错误"
>
  <template #extra>
    <a-button type="primary" @click="handleRetry">
      重新加载
    </a-button>
  </template>
</a-result>
```

### 6.3 拖拽排序

```typescript
// 任务拖拽排序
import { VueDraggable } from 'vue-draggable-plus';

<VueDraggable
  v-model="taskList"
  :animation="200"
  handle=".drag-handle"
  ghost-class="ghost"
  @end="handleDragEnd"
>
  <div
    v-for="task in taskList"
    :key="task.id"
    class="task-item"
  >
    <span class="drag-handle">
      <DragOutlined />
    </span>
    <span>{{ task.name }}</span>
  </div>
</VueDraggable>

<style>
.task-item {
  padding: 12px;
  background: #ffffff;
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  margin-bottom: 8px;
  cursor: move;
  transition: all 0.2s;

  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }
}

.ghost {
  opacity: 0.5;
  background: #f0f9ff;
}
</style>
```

### 6.4 批量操作

```typescript
// 表格批量操作
<template>
  <div class="batch-actions" v-if="selectedRowKeys.length > 0">
    <a-space>
      <span>已选择 {{ selectedRowKeys.length }} 项</span>
      <a-button @click="handleBatchEdit">批量编辑</a-button>
      <a-button @click="handleBatchExport">批量导出</a-button>
      <a-button danger @click="handleBatchDelete">批量删除</a-button>
      <a-button type="link" @click="handleClearSelection">清空</a-button>
    </a-space>
  </div>

  <a-table
    :row-selection="{
      selectedRowKeys,
      onChange: onSelectChange
    }"
    :data-source="dataSource"
    :columns="columns"
  />
</template>

<script setup>
const selectedRowKeys = ref([]);

const onSelectChange = (keys) => {
  selectedRowKeys.value = keys;
};

const handleBatchDelete = () => {
  Modal.confirm({
    title: '批量删除确认',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 项吗？`,
    okType: 'danger',
    onOk: async () => {
      await batchDelete(selectedRowKeys.value);
      message.success('删除成功');
      selectedRowKeys.value = [];
    }
  });
};
</script>
```

---

## 7. 性能优化指南

### 7.1 虚拟滚动
```typescript
// 大数据量列表使用虚拟滚动
<a-table
  :columns="columns"
  :data-source="dataSource"
  :virtual="true"
  :scroll="{ y: 600 }"
/>
```

### 7.2 懒加载
```typescript
// 图片懒加载
<img v-lazy="imageUrl" alt="项目封面" />

// 组件懒加载
const DocumentViewer = defineAsyncComponent(() =>
  import('./components/DocumentViewer.vue')
);
```

### 7.3 防抖节流
```typescript
import { useDebounceFn, useThrottleFn } from '@vueuse/core';

// 搜索防抖
const handleSearch = useDebounceFn((value) => {
  fetchSearchResults(value);
}, 300);

// 滚动节流
const handleScroll = useThrottleFn(() => {
  checkScrollPosition();
}, 100);
```

---

**文档版本**: v1.0
**创建日期**: 2025-09-30
**下一部分**: Part 3 - 用户角色界面设计规范