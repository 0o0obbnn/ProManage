<template>
  <a-drawer
    v-model:open="visible"
    title="任务详情"
    :width="800"
    :body-style="{ paddingBottom: '80px' }"
    @close="handleClose"
  >
    <template v-if="task">
      <div class="task-detail-content">
        <div class="task-header-section">
          <div class="task-title-row">
            <h2>{{ task.title }}</h2>
            <a-dropdown>
              <a-button type="text">
                <MoreOutlined />
              </a-button>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="handleEdit">
                    <EditOutlined /> 编辑
                  </a-menu-item>
                  <a-menu-item @click="handleCopy">
                    <CopyOutlined /> 复制
                  </a-menu-item>
                  <a-menu-divider />
                  <a-menu-item danger @click="handleDelete">
                    <DeleteOutlined /> 删除
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>

          <a-space size="middle" wrap>
            <a-tag :color="getStatusColor(task.status)">
              {{ getStatusText(task.status) }}
            </a-tag>
            <a-tag :color="getPriorityColor(task.priority)">
              {{ getPriorityText(task.priority) }}
            </a-tag>
            <span v-if="task.projectName" class="meta-item">
              <ProjectOutlined /> {{ task.projectName }}
            </span>
            <span v-if="task.sprintName" class="meta-item">
              <ThunderboltOutlined /> {{ task.sprintName }}
            </span>
          </a-space>
        </div>

        <a-descriptions :column="2" bordered style="margin-top: 24px">
          <a-descriptions-item label="负责人">
            <a-space v-if="task.assignee">
              <a-avatar :src="task.assignee.avatar" :size="24">
                {{ task.assignee.name.charAt(0) }}
              </a-avatar>
              {{ task.assignee.name }}
            </a-space>
            <span v-else style="color: #bfbfbf">未分配</span>
          </a-descriptions-item>

          <a-descriptions-item label="报告人">
            <a-space v-if="task.reporter">
              <a-avatar :src="task.reporter.avatar" :size="24">
                {{ task.reporter.name.charAt(0) }}
              </a-avatar>
              {{ task.reporter.name }}
            </a-space>
          </a-descriptions-item>

          <a-descriptions-item label="开始日期">
            {{ task.startDate ? formatDate(task.startDate) : '-' }}
          </a-descriptions-item>

          <a-descriptions-item label="截止日期">
            <span :class="{ 'overdue': task.dueDate && isOverdue(task.dueDate) }">
              {{ task.dueDate ? formatDate(task.dueDate) : '-' }}
            </span>
          </a-descriptions-item>

          <a-descriptions-item label="预估工时">
            {{ task.estimatedHours ? task.estimatedHours + 'h' : '-' }}
          </a-descriptions-item>

          <a-descriptions-item label="实际工时">
            {{ task.actualHours ? task.actualHours + 'h' : '-' }}
          </a-descriptions-item>

          <a-descriptions-item label="进度" :span="2">
            <a-progress :percent="task.progress" :stroke-color="getProgressColor(task.progress)" />
          </a-descriptions-item>
        </a-descriptions>

        <a-divider orientation="left">任务描述</a-divider>
        <div class="task-description">
          <div v-if="task.description" v-html="task.description"></div>
          <div v-else style="color: #bfbfbf">暂无描述</div>
        </div>

        <a-divider orientation="left">标签</a-divider>
        <div class="task-tags">
          <a-space wrap>
            <a-tag v-for="tag in task.tags" :key="tag" color="blue">
              {{ tag }}
            </a-tag>
            <a-button size="small" type="dashed">
              <PlusOutlined /> 添加标签
            </a-button>
          </a-space>
        </div>

        <a-divider orientation="left">
          子任务
          <a-tag style="margin-left: 8px">
            {{ task.completedSubtasks }}/{{ task.subtaskCount }}
          </a-tag>
        </a-divider>
        <div class="subtasks-section">
          <a-list
            v-if="subtasks.length > 0"
            :data-source="subtasks"
            size="small"
          >
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta>
                  <template #avatar>
                    <a-checkbox
                      :checked="item.completed"
                      @change="handleSubTaskToggle(item)"
                    />
                  </template>
                  <template #title>
                    <span :class="{ 'completed-subtask': item.completed }">
                      {{ item.title }}
                    </span>
                  </template>
                  <template #description>
                    <a-space v-if="item.assignee">
                      <a-avatar :src="item.assignee.avatar" :size="20">
                        {{ item.assignee.name.charAt(0) }}
                      </a-avatar>
                      {{ item.assignee.name }}
                    </a-space>
                  </template>
                </a-list-item-meta>
                <template #actions>
                  <a @click="handleEditSubTask(item)">编辑</a>
                  <a @click="handleDeleteSubTask(item)">删除</a>
                </template>
              </a-list-item>
            </template>
          </a-list>

          <div class="add-subtask">
            <a-input
              v-model:value="newSubTaskTitle"
              placeholder="添加子任务..."
              @press-enter="handleAddSubTask"
            >
              <template #suffix>
                <a-button
                  type="link"
                  size="small"
                  :disabled="!newSubTaskTitle.trim()"
                  @click="handleAddSubTask"
                >
                  添加
                </a-button>
              </template>
            </a-input>
          </div>
        </div>

        <a-divider orientation="left">
          附件
          <a-tag style="margin-left: 8px">{{ task.attachmentCount }}</a-tag>
        </a-divider>
        <div class="attachments-section">
          <a-upload
            v-model:file-list="fileList"
            :before-upload="handleBeforeUpload"
            :custom-request="handleUploadAttachment"
            multiple
          >
            <a-button>
              <UploadOutlined /> 上传附件
            </a-button>
          </a-upload>

          <a-list
            v-if="task.attachments && task.attachments.length > 0"
            :data-source="task.attachments"
            size="small"
            style="margin-top: 16px"
          >
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta>
                  <template #avatar>
                    <FileOutlined style="font-size: 24px; color: #1890ff" />
                  </template>
                  <template #title>
                    <a :href="item.url" target="_blank">{{ item.name }}</a>
                  </template>
                  <template #description>
                    {{ formatFileSize(item.size) }} - {{ item.uploadedBy.name }} 上传于 {{ formatDate(item.createdAt) }}
                  </template>
                </a-list-item-meta>
                <template #actions>
                  <a :href="item.url" download>下载</a>
                  <a @click="handleDeleteAttachment(item)">删除</a>
                </template>
              </a-list-item>
            </template>
          </a-list>
        </div>

        <a-divider orientation="left">
          评论
          <a-tag style="margin-left: 8px">{{ comments.length }}</a-tag>
        </a-divider>
        <div class="comments-section">
          <div class="comment-editor">
            <a-textarea
              v-model:value="newComment"
              placeholder="添加评论..."
              :rows="3"
            />
            <div class="comment-actions">
              <a-button
                type="primary"
                :disabled="!newComment.trim()"
                @click="handleAddComment"
              >
                发表评论
              </a-button>
            </div>
          </div>

          <a-list
            v-if="comments.length > 0"
            :data-source="comments"
            item-layout="horizontal"
          >
            <template #renderItem="{ item }">
              <a-list-item>
                <a-comment>
                  <template #avatar>
                    <a-avatar :src="item.user.avatar">
                      {{ item.user.name.charAt(0) }}
                    </a-avatar>
                  </template>
                  <template #author>
                    <span>{{ item.user.name }}</span>
                  </template>
                  <template #content>
                    <p>{{ item.content }}</p>
                  </template>
                  <template #datetime>
                    <span>{{ formatDateTime(item.createdAt) }}</span>
                  </template>
                  <template #actions>
                    <a @click="handleDeleteComment(item)">删除</a>
                  </template>
                </a-comment>
              </a-list-item>
            </template>
          </a-list>
        </div>

        <a-divider orientation="left">活动记录</a-divider>
        <div class="activities-section">
          <a-timeline>
            <a-timeline-item
              v-for="activity in task.activities"
              :key="activity.id"
              :color="getActivityColor(activity.action)"
            >
              <template #dot>
                <a-avatar :src="activity.user.avatar" :size="24">
                  {{ activity.user.name.charAt(0) }}
                </a-avatar>
              </template>
              <p>
                <strong>{{ activity.user.name }}</strong>
                {{ activity.description }}
              </p>
              <p style="color: #8c8c8c; font-size: 12px">
                {{ formatDateTime(activity.createdAt) }}
              </p>
            </a-timeline-item>
          </a-timeline>
        </div>
      </div>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useTaskStore } from '@/stores/modules/task'
