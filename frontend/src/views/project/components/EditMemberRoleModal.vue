<template>
  <a-modal
    v-model:open="visible"
    title="编辑成员角色"
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
      <!-- 成员信息 -->
      <a-form-item label="成员">
        <div class="member-info">
          <a-avatar :size="32">
            {{ member?.realName?.charAt(0) || member?.username?.charAt(0) || '?' }}
          </a-avatar>
          <div class="member-info__details">
            <div class="member-info__name">{{ member?.realName || member?.username }}</div>
            <div class="member-info__email">{{ member?.email }}</div>
          </div>
        </div>
      </a-form-item>

      <!-- 当前角色 -->
      <a-form-item label="当前角色">
        <a-tag :color="getRoleColor(member?.roleId)">
          {{ member?.roleName || '未知' }}
        </a-tag>
      </a-form-item>

      <!-- 新角色选择 -->
      <a-form-item label="新角色" name="roleId">
        <a-select v-model:value="formData.roleId" placeholder="请选择新角色">
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
import { updateProjectMemberRole } from '@/api/modules/project'
import type { ProjectMember } from '@/types/project'

/**
 * 组件属性
 */
interface Props {
  projectId: number
  member: ProjectMember | null
  open: boolean
}

const props = defineProps<Props>()

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
  roleId: undefined as number | undefined
})

/**
 * 表单验证规则
 */
const rules = {
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

/**
 * 监听open属性变化
 */
watch(
  () => props.open,
  (newVal) => {
    visible.value = newVal
    if (newVal && props.member) {
      formData.roleId = props.member.roleId
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
 * 获取角色颜色
 */
const getRoleColor = (roleId?: number): string => {
  const colorMap: Record<number, string> = {
    1: 'red',
    2: 'blue',
    3: 'green',
    4: 'orange',
    5: 'purple',
    6: 'default'
  }
  return roleId ? colorMap[roleId] || 'default' : 'default'
}

/**
 * 处理确定
 */
const handleOk = async () => {
  try {
    await formRef.value?.validate()
    
    if (!props.member || !formData.roleId) {
      return
    }

    // 如果角色没有变化,直接关闭
    if (formData.roleId === props.member.roleId) {
      message.info('角色未发生变化')
      visible.value = false
      return
    }

    loading.value = true
    await updateProjectMemberRole(props.projectId, props.member.userId, formData.roleId)
    
    message.success('更新角色成功')
    emit('success')
    visible.value = false
  } catch (error: any) {
    if (error.errorFields) {
      // 表单验证错误
      return
    }
    console.error('Update member role failed:', error)
    message.error(error.message || '更新角色失败')
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
  formData.roleId = undefined
}
</script>

<style lang="scss" scoped>
.member-info {
  display: flex;
  align-items: center;
  gap: 12px;

  &__details {
    flex: 1;
  }

  &__name {
    font-size: 14px;
    font-weight: 500;
    color: #262626;
  }

  &__email {
    font-size: 12px;
    color: #8c8c8c;
    margin-top: 2px;
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

