<template>
  <div class="bug-page">
    <a-page-header title="缺陷管理" :ghost="false">
      <template #extra>
        <a-space>
          <a-button type="primary" @click="handleCreate">
            <template #icon><PlusOutlined /></template>
            创建缺陷
          </a-button>
          <a-button @click="handleExport">
            <template #icon><ExportOutlined /></template>
            导出
          </a-button>
          <a-segmented
            v-model:value="viewMode"
            :options="[
              { label: '列表视图', value: 'list', icon: h(UnorderedListOutlined) },
              { label: '卡片视图', value: 'card', icon: h(AppstoreOutlined) }
            ]"
          />
        </a-space>
      </template>
    </a-page-header>

    <div class="bug-content">
      <a-row :gutter="16" class="stats-row">
        <a-col :span="6">
          <a-card>
            <a-statistic
              title="缺陷总数"
              :value="statistics?.bugStats.total || 0"
            >
              <template #prefix>
                <BugOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic
              title="待处理"
              :value="statistics?.bugStats.open || 0"
              :value-style="{ color: '#ff4d4f' }"
            >
              <template #prefix>
                <IssuesCloseOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic
              title="处理中"
              :value="statistics?.bugStats.inProgress || 0"
              :value-style="{ color: '#faad14' }"
            >
              <template #prefix>
                <SyncOutlined spin />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic
              title="已解决"
              :value="statistics?.bugStats.resolved || 0"
              :value-style="{ color: '#52c41a' }"
            >
              <template #prefix>
                <CheckCircleOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
      </a-row>

      <a-card class="filter-card" :bordered="false">
        <a-form layout="inline">
          <a-form-item label="搜索">
            <a-input
              v-model:value="queryParams.keyword"
              placeholder="搜索缺陷..."
              style="width: 240px"
              allow-clear
              @press-enter="handleSearch"
            >
              <template #prefix>
                <SearchOutlined />
              </template>
            </a-input>
          </a-form-item>
          <a-form-item label="状态">
            <a-select
              v-model:value="queryParams.status"
              placeholder="全部状态"
              style="width: 140px"
              allow-clear
              @change="handleSearch"
            >
              <a-select-option value="OPEN">待处理</a-select-option>
              <a-select-option value="IN_PROGRESS">处理中</a-select-option>
              <a-select-option value="RESOLVED">已解决</a-select-option>
              <a-select-option value="CLOSED">已关闭</a-select-option>
              <a-select-option value="REOPENED">重新打开</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="严重程度">
            <a-select
              v-model:value="queryParams.severity"
              placeholder="全部"
              style="width: 140px"
              allow-clear
              @change="handleSearch"
            >
              <a-select-option value="BLOCKER">阻塞</a-select-option>
              <a-select-option value="CRITICAL">严重</a-select-option>
              <a-select-option value="MAJOR">主要</a-select-option>
              <a-select-option value="MINOR">次要</a-select-option>
              <a-select-option value="TRIVIAL">轻微</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="优先级">
            <a-select
              v-model:value="queryParams.priority"
              placeholder="全部优先级"
              style="width: 140px"
              allow-clear
              @change="handleSearch"
            >
              <a-select-option value="CRITICAL">紧急</a-select-option>
              <a-select-option value="HIGH">高</a-select-option>
              <a-select-option value="MEDIUM">中</a-select-option>
              <a-select-option value="LOW">低</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item>
            <a-space>
              <a-button type="primary" @click="handleSearch">查询</a-button>
              <a-button @click="handleReset">重置</a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </a-card>

      <a-card v-if="viewMode === 'list'" :bordered="false" class="table-card">
        <a-table
          :columns="columns"
          :data-source="bugList"
          :loading="loading"
          :pagination="{
            current: pagination.page,
            pageSize: pagination.pageSize,
            total: pagination.total,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total: number) => `共 ${total} 条`
          }"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'id'">
              <a @click="handleViewDetail(record)">BUG-{{ record.id }}</a>
            </template>

            <template v-else-if="column.key === 'title'">
              <a @click="handleViewDetail(record)">{{ record.title }}</a>
            </template>

            <template v-else-if="column.key === 'severity'">
              <a-tag :color="getSeverityColor(record.severity)">
                {{ getSeverityText(record.severity) }}
              </a-tag>
            </template>

            <template v-else-if="column.key === 'priority'">
              <a-tag :color="getPriorityColor(record.priority)">
                {{ getPriorityText(record.priority) }}
              </a-tag>
            </template>

            <template v-else-if="column.key === 'status'">
              <a-tag :color="getStatusColor(record.status)">
                {{ getStatusText(record.status) }}
              </a-tag>
            </template>

            <template v-else-if="column.key === 'assignee'">
              <a-avatar
                v-if="record.assignee"
                :size="24"
                :src="record.assignee.avatar"
              >
                {{ record.assignee.name?.charAt(0) }}
              </a-avatar>
              <span v-if="record.assignee" style="margin-left: 8px">
                {{ record.assignee.name }}
              </span>
              <span v-else style="color: #999">未指派</span>
            </template>

            <template v-else-if="column.key === 'actions'">
              <a-space>
                <a-button type="link" size="small" @click="handleEdit(record)">
                  编辑
                </a-button>
                <a-dropdown>
                  <template #overlay>
                    <a-menu @click="({ key }) => handleStatusChange(record, key)">
                      <a-menu-item key="IN_PROGRESS">开始处理</a-menu-item>
                      <a-menu-item key="RESOLVED">标记为已解决</a-menu-item>
                      <a-menu-item key="CLOSED">关闭</a-menu-item>
                      <a-menu-item key="REOPENED">重新打开</a-menu-item>
                    </a-menu>
                  </template>
                  <a-button type="link" size="small">
                    更多 <DownOutlined />
                  </a-button>
                </a-dropdown>
              </a-space>
            </template>
          </template>
        </a-table>
      </a-card>

      <div v-else class="card-view">
        <a-row :gutter="[16, 16]">
          <a-col
            v-for="bug in bugList"
            :key="bug.id"
            :xs="24"
            :sm="12"
            :lg="8"
            :xl="6"
          >
            <a-card
              hoverable
              class="bug-card"
              @click="handleViewDetail(bug)"
            >
              <template #title>
                <a-space>
                  <span>BUG-{{ bug.id }}</span>
                  <a-tag :color="getSeverityColor(bug.severity)" size="small">
                    {{ getSeverityText(bug.severity) }}
                  </a-tag>
                </a-space>
              </template>
              <template #extra>
                <a-tag :color="getStatusColor(bug.status)" size="small">
                  {{ getStatusText(bug.status) }}
                </a-tag>
              </template>
              <div class="bug-card-content">
                <h4 class="bug-title">{{ bug.title }}</h4>
                <p class="bug-description">{{ bug.description }}</p>
                <div class="bug-meta">
                  <a-space>
                    <a-tag :color="getPriorityColor(bug.priority)" size="small">
                      {{ getPriorityText(bug.priority) }}
                    </a-tag>
                    <span v-if="bug.assignee" class="assignee">
                      <a-avatar :size="20" :src="bug.assignee.avatar">
                        {{ bug.assignee.name?.charAt(0) }}
                      </a-avatar>
                      <span style="margin-left: 4px">{{ bug.assignee.name }}</span>
                    </span>
                  </a-space>
                </div>
                <div class="bug-footer">
                  <span class="date">{{ bug.createdAt }}</span>
                </div>
              </div>
            </a-card>
          </a-col>
        </a-row>

        <div class="pagination-wrapper">
          <a-pagination
            v-model:current="pagination.page"
            v-model:page-size="pagination.pageSize"
            :total="pagination.total"
            show-size-changer
            show-quick-jumper
            :show-total="(total: number) => `共 ${total} 条`"
            @change="handlePaginationChange"
          />
        </div>
      </div>
    </div>

    <BugFormModal
      v-model:visible="formModalVisible"
      :bug="currentEditBug"
      @success="handleFormSuccess"
    />

    <BugDetailDrawer
      v-model:visible="detailDrawerVisible"
      :bug-id="currentBugId"
      @edit="handleEdit"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, h, onMounted } from 'vue'
