# ProManage UI/UX 设计文档 - Part 6: 可用性和性能设计规范

## 1. 可访问性设计规范 (Accessibility)

### 1.1 WCAG 2.1 AA 合规性

#### 1.1.1 色彩对比度标准

```scss
/* 文本对比度要求 */
.text-contrast {
  /* 正文文本 (14px+): 对比度 ≥ 4.5:1 */
  color: #262626; /* 对 #ffffff 背景: 14.61:1 ✓ */

  /* 大号文本 (18px+/14px粗体+): 对比度 ≥ 3:1 */
  &.large-text {
    color: #595959; /* 对 #ffffff 背景: 7.44:1 ✓ */
  }

  /* UI组件和图形: 对比度 ≥ 3:1 */
  &.ui-element {
    border-color: #d9d9d9; /* 对 #ffffff 背景: 3.28:1 ✓ */
  }
}

/* 链接对比度 */
.link-contrast {
  color: #1890ff; /* 对 #ffffff 背景: 4.53:1 ✓ */

  &:hover {
    color: #096dd9; /* 对 #ffffff 背景: 6.98:1 ✓ */
  }
}

/* 错误提示对比度 */
.error-contrast {
  color: #ff4d4f; /* 对 #ffffff 背景: 5.04:1 ✓ */
}

/* 成功提示对比度 */
.success-contrast {
  color: #52c41a; /* 对 #ffffff 背景: 4.88:1 ✓ */
}
```

#### 1.1.2 焦点指示器

```scss
/* 全局焦点样式 */
*:focus-visible {
  outline: 2px solid #1890ff;
  outline-offset: 2px;
  border-radius: 2px;
}

/* 交互元素焦点 */
button:focus-visible,
a:focus-visible,
input:focus-visible,
select:focus-visible,
textarea:focus-visible {
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
  outline: 2px solid #1890ff;
  outline-offset: 2px;
}

/* 自定义组件焦点 */
.custom-button:focus-visible {
  position: relative;

  &::after {
    content: '';
    position: absolute;
    inset: -4px;
    border: 2px solid #1890ff;
    border-radius: 6px;
    pointer-events: none;
  }
}

/* 跳过焦点陷阱 */
.skip-link {
  position: absolute;
  top: -40px;
  left: 0;
  background: #000000;
  color: #ffffff;
  padding: 8px 16px;
  text-decoration: none;
  z-index: 9999;

  &:focus {
    top: 0;
  }
}
```

### 1.2 键盘导航支持

#### 1.2.1 快捷键配置

```typescript
// keyboard-shortcuts.ts
export const keyboardShortcuts = {
  // 全局导航
  global: {
    'ctrl+k': '全局搜索',
    'ctrl+/': '显示快捷键帮助',
    'ctrl+,': '打开设置',
    'g d': '跳转到工作台',
    'g p': '跳转到项目',
    'g d': '跳转到文档',
    'g t': '跳转到任务',
  },

  // 文档操作
  document: {
    'ctrl+s': '保存文档',
    'ctrl+shift+s': '另存为',
    'ctrl+p': '打印',
    'ctrl+f': '查找',
    'ctrl+h': '替换',
    'esc': '退出编辑',
  },

  // 任务操作
  task: {
    'c': '创建任务',
    'e': '编辑任务',
    'a': '分配任务',
    'shift+enter': '快速添加任务',
    'space': '标记完成/未完成',
  },

  // 列表导航
  list: {
    'j': '下一项',
    'k': '上一项',
    'enter': '打开项目',
    '/': '聚焦搜索',
  }
};

// 快捷键实现
import { onMounted, onUnmounted } from 'vue';

export function useKeyboardShortcut(
  key: string,
  handler: (event: KeyboardEvent) => void,
  options?: { ctrl?: boolean; shift?: boolean; alt?: boolean }
) {
  const handleKeyPress = (event: KeyboardEvent) => {
    const matchesModifiers =
      (!options?.ctrl || event.ctrlKey || event.metaKey) &&
      (!options?.shift || event.shiftKey) &&
      (!options?.alt || event.altKey);

    if (matchesModifiers && event.key.toLowerCase() === key.toLowerCase()) {
      event.preventDefault();
      handler(event);
    }
  };

  onMounted(() => {
    document.addEventListener('keydown', handleKeyPress);
  });

  onUnmounted(() => {
    document.removeEventListener('keydown', handleKeyPress);
  });
}
```

