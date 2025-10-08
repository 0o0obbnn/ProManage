<template>
  <div class="project-task-list">
    <!-- 工具栏 -->
    <div class="task-toolbar">
      <div class="toolbar-left">
        <a-button type="primary" @click="handleCreateTask">
          <template #icon><PlusOutlined /></template>
          新建任务
        </a-button>
        <a-radio-group v-model:value="currentViewMode" button-style="solid" @change="handleViewModeChange">
          <a-radio-button value="list">
            <UnorderedListOutlined /> 列表
          </a-radio-button>
          <a-radio-button value="gantt">
            <BarChartOutlined /> 甘特图
          </a-radio-button>
        </a-radio-group>
      </div>

      <div class="toolbar-right">
        <a-input-search
          v-model:value="searchKeyword"
          placeholder="搜索任务..."
          style="width: 240px"
          @search="handleSearch"
        />
        <a-select
          v-model:value="filterStatus"
          placeholder="状态筛选"
          style="width: 120px"
          allow-clear
          @change="handleFilterChange"
        >
          <a-select-option value="">全部</a-select-option>
          <a-select-option value="TODO">待处理</a-select-option>
          <a-select-option value="IN_PROGRESS">进行中</a-select-option>
          <a-select-option value="TESTING">测试中</a-select-option>
          <a-select-option value="DONE">已完成</a-select-option>
        </a-select>
        <a-select
          v-model:value="filterPriority"
          placeholder="优先级筛选"
          style="width: 120px"
          allow-clear
          @change="handleFilterChange"
        >
          <a-select-option value="">全部</a-select-option>
          <a-select-option value="urgent">紧急</a-select-option>
          <a-select-option value="high">高</a-select-option>
          <a-select-option value="medium">中</a-select-option>
          <a-select-option value="low">低</a-select-option>
        </a-select>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="task-content">
      <!-- 列表视图 -->
      <div v-if="currentViewMode === 'list'" class="list-view">
        <a-table
          :columns="columns"
          :data-source="filteredTasks"
          :loading="loading"
          :pagination="paginationConfig"
          :row-key="record => record.id"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'title'">
              <div class="title-cell">
                <a-checkbox
                  v-model:checked="record.completed"
                  @click.stop
                  @change="handleTaskComplete(record)"
                />
                <a @click="handleTaskClick(record)">{{ record.title }}</a>
              </div>
            </template>

            <template v-else-if="column.key === 'status'">
              <a-tag :color="getStatusColor(record.status)">
                {{ getStatusText(record.status) }}
              </a-tag>
            </template>

            <template v-else-if="column.key === 'priority'">
              <a-tag :color="getPriorityColor(record.priority)">
                {{ getPriorityText(record.priority) }}
              </a-tag>
            </template>

            <template v-else-if="column.key === 'assignee'">
              <div v-if="record.assignee" class="assignee-cell">
                <a-avatar :src="record.assignee.avatar" :size="24">
                  {{ record.assignee.name.charAt(0) }}
                </a-avatar>
                <span>{{ record.assignee.name }}</span>
              </div>
              <span v-else class="text-gray">未分配</span>
            </template>

            <template v-else-if="column.key === 'dueDate'">
              <span v-if="record.dueDate" :class="getDueDateClass(record.dueDate)">
                {{ formatDate(record.dueDate) }}
              </span>
              <span v-else class="text-gray">-</span>
            </template>

            <template v-else-if="column.key === 'progress'">
              <a-progress
                :percent="record.progress"
                :stroke-color="getProgressColor(record.progress)"
                :size="'small'"
              />
            </template>

            <template v-else-if="column.key === 'actions'">
              <a-space>
                <a-button type="link" size="small" @click="handleEditTask(record)">
                  编辑
                </a-button>
                <a-button type="link" size="small" danger @click="handleDeleteTask(record)">
                  删除
                </a-button>
              </a-space>
            </template>
          </template>
        </a-table>
      </div>

      <!-- 甘特图视图 -->
      <div v-else-if="currentViewMode === 'gantt'" class="gantt-view">
        <GanttView
          :tasks="filteredTasks"
          @task-click="handleTaskClick"
        />
      </div>
    </div>

    <!-- 任务详情抽屉 -->
    <TaskDetailDrawer
      v-model:visible="detailVisible"
      :task-id="selectedTaskId"
      @edit="handleEditTask"
      @delete="handleDeleteTask"
      @close="handleCloseDetail"
    />

    <!-- 任务表单弹窗 -->
    <TaskFormModal
      v-model:visible="formVisible"
      :task="editingTask"
      :project-id="projectId"
      @success="handleFormSuccess"
      @cancel="handleFormCancel"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  PlusOutlined,
  UnorderedListOutlined,
  BarChartOutlined
} from '@ant-design/icons-vue'
import { useTaskStore } from '@/stores/modules/task'
import GanttView from '@/views/task/components/GanttView.vue'
import TaskDetailDrawer from '@/views/task/components/TaskDetailDrawer.vue'
import TaskFormModal from '@/views/task/components/TaskFormModal.vue'
import type { Task } from '@/types/task'
import type { TableColumnsType, TableProps } from 'ant-design-vue'
import dayjs from 'dayjs'

interface Props {
  projectId: number
}

const props = defineProps<Props>()

const emit = defineEmits<{
  createTask: []
  taskClick: [task: Task]
}>()

const taskStore = useTaskStore()

// 响应式数据
const loading = ref(false)
const currentViewMode = ref<'list' | 'gantt'>('list')
const searchKeyword = ref('')
const filterStatus = ref<string>('')
const filterPriority = ref<string>('')
const detailVisible = ref(false)
const formVisible = ref(false)
const selectedTaskId = ref<number>()
const editingTask = ref<Task | null>(null)
const tasks = ref<Task[]>([])
const pagination = ref({
  current: 1,
  pageSize: 20,
  total: 0
})