import { useTestStore } from '@/stores/modules/test'
import { storeToRefs } from 'pinia'
import {
  PlusOutlined,
  ExportOutlined,
  UnorderedListOutlined,
  AppstoreOutlined,
  BugOutlined,
  IssuesCloseOutlined,
  SyncOutlined,
  CheckCircleOutlined,
  SearchOutlined,
  DownOutlined
} from '@ant-design/icons-vue'
import type { Bug, BugStatus, BugSeverity, TestCasePriority } from '@/types/test'
import type { TableColumnsType, TableProps } from 'ant-design-vue'
import BugFormModal from './components/BugFormModal.vue'
import BugDetailDrawer from './components/BugDetailDrawer.vue'
import { message } from 'ant-design-vue'

const testStore = useTestStore()
const {
  bugList,
  loading,
  bugPagination: pagination,
  statistics
} = storeToRefs(testStore)

const viewMode = ref<'list' | 'card'>('list')
const queryParams = ref({
  keyword: '',
  status: undefined as BugStatus | undefined,
  severity: undefined as BugSeverity | undefined,
  priority: undefined as TestCasePriority | undefined
})

const formModalVisible = ref(false)
const detailDrawerVisible = ref(false)
const currentEditBug = ref<Bug | null>(null)
const currentBugId = ref<number | null>(null)

