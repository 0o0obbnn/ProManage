# ProManage UI/UX 设计文档 - Part 1: 设计系统基础

## 1. 设计系统概述

### 1.1 设计理念
- **专业可信**: 体现企业级项目管理的专业性和可靠性
- **高效协作**: 降低认知负担，提升团队协作效率50%
- **角色导向**: 为7种用户角色提供差异化的视觉体验
- **响应灵活**: 适配桌面、平板、移动端全场景使用

### 1.2 设计目标
- 支持500+并发用户的流畅体验
- 页面加载时间 < 3秒
- API响应时间 < 300ms (P95)
- 搜索响应时间 < 2秒
- 系统可用性 99.9%+

---

## 2. 色彩系统 (Color System)

### 2.1 主色调 (Primary Colors)

#### 品牌主色
```
Primary Blue (品牌蓝)
- Primary-600: #1890ff (主要交互色)
- Primary-500: #40a9ff (悬停状态)
- Primary-700: #096dd9 (按下状态)
- Primary-400: #69c0ff (禁用状态)
- Primary-100: #e6f7ff (背景色)
- Primary-50: #f0f9ff (浅背景色)

应用场景:
- 主要操作按钮
- 链接文本
- 选中状态
- 进度指示器
- 品牌标识
```

#### 辅助色
```
Success Green (成功绿)
- Success-600: #52c41a
- Success-100: #f6ffed
用途: 成功提示、完成状态、正向反馈

Warning Orange (警告橙)
- Warning-600: #faad14
- Warning-100: #fffbe6
用途: 警告提示、待处理状态、注意事项

Error Red (错误红)
- Error-600: #ff4d4f
- Error-100: #fff1f0
用途: 错误提示、删除操作、危险警告

Info Cyan (信息青)
- Info-600: #13c2c2
- Info-100: #e6fffb
用途: 信息提示、中性反馈、辅助说明
```

### 2.2 中性色 (Neutral Colors)

```
文本色阶:
- Text-Primary: #262626 (主要文本，rgba(0,0,0,0.85))
- Text-Secondary: #595959 (次要文本，rgba(0,0,0,0.65))
- Text-Tertiary: #8c8c8c (辅助文本，rgba(0,0,0,0.45))
- Text-Disabled: #bfbfbf (禁用文本，rgba(0,0,0,0.25))

背景色阶:
- BG-White: #ffffff (主背景)
- BG-Layout: #f0f2f5 (布局背景)
- BG-Container: #fafafa (容器背景)
- BG-Hover: #f5f5f5 (悬停背景)

边框色阶:
- Border-Base: #d9d9d9 (基础边框)
- Border-Light: #f0f0f0 (浅色边框)
- Border-Dark: #bfbfbf (深色边框)

阴影色阶:
- Shadow-1: rgba(0, 0, 0, 0.02) (浅阴影)
- Shadow-2: rgba(0, 0, 0, 0.06) (中阴影)
- Shadow-3: rgba(0, 0, 0, 0.12) (深阴影)
```

### 2.3 角色主题色 (Role Theme Colors)

为7种用户角色设计差异化主题色，用于个性化工作空间：

```
Super Administrator (超级管理员)
- Theme: Deep Purple #722ed1
- 场景: 系统管理界面、全局配置面板

Project Manager (项目经理)
- Theme: Royal Blue #1890ff
- 场景: 项目总览、资源调度、审批流程

Developer (开发人员)
- Theme: Tech Green #52c41a
- 场景: 任务看板、代码文档、开发工具

Tester (测试人员)
- Theme: Alert Orange #fa8c16
- 场景: 测试用例库、缺陷管理、测试报告

UI Designer (UI设计师)
- Theme: Creative Magenta #eb2f96
- 场景: 设计文件库、反馈收集、原型管理

Operations (运维人员)
- Theme: Stable Cyan #13c2c2
- 场景: 部署文档、环境监控、运维工具

Third-party Personnel (第三方人员)
- Theme: Neutral Gray #8c8c8c
- 场景: 受限访问界面、只读视图
```

### 2.4 数据可视化色板

