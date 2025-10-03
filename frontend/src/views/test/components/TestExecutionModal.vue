<template>
  <a-modal
    v-model:open="visible"
    title="执行测试用例"
    width="800px"
    :confirm-loading="submitting"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <a-spin :spinning="loading">
      <div v-if="testCase" class="test-execution">
        <a-alert
          message="测试用例信息"
          :description="`${testCase.title} - ${testCase.description}`"
          type="info"
          show-icon
          style="margin-bottom: 16px"
        />

        <a-form
          ref="formRef"
          :model="formData"
          :label-col="{ span: 6 }"
          :wrapper-col="{ span: 18 }"
        >
          <a-form-item label="测试计划">
            <a-select
              v-model:value="formData.testPlanId"
              placeholder="选择测试计划（可选）"
              allow-clear
            >
              <a-select-option
                v-for="plan in testPlans"
                :key="plan.id"
                :value="plan.id"
              >
                {{ plan.name }}
              </a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="执行环境">
            <a-input
              v-model:value="formData.environment"
              placeholder="如: Windows 10, Chrome 120"
            />
          </a-form-item>

          <a-divider>测试步骤执行</a-divider>

          <div class="test-steps-execution">
            <a-card
              v-for="(step, index) in formData.steps"
              :key="step.stepId"
              size="small"
              :title="`步骤 ${index + 1}`"
              style="margin-bottom: 16px"
            >
              <p><strong>操作:</strong> {{ getOriginalStep(step.stepId)?.description }}</p>
              <p><strong>预期结果:</strong> {{ getOriginalStep(step.stepId)?.expectedResult }}</p>

              <a-form-item label="执行结果" :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }">
                <a-radio-group v-model:value="step.status" button-style="solid">
                  <a-radio-button value="passed">
                    <CheckCircleOutlined style="color: #52c41a" />
                    通过
                  </a-radio-button>
                  <a-radio-button value="failed">
                    <CloseCircleOutlined style="color: #ff4d4f" />
                    失败
                  </a-radio-button>
                  <a-radio-button value="blocked">
                    <StopOutlined style="color: #faad14" />
                    阻塞
                  </a-radio-button>
                </a-radio-group>
              </a-form-item>

              <a-form-item label="实际结果" :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }">
                <a-textarea
                  v-model:value="step.actualResult"
                  placeholder="请输入实际结果"
                  :rows="2"
                  :disabled="!step.status"
                />
              </a-form-item>
            </a-card>
          </div>

          <a-form-item label="整体状态">
            <a-radio-group v-model:value="formData.status" button-style="solid">
              <a-radio-button value="passed">
                <CheckCircleOutlined style="color: #52c41a" />
                通过
              </a-radio-button>
              <a-radio-button value="failed">
                <CloseCircleOutlined style="color: #ff4d4f" />
                失败
              </a-radio-button>
              <a-radio-button value="blocked">
                <StopOutlined style="color: #faad14" />
                阻塞
              </a-radio-button>
              <a-radio-button value="skipped">
                <MinusCircleOutlined style="color: #d9d9d9" />
                跳过
              </a-radio-button>
            </a-radio-group>
          </a-form-item>

          <a-form-item label="执行时长(秒)">
            <a-input-number
              v-model:value="formData.duration"
              :min="0"
              placeholder="执行耗时"
              style="width: 100%"
            />
          </a-form-item>

          <a-form-item label="备注">
            <a-textarea
              v-model:value="formData.notes"
              placeholder="请输入执行备注"
              :rows="3"
            />
          </a-form-item>

          <a-form-item label="附件">
            <a-upload
              v-model:file-list="fileList"
              :before-upload="beforeUpload"
              :custom-request="handleUpload"
              list-type="picture"
              accept="image/*,.pdf,.log"
            >
              <a-button>
                <template #icon><UploadOutlined /></template>
                上传截图/日志
              </a-button>
            </a-upload>
          </a-form-item>

          <a-divider v-if="formData.status === 'failed'">创建缺陷</a-divider>

          <template v-if="formData.status === 'failed'">
            <a-form-item label="创建缺陷" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
              <a-switch v-model:checked="createBugEnabled" />
              <span style="margin-left: 8px; color: #999">失败时可以直接创建缺陷</span>
            </a-form-item>

            <template v-if="createBugEnabled">
              <a-form-item label="缺陷标题" required>
                <a-input
                  v-model:value="bugFormData.title"
                  placeholder="请输入缺陷标题"
                />
              </a-form-item>

              <a-form-item label="缺陷描述" required>
                <a-textarea
                  v-model:value="bugFormData.description"
                  placeholder="请描述缺陷详情"
                  :rows="3"
                />
              </a-form-item>

              <a-row>
                <a-col :span="12">
                  <a-form-item label="严重程度" :label-col="{ span: 12 }" :wrapper-col="{ span: 12 }">
                    <a-select v-model:value="bugFormData.severity">
                      <a-select-option value="BLOCKER">阻塞</a-select-option>
                      <a-select-option value="CRITICAL">严重</a-select-option>
                      <a-select-option value="MAJOR">主要</a-select-option>
                      <a-select-option value="MINOR">次要</a-select-option>
                      <a-select-option value="TRIVIAL">轻微</a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="优先级" :label-col="{ span: 12 }" :wrapper-col="{ span: 12 }">
                    <a-select v-model:value="bugFormData.priority">
                      <a-select-option value="CRITICAL">紧急</a-select-option>
                      <a-select-option value="HIGH">高</a-select-option>
                      <a-select-option value="MEDIUM">中</a-select-option>
                      <a-select-option value="LOW">低</a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
              </a-row>

              <a-form-item label="指派给">
                <a-select
                  v-model:value="bugFormData.assigneeId"
                  placeholder="选择负责人"
                  allow-clear
                >
                  <a-select-option :value="1">张三</a-select-option>
                  <a-select-option :value="2">李四</a-select-option>
                </a-select>
              </a-form-item>
            </template>
          </template>
        </a-form>
      </div>
    </a-spin>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useTestStore } from '@/stores/modules/test'
