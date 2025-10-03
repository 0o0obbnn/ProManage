<template>
  <div class="task-page">
    <div class="board-header">
      <div class="header-left">
        <h2>任务看板</h2>
        <a-space>
          <a-select
            v-model:value="currentSprintId"
            style="width: 200px"
            placeholder="选择迭代"
            @change="handleSprintChange"
          >
            <a-select-option
              v-for="sprint in sprints"
              :key="sprint.id"
              :value="sprint.id"
            >
              {{ sprint.name }}
              <a-tag
                v-if="sprint.status === 'active'"
                color="green"
                size="small"
                style="margin-left: 8px"
              >
                进行中
              </a-tag>
            </a-select-option>
          </a-select>

          <a-button @click="handleManageSprints">
            <SettingOutlined /> 管理迭代
          </a-button>
        </a-space>
      </div>

      <div class="header-right">
        <a-space>
          <a-button type="primary" @click="handleCreateTask()">
            <template #icon><PlusOutlined /></template>
            新建任务
          </a-button>

          <a-radio-group v-model:value="viewMode" button-style="solid" @change="handleViewModeChange">
            <a-radio-button value="kanban">
              <AppstoreOutlined /> 看板
            </a-radio-button>
            <a-radio-button value="list">
              <UnorderedListOutlined /> 列表
            </a-radio-button>
            <a-radio-button value="gantt">
              <BarChartOutlined /> 甘特图
            </a-radio-button>
          </a-radio-group>

          <a-dropdown>
            <a-button>
              <FilterOutlined /> 筛选
              <DownOutlined />
            </a-button>
            <template #overlay>
              <div class="filter-panel">
                <a-form layout="vertical">
                  <a-form-item label="负责人">
                    <a-select
                      v-model:value="filterParams.assignees"
                      mode="multiple"
                      placeholder="选择负责人"
                      style="width: 240px"
                    >
                      <a-select-option
                        v-for="user in teamMembers"
                        :key="user.id"
                        :value="user.id"
                      >
                        <a-space>
                          <a-avatar :src="user.avatar" :size="20">
                            {{ user.name.charAt(0) }}
                          </a-avatar>
                          {{ user.name }}
                        </a-space>
                      </a-select-option>
                    </a-select>
                  </a-form-item>

                  <a-form-item label="优先级">
                    <a-checkbox-group v-model:value="filterParams.priorities">
                      <a-checkbox value="urgent">紧急</a-checkbox>
                      <a-checkbox value="high">高</a-checkbox>
                      <a-checkbox value="medium">中</a-checkbox>
                      <a-checkbox value="low">低</a-checkbox>
                    </a-checkbox-group>
                  </a-form-item>

                  <a-form-item label="标签">
                    <a-select
                      v-model:value="filterParams.tags"
                      mode="tags"
                      placeholder="输入标签"
                      style="width: 240px"
                    />
                  </a-form-item>

                  <a-form-item>
                    <a-space>
                      <a-button type="primary" @click="handleApplyFilters">
                        应用筛选
                      </a-button>
                      <a-button @click="handleResetFilters">
                        重置
                      </a-button>
                    </a-space>
                  </a-form-item>
                </a-form>
              </div>
            </template>
          </a-dropdown>

          <a-select v-model:value="groupBy" style="width: 120px" @change="handleGroupByChange">
            <a-select-option value="status">按状态</a-select-option>
            <a-select-option value="assignee">按负责人</a-select-option>
            <a-select-option value="priority">按优先级</a-select-option>
            <a-select-option value="label">按标签</a-select-option>
          </a-select>
        </a-space>
      </div>
    </div>

    <div class="board-stats">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-card>
            <a-statistic
              title="总任务数"
              :value="stats?.total || 0"
            >
              <template #prefix>
                <CheckSquareOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic
              title="已完成"
              :value="stats?.completed || 0"
              :value-style="{ color: '#52c41a' }"
            >
              <template #prefix>
                <CheckCircleFilled />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic
              title="进行中"
              :value="stats?.inProgress || 0"
              :value-style="{ color: '#1890ff' }"
            >
              <template #prefix>
                <SyncOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic
              title="完成率"
              :value="stats?.completionRate || 0"
              suffix="%"
            >
              <template #prefix>
                <RiseOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <div class="board-content">
      <a-spin :spinning="loading">
        <KanbanView
          v-if="viewMode === 'kanban'"
          @open-task="handleOpenTask"
          @add-task="handleCreateTask"
        />

        <ListView
          v-else-if="viewMode === 'list'"
          @open-task="handleOpenTask"
          @delete="handleDeleteTask"
        />

        <GanttView
          v-else-if="viewMode === 'gantt'"
          @task-click="handleGanttTaskClick"
        />
      </a-spin>
    </div>

    <TaskDetailDrawer
      v-model:visible="detailVisible"
      :task-id="selectedTaskId"
      @edit="handleEditTask"
      @delete="handleDeleteTask"
      @close="handleCloseDetail"
    />

    <TaskFormModal
      v-model:visible="formVisible"
      :task="editingTask"
      :default-status="defaultStatus"
      @success="handleFormSuccess"
      @cancel="handleFormCancel"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useTaskStore } from '@/stores/modules/task'