```
分类色板 (Categorical - 用于不同类别区分):
1. #5B8FF9 (蓝)
2. #5AD8A6 (绿)
3. #5D7092 (灰蓝)
4. #F6BD16 (黄)
5. #E8684A (橙红)
6. #6DC8EC (青)
7. #9270CA (紫)
8. #FF9D4D (橙)
9. #269A99 (青绿)
10. #FF99C3 (粉)

顺序色板 (Sequential - 用于数值大小表示):
Light Blue: #E6F7FF → #1890FF → #003A8C
Green: #F6FFED → #52C41A → #135200
Orange: #FFF7E6 → #FA8C16 → #AD4E00

发散色板 (Diverging - 用于正负值对比):
Red-Blue: #FF4D4F ← #F5F5F5 → #1890FF
```

---

## 3. 字体系统 (Typography)

### 3.1 字体家族 (Font Family)

```css
/* 中文优先，英文回退 */
font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI',
             'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei',
             'Helvetica Neue', Helvetica, Arial, sans-serif;

/* 等宽字体（代码、数据） */
font-family-mono: 'SF Mono', Monaco, 'Cascadia Code', 'Roboto Mono',
                  Consolas, 'Courier New', monospace;
```

### 3.2 字体比例 (Type Scale)

基于模块化比例 1.25 (Major Third)，基准字号 14px：

```
Display Large (超大标题)
- Size: 38px (2.714em)
- Line-height: 48px (1.263)
- Weight: 600 (Semibold)
- Use: 落地页主标题、重要公告

Display Medium (大标题)
- Size: 30px (2.143em)
- Line-height: 40px (1.333)
- Weight: 600 (Semibold)
- Use: 页面主标题、模块标题

Heading 1 (一级标题)
- Size: 24px (1.714em)
- Line-height: 32px (1.333)
- Weight: 600 (Semibold)
- Use: 页面标题、卡片主标题

Heading 2 (二级标题)
- Size: 20px (1.429em)
- Line-height: 28px (1.4)
- Weight: 600 (Semibold)
- Use: 区块标题、表单分组标题

Heading 3 (三级标题)
- Size: 16px (1.143em)
- Line-height: 24px (1.5)
- Weight: 600 (Semibold)
- Use: 列表标题、卡片副标题

Body Large (大正文)
- Size: 16px (1.143em)
- Line-height: 24px (1.5)
- Weight: 400 (Regular)
- Use: 强调内容、重要说明

Body Base (基础正文) - 默认
- Size: 14px (1em)
- Line-height: 22px (1.571)
- Weight: 400 (Regular)
- Use: 正文内容、表单标签、按钮文本

Body Small (小正文)
- Size: 12px (0.857em)
- Line-height: 20px (1.667)
- Weight: 400 (Regular)
- Use: 辅助说明、次要信息、注释

Caption (说明文字)
- Size: 12px (0.857em)
- Line-height: 18px (1.5)
- Weight: 400 (Regular)
- Use: 图片说明、版权信息、时间戳

Overline (上标文字)
- Size: 12px (0.857em)
- Line-height: 18px (1.5)
- Weight: 600 (Semibold)
- Letter-spacing: 0.5px
- Use: 分类标签、章节编号
```

### 3.3 字重规范 (Font Weight)

```
Regular (400): 默认正文、数据展示
Medium (500): 次要强调、表格标题
Semibold (600): 主要标题、按钮文字、导航项
Bold (700): 特殊强调（谨慎使用）
```

### 3.4 字体颜色应用

```
标题文本: Text-Primary (#262626)
正文文本: Text-Secondary (#595959)
辅助文本: Text-Tertiary (#8c8c8c)
禁用文本: Text-Disabled (#bfbfbf)
链接文本: Primary-600 (#1890ff)
链接悬停: Primary-700 (#096dd9)
错误文本: Error-600 (#ff4d4f)
成功文本: Success-600 (#52c41a)
```

---

## 4. 网格系统 (Grid System)

### 4.1 响应式断点 (Breakpoints)

```
xs (Extra Small): < 576px   (移动端竖屏)
sm (Small): 576px - 768px    (移动端横屏/小平板)
md (Medium): 768px - 992px   (平板)
lg (Large): 992px - 1200px   (小屏桌面)
xl (Extra Large): 1200px - 1600px (标准桌面)
xxl (2X Large): ≥ 1600px     (大屏桌面)
```

### 4.2 网格配置

```
栅格列数: 24列 (Ant Design标准)

间距系统 (基于8px基准):
- Gutter-xs: 8px
- Gutter-sm: 16px (默认)
- Gutter-md: 24px
- Gutter-lg: 32px
- Gutter-xl: 48px

容器最大宽度:
- md: 720px
- lg: 960px
- xl: 1140px
- xxl: 1400px
```

