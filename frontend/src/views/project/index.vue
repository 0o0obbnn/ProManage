<template>
  <div class="project-list">
    <!-- 页面头部 -->
    <div class="project-list__header">
      <div class="project-list__title">
        <h2>项目列表</h2>
        <p>管理和查看您的所有项目</p>
      </div>
      <a-button type="primary" size="large" @click="handleCreateProject">
        <template #icon>
          <PlusOutlined />
        </template>
        创建项目
      </a-button>
    </div>

    <!-- 搜索和筛选 -->
    <div class="project-list__filters">
      <a-row :gutter="16">
        <a-col :xs="24" :sm="12" :md="8" :lg="6">
          <a-input-search
            v-model:value="searchKeyword"
            placeholder="搜索项目名称或编码"
            allow-clear
            @search="handleSearch"
          >
            <template #prefix>
              <SearchOutlined />
            </template>
          </a-input-search>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="6">
          <a-select
            v-model:value="filterStatus"
            placeholder="项目状态"
            allow-clear
            style="width: 100%"
            @change="handleFilterChange"
          >
            <a-select-option :value="0">规划中</a-select-option>
            <a-select-option :value="1">进行中</a-select-option>
            <a-select-option :value="2">已完成</a-select-option>
            <a-select-option :value="3">已归档</a-select-option>
            <a-select-option :value="4">已暂停</a-select-option>
          </a-select>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="6">
          <a-select
            v-model:value="filterPriority"
            placeholder="优先级"
            allow-clear
            style="width: 100%"
            @change="handleFilterChange"
          >
            <a-select-option :value="0">低优先级</a-select-option>
            <a-select-option :value="1">中优先级</a-select-option>
            <a-select-option :value="2">高优先级</a-select-option>
            <a-select-option :value="3">紧急</a-select-option>
          </a-select>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="6">
          <a-select
            v-model:value="sortField"
            placeholder="排序方式"
            style="width: 100%"
            @change="handleSortChange"
          >
            <a-select-option value="updatedAt">最近更新</a-select-option>
            <a-select-option value="createdAt">创建时间</a-select-option>
            <a-select-option value="name">项目名称</a-select-option>
            <a-select-option value="progress">项目进度</a-select-option>
          </a-select>
        </a-col>
      </a-row>
    </div>

    <!-- 视图切换 -->
    <div class="project-list__toolbar">
      <a-radio-group v-model:value="viewMode" button-style="solid">
        <a-radio-button value="grid">
          <AppstoreOutlined />
          网格视图
        </a-radio-button>
        <a-radio-button value="list">
          <UnorderedListOutlined />
          列表视图
        </a-radio-button>
      </a-radio-group>
      <div class="project-list__stats">
        共 <span class="highlight">{{ pagination.total }}</span> 个项目
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="project-list__loading">
      <a-spin size="large" tip="加载中..." />
    </div>

    <!-- 空状态 -->
    <a-empty
      v-else-if="!hasProjects"
      description="暂无项目"
      class="project-list__empty"
    >
      <a-button type="primary" @click="handleCreateProject">
        <template #icon>
          <PlusOutlined />
        </template>
        创建第一个项目
      </a-button>
    </a-empty>

    <!-- 项目列表 - 网格视图 -->
    <div v-else-if="viewMode === 'grid'" class="project-list__grid">
      <a-row :gutter="[16, 16]">
        <a-col
          v-for="project in projectList"
          :key="project.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
          :xl="6"
        >
          <ProjectCard
            :project="project"
            @click="handleProjectClick"
            @edit="handleEditProject"
            @delete="handleDeleteProject"
            @archive="handleArchiveProject"
            @restore="handleRestoreProject"
          />
        </a-col>
      </a-row>
    </div>

    <!-- 项目列表 - 列表视图 -->
    <a-table
      v-else
      :columns="columns"
      :data-source="projectList"
      :loading="loading"
      :pagination="false"
      row-key="id"
      class="project-list__table"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'name'">
          <div class="project-name">
            <div
              class="project-icon"
              :style="{ backgroundColor: record.color || '#1890ff' }"
            >
              <FolderOutlined />
            </div>
            <div>
              <div class="project-title">{{ record.name }}</div>
              <div class="project-code">{{ record.code }}</div>
            </div>
          </div>
        </template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="getStatusColor(record.status)">
            {{ getStatusText(record.status) }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'priority'">
          <a-tag v-if="record.priority !== undefined" :color="getPriorityColor(record.priority)">
            {{ getPriorityText(record.priority) }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'progress'">
          <a-progress :percent="record.progress" size="small" />
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="handleProjectClick(record)">
              查看
            </a-button>
            <a-button type="link" size="small" @click="handleEditProject(record)">
              编辑
            </a-button>
            <a-popconfirm
              title="确定要删除这个项目吗?"
              ok-text="确定"
              cancel-text="取消"
              @confirm="handleDeleteProject(record)"
            >
              <a-button type="link" size="small" danger>
                删除
              </a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 分页 -->
    <div v-if="hasProjects && pagination.total > pagination.pageSize" class="project-list__pagination">
      <a-pagination
        v-model:current="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :show-size-changer="true"
        :show-total="(total) => `共 ${total} 条`"
        :page-size-options="['12', '24', '48', '96']"
        @change="handlePageChange"
        @show-size-change="handlePageSizeChange"
      />
    </div>

    <!-- 创建/编辑项目弹窗 -->
    <ProjectFormModal
      v-model:visible="formModalVisible"
      :project="editingProject"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, defineAsyncComponent } from 'vue'
import { useRouter } from 'vue-router'
import { Modal, message } from 'ant-design-vue'
import { storeToRefs } from 'pinia'
import {
  PlusOutlined,
  SearchOutlined,
  AppstoreOutlined,
  UnorderedListOutlined,
  FolderOutlined
} from '@ant-design/icons-vue'
import { useProjectStore } from '@/stores/modules/project'
import ProjectCard from '@/components/business/ProjectCard.vue'
import type { Project, ProjectStatus, ProjectPriority } from '@/types/project'

// 懒加载表单弹窗组件
const ProjectFormModal = defineAsyncComponent(
  () => import('./components/ProjectFormModal.vue')
)

const router = useRouter()
const projectStore = useProjectStore()

// 响应式数据
const searchKeyword = ref('')
const filterStatus = ref<ProjectStatus | undefined>(undefined)
const filterPriority = ref<ProjectPriority | undefined>(undefined)
const sortField = ref('updatedAt')
const sortOrder = ref<'asc' | 'desc'>('desc')
const viewMode = ref<'grid' | 'list'>('grid')
const formModalVisible = ref(false)
const editingProject = ref<Project | null>(null)

// 计算属性
const { projectList, loading, pagination, hasProjects } = storeToRefs(projectStore)

// 表格列定义
const columns = [
  {
    title: '项目名称',
    key: 'name',
    dataIndex: 'name',
    width: 250
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
    title: '进度',
    key: 'progress',
    dataIndex: 'progress',
    width: 150
  },
  {
    title: '成员',
    key: 'memberCount',
    dataIndex: 'memberCount',
    width: 80
  },
  {
    title: '任务',
    key: 'taskCount',
    dataIndex: 'taskCount',
    width: 80
  },
  {
    title: '更新时间',
    key: 'updatedAt',
    dataIndex: 'updatedAt',
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right'
  }
]

// 方法
const fetchProjects = async () => {
  await projectStore.fetchProjects({
    keyword: searchKeyword.value,
    status: filterStatus.value,
    priority: filterPriority.value,
    sort: sortField.value,
    order: sortOrder.value
  })
}

const handleSearch = () => {
  pagination.value.page = 1
  fetchProjects()
}

const handleFilterChange = () => {
  pagination.value.page = 1
  fetchProjects()
}

const handleSortChange = () => {
  fetchProjects()
}

const handlePageChange = (page: number) => {
  projectStore.setPagination(page)
  fetchProjects()
}

const handlePageSizeChange = (current: number, size: number) => {
  projectStore.setPagination(1, size)
  fetchProjects()
}

const handleCreateProject = () => {
  editingProject.value = null
  formModalVisible.value = true
}

const handleProjectClick = (project: Project) => {
  router.push(`/projects/${project.id}`)
}

const handleEditProject = (project: Project) => {
  editingProject.value = project
  formModalVisible.value = true
}

const handleDeleteProject = async (project: Project) => {
  try {
    await projectStore.deleteProjectById(project.id)
    message.success('项目删除成功')
    fetchProjects()
  } catch (error) {
    console.error('Delete project failed:', error)
  }
}

const handleArchiveProject = async (project: Project) => {
  Modal.confirm({
    title: '确认归档',
    content: '确定要归档这个项目吗?归档后可以恢复。',
    onOk: async () => {
      try {
        await projectStore.archiveProjectById(project.id)
        fetchProjects()
      } catch (error) {
        console.error('Archive project failed:', error)
      }
    }
  })
}

const handleRestoreProject = async (project: Project) => {
  try {
    await projectStore.restoreProjectById(project.id)
    fetchProjects()
  } catch (error) {
    console.error('Restore project failed:', error)
  }
}

const handleFormSuccess = () => {
  formModalVisible.value = false
  fetchProjects()
}

// 辅助函数
const getStatusColor = (status: ProjectStatus): string => {
  const colorMap: Record<ProjectStatus, string> = {
    0: 'default',
    1: 'processing',
    2: 'success',
    3: 'warning',
    4: 'error'
  }
  return colorMap[status] || 'default'
}

const getStatusText = (status: ProjectStatus): string => {
  const textMap: Record<ProjectStatus, string> = {
    0: '规划中',
    1: '进行中',
    2: '已完成',
    3: '已归档',
    4: '已暂停'
  }
  return textMap[status] || '未知'
}

const getPriorityColor = (priority: ProjectPriority): string => {
  const colorMap: Record<ProjectPriority, string> = {
    0: 'default',
    1: 'blue',
    2: 'orange',
    3: 'red'
  }
  return colorMap[priority] || 'default'
}

const getPriorityText = (priority: ProjectPriority): string => {
  const textMap: Record<ProjectPriority, string> = {
    0: '低优先级',
    1: '中优先级',
    2: '高优先级',
    3: '紧急'
  }
  return textMap[priority] || '未知'
}

// 生命周期
onMounted(() => {
  fetchProjects()
})
</script>

<style lang="scss" scoped>
.project-list {
  padding: 24px;

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
  }

  &__title {
    h2 {
      margin: 0;
      font-size: 24px;
      font-weight: 600;
    }

    p {
      margin: 4px 0 0;
      color: #8c8c8c;
    }
  }

  &__filters {
    margin-bottom: 24px;
  }

  &__toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
  }

  &__stats {
    color: #595959;

    .highlight {
      color: #1890ff;
      font-weight: 600;
      font-size: 18px;
    }
  }

  &__loading {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 400px;
  }

  &__empty {
    min-height: 400px;
    display: flex;
    flex-direction: column;
    justify-content: center;
  }

  &__grid {
    margin-bottom: 24px;
  }

  &__table {
    margin-bottom: 24px;

    .project-name {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .project-icon {
      width: 32px;
      height: 32px;
      border-radius: 6px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 16px;
    }

    .project-title {
      font-weight: 500;
    }

    .project-code {
      font-size: 12px;
      color: #8c8c8c;
    }
  }

  &__pagination {
    display: flex;
    justify-content: flex-end;
  }
}
</style>

