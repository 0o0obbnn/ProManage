<template>
  <a-card title="团队活跃度" :loading="loading">
    <template #extra>
      <a-radio-group v-model:value="days" button-style="solid" size="small" @change="handleDaysChange">
        <a-radio-button :value="7">7天</a-radio-button>
        <a-radio-button :value="30">30天</a-radio-button>
        <a-radio-button :value="90">90天</a-radio-button>
      </a-radio-group>
    </template>

    <div ref="chartRef" :style="{ height: `${height}px` }"></div>
  </a-card>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts/core'
import { LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { TeamActivity } from '@/api/modules/dashboard'

// 注册ECharts组件
echarts.use([
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  LineChart,
  CanvasRenderer
])

/**
 * Props
 */
interface Props {
  data: TeamActivity[]
  loading?: boolean
  height?: number
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  height: 300
})

/**
 * Emits
 */
const emit = defineEmits<{
  daysChange: [days: number]
}>()

/**
 * 组件状态
 */
const chartRef = ref<HTMLElement>()
const days = ref(30)
let chartInstance: echarts.ECharts | null = null

/**
 * 初始化图表
 */
const initChart = () => {
  if (!chartRef.value) return

  chartInstance = echarts.init(chartRef.value)

  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const item = params[0]
        return `
          <div style="padding: 8px;">
            <div style="font-weight: bold; margin-bottom: 4px;">${item.axisValue}</div>
            <div>活跃度: ${item.value}</div>
          </div>
        `
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: props.data.map(item => item.date),
      axisLabel: {
        interval: Math.floor(props.data.length / 7),
        rotate: 30
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '{value}'
      }
    },
    series: [
      {
        name: '活跃度',
        type: 'line',
        smooth: true,
        data: props.data.map(item => item.count),
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            {
              offset: 0,
              color: 'rgba(24, 144, 255, 0.3)'
            },
            {
              offset: 1,
              color: 'rgba(24, 144, 255, 0.05)'
            }
          ])
        },
        lineStyle: {
          color: '#1890ff',
          width: 2
        },
        itemStyle: {
          color: '#1890ff'
        }
      }
    ]
  }

  chartInstance.setOption(option)
}

/**
 * 更新图表
 */
const updateChart = () => {
  if (!chartInstance) return
  initChart()
}

/**
 * 处理天数变化
 */
const handleDaysChange = () => {
  emit('daysChange', days.value)
}

/**
 * 监听数据变化
 */
watch(() => props.data, () => {
  updateChart()
}, { deep: true })

/**
 * 监听窗口大小变化
 */
const handleResize = () => {
  chartInstance?.resize()
}

/**
 * 组件挂载
 */
onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

/**
 * 组件卸载
 */
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<style scoped lang="scss">
// 样式
</style>

