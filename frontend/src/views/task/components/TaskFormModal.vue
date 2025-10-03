<template>
  <a-modal
    v-model:open="visible"
    :title="isEdit ? '编辑任务' : '创建任务'"
    :width="720"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      :label-col="{ span: 4 }"
      :wrapper-col="{ span: 20 }"
    >
      <a-form-item label="任务标题" name="title">
        <a-input
          v-model:value="formData.title"
          placeholder="请输入任务标题"
          :maxlength="100"
          show-count
        />
      </a-form-item>

      <a-form-item label="任务描述" name="description">
        <a-textarea
          v-model:value="formData.description"
          placeholder="请输入任务描述"
          :rows="4"
          :maxlength="1000"
          show-count
        />
      </a-form-item>

      <a-form-item label="状态" name="status">
        <a-select v-model:value="formData.status" placeholder="选择状态">
          <a-select-option value="todo">待处理</a-select-option>
          <a-select-option value="in_progress">进行中</a-select-option>
          <a-select-option value="testing">测试中</a-select-option>
          <a-select-option value="done">已完成</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="优先级" name="priority">
        <a-radio-group v-model:value="formData.priority" button-style="solid">
          <a-radio-button value="urgent">
            <a-tag color="red" style="border: none; margin: 0">紧急</a-tag>
          </a-radio-button>
          <a-radio-button value="high">
            <a-tag color="orange" style="border: none; margin: 0">高</a-tag>
          </a-radio-button>
          <a-radio-button value="medium">
            <a-tag color="blue" style="border: none; margin: 0">中</a-tag>
          </a-radio-button>
          <a-radio-button value="low">
            <a-tag color="default" style="border: none; margin: 0">低</a-tag>
          </a-radio-button>
        </a-radio-group>
      </a-form-item>

      <a-form-item label="负责人" name="assigneeId">
        <a-select
          v-model:value="formData.assigneeId"
          placeholder="选择负责人"
          show-search
          allow-clear
          :filter-option="filterOption"
        >
          <a-select-option
            v-for="user in teamMembers"
            :key="user.id"
            :value="user.id"
            :label="user.name"
          >
            <a-space>
              <a-avatar :src="user.avatar" :size="24">
                {{ user.name.charAt(0) }}
              </a-avatar>
              {{ user.name }}
            </a-space>
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="所属项目" name="projectId">
        <a-select
          v-model:value="formData.projectId"
          placeholder="选择项目"
          show-search
          :filter-option="filterOption"
        >
          <a-select-option
            v-for="project in projects"
            :key="project.id"
            :value="project.id"
            :label="project.name"
          >
            {{ project.name }}
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="所属迭代" name="sprintId">
        <a-select
          v-model:value="formData.sprintId"
          placeholder="选择迭代"
          show-search
          allow-clear
          :filter-option="filterOption"
        >
          <a-select-option
            v-for="sprint in sprints"
            :key="sprint.id"
            :value="sprint.id"
            :label="sprint.name"
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
      </a-form-item>

      <a-form-item label="父任务" name="parentId">
        <a-select
          v-model:value="formData.parentId"
          placeholder="选择父任务（可选）"
          show-search
          allow-clear
          :filter-option="filterOption"
        >
          <a-select-option
            v-for="task in availableTasks"
            :key="task.id"
            :value="task.id"
            :label="task.title"
          >
            {{ task.title }}
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="开始日期" name="startDate">
        <a-date-picker
          v-model:value="formData.startDate"
          style="width: 100%"
          placeholder="选择开始日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
        />
      </a-form-item>

      <a-form-item label="截止日期" name="dueDate">
        <a-date-picker
          v-model:value="formData.dueDate"
          style="width: 100%"
          placeholder="选择截止日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          :disabled-date="disabledDate"
        />
      </a-form-item>

      <a-form-item label="预估工时" name="estimatedHours">
        <a-input-number
          v-model:value="formData.estimatedHours"
          :min="0"
          :max="1000"
          :step="0.5"
          style="width: 100%"
          placeholder="预估工时（小时）"
        />
      </a-form-item>

      <a-form-item label="标签" name="tags">
        <a-select
          v-model:value="formData.tags"
          mode="tags"
          placeholder="添加标签"
          style="width: 100%"
          :options="tagOptions"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, computed, watch, reactive } from 'vue'
