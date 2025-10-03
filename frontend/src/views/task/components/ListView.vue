<template>
  <div class="list-view">
    <a-table
      :columns="columns"
      :data-source="taskList"
      :loading="loading"
      :pagination="paginationConfig"
      :row-key="record => record.id"
      :row-selection="rowSelection"
      :expandable="expandableConfig"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record, index }">
        <template v-if="column.key === 'title'">
          <div class="title-cell">
            <a-checkbox
              v-model:checked="record.completed"
              @click.stop
              @change="handleTaskComplete(record)"
            />
            <a @click="handleOpenTask(record)">{{ record.title }}</a>
          </div>
        </template>

        <template v-else-if="column.key === 'status'">
          <a-select
            v-model:value="record.status"
            size="small"
            style="width: 100px"
            @change="handleStatusChange(record)"
          >
            <a-select-option value="todo">待处理</a-select-option>
            <a-select-option value="in_progress">进行中</a-select-option>
            <a-select-option value="testing">测试中</a-select-option>
            <a-select-option value="done">已完成</a-select-option>
          </a-select>
        </template>

        <template v-else-if="column.key === 'priority'">
          <a-tag :color="getPriorityColor(record.priority)">
            {{ getPriorityText(record.priority) }}
          </a-tag>
        </template>

        <template v-else-if="column.key === 'assignee'">
          <a-space v-if="record.assignee">
            <a-avatar :src="record.assignee.avatar" :size="24">
              {{ record.assignee.name.charAt(0) }}
            </a-avatar>
            <span>{{ record.assignee.name }}</span>
          </a-space>
          <span v-else style="color: #bfbfbf">未分配</span>
        </template>

        <template v-else-if="column.key === 'dueDate'">
          <span
            v-if="record.dueDate"
            :class="{ 'overdue': isOverdue(record.dueDate) }"
          >
            {{ formatDate(record.dueDate) }}
          </span>
          <span v-else style="color: #bfbfbf">-</span>
        </template>

        <template v-else-if="column.key === 'progress'">
          <a-progress
            :percent="record.progress"
            :stroke-color="getProgressColor(record.progress)"
            size="small"
          />
        </template>

        <template v-else-if="column.key === 'tags'">
          <a-space wrap size="small">
            <a-tag
              v-for="tag in record.tags?.slice(0, 2)"
              :key="tag"
              size="small"
            >
              {{ tag }}
            </a-tag>
            <a-tag v-if="record.tags && record.tags.length > 2" size="small">
              +{{ record.tags.length - 2 }}
            </a-tag>
          </a-space>
        </template>

        <template v-else-if="column.key === 'actions'">
          <a-space>
            <a-button
              type="link"
              size="small"
              @click="handleOpenTask(record)"
            >
              查看
            </a-button>
            <a-button
              type="link"
              size="small"
              danger
              @click="handleDelete(record)"
            >
              删除
            </a-button>
          </a-space>
        </template>
      </template>

      <template #expandedRowRender="{ record }">
        <div class="expanded-row">
          <div v-if="record.description" class="task-description">
            <strong>描述：</strong>
            <p>{{ record.description }}</p>
          </div>

          <div v-if="record.subtasks && record.subtasks.length > 0" class="subtasks">
            <strong>子任务：</strong>
            <a-list size="small" :data-source="record.subtasks">
              <template #renderItem="{ item }">
                <a-list-item>
                  <a-checkbox
                    :checked="item.completed"
                    @change="handleSubTaskToggle(item)"
                  >
                    {{ item.title }}
                  </a-checkbox>
                </a-list-item>
              </template>
            </a-list>
          </div>
        </div>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useTaskStore } from '@/stores/modules/task'
import type { Task, SubTask } from '@/types/task'
import type { TableColumnsType, TableProps } from 'ant-design-vue'
import dayjs from 'dayjs'

const emit = defineEmits<{
  openTask: [task: Task]
  delete: [task: Task]
}>()