import type { Task, TaskStatus, GanttTask } from '@/types/task'
import KanbanView from './components/KanbanView.vue'
import ListView from './components/ListView.vue'
import GanttView from './components/GanttView.vue'
import TaskDetailDrawer from './components/TaskDetailDrawer.vue'
import TaskFormModal from './components/TaskFormModal.vue'
import {
  PlusOutlined,
  AppstoreOutlined,
  UnorderedListOutlined,
  BarChartOutlined,
  FilterOutlined,
  DownOutlined,
  SettingOutlined,
  CheckSquareOutlined,
  CheckCircleFilled,
  SyncOutlined,
  RiseOutlined
} from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'

const taskStore = useTaskStore()

const viewMode = computed({
  get: () => taskStore.viewMode,
  set: (value) => taskStore.setViewMode(value)
})

const groupBy = computed({
  get: () => taskStore.groupBy,
  set: (value) => taskStore.setGroupBy(value)
})

const loading = computed(() => taskStore.loading)
const sprints = computed(() => taskStore.sprints)
const stats = computed(() => taskStore.stats)
const filterParams = computed(() => taskStore.filterParams)

const currentSprintId = ref<number>()
const selectedTaskId = ref<number>()
const detailVisible = ref(false)
const formVisible = ref(false)
const editingTask = ref<Task | null>(null)
const defaultStatus = ref<TaskStatus>()

// Mock team members - 在实际应用中应该从 API 获取
const teamMembers = ref([
  { id: 1, name: '张三', avatar: '' },
  { id: 2, name: '李四', avatar: '' },
  { id: 3, name: '王五', avatar: '' }
])

onMounted(async () => {
  // 恢复视图模式
  taskStore.restoreViewMode()

  // 初始化数据
  await Promise.all([
    taskStore.fetchTasks(),
    taskStore.fetchSprints(),
    taskStore.fetchStats(),
    taskStore.fetchTags()
  ])

  // 设置当前激活的迭代
  const activeSprint = taskStore.activeSprint
  if (activeSprint) {
    currentSprintId.value = activeSprint.id
  }
})

const handleSprintChange = async (sprintId: number) => {
  taskStore.setQueryParams({ sprintId })
  await taskStore.fetchTasks()
  await taskStore.fetchStats(undefined, sprintId)
}

const handleManageSprints = () => {
  message.info('管理迭代功能开发中')
}

const handleViewModeChange = () => {
  // 视图模式已通过 computed 自动更新
}

const handleGroupByChange = () => {
  // 分组方式已通过 computed 自动更新
  // TODO: 根据分组方式重新组织数据
}

const handleApplyFilters = async () => {
  await taskStore.applyFilters()
}

const handleResetFilters = async () => {
  taskStore.resetFilterParams()
  taskStore.resetQueryParams()
  await taskStore.fetchTasks()
}

const handleCreateTask = (status?: TaskStatus) => {
  editingTask.value = null
  defaultStatus.value = status
  formVisible.value = true
}

const handleOpenTask = (task: Task) => {
  selectedTaskId.value = task.id
  detailVisible.value = true
}

const handleGanttTaskClick = (task: GanttTask) => {
  selectedTaskId.value = task.id
  detailVisible.value = true
}

const handleEditTask = (task: Task) => {
  editingTask.value = task
  formVisible.value = true
  detailVisible.value = false
}

const handleDeleteTask = (task: Task) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除任务"${task.title}"吗？`,
    onOk: async () => {
      await taskStore.deleteTask(task.id)
      detailVisible.value = false
    }
  })
}

const handleCloseDetail = () => {
  detailVisible.value = false
  selectedTaskId.value = undefined
}

const handleFormSuccess = async () => {
  await taskStore.fetchTasks()
  await taskStore.fetchStats()
  editingTask.value = null
}

const handleFormCancel = () => {
  editingTask.value = null
}
</script>

<style scoped lang="scss">
.task-page {
  padding: 24px;
  background: #f0f2f5;
  min-height: calc(100vh - 64px);

  .board-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 24px;
    background: #ffffff;
    border-radius: 8px;
    margin-bottom: 16px;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);

    h2 {
      margin: 0;
      font-size: 20px;
      font-weight: 600;
    }

    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;
    }
  }

  .board-stats {
    margin-bottom: 16px;

    :deep(.ant-card) {
      box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
      border-radius: 8px;

      .ant-card-body {
        padding: 20px;
      }
    }
  }

  .board-content {
    background: #ffffff;
    border-radius: 8px;
    padding: 16px;
    min-height: 500px;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  }

  .filter-panel {
    padding: 16px;
    width: 300px;
    background: #ffffff;
  }
}
</style>