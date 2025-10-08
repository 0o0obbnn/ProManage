<template>
  <a-modal
    :visible="visible"
    :title="isEdit ? '编辑项目' : '创建项目'"
    :confirm-loading="loading"
    :width="720"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 18 }"
    >
      <a-form-item label="项目名称" name="name">
        <a-input
          v-model:value="formData.name"
          placeholder="请输入项目名称"
          :maxlength="100"
          show-count
        />
      </a-form-item>

      <a-form-item label="项目编码" name="code">
        <a-input
          v-model:value="formData.code"
          placeholder="请输入项目编码,如: PROJ-001"
          :maxlength="50"
          :disabled="isEdit"
        />
        <template #extra>
          项目编码创建后不可修改
        </template>
      </a-form-item>

      <a-form-item label="项目描述" name="description">
        <a-textarea
          v-model:value="formData.description"
          placeholder="请输入项目描述"
          :rows="4"
          :maxlength="500"
          show-count
        />
      </a-form-item>

      <a-form-item label="项目类型" name="type">
        <a-select v-model:value="formData.type" placeholder="请选择项目类型">
          <a-select-option value="WEB">Web项目</a-select-option>
          <a-select-option value="APP">移动应用</a-select-option>
          <a-select-option value="SYSTEM">系统软件</a-select-option>
          <a-select-option value="OTHER">其他</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="优先级" name="priority">
        <a-select v-model:value="formData.priority" placeholder="请选择优先级">
          <a-select-option :value="0">低优先级</a-select-option>
          <a-select-option :value="1">中优先级</a-select-option>
          <a-select-option :value="2">高优先级</a-select-option>
          <a-select-option :value="3">紧急</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="项目颜色" name="color">
        <a-input
          v-model:value="formData.color"
          type="color"
          style="width: 100px"
        />
      </a-form-item>

      <a-form-item label="计划时间" name="dateRange">
        <a-range-picker
          v-model:value="dateRange"
          style="width: 100%"
          :placeholder="['开始日期', '结束日期']"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import type { Dayjs } from 'dayjs'
import dayjs from 'dayjs'
import { useProjectStore } from '@/stores/modules/project'
import type { Project, CreateProjectRequest, UpdateProjectRequest } from '@/types/project'

/**
 * 组件属性
 */
interface Props {
  visible: boolean
  project?: Project | null
}

const props = withDefaults(defineProps<Props>(), {
  project: null
})

/**
 * 组件事件
 */
interface Emits {
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}

const emit = defineEmits<Emits>()

const projectStore = useProjectStore()

// 响应式数据
const formRef = ref<FormInstance>()
const loading = ref(false)
const dateRange = ref<[Dayjs, Dayjs] | null>(null)

const formData = ref<CreateProjectRequest>({
  name: '',
  code: '',
  description: '',
  type: undefined,
  priority: 1,
  color: '#1890ff',
  startDate: undefined,
  endDate: undefined
})

// 计算属性
const isEdit = computed(() => !!props.project)

// 方法定义(需要在watch之前定义)
const resetForm = () => {
  formData.value = {
    name: '',
    code: '',
    description: '',
    type: undefined,
    priority: 1,
    color: '#1890ff',
    startDate: undefined,
    endDate: undefined
  }
  dateRange.value = null
  formRef.value?.resetFields()
}

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { min: 2, max: 100, message: '项目名称长度在2-100个字符之间', trigger: 'blur' },
    {
      validator: (_rule: any, value: string) => {
        if (!value) return Promise.resolve()
        // 检查是否包含特殊字符
        if (/[<>'"&]/.test(value)) {
          return Promise.reject('项目名称不能包含特殊字符 < > \' " &')
        }
        return Promise.resolve()
      },
      trigger: 'blur'
    }
  ],
  code: [
    { required: true, message: '请输入项目编码', trigger: 'blur' },
    {
      pattern: /^[A-Z0-9_-]+$/,
      message: '项目编码只能包含大写字母、数字、下划线和连字符',
      trigger: 'blur'
    },
    { min: 2, max: 50, message: '项目编码长度在2-50个字符之间', trigger: 'blur' },
    {
      validator: async (_rule: any, value: string) => {
        if (!value || isEdit.value) return Promise.resolve()
        // 检查项目编码是否已存在
        // TODO: 调用API检查编码唯一性
        // const exists = await projectStore.checkCodeExists(value)
        // if (exists) {
        //   return Promise.reject('项目编码已存在')
        // }
        return Promise.resolve()
      },
      trigger: 'blur'
    }
  ],
  description: [
    { max: 500, message: '项目描述不能超过500个字符', trigger: 'blur' }
  ]
}

// 监听项目变化
watch(
  () => props.project,
  (project) => {
    if (project) {
      formData.value = {
        name: project.name,
        code: project.code,
        description: project.description,
        type: project.type,
        priority: project.priority,
        color: project.color || '#1890ff',
        startDate: project.startDate,
        endDate: project.endDate
      }

      if (project.startDate && project.endDate) {
        dateRange.value = [
          dayjs(project.startDate),
          dayjs(project.endDate)
        ]
      }
    } else {
      resetForm()
    }
  },
  { immediate: true }
)

// 监听日期范围变化
watch(dateRange, (value) => {
  if (value) {
    formData.value.startDate = value[0].format('YYYY-MM-DD')
    formData.value.endDate = value[1].format('YYYY-MM-DD')
  } else {
    formData.value.startDate = undefined
    formData.value.endDate = undefined
  }
})

// 方法
const handleSubmit = async () => {
  try {
    // 验证表单
    await formRef.value?.validate()
    loading.value = true

    if (isEdit.value && props.project) {
      // 更新项目
      const updateData: UpdateProjectRequest = {
        name: formData.value.name,
        description: formData.value.description,
        type: formData.value.type,
        priority: formData.value.priority,
        color: formData.value.color,
        startDate: formData.value.startDate,
        endDate: formData.value.endDate
      }
      await projectStore.updateProjectInfo(props.project.id, updateData)
      // Store中已经有成功提示
    } else {
      // 创建项目
      await projectStore.createNewProject(formData.value)
      // Store中已经有成功提示
    }

    // 触发成功事件
    emit('success')
    emit('update:visible', false)

    // 重置表单
    resetForm()
  } catch (error: any) {
    console.error('Submit form failed:', error)

    // 如果是验证错误,不显示消息(表单会自动显示)
    if (error?.errorFields) {
      return
    }

    // 显示错误消息
    const errorMessage = error?.message || error?.response?.data?.message || '操作失败,请重试'
    message.error(errorMessage)
  } finally {
    loading.value = false
  }
}

const handleCancel = () => {
  emit('update:visible', false)
  resetForm()
}
</script>

<style lang="scss" scoped>
// 样式可以根据需要添加
</style>

