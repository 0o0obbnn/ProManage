<template>
  <div class="project-detail">
    <!-- 面包屑导航 -->
    <a-breadcrumb class="project-detail__breadcrumb">
      <a-breadcrumb-item>
        <router-link to="/projects">
          <HomeOutlined />
          <span>项目列表</span>
        </router-link>
      </a-breadcrumb-item>
      <a-breadcrumb-item>
        {{ project?.name || '项目详情' }}
      </a-breadcrumb-item>
    </a-breadcrumb>

    <!-- 加载状态 -->
    <div v-if="loading" class="project-detail__loading">
      <a-spin size="large" tip="加载中..." />
    </div>

    <!-- 错误状态 -->
    <a-result
      v-else-if="error"
      status="error"
      title="加载失败"
      :sub-title="errorMessage"
    >
      <template #extra>
        <a-button type="primary" @click="loadProjectDetail">
          重新加载
        </a-button>
        <a-button @click="router.push('/projects')">
          返回列表
        </a-button>
      </template>
    </a-result>

    <!-- 项目详情内容 -->
    <div v-else-if="project" class="project-detail__content">
      <!-- 项目头部 -->
      <div class="project-detail__header">
        <div class="project-detail__header-left">
          <div
            class="project-detail__icon"
            :style="{ backgroundColor: project.color || '#1890ff' }"
          >
            <FolderOutlined />
          </div>
          <div class="project-detail__header-info">
            <h1 class="project-detail__title">{{ project.name }}</h1>
            <p class="project-detail__code">{{ project.code }}</p>
          </div>
        </div>
        <div class="project-detail__header-actions">
          <a-button @click="handleEdit">
            <template #icon>
              <EditOutlined />
            </template>
            编辑
          </a-button>
          <a-dropdown>
            <a-button>
              更多
              <DownOutlined />
            </a-button>
            <template #overlay>
              <a-menu @click="handleMenuClick">
                <a-menu-item key="archive" v-if="project.status !== 3">
                  <InboxOutlined />
                  归档
                </a-menu-item>
                <a-menu-item key="restore" v-if="project.status === 3">
                  <RollbackOutlined />
                  恢复
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item key="delete" danger>
                  <DeleteOutlined />
                  删除
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </div>

      <!-- 标签页 -->
      <a-tabs v-model:activeKey="activeTab" class="project-detail__tabs">
        <!-- 概览 -->
        <a-tab-pane key="overview" tab="概览">
          <a-row :gutter="24">
            <!-- 左侧列 -->
            <a-col :xs="24" :lg="16">
              <!-- 项目基本信息卡片 -->
              <ProjectInfoCard :project="project" @edit="handleEdit" />

              <!-- 项目统计图表 -->
              <ProjectChartsCard :project-id="projectId" />

              <!-- 最近活动时间线 -->
              <ProjectActivityTimeline
                :activities="project.recentActivities || []"
                :loading="loading"
              />
            </a-col>

            <!-- 右侧列 -->
            <a-col :xs="24" :lg="8">
              <!-- 项目统计数据 -->
              <ProjectStatisticsCard
                :statistics="statistics"
                :loading="statisticsLoading"
              />

              <!-- 项目成员列表 -->
              <ProjectMemberList
                :project-id="projectId"
                :members="members"
                :loading="memberLoading"
                @refresh="loadMembers"
              />
            </a-col>
          </a-row>
        </a-tab-pane>

        <!-- 看板 -->
        <a-tab-pane key="kanban" tab="看板">
          <ProjectKanban
            :project-id="projectId"
            @create-task="handleCreateTask"
            @task-click="handleTaskClick"
          />
        </a-tab-pane>

        <!-- 任务列表 -->
        <a-tab-pane key="tasks" tab="任务列表">
          <ProjectTaskList
            :project-id="projectId"
            @create-task="handleCreateTask"
            @task-click="handleTaskClick"
          />
        </a-tab-pane>

        <!-- 文档 -->
        <a-tab-pane key="documents" tab="文档">
          <ProjectDocumentList
            :project-id="projectId"
          />
        </a-tab-pane>

        <!-- 设置 -->
        <a-tab-pane key="settings" tab="设置">
          <ProjectSettingsTab
            :project="project"
            @refresh="loadProjectDetail"
          />
        </a-tab-pane>
      </a-tabs>
    </div>

    <!-- 编辑项目弹窗 -->
    <ProjectFormModal
      v-model:visible="formModalVisible"
      :project="project"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Modal, message } from 'ant-design-vue'