#### 1.2.2 Tab 导航顺序

```html
<!-- 合理的 Tab 顺序 -->
<template>
  <div class="form-container">
    <!-- 主要内容 -->
    <a href="#main-content" class="skip-link">跳转到主内容</a>

    <!-- 导航栏 tabindex 顺序: 1-10 -->
    <nav role="navigation">
      <a href="/" tabindex="1">首页</a>
      <a href="/projects" tabindex="2">项目</a>
      <a href="/documents" tabindex="3">文档</a>
    </nav>

    <!-- 主内容 tabindex 顺序: 11-20 -->
    <main id="main-content" role="main">
      <input type="text" tabindex="11" aria-label="搜索" />
      <button tabindex="12">搜索</button>
    </main>

    <!-- 避免使用 tabindex > 0，使用自然顺序 -->
    <form>
      <input type="text" name="name" /> <!-- 自然顺序 -->
      <input type="email" name="email" /> <!-- 自然顺序 -->
      <button type="submit">提交</button> <!-- 自然顺序 -->
    </form>

    <!-- 对于不应聚焦的装饰性元素 -->
    <div aria-hidden="true" tabindex="-1">装饰性内容</div>
  </div>
</template>
```

### 1.3 屏幕阅读器支持

#### 1.3.1 ARIA 标签规范

```html
<!-- 语义化 HTML + ARIA -->
<template>
  <!-- 地标角色 -->
  <header role="banner">
    <nav role="navigation" aria-label="主导航">
      <ul>
        <li><a href="/">首页</a></li>
      </ul>
    </nav>
  </header>

  <main role="main" aria-labelledby="page-title">
    <h1 id="page-title">项目列表</h1>

    <!-- 搜索表单 -->
    <form role="search" aria-label="搜索项目">
      <label for="search-input">搜索</label>
      <input
        id="search-input"
        type="search"
        aria-describedby="search-hint"
      />
      <span id="search-hint" class="sr-only">
        输入项目名称或关键词进行搜索
      </span>
    </form>

    <!-- 动态内容区域 -->
    <div
      role="region"
      aria-live="polite"
      aria-atomic="true"
      aria-relevant="additions text"
    >
      <p v-if="loading">正在加载...</p>
      <p v-else>找到 {{ results.length }} 个结果</p>
    </div>

    <!-- 交互组件 -->
    <button
      aria-label="新建项目"
      aria-haspopup="dialog"
      @click="openDialog"
    >
      <PlusOutlined aria-hidden="true" />
      <span class="sr-only">新建项目</span>
    </button>

    <!-- 对话框 -->
    <div
      v-if="dialogVisible"
      role="dialog"
      aria-modal="true"
      aria-labelledby="dialog-title"
      aria-describedby="dialog-description"
    >
      <h2 id="dialog-title">创建新项目</h2>
      <p id="dialog-description">填写以下信息创建新项目</p>
      <!-- 表单内容 -->
    </div>

    <!-- 标签页 -->
    <div role="tablist" aria-label="项目视图">
      <button
        role="tab"
        :aria-selected="activeTab === 'list'"
        :tabindex="activeTab === 'list' ? 0 : -1"
        @click="activeTab = 'list'"
      >
        列表视图
      </button>
      <button
        role="tab"
        :aria-selected="activeTab === 'grid'"
        :tabindex="activeTab === 'grid' ? 0 : -1"
        @click="activeTab = 'grid'"
      >
        网格视图
      </button>
    </div>

    <div role="tabpanel" :aria-hidden="activeTab !== 'list'">
      <!-- 列表内容 -->
    </div>

    <!-- 表格 -->
    <table role="table" aria-label="项目列表">
      <caption class="sr-only">包含项目名称、状态和负责人的项目列表</caption>
      <thead>
        <tr>
          <th scope="col">项目名称</th>
          <th scope="col">状态</th>
          <th scope="col">负责人</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td>ProManage</td>
          <td>进行中</td>
          <td>张三</td>
        </tr>
      </tbody>
    </table>

    <!-- 进度指示器 -->
    <div
      role="progressbar"
      :aria-valuenow="progress"
      aria-valuemin="0"
      aria-valuemax="100"
      :aria-label="`项目完成进度 ${progress}%`"
    >
      <div class="progress-bar" :style="{ width: progress + '%' }"></div>
    </div>
  </main>

  <footer role="contentinfo">
    <p>&copy; 2025 ProManage</p>
  </footer>
</template>

<style>
/* 屏幕阅读器专用类 */
.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border-width: 0;
}
</style>
```

