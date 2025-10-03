<template>
  <a-layout class="default-layout">
    <!-- 侧边栏 -->
    <a-layout-sider
      v-model:collapsed="collapsed"
      :width="200"
      :collapsed-width="80"
      :trigger="null"
      collapsible
      class="sidebar"
    >
      <div class="logo">
        <img src="/logo.svg" alt="ProManage" />
        <span v-show="!collapsed" class="logo-text">ProManage</span>
      </div>

      <a-menu
        v-model:selected-keys="selectedKeys"
        v-model:open-keys="openKeys"
        mode="inline"
        theme="dark"
        :items="menuItems"
        @click="handleMenuClick"
      />
    </a-layout-sider>

    <!-- 主内容区 -->
    <a-layout>
      <!-- 头部 -->
      <a-layout-header class="header">
        <div class="header-left">
          <menu-unfold-outlined
            v-if="collapsed"
            class="trigger"
            @click="toggleCollapsed"
          />
          <menu-fold-outlined
            v-else
            class="trigger"
            @click="toggleCollapsed"
          />

          <a-breadcrumb class="breadcrumb">
            <a-breadcrumb-item v-for="item in breadcrumbItems" :key="item.path">
              <router-link v-if="item.path" :to="item.path">
                {{ item.label }}
              </router-link>
              <span v-else>{{ item.label }}</span>
            </a-breadcrumb-item>
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
              <template #icon><bell-outlined /></template>
            </a-button>
          </a-badge>

          <!-- 用户菜单 -->
          <a-dropdown>
            <a-space class="user-info">
              <a-avatar :src="userInfo?.avatar" />
              <span>{{ userInfo?.name }}</span>
            </a-space>
            <template #overlay>
              <a-menu @click="handleUserMenuClick">
                <a-menu-item key="profile">
                  <user-outlined /> 个人中心
                </a-menu-item>
                <a-menu-item key="settings">
                  <setting-outlined /> 系统设置
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item key="logout">
                  <logout-outlined /> 退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>

      <!-- 内容区 -->
      <a-layout-content class="content">
        <router-view />
      </a-layout-content>

      <!-- 底部 -->
      <a-layout-footer class="footer">
        ProManage © 2025 Created by ProManage Team
      </a-layout-footer>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, computed, watch, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/modules/user'
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  DashboardOutlined,
  FileTextOutlined,
  CheckSquareOutlined,
  SwapOutlined,
  BellOutlined,
  UserOutlined,
  SettingOutlined,
  LogoutOutlined
} from '@ant-design/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 侧边栏折叠状态
const collapsed = ref(false)
const selectedKeys = ref<string[]>([])
const openKeys = ref<string[]>([])

// 用户信息
const userInfo = computed(() => userStore.userInfo)

// 搜索文本
const searchText = ref('')

// 未读通知数
const unreadCount = ref(0)

// 菜单配置
const menuItems = [
  {
    key: 'dashboard',
    label: '工作台',
    icon: () => h(DashboardOutlined),
    path: '/dashboard'
  },
  {
    key: 'documents',
    label: '文档管理',
    icon: () => h(FileTextOutlined),
    path: '/documents'
  },
  {
    key: 'tasks',
    label: '任务管理',
    icon: () => h(CheckSquareOutlined),
    path: '/tasks'
  },
  {
    key: 'changes',
    label: '变更管理',
    icon: () => h(SwapOutlined),
    path: '/changes'
  }
]

// 面包屑
const breadcrumbItems = computed(() => {
  const items = [{ label: '首页', path: '/' }]
  const matched = route.matched.filter((item) => item.meta && item.meta.title)

  matched.forEach((item) => {
    items.push({
      label: item.meta.title as string,
      path: item.path
    })
  })

  return items
})

// 切换侧边栏
const toggleCollapsed = () => {
  collapsed.value = !collapsed.value
}

// 菜单点击
const handleMenuClick = ({ key }: { key: string }) => {
  const menuItem = menuItems.find((item) => item.key === key)
  if (menuItem?.path) {
    router.push(menuItem.path)
  }
}

// 搜索
const handleSearch = (value: string) => {
  console.log('Search:', value)
  // TODO: 实现搜索功能
}

// 用户菜单点击
const handleUserMenuClick = async ({ key }: { key: string }) => {
  if (key === 'logout') {
    await userStore.logout()
    router.push('/login')
  } else if (key === 'profile') {
    router.push('/profile')
  } else if (key === 'settings') {
    router.push('/settings')
  }
}

// 监听路由变化，更新选中的菜单项
watch(
  () => route.path,
  (path) => {
    const matchedMenuItem = menuItems.find((item) => path.startsWith(item.path))
    if (matchedMenuItem) {
      selectedKeys.value = [matchedMenuItem.key]
    }
  },
  { immediate: true }
)
</script>

<style lang="scss" scoped>
.default-layout {
  min-height: 100vh;

  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    height: 100vh;
    overflow-y: auto;
    background: #001529;
    box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
    z-index: var(--z-fixed);

    .logo {
      height: var(--header-height);
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

      .logo-text {
        transition: opacity var(--duration-base);
      }
    }
  }

  .header {
    position: fixed;
    top: 0;
    right: 0;
    left: var(--sidebar-width);
    z-index: var(--z-sticky);
    background: var(--color-bg-white);
    box-shadow: var(--shadow-base);
    padding: 0 var(--space-6);
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: var(--header-height);

    .header-left {
      display: flex;
      align-items: center;
      gap: var(--space-4);

      .trigger {
        font-size: 18px;
        cursor: pointer;
        transition: color var(--duration-base);

        &:hover {
          color: var(--color-primary-600);
        }
      }

      .breadcrumb {
        margin-left: var(--space-4);
      }
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: var(--space-4);

      .user-info {
        cursor: pointer;
        padding: var(--space-1) var(--space-3);
        border-radius: var(--radius-base);
        transition: background var(--duration-base);

        &:hover {
          background: var(--color-bg-hover);
        }
      }
    }
  }

  .content {
    margin-left: var(--sidebar-width);
    margin-top: var(--header-height);
    padding: var(--space-6);
    min-height: calc(100vh - var(--header-height) - var(--footer-height));
  }

  .footer {
    margin-left: var(--sidebar-width);
    text-align: center;
    color: var(--color-text-secondary);
    background: var(--color-bg-white);
    border-top: 1px solid var(--color-border-light);
  }
}

// 折叠状态样式调整
:deep(.ant-layout-sider-collapsed) + .ant-layout {
  .header,
  .content,
  .footer {
    margin-left: var(--sidebar-collapsed-width);
  }
}
</style>