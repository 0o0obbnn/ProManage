<template>
  <a-drawer
    v-model:open="visible"
    title="缺陷详情"
    width="720"
    :body-style="{ paddingBottom: '80px' }"
  >
    <a-spin :spinning="loading">
      <div v-if="bug" class="bug-detail">
        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="ID">
            BUG-{{ bug.id }}
          </a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag :color="getStatusColor(bug.status)">
              {{ getStatusText(bug.status) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="严重程度">
            <a-tag :color="getSeverityColor(bug.severity)">
              {{ getSeverityText(bug.severity) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="优先级">
            <a-tag :color="getPriorityColor(bug.priority)">
              {{ getPriorityText(bug.priority) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="项目" :span="2">
            {{ bug.projectName || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="模块" :span="2">
            {{ bug.moduleName || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="负责人" :span="2">
            <a-avatar
              v-if="bug.assignee"
              :size="24"
              :src="bug.assignee.avatar"
            >
              {{ bug.assignee.name?.charAt(0) }}
            </a-avatar>
            <span v-if="bug.assignee" style="margin-left: 8px">
              {{ bug.assignee.name }}
            </span>
            <span v-else style="color: #999">未指派</span>
          </a-descriptions-item>
          <a-descriptions-item label="报告人" :span="2">
            <a-avatar :size="24" :src="bug.reporter.avatar">
              {{ bug.reporter.name?.charAt(0) }}
            </a-avatar>
            <span style="margin-left: 8px">{{ bug.reporter.name }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="创建时间" :span="2">
            {{ bug.createdAt }}
          </a-descriptions-item>
          <a-descriptions-item label="更新时间" :span="2">
            {{ bug.updatedAt }}
          </a-descriptions-item>
          <a-descriptions-item v-if="bug.resolvedAt" label="解决时间" :span="2">
            {{ bug.resolvedAt }}
          </a-descriptions-item>
          <a-descriptions-item v-if="bug.closedAt" label="关闭时间" :span="2">
            {{ bug.closedAt }}
          </a-descriptions-item>
        </a-descriptions>

        <a-divider>标题</a-divider>
        <div class="content-section">
          <h3>{{ bug.title }}</h3>
        </div>

        <a-divider>描述</a-divider>
        <div class="content-section">
          <p>{{ bug.description }}</p>
        </div>

        <a-divider v-if="bug.environment">测试环境</a-divider>
        <div v-if="bug.environment" class="content-section">
          <p>{{ bug.environment }}</p>
        </div>

        <a-divider>重现步骤</a-divider>
        <div class="reproduce-steps">
          <ol>
            <li v-for="(step, index) in bug.stepsToReproduce" :key="index">
              {{ step }}
            </li>
          </ol>
        </div>

        <a-divider v-if="bug.expectedBehavior">预期行为</a-divider>
        <div v-if="bug.expectedBehavior" class="content-section">
          <p>{{ bug.expectedBehavior }}</p>
        </div>

        <a-divider v-if="bug.actualBehavior">实际行为</a-divider>
        <div v-if="bug.actualBehavior" class="content-section">
          <p>{{ bug.actualBehavior }}</p>
        </div>

        <a-divider v-if="bug.attachments && bug.attachments.length > 0">
          附件
        </a-divider>
        <div v-if="bug.attachments && bug.attachments.length > 0" class="attachments">
          <a-list :data-source="bug.attachments" size="small">
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

        <a-divider v-if="bug.linkedTestCases && bug.linkedTestCases.length > 0">
          关联测试用例
        </a-divider>
        <div v-if="bug.linkedTestCases && bug.linkedTestCases.length > 0" class="linked-test-cases">
          <a-tag
            v-for="tcId in bug.linkedTestCases"
            :key="tcId"
            color="blue"
            style="margin-bottom: 8px"
          >
            TC-{{ tcId }}
          </a-tag>
        </div>

        <a-divider v-if="bug.statusHistory && bug.statusHistory.length > 0">
          状态变更历史
        </a-divider>
        <div v-if="bug.statusHistory && bug.statusHistory.length > 0" class="status-history">
          <a-timeline>
            <a-timeline-item
              v-for="(history, index) in bug.statusHistory"
              :key="index"
              :color="getStatusColor(history.status)"
            >
              <p>
                <a-tag :color="getStatusColor(history.status)">
                  {{ getStatusText(history.status) }}
                </a-tag>
                <span style="margin-left: 8px">
                  {{ history.changedBy }} 于 {{ history.changedAt }}
                </span>
              </p>
              <p v-if="history.comment">备注: {{ history.comment }}</p>
            </a-timeline-item>
          </a-timeline>
        </div>

        <a-divider>评论</a-divider>
        <div class="comments-section">
          <a-list
            v-if="bug.comments && bug.comments.length > 0"
            :data-source="bug.comments"
            item-layout="horizontal"
          >
            <template #renderItem="{ item }">
              <a-list-item>
                <a-comment
                  :author="item.author.name"
                  :avatar="item.author.avatar"
                  :content="item.content"
                  :datetime="item.createdAt"
                >
                  <template v-if="item.attachments && item.attachments.length > 0" #actions>
                    <span v-for="file in item.attachments" :key="file.id">
                      <a :href="file.url" target="_blank">
                        <PaperClipOutlined /> {{ file.name }}
                      </a>
                    </span>
                  </template>
                </a-comment>
              </a-list-item>
            </template>
          </a-list>
          <a-empty v-else description="暂无评论" />

          <a-form @submit.prevent="handleAddComment">
            <a-form-item>
              <a-textarea
                v-model:value="newComment"
                placeholder="添加评论..."
                :rows="3"
              />
            </a-form-item>
            <a-form-item>
              <a-button
                type="primary"
                html-type="submit"
                :loading="addingComment"
                :disabled="!newComment.trim()"
              >
                添加评论
              </a-button>
            </a-form-item>
          </a-form>
        </div>

        <a-divider v-if="bug.tags && bug.tags.length > 0">标签</a-divider>
        <div v-if="bug.tags && bug.tags.length > 0" class="tags">
          <a-tag
            v-for="tag in bug.tags"
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
        <a-dropdown>
          <template #overlay>
            <a-menu @click="handleStatusMenuClick">
              <a-menu-item key="IN_PROGRESS">
                <SyncOutlined /> 开始处理
              </a-menu-item>
              <a-menu-item key="RESOLVED">
                <CheckCircleOutlined /> 标记为已解决
              </a-menu-item>
              <a-menu-item key="CLOSED">
                <CloseCircleOutlined /> 关闭
              </a-menu-item>
              <a-menu-item key="REOPENED">
                <IssuesCloseOutlined /> 重新打开
              </a-menu-item>
            </a-menu>
          </template>
          <a-button>
            更改状态 <DownOutlined />
          </a-button>
        </a-dropdown>
        <a-button @click="handleEdit">
          <template #icon><EditOutlined /></template>
          编辑
        </a-button>
        <a-popconfirm
          title="确定删除这个缺陷吗？"
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
import type { Bug, BugStatus, BugSeverity, TestCasePriority } from '@/types/test'
import {
  PaperClipOutlined,
  EditOutlined,
  DeleteOutlined,
  DownOutlined,
  SyncOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  IssuesCloseOutlined
} from '@ant-design/icons-vue'
import * as testApi from '@/api/modules/test'
import { message } from 'ant-design-vue'

interface Props {
  visible: boolean
  bugId: number | null
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:visible': [value: boolean]
  'edit': [bug: Bug]
}>()

const testStore = useTestStore()
const loading = ref(false)
const bug = ref<Bug | null>(null)
const newComment = ref('')
const addingComment = ref(false)

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

const formatFileSize = (bytes: number) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

const loadData = async () => {
  if (!props.bugId) return

  try {
    loading.value = true
    const bugData = await testStore.fetchBugDetail(props.bugId)
    bug.value = bugData
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
  if (bug.value) {
    emit('edit', bug.value)
  }
}

const handleStatusMenuClick = async ({ key }: { key: string }) => {
  if (bug.value) {
    try {
      await testStore.updateBugStatus(bug.value.id, key)
      await loadData()
    } catch (error) {
      console.error('Update bug status failed:', error)
    }
  }
}

const handleDelete = async () => {
  if (bug.value) {
    try {
      await testStore.deleteBug(bug.value.id)
      emit('update:visible', false)
    } catch (error) {
      console.error('Delete bug failed:', error)
    }
  }
}

const handleAddComment = async () => {
  if (!bug.value || !newComment.value.trim()) return

  try {
    addingComment.value = true
    await testApi.addBugComment(bug.value.id, newComment.value)
    message.success('评论添加成功')
    newComment.value = ''
    await loadData()
  } catch (error) {
    console.error('Add comment failed:', error)
    message.error('评论添加失败')
  } finally {
    addingComment.value = false
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
.bug-detail {
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
  }

  .reproduce-steps {
    padding: 12px;
    background: #fafafa;
    border-radius: 4px;
    margin-bottom: 16px;

    ol {
      margin: 0;
      padding-left: 24px;

      li {
        margin-bottom: 8px;
      }
    }
  }

  .attachments {
    margin-bottom: 16px;
  }

  .linked-test-cases {
    margin-bottom: 16px;
  }

  .status-history {
    margin-bottom: 16px;
  }

  .comments-section {
    margin-bottom: 16px;
  }

  .tags {
    margin-bottom: 16px;
  }
}
</style>