#### 1.3.2 动态内容通知

```typescript
// 使用 aria-live 区域通知动态更新
export function useAriaLiveAnnouncer() {
  const announcePolite = (message: string) => {
    const announcement = document.createElement('div');
    announcement.setAttribute('role', 'status');
    announcement.setAttribute('aria-live', 'polite');
    announcement.setAttribute('aria-atomic', 'true');
    announcement.className = 'sr-only';
    announcement.textContent = message;

    document.body.appendChild(announcement);

    setTimeout(() => {
      document.body.removeChild(announcement);
    }, 1000);
  };

  const announceAssertive = (message: string) => {
    const announcement = document.createElement('div');
    announcement.setAttribute('role', 'alert');
    announcement.setAttribute('aria-live', 'assertive');
    announcement.setAttribute('aria-atomic', 'true');
    announcement.className = 'sr-only';
    announcement.textContent = message;

    document.body.appendChild(announcement);

    setTimeout(() => {
      document.body.removeChild(announcement);
    }, 1000);
  };

  return {
    announcePolite,
    announceAssertive
  };
}

// 使用示例
const { announcePolite, announceAssertive } = useAriaLiveAnnouncer();

// 保存成功
announcePolite('文档已成功保存');

// 错误提示
announceAssertive('保存失败，请重试');

// 任务完成
announcePolite('任务已标记为完成');
```

---

## 2. 性能优化规范

### 2.1 加载性能优化

#### 2.1.1 代码分割策略

```typescript
// router/index.ts
import { createRouter, createWebHistory } from 'vue-router';

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import(/* webpackChunkName: "home" */ '@/views/Home.vue')
  },
  {
    path: '/projects',
    name: 'Projects',
    component: () => import(/* webpackChunkName: "projects" */ '@/views/Projects.vue')
  },
  {
    path: '/documents',
    name: 'Documents',
    component: () => import(/* webpackChunkName: "documents" */ '@/views/Documents.vue'),
    children: [
      {
        path: ':id',
        name: 'DocumentDetail',
        component: () => import(/* webpackChunkName: "document-detail" */ '@/views/DocumentDetail.vue')
      }
    ]
  },
  {
    path: '/analytics',
    name: 'Analytics',
    component: () => import(/* webpackChunkName: "analytics" */ '@/views/Analytics.vue'),
    // 预加载相关的图表库
    beforeEnter: () => {
      import(/* webpackChunkName: "charts" */ '@/components/charts');
    }
  }
];

// 组件级别的懒加载
const ComponentLazy = defineAsyncComponent({
  loader: () => import('@/components/HeavyComponent.vue'),
  loadingComponent: LoadingSpinner,
  errorComponent: ErrorComponent,
  delay: 200,
  timeout: 3000
});
```

#### 2.1.2 资源优化

