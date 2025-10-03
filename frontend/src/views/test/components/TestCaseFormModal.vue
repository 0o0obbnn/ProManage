<template>
  <a-modal
    :open="visible"
    :title="isEdit ? '编辑测试用例' : '创建测试用例'"
    width="900px"
    :confirm-loading="submitting"
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
      <a-form-item label="标题" name="title">
        <a-input
          v-model:value="formData.title"
          placeholder="请输入测试用例标题"
          :max-length="200"
        />
      </a-form-item>

      <a-form-item label="描述" name="description">
        <a-textarea
          v-model:value="formData.description"
          placeholder="请输入测试用例描述"
          :rows="3"
          :max-length="1000"
        />
      </a-form-item>

      <a-row>
        <a-col :span="12">
          <a-form-item label="类型" name="type" :label-col="{ span: 8 }" :wrapper-col="{ span: 16 }">
            <a-select v-model:value="formData.type" placeholder="选择类型">
              <a-select-option value="FUNCTIONAL">功能测试</a-select-option>
              <a-select-option value="PERFORMANCE">性能测试</a-select-option>
              <a-select-option value="SECURITY">安全测试</a-select-option>
              <a-select-option value="INTEGRATION">集成测试</a-select-option>
              <a-select-option value="UNIT">单元测试</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="优先级" name="priority" :label-col="{ span: 8 }" :wrapper-col="{ span: 16 }">
            <a-select v-model:value="formData.priority" placeholder="选择优先级">
              <a-select-option value="CRITICAL">紧急</a-select-option>
              <a-select-option value="HIGH">高</a-select-option>
              <a-select-option value="MEDIUM">中</a-select-option>
              <a-select-option value="LOW">低</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>

      <a-row>
        <a-col :span="12">
          <a-form-item label="项目" name="projectId" :label-col="{ span: 8 }" :wrapper-col="{ span: 16 }">
            <a-select
              v-model:value="formData.projectId"
              placeholder="选择项目"
              @change="handleProjectChange"
            >
              <a-select-option :value="1">示例项目</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="模块" name="moduleId" :label-col="{ span: 8 }" :wrapper-col="{ span: 16 }">
            <a-select
              v-model:value="formData.moduleId"
              placeholder="选择模块"
              allow-clear
            >
              <a-select-option
                v-for="module in modules"
                :key="module.id"
                :value="module.id"
              >
                {{ module.name }}
              </a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item label="执行人" name="assigneeId">
        <a-select
          v-model:value="formData.assigneeId"
          placeholder="选择执行人"
          allow-clear
          show-search
          :filter-option="filterOption"
        >
          <a-select-option :value="1">张三</a-select-option>
          <a-select-option :value="2">李四</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="前置条件">
        <a-textarea
          v-model:value="formData.preconditions"
          placeholder="请输入前置条件"
          :rows="2"
          :max-length="500"
        />
      </a-form-item>

      <a-form-item label="测试步骤" required>
        <div class="test-steps">
          <div
            v-for="(step, index) in formData.steps"
            :key="index"
            class="test-step-item"
          >
            <div class="step-header">
              <span class="step-number">步骤 {{ index + 1 }}</span>
              <a-space>
                <a-button
                  type="text"
                  size="small"
                  :disabled="index === 0"
                  @click="moveStepUp(index)"
                >
                  <template #icon><UpOutlined /></template>
                </a-button>
                <a-button
                  type="text"
                  size="small"
                  :disabled="index === formData.steps.length - 1"
                  @click="moveStepDown(index)"
                >
                  <template #icon><DownOutlined /></template>
                </a-button>
                <a-button
                  type="text"
                  size="small"
                  danger
                  :disabled="formData.steps.length === 1"
                  @click="removeStep(index)"
                >
                  <template #icon><DeleteOutlined /></template>
                </a-button>
              </a-space>
            </div>
            <a-input
              v-model:value="step.description"
              placeholder="输入步骤描述"
              style="margin-bottom: 8px"
            />
            <a-input
              v-model:value="step.expectedResult"
              placeholder="输入预期结果"
            />
          </div>
          <a-button type="dashed" block @click="addStep">
            <template #icon><PlusOutlined /></template>
            添加步骤
          </a-button>
        </div>
      </a-form-item>

      <a-form-item label="测试数据">
        <a-textarea
          v-model:value="formData.testData"
          placeholder="请输入测试数据"
          :rows="2"
          :max-length="1000"
        />
      </a-form-item>

      <a-form-item label="标签">
        <a-select
          v-model:value="formData.tags"
          mode="tags"
          placeholder="添加标签"
          :max-tag-count="5"
        >
          <a-select-option v-for="tag in tags" :key="tag" :value="tag">
            {{ tag }}
          </a-select-option>
        </a-select>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useTestStore } from '@/stores/modules/test'
