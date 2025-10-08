<template>
  <a-avatar :style="avatarStyle" :size="size">
    <component :is="iconComponent" />
  </a-avatar>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  BellOutlined,
  FileTextOutlined,
  CheckCircleOutlined,
  ExclamationCircleOutlined,
  MessageOutlined,
  UserOutlined,
  SettingOutlined,
  FileSearchOutlined
} from '@ant-design/icons-vue'
import type { NotificationType, NotificationPriority } from '@/types/notification'

/**
 * 组件属性
 */
interface Props {
  type: NotificationType
  priority?: NotificationPriority
  size?: number | 'small' | 'default' | 'large'
}

const props = withDefaults(defineProps<Props>(), {
  priority: 'NORMAL',
  size: 'default'
})

/**
 * 图标组件映射
 */
const iconComponent = computed(() => {
  const iconMap: Record<NotificationType, any> = {
    SYSTEM: BellOutlined,
    TASK: CheckCircleOutlined,
    DOCUMENT: FileTextOutlined,
    CHANGE: ExclamationCircleOutlined,
    TEST: FileSearchOutlined,
    COMMENT: MessageOutlined,
    MENTION: UserOutlined,
    APPROVAL: SettingOutlined
  }
  return iconMap[props.type] || BellOutlined
})

/**
 * 头像样式
 */
const avatarStyle = computed(() => {
  // 根据类型设置背景色
  const colorMap: Record<NotificationType, string> = {
    SYSTEM: '#1890ff',
    TASK: '#52c41a',
    DOCUMENT: '#722ed1',
    CHANGE: '#fa8c16',
    TEST: '#13c2c2',
    COMMENT: '#eb2f96',
    MENTION: '#faad14',
    APPROVAL: '#2f54eb'
  }

  // 根据优先级调整颜色
  let backgroundColor = colorMap[props.type] || '#1890ff'
  
  if (props.priority === 'URGENT') {
    backgroundColor = '#ff4d4f'
  } else if (props.priority === 'HIGH') {
    backgroundColor = '#fa8c16'
  }

  return {
    backgroundColor,
    color: '#fff'
  }
})
</script>

<style scoped lang="scss">
:deep(.anticon) {
  font-size: 16px;
}
</style>

