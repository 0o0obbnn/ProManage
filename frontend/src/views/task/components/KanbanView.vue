<template>
  <div class="kanban-board">
    <div class="kanban-container">
      <div
        v-for="column in kanbanColumns"
        :key="column.id"
        class="kanban-column"
        @drop="handleDrop($event, column.status)"
        @dragover.prevent
        @dragenter.prevent
      >
        <div class="column-header">
          <a-space>
            <a-badge :color="column.color" :text="column.title" />
            <a-tag>{{ column.tasks.length }}</a-tag>
          </a-space>

          <a-dropdown>
            <a-button type="text" size="small">
              <MoreOutlined />
            </a-button>
            <template #overlay>
              <a-menu>
                <a-menu-item @click="handleAddTask(column.status)">
                  <PlusOutlined /> 添加任务
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>

        <div class="column-content">
          <TransitionGroup name="task-list">
            <div
              v-for="task in column.tasks"
              :key="task.id"
              class="task-card"
              draggable="true"
              @dragstart="handleDragStart($event, task)"
              @dragend="handleDragEnd"
              @click="handleOpenTask(task)"
            >
              <div class="task-header">
                <div class="task-title">
                  <a-checkbox
                    v-model:checked="task.completed"
                    @click.stop
                    @change="handleTaskComplete(task)"
                  />
                  <span>{{ task.title }}</span>
                </div>

                <a-tag
                  v-if="task.priority === 'urgent'"
                  color="red"
                  size="small"
                >
                  紧急
                </a-tag>
                <a-tag
                  v-else-if="task.priority === 'high'"
                  color="orange"
                  size="small"
                >
                  高
                </a-tag>
              </div>

              <div v-if="task.description" class="task-description">
                {{ truncateText(task.description, 80) }}
              </div>

              <div v-if="task.tags && task.tags.length > 0" class="task-tags">
                <a-space wrap size="small">
                  <a-tag
                    v-for="tag in task.tags.slice(0, 3)"
                    :key="tag"
                    size="small"
                  >
                    {{ tag }}
                  </a-tag>
                </a-space>
              </div>

              <div class="task-footer">
                <a-space size="small">
                  <span v-if="task.subtaskCount > 0" class="task-meta">
                    <CheckSquareOutlined />
                    {{ task.completedSubtasks }}/{{ task.subtaskCount }}
                  </span>

                  <span v-if="task.attachmentCount > 0" class="task-meta">
                    <PaperClipOutlined />
                    {{ task.attachmentCount }}
                  </span>

                  <span v-if="task.commentCount > 0" class="task-meta">
                    <CommentOutlined />
                    {{ task.commentCount }}
                  </span>

                  <span
                    v-if="task.dueDate"
                    class="task-meta"
                    :class="{ 'overdue': isOverdue(task.dueDate) }"
                  >
                    <ClockCircleOutlined />
                    {{ formatDueDate(task.dueDate) }}
                  </span>
                </a-space>

                <a-avatar
                  v-if="task.assignee"
                  :src="task.assignee.avatar"
                  :size="24"
                  :title="task.assignee.name"
                >
                  {{ task.assignee.name.charAt(0) }}
                </a-avatar>
              </div>
            </div>
          </TransitionGroup>

          <a-button
            type="dashed"
            block
            class="add-task-btn"
            @click="handleAddTask(column.status)"
          >
            <PlusOutlined /> 添加任务
          </a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useTaskStore } from '@/stores/modules/task'
import { TaskStatus } from '@/types/task'
import type { Task } from '@/types/task'
import {
  MoreOutlined,
  PlusOutlined,
  CheckSquareOutlined,
  PaperClipOutlined,
  CommentOutlined,
  ClockCircleOutlined
} from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const emit = defineEmits<{
  openTask: [task: Task]
  addTask: [status: TaskStatus]
}>()

const taskStore = useTaskStore()

const kanbanColumns = computed(() => taskStore.kanbanColumns)

const handleDragStart = (event: DragEvent, task: Task) => {
  taskStore.handleDragStart(task)
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move'
    event.dataTransfer.setData('text/plain', task.id.toString())
  }
}

const handleDragEnd = () => {
  taskStore.handleDragEnd()
}

const handleDrop = async (event: DragEvent, status: TaskStatus) => {
  event.preventDefault()
  await taskStore.handleDrop(status)
}

const handleOpenTask = (task: Task) => {
  emit('openTask', task)
}

const handleAddTask = (status: TaskStatus) => {
  emit('addTask', status)
}

const handleTaskComplete = async (task: Task) => {
  const newStatus = task.completed ? TaskStatus.DONE : task.status
  await taskStore.updateTaskStatus(task.id, newStatus)
}

const truncateText = (text: string, maxLength: number) => {
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

const isOverdue = (dueDate: string) => {
  return dayjs(dueDate).isBefore(dayjs())
}

const formatDueDate = (dueDate: string) => {
  return dayjs(dueDate).fromNow()
}
</script>

<style scoped lang="scss">
.kanban-board {
  .kanban-container {
    display: flex;
    gap: 16px;
    overflow-x: auto;
    padding-bottom: 16px;

    .kanban-column {
      flex: 0 0 300px;
      background: #f5f5f5;
      border-radius: 8px;
      max-height: calc(100vh - 400px);
      display: flex;
      flex-direction: column;

      .column-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 16px;
        border-bottom: 1px solid #e8e8e8;
        font-weight: 600;
        background: #ffffff;
        border-radius: 8px 8px 0 0;
      }

      .column-content {
        flex: 1;
        overflow-y: auto;
        padding: 12px;

        .task-card {
          background: #ffffff;
          border-radius: 6px;
          padding: 12px;
          margin-bottom: 8px;
          cursor: pointer;
          transition: all 0.3s;
          border: 1px solid #f0f0f0;
          box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);

          &:hover {
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
            transform: translateY(-2px);
            border-color: #1890ff;
          }

          .task-header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            margin-bottom: 8px;

            .task-title {
              display: flex;
              align-items: flex-start;
              gap: 8px;
              flex: 1;
              font-weight: 500;
              font-size: 14px;
              line-height: 1.5;
              color: #262626;
            }
          }

          .task-description {
            color: #8c8c8c;
            font-size: 12px;
            margin-bottom: 8px;
            line-height: 1.5;
          }

          .task-tags {
            margin-bottom: 8px;
          }

          .task-footer {
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-size: 12px;
            color: #8c8c8c;
            margin-top: 8px;
            padding-top: 8px;
            border-top: 1px solid #f0f0f0;

            .task-meta {
              display: inline-flex;
              align-items: center;
              gap: 4px;

              &.overdue {
                color: #ff4d4f;
              }
            }
          }
        }

        .add-task-btn {
          margin-top: 8px;
        }
      }
    }
  }
}

.task-list-move {
  transition: transform 0.3s;
}

.task-list-enter-active,
.task-list-leave-active {
  transition: all 0.3s;
}

.task-list-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.task-list-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>