<template>
  <div class="test-case-page">
    <a-page-header title="测试管理" :ghost="false">
      <template #extra>
        <a-space>
          <a-button type="primary" @click="handleCreate">
            <template #icon><PlusOutlined /></template>
            创建用例
          </a-button>
          <a-button @click="handleImport">
            <template #icon><ImportOutlined /></template>
            导入
          </a-button>
          <a-button @click="handleExport">
            <template #icon><ExportOutlined /></template>
            导出
          </a-button>
          <a-button
            v-if="selectedTestCaseCount > 0"
            @click="handleBatchExecute"
          >
            <template #icon><PlayCircleOutlined /></template>
            批量执行 ({{ selectedTestCaseCount }})
          </a-button>
        </a-space>
      </template>
    </a-page-header>

    <div class="test-case-content">
      <a-row :gutter="16" class="stats-row">
        <a-col :span="6">
          <a-card>
            <a-statistic
              title="测试用例总数"
              :value="statistics?.testCaseStats.total || 0"
            >
              <template #prefix>
                <FileTextOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic
              title="通过率"
              :value="statistics?.testCaseStats.passRate || 0"
              suffix="%"
              :value-style="{ color: '#3f8600' }"
            >
              <template #prefix>
                <CheckCircleOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic
              title="未执行"
              :value="(statistics?.testCaseStats.draft || 0) + (statistics?.testCaseStats.ready || 0)"
            >
              <template #prefix>
                <ClockCircleOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic
              title="失败数"
              :value="statistics?.testCaseStats.failed || 0"
              :value-style="{ color: '#cf1322' }"
            >
              <template #prefix>
                <CloseCircleOutlined />
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
              placeholder="搜索测试用例..."
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
              <a-select-option value="DRAFT">草稿</a-select-option>
              <a-select-option value="READY">就绪</a-select-option>
              <a-select-option value="PASSED">通过</a-select-option>
              <a-select-option value="FAILED">失败</a-select-option>
              <a-select-option value="BLOCKED">阻塞</a-select-option>
              <a-select-option value="SKIPPED">跳过</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="类型">
            <a-select
              v-model:value="queryParams.type"
              placeholder="全部类型"
              style="width: 140px"
              allow-clear
              @change="handleSearch"
            >
              <a-select-option value="FUNCTIONAL">功能</a-select-option>
              <a-select-option value="PERFORMANCE">性能</a-select-option>
              <a-select-option value="SECURITY">安全</a-select-option>
              <a-select-option value="INTEGRATION">集成</a-select-option>
              <a-select-option value="UNIT">单元</a-select-option>
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

      <a-card :bordered="false" class="table-card">
        <a-table
          :columns="columns"
          :data-source="testCaseList"
          :loading="loading"
          :pagination="{
            current: pagination.page,
            pageSize: pagination.pageSize,
            total: pagination.total,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total: number) => `共 ${total} 条`
          }"
          :row-selection="{
            selectedRowKeys: selectedTestCaseIds,
            onChange: handleSelectionChange
          }"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'title'">
              <a @click="handleViewDetail(record)">{{ record.title }}</a>
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

            <template v-else-if="column.key === 'type'">
              <a-tag>{{ getTypeText(record.type) }}</a-tag>
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
              <span v-else style="color: #999">未分配</span>
            </template>

            <template v-else-if="column.key === 'passRate'">
              <a-progress
                :percent="getPassRate(record)"
                :status="getPassRateStatus(record)"
                :stroke-color="getPassRateColor(record)"
                size="small"
              />
            </template>

            <template v-else-if="column.key === 'actions'">
              <a-space>
                <a-button type="link" size="small" @click="handleExecute(record)">
                  执行
                </a-button>
                <a-button type="link" size="small" @click="handleEdit(record)">
                  编辑
                </a-button>
                <a-button type="link" size="small" @click="handleCopy(record)">
                  复制
                </a-button>
                <a-popconfirm
                  title="确定删除这个测试用例吗？"
                  @confirm="handleDelete(record)"
                >
                  <a-button type="link" size="small" danger>删除</a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </template>
        </a-table>
      </a-card>
    </div>

    <TestCaseFormModal
      v-model:visible="formModalVisible"
      :test-case="currentEditTestCase"
      @success="handleFormSuccess"
    />

    <TestCaseDetailDrawer
      v-model:visible="detailDrawerVisible"
      :test-case-id="currentTestCaseId"
      @edit="handleEdit"
      @execute="handleExecute"
    />

    <TestExecutionModal
      v-model:visible="executionModalVisible"
      :test-case="currentExecuteTestCase"
      @success="handleExecutionSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useTestStore } from '@/stores/modules/test'
import { storeToRefs } from 'pinia'
import {
  PlusOutlined,
  ImportOutlined,
  ExportOutlined,
  PlayCircleOutlined,
  FileTextOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  CloseCircleOutlined,
  SearchOutlined
} from '@ant-design/icons-vue'
import type { TestCase, TestCaseStatus, TestCasePriority, TestCaseType } from '@/types/test'
import type { TableColumnsType, TableProps } from 'ant-design-vue'
import TestCaseFormModal from './components/TestCaseFormModal.vue'
import TestCaseDetailDrawer from './components/TestCaseDetailDrawer.vue'
import TestExecutionModal from './components/TestExecutionModal.vue'
import { message } from 'ant-design-vue'

