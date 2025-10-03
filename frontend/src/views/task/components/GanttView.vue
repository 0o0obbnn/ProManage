<template>
  <div class="gantt-view">
    <div class="gantt-header">
      <div class="gantt-toolbar">
        <a-space>
          <a-button @click="handleZoomIn">
            <ZoomInOutlined />
          </a-button>
          <a-button @click="handleZoomOut">
            <ZoomOutOutlined />
          </a-button>
          <a-button @click="handleToday">
            今天
          </a-button>
        </a-space>
      </div>
    </div>

    <div class="gantt-container">
      <div class="gantt-sidebar">
        <div class="gantt-sidebar-header">
          <span>任务</span>
        </div>
        <div
          v-for="task in ganttTasks"
          :key="task.id"
          class="gantt-row-label"
          @click="handleTaskClick(task)"
        >
          <div class="task-info">
            <a-tag :color="getPriorityColor(task.priority)" size="small">
              {{ getPriorityText(task.priority) }}
            </a-tag>
            <span class="task-title">{{ task.title }}</span>
          </div>
          <div v-if="task.assignee" class="task-assignee">
            <a-avatar :src="task.assignee.avatar" :size="20">
              {{ task.assignee.name.charAt(0) }}
            </a-avatar>
          </div>
        </div>
      </div>

      <div class="gantt-timeline">
        <div class="gantt-timeline-header">
          <div
            v-for="date in timelineDates"
            :key="date.toString()"
            class="gantt-date-cell"
            :style="{ width: cellWidth + 'px' }"
          >
            <div class="date-label">{{ formatDate(date) }}</div>
          </div>
        </div>

        <div class="gantt-chart">
          <div
            v-for="task in ganttTasks"
            :key="task.id"
            class="gantt-row"
          >
            <div
              class="gantt-bar"
              :style="getBarStyle(task)"
              @click="handleTaskClick(task)"
            >
              <div class="gantt-bar-progress" :style="{ width: task.progress + '%' }"></div>
              <div class="gantt-bar-label">{{ task.title }}</div>
            </div>
          </div>

          <div class="gantt-today-line" :style="getTodayLineStyle()"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useTaskStore } from '@/stores/modules/task'
import type { Task, GanttTask } from '@/types/task'
import { ZoomInOutlined, ZoomOutOutlined } from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import isBetween from 'dayjs/plugin/isBetween'

dayjs.extend(isBetween)

const emit = defineEmits<{
  taskClick: [task: GanttTask]
}>()

const taskStore = useTaskStore()

const cellWidth = ref(80)
const zoomLevel = ref(1)

const ganttTasks = computed<GanttTask[]>(() => {
  return taskStore.taskList
    .filter(task => task.startDate && task.dueDate)
    .map(task => ({
      id: task.id,
      title: task.title,
      start: new Date(task.startDate!),
      end: new Date(task.dueDate!),
      progress: task.progress,
      status: task.status,
      priority: task.priority,
      assignee: task.assignee,
      dependencies: []
    }))
})

const startDate = computed(() => {
  if (ganttTasks.value.length === 0) return dayjs().startOf('month')

  const dates = ganttTasks.value.map(task => dayjs(task.start))
  return dayjs.min(dates)?.startOf('week') || dayjs().startOf('month')
})

const endDate = computed(() => {
  if (ganttTasks.value.length === 0) return dayjs().endOf('month')

  const dates = ganttTasks.value.map(task => dayjs(task.end))
  return dayjs.max(dates)?.endOf('week') || dayjs().endOf('month')
})

const timelineDates = computed(() => {
  const dates: dayjs.Dayjs[] = []
  let current = startDate.value

  while (current.isBefore(endDate.value) || current.isSame(endDate.value)) {
    dates.push(current)
    current = current.add(1, 'day')
  }

  return dates
})

const getBarStyle = (task: GanttTask) => {
  const taskStart = dayjs(task.start)
  const taskEnd = dayjs(task.end)

  const daysFromStart = taskStart.diff(startDate.value, 'day')
  const duration = taskEnd.diff(taskStart, 'day') + 1

  const left = daysFromStart * cellWidth.value
  const width = duration * cellWidth.value

  return {
    left: `${left}px`,
    width: `${width}px`,
    backgroundColor: getTaskColor(task.status)
  }
}

const getTodayLineStyle = () => {
  const today = dayjs()
  const daysFromStart = today.diff(startDate.value, 'day')
  const left = daysFromStart * cellWidth.value

  return {
    left: `${left}px`
  }
}