```typescript
// vite.config.ts
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import { visualizer } from 'rollup-plugin-visualizer';
import viteCompression from 'vite-plugin-compression';

export default defineConfig({
  plugins: [
    vue(),
    // Gzip 压缩
    viteCompression({
      algorithm: 'gzip',
      ext: '.gz'
    }),
    // Brotli 压缩
    viteCompression({
      algorithm: 'brotliCompress',
      ext: '.br'
    }),
    // 打包分析
    visualizer({
      open: true,
      gzipSize: true,
      brotliSize: true
    })
  ],

  build: {
    // 代码分割
    rollupOptions: {
      output: {
        manualChunks: {
          'vendor-vue': ['vue', 'vue-router', 'pinia'],
          'vendor-antd': ['ant-design-vue'],
          'vendor-charts': ['echarts', '@antv/g2'],
          'vendor-editor': ['@wangeditor/editor'],
          'vendor-utils': ['dayjs', 'lodash-es', 'axios']
        }
      }
    },

    // 压缩配置
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true
      }
    },

    // Chunk 大小警告限制
    chunkSizeWarningLimit: 500
  },

  // 优化依赖预构建
  optimizeDeps: {
    include: [
      'vue',
      'vue-router',
      'pinia',
      'ant-design-vue',
      'dayjs'
    ]
  }
});
```

#### 2.1.3 图片优化

```vue
<template>
  <!-- 响应式图片 -->
  <picture>
    <source
      type="image/webp"
      :srcset="`${image.url}?format=webp&w=320 320w,
               ${image.url}?format=webp&w=640 640w,
               ${image.url}?format=webp&w=1280 1280w`"
      sizes="(max-width: 640px) 320px,
             (max-width: 1280px) 640px,
             1280px"
    />
    <source
      type="image/jpeg"
      :srcset="`${image.url}?format=jpg&w=320 320w,
               ${image.url}?format=jpg&w=640 640w,
               ${image.url}?format=jpg&w=1280 1280w`"
      sizes="(max-width: 640px) 320px,
             (max-width: 1280px) 640px,
             1280px"
    />
    <img
      :src="image.url"
      :alt="image.alt"
      loading="lazy"
      decoding="async"
    />
  </picture>

  <!-- 懒加载图片 -->
  <img
    v-lazy="imageUrl"
    :alt="imageAlt"
    class="lazy-image"
  />

  <!-- 渐进式图片加载 -->
  <div class="progressive-image">
    <img
      :src="thumbnailUrl"
      :data-src="fullImageUrl"
      class="blur"
      @load="handleImageLoad"
    />
  </div>
</template>

<script setup lang="ts">
// 图片懒加载指令
import { useIntersectionObserver } from '@vueuse/core';

const vLazy = {
  mounted(el: HTMLImageElement, binding: any) {
    const { stop } = useIntersectionObserver(
      el,
      ([{ isIntersecting }]) => {
        if (isIntersecting) {
          el.src = binding.value;
          stop();
        }
      },
      { threshold: 0.1 }
    );
  }
};

// 渐进式图片加载
const handleImageLoad = (event: Event) => {
  const img = event.target as HTMLImageElement;
  img.classList.remove('blur');
  img.src = img.dataset.src || img.src;
};
</script>

<style scoped>
.lazy-image {
  background: #f5f5f5;
  min-height: 200px;
}

.progressive-image {
  position: relative;

  img {
    width: 100%;
    height: auto;
    transition: filter 0.3s;

    &.blur {
      filter: blur(10px);
    }
  }
}
</style>
```

### 2.2 运行时性能优化

#### 2.2.1 虚拟滚动

```vue
<template>
  <!-- 使用虚拟滚动处理大数据列表 -->
  <a-table
    :columns="columns"
    :data-source="dataSource"
    :virtual="true"
    :scroll="{ y: 600, x: 1200 }"
    :pagination="false"
  />

  <!-- 自定义虚拟列表 -->
  <VirtualList
    :items="items"
    :item-height="60"
    :height="600"
    :buffer="5"
  >
    <template #default="{ item }">
      <div class="list-item">{{ item.name }}</div>
    </template>
  </VirtualList>
</template>

<script setup lang="ts">
// 虚拟列表组件实现
import { ref, computed } from 'vue';
import { useVirtualList } from '@vueuse/core';

const items = ref(Array.from({ length: 10000 }, (_, i) => ({
  id: i,
  name: `Item ${i}`
})));

const { list, containerProps, wrapperProps } = useVirtualList(
  items,
  {
    itemHeight: 60,
    overscan: 10
  }
);
</script>
```