const testStore = useTestStore()
const {
  testCaseList,
  loading,
  testCasePagination: pagination,
  selectedTestCaseIds,
  selectedTestCaseCount,
  statistics
} = storeToRefs(testStore)

const queryParams = ref({
  keyword: '',
  status: undefined as TestCaseStatus | undefined,
  type: undefined as TestCaseType | undefined,
  priority: undefined as TestCasePriority | undefined
})

const formModalVisible = ref(false)
const detailDrawerVisible = ref(false)
const executionModalVisible = ref(false)
const currentEditTestCase = ref<TestCase | null>(null)
const currentTestCaseId = ref<number | null>(null)
const currentExecuteTestCase = ref<TestCase | null>(null)

const columns: TableColumnsType = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 80
  },
  {
    title: '标题',
    dataIndex: 'title',
    key: 'title',
    ellipsis: true
  },
  {
    title: '类型',
    dataIndex: 'type',
    key: 'type',
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
    width: 100
  },
  {
    title: '执行人',
    dataIndex: 'assignee',
    key: 'assignee',
    width: 140
  },
  {
    title: '通过率',
    key: 'passRate',
    width: 150
  },
  {
    title: '操作',
    key: 'actions',
    width: 240,
    fixed: 'right'
  }
]

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

const getStatusColor = (status: TestCaseStatus) => {
  const colors = {
    DRAFT: 'default',
    READY: 'blue',
    PASSED: 'green',
    FAILED: 'red',
    BLOCKED: 'orange',
    SKIPPED: 'purple'
  }
  return colors[status]
}

const getStatusText = (status: TestCaseStatus) => {
  const texts = {
    DRAFT: '草稿',
    READY: '就绪',
    PASSED: '通过',
    FAILED: '失败',
    BLOCKED: '阻塞',
    SKIPPED: '跳过'
  }
  return texts[status]
}

const getTypeText = (type: TestCaseType) => {
  const texts = {
    FUNCTIONAL: '功能',
    PERFORMANCE: '性能',
    SECURITY: '安全',
    INTEGRATION: '集成',
    UNIT: '单元'
  }
  return texts[type]
}

const getPassRate = (record: TestCase) => {
  if (record.executionCount === 0) return 0
  return Math.round((record.passedCount / record.executionCount) * 100)
}

const getPassRateStatus = (record: TestCase) => {
  const rate = getPassRate(record)
  if (rate === 0) return 'normal'
  if (rate < 60) return 'exception'
  if (rate < 80) return 'normal'
  return 'success'
}

const getPassRateColor = (record: TestCase) => {
  const rate = getPassRate(record)
  if (rate < 60) return '#ff4d4f'
  if (rate < 80) return '#faad14'
  return '#52c41a'
}

const handleSearch = () => {
  testStore.setTestCaseQueryParams(queryParams.value)
  testStore.setTestCasePagination(1)
  loadData()
}

const handleReset = () => {
  queryParams.value = {
    keyword: '',
    status: undefined,
    type: undefined,
    priority: undefined
  }
  testStore.resetTestCaseQueryParams()
  testStore.setTestCasePagination(1)
  loadData()
}

const handleTableChange: TableProps['onChange'] = (pag, filters, sorter: any) => {
  testStore.setTestCasePagination(pag.current || 1, pag.pageSize)
  if (sorter.field) {
    testStore.setTestCaseQueryParams({
      sort: sorter.field,
      order: sorter.order === 'ascend' ? 'asc' : 'desc'
    })
  }
  loadData()
}

const handleSelectionChange = (selectedKeys: number[]) => {
  testStore.clearTestCaseSelection()
  selectedKeys.forEach(key => testStore.toggleTestCaseSelect(key))
}

const handleCreate = () => {
  currentEditTestCase.value = null
  formModalVisible.value = true
}

const handleEdit = (testCase: TestCase) => {
  currentEditTestCase.value = testCase
  formModalVisible.value = true
  detailDrawerVisible.value = false
}

const handleViewDetail = (testCase: TestCase) => {
  currentTestCaseId.value = testCase.id
  detailDrawerVisible.value = true
}

const handleExecute = (testCase: TestCase) => {
  currentExecuteTestCase.value = testCase
  executionModalVisible.value = true
  detailDrawerVisible.value = false
}

const handleCopy = async (testCase: TestCase) => {
  try {
    await testStore.copyTestCase(testCase.id)
  } catch (error) {
    console.error('Copy test case failed:', error)
  }
}

const handleDelete = async (testCase: TestCase) => {
  try {
    await testStore.deleteTestCase(testCase.id)
  } catch (error) {
    console.error('Delete test case failed:', error)
  }
}

const handleBatchExecute = () => {
  if (selectedTestCaseIds.value.length === 0) {
    message.warning('请先选择测试用例')
    return
  }
  message.info('批量执行功能开发中...')
}

const handleImport = () => {
  message.info('导入功能开发中...')
}

const handleExport = () => {
  message.info('导出功能开发中...')
}

const handleFormSuccess = () => {
  formModalVisible.value = false
  loadData()
}

const handleExecutionSuccess = () => {
  executionModalVisible.value = false
  loadData()
}

const loadData = async () => {
  try {
    await testStore.fetchTestCases()
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
.test-case-page {
  min-height: 100%;
  background: #f0f2f5;

  .test-case-content {
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
}
</style>