const getTaskColor = (status: string) => {
  const colors: Record<string, string> = {
    todo: '#d9d9d9',
    in_progress: '#1890ff',
    testing: '#faad14',
    done: '#52c41a'
  }
  return colors[status] || '#d9d9d9'
}

const getPriorityColor = (priority: string) => {
  const colors: Record<string, string> = {
    urgent: 'red',
    high: 'orange',
    medium: 'blue',
    low: 'default'
  }
  return colors[priority] || 'default'
}

const getPriorityText = (priority: string) => {
  const texts: Record<string, string> = {
    urgent: '紧急',
    high: '高',
    medium: '中',
    low: '低'
  }
  return texts[priority] || priority
}

const formatDate = (date: dayjs.Dayjs) => {
  return date.format('MM/DD')
}

const handleZoomIn = () => {
  zoomLevel.value = Math.min(zoomLevel.value + 0.25, 2)
  cellWidth.value = 80 * zoomLevel.value
}

const handleZoomOut = () => {
  zoomLevel.value = Math.max(zoomLevel.value - 0.25, 0.5)
  cellWidth.value = 80 * zoomLevel.value
}

const handleToday = () => {
  const todayLine = document.querySelector('.gantt-today-line')
  if (todayLine) {
    todayLine.scrollIntoView({ behavior: 'smooth', inline: 'center', block: 'nearest' })
  }
}

const handleTaskClick = (task: GanttTask) => {
  emit('taskClick', task)
}
</script>

<style scoped lang="scss">
.gantt-view {
  background: #ffffff;
  border-radius: 8px;
  overflow: hidden;

  .gantt-header {
    padding: 16px;
    border-bottom: 1px solid #f0f0f0;

    .gantt-toolbar {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }

  .gantt-container {
    display: flex;
    height: calc(100vh - 450px);
    overflow: hidden;

    .gantt-sidebar {
      width: 250px;
      flex-shrink: 0;
      border-right: 2px solid #f0f0f0;
      overflow-y: auto;

      .gantt-sidebar-header {
        height: 40px;
        line-height: 40px;
        padding: 0 16px;
        font-weight: 600;
        background: #fafafa;
        border-bottom: 1px solid #f0f0f0;
      }

      .gantt-row-label {
        height: 50px;
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0 16px;
        border-bottom: 1px solid #f0f0f0;
        cursor: pointer;
        transition: background 0.3s;

        &:hover {
          background: #f5f5f5;
        }

        .task-info {
          display: flex;
          align-items: center;
          gap: 8px;
          flex: 1;
          min-width: 0;

          .task-title {
            flex: 1;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            font-size: 14px;
          }
        }

        .task-assignee {
          flex-shrink: 0;
          margin-left: 8px;
        }
      }
    }

    .gantt-timeline {
      flex: 1;
      overflow-x: auto;
      overflow-y: hidden;

      .gantt-timeline-header {
        display: flex;
        height: 40px;
        background: #fafafa;
        border-bottom: 1px solid #f0f0f0;

        .gantt-date-cell {
          flex-shrink: 0;
          display: flex;
          align-items: center;
          justify-content: center;
          border-right: 1px solid #f0f0f0;
          font-size: 12px;
          color: #8c8c8c;

          .date-label {
            text-align: center;
          }
        }
      }

      .gantt-chart {
        position: relative;
        overflow-y: auto;

        .gantt-row {
          height: 50px;
          position: relative;
          border-bottom: 1px solid #f0f0f0;
        }

        .gantt-bar {
          position: absolute;
          height: 32px;
          top: 9px;
          border-radius: 4px;
          cursor: pointer;
          overflow: hidden;
          display: flex;
          align-items: center;
          padding: 0 8px;
          box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
          transition: all 0.3s;

          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
          }

          .gantt-bar-progress {
            position: absolute;
            left: 0;
            top: 0;
            height: 100%;
            background: rgba(255, 255, 255, 0.3);
            pointer-events: none;
          }

          .gantt-bar-label {
            position: relative;
            z-index: 1;
            color: #ffffff;
            font-size: 12px;
            font-weight: 500;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }
        }

        .gantt-today-line {
          position: absolute;
          top: 0;
          bottom: 0;
          width: 2px;
          background: #ff4d4f;
          z-index: 10;
          pointer-events: none;

          &::before {
            content: '';
            position: absolute;
            top: 0;
            left: 50%;
            transform: translateX(-50%);
            width: 8px;
            height: 8px;
            background: #ff4d4f;
            border-radius: 50%;
          }
        }
      }
    }
  }
}
</style>