<template>
  <div class="task-detail-table">
    <a-table
      :columns="columns"
      :data-source="taskDetails"
      :loading="loading"
      :pagination="paginationConfig"
      :scroll="{ x: 1200 }"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'title'">
          <a @click="handleTaskClick(record)">{{ record.title }}</a>
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
          <div class="assignee-cell">
            <a-avatar :size="24" :src="record.assignee.avatar">
              {{ record.assignee.name.charAt(0) }}
            </a-avatar>
            <span class="assignee-name">{{ record.assignee.name }}</span>
          </div>
        </template>

        <template v-else-if="column.key === 'progress'">
          <a-progress
            :percent="record.progress"
            :size="'small'"
            :status="record.progress === 100 ? 'success' : 'active'"
          />
        </template>

        <template v-else-if="column.key === 'duration'">
          <span v-if="record.actualHours">
            {{ record.actualHours }}h
            <span v-if="record.estimatedHours" class="estimated">
              / {{ record.estimatedHours }}h
            </span>
          </span>
          <span v-else-if="record.estimatedHours" class="estimated">
            {{ record.estimatedHours }}h (预估)
          </span>
          <span v-else>-</span>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAnalyticsStore } from '@/stores/modules/analytics'
import type { TableColumnType, TableProps } from 'ant-design-vue'
import type { TaskDetail } from '@/types/analytics'
import dayjs from 'dayjs'

interface Props {
  projectId?: string
  dateRange?: [string, string]
}

const props = defineProps<Props>()

const router = useRouter()
const analyticsStore = useAnalyticsStore()

const loading = computed(() => analyticsStore.tableLoading)
const taskDetails = computed(() => analyticsStore.taskDetails)

const columns: TableColumnType<TaskDetail>[] = [
  {
    title: '任务ID',
    dataIndex: 'id',
    key: 'id',
    width: 100,
    fixed: 'left'
  },
  {
    title: '任务标题',
    dataIndex: 'title',
    key: 'title',
    width: 250,
    fixed: 'left',
    ellipsis: true
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100,
    filters: [
      { text: '待处理', value: 'todo' },
      { text: '进行中', value: 'in_progress' },
      { text: '测试中', value: 'testing' },
      { text: '已完成', value: 'done' }
    ]
  },
  {
    title: '优先级',
    dataIndex: 'priority',
    key: 'priority',
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
    dataIndex: 'assignee',
    key: 'assignee',
    width: 150
  },
  {
    title: '项目',
    dataIndex: 'projectName',
    key: 'projectName',
    width: 150,
    ellipsis: true
  },
  {
    title: '开始时间',
    dataIndex: 'startDate',
    key: 'startDate',
    width: 120,
    customRender: ({ text }) => (text ? dayjs(text).format('YYYY-MM-DD') : '-'),
    sorter: true
  },
  {
    title: '完成时间',
    dataIndex: 'completedDate',
    key: 'completedDate',
    width: 120,
    customRender: ({ text }) => (text ? dayjs(text).format('YYYY-MM-DD') : '-'),
    sorter: true
  },
  {
    title: '耗时',
    key: 'duration',
    width: 120,
    sorter: true
  },
  {
    title: '进度',
    dataIndex: 'progress',
    key: 'progress',
    width: 150,
    sorter: true
  }
]

const paginationConfig = computed(() => ({
  current: analyticsStore.taskDetailPagination.page,
  pageSize: analyticsStore.taskDetailPagination.pageSize,
  total: analyticsStore.taskDetailPagination.total,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
}))

const handleTableChange: TableProps['onChange'] = (pagination, filters, sorter) => {
  analyticsStore.setTaskDetailPagination(
    pagination.current || 1,
    pagination.pageSize
  )
  fetchData()
}

const handleTaskClick = (task: TaskDetail) => {
  router.push(`/tasks/${task.id}`)
}

const getStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    todo: 'default',
    in_progress: 'processing',
    testing: 'warning',
    done: 'success'
  }
  return colorMap[status] || 'default'
}

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    todo: '待处理',
    in_progress: '进行中',
    testing: '测试中',
    done: '已完成'
  }
  return textMap[status] || status
}

const getPriorityColor = (priority: string) => {
  const colorMap: Record<string, string> = {
    urgent: 'red',
    high: 'orange',
    medium: 'blue',
    low: 'default'
  }
  return colorMap[priority] || 'default'
}

const getPriorityText = (priority: string) => {
  const textMap: Record<string, string> = {
    urgent: '紧急',
    high: '高',
    medium: '中',
    low: '低'
  }
  return textMap[priority] || priority
}

const fetchData = async () => {
  await analyticsStore.fetchTaskDetails()
}

watch(
  () => [props.projectId, props.dateRange],
  () => {
    if (props.projectId) {
      analyticsStore.setSelectedProject(props.projectId)
    }
    if (props.dateRange) {
      analyticsStore.setDateRange(props.dateRange)
    }
    fetchData()
  },
  { deep: true }
)

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.task-detail-table {
  .assignee-cell {
    display: flex;
    align-items: center;
    gap: 8px;

    .assignee-name {
      color: #262626;
    }
  }

  .estimated {
    color: #8c8c8c;
    font-size: 12px;
  }
}
</style>
