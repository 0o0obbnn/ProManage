<template>
  <a-modal
    v-model:open="visible"
    :title="isEdit ? '编辑文件夹' : '新建文件夹'"
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
      <a-form-item label="文件夹名称" name="name">
        <a-input
          v-model:value="formData.name"
          placeholder="请输入文件夹名称"
          :maxlength="50"
          show-count
        />
      </a-form-item>

      <a-form-item label="父文件夹" name="parentId">
        <a-tree-select
          v-model:value="formData.parentId"
          :tree-data="folderTreeData"
          :field-names="{ label: 'name', value: 'id', children: 'children' }"
          placeholder="选择父文件夹(可选)"
          allow-clear
          tree-default-expand-all
        />
      </a-form-item>

      <a-form-item v-if="!isEdit" label="所属项目" name="projectId">
        <a-select
          v-model:value="formData.projectId"
          placeholder="请选择项目"
          show-search
          :filter-option="filterOption"
        >
          <a-select-option
            v-for="project in projects"
            :key="project.id"
            :value="project.id"
          >
            {{ project.name }}
          </a-select-option>
        </a-select>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import { createFolder, updateFolder } from '@/api/modules/document'
import type { DocumentFolder } from '@/types/document'

/**
 * 组件属性
 */
interface Props {
  open: boolean
  folder?: DocumentFolder | null
  folders?: DocumentFolder[]
  projects?: any[]
}

const props = withDefaults(defineProps<Props>(), {
  folder: null,
  folders: () => [],
  projects: () => []
})

/**
 * 组件事件
 */
interface Emits {
  (e: 'update:open', value: boolean): void
  (e: 'success'): void
}

const emit = defineEmits<Emits>()

// 响应式数据
const visible = ref(false)
const loading = ref(false)
const formRef = ref<FormInstance>()

/**
 * 表单数据
 */
const formData = reactive({
  name: '',
  parentId: undefined as number | undefined,
  projectId: undefined as number | undefined
})

/**
 * 是否为编辑模式
 */
const isEdit = computed(() => !!props.folder)

/**
 * 文件夹树数据(排除当前文件夹及其子文件夹)
 */
const folderTreeData = computed(() => {
  if (!isEdit.value) {
    return props.folders
  }
  
  // 编辑时,排除当前文件夹及其子文件夹
  const excludeIds = new Set<number>()
  const collectIds = (folder: DocumentFolder) => {
    excludeIds.add(folder.id)
    if (folder.children) {
      folder.children.forEach(collectIds)
    }
  }
  
  if (props.folder) {
    collectIds(props.folder)
  }
  
  const filterFolders = (folders: DocumentFolder[]): DocumentFolder[] => {
    return folders
      .filter(f => !excludeIds.has(f.id))
      .map(f => ({
        ...f,
        children: f.children ? filterFolders(f.children) : undefined
      }))
  }
  
  return filterFolders(props.folders)
})

/**
 * 表单验证规则
 */
const rules = {
  name: [
    { required: true, message: '请输入文件夹名称', trigger: 'blur' },
    { min: 1, max: 50, message: '文件夹名称长度在1-50个字符之间', trigger: 'blur' },
    {
      validator: (_rule: any, value: string) => {
        if (!value) return Promise.resolve()
        // 禁止特殊字符
        if (/[<>:"/\\|?*]/.test(value)) {
          return Promise.reject('文件夹名称不能包含特殊字符 < > : " / \\ | ? *')
        }
        return Promise.resolve()
      },
      trigger: 'blur'
    }
  ],
  projectId: [
    { required: true, message: '请选择项目', trigger: 'change' }
  ]
}

/**
 * 项目筛选
 */
const filterOption = (input: string, option: any) => {
  return option.children.toLowerCase().includes(input.toLowerCase())
}

/**
 * 重置表单
 */
const resetForm = () => {
  formData.name = ''
  formData.parentId = undefined
  formData.projectId = undefined
  formRef.value?.resetFields()
}

/**
 * 监听open属性变化
 */
watch(
  () => props.open,
  (newVal) => {
    visible.value = newVal
    if (newVal) {
      if (props.folder) {
        // 编辑模式,填充数据
        formData.name = props.folder.name
        formData.parentId = props.folder.parentId
        formData.projectId = props.folder.projectId
      } else {
        // 新建模式,重置表单
        resetForm()
      }
    }
  },
  { immediate: true }
)

/**
 * 监听visible变化
 */
watch(visible, (newVal) => {
  emit('update:open', newVal)
  if (!newVal) {
    resetForm()
  }
})

/**
 * 处理确定
 */
const handleOk = async () => {
  try {
    await formRef.value?.validate()
    
    loading.value = true
    
    if (isEdit.value && props.folder) {
      // 编辑文件夹
      await updateFolder(props.folder.id, {
        name: formData.name
      })
      message.success('编辑文件夹成功')
    } else {
      // 创建文件夹
      if (!formData.projectId) {
        message.error('请选择项目')
        return
      }
      
      await createFolder({
        name: formData.name,
        parentId: formData.parentId,
        projectId: formData.projectId
      })
      message.success('创建文件夹成功')
    }
    
    emit('success')
    visible.value = false
  } catch (error: any) {
    if (error.errorFields) {
      // 表单验证错误
      return
    }
    console.error('Folder operation failed:', error)
    message.error(error.message || '操作失败')
  } finally {
    loading.value = false
  }
}

/**
 * 处理取消
 */
const handleCancel = () => {
  visible.value = false
}
</script>

<style scoped lang="scss">
// 样式可以根据需要添加
</style>

