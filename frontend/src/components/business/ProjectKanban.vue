<template>
  <div class="project-kanban">
    <!-- 工具栏 -->
    <div class="project-kanban__toolbar">
      <a-space>
        <a-button type="primary" @click="handleCreateTask">
          <template #icon>
            <PlusOutlined />
          </template>
          新建任务
        </a-button>
        <a-input-search
          v-model:value="searchKeyword"
          placeholder="搜索任务..."
          style="width: 200px"
          @search="handleSearch"
        />
      </a-space>
      <a-space>
        <a-select
          v-model:value="filterPriority"
          placeholder="优先级"
          style="width: 120px"
          allow-clear
          @change="handleFilterChange"
        >
          <a-select-option :value="0">低优先级</a-select-option>
          <a-select-option :value="1">中优先级</a-select-option>
          <a-select-option :value="2">高优先级</a-select-option>
          <a-select-option :value="3">紧急</a-select-option>
        </a-select>
        <a-select
          v-model:value="filterAssignee"
          placeholder="指派人"
          style="width: 120px"
          allow-clear
          @change="handleFilterChange"
        >
          <!-- TODO: 从项目成员列表加载 -->
        </a-select>
      </a-space>
    </div>

    <!-- 看板列 -->
    <div class="project-kanban__board">
      <div
        v-for="column in columns"
        :key="column.id"
        class="project-kanban__column"
      >
        <!-- 列头 -->
        <div class="project-kanban__column-header" :style="{ borderTopColor: column.color }">
          <h3 class="project-kanban__column-title">
            {{ column.title }}
            <a-badge :count="column.tasks.length" :number-style="{ backgroundColor: column.color }" />
          </h3>
        </div>

        <!-- 任务列表 -->
        <div
          :ref="(el) => setColumnRef(column.id, el)"
          class="project-kanban__column-content"
          :data-column-id="column.id"
        >
          <TransitionGroup name="task-list">
            <div
              v-for="task in column.tasks"
              :key="task.id"
              class="project-kanban__task-wrapper"
              :data-task-id="task.id"
            >
              <TaskCard :task="task" @click="handleTaskClick" />
            </div>
          </TransitionGroup>

          <!-- 空状态 -->
          <div v-if="column.tasks.length === 0" class="project-kanban__empty">
            <InboxOutlined />
            <p>暂无任务</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="project-kanban__loading">
      <a-spin size="large" tip="加载中..." />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { message } from 'ant-design-vue'
import { useSortable } from '@vueuse/integrations/useSortable'
import { PlusOutlined, InboxOutlined } from '@ant-design/icons-vue'
import TaskCard from './TaskCard.vue'
import { getProjectTasks, updateTaskStatus } from '@/api/modules/task'
import type { Task, TaskStatus, TaskPriority, TaskColumn } from '@/types/task'

/**
 * 组件属性
 */
interface Props {
  projectId: number
}

const props = defineProps<Props>()

/**
 * 组件事件
 */
interface Emits {
  (e: 'create-task'): void
  (e: 'task-click', task: Task): void
}

const emit = defineEmits<Emits>()

// 响应式数据
const loading = ref(false)
const tasks = ref<Task[]>([])
const searchKeyword = ref('')
const filterPriority = ref<TaskPriority | undefined>()
const filterAssignee = ref<number | undefined>()
const columnRefs = ref<Map<string, HTMLElement>>(new Map())

/**
 * 看板列定义
 */
const columnDefinitions: TaskColumn[] = [
  {
    id: 'todo',
    title: '待办',
    status: 0, // TaskStatus.TODO
    color: '#8c8c8c',
    tasks: [],
    order: 0
  },
  {
    id: 'in-progress',
    title: '进行中',
    status: 1, // TaskStatus.IN_PROGRESS
    color: '#1890ff',
    tasks: [],
    order: 1
  },
  {
    id: 'testing',
    title: '测试中',
    status: 2, // TaskStatus.TESTING
    color: '#faad14',
    tasks: [],
    order: 2
  },
  {
    id: 'done',
    title: '已完成',
    status: 3, // TaskStatus.DONE
    color: '#52c41a',
    tasks: [],
    order: 3
  }
]

/**
 * 计算看板列
 */
const columns = computed(() => {
  return columnDefinitions.map(col => ({
    ...col,
    tasks: tasks.value.filter(task => task.status === col.status)
  }))
})