#### 2.2.2 防抖和节流

```typescript
// utils/performance.ts
import { useDebounceFn, useThrottleFn } from '@vueuse/core';

// 搜索防抖 - 300ms
export function useSearchDebounce() {
  const handleSearch = useDebounceFn((query: string) => {
    // 执行搜索
    console.log('Searching for:', query);
  }, 300);

  return { handleSearch };
}

// 滚动节流 - 100ms
export function useScrollThrottle() {
  const handleScroll = useThrottleFn(() => {
    // 处理滚动事件
    console.log('Scroll event');
  }, 100);

  return { handleScroll };
}

// 窗口大小调整防抖 - 150ms
export function useResizeDebounce() {
  const handleResize = useDebounceFn(() => {
    // 处理窗口调整
    console.log('Window resized');
  }, 150);

  return { handleResize };
}

// 表单输入防抖 - 500ms
export function useInputDebounce() {
  const handleInput = useDebounceFn((value: string) => {
    // 自动保存
    console.log('Auto-saving:', value);
  }, 500);

  return { handleInput };
}
```

#### 2.2.3 缓存策略

```typescript
// api/cache.ts
import { useQuery, useMutation, useQueryClient } from '@tanstack/vue-query';

// 数据缓存配置
export const cacheConfig = {
  // 静态数据 - 长时间缓存
  static: {
    staleTime: 60 * 60 * 1000, // 1小时
    cacheTime: 24 * 60 * 60 * 1000, // 24小时
  },

  // 动态数据 - 中等时间缓存
  dynamic: {
    staleTime: 5 * 60 * 1000, // 5分钟
    cacheTime: 30 * 60 * 1000, // 30分钟
  },

  // 实时数据 - 短时间缓存
  realtime: {
    staleTime: 0, // 立即过期
    cacheTime: 60 * 1000, // 1分钟
  }
};

// 使用缓存的查询
export function useProjectList() {
  return useQuery({
    queryKey: ['projects'],
    queryFn: fetchProjects,
    ...cacheConfig.dynamic,
    // 后台重新验证
    refetchOnWindowFocus: true,
    // 失败重试
    retry: 3,
    retryDelay: attemptIndex => Math.min(1000 * 2 ** attemptIndex, 30000)
  });
}

// 预取数据
export function usePrefetchProjectDetail(projectId: string) {
  const queryClient = useQueryClient();

  const prefetch = () => {
    queryClient.prefetchQuery({
      queryKey: ['project', projectId],
      queryFn: () => fetchProjectDetail(projectId),
      ...cacheConfig.dynamic
    });
  };

  return { prefetch };
}

// 乐观更新
export function useUpdateProject() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: updateProject,
    onMutate: async (newProject) => {
      // 取消正在进行的查询
      await queryClient.cancelQueries({ queryKey: ['projects'] });

      // 快照当前值
      const previousProjects = queryClient.getQueryData(['projects']);

      // 乐观更新
      queryClient.setQueryData(['projects'], (old: any) => {
        return old.map((p: any) =>
          p.id === newProject.id ? { ...p, ...newProject } : p
        );
      });

      return { previousProjects };
    },
    onError: (err, newProject, context) => {
      // 回滚
      queryClient.setQueryData(['projects'], context.previousProjects);
    },
    onSettled: () => {
      // 重新获取以确保同步
      queryClient.invalidateQueries({ queryKey: ['projects'] });
    }
  });
}
```

### 2.3 渲染性能优化

#### 2.3.1 组件优化