import type { Task, SubTask, TaskComment, TaskAttachment } from '@/types/task'
import type { UploadProps } from 'ant-design-vue'
import {
  MoreOutlined,
  EditOutlined,
  CopyOutlined,
  DeleteOutlined,
  ProjectOutlined,
  ThunderboltOutlined,
  PlusOutlined,
  UploadOutlined,
  FileOutlined
} from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import dayjs from 'dayjs'

interface Props {
  taskId?: number
  visible?: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  edit: [task: Task]
  delete: [task: Task]
  close: []
}>()

const taskStore = useTaskStore()

const visible = computed({
  get: () => props.visible || false,
  set: (value) => emit('update:visible', value)
})

const task = computed(() => taskStore.currentTask)
const subtasks = computed(() => taskStore.subtasks)
const comments = computed(() => taskStore.comments)

const newSubTaskTitle = ref('')
const newComment = ref('')
const fileList = ref<any[]>([])

watch(() => props.taskId, async (taskId) => {
  if (taskId) {
    await taskStore.fetchTaskDetail(taskId)
    await Promise.all([
      taskStore.fetchSubTasks(taskId),
      taskStore.fetchComments(taskId)
    ])
  }
}, { immediate: true })

const handleClose = () => {
  emit('close')
  emit('update:visible', false)
}