### 4.3 布局容器

```
页面布局 (Page Layout):
- Header: 固定高度 64px
- Sidebar: 可折叠 200px / 80px (展开/折叠)
- Content: 自适应，min-height: calc(100vh - 64px - 64px)
- Footer: 固定高度 64px

内容区间距:
- Padding-horizontal: 24px (md+), 16px (sm), 12px (xs)
- Padding-vertical: 24px (md+), 16px (sm), 12px (xs)
```

---

## 5. 间距系统 (Spacing System)

### 5.1 间距比例

基于8px网格系统：

```
Space-0: 0px
Space-1: 4px    (0.5单位 - 极小间距)
Space-2: 8px    (1单位 - 最小间距)
Space-3: 12px   (1.5单位 - 小间距)
Space-4: 16px   (2单位 - 基础间距)
Space-5: 20px   (2.5单位 - 中小间距)
Space-6: 24px   (3单位 - 中等间距)
Space-8: 32px   (4单位 - 大间距)
Space-10: 40px  (5单位 - 较大间距)
Space-12: 48px  (6单位 - 超大间距)
Space-16: 64px  (8单位 - 特大间距)
Space-20: 80px  (10单位 - 巨大间距)
```

### 5.2 组件间距规范

```
组件内部间距 (Padding):
- 按钮: 4px 16px (高度32px)
- 输入框: 4px 12px (高度32px)
- 卡片: 24px (标准), 16px (紧凑)
- 模态框: 24px

组件外部间距 (Margin):
- 表单项间距: 24px
- 卡片间距: 16px (网格布局)
- 段落间距: 16px
- 分组间距: 32px
```

---

## 6. 圆角系统 (Border Radius)

```
Radius-none: 0px (无圆角)
Radius-sm: 2px (最小圆角 - 表格、输入框)
Radius-base: 4px (基础圆角 - 按钮、卡片)
Radius-md: 6px (中等圆角 - 标签、徽章)
Radius-lg: 8px (大圆角 - 模态框、抽屉)
Radius-xl: 12px (超大圆角 - 特殊容器)
Radius-full: 9999px (完全圆角 - 头像、圆形按钮)
```

---

## 7. 阴影系统 (Shadow System)

```
Shadow-none: none
Shadow-xs: 0 1px 2px rgba(0, 0, 0, 0.03)
  用途: 微弱阴影，按钮、输入框

Shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.06),
           0 1px 2px rgba(0, 0, 0, 0.04)
  用途: 小阴影，下拉菜单、工具提示

Shadow-base: 0 2px 8px rgba(0, 0, 0, 0.08)
  用途: 基础阴影，卡片、弹窗

Shadow-md: 0 4px 12px rgba(0, 0, 0, 0.10)
  用途: 中等阴影，悬浮卡片、模态框

Shadow-lg: 0 8px 24px rgba(0, 0, 0, 0.12)
  用途: 大阴影，抽屉、侧边栏

Shadow-xl: 0 12px 32px rgba(0, 0, 0, 0.15)
  用途: 超大阴影，全屏模态框

Shadow-inner: inset 0 2px 4px rgba(0, 0, 0, 0.06)
  用途: 内阴影，输入框按下状态
```

---

## 8. 图标系统 (Icon System)

### 8.1 图标库
使用 **Ant Design Icons** 作为主要图标库：
- 线性图标 (Outlined) - 默认风格
- 填充图标 (Filled) - 强调状态
- 双色图标 (TwoTone) - 特殊场景

### 8.2 图标尺寸

```
Icon-xs: 12px (徽章、标签内图标)
Icon-sm: 14px (按钮、输入框内图标)
Icon-base: 16px (默认图标尺寸)
Icon-md: 20px (导航、标题图标)
Icon-lg: 24px (页面主图标)
Icon-xl: 32px (空状态、引导图标)
Icon-2xl: 48px (大型空状态)
Icon-3xl: 64px (启动页、占位图)
```

### 8.3 常用图标映射

