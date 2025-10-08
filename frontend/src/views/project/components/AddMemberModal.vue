<template>
  <a-modal
    v-model:open="visible"
    title="添加项目成员"
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
      <!-- 用户选择 -->
      <a-form-item label="选择用户" name="userId">
        <a-select
          v-model:value="formData.userId"
          placeholder="请选择要添加的用户"
          show-search
          :filter-option="filterOption"
          :loading="userLoading"
          @search="handleUserSearch"
        >
          <a-select-option
            v-for="user in availableUsers"
            :key="user.id"
            :value="user.id"
          >
            <div class="user-option">
              <a-avatar :size="24">
                {{ user.realName?.charAt(0) || user.username.charAt(0) }}
              </a-avatar>
              <span class="user-option__name">{{ user.realName || user.username }}</span>
              <span class="user-option__email">{{ user.email }}</span>
            </div>
          </a-select-option>
        </a-select>
      </a-form-item>

      <!-- 角色选择 -->
      <a-form-item label="项目角色" name="roleId">
        <a-select v-model:value="formData.roleId" placeholder="请选择角色">
          <a-select-option :value="1">
            <a-tag color="red">项目经理</a-tag>
            <span class="role-desc">负责项目整体管理和决策</span>
          </a-select-option>
          <a-select-option :value="2">
            <a-tag color="blue">开发人员</a-tag>
            <span class="role-desc">参与项目开发工作</span>
          </a-select-option>
          <a-select-option :value="3">
            <a-tag color="green">测试人员</a-tag>
            <span class="role-desc">负责项目测试工作</span>
          </a-select-option>
          <a-select-option :value="4">
            <a-tag color="orange">设计师</a-tag>
            <span class="role-desc">负责UI/UX设计</span>
          </a-select-option>
          <a-select-option :value="5">
            <a-tag color="purple">运维人员</a-tag>
            <span class="role-desc">负责部署和运维</span>
          </a-select-option>
          <a-select-option :value="6">
            <a-tag color="default">访客</a-tag>
            <span class="role-desc">只读权限</span>
          </a-select-option>
        </a-select>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import { addProjectMember } from '@/api/modules/project'

/**
 * 用户类型
 */
interface User {
  id: number
  username: string
  realName?: string
  email?: string
  avatar?: string
}

/**
 * 组件属性
 */
interface Props {
  projectId: number
  open: boolean
  existingMemberIds?: number[]
}

const props = withDefaults(defineProps<Props>(), {
  existingMemberIds: () => []
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
const userLoading = ref(false)
const formRef = ref<FormInstance>()
const availableUsers = ref<User[]>([])

/**
 * 表单数据
 */
const formData = reactive({
  userId: undefined as number | undefined,
  roleId: 2 as number // 默认为开发人员
})

/**
 * 表单验证规则
 */
const rules = {
  userId: [{ required: true, message: '请选择用户', trigger: 'change' }],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

/**
 * 加载可用用户列表
 */
const loadAvailableUsers = async () => {
  try {
    userLoading.value = true
    // TODO: 调用用户列表API,这里使用模拟数据
    // const result = await getUserList()
    // 过滤掉已经是项目成员的用户
    availableUsers.value = [
      { id: 1, username: 'user1', realName: '张三', email: 'zhangsan@example.com' },
      { id: 2, username: 'user2', realName: '李四', email: 'lisi@example.com' },
      { id: 3, username: 'user3', realName: '王五', email: 'wangwu@example.com' },
      { id: 4, username: 'user4', realName: '赵六', email: 'zhaoliu@example.com' }
    ].filter(user => !props.existingMemberIds.includes(user.id))
  } catch (error) {
    console.error('Load users failed:', error)
    message.error('加载用户列表失败')
  } finally {
    userLoading.value = false
  }
}

/**
 * 用户搜索
 */
const handleUserSearch = (value: string) => {
  // TODO: 实现用户搜索
  console.log('Search user:', value)
}

/**
 * 过滤选项
 */
const filterOption = (input: string, option: any) => {
  const user = availableUsers.value.find(u => u.id === option.value)
  if (!user) return false
  
  const searchText = input.toLowerCase()
  return (
    user.username.toLowerCase().includes(searchText) ||
    user.realName?.toLowerCase().includes(searchText) ||
    user.email?.toLowerCase().includes(searchText) ||
    false
  )
}

/**
 * 处理确定
 */
const handleOk = async () => {
  try {
    await formRef.value?.validate()
    
    if (!formData.userId || !formData.roleId) {
      return
    }

    loading.value = true
    await addProjectMember(props.projectId, formData.userId, formData.roleId)
    
    message.success('添加成员成功')
    emit('success')
    visible.value = false
  } catch (error: any) {
    if (error.errorFields) {
      // 表单验证错误
      return
    }
    console.error('Add member failed:', error)
    message.error(error.message || '添加成员失败')
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

/**
 * 重置表单
 */
const resetForm = () => {
  if (formRef.value && typeof formRef.value.resetFields === 'function') {
    formRef.value.resetFields()
  }
  formData.userId = undefined
  formData.roleId = 2
}

/**
 * 监听open属性变化
 */
watch(
  () => props.open,
  (newVal) => {
    visible.value = newVal
    if (newVal) {
      loadAvailableUsers()
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
</script>

<style lang="scss" scoped>
.user-option {
  display: flex;
  align-items: center;
  gap: 8px;

  &__name {
    font-weight: 500;
    color: #262626;
  }

  &__email {
    font-size: 12px;
    color: #8c8c8c;
  }
}

.role-desc {
  margin-left: 8px;
  font-size: 12px;
  color: #8c8c8c;
}

:deep(.ant-select-item-option-content) {
  display: flex;
  align-items: center;
}
</style>

