<template>
  <a-card title="项目进度" :loading="loading">
    <template #extra>
      <a-button type="link" size="small" @click="handleViewAll">
        查看全部 →
      </a-button>
    </template>

    <div ref="chartRef" :style="{ height: `${height}px` }"></div>
  </a-card>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts/core'
import { BarChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { ProjectProgress } from '@/api/modules/dashboard'

// 注册ECharts组件
echarts.use([
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  BarChart,
  CanvasRenderer
])

/**
 * Props
 */
interface Props {
  data: ProjectProgress[]
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
  viewAll: []
}>()

/**
 * 组件状态
 */
const router = useRouter()
const chartRef = ref<HTMLElement>()
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
      axisPointer: {
        type: 'shadow'
      },
      formatter: (params: any) => {
        const item = params[0]
        const dataIndex = item.dataIndex
        const project = props.data[dataIndex]
        return `
          <div style="padding: 8px;">
            <div style="font-weight: bold; margin-bottom: 4px;">${project.projectName}</div>
            <div>进度: ${project.progress}%</div>
            <div>已完成: ${project.completedTasks}/${project.totalTasks}</div>
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
      data: props.data.map(item => item.projectName),
      axisLabel: {
        interval: 0,
        rotate: 30,
        formatter: (value: string) => {
          return value.length > 8 ? value.substring(0, 8) + '...' : value
        }
      }
    },
    yAxis: {
      type: 'value',
      max: 100,
      axisLabel: {
        formatter: '{value}%'
      }
    },
    series: [
      {
        name: '完成进度',
        type: 'bar',
        data: props.data.map(item => ({
          value: item.progress,
          itemStyle: {
            color: item.progress >= 80
              ? '#52c41a'
              : item.progress >= 50
              ? '#1890ff'
              : item.progress >= 30
              ? '#faad14'
              : '#ff4d4f'
          }
        })),
        barWidth: '60%',
        label: {
          show: true,
          position: 'top',
          formatter: '{c}%'
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
 * 查看全部
 */
const handleViewAll = () => {
  emit('viewAll')
  router.push('/projects')
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