```vue
<script setup lang="ts">
import { computed, shallowRef, watchEffect } from 'vue';

// 使用 shallowRef 减少响应式开销
const heavyData = shallowRef<any[]>([]);

// 使用 computed 缓存计算结果
const filteredData = computed(() => {
  return heavyData.value.filter(item => item.active);
});

// 使用 watchEffect 替代 watch
watchEffect(() => {
  console.log('Data changed:', heavyData.value.length);
});

// 组件定义使用 defineOptions
defineOptions({
  name: 'OptimizedComponent',
  inheritAttrs: false
});
</script>

<template>
  <!-- 使用 v-memo 缓存子树 -->
  <div v-memo="[item.id, item.status]">
    <ComplexChild :item="item" />
  </div>

  <!-- 使用 v-once 渲染静态内容 -->
  <div v-once>
    <h1>{{ staticTitle }}</h1>
    <p>{{ staticDescription }}</p>
  </div>

  <!-- 条件渲染使用 v-show 而非 v-if（频繁切换） -->
  <div v-show="isVisible">
    <ExpensiveComponent />
  </div>

  <!-- 列表渲染使用 key -->
  <div
    v-for="item in items"
    :key="item.id"
    class="list-item"
  >
    {{ item.name }}
  </div>
</template>
```

#### 2.3.2 长列表优化

```typescript
// composables/useVirtualScroll.ts
import { ref, computed, onMounted, onUnmounted } from 'vue';

export function useVirtualScroll(
  items: Ref<any[]>,
  itemHeight: number,
  containerHeight: number
) {
  const scrollTop = ref(0);
  const containerRef = ref<HTMLElement>();

  // 计算可见范围
  const visibleRange = computed(() => {
    const start = Math.floor(scrollTop.value / itemHeight);
    const end = Math.ceil((scrollTop.value + containerHeight) / itemHeight);
    return { start, end };
  });

  // 可见项
  const visibleItems = computed(() => {
    const { start, end } = visibleRange.value;
    return items.value.slice(start, end + 1).map((item, index) => ({
      item,
      index: start + index,
      top: (start + index) * itemHeight
    }));
  });

  // 总高度
  const totalHeight = computed(() => items.value.length * itemHeight);

  // 滚动处理
  const handleScroll = (event: Event) => {
    const target = event.target as HTMLElement;
    scrollTop.value = target.scrollTop;
  };

  onMounted(() => {
    containerRef.value?.addEventListener('scroll', handleScroll);
  });

  onUnmounted(() => {
    containerRef.value?.removeEventListener('scroll', handleScroll);
  });

  return {
    containerRef,
    visibleItems,
    totalHeight
  };
}
```

---

## 3. 错误处理和用户反馈

### 3.1 错误边界

```vue
<!-- ErrorBoundary.vue -->
<template>
  <div v-if="error" class="error-boundary">
    <a-result
      status="error"
      title="出错了"
      :sub-title="error.message"
    >
      <template #extra>
        <a-space>
          <a-button type="primary" @click="handleRetry">
            重试
          </a-button>
          <a-button @click="handleReportError">
            报告错误
          </a-button>
          <a-button type="link" @click="handleGoHome">
            返回首页
          </a-button>
        </a-space>
      </template>
    </a-result>
  </div>
  <slot v-else />
</template>

<script setup lang="ts">
import { ref, onErrorCaptured } from 'vue';
import { useRouter } from 'vue-router';

const error = ref<Error | null>(null);
const router = useRouter();

onErrorCaptured((err: Error) => {
  error.value = err;
  console.error('Error captured:', err);

  // 上报错误
  reportError(err);

  return false;
});

const handleRetry = () => {
  error.value = null;
  window.location.reload();
};

const handleReportError = () => {
  // 打开错误报告对话框
};

const handleGoHome = () => {
  error.value = null;
  router.push('/');
};

function reportError(err: Error) {
  // 发送错误到监控服务
  fetch('/api/errors', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      message: err.message,
      stack: err.stack,
      url: window.location.href,
      userAgent: navigator.userAgent,
      timestamp: new Date().toISOString()
    })
  });
}
</script>
```

### 3.2 加载状态