import { storeToRefs } from 'pinia'
import type { TestCase, TestCaseFormData, TestStep } from '@/types/test'
import type { Rule } from 'ant-design-vue/es/form'
import {
  PlusOutlined,
  DeleteOutlined,
  UpOutlined,
  DownOutlined
} from '@ant-design/icons-vue'
import { TestCaseType, TestCasePriority } from '@/types/test'

interface Props {
  visible: boolean
  testCase?: TestCase | null
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:visible': [value: boolean]
  'success': []
}>()

const testStore = useTestStore()
const { modules, tags } = storeToRefs(testStore)

const formRef = ref()
const submitting = ref(false)

const isEdit = computed(() => !!props.testCase)

const formData = ref<TestCaseFormData>({
  title: '',
  description: '',
  type: TestCaseType.FUNCTIONAL,
  priority: TestCasePriority.MEDIUM,
  projectId: 1,
  moduleId: undefined,
  preconditions: '',
  steps: [
    {
      stepNumber: 1,
      description: '',
      expectedResult: ''
    }
  ],
  testData: '',
  assigneeId: undefined,
  tags: []
})

const rules: Record<string, Rule[]> = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 5, max: 200, message: '标题长度在 5 到 200 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入描述', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择类型', trigger: 'change' }
  ],
  priority: [
    { required: true, message: '请选择优先级', trigger: 'change' }
  ],
  projectId: [
    { required: true, message: '请选择项目', trigger: 'change' }
  ]
}

const addStep = () => {
  formData.value.steps.push({
    stepNumber: formData.value.steps.length + 1,
    description: '',
    expectedResult: ''
  })
}

const removeStep = (index: number) => {
  formData.value.steps.splice(index, 1)
  formData.value.steps.forEach((step, idx) => {
    step.stepNumber = idx + 1
  })
}

const moveStepUp = (index: number) => {
  if (index === 0) return
  const steps = formData.value.steps
  const temp = steps[index]
  steps[index] = steps[index - 1]!
  steps[index - 1] = temp!
  steps.forEach((step, idx) => {
    step.stepNumber = idx + 1
  })
}

const moveStepDown = (index: number) => {
  const steps = formData.value.steps
  if (index === steps.length - 1) return
  const temp = steps[index]
  steps[index] = steps[index + 1]!
  steps[index + 1] = temp!
  steps.forEach((step, idx) => {
    step.stepNumber = idx + 1
  })
}

const handleProjectChange = async (projectId: number) => {
  formData.value.moduleId = undefined
  try {
    await testStore.fetchModules(projectId)
  } catch (error) {
    console.error('Fetch modules failed:', error)
  }
}

const filterOption = (input: string, option: any) => {
  return option.children[0].children.toLowerCase().indexOf(input.toLowerCase()) >= 0
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true

    if (isEdit.value && props.testCase) {
      await testStore.updateTestCase(props.testCase.id, formData.value)
    } else {
      await testStore.createTestCase(formData.value)
    }

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
    title: '',
    description: '',
    type: TestCaseType.FUNCTIONAL,
    priority: TestCasePriority.MEDIUM,
    projectId: 1,
    moduleId: undefined,
    preconditions: '',
    steps: [
      {
        stepNumber: 1,
        description: '',
        expectedResult: ''
      }
    ],
    testData: '',
    assigneeId: undefined,
    tags: []
  }
  formRef.value?.clearValidate()
}

watch(
  () => props.visible,
  (val) => {
    if (val) {
      if (props.testCase) {
        formData.value = {
          title: props.testCase.title,
          description: props.testCase.description,
          type: props.testCase.type,
          priority: props.testCase.priority,
          projectId: props.testCase.projectId,
          moduleId: props.testCase.moduleId,
          preconditions: props.testCase.preconditions,
          steps: props.testCase.steps.map(s => ({
            stepNumber: s.stepNumber,
            description: s.description,
            expectedResult: s.expectedResult
          })),
          testData: props.testCase.testData,
          assigneeId: props.testCase.assignee?.id,
          tags: props.testCase.tags
        }
        if (props.testCase.projectId) {
          testStore.fetchModules(props.testCase.projectId)
        }
      } else {
        resetForm()
      }
      testStore.fetchTags()
    }
  }
)
</script>

<style scoped lang="scss">
.test-steps {
  .test-step-item {
    padding: 12px;
    margin-bottom: 12px;
    background: #fafafa;
    border: 1px solid #d9d9d9;
    border-radius: 4px;

    .step-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      .step-number {
        font-weight: 600;
        color: #1890ff;
      }
    }
  }
}
</style>