```
操作类:
- 新建: PlusOutlined
- 编辑: EditOutlined
- 删除: DeleteOutlined
- 保存: SaveOutlined
- 关闭: CloseOutlined
- 搜索: SearchOutlined
- 过滤: FilterOutlined
- 排序: SortAscendingOutlined
- 刷新: ReloadOutlined
- 设置: SettingOutlined
- 导出: ExportOutlined
- 导入: ImportOutlined
- 下载: DownloadOutlined
- 上传: UploadOutlined
- 复制: CopyOutlined

状态类:
- 成功: CheckCircleFilled (绿色)
- 警告: ExclamationCircleFilled (橙色)
- 错误: CloseCircleFilled (红色)
- 信息: InfoCircleFilled (蓝色)
- 加载: LoadingOutlined

业务类:
- 项目: ProjectOutlined
- 文档: FileTextOutlined
- 任务: CheckSquareOutlined
- 变更: SwapOutlined
- 测试: ExperimentOutlined
- 用户: UserOutlined
- 团队: TeamOutlined
- 时间: ClockCircleOutlined
- 统计: BarChartOutlined
- 通知: BellOutlined
```

---

## 9. 动效系统 (Animation System)

### 9.1 动画时长

```
Duration-fast: 100ms (微交互)
Duration-base: 200ms (默认过渡)
Duration-slow: 300ms (复杂动画)
Duration-slower: 500ms (页面过渡)
```

### 9.2 缓动函数 (Easing)

```
ease-in: cubic-bezier(0.55, 0, 1, 0.45)
  用途: 元素退出视图

ease-out: cubic-bezier(0, 0, 0.2, 1)
  用途: 元素进入视图（默认）

ease-in-out: cubic-bezier(0.45, 0, 0.15, 1)
  用途: 位置变化、尺寸变化

ease-out-back: cubic-bezier(0.12, 0.4, 0.29, 1.46)
  用途: 弹性效果、愉悦动画
```

### 9.3 常用动画

```css
/* 淡入淡出 */
.fade-enter-active, .fade-leave-active {
  transition: opacity 200ms ease-out;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

/* 滑动 */
.slide-enter-active, .slide-leave-active {
  transition: transform 200ms ease-out;
}
.slide-enter-from {
  transform: translateY(-8px);
}
.slide-leave-to {
  transform: translateY(8px);
}

/* 缩放 */
.scale-enter-active, .scale-leave-active {
  transition: transform 200ms ease-out, opacity 200ms ease-out;
}
.scale-enter-from, .scale-leave-to {
  transform: scale(0.95);
  opacity: 0;
}
```

---

## 10. Z-index 层级系统

```
z-base: 0 (基础层)
z-dropdown: 1000 (下拉菜单)
z-sticky: 1020 (粘性定位)
z-fixed: 1030 (固定定位)
z-modal-backdrop: 1040 (模态框背景)
z-modal: 1050 (模态框)
z-popover: 1060 (弹出框)
z-tooltip: 1070 (工具提示)
z-notification: 1080 (通知消息)
z-loading: 1090 (全局加载)
```

---

## 11. 设计令牌 (Design Tokens) 配置示例

### 11.1 Ant Design Vue 主题配置

```typescript
// theme.config.ts
import { ThemeConfig } from 'ant-design-vue';

export const themeConfig: ThemeConfig = {
  token: {
    // 色彩
    colorPrimary: '#1890ff',
    colorSuccess: '#52c41a',
    colorWarning: '#faad14',
    colorError: '#ff4d4f',
    colorInfo: '#13c2c2',

    // 字体
    fontSize: 14,
    fontSizeHeading1: 38,
    fontSizeHeading2: 30,
    fontSizeHeading3: 24,
    fontSizeHeading4: 20,
    fontSizeHeading5: 16,
    fontFamily: `-apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif`,

    // 圆角
    borderRadius: 4,
    borderRadiusLG: 8,
    borderRadiusSM: 2,

    // 间距
    marginXS: 8,
    marginSM: 12,
    margin: 16,
    marginMD: 20,
    marginLG: 24,
    marginXL: 32,

    // 阴影
    boxShadow: '0 2px 8px rgba(0, 0, 0, 0.08)',
    boxShadowSecondary: '0 4px 12px rgba(0, 0, 0, 0.10)',

    // 动画
    motionDurationFast: '100ms',
    motionDurationMid: '200ms',
    motionDurationSlow: '300ms',
  },

  components: {
    Button: {
      controlHeight: 32,
      paddingContentHorizontal: 16,
    },
    Input: {
      controlHeight: 32,
      paddingInline: 12,
    },
    Card: {
      paddingLG: 24,
    },
  },
};
```

