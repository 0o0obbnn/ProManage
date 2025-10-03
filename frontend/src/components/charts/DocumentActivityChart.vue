<template>
  <div ref="chartRef" :style="{ height: `${height}px`, width: '100%' }"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'
import type { DocumentActivity } from '@/types/analytics'
import { createLineChartOption } from '@/utils/chart'

interface Props {
  data: DocumentActivity | null
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
      name: '新建',
      data: props.data.data.map(item => item.created),
      smooth: true
    },
    {
      name: '更新',
      data: props.data.data.map(item => item.updated),
      smooth: true
    },
    {
      name: '查看',
      data: props.data.data.map(item => item.views),
      smooth: true
    }
  ]

  const option = createLineChartOption(xAxisData, series, {
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
