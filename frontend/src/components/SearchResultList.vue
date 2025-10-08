<template>
  <div class="search-result-list">
    <!-- 加载状态 -->
    <a-spin v-if="loading" class="loading-spinner" />

    <!-- 空状态 -->
    <a-empty v-else-if="results.length === 0" description="暂无搜索结果" />

    <!-- 结果列表 -->
    <div v-else class="result-list">
      <div
        v-for="item in results"
        :key="item.id"
        class="result-item"
        @click="handleSelect(item)"
      >
        <!-- 类型图标 -->
        <div class="item-icon">
          <component :is="getTypeIcon(item.type)" :style="{ color: getTypeColor(item.type) }" />
        </div>

        <!-- 内容 -->
        <div class="item-content">
          <!-- 标题 -->
          <div class="item-title" v-html="highlightKeyword(item.title, keyword)"></div>

          <!-- 描述 -->
          <div
            v-if="item.description || item.content"
            class="item-description"
            v-html="highlightKeyword(item.description || item.content || '', keyword)"
          ></div>

          <!-- 元信息 -->
          <div class="item-meta">
            <a-tag size="small">{{ getTypeLabel(item.type) }}</a-tag>
            <span v-if="item.creator" class="creator">
              <UserOutlined />
              {{ item.creator.name }}
            </span>
            <span class="time">{{ formatTime(item.createdAt) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  ProjectOutlined,
  FileTextOutlined,
  CheckCircleOutlined,
  ExclamationCircleOutlined,
  UserOutlined
} from '@ant-design/icons-vue'
import { SearchType } from '@/api/modules/search'
import type { SearchResultItem } from '@/api/modules/search'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

/**
 * Props
 */
interface Props {
  results: SearchResultItem[]
  keyword: string
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
})

/**
 * Emits
 */
const emit = defineEmits<{
  select: [item: SearchResultItem]
}>()

/**
 * 获取类型图标
 */
const getTypeIcon = (type: SearchType) => {
  const iconMap = {
    [SearchType.PROJECT]: ProjectOutlined,
    [SearchType.DOCUMENT]: FileTextOutlined,
    [SearchType.TASK]: CheckCircleOutlined,
    [SearchType.CHANGE]: ExclamationCircleOutlined
  }
  return iconMap[type] || FileTextOutlined
}

/**
 * 获取类型颜色
 */
const getTypeColor = (type: SearchType) => {
  const colorMap = {
    [SearchType.PROJECT]: '#1890ff',
    [SearchType.DOCUMENT]: '#722ed1',
    [SearchType.TASK]: '#52c41a',
    [SearchType.CHANGE]: '#fa8c16'
  }
  return colorMap[type] || '#1890ff'
}

/**
 * 获取类型标签
 */
const getTypeLabel = (type: SearchType) => {
  const labelMap = {
    [SearchType.PROJECT]: '项目',
    [SearchType.DOCUMENT]: '文档',
    [SearchType.TASK]: '任务',
    [SearchType.CHANGE]: '变更'
  }
  return labelMap[type] || '未知'
}

/**
 * 高亮关键词
 */
const highlightKeyword = (text: string, keyword: string): string => {
  if (!keyword || !text) return text

  // 转义特殊字符
  const escapedKeyword = keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const regex = new RegExp(`(${escapedKeyword})`, 'gi')
  return text.replace(regex, '<mark>$1</mark>')
}

/**
 * 格式化时间
 */
const formatTime = (time: string): string => {
  return dayjs(time).fromNow()
}

/**
 * 处理选择
 */
const handleSelect = (item: SearchResultItem) => {
  emit('select', item)
}
</script>

<style scoped lang="scss">
.search-result-list {
  min-height: 200px;

  .loading-spinner {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 200px;
  }

  .result-list {
    .result-item {
      display: flex;
      gap: 12px;
      padding: 16px;
      border-radius: 4px;
      cursor: pointer;
      transition: background-color 0.3s;

      &:hover {
        background-color: rgba(0, 0, 0, 0.04);
      }

      .item-icon {
        font-size: 24px;
        flex-shrink: 0;
      }

      .item-content {
        flex: 1;
        min-width: 0;

        .item-title {
          font-size: 16px;
          font-weight: 500;
          margin-bottom: 8px;
          color: rgba(0, 0, 0, 0.85);

          :deep(mark) {
            background-color: #fff566;
            padding: 0 2px;
            border-radius: 2px;
          }
        }

        .item-description {
          font-size: 14px;
          color: rgba(0, 0, 0, 0.65);
          margin-bottom: 8px;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;

          :deep(mark) {
            background-color: #fff566;
            padding: 0 2px;
            border-radius: 2px;
          }
        }

        .item-meta {
          display: flex;
          align-items: center;
          gap: 12px;
          font-size: 12px;
          color: rgba(0, 0, 0, 0.45);

          .creator {
            display: flex;
            align-items: center;
            gap: 4px;
          }

          .time {
            margin-left: auto;
          }
        }
      }
    }
  }
}
</style>

