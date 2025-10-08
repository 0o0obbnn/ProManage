<template>
  <div class="gantt-view">
    <div class="gantt-toolbar">
      <a-space>
        <a-button @click="handleZoomIn">
          <template #icon><ZoomInOutlined /></template>
          放大
        </a-button>
        <a-button @click="handleZoomOut">
          <template #icon><ZoomOutOutlined /></template>
          缩小
        </a-button>
        <a-button @click="handleFitToScreen">
          <template #icon><FullscreenOutlined /></template>
          适应屏幕
        </a-button>
        <a-divider type="vertical" />
        <a-checkbox v-model:checked="showCriticalPath">
          显示关键路径
        </a-checkbox>
        <a-checkbox v-model:checked="showDependencies">
          显示依赖关系
        </a-checkbox>
        <a-divider type="vertical" />
        <a-button @click="handleExportPDF">
          <template #icon><FilePdfOutlined /></template>
          导出PDF
        </a-button>
        <a-button @click="handleExportImage">
          <template #icon><FileImageOutlined /></template>
          导出图片
        </a-button>
      </a-space>
    </div>

    <div ref="ganttContainer" class="gantt-container"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { message } from 'ant-design-vue'
import {
  ZoomInOutlined,
  ZoomOutOutlined,
  FullscreenOutlined,
  FilePdfOutlined,
  FileImageOutlined
} from '@ant-design/icons-vue'
import gantt from 'dhtmlx-gantt'
import 'dhtmlx-gantt/codebase/dhtmlxgantt.css'
import html2canvas from 'html2canvas'
import jsPDF from 'jspdf'

interface Props {
  tasks?: any[]
  projectId?: number
}

const props = withDefaults(defineProps<Props>(), {
  tasks: () => [],
  projectId: undefined
})

const emit = defineEmits(['taskUpdate', 'taskCreate', 'taskDelete', 'linkCreate', 'linkDelete'])

const ganttContainer = ref<HTMLElement>()
const showCriticalPath = ref(true)
const showDependencies = ref(true)

// 初始化甘特图
const initGantt = () => {
  if (!ganttContainer.value) return

  // 基础配置
  gantt.config.date_format = '%Y-%m-%d %H:%i'
  gantt.config.scale_unit = 'day'
  gantt.config.date_scale = '%m月%d日'
  gantt.config.subscales = [
    { unit: 'week', step: 1, date: '第%W周' }
  ]
  gantt.config.duration_unit = 'day'
  gantt.config.work_time = true
  gantt.config.correct_work_time = true
  gantt.config.skip_off_time = true

  // 列配置
  gantt.config.columns = [
    {
      name: 'text',
      label: '任务名称',
      width: '*',
      tree: true,
      resize: true
    },
    {
      name: 'start_date',
      label: '开始时间',
      width: 100,
      align: 'center',
      resize: true
    },
    {
      name: 'duration',
      label: '持续时间',
      width: 80,
      align: 'center',
      resize: true
    },
    {
      name: 'assignee',
      label: '负责人',
      width: 100,
      align: 'center',
      resize: true,
      template: (task: any) => task.assignee?.name || '未分配'
    },
    {
      name: 'progress',
      label: '进度',
      width: 80,
      align: 'center',
      template: (task: any) => `${Math.round(task.progress * 100)}%`
    }
  ]

  // 启用拖拽
  gantt.config.drag_links = true
  gantt.config.drag_progress = true
  gantt.config.drag_resize = true
  gantt.config.drag_move = true

  // 关键路径配置
  gantt.config.highlight_critical_path = showCriticalPath.value

  // 工具提示
  gantt.templates.tooltip_text = (start: Date, end: Date, task: any) => {
    return `
      <b>任务:</b> ${task.text}<br/>
      <b>开始:</b> ${gantt.templates.tooltip_date_format(start)}<br/>
      <b>结束:</b> ${gantt.templates.tooltip_date_format(end)}<br/>
      <b>进度:</b> ${Math.round(task.progress * 100)}%<br/>
      <b>负责人:</b> ${task.assignee?.name || '未分配'}
    `
  }

  // 任务样式
  gantt.templates.task_class = (start: Date, end: Date, task: any) => {
    const classes = []
    
    if (task.priority === 'HIGH') {
      classes.push('high-priority')
    } else if (task.priority === 'LOW') {
      classes.push('low-priority')
    }
    
    if (task.status === 'COMPLETED') {
      classes.push('completed')
    } else if (task.status === 'IN_PROGRESS') {
      classes.push('in-progress')
    }
    
    return classes.join(' ')
  }

  // 事件监听
  gantt.attachEvent('onAfterTaskUpdate', (id: string, task: any) => {
    emit('taskUpdate', { id, task })
  })

  gantt.attachEvent('onAfterTaskAdd', (id: string, task: any) => {
    emit('taskCreate', { id, task })
  })

  gantt.attachEvent('onAfterTaskDelete', (id: string) => {
    emit('taskDelete', { id })
  })

  gantt.attachEvent('onAfterLinkAdd', (id: string, link: any) => {
    emit('linkCreate', { id, link })
  })

  gantt.attachEvent('onAfterLinkDelete', (id: string) => {
    emit('linkDelete', { id })
  })

  // 初始化
  gantt.init(ganttContainer.value)
  
  // 加载数据
  loadData()
}

