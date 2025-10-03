<template>
  <div ref="chartRef" :style="{ height: `${height}px`, width: '100%' }"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'
import type { ChangeImpactData } from '@/types/analytics'
import { createStackedAreaChartOption } from '@/utils/chart'

interface Props {
  data: ChangeImpactData | null
  height?: number
}

const props = withDefaults(defineProps<Props>(), {
  height: 300
})

const chartRef = ref<HTMLElement>()
let chartInstance: ECharts | null = null

const initChart = () => {
  if (!chartRef.value || !props.data) return

  if (chartInstance) {
    chartInstance.dispose()
  }

  chartInstance = echarts.init(chartRef.value)

  const xAxisData = props.data.data.map(item => {
    const date = new Date(item.date)
    return `${date.getMonth() + 1}/${date.getDate()}`
  })

  const series = [
    {
      name: '变更数量',
      data: props.data.data.map(item => item.changes)
    },
    {
      name: '影响模块',
      data: props.data.data.map(item => item.affectedModules)
    },
    {
      name: '通知人数',
      data: props.data.data.map(item => item.notifiedUsers)
    }
  ]

  const option = createStackedAreaChartOption(xAxisData, series, {
    legend: true,
    tooltip: true,
    grid: {
      top: 40,
      right: 20,
      bottom: 40,
      left: 50
    }
  })

  chartInstance.setOption(option)
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