const handleEdit = () => {
  if (task.value) {
    emit('edit', task.value)
  }
}

const handleCopy = async () => {
  if (task.value) {
    await taskStore.createTask({
      title: task.value.title + ' (副本)',
      description: task.value.description,
      status: task.value.status,
      priority: task.value.priority,
      projectId: task.value.projectId,
      sprintId: task.value.sprintId,
      tags: task.value.tags
    })
    message.success('任务复制成功')
  }
}

const handleDelete = () => {
  if (task.value) {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除这个任务吗？',
      onOk: async () => {
        await taskStore.deleteTask(task.value!.id)
        emit('delete', task.value!)
        handleClose()
      }
    })
  }
}

const handleSubTaskToggle = async (subtask: SubTask) => {
  await taskStore.toggleSubTaskComplete(subtask.id, !subtask.completed)
  if (props.taskId) {
    await taskStore.fetchTaskDetail(props.taskId)
  }
}

const handleAddSubTask = async () => {
  if (!newSubTaskTitle.value.trim() || !props.taskId) return

  await taskStore.createSubTask(props.taskId, {
    title: newSubTaskTitle.value.trim()
  })

  newSubTaskTitle.value = ''
  await taskStore.fetchTaskDetail(props.taskId)
}

const handleEditSubTask = (subtask: SubTask) => {
  // TODO: Implement edit subtask
  message.info('编辑子任务功能开发中')
}

const handleDeleteSubTask = async (subtask: SubTask) => {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这个子任务吗？',
    onOk: async () => {
      await taskStore.deleteSubTask(subtask.id)
      if (props.taskId) {
        await taskStore.fetchTaskDetail(props.taskId)
      }
    }
  })
}

const handleBeforeUpload: UploadProps['beforeUpload'] = () => {
  return false
}

const handleUploadAttachment = async ({ file }: any) => {
  // TODO: Implement upload attachment
  message.info('上传附件功能开发中')
}

const handleDeleteAttachment = (attachment: TaskAttachment) => {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这个附件吗？',
    onOk: async () => {
      message.info('删除附件功能开发中')
    }
  })
}

const handleAddComment = async () => {
  if (!newComment.value.trim() || !props.taskId) return

  await taskStore.addComment(props.taskId, newComment.value.trim())
  newComment.value = ''
}

const handleDeleteComment = (comment: TaskComment) => {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这条评论吗？',
    onOk: async () => {
      if (props.taskId) {
        await taskStore.deleteComment(props.taskId, comment.id)
      }
    }
  })
}

const getStatusColor = (status: string) => {
  const colors: Record<string, string> = {
    todo: 'default',
    in_progress: 'processing',
    testing: 'warning',
    done: 'success'
  }
  return colors[status] || 'default'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    todo: '待处理',
    in_progress: '进行中',
    testing: '测试中',
    done: '已完成'
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

const getProgressColor = (progress: number) => {
  if (progress === 100) return '#52c41a'
  if (progress >= 60) return '#1890ff'
  if (progress >= 30) return '#faad14'
  return '#ff4d4f'
}

const getActivityColor = (action: string) => {
  const colors: Record<string, string> = {
    create: 'green',
    update: 'blue',
    delete: 'red',
    comment: 'purple'
  }
  return colors[action] || 'gray'
}

const isOverdue = (dueDate: string) => {
  return dayjs(dueDate).isBefore(dayjs())
}

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD')
}

const formatDateTime = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const formatFileSize = (size: number) => {
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB'
  return (size / (1024 * 1024)).toFixed(2) + ' MB'
}
</script>

<style scoped lang="scss">
.task-detail-content {
  .task-header-section {
    .task-title-row {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 16px;

      h2 {
        margin: 0;
        flex: 1;
      }
    }

    .meta-item {
      color: #8c8c8c;
      font-size: 14px;
    }
  }

  .task-description {
    padding: 16px;
    background: #fafafa;
    border-radius: 4px;
    line-height: 1.8;
    min-height: 100px;
  }

  .task-tags {
    margin-bottom: 24px;
  }

  .subtasks-section {
    .completed-subtask {
      text-decoration: line-through;
      color: #bfbfbf;
    }

    .add-subtask {
      margin-top: 16px;
    }
  }

  .attachments-section {
    margin-bottom: 24px;
  }

  .comments-section {
    .comment-editor {
      margin-bottom: 24px;

      .comment-actions {
        margin-top: 8px;
        text-align: right;
      }
    }
  }

  .activities-section {
    margin-top: 16px;
  }

  .overdue {
    color: #ff4d4f;
  }
}
</style>