import { useTaskStore } from '@/stores/modules/task'
import type { Task, TaskFormData, TaskStatus } from '@/types/task'
import type { FormInstance, Rule } from 'ant-design-vue/es/form'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'

interface Props {
  visible?: boolean
  task?: Task | null
  defaultStatus?: TaskStatus
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  success: []
  cancel: []
}>()

const taskStore = useTaskStore()
const formRef = ref<FormInstance>()

const visible = computed({
  get: () => props.visible || false,
  set: (value) => emit('update:visible', value)
})

const isEdit = computed(() => !!props.task)

const formData = reactive<TaskFormData>({
  title: '',
  description: '',
  status: props.defaultStatus || 'todo',
  priority: 'medium',
  assigneeId: undefined,
  projectId: 0,
  sprintId: undefined,
  parentId: undefined,
  tags: [],
  dueDate: undefined,
  startDate: undefined,
  estimatedHours: undefined
})

const rules: Record<string, Rule[]> = {
  title: [
    { required: true, message: '请输入任务标题', trigger: 'blur' },
    { min: 2, max: 100, message: '任务标题长度为2-100个字符', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择任务状态', trigger: 'change' }
  ],
  priority: [
    { required: true, message: '请选择优先级', trigger: 'change' }
  ],
  projectId: [
    { required: true, message: '请选择所属项目', trigger: 'change' }
  ]
}

// Mock data - 在实际应用中应该从 store 或 API 获取
const teamMembers = ref([
  { id: 1, name: '张三', avatar: '' },
  { id: 2, name: '李四', avatar: '' },
  { id: 3, name: '王五', avatar: '' }
])

const projects = ref([
  { id: 1, name: '项目A' },
  { id: 2, name: '项目B' },
  { id: 3, name: '项目C' }
])

const sprints = computed(() => taskStore.sprints)
const availableTasks = computed(() => {
  // 排除当前任务和已是子任务的任务
  return taskStore.taskList.filter(t => {
    if (props.task && t.id === props.task.id) return false
    if (t.parentId) return false
    return true
  })
})

const tagOptions = computed(() => {
  return taskStore.tags.map(tag => ({ value: tag, label: tag }))
})

watch(() => props.visible, (newVal) => {
  if (newVal) {
    if (props.task) {
      // 编辑模式：填充表单数据
      Object.assign(formData, {
        title: props.task.title,
        description: props.task.description,
        status: props.task.status,
        priority: props.task.priority,
        assigneeId: props.task.assigneeId,
        projectId: props.task.projectId,
        sprintId: props.task.sprintId,
        parentId: props.task.parentId,
        tags: props.task.tags || [],
        dueDate: props.task.dueDate,
        startDate: props.task.startDate,
        estimatedHours: props.task.estimatedHours
      })
    } else {
      // 创建模式：重置表单
      formRef.value?.resetFields()
      Object.assign(formData, {
        title: '',
        description: '',
        status: props.defaultStatus || 'todo',
        priority: 'medium',
        assigneeId: undefined,
        projectId: projects.value[0]?.id || 0,
        sprintId: undefined,
        parentId: undefined,
        tags: [],
        dueDate: undefined,
        startDate: undefined,
        estimatedHours: undefined
      })
    }
  }
})

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()

    if (isEdit.value && props.task) {
      await taskStore.updateTask(props.task.id, formData)
      message.success('任务更新成功')
    } else {
      await taskStore.createTask(formData)
      message.success('任务创建成功')
    }

    emit('success')
    visible.value = false
  } catch (error) {
    console.error('Form validation failed:', error)
  }
}

const handleCancel = () => {
  emit('cancel')
  visible.value = false
}

const filterOption = (input: string, option: any) => {
  const label = option.label || option.children?.[0]?.children || ''
  return label.toLowerCase().includes(input.toLowerCase())
}

const disabledDate = (current: dayjs.Dayjs) => {
  if (!formData.startDate) return false
  return current && current.isBefore(dayjs(formData.startDate), 'day')
}
</script>

<style scoped lang="scss">
// 样式已通过 Ant Design 组件自动处理
</style>