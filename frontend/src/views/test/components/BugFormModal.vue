<template>
  <a-modal
    :open="visible"
    :title="isEdit ? '编辑缺陷' : '创建缺陷'"
    width="800px"
    :confirm-loading="submitting"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      :label-col="{ span: 5 }"
      :wrapper-col="{ span: 19 }"
    >
      <a-form-item label="标题" name="title">
        <a-input
          v-model:value="formData.title"
          placeholder="请输入缺陷标题"
          :max-length="200"
        />
      </a-form-item>

      <a-form-item label="描述" name="description">
        <a-textarea
          v-model:value="formData.description"
          placeholder="请详细描述缺陷"
          :rows="4"
          :max-length="2000"
        />
      </a-form-item>

      <a-row>
        <a-col :span="12">
          <a-form-item label="严重程度" name="severity" :label-col="{ span: 10 }" :wrapper-col="{ span: 14 }">
            <a-select v-model:value="formData.severity" placeholder="选择严重程度">
              <a-select-option value="BLOCKER">
                <a-tag color="red">阻塞</a-tag>
              </a-select-option>
              <a-select-option value="CRITICAL">
                <a-tag color="red">严重</a-tag>
              </a-select-option>
              <a-select-option value="MAJOR">
                <a-tag color="orange">主要</a-tag>
              </a-select-option>
              <a-select-option value="MINOR">
                <a-tag color="blue">次要</a-tag>
              </a-select-option>
              <a-select-option value="TRIVIAL">
                <a-tag>轻微</a-tag>
              </a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="优先级" name="priority" :label-col="{ span: 10 }" :wrapper-col="{ span: 14 }">
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
          <a-form-item label="项目" name="projectId" :label-col="{ span: 10 }" :wrapper-col="{ span: 14 }">
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
          <a-form-item label="模块" name="moduleId" :label-col="{ span: 10 }" :wrapper-col="{ span: 14 }">
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

      <a-form-item label="负责人" name="assigneeId">
        <a-select
          v-model:value="formData.assigneeId"
          placeholder="选择负责人"
          allow-clear
          show-search
          :filter-option="filterOption"
        >
          <a-select-option :value="1">张三</a-select-option>
          <a-select-option :value="2">李四</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="测试环境">
        <a-input
          v-model:value="formData.environment"
          placeholder="如: Windows 10, Chrome 120"
        />
      </a-form-item>

      <a-form-item label="重现步骤" required>
        <div class="reproduce-steps">
          <div
            v-for="(step, index) in formData.stepsToReproduce"
            :key="index"
            class="step-item"
          >
            <a-input
              v-model:value="formData.stepsToReproduce[index]"
              :placeholder="`步骤 ${index + 1}`"
            >
              <template #addonAfter>
                <a-button
                  type="text"
                  size="small"
                  danger
                  :disabled="formData.stepsToReproduce.length === 1"
                  @click="removeStep(index)"
                >
                  <template #icon><DeleteOutlined /></template>
                </a-button>
              </template>
            </a-input>
          </div>
          <a-button type="dashed" block @click="addStep">
            <template #icon><PlusOutlined /></template>
            添加步骤
          </a-button>
        </div>
      </a-form-item>

      <a-form-item label="预期行为">
        <a-textarea
          v-model:value="formData.expectedBehavior"
          placeholder="描述正常情况下应该出现的结果"
          :rows="2"
        />
      </a-form-item>

      <a-form-item label="实际行为">
        <a-textarea
          v-model:value="formData.actualBehavior"
          placeholder="描述实际出现的异常行为"
          :rows="2"
        />
      </a-form-item>

      <a-form-item label="关联测试用例">
        <a-select
          v-model:value="formData.linkedTestCases"
          mode="multiple"
          placeholder="选择关联的测试用例"
          allow-clear
          show-search
          :filter-option="filterTestCaseOption"
        >
          <a-select-option :value="1">TC-001: 登录功能测试</a-select-option>
          <a-select-option :value="2">TC-002: 注册功能测试</a-select-option>
        </a-select>
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

      <a-form-item label="附件">
        <a-upload
          v-model:file-list="fileList"
          :before-upload="beforeUpload"
          :custom-request="handleUpload"
          list-type="picture"
          accept="image/*,.pdf,.log,.txt"
        >
          <a-button>
            <template #icon><UploadOutlined /></template>
            上传截图/日志
          </a-button>
          <div style="margin-top: 8px; color: #999; font-size: 12px">
            支持格式: 图片、PDF、日志文件，单个文件不超过 10MB
          </div>
        </a-upload>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useTestStore } from '@/stores/modules/test'