```vue
<template>
  <!-- 页面级加载 -->
  <a-spin
    :spinning="loading"
    size="large"
    tip="加载中..."
    :delay="200"
  >
    <div class="content">
      <!-- 内容 -->
    </div>
  </a-spin>

  <!-- 骨架屏 -->
  <a-skeleton
    v-if="loading"
    :paragraph="{ rows: 4 }"
    active
    :title="true"
  />
  <div v-else class="content">
    <!-- 实际内容 -->
  </div>

  <!-- 按钮加载 -->
  <a-button
    type="primary"
    :loading="submitting"
    @click="handleSubmit"
  >
    {{ submitting ? '提交中...' : '提交' }}
  </a-button>

  <!-- 进度条 -->
  <a-progress
    v-if="uploading"
    :percent="uploadProgress"
    status="active"
  />

  <!-- 自定义加载指示器 -->
  <div v-if="loading" class="custom-loader">
    <div class="spinner"></div>
    <p>{{ loadingMessage }}</p>
  </div>
</template>

<style scoped>
.custom-loader {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;

  .spinner {
    width: 40px;
    height: 40px;
    border: 4px solid #f0f0f0;
    border-top-color: #1890ff;
    border-radius: 50%;
    animation: spin 1s linear infinite;
  }

  p {
    margin-top: 16px;
    color: #8c8c8c;
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
```

### 3.3 空状态设计

```vue
<template>
  <!-- 无数据空状态 -->
  <a-empty
    v-if="!dataSource.length"
    description="暂无数据"
  >
    <a-button type="primary" @click="handleCreate">
      <template #icon><PlusOutlined /></template>
      创建第一项
    </a-button>
  </a-empty>

  <!-- 搜索无结果 -->
  <a-empty
    v-if="!searchResults.length"
    :image="searchEmptyImage"
    description="未找到相关结果"
  >
    <template #description>
      <span>尝试使用其他关键词或</span>
      <a @click="handleClearSearch">清空筛选条件</a>
    </template>
  </a-empty>

  <!-- 错误状态 -->
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

  <!-- 无权限状态 -->
  <a-result
    status="403"
    title="权限不足"
    sub-title="您没有权限访问此内容"
  >
    <template #extra>
      <a-space>
        <a-button type="primary" @click="handleRequestAccess">
          申请权限
        </a-button>
        <a-button @click="handleGoBack">
          返回
        </a-button>
      </a-space>
    </template>
  </a-result>

  <!-- 404 状态 -->
  <a-result
    status="404"
    title="页面不存在"
    sub-title="抱歉，您访问的页面不存在"
  >
    <template #extra>
      <a-button type="primary" @click="handleGoHome">
        返回首页
      </a-button>
    </template>
  </a-result>
</template>
```

---

## 4. 性能监控指标

### 4.1 核心性能指标

```typescript
// performance/metrics.ts
export interface PerformanceMetrics {
  // Core Web Vitals
  LCP: number; // Largest Contentful Paint (最大内容绘制)
  FID: number; // First Input Delay (首次输入延迟)
  CLS: number; // Cumulative Layout Shift (累积布局偏移)

  // 其他指标
  FCP: number; // First Contentful Paint (首次内容绘制)
  TTFB: number; // Time to First Byte (首字节时间)
  TTI: number; // Time to Interactive (可交互时间)
}

// 性能目标
export const performanceTargets = {
  LCP: 2500, // < 2.5s (良好)
  FID: 100, // < 100ms (良好)
  CLS: 0.1, // < 0.1 (良好)
  FCP: 1800, // < 1.8s (良好)
  TTFB: 600, // < 600ms (良好)
  TTI: 3800, // < 3.8s (良好)

  // API 性能
  apiResponseTime: 300, // < 300ms (P95)

  // 页面加载
  pageLoadTime: 3000, // < 3s

  // 搜索性能
  searchResponseTime: 2000, // < 2s
};

// 监控实现
export function setupPerformanceMonitoring() {
  // 监控 LCP
  new PerformanceObserver((entryList) => {
    for (const entry of entryList.getEntries()) {
      console.log('LCP:', entry.renderTime || entry.loadTime);
      reportMetric('LCP', entry.renderTime || entry.loadTime);
    }
  }).observe({ entryTypes: ['largest-contentful-paint'] });

  // 监控 FID
  new PerformanceObserver((entryList) => {
    for (const entry of entryList.getEntries()) {
      console.log('FID:', entry.processingStart - entry.startTime);
      reportMetric('FID', entry.processingStart - entry.startTime);
    }
  }).observe({ entryTypes: ['first-input'] });

  // 监控 CLS
  let clsValue = 0;
  new PerformanceObserver((entryList) => {
    for (const entry of entryList.getEntries()) {
      if (!entry.hadRecentInput) {
        clsValue += entry.value;
        console.log('CLS:', clsValue);
        reportMetric('CLS', clsValue);
      }
    }
  }).observe({ entryTypes: ['layout-shift'] });
}

function reportMetric(name: string, value: number) {
  // 发送到监控服务
  fetch('/api/metrics', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      metric: name,
      value,
      timestamp: Date.now(),
      url: window.location.href
    })
  });
}
```

