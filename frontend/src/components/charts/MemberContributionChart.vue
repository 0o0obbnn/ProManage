<template>
  <div ref="chartRef" class="member-contribution-chart"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'

/**
 * 成员贡献数据
 */
interface MemberContribution {
  name: string
  taskCount: number
  completedCount: number
}

/**
 * 组件属性
 */
interface Props {
  data: MemberContribution[]
}

const props = defineProps<Props>()

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
  if (!chartInstance || !props.data || props.data.length === 0) return

  const memberNames = props.data.map(item => item.name)
  const taskCounts = props.data.map(item => item.taskCount)
  const completedCounts = props.data.map(item => item.completedCount)

  const option: EChartsOption = {
    title: {
      text: '成员贡献',
      left: 'center',
      textStyle: {
        fontSize: 16,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['总任务数', '已完成'],
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: memberNames,
      axisLabel: {
        interval: 0,
        rotate: memberNames.length > 5 ? 45 : 0
      }
    },
    yAxis: {
      type: 'value',
      name: '任务数'
    },
    series: [
      {
        name: '总任务数',
        type: 'bar',
        data: taskCounts,
        itemStyle: {
          color: '#1890ff'
        }
      },
      {
        name: '已完成',
        type: 'bar',
        data: completedCounts,
        itemStyle: {
          color: '#52c41a'
        }
      }
    ]
  }

  chartInstance.setOption(option)
}

/**
 * 监听数据变化
 */
watch(
  () => props.data,
  () => {
    updateChart()
  },
  { deep: true }
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
.member-contribution-chart {
  width: 100%;
  height: 300px;
}
</style>