const taskStore = useTaskStore()

const selectedRowKeys = ref<number[]>([])

const taskList = computed(() => taskStore.taskList)
const loading = computed(() => taskStore.loading)

const columns: TableColumnsType = [
  {
    title: '任务标题',
    key: 'title',
    dataIndex: 'title',
    width: 300,
    ellipsis: true
  },
  {
    title: '状态',
    key: 'status',
    dataIndex: 'status',
    width: 120,
    filters: [
      { text: '待处理', value: 'todo' },
      { text: '进行中', value: 'in_progress' },
      { text: '测试中', value: 'testing' },
      { text: '已完成', value: 'done' }
    ]
  },
  {
    title: '优先级',
    key: 'priority',
    dataIndex: 'priority',
    width: 100,
    filters: [
      { text: '紧急', value: 'urgent' },
      { text: '高', value: 'high' },
      { text: '中', value: 'medium' },
      { text: '低', value: 'low' }
    ]
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
    width: 120,
    sorter: true
  },
  {
    title: '进度',
    key: 'progress',
    dataIndex: 'progress',
    width: 150
  },
  {
    title: '标签',
    key: 'tags',
    dataIndex: 'tags',
    width: 200
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right'
  }
]

const paginationConfig = computed(() => ({
  current: taskStore.pagination.page,
  pageSize: taskStore.pagination.pageSize,
  total: taskStore.pagination.total,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
}))

const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: number[]) => {
    selectedRowKeys.value = keys
  }
}))

const expandableConfig = {
  expandedRowRender: (record: Task) => record,
  rowExpandable: (record: Task) =>
    (record.description && record.description.length > 0) ||
    (record.subtasks && record.subtasks.length > 0)
}

const handleTableChange: TableProps['onChange'] = (pagination, filters, sorter) => {
  if (pagination.current && pagination.pageSize) {
    taskStore.setPagination(pagination.current, pagination.pageSize)
  }

  const params: any = {}

  if (filters.status && filters.status.length > 0) {
    params.status = filters.status[0]
  }

  if (filters.priority && filters.priority.length > 0) {
    params.priority = filters.priority[0]
  }

  if (sorter && !Array.isArray(sorter)) {
    if (sorter.field && sorter.order) {
      params.sort = sorter.field
      params.order = sorter.order === 'ascend' ? 'asc' : 'desc'
    }
  }

  taskStore.setQueryParams(params)
  taskStore.fetchTasks()
}

const handleOpenTask = (task: Task) => {
  emit('openTask', task)
}

const handleTaskComplete = async (task: Task) => {
  const newStatus = task.completed ? 'done' : task.status
  await taskStore.updateTaskStatus(task.id, newStatus as any)
}

const handleStatusChange = async (task: Task) => {
  await taskStore.updateTaskStatus(task.id, task.status)
}

const handleSubTaskToggle = async (subtask: SubTask) => {
  await taskStore.toggleSubTaskComplete(subtask.id, !subtask.completed)
}

const handleDelete = (task: Task) => {
  emit('delete', task)
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

const isOverdue = (dueDate: string) => {
  return dayjs(dueDate).isBefore(dayjs())
}

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD')
}

const getProgressColor = (progress: number) => {
  if (progress === 100) return '#52c41a'
  if (progress >= 60) return '#1890ff'
  if (progress >= 30) return '#faad14'
  return '#ff4d4f'
}
</script>

<style scoped lang="scss">
.list-view {
  .title-cell {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .overdue {
    color: #ff4d4f;
  }

  .expanded-row {
    padding: 16px;
    background: #fafafa;

    .task-description {
      margin-bottom: 16px;

      p {
        margin-top: 8px;
        color: #595959;
        line-height: 1.6;
      }
    }

    .subtasks {
      strong {
        display: block;
        margin-bottom: 8px;
      }
    }
  }
}
</style>