import { storeToRefs } from 'pinia'
import {
  HomeOutlined,
  FolderOutlined,
  EditOutlined,
  DownOutlined,
  InboxOutlined,
  RollbackOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue'
import { useProjectStore } from '@/stores/modules/project'
import ProjectInfoCard from './components/ProjectInfoCard.vue'
import ProjectStatisticsCard from './components/ProjectStatisticsCard.vue'
import ProjectMemberList from './components/ProjectMemberList.vue'
import ProjectActivityTimeline from './components/ProjectActivityTimeline.vue'
import ProjectFormModal from './components/ProjectFormModal.vue'
import ProjectChartsCard from './components/ProjectChartsCard.vue'
import ProjectKanban from '@/components/business/ProjectKanban.vue'
import ProjectTaskList from './components/ProjectTaskList.vue'
import ProjectDocumentList from './components/ProjectDocumentList.vue'
import ProjectSettingsTab from './components/ProjectSettingsTab.vue'
import type { Task } from '@/types/task'

const route = useRoute()
const router = useRouter()
const projectStore = useProjectStore()

// 响应式数据
const loading = ref(false)
const statisticsLoading = ref(false)
const error = ref(false)
const errorMessage = ref('')
const formModalVisible = ref(false)
const activeTab = ref('overview')

// Store数据
const { currentProject, projectMembers, projectStatistics, memberLoading } = storeToRefs(projectStore)

// 计算属性
const projectId = computed(() => Number(route.params.id))
const project = computed(() => currentProject.value)
const members = computed(() => projectMembers.value)
const statistics = computed(() => projectStatistics.value)

// 方法

/**
 * 加载项目详情
 */
const loadProjectDetail = async () => {
  try {
    loading.value = true
    error.value = false
    errorMessage.value = ''

    // 加载项目详情
    await projectStore.fetchProjectDetail(projectId.value)

    // 并行加载成员和统计信息
    await Promise.all([
      loadProjectMembers(),
      loadProjectStatistics()
    ])
  } catch (err: any) {
    error.value = true
    errorMessage.value = err.message || '加载项目详情失败'
    console.error('Load project detail failed:', err)
  } finally {
    loading.value = false
  }
}

/**
 * 加载项目成员
 */
const loadProjectMembers = async () => {
  try {
    await projectStore.fetchProjectMembers(projectId.value)
  } catch (err) {
    console.error('Load project members failed:', err)
  }
}

/**
 * 加载项目统计信息
 */
const loadProjectStatistics = async () => {
  try {
    statisticsLoading.value = true
    await projectStore.fetchProjectStatistics(projectId.value)
  } catch (err) {
    console.error('Load project statistics failed:', err)
  } finally {
    statisticsLoading.value = false
  }
}

/**
 * 处理编辑项目
 */
const handleEdit = () => {
  formModalVisible.value = true
}

/**
 * 处理菜单点击
 */
const handleMenuClick = ({ key }: { key: string }) => {
  switch (key) {
    case 'archive':
      handleArchive()
      break
    case 'restore':
      handleRestore()
      break
    case 'delete':
      handleDelete()
      break
  }
}

/**
 * 处理归档项目
 */
const handleArchive = () => {
  Modal.confirm({
    title: '确认归档',
    content: '确定要归档这个项目吗?归档后可以恢复。',
    onOk: async () => {
      try {
        await projectStore.archiveProjectById(projectId.value)
        await loadProjectDetail()
      } catch (error) {
        console.error('Archive project failed:', error)
      }
    }
  })
}

/**
 * 处理恢复项目
 */
const handleRestore = async () => {
  try {
    await projectStore.restoreProjectById(projectId.value)
    await loadProjectDetail()
  } catch (error) {
    console.error('Restore project failed:', error)
  }
}

/**
 * 处理删除项目
 */
const handleDelete = () => {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这个项目吗?此操作不可恢复!',
    okText: '确定',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        await projectStore.deleteProjectById(projectId.value)
        message.success('项目删除成功')
        router.push('/projects')
      } catch (error) {
        console.error('Delete project failed:', error)
      }
    }
  })
}

/**
 * 处理表单提交成功
 */
const handleFormSuccess = () => {
  formModalVisible.value = false
  loadProjectDetail()
}

/**
 * 处理创建任务
 */
const handleCreateTask = () => {
  // TODO: 实现创建任务功能
  message.info('创建任务功能待实现')
}

/**
 * 处理任务点击
 */
const handleTaskClick = (task: Task) => {
  // TODO: 实现任务详情功能
  message.info(`查看任务: ${task.title}`)
}

// 监听路由参数变化
watch(() => route.params.id, (newId) => {
  if (newId) {
    loadProjectDetail()
  }
})

// 生命周期
onMounted(() => {
  loadProjectDetail()
})
</script>

<style lang="scss" scoped>
.project-detail {
  padding: 24px;
  min-height: calc(100vh - 64px);
  background-color: #f0f2f5;

  &__breadcrumb {
    margin-bottom: 24px;
    padding: 16px 24px;
    background-color: white;
    border-radius: 8px;

    a {
      display: inline-flex;
      align-items: center;
      gap: 8px;
    }
  }

  &__loading {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 400px;
  }

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    padding: 24px;
    background-color: white;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

    &-left {
      display: flex;
      align-items: center;
      gap: 16px;
    }

    &-info {
      flex: 1;
    }

    &-actions {
      display: flex;
      gap: 12px;
    }
  }

  &__icon {
    width: 64px;
    height: 64px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 32px;
    flex-shrink: 0;
  }

  &__title {
    margin: 0;
    font-size: 24px;
    font-weight: 600;
    color: #262626;
  }

  &__code {
    margin: 4px 0 0;
    font-size: 14px;
    color: #8c8c8c;
  }

  &__tabs {
    background: white;
    border-radius: 8px;
    padding: 0 24px;

    :deep(.ant-tabs-content) {
      padding: 24px 0;
    }
  }

  &__placeholder {
    min-height: 400px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}
</style>