// 表格列定义
const columns: TableColumnsType = [
  {
    title: '任务名称',
    key: 'title',
    dataIndex: 'title',
    width: 300,
    ellipsis: true
  },
  {
    title: '状态',
    key: 'status',
    dataIndex: 'status',
    width: 100
  },
  {
    title: '优先级',
    key: 'priority',
    dataIndex: 'priority',
    width: 100
  },
  {
    title: '负责人',
    key: 'assignee',
    dataIndex: 'assignee',
    width: 150
  },
  {
    title: '截止日期',
    key: 'dueDate',
    dataIndex: 'dueDate',
    width: 120
  },
  {
    title: '进度',
    key: 'progress',
    dataIndex: 'progress',
    width: 150
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right'
  }
]

// 计算属性
const filteredTasks = computed(() => {
  let result = tasks.value

  // 搜索过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(task =>
      task.title.toLowerCase().includes(keyword) ||
      task.description?.toLowerCase().includes(keyword)
    )
  }

  // 状态过滤
  if (filterStatus.value) {
    result = result.filter(task => task.status === filterStatus.value)
  }

  // 优先级过滤
  if (filterPriority.value) {
    result = result.filter(task => task.priority === filterPriority.value)
  }

  return result
})

const paginationConfig = computed(() => ({
  current: pagination.value.current,
  pageSize: pagination.value.pageSize,
  total: pagination.value.total,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
}))

// 方法
const loadTasks = async () => {
  try {
    loading.value = true
    const result = await taskStore.fetchTasks({
      projectId: props.projectId,
      page: pagination.value.current,
      pageSize: pagination.value.pageSize
    })
    tasks.value = result.list
    pagination.value.total = result.total
  } catch (error) {
    console.error('Load tasks failed:', error)
    message.error('加载任务列表失败')
  } finally {
    loading.value = false
  }
}

const handleViewModeChange = () => {
  // 视图模式已通过 v-model 自动更新
}

const handleSearch = () => {
  // 搜索已通过 computed 自动过滤
}

const handleFilterChange = () => {
  // 过滤已通过 computed 自动过滤
}

const handleTableChange: TableProps['onChange'] = (pag) => {
  if (pag.current && pag.pageSize) {
    pagination.value.current = pag.current
    pagination.value.pageSize = pag.pageSize
    loadTasks()
  }
}

const handleCreateTask = () => {
  editingTask.value = null
  formVisible.value = true
  emit('createTask')
}

const handleTaskClick = (task: Task) => {
  selectedTaskId.value = task.id
  detailVisible.value = true
  emit('taskClick', task)
}

const handleEditTask = (task: Task) => {
  editingTask.value = task
  formVisible.value = true
  detailVisible.value = false
}

const handleDeleteTask = (task: Task) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除任务"${task.title}"吗?`,
    okText: '确定',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        await taskStore.deleteTask(task.id)
        message.success('任务删除成功')
        await loadTasks()
      } catch (error) {
        console.error('Delete task failed:', error)
      }
    }
  })
}

const handleTaskComplete = async (task: Task) => {
  try {
    await taskStore.updateTask(task.id, {
      completed: task.completed
    })
    message.success(task.completed ? '任务已完成' : '任务已取消完成')
  } catch (error) {
    console.error('Update task failed:', error)
    task.completed = !task.completed // 回滚
  }
}

const handleCloseDetail = () => {
  detailVisible.value = false
  selectedTaskId.value = undefined
}

const handleFormSuccess = () => {
  formVisible.value = false
  editingTask.value = null
  loadTasks()
}

const handleFormCancel = () => {
  formVisible.value = false
  editingTask.value = null
}

// 辅助函数
const getStatusColor = (status: string) => {
  const colors: Record<string, string> = {
    TODO: 'default',
    IN_PROGRESS: 'processing',
    TESTING: 'warning',
    DONE: 'success'
  }
  return colors[status] || 'default'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    TODO: '待处理',
    IN_PROGRESS: '进行中',
    TESTING: '测试中',
    DONE: '已完成'
  }
  return texts[status] || status
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

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD')
}

const getDueDateClass = (dueDate: string) => {
  const now = dayjs()
  const due = dayjs(dueDate)
  const diff = due.diff(now, 'day')

  if (diff < 0) return 'text-danger' // 已过期
  if (diff <= 3) return 'text-warning' // 即将到期
  return ''
}

const getProgressColor = (progress: number) => {
  if (progress >= 100) return '#52c41a'
  if (progress >= 70) return '#1890ff'
  if (progress >= 30) return '#faad14'
  return '#ff4d4f'
}

// 生命周期
onMounted(() => {
  loadTasks()
})

// 监听项目ID变化
watch(() => props.projectId, () => {
  loadTasks()
})
</script>

<style lang="scss" scoped>
.project-task-list {
  .task-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding: 16px;
    background: #fafafa;
    border-radius: 4px;

    .toolbar-left,
    .toolbar-right {
      display: flex;
      gap: 12px;
      align-items: center;
    }
  }

  .task-content {
    min-height: 400px;
  }

  .title-cell {
    display: flex;
    align-items: center;
    gap: 8px;

    a {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .assignee-cell {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .text-gray {
    color: #8c8c8c;
  }

  .text-danger {
    color: #ff4d4f;
  }

  .text-warning {
    color: #faad14;
  }

  .gantt-view {
    background: white;
    border-radius: 4px;
    padding: 16px;
  }
}
</style>

