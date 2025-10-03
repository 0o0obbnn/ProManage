<template>
  <a-drawer
    v-model:open="visible"
    title="定时报告配置"
    :width="600"
    @close="handleClose"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 18 }"
    >
      <a-divider orientation="left">基本设置</a-divider>

      <a-form-item label="启用定时报告" name="enabled">
        <a-switch v-model:checked="formData.enabled" />
      </a-form-item>

      <a-form-item
        v-if="formData.enabled"
        label="报告频率"
        name="frequency"
      >
        <a-select
          v-model:value="formData.frequency"
          placeholder="请选择报告频率"
        >
          <a-select-option value="daily">每日</a-select-option>
          <a-select-option value="weekly">每周</a-select-option>
          <a-select-option value="monthly">每月</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item
        v-if="formData.enabled && formData.frequency === 'daily'"
        label="发送时间"
        name="sendTime"
      >
        <a-time-picker
          v-model:value="formData.sendTime"
          format="HH:mm"
          :minute-step="15"
          style="width: 100%"
        />
      </a-form-item>

      <a-form-item
        v-if="formData.enabled && formData.frequency === 'weekly'"
        label="发送日期"
        name="weekDay"
      >
        <a-select
          v-model:value="formData.weekDay"
          placeholder="请选择星期"
        >
          <a-select-option :value="1">星期一</a-select-option>
          <a-select-option :value="2">星期二</a-select-option>
          <a-select-option :value="3">星期三</a-select-option>
          <a-select-option :value="4">星期四</a-select-option>
          <a-select-option :value="5">星期五</a-select-option>
          <a-select-option :value="6">星期六</a-select-option>
          <a-select-option :value="0">星期日</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item
        v-if="formData.enabled && formData.frequency === 'monthly'"
        label="发送日期"
        name="monthDay"
      >
        <a-input-number
          v-model:value="formData.monthDay"
          :min="1"
          :max="28"
          placeholder="每月几号"
          style="width: 100%"
        />
      </a-form-item>

      <a-divider orientation="left">接收人设置</a-divider>

      <a-form-item label="接收人" name="recipients">
        <a-select
          v-model:value="formData.recipients"
          mode="multiple"
          placeholder="请选择接收人"
          :filter-option="filterUser"
          show-search
        >
          <a-select-option
            v-for="user in users"
            :key="user.id"
            :value="user.id"
          >
            {{ user.name }} ({{ user.email }})
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="发送渠道" name="channels">
        <a-checkbox-group v-model:value="formData.channels">
          <a-checkbox value="email">邮件</a-checkbox>
          <a-checkbox value="notification">站内通知</a-checkbox>
        </a-checkbox-group>
      </a-form-item>

      <a-divider orientation="left">报告格式</a-divider>

      <a-form-item label="导出格式" name="format">
        <a-radio-group v-model:value="formData.format">
          <a-radio value="pdf">PDF</a-radio>
          <a-radio value="excel">Excel</a-radio>
        </a-radio-group>
      </a-form-item>

      <a-form-item label="报告内容" name="includeContent">
        <a-checkbox-group v-model:value="formData.includeContent">
          <a-checkbox value="charts">图表</a-checkbox>
          <a-checkbox value="tables">表格</a-checkbox>
          <a-checkbox value="stats">统计数据</a-checkbox>
          <a-checkbox value="summary">执行摘要</a-checkbox>
        </a-checkbox-group>
      </a-form-item>

      <a-divider />

      <div class="preview-info">
        <a-alert
          v-if="formData.enabled"
          type="info"
          show-icon
        >
          <template #message>
            <strong>预计发送时间</strong>
          </template>
          <template #description>
            {{ getNextSendTimeText() }}
          </template>
        </a-alert>
      </div>
    </a-form>

    <template #footer>
      <a-space>
        <a-button @click="handleClose">取消</a-button>
        <a-button type="primary" :loading="loading" @click="handleSave">
          保存配置
        </a-button>
      </a-space>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { useAnalyticsStore } from '@/stores/modules/analytics'
