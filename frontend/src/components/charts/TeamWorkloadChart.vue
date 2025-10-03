<template>
  <div ref="chartRef" :style="{ height: `${height}px`, width: '100%' }"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'
import type { TeamWorkload } from '@/types/analytics'
import { createHorizontalBarChartOption } from '@/utils/chart'

interface Props {
  data: TeamWorkload | null
  height?: number
}

const props = withDefaults(defineProps<Props>(), {
  height: 300
})

const emit = defineEmits<{
  (e: 'memberClick', userId: string): void
}>()

const chartRef = ref<HTMLElement>()
let chartInstance: ECharts | null = null

const initChart = () => {
  if (!chartRef.value || !props.data) return

  if (chartInstance) {
    chartInstance.dispose()
  }

  chartInstance = echarts.init(chartRef.value)

  const yAxisData = props.data.members.map(member => member.userName)

  const series = [
    {
      name: '待办',
      data: props.data.members.map(member => member.todo),
      stack: 'total'
    },
    {
      name: '进行中',
      data: props.data.members.map(member => member.inProgress),
      stack: 'total'
    },
    {
      name: '已完成',
      data: props.data.members.map(member => member.completed),
      stack: 'total'
    }
  ]

  const option = createHorizontalBarChartOption(yAxisData, series, {
    legend: true,
    tooltip: true,
    grid: {
      top: 40,
      right: 40,
      bottom: 40,
      left: 100
    }
  })

  chartInstance.setOption(option)

  // 添加点击事件
  chartInstance.on('click', (params: any) => {
    if (params.componentType === 'series') {
      const member = props.data?.members[params.dataIndex]
      if (member) {
        emit('memberClick', member.userId)
      }
    }
  })
}

const resizeChart = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

watch(
  () => props.data,
  () => {
    nextTick(() => {
      initChart()
    })
  },
  { deep: true }
)

onMounted(() => {
  initChart()
  window.addEventListener('resize', resizeChart)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeChart)
  if (chartInstance) {
    chartInstance.dispose()
  }
})

defineExpose({
  chartInstance,
  resize: resizeChart
})
</script>

<style scoped lang="scss">
// Chart container styles are handled inline
</style>
