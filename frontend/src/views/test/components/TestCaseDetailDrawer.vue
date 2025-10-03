<template>
  <a-drawer
    v-model:open="visible"
    title="测试用例详情"
    width="720"
    :body-style="{ paddingBottom: '80px' }"
  >
    <a-spin :spinning="loading">
      <div v-if="testCase" class="test-case-detail">
        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="ID">
            {{ testCase.id }}
          </a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag :color="getStatusColor(testCase.status)">
              {{ getStatusText(testCase.status) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="类型">
            <a-tag>{{ getTypeText(testCase.type) }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="优先级">
            <a-tag :color="getPriorityColor(testCase.priority)">
              {{ getPriorityText(testCase.priority) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="项目" :span="2">
            {{ testCase.projectName || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="模块" :span="2">
            {{ testCase.moduleName || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="执行人" :span="2">
            <a-avatar
              v-if="testCase.assignee"
              :size="24"
              :src="testCase.assignee.avatar"
            >
              {{ testCase.assignee.name?.charAt(0) }}
            </a-avatar>
            <span v-if="testCase.assignee" style="margin-left: 8px">
              {{ testCase.assignee.name }}
            </span>
            <span v-else style="color: #999">未分配</span>
          </a-descriptions-item>
          <a-descriptions-item label="创建人" :span="2">
            <a-avatar :size="24" :src="testCase.author.avatar">
              {{ testCase.author.name?.charAt(0) }}
            </a-avatar>
            <span style="margin-left: 8px">{{ testCase.author.name }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="执行次数">
            {{ testCase.executionCount }}
          </a-descriptions-item>
          <a-descriptions-item label="通过次数">
            <span style="color: #52c41a">{{ testCase.passedCount }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="失败次数">
            <span style="color: #ff4d4f">{{ testCase.failedCount }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="通过率">
            <a-progress
              :percent="getPassRate()"
              :stroke-color="getPassRateColor()"
              size="small"
              style="width: 120px"
            />
          </a-descriptions-item>
          <a-descriptions-item label="最后执行" :span="2">
            {{ testCase.lastExecutionTime || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="创建时间" :span="2">
            {{ testCase.createdAt }}
          </a-descriptions-item>
          <a-descriptions-item label="更新时间" :span="2">
            {{ testCase.updatedAt }}
          </a-descriptions-item>
        </a-descriptions>

        <a-divider>标题</a-divider>
        <div class="content-section">
          <h3>{{ testCase.title }}</h3>
        </div>

        <a-divider>描述</a-divider>
        <div class="content-section">
          <p>{{ testCase.description }}</p>
        </div>

        <a-divider v-if="testCase.preconditions">前置条件</a-divider>
        <div v-if="testCase.preconditions" class="content-section">
          <p>{{ testCase.preconditions }}</p>
        </div>

        <a-divider>测试步骤</a-divider>
        <div class="test-steps">
          <a-card
            v-for="step in testCase.steps"
            :key="step.id"
            size="small"
            :title="`步骤 ${step.stepNumber}`"
            style="margin-bottom: 12px"
          >
            <p><strong>操作:</strong> {{ step.description }}</p>
            <p><strong>预期结果:</strong> {{ step.expectedResult }}</p>
          </a-card>
        </div>

        <a-divider v-if="testCase.testData">测试数据</a-divider>
        <div v-if="testCase.testData" class="content-section">
          <pre>{{ testCase.testData }}</pre>
        </div>

        <a-divider v-if="testCase.attachments && testCase.attachments.length > 0">
          附件
        </a-divider>
        <div v-if="testCase.attachments && testCase.attachments.length > 0" class="attachments">
          <a-list :data-source="testCase.attachments" size="small">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta>
                  <template #title>
                    <a :href="item.url" target="_blank">
                      <PaperClipOutlined />
                      {{ item.name }}
                    </a>
                  </template>
                  <template #description>
                    {{ formatFileSize(item.size) }}
                  </template>
                </a-list-item-meta>
              </a-list-item>
            </template>
          </a-list>
        </div>

        <a-divider>执行历史</a-divider>
        <div class="execution-history">
          <a-timeline>
            <a-timeline-item
              v-for="execution in executions"
              :key="execution.id"
              :color="getExecutionColor(execution.status)"
            >
              <template #dot>
                <CheckCircleOutlined v-if="execution.status === 'passed'" style="color: #52c41a" />
                <CloseCircleOutlined v-else-if="execution.status === 'failed'" style="color: #ff4d4f" />
                <MinusCircleOutlined v-else-if="execution.status === 'blocked'" style="color: #faad14" />
                <StopOutlined v-else style="color: #d9d9d9" />
              </template>
              <p>
                <a-tag :color="getExecutionColor(execution.status)">
                  {{ getExecutionStatusText(execution.status) }}
                </a-tag>
                <span style="margin-left: 8px">
                  {{ execution.executor.name }} 执行于 {{ execution.executedAt }}
                </span>
              </p>
              <p v-if="execution.notes">备注: {{ execution.notes }}</p>
              <p v-if="execution.duration">耗时: {{ execution.duration }}s</p>
            </a-timeline-item>
          </a-timeline>
          <a-empty v-if="executions.length === 0" description="暂无执行记录" />
        </div>

        <a-divider v-if="testCase.linkedBugs && testCase.linkedBugs.length > 0">
          关联缺陷
        </a-divider>
        <div v-if="testCase.linkedBugs && testCase.linkedBugs.length > 0" class="linked-bugs">
          <a-tag
            v-for="bugId in testCase.linkedBugs"
            :key="bugId"
            color="red"
            style="margin-bottom: 8px"
          >
            BUG-{{ bugId }}
          </a-tag>
        </div>

        <a-divider v-if="testCase.tags && testCase.tags.length > 0">标签</a-divider>
        <div v-if="testCase.tags && testCase.tags.length > 0" class="tags">
          <a-tag
            v-for="tag in testCase.tags"
            :key="tag"
            color="blue"
          >
            {{ tag }}
          </a-tag>
        </div>
      </div>
    </a-spin>

    <template #footer>
      <a-space>
        <a-button @click="handleClose">关闭</a-button>
        <a-button type="primary" @click="handleExecute">
          <template #icon><PlayCircleOutlined /></template>
          执行测试
        </a-button>
        <a-button @click="handleEdit">
          <template #icon><EditOutlined /></template>
          编辑
        </a-button>
        <a-button @click="handleCopy">
          <template #icon><CopyOutlined /></template>
          复制
        </a-button>
        <a-popconfirm
          title="确定删除这个测试用例吗？"
          @confirm="handleDelete"
        >
          <a-button danger>
            <template #icon><DeleteOutlined /></template>
            删除
          </a-button>
        </a-popconfirm>
      </a-space>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useTestStore } from '@/stores/modules/test'
import type { TestCase, TestExecution, TestCaseStatus, TestCasePriority, TestCaseType } from '@/types/test'
import {
  PaperClipOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  MinusCircleOutlined,
  StopOutlined,
  PlayCircleOutlined,
  EditOutlined,
  CopyOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue'

interface Props {
  visible: boolean
  testCaseId: number | null
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:visible': [value: boolean]
  'edit': [testCase: TestCase]
  'execute': [testCase: TestCase]
}>()

const testStore = useTestStore()
const loading = ref(false)
const testCase = ref<TestCase | null>(null)
const executions = ref<TestExecution[]>([])

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

const getPassRate = () => {
  if (!testCase.value || testCase.value.executionCount === 0) return 0
  return Math.round((testCase.value.passedCount / testCase.value.executionCount) * 100)
}

const getPassRateColor = () => {
  const rate = getPassRate()
  if (rate < 60) return '#ff4d4f'
  if (rate < 80) return '#faad14'
  return '#52c41a'
}

const getExecutionColor = (status: string) => {
  const colors = {
    passed: 'green',
    failed: 'red',
    blocked: 'orange',
    skipped: 'default'
  }
  return colors[status] || 'default'
}

const getExecutionStatusText = (status: string) => {
  const texts = {
    passed: '通过',
    failed: '失败',
    blocked: '阻塞',
    skipped: '跳过'
  }
  return texts[status] || status
}

const formatFileSize = (bytes: number) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

const loadData = async () => {
  if (!props.testCaseId) return

  try {
    loading.value = true
    const [testCaseData, executionsData] = await Promise.all([
      testStore.fetchTestCaseDetail(props.testCaseId),
      testStore.fetchTestCaseExecutions(props.testCaseId)
    ])
    testCase.value = testCaseData
    executions.value = executionsData
  } catch (error) {
    console.error('Load data failed:', error)
  } finally {
    loading.value = false
  }
}

const handleClose = () => {
  emit('update:visible', false)
}

const handleEdit = () => {
  if (testCase.value) {
    emit('edit', testCase.value)
  }
}

const handleExecute = () => {
  if (testCase.value) {
    emit('execute', testCase.value)
  }
}

const handleCopy = async () => {
  if (testCase.value) {
    try {
      await testStore.copyTestCase(testCase.value.id)
      emit('update:visible', false)
    } catch (error) {
      console.error('Copy test case failed:', error)
    }
  }
}

const handleDelete = async () => {
  if (testCase.value) {
    try {
      await testStore.deleteTestCase(testCase.value.id)
      emit('update:visible', false)
    } catch (error) {
      console.error('Delete test case failed:', error)
    }
  }
}

watch(
  () => props.visible,
  (val) => {
    if (val) {
      loadData()
    }
  }
)
</script>

<style scoped lang="scss">
.test-case-detail {
  .content-section {
    padding: 12px;
    background: #fafafa;
    border-radius: 4px;
    margin-bottom: 16px;

    h3 {
      margin: 0;
      font-size: 18px;
    }

    p {
      margin: 0;
      white-space: pre-wrap;
    }

    pre {
      margin: 0;
      white-space: pre-wrap;
      word-wrap: break-word;
      font-family: 'Courier New', monospace;
      background: #fff;
      padding: 12px;
      border-radius: 4px;
    }
  }

  .test-steps {
    margin-bottom: 16px;
  }

  .execution-history {
    margin-bottom: 16px;
  }

  .attachments {
    margin-bottom: 16px;
  }

  .linked-bugs {
    margin-bottom: 16px;
  }

  .tags {
    margin-bottom: 16px;
  }
}
</style>