import { storeToRefs } from 'pinia'
import type { TestCase, TestExecutionData, BugFormData } from '@/types/test'
import type { UploadProps } from 'ant-design-vue'
import {
  CheckCircleOutlined,
  CloseCircleOutlined,
  StopOutlined,
  MinusCircleOutlined,
  UploadOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import * as testApi from '@/api/modules/test'

interface Props {
  visible: boolean
  testCase: TestCase | null
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:visible': [value: boolean]
  'success': []
}>()

const testStore = useTestStore()
const { testPlans } = storeToRefs(testStore)

const formRef = ref()
const loading = ref(false)
const submitting = ref(false)
const createBugEnabled = ref(false)
const fileList = ref<any[]>([])

const formData = ref<Omit<TestExecutionData, 'createBug'>>({
  testCaseId: 0,
  testPlanId: undefined,
  status: 'passed',
  steps: [],
  notes: '',
  duration: undefined,
  environment: ''
})

const bugFormData = ref<BugFormData>({
  title: '',
  description: '',
  severity: 'MAJOR',
  priority: 'MEDIUM',
  projectId: 1,
  stepsToReproduce: []
})

const getOriginalStep = (stepId: number) => {
  return props.testCase?.steps.find(s => s.id === stepId)
}

const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    message.error('文件大小不能超过 10MB!')
    return false
  }
  return true
}

const handleUpload = async (options: any) => {
  try {
    const { file, onSuccess, onError } = options
    const result = await testApi.uploadTestAttachment(file, 'execution')
    onSuccess(result, file)
    message.success('上传成功')
  } catch (error) {
    console.error('Upload failed:', error)
    message.error('上传失败')
    options.onError(error)
  }
}

const handleSubmit = async () => {
  try {
    submitting.value = true

    const executionData: TestExecutionData = {
      ...formData.value,
      createBug: createBugEnabled.value && formData.value.status === 'failed'
        ? bugFormData.value
        : undefined
    }

    await testStore.executeTestCase(executionData)
    emit('success')
  } catch (error) {
    console.error('Submit failed:', error)
  } finally {
    submitting.value = false
  }
}

const handleCancel = () => {
  emit('update:visible', false)
}

const resetForm = () => {
  formData.value = {
    testCaseId: 0,
    testPlanId: undefined,
    status: 'passed',
    steps: [],
    notes: '',
    duration: undefined,
    environment: ''
  }
  bugFormData.value = {
    title: '',
    description: '',
    severity: 'MAJOR',
    priority: 'MEDIUM',
    projectId: 1,
    stepsToReproduce: []
  }
  createBugEnabled.value = false
  fileList.value = []
}

watch(
  () => props.visible,
  async (val) => {
    if (val && props.testCase) {
      resetForm()
      formData.value.testCaseId = props.testCase.id
      formData.value.steps = props.testCase.steps.map(step => ({
        stepId: step.id,
        status: 'passed',
        actualResult: ''
      }))

      if (props.testCase.projectId) {
        bugFormData.value.projectId = props.testCase.projectId
      }

      try {
        await testStore.fetchTestPlans(props.testCase.projectId)
      } catch (error) {
        console.error('Fetch test plans failed:', error)
      }
    }
  }
)

watch(
  () => formData.value.status,
  (newStatus) => {
    if (newStatus !== 'failed') {
      createBugEnabled.value = false
    }
    if (newStatus === 'failed' && props.testCase) {
      bugFormData.value.title = `[${props.testCase.title}] 测试失败`
      bugFormData.value.stepsToReproduce = props.testCase.steps.map(
        s => `${s.stepNumber}. ${s.description}`
      )
    }
  }
)

watch(
  () => formData.value.steps,
  (steps) => {
    const hasFailedStep = steps.some(s => s.status === 'failed')
    const hasBlockedStep = steps.some(s => s.status === 'blocked')

    if (hasFailedStep) {
      formData.value.status = 'failed'
    } else if (hasBlockedStep) {
      formData.value.status = 'blocked'
    } else if (steps.every(s => s.status === 'passed')) {
      formData.value.status = 'passed'
    }
  },
  { deep: true }
)
</script>

<style scoped lang="scss">
.test-execution {
  .test-steps-execution {
    margin: 16px 0;
  }
}
</style>