// 加载数据
const loadData = () => {
  const ganttData = {
    data: props.tasks.map(task => ({
      id: task.id,
      text: task.title,
      start_date: task.startDate,
      duration: task.duration || 1,
      progress: task.progress || 0,
      assignee: task.assignee,
      priority: task.priority,
      status: task.status,
      parent: task.parentId || 0
    })),
    links: props.tasks
      .filter(task => task.dependencies && task.dependencies.length > 0)
      .flatMap(task =>
        task.dependencies.map((depId: number, index: number) => ({
          id: `${task.id}_${depId}_${index}`,
          source: depId,
          target: task.id,
          type: '0' // FS (Finish-to-Start)
        }))
      )
  }

  gantt.parse(ganttData)
}

// 放大
const handleZoomIn = () => {
  gantt.ext.zoom.zoomIn()
}

// 缩小
const handleZoomOut = () => {
  gantt.ext.zoom.zoomOut()
}

// 适应屏幕
const handleFitToScreen = () => {
  gantt.ext.zoom.setLevel('day')
}

// 导出PDF
const handleExportPDF = async () => {
  try {
    if (!ganttContainer.value) return

    message.loading({ content: '正在生成PDF...', key: 'export' })

    const canvas = await html2canvas(ganttContainer.value, {
      scale: 2,
      logging: false,
      useCORS: true
    })

    const imgData = canvas.toDataURL('image/png')
    const pdf = new jsPDF({
      orientation: 'landscape',
      unit: 'px',
      format: [canvas.width, canvas.height]
    })

    pdf.addImage(imgData, 'PNG', 0, 0, canvas.width, canvas.height)
    pdf.save(`gantt-chart-${Date.now()}.pdf`)

    message.success({ content: 'PDF导出成功', key: 'export' })
  } catch (error) {
    message.error({ content: 'PDF导出失败', key: 'export' })
  }
}

// 导出图片
const handleExportImage = async () => {
  try {
    if (!ganttContainer.value) return

    message.loading({ content: '正在生成图片...', key: 'export' })

    const canvas = await html2canvas(ganttContainer.value, {
      scale: 2,
      logging: false,
      useCORS: true
    })

    canvas.toBlob((blob) => {
      if (blob) {
        const url = URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `gantt-chart-${Date.now()}.png`
        link.click()
        URL.revokeObjectURL(url)
        message.success({ content: '图片导出成功', key: 'export' })
      }
    })
  } catch (error) {
    message.error({ content: '图片导出失败', key: 'export' })
  }
}

// 监听关键路径显示
watch(showCriticalPath, (value) => {
  gantt.config.highlight_critical_path = value
  gantt.render()
})

// 监听依赖关系显示
watch(showDependencies, (value) => {
  if (value) {
    gantt.config.show_links = true
  } else {
    gantt.config.show_links = false
  }
  gantt.render()
})

// 监听任务数据变化
watch(() => props.tasks, () => {
  loadData()
}, { deep: true })

onMounted(() => {
  initGantt()
})

onUnmounted(() => {
  if (gantt.$container) {
    gantt.clearAll()
  }
})
</script>

<style scoped lang="scss">
.gantt-view {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;

  .gantt-toolbar {
    padding: 12px 16px;
    border-bottom: 1px solid #e8e8e8;
  }

  .gantt-container {
    flex: 1;
    overflow: hidden;
  }
}

// 甘特图自定义样式
:deep(.gantt_task_line) {
  &.high-priority {
    background-color: #ff4d4f;
    border-color: #cf1322;
  }

  &.low-priority {
    background-color: #52c41a;
    border-color: #389e0d;
  }

  &.completed {
    opacity: 0.6;
  }

  &.in-progress {
    background-color: #1890ff;
    border-color: #096dd9;
  }
}

:deep(.gantt_task_progress) {
  background-color: rgba(0, 0, 0, 0.2);
}

:deep(.gantt_critical_task) {
  background-color: #ff4d4f !important;
  border-color: #cf1322 !important;
}

:deep(.gantt_critical_link) {
  .gantt_line_wrapper div {
    background-color: #ff4d4f !important;
  }
  
  .gantt_link_arrow {
    border-color: #ff4d4f !important;
  }
}
</style>
