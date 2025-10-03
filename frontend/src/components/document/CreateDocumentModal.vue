<template>
  <a-modal
    :visible="visible"
    title="新建文档"
    :confirm-loading="confirmLoading"
    @ok="handleOk"
    @cancel="handleCancel"
    :width="800"
  >
    <a-form
      :model="formState"
      :rules="rules"
      ref="formRef"
      layout="vertical"
    >
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="文档标题" name="title">
            <a-input v-model:value="formState.title" placeholder="请输入文档标题" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="文档类型" name="type">
            <a-select v-model:value="formState.type" placeholder="请选择文档类型">
              <a-select-option value="PRD">需求文档</a-select-option>
              <a-select-option value="Design">设计文档</a-select-option>
              <a-select-option value="API">接口文档</a-select-option>
              <a-select-option value="Test">测试文档</a-select-option>
              <a-select-option value="Other">其他文档</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>
      
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="内容类型" name="contentType">
            <a-select v-model:value="formState.contentType" placeholder="请选择内容类型">
              <a-select-option value="markdown">Markdown</a-select-option>
              <a-select-option value="html">HTML</a-select-option>
              <a-select-option value="rich_text">富文本</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="优先级" name="priority">
            <a-select v-model:value="formState.priority" placeholder="请选择优先级">
              <a-select-option :value="1">低</a-select-option>
              <a-select-option :value="2">中</a-select-option>
              <a-select-option :value="3">高</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>
      
      <a-form-item label="所属项目" name="projectId">
        <a-select v-model:value="formState.projectId" placeholder="请选择项目">
          <a-select-option :value="1">ProManage项目管理系统</a-select-option>
          <a-select-option :value="2">客户关系管理系统</a-select-option>
          <a-select-option :value="3">电商平台</a-select-option>
        </a-select>
      </a-form-item>
      
      <a-form-item label="文档摘要" name="summary">
        <a-textarea 
          v-model:value="formState.summary" 
          placeholder="请输入文档摘要" 
          :rows="3" 
          :maxlength="500"
          show-count
        />
      </a-form-item>
      
      <a-form-item label="标签" name="tags">
        <a-select 
          v-model:value="formState.tags" 
          mode="tags" 
          placeholder="请输入标签，按回车确认"
          style="width: 100%"
        />
      </a-form-item>
      
      <a-form-item label="文档内容" name="content">
        <a-textarea 
          v-model:value="formState.content" 
          placeholder="请输入文档内容" 
          :rows="8"
          :maxlength="10000"
          show-count
        />
      </a-form-item>
      
      <a-form-item name="isTemplate" label="模板">
        <a-checkbox v-model:checked="formState.isTemplate">
          设为模板
        </a-checkbox>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, watch, defineEmits, defineProps } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import { useDocumentStore } from '@/stores/modules/document'
import type { CreateDocumentRequest } from '@/types/document'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible', 'success'])

const documentStore = useDocumentStore()

const formRef = ref<FormInstance>()
const confirmLoading = ref(false)

const formState = reactive<CreateDocumentRequest>({
  title: '',
  content: '',
  contentType: 'markdown',
  summary: '',
  type: 'PRD',
  projectId: 1,
  priority: 2,
  isTemplate: false,
  tags: []
})

const rules = {
  title: [
    { required: true, message: '请输入文档标题' },
    { max: 200, message: '文档标题长度不能超过200个字符' }
  ],
  content: [
    { required: true, message: '请输入文档内容' }
  ],
  type: [
    { required: true, message: '请选择文档类型' }
  ],
  projectId: [
    { required: true, message: '请选择所属项目' }
  ]
}

const handleOk = async () => {
  try {
    confirmLoading.value = true
    await formRef.value?.validate()
    
    // 调用创建文档接口
    await documentStore.createDocument(formState.projectId!, formState)
    
    message.success('文档创建成功')
    emit('success')
    handleCancel()
  } catch (error) {
    console.error('创建文档失败:', error)
    message.error('文档创建失败')
  } finally {
    confirmLoading.value = false
  }
}

const handleCancel = () => {
  emit('update:visible', false)
  // 重置表单
  formRef.value?.resetFields()
}

// 监听visible变化
watch(() => props.visible, (newVal) => {
  if (!newVal) {
    // 重置表单
    formRef.value?.resetFields()
  }
})
</script>