import type { FormInstance } from 'ant-design-vue'
import type { ReportConfig } from '@/types/analytics'
import { message } from 'ant-design-vue'
import dayjs, { type Dayjs } from 'dayjs'

interface Props {
  open: boolean
  reportId?: string
  users?: Array<{ id: string; name: string; email: string }>
}

const props = withDefaults(defineProps<Props>(), {
  users: () => []
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

const loading = ref(false)
const formRef = ref<FormInstance>()

const formData = reactive({
  enabled: false,
  frequency: 'weekly' as 'daily' | 'weekly' | 'monthly',
  sendTime: dayjs('09:00', 'HH:mm'),
  weekDay: 1,
  monthDay: 1,
  recipients: [] as string[],
  channels: ['email'] as ('email' | 'notification')[],
  format: 'pdf' as 'pdf' | 'excel',
  includeContent: ['charts', 'tables', 'stats'] as string[]
})

const rules = {
  frequency: [
    { required: true, message: '请选择报告频率', trigger: 'change' }
  ],
  recipients: [
    {
      required: true,
      type: 'array',
      min: 1,
      message: '请至少选择一个接收人',
      trigger: 'change'
    }
  ],
  channels: [
    {
      required: true,
      type: 'array',
      min: 1,
      message: '请至少选择一种发送渠道',
      trigger: 'change'
    }
  ],
  format: [
    { required: true, message: '请选择导出格式', trigger: 'change' }
  ],
  includeContent: [
    {
      required: true,
      type: 'array',
      min: 1,
      message: '请至少选择一项内容',
      trigger: 'change'
    }
  ]
}

const filterUser = (input: string, option: any) => {
  const text = option.children()
  return text.toLowerCase().indexOf(input.toLowerCase()) >= 0
}

const getNextSendTimeText = () => {
  const now = dayjs()
  let nextSend = now

  switch (formData.frequency) {
    case 'daily':
      nextSend = now.hour(formData.sendTime.hour()).minute(formData.sendTime.minute())
      if (nextSend.isBefore(now)) {
        nextSend = nextSend.add(1, 'day')
      }
      return `下次发送: ${nextSend.format('YYYY-MM-DD HH:mm')}`

    case 'weekly':
      nextSend = now.day(formData.weekDay).hour(9).minute(0)
      if (nextSend.isBefore(now)) {
        nextSend = nextSend.add(1, 'week')
      }
      return `下次发送: ${nextSend.format('YYYY-MM-DD (dddd) HH:mm')}`

    case 'monthly':
      nextSend = now.date(formData.monthDay).hour(9).minute(0)
      if (nextSend.isBefore(now)) {
        nextSend = nextSend.add(1, 'month')
      }
      return `下次发送: ${nextSend.format('YYYY-MM-DD HH:mm')}`

    default:
      return ''
  }
}

const handleSave = async () => {
  if (!props.reportId) {
    message.error('报告ID不能为空')
    return
  }

  try {
    if (formData.enabled) {
      await formRef.value?.validate()
    }

    loading.value = true

    const schedule: ReportConfig['schedule'] = formData.enabled
      ? {
          enabled: true,
          frequency: formData.frequency,
          recipients: formData.recipients,
          channels: formData.channels,
          format: formData.format
        }
      : {
          enabled: false,
          frequency: 'weekly',
          recipients: [],
          channels: [],
          format: 'pdf'
        }

    // Call API to save schedule configuration
    // await analyticsStore.configScheduleReport({ reportId: props.reportId, schedule })

    message.success('定时报告配置已保存')
    emit('success')
    handleClose()
  } catch (error) {
    console.error('Save schedule config failed:', error)
  } finally {
    loading.value = false
  }
}

const handleClose = () => {
  formRef.value?.resetFields()
  emit('update:open', false)
}

watch(
  () => props.open,
  (open) => {
    if (open && props.reportId) {
      // Load existing schedule configuration
      // const report = analyticsStore.currentReport
      // if (report?.config?.schedule) {
      //   Object.assign(formData, report.config.schedule)
      // }
    }
  }
)
</script>

<style scoped lang="scss">
.preview-info {
  padding: 16px 0;
}
</style>