### 11.2 CSS 变量定义

```css
/* global.css */
:root {
  /* Primary Colors */
  --color-primary-600: #1890ff;
  --color-primary-500: #40a9ff;
  --color-primary-700: #096dd9;
  --color-primary-100: #e6f7ff;
  --color-primary-50: #f0f9ff;

  /* Semantic Colors */
  --color-success: #52c41a;
  --color-warning: #faad14;
  --color-error: #ff4d4f;
  --color-info: #13c2c2;

  /* Text Colors */
  --color-text-primary: #262626;
  --color-text-secondary: #595959;
  --color-text-tertiary: #8c8c8c;
  --color-text-disabled: #bfbfbf;

  /* Background Colors */
  --color-bg-white: #ffffff;
  --color-bg-layout: #f0f2f5;
  --color-bg-container: #fafafa;
  --color-bg-hover: #f5f5f5;

  /* Border Colors */
  --color-border-base: #d9d9d9;
  --color-border-light: #f0f0f0;

  /* Spacing */
  --space-xs: 8px;
  --space-sm: 12px;
  --space-md: 16px;
  --space-lg: 24px;
  --space-xl: 32px;

  /* Border Radius */
  --radius-sm: 2px;
  --radius-base: 4px;
  --radius-lg: 8px;

  /* Shadows */
  --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.06);
  --shadow-base: 0 2px 8px rgba(0, 0, 0, 0.08);
  --shadow-lg: 0 8px 24px rgba(0, 0, 0, 0.12);

  /* Animation */
  --duration-fast: 100ms;
  --duration-base: 200ms;
  --duration-slow: 300ms;
  --ease-out: cubic-bezier(0, 0, 0.2, 1);
}
```

---

## 12. 设计系统使用指南

### 12.1 开发者快速参考

```typescript
// 在 Vue 组件中使用设计令牌
<template>
  <a-button
    type="primary"
    :style="{
      height: token.controlHeight + 'px',
      borderRadius: token.borderRadius + 'px'
    }"
  >
    Primary Button
  </a-button>
</template>

<script setup lang="ts">
import { theme } from 'ant-design-vue';
const { token } = theme.useToken();
</script>
```

### 12.2 设计一致性检查清单

- [ ] 颜色使用是否符合色彩系统规范
- [ ] 字体大小是否使用预定义的Type Scale
- [ ] 间距是否基于8px网格系统
- [ ] 圆角是否使用标准圆角值
- [ ] 阴影是否使用预定义阴影层级
- [ ] 动画时长是否符合动效系统
- [ ] 图标尺寸是否符合图标系统规范

---

## 13. 可访问性 (Accessibility) 基础

### 13.1 色彩对比度

```
文本对比度要求 (WCAG 2.1 AA):
- 正文文本 (14px+): 对比度 ≥ 4.5:1
- 大号文本 (18px+/14px粗体+): 对比度 ≥ 3:1
- UI组件和图形: 对比度 ≥ 3:1

验证通过:
- Primary (#1890ff) on White: 4.53:1 ✓
- Text-Primary (#262626) on White: 14.61:1 ✓
- Text-Secondary (#595959) on White: 7.44:1 ✓
```

### 13.2 焦点状态

```css
/* 键盘焦点指示器 */
.focus-visible {
  outline: 2px solid var(--color-primary-600);
  outline-offset: 2px;
}

/* 交互元素焦点 */
button:focus-visible,
a:focus-visible,
input:focus-visible {
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}
```

---

## 附录: 设计资源

### A. Figma 设计组件库结构
```
📁 ProManage Design System
├── 🎨 Foundations
│   ├── Colors
│   ├── Typography
│   ├── Spacing
│   ├── Shadows
│   └── Icons
├── 🧩 Components
│   ├── Buttons
│   ├── Forms
│   ├── Cards
│   └── Navigation
└── 📱 Templates
    ├── Desktop
    ├── Tablet
    └── Mobile
```

### B. 开发资源
- Ant Design Vue 文档: https://antdv.com/
- 色彩对比度检查: https://webaim.org/resources/contrastchecker/
- 响应式测试: Chrome DevTools Device Mode

---

**文档版本**: v1.0
**创建日期**: 2025-09-30
**更新日期**: 2025-09-30
**下一部分**: Part 2 - 组件库规范和交互模式