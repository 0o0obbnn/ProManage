<template>
  <div
    ref="containerRef"
    class="virtual-list-container"
    :style="{ height: containerHeight }"
    @scroll.passive="handleScroll"
  >
    <!-- 滚动区域 -->
    <div class="virtual-list-phantom" :style="{ height: `${totalHeight}px` }">
      <!-- 可见项目 -->
      <div
        class="virtual-list-content"
        :style="{ transform: `translate3d(0, ${offsetY}px, 0)` }"
      >
        <div
          v-for="item in visibleItems"
          :key="getItemKey(item)"
          class="virtual-list-item"
          :style="{ height: `${itemHeight}px` }"
        >
          <slot :item="item" :index="item.index" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'

interface Props {
  // 数据列表
  items: any[]
  // 每项高度
  itemHeight: number
  // 容器高度
  height: number | string
  // 缓冲区大小（额外渲染的项目数）
  bufferSize?: number
  // 获取唯一key的函数
  itemKey?: string | ((item: any) => string | number)
}

const props = withDefaults(defineProps<Props>(), {
  bufferSize: 5,
  itemKey: 'id'
})

const emit = defineEmits(['scroll'])

const containerRef = ref<HTMLElement>()
const scrollTop = ref(0)
const containerHeight = ref('400px')

// 计算容器高度
const updateContainerHeight = () => {
  if (typeof props.height === 'number') {
    containerHeight.value = `${props.height}px`
  } else {
    containerHeight.value = props.height
  }
}

// 总高度
const totalHeight = computed(() => {
  return props.items.length * props.itemHeight
})

// 可见区域的项目数量
const visibleCount = computed(() => {
  const containerEl = containerRef.value
  if (!containerEl || !props.itemHeight || props.itemHeight <= 0) return 10 // 默认最小值
  
  const containerHeightValue = containerEl.clientHeight
  return Math.max(10, Math.ceil(containerHeightValue / props.itemHeight)) // 保证最少显示10个
})

// 开始索引
const startIndex = computed(() => {
  const index = Math.floor(scrollTop.value / props.itemHeight)
  return Math.max(0, index - props.bufferSize)
})

// 结束索引
const endIndex = computed(() => {
  const index = startIndex.value + visibleCount.value + props.bufferSize * 2
  return Math.min(props.items.length - 1, index)
})

// 可见项目
const visibleItems = computed(() => {
  const items = []
  for (let i = startIndex.value; i <= endIndex.value; i++) {
    if (props.items[i]) {
      items.push({
        ...props.items[i],
        index: i
      })
    }
  }
  return items
})

// Y轴偏移量
const offsetY = computed(() => {
  return startIndex.value * props.itemHeight
})

// 获取项目唯一key
const getItemKey = (item: any): string | number => {
  if (typeof props.itemKey === 'function') {
    return props.itemKey(item)
  }
  return item[props.itemKey] || item.index
}

// 处理滚动（使用requestAnimationFrame优化性能）
let rafId: number | null = null
const handleScroll = (e: Event) => {
  const target = e.target as HTMLElement
  const newScrollTop = target.scrollTop
  
  // 取消上一次的 RAF
  if (rafId !== null) {
    cancelAnimationFrame(rafId)
  }
  
  // 使用requestAnimationFrame避免频繁更新
  rafId = requestAnimationFrame(() => {
    scrollTop.value = newScrollTop
    rafId = null
  })
  
  emit('scroll', e)
}

// 滚动到指定项目
const scrollToItem = (index: number, behavior: ScrollBehavior = 'smooth') => {
  if (!containerRef.value || !props.itemHeight) return
  
  // 边界检查
  const clampedIndex = Math.max(0, Math.min(index, props.items.length - 1))
  const targetScrollTop = clampedIndex * props.itemHeight
  
  containerRef.value.scrollTo({
    top: targetScrollTop,
    behavior
  })
}

// 滚动到顶部
const scrollToTop = () => {
  if (!containerRef.value) return
  containerRef.value.scrollTop = 0
}

// 滚动到底部
const scrollToBottom = () => {
  if (!containerRef.value) return
  containerRef.value.scrollTop = totalHeight.value
}

// 暴露方法
defineExpose({
  scrollToItem,
  scrollToTop,
  scrollToBottom
})

// 监听高度变化
watch(() => props.height, updateContainerHeight, { immediate: true })

onMounted(() => {
  updateContainerHeight()
})

onUnmounted(() => {
  // 清理 RAF
  if (rafId !== null) {
    cancelAnimationFrame(rafId)
  }
})
</script>

<style scoped lang="scss">
.virtual-list-container {
  position: relative;
  overflow-y: auto;
  overflow-x: hidden;
  
  &::-webkit-scrollbar {
    width: 8px;
    height: 8px;
  }
  
  &::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 4px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 4px;
    
    &:hover {
      background: #a8a8a8;
    }
  }
}

.virtual-list-phantom {
  position: relative;
}

.virtual-list-content {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
}

.virtual-list-item {
  overflow: hidden;
}
</style>