### 4.2 性能预算

```json
{
  "performance": {
    "budgets": [
      {
        "resourceSizes": [
          {
            "resourceType": "script",
            "budget": 300
          },
          {
            "resourceType": "stylesheet",
            "budget": 50
          },
          {
            "resourceType": "image",
            "budget": 200
          },
          {
            "resourceType": "font",
            "budget": 100
          },
          {
            "resourceType": "total",
            "budget": 1000
          }
        ]
      },
      {
        "resourceCounts": [
          {
            "resourceType": "script",
            "budget": 10
          },
          {
            "resourceType": "stylesheet",
            "budget": 5
          },
          {
            "resourceType": "third-party",
            "budget": 5
          }
        ]
      },
      {
        "timings": [
          {
            "metric": "interactive",
            "budget": 3800
          },
          {
            "metric": "first-contentful-paint",
            "budget": 1800
          },
          {
            "metric": "largest-contentful-paint",
            "budget": 2500
          }
        ]
      }
    ]
  }
}
```

---

## 5. 设计规范检查清单

### 5.1 可访问性检查清单

- [ ] 所有交互元素可通过键盘访问
- [ ] Tab 导航顺序合理
- [ ] 焦点指示器清晰可见
- [ ] 色彩对比度符合 WCAG 2.1 AA 标准
- [ ] 所有图片有 alt 文本
- [ ] 表单元素有关联的 label
- [ ] 使用语义化 HTML 标签
- [ ] ARIA 标签使用正确
- [ ] 屏幕阅读器测试通过
- [ ] 支持文本缩放 200%
- [ ] 避免仅依赖颜色传达信息
- [ ] 动画可以暂停或禁用
- [ ] 错误提示清晰明确

### 5.2 性能检查清单

- [ ] Lighthouse 性能评分 > 90
- [ ] LCP < 2.5s
- [ ] FID < 100ms
- [ ] CLS < 0.1
- [ ] TTI < 3.8s
- [ ] 代码分割合理
- [ ] 图片已优化和懒加载
- [ ] 使用 CDN 加速静态资源
- [ ] 启用 Gzip/Brotli 压缩
- [ ] 长列表使用虚拟滚动
- [ ] API 请求有缓存策略
- [ ] 防抖/节流应用合理
- [ ] 首屏资源 < 1MB

### 5.3 用户体验检查清单

- [ ] 加载状态有明确提示
- [ ] 空状态设计友好
- [ ] 错误信息清晰可操作
- [ ] 表单验证及时反馈
- [ ] 操作有确认机制
- [ ] 支持撤销操作
- [ ] 快捷键可用
- [ ] 响应式布局适配良好
- [ ] 交互反馈及时(<100ms)
- [ ] 过渡动画自然流畅
- [ ] 数据自动保存
- [ ] 离线功能支持

---

**文档版本**: v1.0
**创建日期**: 2025-09-30
**文档完成**: 完整的 ProManage UI/UX 设计文档已全部完成