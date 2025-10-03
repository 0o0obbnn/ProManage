<template>
  <div ref="chartRef" :style="{ height: `${height}px`, width: '100%' }"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick, computed } from 'vue'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'
import type { BugAnalysis } from '@/types/analytics'
import { createPieChartOption } from '@/utils/chart'
import { debounce } from 'lodash-es'

interface Props {
  data: BugAnalysis | null
  type: 'severity' | 'module' | 'status'
  height?: number
}

const props = withDefaults(defineProps<Props>(), {
  height: 300,
  type: 'severity'
})

const chartRef = ref<HTMLElement>()
let chartInstance: ECharts | null = null

const chartData = computed(() => {
  if (!props.data) return []

  switch (props.type) {
    case 'severity':
      return props.data.bySeverity
    case 'module':
      return props.data.byModule
    case 'status':
      return props.data.byStatus
    default:
      return []
  }
})

const chartTitle = computed(() => {
  switch (props.type) {
    case 'severity':
      return '按严重程度'
    case 'module':
      return '按模块'
    case 'status':
      return '按状态'
    default:
      return ''
  }
})

const initChart = () => {
  if (!chartRef.value || !chartData.value.length) return

  if (chartInstance) {
    chartInstance.dispose()
  }

  chartInstance = echarts.init(chartRef.value)

  const data = chartData.value.map(item => ({
    name: item.name,
    value: item.value,
    itemStyle: item.color ? { color: item.color } : undefined
  }))

  const option = createPieChartOption(data, {
    title: chartTitle.value,
    legend: true,
    tooltip: true
  })

  chartInstance.setOption(option)
}

// 使用防抖优化resize性能，避免频繁调用
const debouncedResize = debounce(() => {
  if (chartInstance) {
    chartInstance.resize()
  }
}, 300)

watch(
  () => props.data,
  () => {
    nextTick(() => {
      initChart()
    })
  },
  { deep: true }
)

watch(
  () => props.type,
  () => {
    nextTick(() => {
      initChart()
    })
  }
)

onMounted(() => {
  initChart()
  window.addEventListener('resize', debouncedResize)
})

onUnmounted(() => {
  // 清理事件监听器
  window.removeEventListener('resize', debouncedResize)
  
  // 清理图表实例
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
  
  // 清理防抖函数
  debouncedResize.cancel()
})

defineExpose({
  chartInstance,
  resize: debouncedResize
})
</script>

<style scoped lang="scss">
// Chart container styles are handled inline
</style>
