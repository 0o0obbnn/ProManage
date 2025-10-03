/**
 * 图表工具函数
 */
import type { EChartsOption } from 'echarts'
import type { ChartOptions } from '@/types/analytics'
import * as echarts from 'echarts'

/**
 * 默认颜色调色板
 */
export const DEFAULT_COLORS = [
  '#1890ff',
  '#52c41a',
  '#faad14',
  '#f5222d',
  '#722ed1',
  '#13c2c2',
  '#eb2f96',
  '#fa8c16',
  '#a0d911',
  '#2f54eb'
]

/**
 * 获取图表通用配置
 */
export function getChartCommonOptions(options?: ChartOptions): EChartsOption {
  return {
    color: DEFAULT_COLORS,
    title: options?.title
      ? {
          text: options.title,
          textStyle: {
            fontSize: 16,
            fontWeight: 'normal',
            color: '#262626'
          },
          left: 'center',
          top: 0
        }
      : undefined,
    legend: options?.legend !== false
      ? {
          type: 'scroll',
          orient: 'horizontal',
          bottom: 0,
          left: 'center',
          textStyle: {
            color: '#595959'
          }
        }
      : undefined,
    grid: {
      top: options?.grid?.top ?? 60,
      right: options?.grid?.right ?? 40,
      bottom: options?.grid?.bottom ?? 60,
      left: options?.grid?.left ?? 60,
      containLabel: true
    },
    tooltip: options?.tooltip !== false
      ? {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          backgroundColor: 'rgba(0, 0, 0, 0.75)',
          borderWidth: 0,
          textStyle: {
            color: '#fff',
            fontSize: 12
          },
          padding: [8, 12]
        }
      : undefined,
    dataZoom: options?.dataZoom
      ? [
          {
            type: 'inside',
            start: 0,
            end: 100
          },
          {
            start: 0,
            end: 100,
            height: 20,
            bottom: 10
          }
        ]
      : undefined,
    animation: options?.animation !== false,
    animationDuration: 800,
    animationEasing: 'cubicOut'
  }
}

/**
 * 格式化图表数据
 */
export function formatChartData(
  data: any[],
  type: 'line' | 'bar' | 'pie' | 'area'
): any {
  if (!data || data.length === 0) {
    return { xAxisData: [], series: [] }
  }

  switch (type) {
    case 'line':
    case 'area':
    case 'bar':
      return formatSeriesData(data)
    case 'pie':
      return formatPieData(data)
    default:
      return data
  }
}

/**
 * 格式化系列数据（折线图、柱状图、面积图）
 */
function formatSeriesData(data: any[]) {
  const keys = Object.keys(data[0]).filter(k => k !== 'date' && k !== 'name')
  const xAxisData = data.map(item => item.date || item.name)

  const series = keys.map(key => ({
    name: key,
    data: data.map(item => item[key])
  }))

  return { xAxisData, series }
}

/**
 * 格式化饼图数据
 */
function formatPieData(data: any[]) {
  return data.map(item => ({
    name: item.name,
    value: item.value,
    percentage: item.percentage
  }))
}

/**
 * 生成调色板
 */
export function generateColorPalette(count: number): string[] {
  if (count <= DEFAULT_COLORS.length) {
    return DEFAULT_COLORS.slice(0, count)
  }

  const colors: string[] = [...DEFAULT_COLORS]
  const hueStep = 360 / count

  for (let i = DEFAULT_COLORS.length; i < count; i++) {
    const hue = (hueStep * i) % 360
    colors.push(`hsl(${hue}, 70%, 55%)`)
  }

  return colors
}

/**
 * 导出图表为图片
 */
