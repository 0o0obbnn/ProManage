<template>
  <a-layout class="default-layout">
    <a-layout-sider
      v-model:collapsed="collapsed"
      :trigger="null"
      collapsible
      :width="240"
      class="layout-sider"
    >
      <div class="logo">
        <img v-if="!collapsed" src="/logo.png" alt="ProManage" />
        <img v-else src="/logo-mini.png" alt="PM" />
      </div>

      <a-menu
        v-model:selectedKeys="selectedKeys"
        v-model:openKeys="openKeys"
        mode="inline"
        theme="dark"
        :inline-collapsed="collapsed"
        @click="handleMenuClick"
      >
        <a-menu-item key="dashboard">
          <template #icon>
            <DashboardOutlined />
          </template>
          <span>仪表板</span>
        </a-menu-item>

        <a-menu-item key="tasks">
          <template #icon>
            <CheckSquareOutlined />
          </template>
          <span>任务管理</span>
        </a-menu-item>

        <a-menu-item key="documents">
          <template #icon>
            <FileTextOutlined />
          </template>
          <span>文档管理</span>
        </a-menu-item>

        <a-menu-item key="changes">
          <template #icon>
            <SwapOutlined />
          </template>
          <span>变更管理</span>
        </a-menu-item>

        <a-menu-item key="tests">
          <template #icon>
            <BugOutlined />
          </template>
          <span>测试管理</span>
        </a-menu-item>

        <a-menu-item key="notifications">
          <template #icon>
            <BellOutlined />
          </template>
          <span>通知中心</span>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>

    <a-layout>
      <a-layout-header class="layout-header">
        <div class="header-left">
          <MenuUnfoldOutlined
            v-if="collapsed"
            class="trigger"
            @click="() => (collapsed = !collapsed)"
          />
          <MenuFoldOutlined
            v-else
            class="trigger"
            @click="() => (collapsed = !collapsed)"
          />
        </div>

        <div class="header-right">
          <a-space :size="8">
            <!-- 通知Badge -->
            <NotificationBadge />

            <!-- 用户菜单 -->
            <a-dropdown placement="bottomRight">
              <a-space class="user-info">
                <a-avatar :src="userAvatar" :size="32">
                  {{ userName.charAt(0) }}
                </a-avatar>
                <span class="user-name">{{ userName }}</span>
              </a-space>

              <template #overlay>
                <a-menu>
                  <a-menu-item key="profile">
                    <UserOutlined />
                    个人中心
                  </a-menu-item>
                  <a-menu-item key="settings">
                    <SettingOutlined />
                    系统设置
                  </a-menu-item>
                  <a-menu-divider />
                  <a-menu-item key="logout">
                    <LogoutOutlined />
                    退出登录
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </a-space>
        </div>
      </a-layout-header>

      <a-layout-content class="layout-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </a-layout-content>
    </a-layout>

    <!-- Toast通知组件 -->
    <ToastNotification />
  </a-layout>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import {
  DashboardOutlined,
  CheckSquareOutlined,
  FileTextOutlined,
  SwapOutlined,
  BugOutlined,
  BellOutlined,
  MenuUnfoldOutlined,
  MenuFoldOutlined,
  UserOutlined,
  SettingOutlined,
  LogoutOutlined
} from '@ant-design/icons-vue';
import NotificationBadge from '@/components/notification/NotificationBadge.vue';
import ToastNotification from '@/components/notification/ToastNotification.vue';

const router = useRouter();
const route = useRoute();

const collapsed = ref(false);
const selectedKeys = ref<string[]>([]);
const openKeys = ref<string[]>([]);

// 模拟用户信息
const userName = ref('张三');
const userAvatar = ref('');

const handleMenuClick = ({ key }: { key: string }) => {
  const routeMap: Record<string, string> = {
    dashboard: '/dashboard',
    tasks: '/tasks',
    documents: '/documents',
    changes: '/changes',
    tests: '/tests',
    notifications: '/notifications'
  };

  const path = routeMap[key];
  if (path) {
    router.push(path);
  }
};

// 根据当前路由更新选中的菜单项
watch(
  () => route.path,
  (path) => {
    const firstSegment = path.split('/')[1];
    if (firstSegment) {
      selectedKeys.value = [firstSegment];
    }
  },
  { immediate: true }
);

onMounted(() => {
  // 初始化
});
</script>

<style scoped lang="scss">
.default-layout {
  min-height: 100vh;
}

.layout-sider {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 100;
  overflow: auto;

  :deep(.ant-layout-sider-children) {
    display: flex;
    flex-direction: column;
  }
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  background: rgba(255, 255, 255, 0.1);

  img {
    max-height: 32px;
    max-width: 100%;
    object-fit: contain;
  }
}

.layout-header {
  position: fixed;
  top: 0;
  right: 0;
  left: 240px;
  z-index: 99;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: var(--color-bg-elevated);
  border-bottom: 1px solid var(--color-border-secondary);
  transition: left 0.2s;

  .trigger {
    font-size: 18px;
    cursor: pointer;
    transition: color 0.3s;

    &:hover {
      color: var(--color-primary);
    }
  }

  .user-info {
    cursor: pointer;
    padding: 4px 12px;
    border-radius: var(--border-radius-base);
    transition: background-color 0.2s;

    &:hover {
      background-color: var(--color-fill-secondary);
    }

    .user-name {
      font-size: var(--font-size-base);
      color: var(--color-text-primary);
    }
  }
}

.layout-content {
  margin-top: 64px;
  margin-left: 240px;
  min-height: calc(100vh - 64px);
  background: var(--color-bg-layout);
  transition: margin-left 0.2s;
}

// 侧边栏收起时的样式调整
:global(.ant-layout-sider-collapsed) ~ .layout-header {
  left: 80px;
}

:global(.ant-layout-sider-collapsed) ~ * .layout-content {
  margin-left: 80px;
}

// 路由切换动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease-in-out;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