import { storeToRefs } from 'pinia'
import type { Bug, BugFormData } from '@/types/test'
import type { Rule } from 'ant-design-vue/es/form'
import type { UploadProps } from 'ant-design-vue'
import {
  PlusOutlined,
  DeleteOutlined,
  UploadOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import * as testApi from '@/api/modules/test'
import { BugSeverity, TestCasePriority } from '@/types/test'

interface Props {
  visible: boolean
  bug?: Bug | null
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
const fileList = ref<any[]>([])

const isEdit = computed(() => !!props.bug)

const formData = ref<BugFormData>({
  title: '',
  description: '',
  severity: BugSeverity.MAJOR,
  priority: TestCasePriority.MEDIUM,
  projectId: 1,
  moduleId: undefined,
  environment: '',
  stepsToReproduce: [''],
  expectedBehavior: '',
  actualBehavior: '',
  assigneeId: undefined,
  tags: [],
  linkedTestCases: []
})

const rules: Record<string, Rule[]> = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 5, max: 200, message: '标题长度在 5 到 200 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入描述', trigger: 'blur' },
    { min: 10, message: '描述至少 10 个字符', trigger: 'blur' }
  ],
  severity: [
    { required: true, message: '请选择严重程度', trigger: 'change' }
  ],
  priority: [
    { required: true, message: '请选择优先级', trigger: 'change' }
  ],
  projectId: [
    { required: true, message: '请选择项目', trigger: 'change' }
  ]
}

const addStep = () => {
  formData.value.stepsToReproduce.push('')
}

const removeStep = (index: number) => {
  formData.value.stepsToReproduce.splice(index, 1)
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

const filterTestCaseOption = (input: string, option: any) => {
  return option.children[0].children.toLowerCase().indexOf(input.toLowerCase()) >= 0
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
    const result = await testApi.uploadTestAttachment(file, 'bug')
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
    await formRef.value.validate()
    submitting.value = true

    const submitData = {
      ...formData.value,
      stepsToReproduce: formData.value.stepsToReproduce.filter(s => s.trim())
    }

    if (isEdit.value && props.bug) {
      await testStore.updateBug(props.bug.id, submitData)
    } else {
      await testStore.createBug(submitData)
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
    severity: BugSeverity.MAJOR,
    priority: TestCasePriority.MEDIUM,
    projectId: 1,
    moduleId: undefined,
    environment: '',
    stepsToReproduce: [''],
    expectedBehavior: '',
    actualBehavior: '',
    assigneeId: undefined,
    tags: [],
    linkedTestCases: []
  }
  fileList.value = []
  formRef.value?.clearValidate()
}

watch(
  () => props.visible,
  (val) => {
    if (val) {
      if (props.bug) {
        formData.value = {
          title: props.bug.title,
          description: props.bug.description,
          severity: props.bug.severity,
          priority: props.bug.priority,
          projectId: props.bug.projectId,
          moduleId: props.bug.moduleId,
          environment: props.bug.environment,
          stepsToReproduce: props.bug.stepsToReproduce.length > 0
            ? props.bug.stepsToReproduce
            : [''],
          expectedBehavior: props.bug.expectedBehavior,
          actualBehavior: props.bug.actualBehavior,
          assigneeId: props.bug.assignee?.id,
          tags: props.bug.tags,
          linkedTestCases: props.bug.linkedTestCases
        }
        if (props.bug.projectId) {
          testStore.fetchModules(props.bug.projectId)
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
.reproduce-steps {
  .step-item {
    margin-bottom: 12px;
  }
}
</style>