/**
 * 设置列引用
 */
const setColumnRef = (columnId: string, el: any) => {
  if (el) {
    columnRefs.value.set(columnId, el as HTMLElement)
  }
}

/**
 * 加载任务列表
 */
const loadTasks = async () => {
  try {
    loading.value = true
    const params: any = {
      page: 1,
      pageSize: 1000 // 看板视图加载所有任务
    }

    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    if (filterPriority.value !== undefined) {
      params.priority = filterPriority.value
    }
    if (filterAssignee.value) {
      params.assigneeId = filterAssignee.value
    }

    const result = await getProjectTasks(props.projectId, params)
    tasks.value = result.list || []
  } catch (error) {
    console.error('Load tasks failed:', error)
    message.error('加载任务失败')
  } finally {
    loading.value = false
  }
}

/**
 * 处理创建任务
 */
const handleCreateTask = () => {
  emit('create-task')
}

/**
 * 处理任务点击
 */
const handleTaskClick = (task: Task) => {
  emit('task-click', task)
}

/**
 * 处理搜索
 */
const handleSearch = () => {
  loadTasks()
}

/**
 * 处理筛选变化
 */
const handleFilterChange = () => {
  loadTasks()
}

/**
 * 处理任务拖拽
 */
const handleTaskDrop = async (taskId: number, newStatus: TaskStatus) => {
  try {
    await updateTaskStatus(taskId, newStatus)
    message.success('任务状态更新成功')
    await loadTasks()
  } catch (error) {
    console.error('Update task status failed:', error)
    message.error('更新任务状态失败')
    await loadTasks() // 重新加载以恢复原状态
  }
}

/**
 * 初始化拖拽
 */
const initDragAndDrop = () => {
  columnRefs.value.forEach((el, columnId) => {
    const column = columnDefinitions.find(c => c.id === columnId)
    if (!column) return

    useSortable(el, {
      group: 'tasks',
      animation: 150,
      ghostClass: 'task-ghost',
      chosenClass: 'task-chosen',
      dragClass: 'task-drag',
      onEnd: (evt: any) => {
        const taskId = Number(evt.item.dataset.taskId)
        const newColumnId = evt.to.dataset.columnId
        const newColumn = columnDefinitions.find(c => c.id === newColumnId)
        
        if (newColumn && taskId) {
          handleTaskDrop(taskId, newColumn.status)
        }
      }
    })
  })
}

// 监听项目ID变化
watch(() => props.projectId, () => {
  loadTasks()
})

// 生命周期
onMounted(() => {
  loadTasks()
  // 延迟初始化拖拽,确保DOM已渲染
  setTimeout(() => {
    initDragAndDrop()
  }, 100)
})
</script>

<style lang="scss" scoped>
.project-kanban {
  height: 100%;
  display: flex;
  flex-direction: column;

  &__toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    background: white;
    border-bottom: 1px solid #f0f0f0;
  }

  &__board {
    flex: 1;
    display: flex;
    gap: 16px;
    padding: 16px;
    overflow-x: auto;
    background: #f5f5f5;
  }

  &__column {
    flex: 0 0 300px;
    display: flex;
    flex-direction: column;
    background: #fafafa;
    border-radius: 8px;
    overflow: hidden;

    &-header {
      padding: 12px 16px;
      background: white;
      border-top: 3px solid;
    }

    &-title {
      margin: 0;
      font-size: 14px;
      font-weight: 600;
      color: #262626;
      display: flex;
      align-items: center;
      justify-content: space-between;
    }

    &-content {
      flex: 1;
      padding: 12px;
      overflow-y: auto;
      min-height: 200px;
    }
  }

  &__task-wrapper {
    margin-bottom: 8px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  &__empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40px 20px;
    color: #bfbfbf;
    font-size: 14px;

    .anticon {
      font-size: 48px;
      margin-bottom: 12px;
    }

    p {
      margin: 0;
    }
  }

  &__loading {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 255, 255, 0.8);
    z-index: 10;
  }
}

// 拖拽样式
.task-ghost {
  opacity: 0.5;
}

.task-chosen {
  cursor: move;
}

.task-drag {
  opacity: 0;
}

// 过渡动画
.task-list-move,
.task-list-enter-active,
.task-list-leave-active {
  transition: all 0.3s ease;
}

.task-list-enter-from,
.task-list-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

.task-list-leave-active {
  position: absolute;
}
</style>

