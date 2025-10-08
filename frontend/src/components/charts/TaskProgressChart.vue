<template>
  <div ref="chartRef" class="task-progress-chart"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'

/**
 * 组件属性
 */
interface Props {
  totalTasks: number
  completedTasks: number
  inProgressTasks: number
  todoTasks?: number
}

const props = withDefaults(defineProps<Props>(), {
  todoTasks: 0
})

// 响应式数据
const chartRef = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null

/**
 * 初始化图表
 */
const initChart = () => {
  if (!chartRef.value) return

  chartInstance = echarts.init(chartRef.value)
  updateChart()
}

/**
 * 更新图表
 */
const updateChart = () => {
  if (!chartInstance) return

  const todoTasks = props.todoTasks || (props.totalTasks - props.completedTasks - props.inProgressTasks)

  const option: EChartsOption = {
    title: {
      text: '任务进度',
      left: 'center',
      textStyle: {
        fontSize: 16,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: ['已完成', '进行中', '待办']
    },
    series: [
      {
        name: '任务状态',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          {
            value: props.completedTasks,
            name: '已完成',
            itemStyle: { color: '#52c41a' }
          },
          {
            value: props.inProgressTasks,
            name: '进行中',
            itemStyle: { color: '#1890ff' }
          },
          {
            value: todoTasks,
            name: '待办',
            itemStyle: { color: '#d9d9d9' }
          }
        ]
      }
    ]
  }

  chartInstance.setOption(option)
}

/**
 * 监听数据变化
 */
watch(
  () => [props.totalTasks, props.completedTasks, props.inProgressTasks, props.todoTasks],
  () => {
    updateChart()
  }
)

/**
 * 窗口大小变化时重新渲染
 */
const handleResize = () => {
  chartInstance?.resize()
}

// 生命周期
onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<style lang="scss" scoped>
.task-progress-chart {
  width: 100%;
  height: 300px;
}
</style>

