<template>
  <Teleport to="body">
    <div class="toast-notification-container">
      <TransitionGroup name="toast">
        <div
          v-for="toast in toasts"
          :key="toast.id"
          :class="['toast-notification', `toast-${toast.type}`]"
          @click="handleClick(toast)"
        >
          <div class="toast-icon">
            <InfoCircleOutlined v-if="toast.type === 'info'" />
            <CheckCircleOutlined v-if="toast.type === 'success'" />
            <ExclamationCircleOutlined v-if="toast.type === 'warning'" />
            <CloseCircleOutlined v-if="toast.type === 'error'" />
          </div>
          <div class="toast-content">
            <div class="toast-title">{{ toast.title }}</div>
            <div v-if="toast.content" class="toast-body">{{ toast.content }}</div>
          </div>
          <CloseOutlined class="toast-close" @click.stop="handleClose(toast.id)" />
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useNotificationStore } from '@/stores/modules/notification';
import {
  InfoCircleOutlined,
  CheckCircleOutlined,
  ExclamationCircleOutlined,
  CloseCircleOutlined,
  CloseOutlined
} from '@ant-design/icons-vue';
import type { ToastNotificationOptions } from '@/types/notification';

const router = useRouter();
const notificationStore = useNotificationStore();

const toasts = computed(() => notificationStore.toastQueue);

const handleClick = (toast: ToastNotificationOptions) => {
  if (toast.onClick) {
    toast.onClick();
  } else if (toast.link) {
    router.push(toast.link);
  }
  handleClose(toast.id!);
};

const handleClose = (id: string) => {
  notificationStore.removeToast(id);
};
</script>

<style scoped lang="scss">
.toast-notification-container {
  position: fixed;
  top: 80px;
  right: 24px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 12px;
  pointer-events: none;
}

.toast-notification {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  min-width: 320px;
  max-width: 400px;
  padding: 16px;
  background: var(--color-bg-elevated);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-lg);
  cursor: pointer;
  pointer-events: all;
  transition: all 0.3s var(--ease-in-out);

  &:hover {
    transform: translateX(-4px);
    box-shadow: var(--shadow-xl);
  }
}

.toast-icon {
  font-size: 20px;
  line-height: 1;
  flex-shrink: 0;
}

.toast-content {
  flex: 1;
  min-width: 0;
}

.toast-title {
  font-size: var(--font-size-base);
  font-weight: 600;
  line-height: 1.5;
  margin-bottom: 4px;
  color: var(--color-text-primary);
}

.toast-body {
  font-size: var(--font-size-sm);
  line-height: 1.5;
  color: var(--color-text-secondary);
  word-wrap: break-word;
}

.toast-close {
  font-size: 14px;
  color: var(--color-text-tertiary);
  cursor: pointer;
  flex-shrink: 0;
  transition: color 0.2s;

  &:hover {
    color: var(--color-text-primary);
  }
}

.toast-info {
  border-left: 4px solid var(--color-primary);

  .toast-icon {
    color: var(--color-primary);
  }
}

.toast-success {
  border-left: 4px solid var(--color-success);

  .toast-icon {
    color: var(--color-success);
  }
}

.toast-warning {
  border-left: 4px solid var(--color-warning);

  .toast-icon {
    color: var(--color-warning);
  }
}

.toast-error {
  border-left: 4px solid var(--color-error);

  .toast-icon {
    color: var(--color-error);
  }
}

// Transition animations
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s var(--ease-in-out);
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(100%);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(100%) scale(0.8);
}

.toast-move {
  transition: transform 0.3s var(--ease-in-out);
}
</style>
