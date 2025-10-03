<template>
  <a-modal
    v-model:open="visible"
    title="创建自定义报告"
    :width="640"
    :confirm-loading="loading"
    @ok="handleOk"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 18 }"
    >
      <a-form-item label="报告名称" name="name">
        <a-input
          v-model:value="formData.name"
          placeholder="请输入报告名称"
          :maxlength="50"
        />
      </a-form-item>

      <a-form-item label="报告描述" name="description">
        <a-textarea
          v-model:value="formData.description"
          placeholder="请输入报告描述（可选）"
          :rows="3"
          :maxlength="200"
        />
      </a-form-item>

      <a-form-item label="报告类型" name="type">
        <a-select
          v-model:value="formData.type"
          placeholder="请选择报告类型"
        >
          <a-select-option value="project_overview">项目概览</a-select-option>
          <a-select-option value="task_statistics">任务统计</a-select-option>
          <a-select-option value="quality_report">质量报告</a-select-option>
          <a-select-option value="team_analysis">团队分析</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="选择项目" name="projectId">
        <a-select
          v-model:value="formData.projectId"
          placeholder="请选择项目（可选）"
          allow-clear
          show-search
        >
          <a-select-option value="">全部项目</a-select-option>
          <a-select-option
            v-for="project in projects"
            :key="project.id"
            :value="project.id"
          >
            {{ project.name }}
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="时间范围" name="dateRange">
        <a-range-picker
          v-model:value="formData.dateRange"
          :presets="datePresets"
          style="width: 100%"
        />
      </a-form-item>

      <a-form-item label="包含内容" name="includeContent">
        <a-checkbox-group v-model:value="formData.includeContent">
          <a-checkbox value="charts">图表</a-checkbox>
          <a-checkbox value="tables">表格</a-checkbox>
          <a-checkbox value="stats">统计数据</a-checkbox>
        </a-checkbox-group>
      </a-form-item>

      <a-form-item label="保存为模板" name="saveAsTemplate">
        <a-switch v-model:checked="formData.saveAsTemplate" />
        <span style="margin-left: 8px; color: #8c8c8c; font-size: 12px">
          保存后可重复使用
        </span>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useAnalyticsStore } from '@/stores/modules/analytics'
import type { FormInstance } from 'ant-design-vue'
import type { ReportType, ReportConfig } from '@/types/analytics'
import { message } from 'ant-design-vue'
import dayjs, { type Dayjs } from 'dayjs'

interface Props {
  open: boolean
  projects?: Array<{ id: string; name: string }>
}

const props = withDefaults(defineProps<Props>(), {
  projects: () => []
})

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'success'): void
}>()

const analyticsStore = useAnalyticsStore()

const visible = computed({
  get: () => props.open,
  set: (value) => emit('update:open', value)
})

const loading = computed(() => analyticsStore.reportLoading)

const formRef = ref<FormInstance>()

const formData = reactive({
  name: '',
  description: '',
  type: '' as ReportType,
  projectId: undefined as string | undefined,
  dateRange: [dayjs().subtract(30, 'day'), dayjs()] as [Dayjs, Dayjs] | null,
  includeContent: ['charts', 'tables', 'stats'] as string[],
  saveAsTemplate: false
})

const rules = {
  name: [
    { required: true, message: '请输入报告名称', trigger: 'blur' },
    { min: 2, max: 50, message: '报告名称长度在2-50个字符之间', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择报告类型', trigger: 'change' }
  ],
  includeContent: [
    {
      required: true,
      type: 'array',
      min: 1,
      message: '至少选择一项内容',
      trigger: 'change'
    }
  ]
}

const datePresets = [
  { label: '最近7天', value: [dayjs().subtract(7, 'day'), dayjs()] as [Dayjs, Dayjs] },
  { label: '最近14天', value: [dayjs().subtract(14, 'day'), dayjs()] as [Dayjs, Dayjs] },
  { label: '最近30天', value: [dayjs().subtract(30, 'day'), dayjs()] as [Dayjs, Dayjs] },
  { label: '最近90天', value: [dayjs().subtract(90, 'day'), dayjs()] as [Dayjs, Dayjs] },
  { label: '本月', value: [dayjs().startOf('month'), dayjs()] as [Dayjs, Dayjs] },
  { label: '上月', value: [dayjs().subtract(1, 'month').startOf('month'), dayjs().subtract(1, 'month').endOf('month')] as [Dayjs, Dayjs] }
]

const handleOk = async () => {
  try {
    await formRef.value?.validate()

    const config: ReportConfig = {
      name: formData.name,
      description: formData.description || undefined,
      type: formData.type,
      projectId: formData.projectId,
      dateRange: formData.dateRange
        ? {
            start: formData.dateRange[0].format('YYYY-MM-DD'),
            end: formData.dateRange[1].format('YYYY-MM-DD')
          }
        : undefined,
      includeCharts: formData.includeContent.includes('charts'),
      includeTables: formData.includeContent.includes('tables'),
      includeStats: formData.includeContent.includes('stats'),
      saveAsTemplate: formData.saveAsTemplate
    }

    await analyticsStore.createCustomReport(config)
    message.success('报告创建成功')
    emit('success')
    handleCancel()
  } catch (error) {
    const errorMessage = error instanceof Error ? error.message : '创建报告失败，请重试'
    message.error(errorMessage)
    
    // 记录详细错误信息用于调试
    console.error('Create report failed:', {
      error,
      formData: { ...formData },
      timestamp: new Date().toISOString()
    })
    
    // 如果是网络错误，提供重试选项
    if (error instanceof Error && error.message.includes('network')) {
      message.warning('网络连接异常，请检查网络后重试')
    }
  }
}

const handleCancel = () => {
  formRef.value?.resetFields()
  emit('update:open', false)
}
</script>

<style scoped lang="scss">
// Modal styles are handled by Ant Design Vue
</style>