const columns: TableColumnsType = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 100
  },
  {
    title: '标题',
    dataIndex: 'title',
    key: 'title',
    ellipsis: true
  },
  {
    title: '严重程度',
    dataIndex: 'severity',
    key: 'severity',
    width: 100
  },
  {
    title: '优先级',
    dataIndex: 'priority',
    key: 'priority',
    width: 100
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 120
  },
  {
    title: '负责人',
    dataIndex: 'assignee',
    key: 'assignee',
    width: 140
  },
  {
    title: '创建时间',
    dataIndex: 'createdAt',
    key: 'createdAt',
    width: 180
  },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    fixed: 'right'
  }
]

const getSeverityColor = (severity: BugSeverity) => {
  const colors = {
    BLOCKER: 'red',
    CRITICAL: 'red',
    MAJOR: 'orange',
    MINOR: 'blue',
    TRIVIAL: 'default'
  }
  return colors[severity]
}

const getSeverityText = (severity: BugSeverity) => {
  const texts = {
    BLOCKER: '阻塞',
    CRITICAL: '严重',
    MAJOR: '主要',
    MINOR: '次要',
    TRIVIAL: '轻微'
  }
  return texts[severity]
}

const getPriorityColor = (priority: TestCasePriority) => {
  const colors = {
    CRITICAL: 'red',
    HIGH: 'orange',
    MEDIUM: 'blue',
    LOW: 'default'
  }
  return colors[priority]
}

const getPriorityText = (priority: TestCasePriority) => {
  const texts = {
    CRITICAL: '紧急',
    HIGH: '高',
    MEDIUM: '中',
    LOW: '低'
  }
  return texts[priority]
}

const getStatusColor = (status: BugStatus) => {
  const colors = {
    OPEN: 'red',
    IN_PROGRESS: 'orange',
    RESOLVED: 'green',
    CLOSED: 'default',
    REOPENED: 'purple'
  }
  return colors[status]
}

const getStatusText = (status: BugStatus) => {
  const texts = {
    OPEN: '待处理',
    IN_PROGRESS: '处理中',
    RESOLVED: '已解决',
    CLOSED: '已关闭',
    REOPENED: '重新打开'
  }
  return texts[status]
}

const handleSearch = () => {
  testStore.setBugQueryParams(queryParams.value)
  testStore.setBugPagination(1)
  loadData()
}

const handleReset = () => {
  queryParams.value = {
    keyword: '',
    status: undefined,
    severity: undefined,
    priority: undefined
  }
  testStore.resetBugQueryParams()
  testStore.setBugPagination(1)
  loadData()
}

const handleTableChange: TableProps['onChange'] = (pag, filters, sorter: any) => {
  testStore.setBugPagination(pag.current || 1, pag.pageSize)
  if (sorter.field) {
    testStore.setBugQueryParams({
      sort: sorter.field,
      order: sorter.order === 'ascend' ? 'asc' : 'desc'
    })
  }
  loadData()
}

const handlePaginationChange = (page: number, pageSize: number) => {
  testStore.setBugPagination(page, pageSize)
  loadData()
}

const handleCreate = () => {
  currentEditBug.value = null
  formModalVisible.value = true
}

const handleEdit = (bug: Bug) => {
  currentEditBug.value = bug
  formModalVisible.value = true
  detailDrawerVisible.value = false
}

const handleViewDetail = (bug: Bug) => {
  currentBugId.value = bug.id
  detailDrawerVisible.value = true
}

const handleStatusChange = async (bug: Bug, status: string) => {
  try {
    await testStore.updateBugStatus(bug.id, status)
  } catch (error) {
    console.error('Update bug status failed:', error)
  }
}

const handleExport = () => {
  message.info('导出功能开发中...')
}

const handleFormSuccess = () => {
  formModalVisible.value = false
  loadData()
}

const loadData = async () => {
  try {
    await testStore.fetchBugs()
    await testStore.fetchStatistics()
  } catch (error) {
    console.error('Load data failed:', error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.bug-page {
  min-height: 100%;
  background: #f0f2f5;

  .bug-content {
    padding: 16px;
  }

  .stats-row {
    margin-bottom: 16px;
  }

  .filter-card {
    margin-bottom: 16px;
  }

  .table-card {
    :deep(.ant-table) {
      .ant-table-thead > tr > th {
        background: #fafafa;
        font-weight: 600;
      }
    }
  }

  .card-view {
    .bug-card {
      height: 100%;

      .bug-card-content {
        .bug-title {
          font-size: 16px;
          font-weight: 600;
          margin-bottom: 8px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .bug-description {
          color: #666;
          margin-bottom: 12px;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          min-height: 40px;
        }

        .bug-meta {
          margin-bottom: 12px;
          padding-bottom: 12px;
          border-bottom: 1px solid #f0f0f0;

          .assignee {
            display: inline-flex;
            align-items: center;
          }
        }

        .bug-footer {
          .date {
            font-size: 12px;
            color: #999;
          }
        }
      }
    }

    .pagination-wrapper {
      display: flex;
      justify-content: center;
      margin-top: 24px;
    }
  }
}
</style>