export function exportChartAsImage(
  chartInstance: echarts.ECharts,
  fileName: string = 'chart'
): void {
  const url = chartInstance.getDataURL({
    type: 'png',
    pixelRatio: 2,
    backgroundColor: '#fff'
  })

  const link = document.createElement('a')
  link.download = `${fileName}.png`
  link.href = url
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

/**
 * 计算趋势百分比
 */
export function calculateTrendPercentage(
  current: number,
  previous: number
): {
  value: number
  percentage: string
  trend: 'up' | 'down' | 'stable'
} {
  if (previous === 0) {
    return {
      value: current,
      percentage: current > 0 ? '+100%' : '0%',
      trend: current > 0 ? 'up' : 'stable'
    }
  }

  const diff = current - previous
  const percentage = (diff / previous) * 100
  const trend = diff > 0 ? 'up' : diff < 0 ? 'down' : 'stable'

  return {
    value: diff,
    percentage: `${percentage >= 0 ? '+' : ''}${percentage.toFixed(1)}%`,
    trend
  }
}

/**
 * 创建折线图配置
 */
export function createLineChartOption(
  xAxisData: string[],
  series: Array<{
    name: string
    data: number[]
    smooth?: boolean
    areaStyle?: any
  }>,
  options?: ChartOptions
): EChartsOption {
  const commonOptions = getChartCommonOptions(options)

  return {
    ...commonOptions,
    xAxis: {
      type: 'category',
      data: xAxisData,
      boundaryGap: false,
      axisLine: {
        lineStyle: {
          color: '#d9d9d9'
        }
      },
      axisLabel: {
        color: '#8c8c8c'
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        show: false
      },
      axisLabel: {
        color: '#8c8c8c'
      },
      splitLine: {
        lineStyle: {
          color: '#f0f0f0',
          type: 'dashed'
        }
      }
    },
    series: series.map(s => ({
      name: s.name,
      type: 'line',
      smooth: s.smooth ?? true,
      data: s.data,
      areaStyle: s.areaStyle,
      emphasis: {
        focus: 'series'
      }
    }))
  }
}

/**
 * 创建柱状图配置
 */
export function createBarChartOption(
  xAxisData: string[],
  series: Array<{
    name: string
    data: number[]
    stack?: string
  }>,
  options?: ChartOptions
): EChartsOption {
  const commonOptions = getChartCommonOptions(options)

  return {
    ...commonOptions,
    xAxis: {
      type: 'category',
      data: xAxisData,
      axisLine: {
        lineStyle: {
          color: '#d9d9d9'
        }
      },
      axisLabel: {
        color: '#8c8c8c'
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        show: false
      },
      axisLabel: {
        color: '#8c8c8c'
      },
      splitLine: {
        lineStyle: {
          color: '#f0f0f0',
          type: 'dashed'
        }
      }
    },
    series: series.map(s => ({
      name: s.name,
      type: 'bar',
      data: s.data,
      stack: s.stack,
      emphasis: {
        focus: 'series'
      },
      barMaxWidth: 40
    }))
  }
}

/**
 * 创建饼图配置
 */
export function createPieChartOption(
  data: Array<{
    name: string
    value: number
  }>,
  options?: ChartOptions
): EChartsOption {
  const commonOptions = getChartCommonOptions(options)

  return {
    ...commonOptions,
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)',
      backgroundColor: 'rgba(0, 0, 0, 0.75)',
      borderWidth: 0,
      textStyle: {
        color: '#fff',
        fontSize: 12
      }
    },
    series: [
      {
        name: options?.title || '数据分布',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 4,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {d}%',
          color: '#595959'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          },
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        data
      }
    ]
  }
}

/**
 * 创建堆叠面积图配置
 */
export function createStackedAreaChartOption(
  xAxisData: string[],
  series: Array<{
    name: string
    data: number[]
  }>,
  options?: ChartOptions
): EChartsOption {
  const commonOptions = getChartCommonOptions(options)

  return {
    ...commonOptions,
    xAxis: {
      type: 'category',
      data: xAxisData,
      boundaryGap: false,
      axisLine: {
        lineStyle: {
          color: '#d9d9d9'
        }
      },
      axisLabel: {
        color: '#8c8c8c'
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        show: false
      },
      axisLabel: {
        color: '#8c8c8c'
      },
      splitLine: {
        lineStyle: {
          color: '#f0f0f0',
          type: 'dashed'
        }
      }
    },
    series: series.map(s => ({
      name: s.name,
      type: 'line',
      stack: 'Total',
      smooth: true,
      data: s.data,
      areaStyle: {},
      emphasis: {
        focus: 'series'
      }
    }))
  }
}

/**
 * 创建横向柱状图配置
 */
export function createHorizontalBarChartOption(
  yAxisData: string[],
  series: Array<{
    name: string
    data: number[]
    stack?: string
  }>,
  options?: ChartOptions
): EChartsOption {
  const commonOptions = getChartCommonOptions(options)

  return {
    ...commonOptions,
    grid: {
      ...commonOptions.grid,
      left: 100
    },
    xAxis: {
      type: 'value',
      axisLine: {
        show: false
      },
      axisLabel: {
        color: '#8c8c8c'
      },
      splitLine: {
        lineStyle: {
          color: '#f0f0f0',
          type: 'dashed'
        }
      }
    },
    yAxis: {
      type: 'category',
      data: yAxisData,
      axisLine: {
        lineStyle: {
          color: '#d9d9d9'
        }
      },
      axisLabel: {
        color: '#8c8c8c'
      }
    },
    series: series.map(s => ({
      name: s.name,
      type: 'bar',
      data: s.data,
      stack: s.stack,
      emphasis: {
        focus: 'series'
      },
      barMaxWidth: 20
    }))
  }
}

/**
 * 格式化数字
 */
export function formatNumber(num: number, precision: number = 2): string {
  if (num >= 1000000) {
    return `${(num / 1000000).toFixed(precision)}M`
  }
  if (num >= 1000) {
    return `${(num / 1000).toFixed(precision)}K`
  }
  return num.toFixed(precision)
}

/**
 * 格式化百分比
 */
export function formatPercentage(value: number, total: number): string {
  if (total === 0) return '0%'
  return `${((value / total) * 100).toFixed(1)}%`
}

/**
 * 获取趋势图标
 */
export function getTrendIcon(trend: 'up' | 'down' | 'stable'): string {
  switch (trend) {
    case 'up':
      return '↑'
    case 'down':
      return '↓'
    case 'stable':
      return '→'
    default:
      return '-'
  }
}

/**
 * 获取趋势颜色
 */
export function getTrendColor(trend: 'up' | 'down' | 'stable', inverse: boolean = false): string {
  if (trend === 'stable') return '#8c8c8c'

  if (inverse) {
    return trend === 'up' ? '#f5222d' : '#52c41a'
  }

  return trend === 'up' ? '#52c41a' : '#f5222d'
}
